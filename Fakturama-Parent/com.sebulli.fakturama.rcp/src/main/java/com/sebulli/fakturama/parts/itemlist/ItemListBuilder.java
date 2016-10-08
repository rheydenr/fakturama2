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

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dialogs.SelectDeliveryNoteDialog;
import com.sebulli.fakturama.dialogs.SelectProductDialog;
import com.sebulli.fakturama.dto.DocumentItemDTO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;

/**
 * Builder for the {@link DocumentItem}s list.
 * 
 */
public class ItemListBuilder {

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private IEclipseContext context;
    
    @Inject
    @Preference 
    protected IEclipsePreferences preferences;
    
    @Inject
    protected ESelectionService selectionService;

    @Inject
    protected VatsDAO vatDao;

    private Composite parent;
    private Document document;

    private DocumentType documentType;
//    private boolean useGross;
    private int netgross = DocumentSummary.ROUND_NOTSPECIFIED;
	private DocumentEditor container;

//    protected NatTable natTable;

    /**
     * Build the {@link DocumentsListTable}.
     * @return
     */
    public DocumentItemListTable build() {
        FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;

//        String ID = "com.sebulli.fakturama.documentitemslist.toolbar";
//        this.part = (MPart) parent.getData("modelElement");
//        MSnippetContainer snippetWindow = (MSnippetContainer)modelService.find("com.sebulli.fakturama.snippets", application);
//        modelService.cloneSnippet(snippetWindow, ID, part);
        
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
        addFromListButton.setToolTipText(msg.dialogSelectproductTooltip);
        addFromListButton.setImage(Icon.DOCEDIT_PRODUCT_LIST.getImage(IconSize.BrowserIconSize));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addFromListButton);
        addFromListButton.addMouseListener(new MouseAdapter() {
            // Open the product dialog and add the
            // selected product as new item.
            public void mouseDown(MouseEvent e) {

                // T: Document Editor
                // T: Title of the dialog to select a product
                // SelectProductDialog
			    context.set(DocumentEditor.DOCUMENT_ID, document.getName());
			    context.set(ESelectionService.class, selectionService);
			    SelectProductDialog dlg = ContextInjectionFactory.make(SelectProductDialog.class, context);
			    dlg.open();

                // handling of adding a new list item is done via event handling in DocumentEditor
			    // (setting via dlg.getResult() would get too complicated, since we have to hold
			    // a reference to the calling editor)
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
    			    context.set(DocumentEditor.DOCUMENT_ID, document.getName());
    			    context.set(ESelectionService.class, selectionService);
    			    SelectDeliveryNoteDialog dlg = ContextInjectionFactory.make(SelectDeliveryNoteDialog.class, context);
    			    dlg.open();
//                    MDialog dialog = (MDialog) modelService.find("fakturama.dialog.selectdeliverynotes", application);
//                    dialog.setToBeRendered(true);
//                    dialog.setVisible(true);
//                    dialog.setOnTop(true);
//                    modelService.bringToTop(dialog);
                    
                    // set document dirty
                    container.setDirty(true);

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
//        itemListTable.setContainer(container);
        Control tableComposite = itemListTable.createPartControl(parent, document/*, useGross*/, container, netgross);
        GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(tableComposite);

        addButton.addMouseListener(new MouseAdapter() {

            // Add a new item with default properties
            public void mouseDown(MouseEvent e) {
                DocumentItem item = modelFactory.createDocumentItem();
                //T: Text of a new item
                item.setName(msg.commonFieldName);
                item.setItemNumber(msg.productFieldItemno);
                // other values are set by default values (look into model)
                int defaultVatId = preferences.getInt(Constants.DEFAULT_VAT, 1);
                VAT defaultVat = vatDao.findById(defaultVatId);
                Optional<DocumentItemDTO> maxPosItem = itemListTable.getDocumentItemsListData().stream().max(new Comparator<DocumentItemDTO>() {
                    @Override
                    public int compare(DocumentItemDTO o1, DocumentItemDTO o2) {
                        return o1.getDocumentItem().getPosNr().compareTo(o2.getDocumentItem().getPosNr());
                    }
                });
                
                Integer newPosNr = maxPosItem.isPresent() ? maxPosItem.get().getDocumentItem().getPosNr() + Integer.valueOf(1) : Integer.valueOf(1);
                item.setPosNr(newPosNr);

                // Use the standard VAT value
                item.setItemVat(defaultVat);
                DocumentItemDTO newItem = new DocumentItemDTO(item);
                itemListTable.addNewItem(newItem);

                // table refresh is done via sending an event to DocumentEditor and GlazedListsEventList 

                // Renumber all Items
                // renumberItems();
                
                // set document dirty
                container.setDirty(true);

            }
        });

        deleteButton.addMouseListener(new MouseAdapter() {

            // Delete the selected item
            public void mouseDown(MouseEvent e) {
                itemListTable.removeSelectedEntry();
                itemListTable.renumberItems();
            }
        });

    //Create the context menu
    //createContextMenu(tableViewerItems);
    
        return itemListTable;
    }

    public ItemListBuilder withParent(Composite parent) {
        this.parent = parent;
        return this;
    }

//    public ItemListBuilder withUseGross(boolean useGross) {
//        this.useGross = useGross;
//        return this;
//    }

    public ItemListBuilder withNetGross(int netgross) {
        this.netgross = netgross;
        return this;
    }

    public ItemListBuilder withDocument(Document document) {
        this.document = document;
        this.documentType = DocumentType.findByKey(document.getBillingType().getValue());
        return this;
    }
//
//    public ItemListBuilder withDocumentSummary(DocumentSummary documentSummary) {
//        this.documentSummary = documentSummary;
//        return this;
//    }

	public ItemListBuilder withContainer(DocumentEditor documentEditor) {
		this.container = documentEditor;
		return this;
	}

}
