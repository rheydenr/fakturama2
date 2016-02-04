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

package org.fakturama.export.wizard.contacts;


import java.util.ArrayList;

/**
 * This class generates a list with all contacts
 * 
 * @author Gerd Bartelt
 */
public class AddressListExport /*extends OOCalcExporter*/{
	
	/**
	 * Constructor
	 * 
	 */
	public AddressListExport() {
		super();
	}

	/**
	 * 	Do the export job.
	 * 
	 * @return
	 * 			<code>true</code> if the export was successful
	 */
	public boolean export() {
//
//		// Try to generate a spreadsheet
//		if (!createSpreadSheet())
//			return false;
//
//		// Get all undeleted contacts
//		ArrayList<DataSetContact> contacts = Data.INSTANCE.getContacts().getActiveDatasets();
//
//		// Counter for the current row and columns in the Calc document
//		int row = 0;
//		int col = 0;
//
//		//T: Table heading 
//		String deliveryAddress = " ("+_("Delivery Address")+")";
//
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, "ID");
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Category"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Gender"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Title","ADDRESS"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("First Name"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++,_("Last Name"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Company"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Street"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("ZIP"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("City"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Country"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Gender")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Title","ADDRESS")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("First Name")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++,_("Last Name")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Company")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Street")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("ZIP")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("City")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Country")+ deliveryAddress);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Account Holder"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Account Number"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Bank Code"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Name of the Bank"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("IBAN"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("BIC"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Customer ID"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Notice"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Date"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Payment"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Reliability"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Telephone"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Telefax"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Mobile"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("E-Mail"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Web Site"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("VAT Number"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("VAT Number valid"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Discount","CUSTOMER"));
//		
//		// Draw a horizontal line
//		for (col = 0; col < 39; col++) {
//			setBorder(row, col, 0x000000, false, false, true, false);
//		}
//		row++;
//		
//		// Export the document data
//		for (DataSetContact contact : contacts) {
//			
//			col = 0;
//			
//			// Place the contact information into the table
//			setCellText(row, col++, contact.getFormatedStringValueByKey("id"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("category"));
//			setCellText(row, col++, DataSetContact.getGenderString(contact.getIntValueByKey("gender")));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("title"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("firstname"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("name"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("company"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("street"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("zip"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("city"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("country"));
//			setCellText(row, col++, DataSetContact.getGenderString(contact.getIntValueByKey("delivery_gender")));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("delivery_title"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("delivery_firstname"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("delivery_name"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("delivery_company"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("delivery_street"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("delivery_zip"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("delivery_city"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("delivery_country"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("account_holder"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("account"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("bank_code"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("bank_name"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("iban"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("bic"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("nr"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("note"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("date_added"));
//			setCellText(row, col++, contact.getFormatedStringValueByKeyFromOtherTable("payment.PAYMENTS:description"));
//			setCellText(row, col++, DataSetContact.getReliabilityString(contact.getIntValueByKey("reliability")));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("phone"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("fax"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("mobile"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("email"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("website"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("vatnr"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("vatnrvalid"));
//			setCellText(row, col++, contact.getFormatedStringValueByKey("discount"));
//
//			// Alternate the background color
//			//if ((row % 2) == 0)
//			//	setBackgroundColor( 0, row, col-1, row, 0x00e8ebed);
//
//			row++;
//		}

		// True = Export was successful
		return true;
	}

}
