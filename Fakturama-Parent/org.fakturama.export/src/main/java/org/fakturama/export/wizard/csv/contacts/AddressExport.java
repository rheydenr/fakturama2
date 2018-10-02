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

package org.fakturama.export.wizard.csv.contacts;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.fakturama.wizards.ExporterHelper;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.util.ContactUtil;


/**
 * This class generates a list with all contacts
 * 
 */
public class AddressExport {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-mm-dd");
    
	@Inject
	private ILocaleService localeUtil;

	@Inject
	private ContactsDAO contactsDAO;
    
    @Inject
    private IEclipseContext context;
    
	@Inject
	private INumberFormatterService numberFormatterService;

	/**
	 * 	Do the export job.
	 * 
	 * @param filename
	 * 			The name of the export file
	 * @return
	 * 			True, if the export was successful
	 */
	public boolean export(String filename) {
		ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);

		String NEW_LINE = System.lineSeparator();
		
		// Create a File object
		Path csvFile = Paths.get(filename);
		
		// Create a new file
		try (BufferedWriter bos = Files.newBufferedWriter(csvFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);){

			// TODO Use OpenCSV
			
			bos.write(
					//T: Used as heading of a table. Keep the word short.
					"\"id\";"+ 
					"\"category\";"+
					
					"\"gender\";"+
					"\"title\";"+
					"\"firstname\";"+
					"\"name\";"+
					"\"company\";"+
					"\"street\";"+
					"\"zip\";"+
					"\"city\";"+
					"\"country\";"+

					"\"delivery_gender\";"+
					"\"delivery_title\";"+
					"\"delivery_firstname\";"+
					"\"delivery_name\";"+
					"\"delivery_company\";"+
					"\"delivery_street\";"+
					"\"delivery_zip\";"+
					"\"delivery_city\";"+
					"\"delivery_country\";"+
					
					"\"account_holder\";"+
					"\"account\";"+
					"\"bank_code\";"+
					"\"bank_name\";"+
					"\"iban\";"+
					"\"bic\";"+
					
					"\"nr\";"+

					"\"note\";"+
					"\"date_added\";"+
					"\"payment\";"+
					"\"reliability\";"+
					"\"phone\";"+
					"\"fax\";"+
					"\"mobile\";"+
					"\"email\";"+
					"\"website\";"+
					"\"vatnr\";"+
					"\"vatnrvalid\";"+
					"\"discount\";"+
					"\"birthday\""+
					NEW_LINE);
		
			// Get all undeleted contacts
			List<Contact> contacts = contactsDAO.findAll();
			
			// Export the contact data
			for (Contact contact : contacts) {
				
				// Place the contacts information into the table
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(contact.getId()).append(";");
				if(contact.getCategories() != null) {
					stringBuffer.append(ExporterHelper.inQuotes(contact.getCategories().getName()));
				}
				stringBuffer.append(";")
					.append(ExporterHelper.inQuotes(contactUtil.getSalutationString(contact.getGender()))).append(";")
					.append(ExporterHelper.inQuotes(contact.getTitle())).append(";")
					.append(ExporterHelper.inQuotes(contact.getFirstName())).append(";")
					.append(ExporterHelper.inQuotes(contact.getName())).append(";")
					.append(ExporterHelper.inQuotes(contact.getCompany())).append(";");
				
				if(contact.getAddress() != null) {
						stringBuffer.append(ExporterHelper.inQuotes(contact.getAddress().getStreet())).append(";")
						   .append(ExporterHelper.inQuotes(contact.getAddress().getZip())).append(";")
						   .append(ExporterHelper.inQuotes(contact.getAddress().getCity())).append(";")
						   .append(ExporterHelper.inQuotes(localeUtil.findByCode(contact.getAddress().getCountryCode()).orElse(localeUtil.getDefaultLocale()).getDisplayCountry())).append(";");
				} else {
					stringBuffer.append(";;;;");
				}
		
				
				Contact deliveryContact;
				if (contact.getAlternateContacts() != null) {
					deliveryContact = contact.getAlternateContacts();
				} else {
					deliveryContact = contact;
				}
				stringBuffer.append(ExporterHelper.inQuotes(contactUtil.getSalutationString(deliveryContact.getGender()))).append(";")
				   .append(ExporterHelper.inQuotes(deliveryContact.getTitle())).append(";")
				   .append(ExporterHelper.inQuotes(deliveryContact.getFirstName())).append(";")
				   .append(ExporterHelper.inQuotes(deliveryContact.getName())).append(";")
				   .append(ExporterHelper.inQuotes(deliveryContact.getCompany())).append(";");
				if (deliveryContact.getAddress() != null) {
					stringBuffer.append(ExporterHelper.inQuotes(deliveryContact.getAddress().getStreet())).append(";")
					   .append(ExporterHelper.inQuotes(deliveryContact.getAddress().getZip())).append(";")
					   .append(ExporterHelper.inQuotes(deliveryContact.getAddress().getCity())).append(";")
					   .append(ExporterHelper.inQuotes(localeUtil.findByCode(deliveryContact.getAddress().getCountryCode()).orElse(localeUtil.getDefaultLocale()).getDisplayCountry())).append(";");
				} else {
					stringBuffer.append(";;;;");
				}
				
				if(contact.getBankAccount() != null) {
					stringBuffer.append(ExporterHelper.inQuotes(contact.getBankAccount().getAccountHolder())).append(";")
					   .append(ExporterHelper.inQuotes(contact.getBankAccount().getName())).append(";")
					   .append(ExporterHelper.inQuotes(contact.getBankAccount().getBankCode() != null ? Integer.toString(contact.getBankAccount().getBankCode()) : "")).append(";")
					   .append(ExporterHelper.inQuotes(contact.getBankAccount().getBankName())).append(";")
					   .append(ExporterHelper.inQuotes(contact.getBankAccount().getIban())).append(";")
					   .append(ExporterHelper.inQuotes(contact.getBankAccount().getBic())).append(";");
				} else {
					stringBuffer.append(";;;;;;");
				}
				
				stringBuffer.append(ExporterHelper.inQuotes(contact.getCustomerNumber())).append(";")
				   .append(ExporterHelper.inQuotes(contact.getNote())).append(";")
				   .append(DateFormat.getDateInstance().format(contact.getDateAdded())).append(";");
				
				if(contact.getPayment() != null) {
					stringBuffer.append(ExporterHelper.inQuotes(contact.getPayment().getDescription()));
				}
				stringBuffer.append(";");
				
				if(contact.getReliability() != null) {
					stringBuffer.append(ExporterHelper.inQuotes(contactUtil.getReliabilityString(contact.getReliability())));
				}
				stringBuffer.append(";");
				
				stringBuffer.append(ExporterHelper.inQuotes(contact.getPhone())).append(";")
				   .append(ExporterHelper.inQuotes(contact.getFax())).append(";")
				   .append(ExporterHelper.inQuotes(contact.getMobile())).append(";")
				   .append(ExporterHelper.inQuotes(contact.getEmail())).append(";")
				   .append(ExporterHelper.inQuotes(contact.getWebsite())).append(";")
				   .append(ExporterHelper.inQuotes(contact.getVatNumber())).append(";")
				   .append(BooleanUtils.isTrue(contact.getVatNumberValid())).append(";")
				   .append(ExporterHelper.inQuotes(numberFormatterService.DoubleToDecimalFormatedValue(contact.getDiscount(), "0.00"))).append(";");
				
				if(contact.getBirthday() != null) {
					stringBuffer.append(sdf.format(contact.getBirthday()));
				}				
				stringBuffer.append(";");

				bos.write(stringBuffer.toString() + NEW_LINE);
			}
		}
		catch (IOException e) {
			return false;
		}

		// True = Export was successful
		return true;
	}
	
	

}
