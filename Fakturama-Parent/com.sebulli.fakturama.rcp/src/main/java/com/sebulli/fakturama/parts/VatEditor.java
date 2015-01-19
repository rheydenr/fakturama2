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

import java.sql.SQLException;
import java.util.Comparator;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.PercentFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.VAT_;
import com.sebulli.fakturama.parts.converter.CommonConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.parts.converter.CategoryConverter;

/**
 * The VAT editor
 */
public class VatEditor extends Editor<VAT> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected VatsDAO vatDao;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    protected VatCategoriesDAO vatCategoriesDAO;
    
    @Inject
    protected ILogger log;

    // Editor's ID
    public static final String ID = "com.sebulli.fakturama.editors.vatEditor";

    // SWT widgets of the editor
    private Composite top;
    private Text textName;
    private Text textDescription;
    private FormattedText textValue;
    private Combo comboCategory;

    // defines if the vat is just created
    private boolean newVat;

    /**
     * This field can't be injected since the part is created from
     * a PartDescriptor (see createPartControl).
     */
    private MPart part;

    // This UniDataSet represents the editor's input 
    private VAT editorVat = null;

    /**
     * Saves the contents of this part
     * 
     * @param monitor
     *            Progress monitor
     */
    @Persist
    public void doSave(IProgressMonitor monitor) {

        /*
         * the following parameters are not saved: 
         * - id (constant)
         */

        // Always set the editor's data to "undeleted"
        editorVat.setDeleted(Boolean.FALSE);

   		// Set the VAT data
        // ... done through databinding...

   		// save the new or updated VAT
        try {
            // at first, check the category for a new entry
            // (the user could have written a new one into the combo field)
            String testCat = comboCategory.getText();
            // if there's no category we can skip this step
            if(StringUtils.isNotBlank(testCat)) {
                VATCategory parentCategory = vatCategoriesDAO.getCategory(testCat, true);
                // parentCategory now has the last found Category
                editorVat.setCategory(parentCategory);
            }
            
            /*
             * If we DON'T use update, the category is saved again and again and again
             * because we have CascadeType.PERSIST. If we use update and save the new VatCategory before,
             * all went ok. That's the point...
             */
            editorVat = vatDao.update(editorVat);
        }
        catch (SQLException e) {
            log.error(e, "can't save the current VAT: " + editorVat.toString());
        }

        if (newVat) {
			newVat = false;
			stdComposite.stdButton.setEnabled(true);
    	}

       	// Set the Editor's name to the payment name.
        part.setLabel(editorVat.getName());
        
		// Refresh the table view of all VATs (this also refreshes the tree of categories)
        evtBroker.post("VatEditor", "update");
        
        // reset dirty flag
		getMDirtyablePart().setDirty(false);
    }

    /**
     * Initializes the editor. If an existing data set is opened, the local
     * variable "vat" is set to this data set. If the editor is opened to create
     * a new one, a new data set is created and the local variable "vat" is set
     * to this one.<br />
     * If we get an ID from the opening command we try to open the given
     * {@link VAT}.
     * 
     * @param parent
     *            the parent control
     */
    @PostConstruct
    public void createPartControl(Composite parent) {
        Long objId = null;
        VAT stdVat = null;
        long stdID = 1L;
        this.part = (MPart) parent.getData("modelElement");
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
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
            // Set the Editor's name to the VAT name.
            part.setLabel(editorVat.getName());
        }

        // Create the top Composite
        top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
        
		// Add context help reference 
        //		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.VAT_EDITOR);

        // Large VAT label
        Label labelTitle = new Label(top, SWT.NONE);
        labelTitle.setText(msg.editorVatTitle);
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).applyTo(labelTitle);
        makeLargeLabel(labelTitle);

        // Name of the VAT
        Label labelName = new Label(top, SWT.NONE);
        labelName.setText(msg.commonFieldName);
        labelName.setToolTipText(msg.editorVatNameTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
        textName = new Text(top, SWT.BORDER);
        textName.setToolTipText(labelName.getToolTipText());
        bindModelValue(editorVat, textName, "name", 64);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);

        // Category of the VAT
        Label labelCategory = new Label(top, SWT.NONE);
        labelCategory.setText(msg.commonFieldCategory);
        labelCategory.setToolTipText(msg.editorVatCategoryTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

        createCategoryCombo();
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);

        // The description
        Label labelDescription = new Label(top, SWT.NONE);
        labelDescription.setText(msg.commonFieldDescription);
        labelDescription.setToolTipText(msg.editorVatDescriptionTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDescription);
        textDescription = new Text(top, SWT.BORDER);
        textDescription.setToolTipText(labelDescription.getToolTipText());
        bindModelValue(editorVat, textDescription, VAT_.description.getName(), 250);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textDescription);

        // The value
        Label labelValue = new Label(top, SWT.NONE);
        labelValue.setText(msg.commonFieldValue);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelValue);

        textValue = new FormattedText(top, SWT.BORDER | SWT.SINGLE);
        textValue.setFormatter(new PercentFormatter());
        GridData data = new GridData();
        data.widthHint = 200;
        textValue.getControl().setLayoutData(data);
        bindModelValue(editorVat, textValue, VAT_.taxValue.getName(), 16);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textValue.getControl());

        // Create the composite to make this VAT to the standard VAT. 
        Label labelStdVat = new Label(top, SWT.NONE);
        labelStdVat.setText(msg.commonLabelDefault);
        labelStdVat.setToolTipText(msg.editorVatNameTooltip);

        // Get the ID of the standard entity from preferences
        try {
            stdID = defaultValuePrefs.getLong(getDefaultEntryKey(), 1L);
        } catch (NumberFormatException | NullPointerException e) {
            stdID = 1L;
        } finally {
            stdVat = vatDao.findById(stdID);
        }

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelStdVat);
        //T: VAT Editor: Button description to make this as standard VAT.
        stdComposite = new StdComposite(top, editorVat, stdVat, msg.editorVatDefaultbutton, 1);
        stdComposite.setToolTipText(msg.editorVatDefaultbuttonTooltip);

        // Disable the Standard Button if this is a new VAT;
        // the Button is disabled by default, see Editor.StdComposite
        if (!newVat) {
            stdComposite.stdButton.setEnabled(true);
        }
    }

    /**
     * creates the combo box for the VAT category
     */
    private void createCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<VATCategory> categories = new TreeSet<VATCategory>(new Comparator<VATCategory>() {
            @Override
            public int compare(VATCategory cat1, VATCategory cat2) {
                return cat1.getName().compareTo(cat2.getName());
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
                return element instanceof VATCategory ? CommonConverter.getCategoryName((VATCategory)element, "") : null;
            }
        });

        UpdateValueStrategy vatCatModel2Target = new UpdateValueStrategy();
        vatCatModel2Target.setConverter(new CategoryConverter<VATCategory>(VATCategory.class));
        
        UpdateValueStrategy target2VatcatModel = new UpdateValueStrategy();
        target2VatcatModel.setConverter(new StringToCategoryConverter<VATCategory>(categories, VATCategory.class));
        bindModelValue(editorVat, comboCategory, VAT_.category.getName(), target2VatcatModel, vatCatModel2Target);
    }
    
    @Inject
    @Optional
    public void partActivation(@UIEventTopic(UIEvents.UILifeCycle.BRINGTOTOP) 
      Event event) {
      // do something
//      System.out.println("Got Part");
    }     
    
    @Override
    protected String getDefaultEntryKey() {
        return Constants.DEFAULT_VAT;
    }
    
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }
}
