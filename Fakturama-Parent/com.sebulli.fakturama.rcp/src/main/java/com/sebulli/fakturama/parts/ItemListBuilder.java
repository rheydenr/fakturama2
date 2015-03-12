/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.parts;

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * Builder for the {@link DocumentItem}s list.
 * 
 */
public class ItemListBuilder {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private EModelService modelService;

    @Inject
    private MApplication application;

    private Composite parent;
    private DocumentType documentType;

    public void build() {
        // Container for the label and the add and delete button.
        Composite addButtonComposite = new Composite(parent, SWT.NONE | SWT.RIGHT);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addButtonComposite);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addButtonComposite);
        // Items label
        Label labelItems = new Label(addButtonComposite, SWT.NONE | SWT.RIGHT);
        //T: Document Editor
        //T: Label items
        labelItems.setText(msg.editorDocumentItems);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(labelItems);

        // Item add button
        Label addFromListButton = new Label(addButtonComposite, SWT.NONE);
        //T: Tool Tip Text
        addFromListButton.setToolTipText(msg.dialogSelectproductTooltip);
        addFromListButton.setImage(Icon.DOCEDIT_PRODUCT_LIST.getImage(IconSize.BrowserIconSize));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addFromListButton);
        addFromListButton.addMouseListener(new MouseAdapter() {
            // Open the product dialog and add the
            // selected product as new item.
            public void mouseDown(MouseEvent e) {

                //                T: Document Editor
                //                T: Title of the dialog to select a product
                // SelectProductDialog
                MDialog dialog = (MDialog) modelService.find("fakturama.dialog.select.product", application);
                dialog.setToBeRendered(true);
                dialog.setVisible(true);
                dialog.setOnTop(true);
                modelService.bringToTop(dialog);

                // handling of adding a new list item is done via event handling in DocumentEditor
            }
        });

        // Add the button to add all items from a delivery note
        // was: documentType.hasAddFromDeliveryNote()
        if (documentType == DocumentType.INVOICE || documentType == DocumentType.PROFORMA) {
            // Item add button
            Label addFromDeliveryNoteButton = new Label(addButtonComposite, SWT.NONE);
            //T: Tool Tip Text
            addFromDeliveryNoteButton.setToolTipText(msg.editorDocumentCollectiveinvoiceTooltip);
            addFromDeliveryNoteButton.setImage(Icon.DOCEDIT_DELIVERY_NOTE_LIST.getImage(IconSize.DocumentIconSize));
            GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addFromDeliveryNoteButton);
            addFromDeliveryNoteButton.addMouseListener(new MouseAdapter() {

                // Open the product dialog and add the
                // selected product as new item.
                public void mouseDown(MouseEvent e) {
                    //T: Document Editor
                    //T: Title of the dialog to select a delivery note
                    //              SelectDeliveryNoteDialog
                    //              dialog = new SelectDeliveryNoteDialog(_("Select a delivey note"), addressId);
                    MDialog dialog = (MDialog) modelService.find("fakturama.dialog.selectdeliverynotes", application);
                    dialog.setToBeRendered(true);
                    dialog.setVisible(true);
                    dialog.setOnTop(true);
                    modelService.bringToTop(dialog);

                    // handling of adding a new list item is done via event handling in DocumentEditor
                }
            });
        }

    // Item add button
    Label addButton = new Label(addButtonComposite, SWT.NONE);
    //T: Tool Tip Text
    addButton.setToolTipText(msg.editorDocumentAdditemTooltip);
    addButton.setImage(Icon.COMMAND_PLUS.getImage(IconSize.DefaultIconSize));
    GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addButton);
    addButton.addMouseListener(new MouseAdapter() {
    
        // Add a new item with default properties
        public void mouseDown(MouseEvent e) {
    
    //      // Cancel the item editing
    //      if (itemEditingSupport != null)
    //          itemEditingSupport.cancelAndSave();
    //      
    //      //T: Text of a new item
    //      DocumentItem newItem = new DocumentItem();
    ////        msg.commonFieldName, 
    ////                //T: Text of a new item
    ////                msg.productFieldItemno, "", documentType.getSign() * 1.0, "", 0.0, 0, "", "");
    ////
    //      // Use the standard VAT value
    //      newItem.setVat(Integer.parseInt(Data.INSTANCE.getProperty("standardvat")));
    //      addNewItem(newItem);
    //
    //      tableViewerItems.refresh();
    //      tableViewerItems.reveal(newItem);
//            calculate();
//    //      checkDirty();
//    //      
//            // Renumber all Items
//            renumberItems();
    
        }
    });
    
    // Item delete button
    Label deleteButton = new Label(addButtonComposite, SWT.NONE);
    //T: Tool Tip Text
    deleteButton.setToolTipText(msg.editorDocumentDeleteitemTooltip);
    deleteButton.setImage(Icon.COMMAND_DELETE.getImage(IconSize.DefaultIconSize));
    GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(deleteButton);
    deleteButton.addMouseListener(new MouseAdapter() {
    
        // Delete the selected item
        public void mouseDown(MouseEvent e) {
    //        ISelection selection = tableViewerItems.getSelection();
    //        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    //        if (!structuredSelection.isEmpty()) {
    //            IStructuredSelection iselection = ((IStructuredSelection) selection);
    //            for (Iterator iterator = iselection.iterator(); iterator.hasNext();) {
    //                Object obj = (Object) iterator.next();
    //                // If we had a selection, delete it
    //                if (obj != null) {
    //                    UniDataSet uds = (UniDataSet) obj;
    //                    deleteItem(uds);
    //                }
    //            }
    //
    //            // Renumber all Items
    //            renumberItems();
    //
    //        }
        }
    });
    
    // Composite that contains the table
    Composite tableComposite = new Composite(parent, SWT.NONE);
    GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(tableComposite);
//    tableColumnLayout = new TableColumnLayout();
//    tableComposite.setLayout(tableColumnLayout);
//    
//    // The table viewer
//    tableViewerItems = new TableViewer(tableComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
//    tableViewerItems.getTable().setLinesVisible(true);
//    tableViewerItems.getTable().setHeaderVisible(true);
//    tableViewerItems.setContentProvider(new ViewDataSetTableContentProvider(tableViewerItems));
    //// Workaround for of an Error in Windows JFace
    //// Add an empty column
    //// Mantis #0072
    //if (OSDependent.isWin())
    //    itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER,"", 0, true, "$", null));
    //
    //if (preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_ITEM_POS, false))
    ////T: Used as heading of a table. Keep the word short.
    //    itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER,_("Pos"), cw_pos, true, "$Row", null));
    //
    //// Create the table columns 
    //if (containsOptionalItems || preferences.getBoolean(Constants.PREFERENCES_OPTIONALITEMS_USE, false) && (documentType == DocumentType.OFFER))
    //    //T: Used as heading of a table. Keep the word short.
    //    itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER, _("Opt."), cw_opt, true, "$Optional", new DocumentItemEditingSupport(this,
    //            tableViewerItems, DocumentItemEditingSupport.Column.OPTIONAL)));
    ////T: Used as heading of a table. Keep the word short.
    //itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER, _("Qty."), cw_qty, true, "quantity", new DocumentItemEditingSupport(this,
    //        tableViewerItems, DocumentItemEditingSupport.Column.QUANTITY)));
    //
    //if (Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_QUNIT"))
    ////T: Used as heading of a table. Keep the word short.
    //itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER, _("Q. Unit"), cw_qunit, true, "qunit", new DocumentItemEditingSupport(this,
    //        tableViewerItems, DocumentItemEditingSupport.Column.QUNIT)));
    //
    //if (Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_ITEMNR"))
    //    //T: Used as heading of a table. Keep the word short.
    //    itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.LEFT, _("Item No."), cw_itemno, true, "itemnr", new DocumentItemEditingSupport(this,
    //            tableViewerItems, DocumentItemEditingSupport.Column.ITEMNR)));
    //
    //if (Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_PREVIEW_PICTURE"))
    //    //T: Used as heading of a table. Keep the word short.
    //    itemTableColumns.add( new UniDataSetTableColumn(parent.getDisplay() , tableColumnLayout, tableViewerItems, SWT.LEFT, _("Picture"), cw_picture, true, "$ProductPictureSmall", new DocumentItemEditingSupport(this,
    //        tableViewerItems, DocumentItemEditingSupport.Column.PICTURE)));
    //
    ////T: Used as heading of a table. Keep the word short.
    //itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.LEFT, _("Name"), cw_name, false, "name", new DocumentItemEditingSupport(this,
    //        tableViewerItems, DocumentItemEditingSupport.Column.NAME)));
    ////T: Used as heading of a table. Keep the word short.
    //itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.LEFT, _("Description"), cw_description, false, "description", new DocumentItemEditingSupport(
    //        this, tableViewerItems, DocumentItemEditingSupport.Column.DESCRIPTION)));
    //if (documentType.hasItemsPrice()) {
    //    //T: Used as heading of a table. Keep the word short.
    //    itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("VAT"), cw_vat, true, "$ItemVatPercent", new DocumentItemEditingSupport(this,
    //            tableViewerItems, DocumentItemEditingSupport.Column.VAT)));
    //
    //    if (useGross) {
    //        //T: Unit Price.
    //        //T: Used as heading of a table. Keep the word short.
    //        itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("U.Price"), cw_uprice, true, "$ItemGrossPrice",
    //                new DocumentItemEditingSupport(this, tableViewerItems, DocumentItemEditingSupport.Column.PRICE)));
    //    }
    //    else {
    //        //T: Used as heading of a table. Keep the word short.
    //        itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("U.Price"), cw_uprice, true, "price", new DocumentItemEditingSupport(this,
    //                tableViewerItems, DocumentItemEditingSupport.Column.PRICE)));
    //    }
    //    unitPriceColumn = itemTableColumns.size()-1;
    //    
    //    if (containsDiscountedItems || Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_DISCOUNT_EACH_ITEM"))
    //        //T: Used as heading of a table. Keep the word short.
    //        itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("Discount"), cw_discount, true, "discount", new DocumentItemEditingSupport(this,
    //                tableViewerItems, DocumentItemEditingSupport.Column.DISCOUNT)));
    //
    //    
    //    if (useGross){
    //        //T: Used as heading of a table. Keep the word short.
    //        itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("Price"), cw_price, true, "$ItemGrossTotal", new DocumentItemEditingSupport(
    //                this, tableViewerItems, DocumentItemEditingSupport.Column.TOTAL)));
    //    }
    //    else {
    //        //T: Used as heading of a table. Keep the word short.
    //        itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("Price"), cw_price, true, "$ItemNetTotal", new DocumentItemEditingSupport(
    //                this, tableViewerItems, DocumentItemEditingSupport.Column.TOTAL)));
    //    }
    //
    //    totalPriceColumn = itemTableColumns.size()-1;
    //
    //}
    //// Fill the table with items
    //tableViewerItems.setInput(items);
    //
    ////Create the context menu
    //createContextMenu(tableViewerItems);
    }
  ////
/////**
//// * Create the default context menu 
//// */
////private void createContextMenu(TableViewer tableViewerItems) {
////    
////    //Cancel, if there are no items
////    if (tableViewerItems == null)
////        return;
////    
////    menuManager = new MenuManager();
////    menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
////    tableViewerItems.getTable().setMenu(menuManager.createContextMenu(tableViewerItems.getTable()));
////
////    getSite().registerContextMenu("com.sebulli.fakturama.editors.DocumentEditor.tableViewerItems.contextmenu", menuManager, tableViewerItems);
////    getSite().setSelectionProvider(tableViewerItems);
////    
////    // Add up/down and delete actions
////    menuManager.add(new MoveEntryUpAction());
////    menuManager.add(new MoveEntryDownAction());
////    menuManager.add(new DeleteDataSetAction());
////}
////
/////**
//// * Move an item up or down
//// */
////public void moveItem(UniDataSet uds, boolean up) {
////
////    if (!(uds instanceof DataSetItem))
////        return;
////    
////    if (items.getActiveDatasets().contains(uds)) {
////
////        // Get the position of the selected element
////        int prepos = items.getDatasets().indexOf(items.getPreviousDataSet((DataSetItem) uds));
////        int pos = items.getDatasets().indexOf(uds);
////        int nextpos = items.getDatasets().indexOf(items.getNextDataSet((DataSetItem) uds));
////        int size = items.getDatasets().size();
////        int activesize = items.getActiveDatasets().size();
////
////        // Do not move one single item
////        if (activesize >= 2) {
////            // Move up
////            if (up && (prepos >=0 )){
////                items.swapPosition(prepos, pos);
////            }
////
////            // Move down
////            if (!up && (nextpos < size) && nextpos >= 0){
////                items.swapPosition(pos, nextpos);
////            }
////        }
////        
////        
////    }
////
////    //Renumber the items
////    RenumberItems();
////
////    // Refresh the table
////    tableViewerItems.refresh();
////    checkDirty();
////}
////
/////**
//// * delete an item
//// */
////public void deleteItem(UniDataSet uds) {
////
////    if (!(uds instanceof DataSetItem))
////        return;
////    
////    // Delete it (mark it as deleted)
////    uds.setBooleanValueByKey("deleted", true);
////    
////    // Renumber the items
////    RenumberItems();
////
////    tableViewerItems.refresh();
////    calculate();
////    checkDirty();
////}
////

    public ItemListBuilder withParent(Composite parent) {
        this.parent = parent;
        return this;
    }

    public ItemListBuilder withDocumentType(DocumentType documentType) {
        this.documentType = documentType;
        return this;
    }

}
