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
 * Interface defining the application's command IDs. Key bindings can be defined
 * for specific commands. To associate an action with a command, use
 * IAction.setActionDefinitionId(commandId).
 * 
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public interface ICommandIds {

	public static final String CMD_OPEN_CONTACTS = "com.sebulli.fakturama.commands.openContacts";
	public static final String CMD_OPEN_PRODUCTS = "com.sebulli.fakturama.handler.openProducts";
	public static final String CMD_OPEN_VATS = "com.sebulli.fakturama.handler.openVats";
	public static final String CMD_OPEN_DOCUMENTS = "com.sebulli.fakturama.handler.openDocuments";
	public static final String CMD_OPEN_PAYMENTS = "com.sebulli.fakturama.handler.openPayments";
	public static final String CMD_OPEN_SHIPPINGS = "com.sebulli.fakturama.handler.openShippings";
	public static final String CMD_OPEN_TEXTS = "com.sebulli.fakturama.handler.openTexts";
	public static final String CMD_OPEN_LISTS = "com.sebulli.fakturama.handler.openLists";
	public static final String CMD_OPEN_EXPENDITUREVOUCHERS = "com.sebulli.fakturama.handler.openExpenditureVouchers";
	public static final String CMD_OPEN_RECEIPTVOUCHERS = "com.sebulli.fakturama.handler.openReceiptVouchers";

	public static final String CMD_NEW_CONTACT = "com.sebulli.fakturama.handler.newContact";
	public static final String CMD_NEW_PRODUCT = "com.sebulli.fakturama.handler.newProduct";
	public static final String CMD_NEW_VAT = "com.sebulli.fakturama.handler.newVat";
	public static final String CMD_NEW_DOCUMENT = "com.sebulli.fakturama.handler.newDocument";
	public static final String CMD_NEW_PAYMENT = "com.sebulli.fakturama.handler.newPayment";
	public static final String CMD_NEW_SHIPPING = "com.sebulli.fakturama.handler.newShipping";
	public static final String CMD_NEW_TEXT = "com.sebulli.fakturama.handler.newText";
	public static final String CMD_NEW_LISTENTRY = "com.sebulli.fakturama.handler.newListEntry";
	public static final String CMD_NEW_EXPENDITUREVOUCHER = "com.sebulli.fakturama.handler.newExpenditureVoucher";
	public static final String CMD_NEW_RECEIPTVOUCHER = "com.sebulli.fakturama.handler.newReceiptVoucher";

	public static final String CMD_NEW_ = "com.sebulli.fakturama.handler.new";

	public static final String CMD_CREATE_OODOCUMENT = "com.sebulli.fakturama.handler.createOODocument";
	public static final String CMD_SAVE = "com.sebulli.fakturama.handler.save";

	public static final String CMD_DELETE_DATASET = "com.sebulli.fakturama.handler.deleteDataSet";

	public static final String CMD_SELECT_WORKSPACE = "com.sebulli.fakturama.handler.selectWorkspace";

	public static final String CMD_WEBSHOP_IMPORT = "com.sebulli.fakturama.handler.webShopImport";

	public static final String CMD_MARK_ORDER_AS = "com.sebulli.fakturama.handler.markOrderAs";
	public static final String CMD_MARK_DOCUMENT_AS_PAID = "com.sebulli.fakturama.handler.markDocumentAsPaid";

	public static final String CMD_IMPORT_CSV = "com.sebulli.fakturama.handler.importCSV";

	public static final String CMD_EXPORT_SALES_SUMMARY = "com.sebulli.fakturama.handler.exportSalesSummary";

	public static final String CMD_OPEN_BROWSER_EDITOR = "com.sebulli.fakturama.handler.openBrowserEditor";
	public static final String CMD_OPEN_CALCULATOR = "com.sebulli.fakturama.handler.openCalculator";

	public static final String CMD_P2_UPDATE = "com.sebulli.fakturama.handler.update";
	public static final String CMD_P2_INSTALL = "com.sebulli.fakturama.handler.install";
	
	public static final String CMD_OPEN_PARCEL_SERVICE = "com.sebulli.fakturama.handler.openParcelService";

	public static final String CMD_MOVE_UP = "com.sebulli.fakturama.handler.moveEntryUp";
	public static final String CMD_MOVE_DOWN = "com.sebulli.fakturama.handler.moveEntryDown";
	
	public static final String CMD_REOGANIZE_DOCUMENTS = "com.sebulli.fakturama.handler.reorganizeDocuments";
	
	
}
