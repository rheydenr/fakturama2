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

package org.fakturama.export.wizard.vcf.contacts;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import com.ibm.icu.text.SimpleDateFormat;
import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * This class generates a list with all contacts
 * 
 * @author Gerd Bartelt
 */
public class VcardExport {
    
	@Inject
	private ILocaleService localeUtil;

	@Inject
	private ContactsDAO contactsDAO;
    
    @Inject
    private IEclipseContext context;
    
    @Inject
    private IDocumentAddressManager addressManager;

    private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");

	// Buffered writer for the output stream
	private BufferedWriter bos  = null;
	
	// Constant for a OS dependent new line
	private String NEW_LINE = System.lineSeparator();

	/**
	 * Convert the special characters in a vcard string
	 * and add a backslash \
	 * 
	 * @param 
	 * 			s The String to convert
	 * @return
	 * 			The converted string
	 */
	private String encodeVCardString(String s) {
		s = s.replace("\n", "\\n");
		s = s.replace(":", "\\:");
		s = s.replace(",", "\\,");
		s = s.replace(";", "\\;");
		return s;
	}
	
	/**
	 * Write an attribute, if it is not empty and add 
	 * a semicolon between two attributes.
	 * 
	 * @param s
	 * 			The attribute to write
	 * @param first
	 * 			True, if it is the first attribute
	 * 
	 */
	private void writeAttribute(String s, boolean first) {

		// Exit, if the attribute is null
		if (s == null)
			return;
		
		// Write the attribute and add a semicolon before all
		// attributes, except the first one.
		try {
			if (!first)
				bos.write(";");
			bos.write(encodeVCardString(s));
		}
		catch (IOException e) {}

	}

	/**
	 * Write a property and n attributes
	 * 
	 * @param property
	 * 			The property to write
	 * @param attributes
	 * 			The attribute(d)
	 */
	private void writeVCard(String property, String... attributes) {

		// Exit, if all attributes are empty 
		if (attributes.length == 0 || Arrays.stream(attributes).allMatch(o -> o != null && o.length() == 0)) {
			return;
		}
		
		// Write the property and all attributes
		try {
			bos.write(property);
			boolean isFirst = true;
			for (String string : attributes) {
				writeAttribute(string, isFirst);
				isFirst = false;
			}
			bos.write(NEW_LINE);
		}
		catch (IOException e) {
		}
	}
	
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

		// Create a File object
		Path csvFile = Paths.get(filename);
		
		// Create a new file
		try {
			bos = Files.newBufferedWriter(csvFile, StandardOpenOption.CREATE);
			
			// Get all undeleted contacts
			List<Contact> contacts = contactsDAO.findAll();
			
			// Export the product data
			for (Contact contact : contacts) {
				
				// Export one VCARD
				writeVCard("BEGIN:","VCARD");
				writeVCard("VERSION:","2.1");
				writeVCard("N:", contact.getName(),
						contact.getFirstName());
				writeVCard("FN:", contactUtil.getNameWithCompany(contact));
				switch (contact.getGender()) {
				case 1:
					writeVCard("GENDER:", "M");
					break;
				case 2:
					writeVCard("GENDER:", "F");
					break;
				default:
					break;
				}
				
				// doesn't work :-( ... at least not with Thunderbird
				if(contact.getBirthday() != null) {
					writeVCard("BDAY:", sdf.format(contact.getBirthday()));
				}
				Address address = addressManager.getAddressFromContact(contact,ContactType.BILLING);
				if(address != null
						&& (StringUtils.isNotBlank(contact.getCompany()) 
								|| StringUtils.isNotBlank(address.getStreet()) 
								|| StringUtils.isNotBlank(address.getCity())
					)) {
					writeVCard("ADR;WORK;PREF:",
							"",
							contact.getCompany(),
							address.getStreet(),
							address.getCity(),
							"",
							address.getZip(),
							address.getCountryCode() != null
							   ? localeUtil.findByCode(address.getCountryCode()).get().getDisplayCountry()
							   : ""
							);
					writeVCard("TEL;WORK;VOICE:",address.getPhone());
					writeVCard("TEL;FAX:",address.getFax());
					writeVCard("TEL;CELL;VOICE:",address.getMobile());
					writeVCard("EMAIL;internet:",address.getEmail());
				}
				
				address = addressManager.getAddressFromContact(contact, ContactType.DELIVERY);
				if(address != null) {
					String countryCode = address.getCountryCode();
					String displayCountry = "";
					if(countryCode != null) {
						displayCountry = localeUtil.findByCode(countryCode).orElse(localeUtil.getDefaultLocale()).getDisplayCountry();
					}
					if(StringUtils.isNotBlank(contact.getCompany()) 
							|| StringUtils.isNotBlank(address.getStreet()) 
							|| StringUtils.isNotBlank(address.getCity())) {	
						writeVCard("ADR;TYPE=postal:",
								"",
								contact.getCompany(),
								address.getStreet(),
								address.getCity(),
								"",
								StringUtils.defaultString(address.getZip()),
								displayCountry
								);
						writeVCard("ORG:", contact.getCompany());
					}
					writeVCard("ADR;TYPE=other:",
							StringUtils.defaultString(address.getCityAddon()),
							StringUtils.defaultString(contact.getCompany()),
							StringUtils.defaultString(address.getStreet()),
							StringUtils.defaultString(address.getCity()),
							"",
							StringUtils.defaultString(address.getZip()),
							displayCountry
							);
					writeVCard("TEL;HOME;VOICE:",address.getPhone());
					writeVCard("TEL;WORK;FAX:",address.getFax());
					writeVCard("TEL;CELL;VOICE:",address.getMobile());
					writeVCard("EMAIL;internet:",address.getEmail());
				}
				
				writeVCard("URL;WORK:",contact.getWebsite());

				writeVCard("NOTE:",contact.getNote());
				writeVCard("CATEGORIES:", contact.getCategories() != null ? CommonConverter.getCategoryName(contact.getCategories(), "/") : "");
				
				writeVCard("END:","VCARD");
			}
		}
		catch (IOException e) {
			return false;
		} finally {
			if(bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// True = Export was successful
		return true;
	}

}
