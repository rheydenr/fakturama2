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
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

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
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    protected VatCategoriesDAO vatCategoriesDAO;
    
    @Inject
    protected Logger log;

    // Editor's ID
    public static final String ID = "com.sebulli.fakturama.editors.vatEditor";

    // SWT widgets of the editor
    private Composite top;
    private Text textName;
    private Text textDescription;
    private FormattedText textValue;
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

    /**
     * Saves the contents of this part
     * 
     * @param monitor
     *            Progress monitor
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
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
            // to find the complete Category we have to split the category string and check each of the parts
            String[] splittedCategories = testCat.split("/");
            VATCategory parentCategory = null;
            for (String category : splittedCategories) {
                VATCategory searchCat = vatCategoriesDAO.findVATCategoryByName(category);
                if(searchCat == null) {
                    // not found? Then create a new one.
                    VATCategory newCategory = new VATCategory();
                    newCategory.setName(category);
                    newCategory.setParent(parentCategory);
                    vatCategoriesDAO.save(newCategory);
                    searchCat = newCategory;
                }
                // save the parent and then dive deeper...
                parentCategory = searchCat;
            }
            // parentCategory now has the last found Category
            editorVat.setCategory(parentCategory);
            vatDao.save(editorVat);
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
        
		// Refresh the table view of all VATs
        evtBroker.post("VatEditor", "update");
        
        // reset dirty flag
		getMDirtyablePart().setDirty(false);
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
        VAT stdVat = null;
        int stdID = 0;
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
        textName.setToolTipText(labelName.getToolTipText());
        bindModelValue(editorVat, textName, "name", 64);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);

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

        UpdateValueStrategy vatCatModel2Target = new UpdateValueStrategy();
        vatCatModel2Target.setConverter(new VATCategoryConverter());
        
        UpdateValueStrategy target2VatcatModel = new UpdateValueStrategy();
        target2VatcatModel.setConverter(new StringToVatCategoryConverter(categories));
        bindModelValue(editorVat, comboCategory, "category", target2VatcatModel, vatCatModel2Target);
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

        textValue = new FormattedText(top, SWT.BORDER | SWT.SINGLE);
        textValue.setFormatter(new PercentFormatter());
        GridData data = new GridData();
        data.widthHint = 200;
        textValue.getControl().setLayoutData(data);
        bindModelValue(editorVat, textValue, "taxValue", 16);
//        GridDataFactory.fillDefaults().grab(true, false).applyTo(textValue);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textValue.getControl());

        // Create the composite to make this payment to the standard payment. 
        Label labelStdVat = new Label(top, SWT.NONE);
        labelStdVat.setText(msg.commonLabelDefault);
        //T: Tool Tip Text
        labelStdVat.setToolTipText(msg.editorVatNameTooltip);

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
        // the Button is disabled by default, see Editor.StdComposite
        if (!newVat) {
            stdComposite.stdButton.setEnabled(true);
        }
    }

    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }

}
