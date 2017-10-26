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

package org.fakturama.imp.wizard.csv.contacts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.imp.ImportMessages;

import com.opencsv.CSVReader;
import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.ReliabilityType;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * CSV importer for {@link Contact}s
 */
public class ContactsCsvImporter {
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	@Translation
	protected Messages msg;
    
    @Inject
    protected Logger log;
	
	@Inject
	private ContactsDAO contactsDAO;
	
    @Inject
    private ContactCategoriesDAO contactCategoriesDAO;
    
    @Inject
	private ContactUtil contactUtil;
    
    @Inject
    private PaymentsDAO paymentsDAO;

    
	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory;

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { "category", "gender", "title", "firstname", "name", "company", "street", "zip", "city", "country",
			"delivery_gender", "delivery_title", "delivery_firstname", "delivery_name", "delivery_company",
			"delivery_street", "delivery_zip", "delivery_city", "delivery_country",
			"account_holder", "account", "bank_code", "bank_name", "iban", "bic",
			"nr", "note", "date_added",  "payment", "reliability",
			"phone", "fax", "mobile", "email", "website", "vatnr", "vatnrvalid", "discount" };

	// The result string
	String result = "";

	// NewLine
	String NL = System.lineSeparator();

	/**
	 * Returns if a column is in the list of required columns
	 * 
	 * @param columnName
	 *            The name of the column to test
	 * @return <code>true</code>, if this column is in the list of required columns
	 */
	private boolean isRequiredColumn(String columnName) {
		// Test all columns
		return Arrays.stream(requiredHeaders).anyMatch(col -> columnName.equalsIgnoreCase(col));
	}


	/**
	 * The import procedure
	 * 
	 * @param fileName
	 *            Name of the file to import
	 * @param classifier
	 *            the contact classifier (used from {@link FakturamaModelPackage})
	 * @param updateExisting
	 *            if <code>true</code>, also existing entries will be updated
	 * @param importEmptyValues
	 *            if <code>true</code>, also empty values will be updated
	 */
	public void importCSV(final String fileName, int classifier, boolean updateExisting, boolean importEmptyValues) {
		modelFactory = FakturamaModelPackage.MODELFACTORY;

		// Result string
		//T: Importing + .. FILENAME
		result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

		// Count the imported contacts
		int importedContacts = 0;
		int updatedContacts = 0;

		// Count the line of the import file
		int lineNr = 0;

		String[] columns;
	
		// Open the existing file
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			 BufferedReader in = new BufferedReader(isr);
			 CSVReader csvr = new CSVReader(in, ';');	) {
			
			// Read next CSV line
			columns = csvr.readNext();
			
			if (columns.length < 5) {
				//T: Error message
				result += NL + importMessages.wizardImportErrorFirstline;
				return;
			}

		// Read the existing file and store it in a buffer
		// with a fix size. Only the newest lines are kept.

			// Read line by line
			String[] cells;
			while ((cells = csvr.readNext()) != null) {
				lineNr++;

				Contact contact;
				switch (classifier) {
				case FakturamaModelPackage.DEBITOR_CLASSIFIER_ID:
					contact = modelFactory.createDebitor();
					break;
				case FakturamaModelPackage.CREDITOR_CLASSIFIER_ID:
					contact = modelFactory.createCreditor();
					break;
				default:
					contact = modelFactory.createDebitor();
					break;
				}
				Properties prop = new Properties();

				// Dispatch all the cells into a property
				for (int col = 0; col < cells.length; col++) {
					if (col < columns.length && isRequiredColumn(columns[col])) {
						prop.setProperty(columns[col].toLowerCase(), cells[col]);
					}
				}

				// Test if all columns are used
				if (prop.size() > 0 && prop.size() != requiredHeaders.length) {
					for (int i = 0; i < requiredHeaders.length; i++) {
						if (!prop.containsKey(requiredHeaders[i]))
							//T: Format: LINE: xx: NO DATA IN COLUMN yy FOUND.
							result += NL 
								+ MessageFormat.format(importMessages.wizardImportErrorNodatafound, Integer.toString(lineNr),
							    "\"" + requiredHeaders[i] + "\""); 
					}
				}
				else {
					// check some attributes to get an existing entry
					contact.setCustomerNumber(prop.getProperty("nr"));
					contact.setFirstName(prop.getProperty("firstname"));
					contact.setName(prop.getProperty("name"));
					Address address = modelFactory.createAddress();
					address.setZip(prop.getProperty("zip"));
					contact.setAddress(address);
					/*
					 * Customer number, first name, name and ZIP are compared
					 */
					Contact testContact = contactsDAO.findOrCreate(contact);
					
					ContactCategory category = contactCategoriesDAO.findByName(prop.getProperty("category"));
					if(category == null && prop.getProperty("category") != null) {
						category = modelFactory.createContactCategory();
						category.setName(prop.getProperty("category"));
					}
					// work further with testcontact
					testContact.setCategories(category);
					testContact.setGender(contactUtil.getGenderID(prop.getProperty("gender")));

					testContact.setTitle(prop.getProperty("title"));
					testContact.setCompany(prop.getProperty("company"));
					
					// if previous address is given use it
					if(testContact.getAddress() != null) {
						address = testContact.getAddress();
					}
					address.setValidFrom(Calendar.getInstance().getTime());
					address.setStreet(prop.getProperty("street"));
					address.setCity(prop.getProperty("city"));
//					address.setCountryCode(prop.getProperty("country")); TODO get correct country code!
					testContact.setAddress(address);

					Debitor deliveryContact = testContact.getAlternateContacts() != null ? (Debitor) testContact.getAlternateContacts() : modelFactory.createDebitor();
					deliveryContact.setGender(contactUtil.getGenderID(prop.getProperty("delivery_gender")));
					deliveryContact.setTitle(prop.getProperty("delivery_title"));
					deliveryContact.setFirstName(prop.getProperty("delivery_firstname"));
					deliveryContact.setName(prop.getProperty("delivery_name"));
					deliveryContact.setCompany(prop.getProperty("delivery_company"));
					
					Address deliveryAddress = deliveryContact.getAddress() != null ? deliveryContact.getAddress() : modelFactory.createAddress();
					deliveryAddress.setValidFrom(Calendar.getInstance().getTime());
					deliveryAddress.setStreet(prop.getProperty("delivery_street"));
					deliveryAddress.setZip(prop.getProperty("delivery_zip"));
					deliveryAddress.setCity(prop.getProperty("delivery_city"));
//					deliveryAddress.setCountryCode(prop.getProperty("delivery_country")); // FIXME set correct country code!!!!
					deliveryContact.setAddress(deliveryAddress);
					testContact.setAlternateContacts(deliveryContact);

					BankAccount account = testContact.getBankAccount() != null ? testContact.getBankAccount() : modelFactory.createBankAccount();
					account.setValidFrom(Calendar.getInstance().getTime());
					account.setAccountHolder(prop.getProperty("account_holder"));
					account.setName(prop.getProperty("account"));
//					testContact.setStringValueByKey("bank_code", prop.getProperty("bank_code"));
					account.setBankName(prop.getProperty("bank_name"));
					account.setIban(prop.getProperty("iban"));
					account.setBic(prop.getProperty("bic"));
					testContact.setBankAccount(account);
					
					testContact.setNote(prop.getProperty("note"));
					
					if (prop.getProperty("date_added").isEmpty()) {
						testContact.setDateAdded(Calendar.getInstance().getTime());
					} else {
						testContact.setDateAdded(DataUtils.getInstance().getCalendarFromDateString(prop.getProperty("date_added")).getTime());
					}
					
					Payment payment = paymentsDAO.findByName(prop.getProperty("payment"));
					if(payment != null) {
						testContact.setPayment(payment);
					}
					testContact.setReliability(ReliabilityType.getByName(prop.getProperty("reliability")));

					testContact.setPhone(prop.getProperty("phone"));
					testContact.setFax(prop.getProperty("fax"));
					testContact.setMobile(prop.getProperty("mobile"));
					testContact.setEmail(prop.getProperty("email"));
					testContact.setWebsite(prop.getProperty("website"));
					testContact.setVatNumber(prop.getProperty("vatnr"));
					testContact.setVatNumberValid(BooleanUtils.toBooleanObject(prop.getProperty("vatnrvalid")));
					testContact.setDiscount(DataUtils.getInstance().StringToDouble(prop.getProperty("discount")));
					
					// Add the contact to the data base
					if (DateUtils.isSameDay(testContact.getDateAdded(), Calendar.getInstance().getTime())) {
						importedContacts++;
					} else if (updateExisting) {
						// Update data
						updatedContacts++;
					}
					// Update the modified contact data
					contactsDAO.update(testContact);
				}
			}
			
			// The result string
			//T: Message: xx Contacts have been imported 
			result += NL + importedContacts + " " + importMessages.wizardImportInfoContactsimported;
			if (updatedContacts > 0)
				result += NL + updatedContacts + " " + importMessages.wizardImportInfoContactsupdated;
		}
		
		catch (UnsupportedEncodingException e) {
			log.error(e, "Unsupported UTF-8 encoding");
			result += NL + "Unsupported UTF-8 encoding";
			return;
		}
		catch (FileNotFoundException e) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorFilenotfound;
			return;
		}
		catch (IOException e) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorOpenfile;
		}
		catch (FakturamaStoringException e) {
			log.error("can't save or update imported contact");
		}
	}

	public String getResult() {
		return result;
	}

}
