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

package com.sebulli.fakturama.parts.itemlist;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.sebulli.fakturama.dto.DocumentItemDTO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
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
    
    @Inject
    private IEclipseContext context;

    private Composite parent;
    private Document document;
    private DocumentType documentType;
    private boolean useGross;
    private int netgross = DocumentSummary.ROUND_NOTSPECIFIED;

    protected NatTable natTable;

    public DocumentItemListTable build() {

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
    
    // Item delete button
    Label deleteButton = new Label(addButtonComposite, SWT.NONE);
    //T: Tool Tip Text
    deleteButton.setToolTipText(msg.editorDocumentDeleteitemTooltip);
    deleteButton.setImage(Icon.COMMAND_DELETE.getImage(IconSize.DefaultIconSize));
    GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(deleteButton);
    
    // Composite that contains the table
    // The table viewer
    final DocumentItemListTable itemListTable = ContextInjectionFactory.make(DocumentItemListTable.class, context);
    Control tableComposite = itemListTable.createPartControl(parent, document, useGross, netgross);
    GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(tableComposite);

    addButton.addMouseListener(new MouseAdapter() {
        
        // Add a new item with default properties
        public void mouseDown(MouseEvent e) {
    //      // Cancel the item editing
    //      if (itemEditingSupport != null)
    //          itemEditingSupport.cancelAndSave();
    //      
          //T: Text of a new item
          DocumentItemDTO newItem = new DocumentItemDTO(new DocumentItem());
    //        msg.commonFieldName, 
    //                //T: Text of a new item
    //                msg.productFieldItemno, "", documentType.getSign() * 1.0, "", 0.0, 0, "", "");
    ////
    //      // Use the standard VAT value
    //      newItem.setVat(Integer.parseInt(Data.INSTANCE.getProperty("standardvat")));
    //      addNewItem(newItem);
    //
            itemListTable.addNewItem(newItem);
    
    //      tableViewerItems.refresh();
    //      tableViewerItems.reveal(newItem);
//            calculate();
//    //      checkDirty();
//    //      
//            // Renumber all Items
//            renumberItems();
    
        }
    });    
    
    deleteButton.addMouseListener(new MouseAdapter() {
    
        // Delete the selected item
        public void mouseDown(MouseEvent e) {
            itemListTable.removeSelectedEntry();
        }
    });

    ////Create the context menu
    //createContextMenu(tableViewerItems);
    
    return itemListTable;
    }

    public ItemListBuilder withParent(Composite parent) {
        this.parent = parent;
        return this;
    }

    public ItemListBuilder withUseGross(boolean useGross) {
        this.useGross = useGross;
        return this;
    }

    public ItemListBuilder withNetGross(int netgross) {
        this.netgross = netgross;
        return this;
    }

    public ItemListBuilder withDocument(Document document) {
        this.document = document;
        this.documentType = DocumentType.findByKey(document.getBillingType().getValue());
        return this;
    }

}
