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
import java.util.Calendar;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.imp.ImportMessages;

import com.opencsv.CSVReader;
import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
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
    

	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory;

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { "category", "gender", "title", "firstname", "name", "company", "street", "zip", "city", "country",
			"delivery_gender", "delivery_title", "delivery_firstname", "delivery_name", "delivery_company",
			"delivery_street", "delivery_zip", "delivery_city", "delivery_country",
			"account_holder", "account", "bank_code", "bank_name", "iban", "bic",
			"nr", "note", "date_added", /* "payment",*/ "reliability",
			"phone", "fax", "mobile", "email", "website", "vatnr", "vatnrvalid", "discount" };

	// The result string
	String result = "";

	// NewLine
	String NL = System.lineSeparator();

	/**
	 * Returns if a column is in the list of required columns
	 * 
	 * @param columnName
	 *            The name of the columns to test
	 * @return <code>true</code>, if this column is in the list of required columns
	 */
	private boolean isRequiredColumn(String columnName) {

		// Test all columns
		for (int i = 0; i < requiredHeaders.length; i++) {
			if (columnName.equalsIgnoreCase(requiredHeaders[i]))
				return true;
		}
		return false;
	}


	/**
	 * The import procedure
	 * 
	 * @param fileName
	 *            Name of the file to import
	 * @param test
	 *            if <code>true</code>, the dataset are not imported (currently not used)
	 * @param updateExisting
	 *            if <code>true</code>, also existing entries will be updated
	 * @param importEmptyValues
	 *            if <code>true</code>, also empty values will be updated
	 */
	public void importCSV(final String fileName, boolean test, boolean updateExisting, boolean importEmptyValues) {
		ContactUtil contactUtil = new ContactUtil();
		modelFactory = FakturamaModelPackage.MODELFACTORY;

		// Result string
		//T: Importing + .. FILENAME
		result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

		// Count the imported contacts
		int importedContacts = 0;
		int updatedContacts = 0;

		// Count the line of the import file
		int lineNr = 0;

		CSVReader csvr = null;
	
		// Open the existing file
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			 BufferedReader in = new BufferedReader(isr)) {
			
			csvr = new CSVReader(in, ';');
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
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return;
		}

		String[] columns;

		// Read the first line
		try {
		
			// Read next CSV line
			columns = csvr.readNext();
			
			if (columns.length < 5) {
				//T: Error message
				result += NL + importMessages.wizardImportErrorFirstline;
				return;
			}
		}
		catch (IOException e1) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorFirstline;
			return;
		}

		// Read the existing file and store it in a buffer
		// with a fix size. Only the newest lines are kept.
		try {

			// Read line by line
			String[] cells;
			while ((cells = csvr.readNext()) != null) {
				lineNr++;

				Contact contact =  modelFactory.createDebitor();
				Properties prop = new Properties();

				// Dispatch all the cells into a property
				for (int col = 0; col < cells.length; col++) {
					if (col < columns.length) {

						if (isRequiredColumn(columns[col])) {
							prop.setProperty(columns[col].toLowerCase(), cells[col]);
						}
					}
				}

				// Test if all columns are used
				if ((prop.size() > 0) && (prop.size() != requiredHeaders.length)) {
					for (int i = 0; i < requiredHeaders.length; i++) {
						if (!prop.containsKey(requiredHeaders[i]))
							//T: Format: LINE: xx: NO DATA IN COLUMN yy FOUND.
							result += NL 
								+ MessageFormat.format(importMessages.wizardImportErrorNodatafound, Integer.toString(lineNr) 
							    + "\"" + requiredHeaders[i] + "\""); 
					}
				}
				else {
					ContactCategory category = contactCategoriesDAO.findByName(prop.getProperty("category"));
					contact.setCategories(category);
					contact.setGender(contactUtil.getGenderID(prop.getProperty("gender")));

					contact.setTitle(prop.getProperty("title"));
					contact.setFirstName(prop.getProperty("firstname"));
					contact.setName(prop.getProperty("name"));
					contact.setCompany(prop.getProperty("company"));
					
					Address address = modelFactory.createAddress();
					address.setStreet(prop.getProperty("street"));
					address.setZip(prop.getProperty("zip"));
					address.setCity(prop.getProperty("city"));
//					address.setCountryCode(prop.getProperty("country")); TODO get correct country code!
					contact.setAddress(address);

					Debitor deliveryContact = modelFactory.createDebitor();
					deliveryContact.setGender(contactUtil.getGenderID(prop.getProperty("delivery_gender")));
					deliveryContact.setTitle(prop.getProperty("delivery_title"));
					deliveryContact.setFirstName(prop.getProperty("delivery_firstname"));
					deliveryContact.setName(prop.getProperty("delivery_name"));
					deliveryContact.setCompany(prop.getProperty("delivery_company"));
					
					Address deliveryAddress = modelFactory.createAddress();
					deliveryAddress.setStreet(prop.getProperty("delivery_street"));
					deliveryAddress.setZip(prop.getProperty("delivery_zip"));
					deliveryAddress.setCity(prop.getProperty("delivery_city"));
//					deliveryAddress.setCountryCode(prop.getProperty("delivery_country")); // FIXME set correct country code!!!!
					contact.setAlternateContacts(deliveryContact);

					BankAccount account = modelFactory.createBankAccount();
					account.setAccountHolder(prop.getProperty("account_holder"));
					account.setName(prop.getProperty("account"));
//					contact.setStringValueByKey("bank_code", prop.getProperty("bank_code"));
					account.setBankName(prop.getProperty("bank_name"));
					account.setIban(prop.getProperty("iban"));
					account.setBic(prop.getProperty("bic"));
					
					contact.setCustomerNumber(prop.getProperty("nr"));
					contact.setNote(prop.getProperty("note"));
					
					if (prop.getProperty("date_added").isEmpty()) {
						contact.setDateAdded(Calendar.getInstance().getTime());
					} else {
						contact.setDateAdded(DataUtils.getInstance().getCalendarFromDateString(prop.getProperty("date_added")).getTime());
					}
					
					//contact.setStringValueByKey("payment", prop.getProperty("payment"));
					contact.setReliability(ReliabilityType.getByName(prop.getProperty("reliability")));

					contact.setPhone(prop.getProperty("phone"));
					contact.setFax(prop.getProperty("fax"));
					contact.setMobile(prop.getProperty("mobile"));
					contact.setEmail(prop.getProperty("email"));
					contact.setWebsite(prop.getProperty("website"));
					contact.setVatNumber(prop.getProperty("vatnr"));
					contact.setVatNumberValid(BooleanUtils.toBooleanObject(prop.getProperty("vatnrvalid")));
					contact.setDiscount(Double.parseDouble(prop.getProperty("discount")));
					
					// Add the contact to the data base
					Contact testContact = contactsDAO.findOrCreate(contact);
					if (testContact.getDateAdded().equals(Calendar.getInstance().getTime())) {
						importedContacts++;
					} else if (updateExisting) {
						// Update data
						updatedContacts ++;
						
						// Update the modified contact data
						contactsDAO.update(testContact);
					}
				}
			}
			
			// The result string
			//T: Message: xx Contacts have been imported 
			result += NL + importedContacts + " " + importMessages.wizardImportInfoContactsimported;
			if (updatedContacts > 0)
				result += NL + updatedContacts + " " + importMessages.wizardImportInfoContactsupdated;
		}
		catch (IOException e) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorOpenfile;
		} catch (FakturamaStoringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (csvr != null) {
					csvr.close();
				}
			}
			catch (IOException e) {
				result += NL + msg.commonErrorClosefile;
			}
		}
	}

	public String getResult() {
		return result;
	}

}
