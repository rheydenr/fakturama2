/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.parts;

import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.TextCategoriesDAO;
import com.sebulli.fakturama.dao.TextsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.model.CategoryComparator;
import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.TextModule_;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.resources.core.Icon;

/**
 * The text editor
 * 
 * @author Gerd Bartelt
 */
public class TextEditor extends Editor<TextModule> {

	// Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.textEditor";

    public static final String EDITOR_ID = "TextEditor";

	@Inject
	private TextsDAO textsDAO;
	
	@Inject
	private TextCategoriesDAO textCategoriesDAO;

	// This UniDataSet represents the editor's input 
	private TextModule editorText;

    /**
     * This field can't be injected since the part is created from
     * a PartDescriptor (see createPartControl).
     */
    private MPart part;

	// SWT widgets of the editor
    private Composite top;
	private Text textName;
	private Text textText;
	private Combo comboCategory;

	// defines, if the text is new created
	private boolean newText;

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

		// Always set the editor's data set to "undeleted"
	    editorText.setDeleted(Boolean.FALSE);

        // Set the Text data
        // ... done through databinding...
        try {
            // at first, check the category for a new entry
            // (the user could have written a new one into the combo field)
            String testCat = comboCategory.getText();
            // if there's no category we can skip this step
            if(StringUtils.isNotBlank(testCat)) {
                TextCategory parentCategory = textCategoriesDAO.getOrCreateCategory(testCat, true);
                // parentCategory now has the last found Category
                editorText.setCategories(parentCategory);
            }
            
            /*
             * If we DON'T use update, the category is saved again and again and again
             * because we have CascadeType.PERSIST. If we use update and save the new TextCategory before,
             * all went ok. That's the point...
             */
            editorText = textsDAO.update(editorText);
        }
        catch (FakturamaStoringException e) {
            log.error(e, "can't save the current VAT: " + editorText.toString());
        }

		// If it is a new text, add it to the text list and
		// to the data base
		if (newText) {
			newText = false;
		}

		// Set the Editor's name to the shipping name...
		part.setLabel(editorText.getName());
        
        // ...and "mark" it with current objectId (though it can be find by 
        // CallEditor if one tries to open it immediately from list view)
        part.getTransientData().put(CallEditor.PARAM_OBJ_ID, Long.toString(editorText.getId()));

		// Refresh the table view of all texts
        evtBroker.post(TextEditor.EDITOR_ID, Editor.UPDATE_EVENT);
        
        bindModel();
        
        // reset dirty flag
        getMDirtyablePart().setDirty(false);
	}

	/**
     * Initializes the editor. If an existing data set is opened, the local
     * variable "text" is set to This data set. If the editor is opened to
     * create a new one, a new data set is created and the local variable "text"
     * is set to this one.
	 * Creates the SWT controls for this workbench part
	 * 
	 * @param the
	 *            parent control
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
        Long objId = null;
        this.part = (MPart) parent.getData("modelElement");
        this.part.setIconURI(Icon.COMMAND_TEXT.getIconURI());
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            editorText = textsDAO.findById(objId);
        }

        // test, if the editor is opened to create a new data set. This is,
        // if there is no input set.
        newText = (editorText == null);

        // If new ..
        if (newText) {

            // Create a new data set
            editorText = modelFactory.createTextModule();
            String category = (String) part.getProperties().get(CallEditor.PARAM_CATEGORY);
            if(StringUtils.isNotEmpty(category)) {
                TextCategory newCat = textCategoriesDAO.findTextCategoryByName(category);
                editorText.setCategories(newCat);
            }
            
            //T: Text Editor: Part Name of a new text entry
            part.setLabel(msg.editorTextNameNeu);
            getMDirtyablePart().setDirty(true);
        }
        else {

            // Set the Editor's name to the shipping name.
            part.setLabel(editorText.getName());
        }

		// Create the top Composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.TEXT_EDITOR);

		// Create the title
		Label labelTitle = new Label(top, SWT.NONE);
		//T: Text Editor: Title
		labelTitle.setText(msg.editorTextHeader);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).applyTo(labelTitle);
		makeLargeLabel(labelTitle);

		// The name
		Label labelName = new Label(top, SWT.NONE);
		labelName.setText(msg.commonFieldName);
		labelName.setToolTipText(msg.textFieldNameTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
		textName = new Text(top, SWT.BORDER);
		textName.setToolTipText(labelName.getToolTipText());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);

		// The category
		Label labelCategory = new Label(top, SWT.NONE);
		labelCategory.setText(msg.commonFieldCategory);
		labelCategory.setToolTipText(msg.textFieldCategoryTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

        comboCategory = new Combo(top, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);
		
		// The text
		Label labelText = new Label(top, SWT.NONE);
		labelText.setText(msg.commonFieldText);
		labelText.setToolTipText(msg.textFieldTextTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelText);
		textText = new Text(top, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		textText.setToolTipText(labelText.getToolTipText());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textText);
		
		bindModel();
	}
	
	@Override
	protected void bindModel() {
		bindModelValue(editorText, textName, TextModule_.name.getName(), 64);
        fillAndBindCategoryCombo();
        bindModelValue(editorText, textText, TextModule_.text.getName(), 10000);
	}

    /**
     * creates the combo box for the TextModule category
     */
    private void fillAndBindCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<TextCategory> categories = new TreeSet<TextCategory>(new CategoryComparator<>());
        categories.addAll(textCategoriesDAO.findAll());

        ComboViewer viewer = new ComboViewer(comboCategory);
        viewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return categories.toArray();
            }
        });
        
        TextCategory tmpCategory = editorText.getCategories();
        // Add all categories to the combo
        viewer.setInput(categories);
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element instanceof TextCategory ? CommonConverter.getCategoryName((TextCategory)element, "") : null;
            }
        });
        editorText.setCategories(tmpCategory);

        UpdateValueStrategy textCatModel2Target = new UpdateValueStrategy();
        textCatModel2Target.setConverter(new CategoryConverter<TextCategory>(TextCategory.class));
        
        UpdateValueStrategy target2VatcatModel = new UpdateValueStrategy();
        target2VatcatModel.setConverter(new StringToCategoryConverter<TextCategory>(categories, TextCategory.class));
        bindModelValue(editorText, comboCategory, TextModule_.categories.getName(), target2VatcatModel, textCatModel2Target);
    }
	
	/**
	 * Set the focus to the top composite.
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
	 */
	@Focus
	public void setFocus() {
		if(top != null) 
			top.setFocus();
	}
    
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }
    
    @Override
    protected Class<TextModule> getModelClass() {
        return TextModule.class;
    }
}
