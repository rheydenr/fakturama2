/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.parts;

import java.util.Comparator;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.text.NumberFormat;
import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;

/**
 * The VAT editor
 * 
 * @author Ralf Heydenreich
 */
public class VatEditor extends Editor<VAT> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected VatsDAO vatDao;

    @Inject
    protected VatCategoriesDAO vatCategoriesDAO;

    // Editor's ID
    public static final String ID = "com.sebulli.fakturama.editors.vatEditor";

    // SWT widgets of the editor
    private Composite top;
    private Text textName;
    private Text textDescription;
    private Text textValue;
    private Combo comboCategory;

    // defines, if the vat is just created
    private boolean newVat;

    /*
     * This field can't be injected since the part is created from
     * a PartDescriptor (see createPartControl).
     */
    private MPart part;

    // This UniDataSet represents the editor's input 
    private VAT editorVat = null;

    //	/**
    //	 * Constructor
    //	 * 
    //	 * Associate the table view with the editor
    //	 */
    //	public VatEditor() {
    //		tableViewID = ViewVatTable.ID;
    //		editorID = "vat";
    //	}

    /**
     * Saves the contents of this part
     * 
     * @param monitor
     *            Progress monitor
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    //	@Override
    @Persist
    public void doSave(IProgressMonitor monitor) {

        /*
         * the following parameters are not saved: 
         * - id (constant)
         */

        // Always set the editor's data to "undeleted"
        editorVat.setDeleted(Boolean.FALSE);

        //		// Set the VAT data
        //		vat.setStringValueByKey("name", textName.getText());
        //		vat.setStringValueByKey("category", comboCategory.getText());
        //		vat.setStringValueByKey("description", textDescription.getText());
        //		vat.setDoubleValueByKey("value", DataUtils.StringToDouble(textValue.getText() + "%"));
        //
        //		// If it is a new VAT, add it to the VAT list and
        //		// to the data base
        //		if (newVat) {
        //			vat = Data.INSTANCE.getVATs().addNewDataSet(vat);
        //			newVat = false;
        //			stdComposite.stdButton.setEnabled(true);
        //		}
        //		// If it's not new, update at least the data base
        //		else {
        //			Data.INSTANCE.getVATs().updateDataSet(vat);
        //		}
        //
        		// Set the Editor's name to the payment name.
        part.setLabel(editorVat.getName());
        
        		// Refresh the table view of all payments
        		refreshView();
        		getMDirtyablePart().setDirty(false);
    }

    /**
     * Returns whether the "Save As" operation is supported by this part.
     * 
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     * @return False, SaveAs is not allowed
     */
    //	@Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * Initializes the editor. If an existing data set is opened, the local
     * variable "vat" is set to this data set. If the editor is opened to create
     * a new one, a new data set is created and the local variable "vat" is set
     * to this one.
     * 
     * 
     * @param parent
     *            the parent control
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @PostConstruct
    public void createPartControl(Composite parent) {
        Long objId = null;
        this.part = (MPart) parent.getData("modelElement");
        String tmpObjId = (String) part.getContext().get("com.sebulli.fakturama.rcp.editor.objId");
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            editorVat = vatDao.findById(objId);
        }

        // test, if the editor is opened to create a new data set. This is,
        // if there is no input set.
        newVat = (editorVat == null);

        // If new ..
        if (newVat) {

            // Create a new data set
            editorVat = new VAT();

            //T: VAT Editor: Part Name of a new VAT Entry
            part.setLabel(msg.editorVatHeader);
        }
        else {

            // Set the Editor's name to the payment name.
            part.setLabel(editorVat.getName());
        }

        // Create the top Composite
        top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
        //
        //		// Add context help reference 
        //		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.VAT_EDITOR);

        // Large VAT label
        Label labelTitle = new Label(top, SWT.NONE);
        //T: VAT Editor: Title VAT Entry
        labelTitle.setText(msg.editorVatTitle);
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).applyTo(labelTitle);
        makeLargeLabel(labelTitle);

        // Name of the VAT
        Label labelName = new Label(top, SWT.NONE);
        labelName.setText(msg.commonFieldName);
        //T: Tool Tip Text
        labelName.setToolTipText(msg.editorVatNameTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
        textName = new Text(top, SWT.BORDER);
        //		textName.setText(editorVat.getName());
        textName.setToolTipText(labelName.getToolTipText());
        bindModelValue(editorVat, textName, "name", 64);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);

        //		org.eclipse.core.internal.databinding.provisional.bind.Bind.twoWay(editorVat);

        // Category of the VAT
        Label labelCategory = new Label(top, SWT.NONE);
        labelCategory.setText(msg.commonFieldCategory);
        //T: Tool Tip Text
        labelCategory.setToolTipText(msg.editorVatCategoryTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

        // Collect all category strings
        final TreeSet<VATCategory> categories = new TreeSet<VATCategory>(new Comparator<VATCategory>() {
            @Override
            public int compare(VATCategory o1, VATCategory o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        categories.addAll(vatCategoriesDAO.findAll());

        comboCategory = new Combo(top, SWT.BORDER);
        ComboViewer viewer = new ComboViewer(comboCategory);
        viewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return categories.toArray();
            }
        });
        
        // Add all categories to the combo
        viewer.setInput(categories);
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element instanceof VATCategory ? ((VATCategory)element).getName() : null;
            }
        });

//        DataBindingContext dbc = new DataBindingContext();
//        ViewerSupport.bind(viewer, BeansObservables.observeSet(editorVat,
//                "category", VATCategory.class), Properties.selfValue(VATCategory.class));
//        dbc.bindValue(ViewersObservables.observeSingleSelection(viewer),
//                BeansObservables.observeValue(editorVat, "category"));
      

        IObservableValue widgetValue = WidgetProperties.selection().observe(comboCategory);
        IObservableValue modelValue = BeanProperties.value("category").observe(editorVat);
        UpdateValueStrategy vatCatModel2Target = new UpdateValueStrategy();
        vatCatModel2Target.setConverter(new VATCategoryConverter());
        
        UpdateValueStrategy target2VatcatModel = new UpdateValueStrategy();
        target2VatcatModel.setConverter(new StringToVatCategoryConverter());
        ctx.bindValue(widgetValue, modelValue, target2VatcatModel, vatCatModel2Target);
        
        
        
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);

        // The description
        Label labelDescription = new Label(top, SWT.NONE);
        labelDescription.setText(msg.commonFieldDescription);
        //T: Tool Tip Text
        labelDescription.setToolTipText(msg.editorVatDescriptionTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDescription);
        textDescription = new Text(top, SWT.BORDER);
        //		textDescription.setText(editorVat.getDescription());
        textDescription.setToolTipText(labelDescription.getToolTipText());
        bindModelValue(editorVat, textDescription, "description", 250);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textDescription);

        // The value
        Label labelValue = new Label(top, SWT.NONE);
        labelValue.setText(msg.commonFieldValue);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelValue);
        textValue = new Text(top, SWT.BORDER);
        // create UpdateValueStrategy and assign it to the binding
        UpdateValueStrategy modelToTargetStrategy = new UpdateValueStrategy();
        NumberFormat percentNumberFormat = NumberFormat.getPercentInstance();
        percentNumberFormat.setMinimumFractionDigits(2);
        NumberToStringConverter model2TargetConverter = NumberToStringConverter.fromDouble(percentNumberFormat, false);
        StringToNumberConverter target2ModelConverter = StringToNumberConverter.toDouble(percentNumberFormat, false);
        UpdateValueStrategy targetToModelStrategy = new UpdateValueStrategy();
        targetToModelStrategy.setConverter(target2ModelConverter);
        modelToTargetStrategy.setConverter(model2TargetConverter);

        //		textValue.setText(DataUtils.DoubleToFormatedPercent(editorVat.getTaxValue()));
        bindModelValue(editorVat, textValue, "taxValue", 16, targetToModelStrategy, modelToTargetStrategy);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textValue);

        // Create the composite to make this payment to the standard payment. 
        Label labelStdVat = new Label(top, SWT.NONE);
        labelStdVat.setText(msg.commonLabelDefault);
        //T: Tool Tip Text
        labelStdVat.setToolTipText(msg.editorVatNameTooltip);

        VAT stdVat = null;
        int stdID = 0;

        // Get the ID of the standard unidataset
        try {
            // TODO check if 1 is a valid default ID
            stdID = defaultValuePrefs.getInt("standardvat", 1);
            stdVat = vatDao.findById(stdID);
        }
        catch (NumberFormatException | NullPointerException e) {
            stdID = 0;
        }

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelStdVat);
        //T: VAT Editor: Button description to make this as standard VAT.
        stdComposite = new StdComposite(top, editorVat, stdVat, msg.editorVatDefaultbutton, 1);
        //T: Tool Tip Text
        stdComposite.setToolTipText(msg.editorVatDefaultbuttonTooltip);

        // Disable the Standard Button, if this is a new VAT
        if (!newVat) {
            stdComposite.stdButton.setEnabled(true);
        }
    }

    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }

}
