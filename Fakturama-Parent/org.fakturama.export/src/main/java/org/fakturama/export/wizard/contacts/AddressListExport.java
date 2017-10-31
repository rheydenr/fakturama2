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


import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.fakturama.export.wizard.CellFormatter;
import org.fakturama.export.wizard.OOCalcExporter;
import org.odftoolkit.odfdom.type.Color;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * This class generates a list with all contacts
 * 
 * @author Gerd Bartelt
 */
public class AddressListExport extends OOCalcExporter {
	
	@Inject
	private ContactsDAO contactsDAO;
    
    @Inject
    private IEclipseContext context;

	/**
	 * 	Do the export job.
	 * 
	 * @return
	 * 			<code>true</code> if the export was successful
	 */
	public boolean export() {
		ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);

		// Try to generate a spreadsheet
		if (!createSpreadSheet())
			return false;

		// Get all undeleted contacts
		List<Contact> contacts = contactsDAO.findAll();
		
		// if no data, return immediately
		if(contacts.isEmpty()) {
			MessageDialog.openInformation(shell, msg.dialogMessageboxTitleInfo, exportMessages.wizardCommonNodata);
			return true;
		}

		// Counter for the current row and columns in the Calc document
		int row = 0;
		int col = 0;

		//T: Table heading 
		String deliveryAddress = " ("+msg.commonFieldDeliveryaddress+")";

		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, "ID");
		setCellTextInBold(row, col++, msg.editorContactFieldNumberName);
		setCellTextInBold(row, col++, "TYPE");
		setCellTextInBold(row, col++, msg.commonFieldCategory);
		setCellTextInBold(row, col++, msg.commonFieldGender);
		setCellTextInBold(row, col++, msg.commonFieldTitle);
		setCellTextInBold(row, col++, msg.commonFieldFirstname);
		setCellTextInBold(row, col++, msg.commonFieldLastname);
		setCellTextInBold(row, col++, msg.commonFieldCompany);
		setCellTextInBold(row, col++, msg.commonFieldStreet);
		setCellTextInBold(row, col++, msg.commonFieldZipcode);
		setCellTextInBold(row, col++, msg.commonFieldCity);
		setCellTextInBold(row, col++, msg.commonFieldCountry);
		setCellTextInBold(row, col++, msg.commonFieldGender + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldTitle + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldFirstname + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldLastname + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldCompany + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldStreet + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldZipcode + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldCity + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldCountry + deliveryAddress);
		setCellTextInBold(row, col++, msg.commonFieldAccountholder);
		setCellTextInBold(row, col++, msg.commonFieldAccount);
		setCellTextInBold(row, col++, msg.editorContactFieldBankcodeName);
		setCellTextInBold(row, col++, msg.editorContactFieldBankName);
		setCellTextInBold(row, col++, msg.exporterDataIban);
		setCellTextInBold(row, col++, msg.exporterDataBic);
		setCellTextInBold(row, col++, msg.editorContactLabelNotice);
		setCellTextInBold(row, col++, msg.commonFieldDate);
		setCellTextInBold(row, col++, msg.editorContactFieldPaymentName);
		setCellTextInBold(row, col++, msg.editorContactFieldReliabilityName);
		setCellTextInBold(row, col++, msg.exporterDataTelephone);
		setCellTextInBold(row, col++, msg.exporterDataTelefax);
		setCellTextInBold(row, col++, msg.exporterDataMobile);
		setCellTextInBold(row, col++, msg.exporterDataEmail);
		setCellTextInBold(row, col++, msg.exporterDataWebsite);
		setCellTextInBold(row, col++, msg.exporterDataVatno);
		setCellTextInBold(row, col++, msg.exporterDataVatnoValid);
		setCellTextInBold(row, col++, msg.exporterDataRebate);
		setCellTextInBold(row, col++, msg.editorContactFieldBirthdayName);
		
		// Draw a horizontal line
		for (int c = 0; c < col; c++) {
			setBorder(row, c, Color.BLACK, false, false, true, false);
		}
		row++;
		
		// Export the document data
		for (Contact contact : contacts) {
			
			col = 0;
			
			// Place the contact information into the table
			setCellText(row, col++, contact.getCustomerNumber());
			setCellText(row, col++, contact.getClass().getSimpleName());
			if(contact.getCategories() != null) {
				setCellText(row, col++, contact.getCategories().getName());
			} else {
				col++;
			}
			setCellText(row, col++, contactUtil.getGenderString(contact.getGender()));
			setCellText(row, col++, contact.getTitle());
			setCellText(row, col++, contact.getFirstName());
			setCellText(row, col++, contact.getName());
			setCellText(row, col++, contact.getCompany());
			
			if(contact.getAddress() != null) {
				setCellText(row, col++, contact.getAddress().getStreet());
				setCellText(row, col++, contact.getAddress().getZip());
				setCellText(row, col++, contact.getAddress().getCity());
				setCellText(row, col++, LocaleUtil.getInstance().findByCode(contact.getAddress().getCountryCode()).orElse(LocaleUtil.getInstance().getDefaultLocale()).getDisplayCountry());
			} else {
				col += 4;
			}
			
			Contact deliveryContact;
			if (contact.getAlternateContacts() != null) {
				deliveryContact = contact.getAlternateContacts();
			} else {
				deliveryContact = contact;
			}
			setCellText(row, col++, contactUtil.getGenderString(deliveryContact.getGender()));
			setCellText(row, col++, deliveryContact.getTitle());
			setCellText(row, col++, deliveryContact.getFirstName());
			setCellText(row, col++, deliveryContact.getName());
			setCellText(row, col++, deliveryContact.getCompany());
			if (deliveryContact.getAddress() != null) {
				setCellText(row, col++, deliveryContact.getAddress().getStreet());
				setCellText(row, col++, deliveryContact.getAddress().getZip());
				setCellText(row, col++, deliveryContact.getAddress().getCity());
				setCellText(row, col++, LocaleUtil.getInstance().findByCode(deliveryContact.getAddress().getCountryCode()).orElse(LocaleUtil.getInstance().getDefaultLocale()).getDisplayCountry());
			} else {
				col += 4;
			}
			
			if(contact.getBankAccount() != null) {
				setCellText(row, col++, contact.getBankAccount().getAccountHolder());
				setCellText(row, col++, contact.getBankAccount().getName());
				setCellText(row, col++, contact.getBankAccount().getBankCode() != null ? contact.getBankAccount().getBankCode().toString() : "");
				setCellText(row, col++, contact.getBankAccount().getBankName());
				setCellText(row, col++, contact.getBankAccount().getIban());
				setCellText(row, col++, contact.getBankAccount().getBic());
			} else {
				col += 6;
			}
			
			setCellText(row, col++, contact.getNote());
			setCellText(row, col++, DateFormat.getDateInstance().format(contact.getDateAdded()));
			
			if(contact.getPayment() != null) {
				setCellText(row, col++, contact.getPayment().getDescription());
			} else {
				col++;
			}
			
			if(contact.getReliability() != null) {
				setCellText(row, col++, contactUtil.getReliabilityString(contact.getReliability()));
			} else {
				col++;
			}
			
			setCellText(row, col++, contact.getPhone());
			setCellText(row, col++, contact.getFax());
			setCellText(row, col++, contact.getMobile());
			setCellText(row, col++, contact.getEmail());
			setCellText(row, col++, contact.getWebsite());
			setCellText(row, col++, contact.getVatNumber());
			setCellValueAsBoolean(row, col++, contact.getVatNumberValid());
			setCellValueAsPercent(row, col++, contact.getDiscount());
			if(contact.getBirthday() != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(contact.getBirthday());
				setCellValueAsDate(row, col++, cal);
			} else {
				col++;
			} 

			// Alternate the background color
			if ((row % 2) == 0)
				setBackgroundColor( 0, row, col-1, row, CellFormatter.ALTERNATE_BACKGROUND_COLOR);

			row++;
		}
		
		save();

		// True = Export was successful
		return true;
	}
	
	@Override
	protected String getOutputFileName() {
		return exportMessages.wizardExportContactsAllcontactsDefaultfilename;
	}

}
