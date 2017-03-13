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

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * This class generates a list with all contacts
 * 
 * @author Gerd Bartelt
 */
public class VcardExport {
	
	@Inject
	private ContactsDAO contactsDAO;
    
    @Inject
    private IEclipseContext context;
    
//    private SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");

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
	 * Write a property and one attribute
	 * 
	 * @param property
	 * 			The property to write
	 * @param s
	 * 			The 1st attribute
	 */
	private void writeVCard(String property, String s) {
		if(StringUtils.isNotBlank(s)) {
			writeVCard(property, s, null);
		}
	}

	/**
	 * Write a property and two attributes
	 * 
	 * @param property
	 * 			The property to write
	 * @param s1
	 * 			The 1st attribute
	 * @param s2
	 * 			The 2nd attribute
	 */
	private void writeVCard(String property, String s1, String s2) {
		writeVCard(property, s1, s2, null, null, null, null, null);
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
	 * @param s
	 * 			The attribute(d)
	 */
	private void writeVCard(String property, String... s) {

		// Exit, if all attributes are empty 
		if (s.length == 0 || Arrays.stream(s).allMatch(o -> o.length() == 0)) {
			return;
		}
		
		// Write the property and all attributes
		try {
			bos.write(property);
			boolean isFirst = true;
			for (String string : s) {
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
				writeVCard("VERSION:","3.0");
				writeVCard("N:", contact.getName(),
						contact.getFirstName());
				writeVCard("FN:", contactUtil.getNameWithCompany(contact));
				// doesn't work :-( ... at least not with Thunderbird
//				if(contact.getBirthday() != null) {
//					writeVCard("BDAY:", sdf.format(contact.getBirthday()));
//				}
				Address address = contact.getAddress();
				if(address != null
						&& (StringUtils.isNotBlank(contact.getCompany()) 
								|| StringUtils.isNotBlank(address.getStreet()) 
								|| StringUtils.isNotBlank(address.getCity())
					)) {
					writeVCard("ADR;TYPE=home:",
							"",
							contact.getCompany(),
							address.getStreet(),
							address.getCity(),
							"",
							address.getZip(),
							address.getCountryCode() != null
							   ? LocaleUtil.getInstance().findByCode(address.getCountryCode()).get().getDisplayCountry()
							   : ""
							);
				}
				
				Contact alternateContacts;
				if (contact.getAlternateContacts() != null) {
					alternateContacts = contact.getAlternateContacts();
				} else {
					alternateContacts = contact;
				}
				address = alternateContacts.getAddress();
				if(address != null) {
					if(StringUtils.isNotBlank(contact.getCompany()) 
							|| StringUtils.isNotBlank(address.getStreet()) 
							|| StringUtils.isNotBlank(address.getCity())) {	
						writeVCard("ADR;TYPE=postal:",
								"",
								alternateContacts.getCompany(),
								address.getStreet(),
								address.getCity(),
								"",
								address.getZip(),
								LocaleUtil.getInstance().findByCode(address.getCountryCode()).orElse(LocaleUtil.getInstance().getDefaultLocale()).getDisplayCountry()
								);
					}
					writeVCard("ADR;TYPE=other:",
							contactUtil.getNameWithCompany(alternateContacts),
							alternateContacts.getCompany(),
							address.getStreet(),
							address.getCity(),
							"",
							address.getZip(),
							LocaleUtil.getInstance().findByCode(address.getCountryCode()).orElse(LocaleUtil.getInstance().getDefaultLocale()).getDisplayCountry()
							);
				}
				
				writeVCard("TEL;TYPE=HOME,WORK,VOICE:",contact.getPhone());
				writeVCard("TEL;TYPE=HOME,WORK,FAX:",contact.getFax());
				writeVCard("TEL;TYPE=HOME,WORK,CELL:",contact.getMobile());
				writeVCard("EMAIL;TYPE=internet:",contact.getEmail());
				writeVCard("URL:",contact.getWebsite());

				writeVCard("NOTE:",contact.getNote());
				writeVCard("CATEGORIES:", contact.getCategories() != null ? contact.getCategories().getName() : "");
				
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
