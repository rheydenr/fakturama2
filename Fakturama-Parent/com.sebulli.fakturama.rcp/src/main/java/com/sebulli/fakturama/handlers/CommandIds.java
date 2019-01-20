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

package com.sebulli.fakturama.handlers;

/**
 * Constants defining the application's command IDs. 
 */
public final class CommandIds {

	public static final String CMD_OPEN_CONTACTS = "com.sebulli.fakturama.command.openContacts";
	public static final String CMD_OPEN_PRODUCTS = "com.sebulli.fakturama.command.openProducts";
	public static final String CMD_OPEN_VATS = "com.sebulli.fakturama.command.openVats";
	public static final String CMD_OPEN_DOCUMENTS = "com.sebulli.fakturama.command.openDocuments";
	public static final String CMD_OPEN_PAYMENTS = "com.sebulli.fakturama.command.openPayments";
	public static final String CMD_OPEN_SHIPPINGS = "com.sebulli.fakturama.command.openShippings";
	public static final String CMD_OPEN_TEXTS = "com.sebulli.fakturama.command.openTexts";
	public static final String CMD_OPEN_LISTS = "com.sebulli.fakturama.command.openLists";
	public static final String CMD_OPEN_EXPENDITUREVOUCHERS = "com.sebulli.fakturama.command.openExpenditureVouchers";
	public static final String CMD_OPEN_RECEIPTVOUCHERS = "com.sebulli.fakturama.command.openReceiptVouchers";
	
	public static final String CMD_CALL_EDITOR = "com.sebulli.fakturama.command.callEditor";

    public static final String CMD_NEW_CONTACT = "command.new.contact";
    public static final String CMD_NEW_PRODUCT = "command.new.product";
    public static final String CMD_NEW_VAT = "command.new.vat";
    
    public static final String CMD_NEW_DOCUMENT = "command.new.document";
    public static final String CMD_NEW_LETTER = "command.new.letter";
    public static final String CMD_NEW_OFFER = "command.new.offer";
    public static final String CMD_NEW_ORDER = "command.new.order";
    public static final String CMD_NEW_CONFIRMATION = "command.new.confirmation";
    public static final String CMD_NEW_INVOICE = "command.new.invoice";
    public static final String CMD_NEW_DELIVERY = "command.new.delivery";
    public static final String CMD_NEW_DELIVERYNOTE = "command.new.deliverynote";
    public static final String CMD_NEW_CREDIT = "command.new.credit";
    public static final String CMD_NEW_DUNNING = "command.new.dunning";
    public static final String CMD_NEW_PROFORMA = "command.new.proforma";
    
    public static final String CMD_NEW_PAYMENT = "command.new.payment";
    public static final String CMD_NEW_SHIPPING = "command.new.shipping";
    public static final String CMD_NEW_TEXT = "command.new.text";
    public static final String CMD_NEW_LISTENTRY = "command.new.listentry";
    public static final String CMD_NEW_EXPENDITUREVOUCHER = "command.new.expenditurevoucher";
    public static final String CMD_NEW_RECEIPTVOUCHER = "command.new.receiptvoucher";
	public static final String CMD_NEW_ = "com.sebulli.fakturama.command.new";

	public static final String TOOLBAR_CONTACT = "main.menu.new.contact.name";
    public static final String TOOLBAR_PRODUCT = "command.new.product.name";
    public static final String TOOLBAR_VAT = "main.menu.new.vat";
    
    public static final String TOOLBAR_DOCUMENT = "main.menu.new.document";
    public static final String TOOLBAR_LETTER = "main.menu.new.letter";
    public static final String TOOLBAR_OFFER = "main.menu.new.offer";
    public static final String TOOLBAR_ORDER = "main.menu.new.order";
    public static final String TOOLBAR_CONFIRMATION = "main.menu.new.confirmation";
    public static final String TOOLBAR_INVOICE = "main.menu.new.invoice";
    public static final String TOOLBAR_DELIVERY = "main.menu.new.delivery";
    public static final String TOOLBAR_DELIVERYNOTE = "main.menu.new.deliverynote";
    public static final String TOOLBAR_CREDIT = "main.menu.new.credit";
    public static final String TOOLBAR_DUNNING = "main.menu.new.dunning";
    public static final String TOOLBAR_PROFORMA = "main.menu.new.proforma";
    
    public static final String TOOLBAR_PAYMENT = "main.menu.new.payment";
    public static final String TOOLBAR_SHIPPING = "main.menu.new.shipping";
    public static final String TOOLBAR_TEXT = "main.menu.new.text";
    public static final String TOOLBAR_LISTENTRY = "main.menu.new.listentry";
    public static final String TOOLBAR_EXPENDITUREVOUCHER = "main.menu.new.expenditurevoucher";
    public static final String TOOLBAR_RECEIPTVOUCHER = "main.menu.new.receiptvoucher";
    
    /**
     * "Add" command for the list view.  
     */
    public static final String LISTTOOLBAR_ADD_VAT      = "com.sebulli.fakturama.listview.vat.add";
    public static final String LISTTOOLBAR_ADD_TEXT     = "com.sebulli.fakturama.listview.text.add";
    public static final String LISTTOOLBAR_ADD_CONTACT  = "com.sebulli.fakturama.listview.contact.add";
    public static final String LISTTOOLBAR_ADD_SHIPPING = "com.sebulli.fakturama.listview.shipping.add";
    public static final String LISTTOOLBAR_ADD_PAYMENT  = "com.sebulli.fakturama.listview.payment.add";
    public static final String LISTTOOLBAR_ADD_DOCUMENT = "com.sebulli.fakturama.listview.document.add";
    public static final String LISTTOOLBAR_ADD_EXPENDITURE = "com.sebulli.fakturama.listview.Voucher.add";
    public static final String LISTTOOLBAR_ADD_RECEIPTVOUCHER = "com.sebulli.fakturama.listview.receiptvoucher.add";
    public static final String LISTTOOLBAR_ADD_PRODUCT = "com.sebulli.fakturama.listview.product.add";
    
    public static final String CMD_CREATE_OODOCUMENT = "com.sebulli.fakturama.command.createOODocument";
    
	public static final String CMD_SAVE = "org.eclipse.ui.file.save";

	public static final String CMD_DELETE_DATASET = "com.sebulli.fakturama.command.deleteDataSet";

	public static final String CMD_SELECT_WORKSPACE = "com.sebulli.fakturama.command.selectWorkspace";

	public static final String CMD_WEBSHOP_IMPORT = "com.sebulli.fakturama.command.webShopImport";
	public static final String CMD_PRODUCTS_STOCKUPDATE = "org.fakturama.command.updatestock";

	public static final String CMD_MARK_ORDER_AS = "com.sebulli.fakturama.command.order.markas";
	public static final String CMD_MARK_DOCUMENT_AS = "com.sebulli.fakturama.command.document.markas";

	public static final String CMD_IMPORT_CSV = "com.sebulli.fakturama.command.importCSV";

	public static final String CMD_EXPORT_SALES_SUMMARY = "com.sebulli.fakturama.command.exportSalesSummary";

	public static final String CMD_OPEN_BROWSER_EDITOR = "com.sebulli.fakturama.command.openBrowserEditor";
	public static final String CMD_OPEN_CALCULATOR = "com.sebulli.fakturama.command.openCalculator";

	public static final String CMD_P2_UPDATE = "com.sebulli.fakturama.command.update";
	public static final String CMD_P2_INSTALL = "com.sebulli.fakturama.command.install";
	
	public static final String CMD_OPEN_PARCEL_SERVICE = "com.sebulli.fakturama.command.openParcelService";

	public static final String CMD_MOVE_UP = "com.sebulli.fakturama.command.moveEntryUp";
	public static final String CMD_MOVE_DOWN = "com.sebulli.fakturama.command.moveEntryDown";
	
	public static final String CMD_REORGANIZE_DOCUMENTS = "com.sebulli.fakturama.command.reorganizeDocuments";
	public static final String CMD_EXPORT_CSV4DP =  "org.fakturama.command.exportcsv4dp";
	
	
}
