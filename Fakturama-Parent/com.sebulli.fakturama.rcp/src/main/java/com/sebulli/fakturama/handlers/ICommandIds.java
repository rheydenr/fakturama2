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

	public static final String CMD_NEW_CONTACT = "com.sebulli.fakturama.command.newContact";
	public static final String CMD_NEW_PRODUCT = "com.sebulli.fakturama.command.newProduct";
	public static final String CMD_NEW_VAT = "com.sebulli.fakturama.command.newVat";
	
	public static final String CMD_NEW_DOCUMENT = "com.sebulli.fakturama.command.newDocument";
	public static final String CMD_NEW_LETTER = "com.sebulli.fakturama.command.newLetter";
	public static final String CMD_NEW_OFFER = "com.sebulli.fakturama.command.newOffer";
	public static final String CMD_NEW_ORDER = "com.sebulli.fakturama.command.newOrder";
	public static final String CMD_NEW_CONFIRMATION = "com.sebulli.fakturama.command.newConfirmation";
	public static final String CMD_NEW_INVOICE = "com.sebulli.fakturama.command.newInvoice";
	public static final String CMD_NEW_DELIVERY = "com.sebulli.fakturama.command.newDelivery";
	public static final String CMD_NEW_DELIVERYNOTE = "com.sebulli.fakturama.command.newDeliverynote";
	public static final String CMD_NEW_CREDIT = "com.sebulli.fakturama.command.newCredit";
	public static final String CMD_NEW_DUNNING = "com.sebulli.fakturama.command.newDunning";
	public static final String CMD_NEW_PROFORMA = "com.sebulli.fakturama.command.newProforma";
	
	public static final String CMD_NEW_PAYMENT = "com.sebulli.fakturama.command.newPayment";
	public static final String CMD_NEW_SHIPPING = "com.sebulli.fakturama.command.newShipping";
	public static final String CMD_NEW_TEXT = "com.sebulli.fakturama.command.newText";
	public static final String CMD_NEW_LISTENTRY = "com.sebulli.fakturama.command.newListEntry";
	public static final String CMD_NEW_EXPENDITUREVOUCHER = "com.sebulli.fakturama.command.newExpenditureVoucher";
	public static final String CMD_NEW_RECEIPTVOUCHER = "com.sebulli.fakturama.command.newReceiptVoucher";

	public static final String CMD_NEW_ = "com.sebulli.fakturama.command.new";

	public static final String CMD_CREATE_OODOCUMENT = "com.sebulli.fakturama.command.createOODocument";
	public static final String CMD_SAVE = "com.sebulli.fakturama.command.save";

	public static final String CMD_DELETE_DATASET = "com.sebulli.fakturama.command.deleteDataSet";

	public static final String CMD_SELECT_WORKSPACE = "com.sebulli.fakturama.command.selectWorkspace";

	public static final String CMD_WEBSHOP_IMPORT = "com.sebulli.fakturama.command.webShopImport";

	public static final String CMD_MARK_ORDER_AS = "com.sebulli.fakturama.command.markOrderAs";
	public static final String CMD_MARK_DOCUMENT_AS_PAID = "com.sebulli.fakturama.command.markDocumentAsPaid";

	public static final String CMD_IMPORT_CSV = "com.sebulli.fakturama.command.importCSV";

	public static final String CMD_EXPORT_SALES_SUMMARY = "com.sebulli.fakturama.command.exportSalesSummary";

	public static final String CMD_OPEN_BROWSER_EDITOR = "com.sebulli.fakturama.command.openBrowserEditor";
	public static final String CMD_OPEN_CALCULATOR = "com.sebulli.fakturama.command.openCalculator";

	public static final String CMD_P2_UPDATE = "com.sebulli.fakturama.command.update";
	public static final String CMD_P2_INSTALL = "com.sebulli.fakturama.command.install";
	
	public static final String CMD_OPEN_PARCEL_SERVICE = "com.sebulli.fakturama.command.openParcelService";

	public static final String CMD_MOVE_UP = "com.sebulli.fakturama.command.moveEntryUp";
	public static final String CMD_MOVE_DOWN = "com.sebulli.fakturama.command.moveEntryDown";
	
	public static final String CMD_REOGANIZE_DOCUMENTS = "com.sebulli.fakturama.command.reorganizeDocuments";
	
	
}
