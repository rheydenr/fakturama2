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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.imp.ImportMessages;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.ReliabilityType;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * CSV importer for {@link Contact}s
 */
public class ContactsCsvImporter {
	
	private static final String DELIVERY_FIELD_PREFIX = "delivery_";

	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	@Translation
	protected Messages msg;

    @Inject
    protected ILogger log;
	
	@Inject
	private ContactsDAO contactsDAO;
	
    @Inject
    private ContactCategoriesDAO contactCategoriesDAO;
    
    @Inject
	private ContactUtil contactUtil;
    
    @Inject
    private PaymentsDAO paymentsDAO;
    
    @Inject
    private IDateFormatterService dateFormatterService;
    
    @Inject
    private IDocumentAddressManager addressManager;

    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    private char quoteChar, separator;
    
	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory;

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { "category", "gender", "title", "firstname", "name", "company", "street", "zip", "city", "country",
			"delivery_gender", "delivery_title", "delivery_firstname", "delivery_name", "delivery_company",
			"delivery_street", "delivery_zip", "delivery_city", "delivery_country",
			"delivery_phone", "delivery_fax", "delivery_mobile", "delivery_email", 
			"account_holder", "bank_name", "iban", "bic",
			"nr", "note", "date_added",  "payment", "reliability",
			"phone", "fax", "mobile", "email", "website", "vatnr", "vatnrvalid", "discount", "supplier_nr", "username", "birthday" };

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
		int skippedContacts = 0;

		// Count the line of the import file
		int lineNr = 0;

		String[] columns;
		Path inputFile = Paths.get(fileName);
	
		// Open the existing file
		try (BufferedReader in = Files.newBufferedReader(inputFile)) {
			ICSVParser csvParser = new CSVParserBuilder().withSeparator(separator).withQuoteChar(quoteChar).build();
			CSVReader csvr = new CSVReaderBuilder(in).withCSVParser(csvParser).build();
			
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
					
					// TODO make BillingType changeable
					address.getContactTypes().add(ContactType.BILLING);
					contact.getAddresses().add(address);
					/*
					 * Customer number, first name, name and ZIP are compared
					 */
					Contact testContact = contactsDAO.findOrCreate(contact, true);
					
					// if found and no update is required skip to the next record
					if(testContact != null && !updateExisting) {
						skippedContacts++;
						continue;
					}
					
					if(testContact == null) {
						// work further with testcontact
						testContact = contact;
					}
					
					String categoryString = StringUtils.stripStart(prop.getProperty("category"), "/");
					if (categoryString != null) {
						ContactCategory category = contactCategoriesDAO.getCategory(prop.getProperty("category"), true);
						testContact.setCategories(category);
					}
					testContact.setGender(contactUtil.getSalutationID(prop.getProperty("gender")));

					testContact.setTitle(prop.getProperty("title"));
					testContact.setCompany(prop.getProperty("company"));
					
					// if previous address is given use it
					Address tmpAddress = addressManager.getAddressFromContact(testContact, ContactType.BILLING).orElse(null);
					if(tmpAddress != null) {
						address = tmpAddress;
					}
					address = createAddressFromProperties(prop, address, "");
					testContact.getAddresses().add(address);
					address.setContact(testContact);
					
					if(isDeliveryAddressAvailable(prop)) {
						Address deliveryAddress = addressManager.getAddressFromContact(testContact, ContactType.DELIVERY).orElse(null);
						if(deliveryAddress.getId() == address.getId()) {
							// recreation of delivery address, if any
							deliveryAddress = modelFactory.createAddress();
						}
						deliveryAddress = createAddressFromProperties(prop, deliveryAddress, DELIVERY_FIELD_PREFIX);
						deliveryAddress.getContactTypes().add(ContactType.DELIVERY);
						testContact.getAddresses().add(deliveryAddress);
						deliveryAddress.setContact(testContact);
					}

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
						testContact.setDateAdded(dateFormatterService.getCalendarFromDateString(prop.getProperty("date_added")).getTime());
					}
					
					Payment payment = paymentsDAO.findByName(prop.getProperty("payment"));
					if(payment != null) {
						testContact.setPayment(payment);
					}
					testContact.setReliability(ReliabilityType.getByName(prop.getProperty("reliability")));

					testContact.setWebsite(prop.getProperty("website"));
					testContact.setSupplierNumber(prop.getProperty("supplier_nr"));
					testContact.setWebshopName(prop.getProperty("username"));
					testContact.setVatNumber(prop.getProperty("vatnr"));
					testContact.setVatNumberValid(BooleanUtils.toBooleanObject(prop.getProperty("vatnrvalid")));
					testContact.setDiscount(DataUtils.getInstance().StringToDouble(prop.getProperty("discount")));
					String birthday = prop.getProperty("birthday");
					if(StringUtils.isNotBlank(birthday)) {
						GregorianCalendar dateFromString = dateFormatterService.getCalendarFromDateString(birthday);
						testContact.setBirthday(dateFromString.getTime());
					}
					// Add the contact to the data base
					// Update the modified contact data
					contactsDAO.update(testContact);
					if (DateUtils.isSameDay(testContact.getDateAdded(), Calendar.getInstance().getTime())) {
						importedContacts++;
					} else if (updateExisting) {
						// Update data
						updatedContacts++;
					}
				}
			}
			
			// The result string
			//T: Message: xx Contacts have been imported 
			result += NL + importedContacts + " " + importMessages.wizardImportInfoContactsimported;
			if (updatedContacts > 0)
				result += NL + updatedContacts + " " + importMessages.wizardImportInfoContactsupdated;
			if (skippedContacts > 0)
				result += NL + skippedContacts + " " + importMessages.wizardImportInfoContactsskipped;
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
		evtBroker.post(classifier == FakturamaModelPackage.DEBITOR_CLASSIFIER_ID ? "Debtor" : "Creditor", "update");
	}


	private boolean isDeliveryAddressAvailable(Properties prop) {
		List<String> fieldsToCheck = Arrays.asList(
				"street", //
				"zip", //
				"city", //
				"phone", //
				"fax", //
				"mobile", //
				"email");
		return fieldsToCheck.stream().anyMatch(p -> prop.get(DELIVERY_FIELD_PREFIX + p) != null && !prop.get(DELIVERY_FIELD_PREFIX + p).toString().isEmpty());
	}

	/**
	 * Creates an {@link Address} from CSV properties.
	 * 
	 * @param prop
	 * @param address
	 * @param prefix necessary e.g. for delivery addresses
	 * @return 
	 */
	private Address createAddressFromProperties(Properties prop, Address address, String prefix) {
		address.setValidFrom(Calendar.getInstance().getTime());
		address.setStreet(prop.getProperty(StringUtils.join(prefix, "street")));
		address.setZip(prop.getProperty(StringUtils.join(prefix, "zip")));
		address.setCity(prop.getProperty(StringUtils.join(prefix, "city")));
		address.setPhone(prop.getProperty(StringUtils.join(prefix, "phone")));
		address.setFax(prop.getProperty(StringUtils.join(prefix, "fax")));
		address.setMobile(prop.getProperty(StringUtils.join(prefix, "mobile")));
		address.setEmail(prop.getProperty(StringUtils.join(prefix, "email")));
//		address.setCountryCode(prop.getProperty("country")); // FIXME set correct country code!!!!
		return address;
	}

	public String getResult() {
		return result;
	}


	/**
	 * @param quoteChar the quoteChar to set
	 */
	public final void setQuoteChar(char quoteChar) {
		this.quoteChar = quoteChar;
	}


	/**
	 * @param separator the separator to set
	 */
	public final void setSeparator(char separator) {
		this.separator = separator;
	}

}
