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
import org.eclipse.core.runtime.IProgressMonitor;
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

import com.sebulli.fakturama.dao.ItemAccountTypeDAO;
import com.sebulli.fakturama.dao.ItemListTypeCategoriesDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.ItemAccountType_;
import com.sebulli.fakturama.model.ItemListTypeCategory;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.MessageKeyToCategoryConverter;
import com.sebulli.fakturama.resources.core.Icon;

/**
 * The text editor
 * 
 * @author Gerd Bartelt
 */
public class ListEditor extends Editor<ItemAccountType> {

    // Editor's ID
    public static final String ID = "com.sebulli.fakturama.editors.accountTypeEditor";

    public static final String EDITOR_ID = "ListEditor";

    // This UniDataSet represents the editor's input 
    private ItemAccountType editorListEntry;

    @Inject
    private ItemAccountTypeDAO itemAccountTypeDAO;

    @Inject
    private ItemListTypeCategoriesDAO itemListTypeCategoriesDAO;

    // SWT widgets of the editor
    private Composite top;
    private Text textName;
    private Text textValue;
    private Combo comboCategory;

    private MPart part;

    // defines, if the text is new created
    private boolean newList;

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

        try {

            /*
             * If we DON'T use update, the category is saved again and again and again
             * because we have CascadeType.PERSIST. If we use update and save the new ItemListTypeCategory before,
             * all went ok. That's the point...
             */
            editorListEntry = itemAccountTypeDAO.update(editorListEntry);
        } catch (FakturamaStoringException e) {
            log.error(e, "can't save the current ItemAccountType: " + editorListEntry.toString());
        }

        // Always set the editor's data set to "undeleted"
        editorListEntry.setDeleted(Boolean.FALSE);

        // If it is a new text, add it to the text list and
        // to the data base
        if (newList) {
            newList = false;
        }

        // Set the Editor's name to the list name.
        part.setLabel(editorListEntry.getName());

        // Refresh the table view of all ItemAccountTypes (this also refreshes the tree of categories)
        evtBroker.post(ListEditor.class.getSimpleName(), "update");

        // reset dirty flag
        getMDirtyablePart().setDirty(false);
    }

    /**
     * Initializes the editor. If an existing data set is opened, the local
     * variable "text" is set to This data set. If the editor is opened to
     * create a new one, a new data set is created and the local variable "text"
     * is set to this one. Creates the SWT controls for this workbench part
     * 
     * @param the
     *            parent control
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @PostConstruct
    public void createPartControl(Composite parent) {
        Long objId = null;
        this.part = (MPart) parent.getData("modelElement");
        this.part.setIconURI(Icon.COMMAND_LIST.getIconURI());
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            editorListEntry = itemAccountTypeDAO.findById(objId);
        }

        // test, if the editor is opened to create a new data set. This is,
        // if there is no input set.
        newList = (editorListEntry == null);

        // If new ..
        if (newList) {

            // Create a new data set
            editorListEntry = modelFactory.createItemAccountType();

            //T: List Editor: Part Name of a new list entry
            part.setLabel(msg.mainMenuNewListentry);
        } else {

            // Set the Editor's name to the list name.
            part.setLabel(editorListEntry.getName());
        }

        // Create the top Composite
        top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);

        // Add context help reference 
        //		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.LIST_EDITOR);

        // Create the title
        Label labelTitle = new Label(top, SWT.NONE);
        //T: List Editor - Title
        labelTitle.setText(msg.editorListHeader);
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).applyTo(labelTitle);
        makeLargeLabel(labelTitle);

        // The category
        Label labelCategory = new Label(top, SWT.NONE);
        //T: List Editor - Category ( Name of the List to place this entry)
        labelCategory.setText(msg.editorListListfield);
        labelCategory.setToolTipText(msg.editorListTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);
        createCategoryCombo();
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(300, SWT.DEFAULT).applyTo(comboCategory);

        // The name
        Label labelName = new Label(top, SWT.NONE);
        labelName.setText(msg.commonFieldName);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
        textName = new Text(top, SWT.BORDER);
        bindModelValue(editorListEntry, textName, ItemAccountType_.name.getName(), 64);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);

        // The value
        Label labelCode = new Label(top, SWT.NONE);
        labelCode.setText(msg.commonFieldValue);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCode);
        textValue = new Text(top, SWT.BORDER);
        bindModelValue(editorListEntry, textValue, ItemAccountType_.value.getName(), 250);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textValue);
    }

    /**
     * creates the combo box for the ItemAccountType category
     */
    private void createCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<ItemListTypeCategory> categories = new TreeSet<ItemListTypeCategory>(new Comparator<ItemListTypeCategory>() {
            @Override
            public int compare(ItemListTypeCategory cat1, ItemListTypeCategory cat2) {
                return cat1.getName().compareTo(cat2.getName());
            }
        });
        categories.addAll(itemListTypeCategoriesDAO.findAll());

        comboCategory = new Combo(top, SWT.BORDER | SWT.READ_ONLY);
        comboCategory.setToolTipText(msg.editorListTooltip);
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
                // the name in this case is the key for the localized name
                return element instanceof ItemListTypeCategory ? msg.getMessageFromKey(((ItemListTypeCategory) element).getName()) : null;
//                return element instanceof ItemListTypeCategory ? ((ItemListTypeCategory) element).getName() : null;
            }
        });

        UpdateValueStrategy itemListTypeCatModel2Target = new UpdateValueStrategy();
        itemListTypeCatModel2Target.setConverter(new CategoryConverter<ItemListTypeCategory>(ItemListTypeCategory.class, msg));

        UpdateValueStrategy target2ItemListTypecatModel = new UpdateValueStrategy();
        target2ItemListTypecatModel.setConverter(new MessageKeyToCategoryConverter<ItemListTypeCategory>(categories, ItemListTypeCategory.class, msg));
        bindModelValue(editorListEntry, comboCategory, ItemAccountType_.category.getName(), target2ItemListTypecatModel, itemListTypeCatModel2Target);
    }

    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }

    @Override
    protected Class<ItemAccountType> getModelClass() {
        return ItemAccountType.class;
    }
}
