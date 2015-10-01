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

package com.sebulli.fakturama.office;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Transaction;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.util.ContactUtil;


public class Placeholders {
	    
    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private IEclipseContext context;
//
//    @Inject
//    private Logger log;

    @Inject
    private IPreferenceStore preferences;

	// all placeholders
	private static String placeholders[] = {
			"YOURCOMPANY.COMPANY",
			"YOURCOMPANY.OWNER",
			"YOURCOMPANY.OWNER.FIRSTNAME",
			"YOURCOMPANY.OWNER.LASTNAME",
			"YOURCOMPANY.STREET",
			"YOURCOMPANY.STREETNAME",
			"YOURCOMPANY.STREETNO",
			"YOURCOMPANY.ZIP",
			"YOURCOMPANY.CITY",
			"YOURCOMPANY.COUNTRY",
			"YOURCOMPANY.EMAIL",
			"YOURCOMPANY.PHONE",
			"YOURCOMPANY.PHONE.PRE",
			"YOURCOMPANY.PHONE.POST",
			"YOURCOMPANY.FAX",
			"YOURCOMPANY.FAX.PRE",
			"YOURCOMPANY.FAX.POST",
			"YOURCOMPANY.WEBSITE",
			"YOURCOMPANY.VATNR",
			"YOURCOMPANY.TAXOFFICE",
			"YOURCOMPANY.BANKACCOUNTNR",
			"YOURCOMPANY.BANK",
			"YOURCOMPANY.BANKCODE",
			"YOURCOMPANY.IBAN",
			"YOURCOMPANY.BIC",
			"YOURCOMPANY.CREDITORID",
			"DOCUMENT.DATE",
			"DOCUMENT.ADDRESSES.EQUAL",
			"DOCUMENT.ADDRESS",
			"DOCUMENT.DELIVERYADDRESS",
			"DOCUMENT.DIFFERENT.ADDRESS",
			"DOCUMENT.DIFFERENT.DELIVERYADDRESS",
			"DOCUMENT.TYPE",
			"DOCUMENT.NAME",
			"DOCUMENT.CUSTOMERREF",
			"DOCUMENT.CONSULTANT",
			"DOCUMENT.SERVICEDATE",
			"DOCUMENT.MESSAGE",
			"DOCUMENT.MESSAGE1",
			"DOCUMENT.MESSAGE2",
			"DOCUMENT.MESSAGE3",
			"DOCUMENT.TRANSACTION",
			"DOCUMENT.INVOICE",
			"DOCUMENT.WEBSHOP.ID",
			"DOCUMENT.WEBSHOP.DATE",
			"DOCUMENT.ORDER.DATE",
			"DOCUMENT.ITEMS.GROSS",
			"DOCUMENT.ITEMS.NET",
			"DOCUMENT.ITEMS.COUNT",
			"DOCUMENT.TOTAL.NET",
			"DOCUMENT.TOTAL.VAT",
			"DOCUMENT.TOTAL.GROSS",
			"DOCUMENT.DEPOSIT.DEPOSIT",
			"DOCUMENT.DEPOSIT.FINALPAYMENT",
			"DOCUMENT.DEPOSIT.DEP_TEXT",
			"DOCUMENT.DEPOSIT.FINALPMT_TEXT",
			"DOCUMENT.REFERENCE.OFFER",
			"DOCUMENT.REFERENCE.ORDER",
			"DOCUMENT.REFERENCE.CONFIRMATION",
			"DOCUMENT.REFERENCE.INVOICE",
			"DOCUMENT.REFERENCE.INVOICE.DATE",
			"DOCUMENT.REFERENCE.DELIVERY",
			"DOCUMENT.REFERENCE.CREDIT",
			"DOCUMENT.REFERENCE.DUNNING",
			"DOCUMENT.REFERENCE.PROFORMA",
			"ITEMS.DISCOUNT.PERCENT",
			"ITEMS.DISCOUNT.NET",
			"ITEMS.DISCOUNT.GROSS",

			"ITEMS.DISCOUNT.VALUE",
			"ITEMS.DISCOUNT.NETVALUE",
			"ITEMS.DISCOUNT.TARAVALUE",
			"ITEMS.DISCOUNT.DISCOUNTPERCENT",
			"ITEMS.DISCOUNT.DAYS",
			"ITEMS.DISCOUNT.DUEDATE",

			"SHIPPING.NET",
			"SHIPPING.VAT",
			"SHIPPING.GROSS",
			"SHIPPING.DESCRIPTION",
			"SHIPPING.VAT.DESCRIPTION",
			"DOCUMENT.DUNNING.LEVEL",
			"PAYMENT.TEXT",
			"PAYMENT.DESCRIPTION",
			"PAYMENT.PAID.VALUE",
			"PAYMENT.PAID.DATE",
			"PAYMENT.DUE.DAYS",
			"PAYMENT.DUE.DATE",
			"PAYMENT.PAID",
			"ADDRESS.FIRSTLINE",
			"ADDRESS",
			"ADDRESS.GENDER",
			"ADDRESS.GREETING",
			"ADDRESS.TITLE",
			"ADDRESS.NAME",
			"ADDRESS.BIRTHDAY",
			"ADDRESS.NAMEWITHCOMPANY",
			"ADDRESS.FIRSTNAME",
			"ADDRESS.LASTNAME",
			"ADDRESS.COMPANY",
			"ADDRESS.STREET",
			"ADDRESS.STREETNAME",
			"ADDRESS.STREETNO",
			"ADDRESS.ZIP",
			"ADDRESS.CITY",
			"ADDRESS.COUNTRY",
			"ADDRESS.COUNTRY.CODE2",
			"ADDRESS.COUNTRY.CODE3",
			"DELIVERY.ADDRESS.FIRSTLINE",
			"DELIVERY.ADDRESS",
			"DELIVERY.ADDRESS.GENDER",
			"DELIVERY.ADDRESS.GREETING",
			"DELIVERY.ADDRESS.TITLE",
			"DELIVERY.ADDRESS.NAME",
			"DELIVERY.ADDRESS.BIRTHDAY",
			"DELIVERY.ADDRESS.NAMEWITHCOMPANY",
			"DELIVERY.ADDRESS.FIRSTNAME",
			"DELIVERY.ADDRESS.LASTNAME",
			"DELIVERY.ADDRESS.COMPANY",
			"DELIVERY.ADDRESS.STREET",
			"DELIVERY.ADDRESS.STREETNAME",
			"DELIVERY.ADDRESS.STREETNO",
			"DELIVERY.ADDRESS.ZIP",
			"DELIVERY.ADDRESS.CITY",
			"DELIVERY.ADDRESS.COUNTRY",
			"DELIVERY.ADDRESS.COUNTRY.CODE2",
			"DELIVERY.ADDRESS.COUNTRY.CODE3",
			"ADDRESS.BANK.ACCOUNT.HOLDER",
			"ADDRESS.BANK.ACCOUNT",
			"ADDRESS.BANK.CODE",
			"ADDRESS.BANK.NAME",
			"ADDRESS.BANK.IBAN",
			"ADDRESS.BANK.BIC",
			"DEBITOR.MANDATREF",
			"ADDRESS.NR",
			"ADDRESS.PHONE",
			"ADDRESS.PHONE.PRE",
			"ADDRESS.PHONE.POST",
			"ADDRESS.FAX",
			"ADDRESS.FAX.PRE",
			"ADDRESS.FAX.POST",
			"ADDRESS.MOBILE",
			"ADDRESS.MOBILE.PRE",
			"ADDRESS.MOBILE.POST",
			"ADDRESS.SUPPLIER.NUMBER",
			"ADDRESS.EMAIL",
			"ADDRESS.WEBSITE",
			"ADDRESS.VATNR",
			"ADDRESS.NOTE",
			"ADDRESS.DISCOUNT"			
	};
	
	private static NumberFormat localizedNumberFormat = NumberFormat.getInstance(Locale.getDefault());

    private ContactUtil contactUtil;
	
	/**
	 * Returns the first name of a complete name
	 * 
	 * @param name
	 * 		First name and last name
	 * @return
	 * 		Only the first name
	 */
	private String getFirstName (String name) {
		String s = name.trim();
		int lastSpace = s.lastIndexOf(" ");
		if (lastSpace > 0)
			return s.substring(0, lastSpace).trim();
		else
			return "";
	}
	
	/**
	 * Returns the last name of a complete name
	 * 
	 * @param name
	 * 		First name and last name
	 * @return
	 * 		Only the last name
	 */
	private String getLastName (String name) {
		String s = name.trim();
		int lastSpace = s.lastIndexOf(" ");
		if (lastSpace > 0)
			return s.substring(lastSpace + 1).trim();
		else
			return "";
	}
	
	/**
	 * Returns the street name without the number
	 * 
	 * @param streetWithNo
	 * 		
	 * @return
	 * 		Only the street name
	 */
	private String getStreetName (String streetWithNo) {
		String s = streetWithNo.trim();
		int indexNo = 0;
		
		// Search for the number
		Matcher matcher = Pattern.compile( "\\d+" ).matcher( s );
		if ( matcher.find() ) {
			indexNo = matcher.start();
		}
		
		// Extract the street
		if (indexNo > 0)
			return s.substring(0, indexNo).trim();
		else
			return s;
	}

	/**
	 * Returns the street number without the name
	 * 
	 * @param streetWithNo
	 * 		
	 * @return
	 * 		Only the street No
	 */
	private String getStreetNo (String streetWithNo) {
		String s = streetWithNo.trim();
		int indexNo = 0;
		
		// Search for the number
		Matcher matcher = Pattern.compile( "\\d+" ).matcher( s );
		if ( matcher.find() ) {
			indexNo = matcher.start();
		}
		
		// Extract the Number
		if (indexNo > 0)
			return s.substring(indexNo).trim();
		else
			return "";
	}
	
	/**
	 * Get a part of the telephone number
	 * 
	 * @param pre
	 * 		TRUE, if the area code should be returned
	 * @return
	 * 		Part of the telephone number
	 */
	private String getTelPrePost(String no, boolean pre){
		// if no contains "/" ord " " (space) then split there
		String parts[] = no.trim().split("[ /]", 2);
		
		// Split the number
		if (parts.length < 2) {
			String tel = parts[0];
			// devide the number at the 4th position
			if (tel.length() > 4) {
				if (pre)
					return tel.substring(0, 4);
				else
					return tel.substring(4);
			}
			// The number is very short
			return pre ? "" : tel;
		}
		// return the first or the second part
		else {
			return pre ? parts[0] : parts[1];
		}
	}
	
	private String getDataFromAddressField(String address, String key) {
		String addressName = "";
		String addressFirstName = "";
		String addressLastName = "";
		String addressLine = "";
		String addressStreet = "";
		String addressZIP = "";
		String addressCity = "";
		String addressCountry = "";
		
		String[] addressLines;
		if (address == null) {
			return "";
		} else {
			addressLines = address.split("\\n");
		}
		
		Boolean countryFound = false;
		Boolean cityFound = false;
		Boolean streetFound = false;
		String line = "";
		addressLine = "";
		
		// The first line is the name
		addressName = addressLines[0];
		addressFirstName = getFirstName(addressName);
		addressLastName = getLastName(addressName);
		
		// Analyze all the other lines. Start with the last
		for (int lineNr = addressLines.length -1; lineNr >= 1;lineNr--) {
			
			// Get one line
			line = addressLines[lineNr].trim();
			
			// Use only non-empty lines
			if (!line.isEmpty()) {
				
				if (!countryFound || !cityFound) {
					Matcher matcher = Pattern.compile( "\\d+" ).matcher( line );
					
					// A Number was found. So this line was not the country, it must be the ZIP code
					if ( matcher.find() ) {
						if (matcher.start() < 4)  {
							int codelen = matcher.end() - matcher.start();
							
							// Extract the ZIP code
							if (codelen >= 4 && codelen <=5 ) {
								addressZIP = matcher.group();

								// and the city
								addressCity = line.substring(matcher.end()+1).trim();
								
							}
							cityFound = true;
							countryFound = true;
						}
					}
					else {
						// It must be the country
						addressCountry =  line;
						countryFound = true;
					}
				}
				// City and maybe country were found. Search now for the street.
				else if (!streetFound){
					Matcher matcher = Pattern.compile( "\\d+" ).matcher( line );
					
					// A Number was found. This must be the street number
					if ( matcher.find() ) {
						if (matcher.start() > 3)  {
							// Extract the street number
							addressStreet  = line;
							streetFound = true;
						}
					}
				}
				// Street, city and maybe country were found. 
				// Search now for additional address information
				else {
					if (!addressLine.isEmpty())
						addressLine +=" ";
					addressLine = line;
				}
			}
		}

		if (key.equals("name")) return addressName;
		if (key.equals("firstname")) return addressFirstName;
		if (key.equals("lastname")) return addressLastName;
		if (key.equals("addressfirstline")) return addressLine;
		if (key.equals("street")) return addressStreet;
		if (key.equals("streetname")) return getStreetName(addressStreet);
		if (key.equals("streetno")) return getStreetNo(addressStreet);
		if (key.equals("zip")) return addressZIP;
		if (key.equals("city")) return addressCity;
		if (key.equals("county")) return addressCountry;
		return "";
	}
	

	/**
	 * Replaces all line breaks by a "-"
	 * 
	 * @param s
	 * 	The string in multiple lines
	 * @param replacement
	 * 	The replacement
	 * @return
	 * 	The string in one line, seperated by a "-"
	 */
	private String StringInOneLine(String s, String replacement) {
		// Convert CRLF to LF 
		s = DataUtils.getInstance().convertCRLF2LF(s).trim();
		// Replace line feeds by a " - "
		s = s.replaceAll("\\n", replacement);
		return s;
	}

	/**
	 * Removes the quotation marks of a String
	 * @param s
	 * 	The string with quotation marks
	 * @return
	 *  The string without them
	 */
	private String removeQuotationMarks(String s) {
		
		// remove leading and trailing spaces
		s = s.trim();
		
		// Remove the leading
		if (s.startsWith("\""))
			s = s.substring(1);

		// Remove the trailing
		if (s.endsWith("\""))
			s = s.substring(0, s.length() - 1);
		
		return s;
	}
	
	/**
	 * Replace the placeholder values by a value in a list
	 * 
	 * @param replacements
	 * 		A list of replacements, separates by a ";"
	 * 		eg: {"Belgien","BEL";"Dänemark","DNK"}
	 * @param value
	 * 		The input value
	 * @return
	 * 		The modified value
	 */
	private String replaceValues(String replacements, String value) {
		
		// Remove spaces
		replacements = replacements.trim();
		
		// Remove the leading {
		if (replacements.startsWith("{"))
			replacements = replacements.substring(1);

		// Remove the trailing }
		if (replacements.endsWith("}"))
			replacements = replacements.substring(0, replacements.length() - 1);

		String parts[] = replacements.split(";");

		// Nothing to do
		if (parts.length < 1)
			return value;
		
		// get all parts
		for (String part : parts) {
			String twoStrings[] = part.split(",");
			if (twoStrings.length == 2) {
				
				//Escape sequence "%COMMA" for ","
				twoStrings[0] = twoStrings[0].replace("%COMMA", ",");
			    
				// Replace the value, if it is equal to the entry
				if (DataUtils.getInstance().replaceAllAccentedChars(value).equalsIgnoreCase(
				        DataUtils.getInstance().replaceAllAccentedChars(removeQuotationMarks(twoStrings[0])))) {
					value = removeQuotationMarks(twoStrings[1]);
					return value;
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Interprets the placeholder parameters
	 * 
	 * @param placeholder
	 * 		Name of the placeholder
	 * @param value
	 * 		The value
	 * @return
	 * 		The value mofified by the parameters
	 */
	String interpretParameters(String placeholder, String value) {
		String par;
		
		if (value == null)
			return value;
		
		// The parameters "PRE" and "POST" are only used, if the
		// placeholder value is not empty
		if (!value.isEmpty()) {
			
			// Parameter "PRE"
			par = extractParam(placeholder,"PRE");
			if (!par.isEmpty())
					value =  removeQuotationMarks(par) + value;

			// Parameter "POST"
			par = extractParam(placeholder,"POST");
			if (!par.isEmpty())
					value += removeQuotationMarks(par);

			// Parameter "INONELINE"
			par = extractParam(placeholder,"INONELINE");
			if (!par.isEmpty())
				value = StringInOneLine(value, removeQuotationMarks(par));

			// Parameter "REPLACE"
			par = extractParam(placeholder,"REPLACE");
			if (!par.isEmpty())
				value = replaceValues(removeQuotationMarks(par) , value);

			// Parameter "FORMAT"
			par = extractParam(placeholder,"FORMAT");
			if (!par.isEmpty()) {
				try {
					Double parsedDouble = localizedNumberFormat.parse(value).doubleValue();
					value = DataUtils.getInstance().DoubleToDecimalFormatedValue(parsedDouble, par);
				}
				catch (Exception e) {
					// TODO implement!
				}
			}
		}
		else {
			// Parameter "EMPTY"
			par = extractParam(placeholder,"EMPTY");
			if (!par.isEmpty())
					value = removeQuotationMarks(par);
		}
		
		// Encode some special characters
		value = encodeEntinities(value);
		return value;
	}
	
	/**
	 * Extract the placeholder values from a given document
	 * 
	 * @param document
	 * 		The document with all the values
	 * @param placeholder
	 * 		The placeholder to extract
	 * @return
	 * 		The extracted value
	 */
	public String getDocumentInfo(Document document, DocumentSummary documentSummary, String placeholder) {
        contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);
		String value = getDocumentInfoByPlaceholder(document, documentSummary, extractPlaceholderName(placeholder));
		return interpretParameters(placeholder, value);
	}
	
	
	/**
	 * Extract the value of the parameter of a placeholder
	 * 
	 * @param placeholder
	 * 	The placeholder name
	 * 
	 * @param param
	 * 	Name of the parameter to extract
	 * 
	 * @return
	 *  The extracted value
	 */
	String extractParam(String placeholder, String param) {
		String s;
		
		// A parameter starts with "$" and ends with ":"
		param = "$" + param + ":";
		
		// Return, if parameter was not in placeholder's name
		if (!placeholder.contains(param))
			return "";

		// Extract the string after the parameter name
		s = placeholder.substring(placeholder.indexOf(param)+param.length());

		// Extract the string until the next parameter, or the end
		int i;
		i = s.indexOf("$");
		if ( i>0 )
			s= s.substring(0, i);
		else if (i == 0)
			s = "";
		
		i = s.indexOf(">");
		if ( i>0 )
			s= s.substring(0, i);
		else if (i == 0)
			s = "";

		// Return the value
		return s;
	}
	
	/**
	 * Extracts the placeholder name, separated by a $
	 * 
	 * @param s
	 * 		The placeholder with parameters
	 * @return
	 * 		The placeholder name without paramater
	 */
	private String extractPlaceholderName(String s) {
		return s.split("\\$" , 2)[0];
	}
	
	/**
	 * Decode the special characters
	 * 
	 * @param s
	 * 	String to convert
	 * @return
	 *  Converted
	 */
	private String encodeEntinities(String s) {
	
		s = s.replaceAll("%LT", "<");
		s = s.replaceAll("%GT", ">");
		s = s.replaceAll("%NL", "\n");
		s = s.replaceAll("%TAB", "\t");
		s = s.replaceAll("%DOLLAR", Matcher.quoteReplacement("$"));
		s = s.replaceAll("%A_GRAVE", Matcher.quoteReplacement("À"));
		s = s.replaceAll("%A_ACUTE", Matcher.quoteReplacement("Á"));
		s = s.replaceAll("%A_CIRC", Matcher.quoteReplacement("Â"));
		s = s.replaceAll("%A_TILDE", Matcher.quoteReplacement("Ã"));
		s = s.replaceAll("%A_RING", Matcher.quoteReplacement("Å"));
		s = s.replaceAll("%C_CED", Matcher.quoteReplacement("Ç"));
		s = s.replaceAll("%E_GRAVE", Matcher.quoteReplacement("È"));
		s = s.replaceAll("%E_ACUTE", Matcher.quoteReplacement("É"));
		s = s.replaceAll("%E_CIRC", Matcher.quoteReplacement("Ê"));
		s = s.replaceAll("%I_GRAVE", Matcher.quoteReplacement("Ì"));
		s = s.replaceAll("%I_ACUTE", Matcher.quoteReplacement("Í"));
		s = s.replaceAll("%I_CIRC", Matcher.quoteReplacement("Î"));
		s = s.replaceAll("%O_GRAVE", Matcher.quoteReplacement("Ò"));
		s = s.replaceAll("%O_ACUTE", Matcher.quoteReplacement("Ó"));
		s = s.replaceAll("%O_CIRC", Matcher.quoteReplacement("Ô"));
		s = s.replaceAll("%O_TILDE", Matcher.quoteReplacement("Õ"));
		s = s.replaceAll("%O_STROKE", Matcher.quoteReplacement("Ø"));
		s = s.replaceAll("%U_GRAVE", Matcher.quoteReplacement("Ù"));
		s = s.replaceAll("%U_ACUTE", Matcher.quoteReplacement("Ú"));
		s = s.replaceAll("%U_CIRC", Matcher.quoteReplacement("Û"));
		s = s.replaceAll("%a_GRAVE", Matcher.quoteReplacement("à"));
		s = s.replaceAll("%a_ACUTE", Matcher.quoteReplacement("á"));
		s = s.replaceAll("%a_CIRC", Matcher.quoteReplacement("â"));
		s = s.replaceAll("%a_TILDE", Matcher.quoteReplacement("ã"));
		s = s.replaceAll("%a_RING", Matcher.quoteReplacement("å"));
		s = s.replaceAll("%c_CED", Matcher.quoteReplacement("ç"));
		s = s.replaceAll("%e_GRAVE", Matcher.quoteReplacement("è"));
		s = s.replaceAll("%e_ACUTE", Matcher.quoteReplacement("é"));
		s = s.replaceAll("%e_CIRC", Matcher.quoteReplacement("ê"));
		s = s.replaceAll("%i_GRAVE", Matcher.quoteReplacement("ì"));
		s = s.replaceAll("%i_ACUTE", Matcher.quoteReplacement("í"));
		s = s.replaceAll("%i_CIRC", Matcher.quoteReplacement("î"));
		s = s.replaceAll("%n_TILDE", Matcher.quoteReplacement("ñ"));
		s = s.replaceAll("%o_GRAVE", Matcher.quoteReplacement("ò"));
		s = s.replaceAll("%o_ACUTE", Matcher.quoteReplacement("ó"));
		s = s.replaceAll("%o_CIRC", Matcher.quoteReplacement("ô"));
		s = s.replaceAll("%o_TILDE", Matcher.quoteReplacement("õ"));
		s = s.replaceAll("%u_GRAVE", Matcher.quoteReplacement("ù"));
		s = s.replaceAll("%u_ACUTE", Matcher.quoteReplacement("ú"));
		s = s.replaceAll("%u_CIRC", Matcher.quoteReplacement("û"));
		
		return s;
	}
	

	
	/**
	 * Get Information from document.
	 * If there is no reference to a customer, use the address field to
	 * Extract the address
	 * 
	 * @param document
	 * 	The document
	 * @param key
	 * 	The key to extract
	 * @return
	 *  The extracted result
	 */
	private String getDocumentInfoByPlaceholder(Document document, DocumentSummary documentSummary, String key) {
		
		// Get the company information from the preferences
		if (key.startsWith("YOURCOMPANY")) {
			if (key.equals("YOURCOMPANY.COMPANY")) return  preferences.getString("YOURCOMPANY_COMPANY_NAME");

			String owner = preferences.getString("YOURCOMPANY_COMPANY_OWNER");
			if (key.equals("YOURCOMPANY.OWNER")) return  owner;
			if (key.equals("YOURCOMPANY.OWNER.FIRSTNAME")) return  getFirstName(owner);
			if (key.equals("YOURCOMPANY.OWNER.LASTNAME")) return  getLastName(owner);

			String streetWithNo = preferences.getString("YOURCOMPANY_COMPANY_STREET");
			if (key.equals("YOURCOMPANY.STREET")) return  streetWithNo;
			if (key.equals("YOURCOMPANY.STREETNAME")) return  getStreetName(streetWithNo);
			if (key.equals("YOURCOMPANY.STREETNO")) return  getStreetNo(streetWithNo);

			if (key.equals("YOURCOMPANY.ZIP")) return  preferences.getString("YOURCOMPANY_COMPANY_ZIP");
			if (key.equals("YOURCOMPANY.CITY")) return  preferences.getString("YOURCOMPANY_COMPANY_CITY");
			if (key.equals("YOURCOMPANY.COUNTRY")) return  preferences.getString("YOURCOMPANY_COMPANY_COUNTRY");
			if (key.equals("YOURCOMPANY.EMAIL")) return  preferences.getString("YOURCOMPANY_COMPANY_EMAIL");
			if (key.equals("YOURCOMPANY.PHONE")) return  preferences.getString("YOURCOMPANY_COMPANY_TEL");
			if (key.equals("YOURCOMPANY.PHONE.PRE")) return  getTelPrePost(preferences.getString("YOURCOMPANY_COMPANY_TEL"), true);
			if (key.equals("YOURCOMPANY.PHONE.POST")) return  getTelPrePost(preferences.getString("YOURCOMPANY_COMPANY_TEL"), false);
			if (key.equals("YOURCOMPANY.FAX")) return  preferences.getString("YOURCOMPANY_COMPANY_FAX");
			if (key.equals("YOURCOMPANY.FAX.PRE")) return  getTelPrePost(preferences.getString("YOURCOMPANY_COMPANY_FAX"), true);
			if (key.equals("YOURCOMPANY.FAX.POST")) return  getTelPrePost(preferences.getString("YOURCOMPANY_COMPANY_FAX"), false);
			if (key.equals("YOURCOMPANY.WEBSITE")) return  preferences.getString("YOURCOMPANY_COMPANY_WEBSITE");
			if (key.equals("YOURCOMPANY.VATNR")) return  preferences.getString("YOURCOMPANY_COMPANY_VATNR");
			if (key.equals("YOURCOMPANY.TAXOFFICE")) return  preferences.getString("YOURCOMPANY_COMPANY_TAXOFFICE");
			if (key.equals("YOURCOMPANY.BANKACCOUNTNR")) return  preferences.getString("YOURCOMPANY_COMPANY_BANKACCOUNTNR");
			if (key.equals("YOURCOMPANY.BANKCODE")) return  preferences.getString("YOURCOMPANY_COMPANY_BANKCODE");
			if (key.equals("YOURCOMPANY.BANK")) return  preferences.getString("YOURCOMPANY_COMPANY_BANK");
			if (key.equals("YOURCOMPANY.IBAN")) return  preferences.getString("YOURCOMPANY_COMPANY_IBAN");
			if (key.equals("YOURCOMPANY.BIC")) return  preferences.getString("YOURCOMPANY_COMPANY_BIC");
		}

		if (document == null)
			return null;
		
		// Get the contact of the UniDataSet document
		Contact contact = document.getBillingContact();

		if (key.equals("DOCUMENT.DATE")) return DataUtils.getInstance().getFormattedLocalizedDate(document.getDocumentDate());
		if (key.equals("DOCUMENT.ADDRESSES.EQUAL")) {
            return (contactUtil.deliveryAddressEqualsBillingAddress(document)).toString();
        }

		// Get address and delivery address
		// with option "DIFFERENT" and without
		String deliverystring;
		String differentstring;
		// address and delivery address
		for (int i = 0;i<2 ; i++) {
		    String s;
			deliverystring = i==1 ? "delivery" : "";
			if(i == 1) {
                s = contactUtil.getAddressAsString(document.getDeliveryContact());
			} else {
			    s = contactUtil.getAddressAsString(document.getBillingContact());
			}
			
			//  with option "DIFFERENT" and without
			for (int ii = 0 ; ii<2; ii++) {
				differentstring = ii==1 ? ".DIFFERENT" : "";
				if (ii==1) {
					if (contactUtil.deliveryAddressEqualsBillingAddress(document))
						s="";
				}
				if (key.equals("DOCUMENT" + differentstring +"."+ deliverystring.toUpperCase()+ "ADDRESS")) return s;
				
			}
		}
		
		// Get information from the document
		if (key.equals("DOCUMENT.TYPE")) return document.getBillingType().getName();
		if (key.equals("DOCUMENT.NAME")) return document.getName();
		if (key.equals("DOCUMENT.CUSTOMERREF")) return document.getCustomerRef();
		if (key.equals("DOCUMENT.CONSULTANT")) return document.getConsultant();
		if (key.equals("DOCUMENT.SERVICEDATE")) return DataUtils.getInstance().getFormattedLocalizedDate(document.getServiceDate());
		if (key.equals("DOCUMENT.MESSAGE")) return document.getMessage();
		if (key.equals("DOCUMENT.MESSAGE1")) return document.getMessage();
		if (key.equals("DOCUMENT.MESSAGE2")) return document.getMessage2();
		if (key.equals("DOCUMENT.MESSAGE3")) return document.getMessage3();
		if (key.equals("DOCUMENT.TRANSACTION")) return Optional.ofNullable(document.getTransactionId()).orElse(Long.valueOf(0)).toString();
		if (key.equals("DOCUMENT.INVOICE")) return document.getInvoiceReference() != null ? document.getInvoiceReference().getName() : "";
		if (key.equals("DOCUMENT.WEBSHOP.ID")) return document.getWebshopId();
		if (key.equals("DOCUMENT.WEBSHOP.DATE")) return DataUtils.getInstance().getFormattedLocalizedDate(document.getWebshopDate());
		if (key.equals("DOCUMENT.ORDER.DATE")) return DataUtils.getInstance().getFormattedLocalizedDate(document.getOrderDate());
		if (key.equals("DOCUMENT.ITEMS.GROSS")) return DataUtils.getInstance().formatCurrency(documentSummary.getItemsGross());
		if (key.equals("DOCUMENT.ITEMS.NET")) return DataUtils.getInstance().formatCurrency(documentSummary.getItemsNet());
		if (key.equals("DOCUMENT.TOTAL.NET")) return DataUtils.getInstance().formatCurrency(documentSummary.getTotalNet());
		if (key.equals("DOCUMENT.TOTAL.VAT")) return DataUtils.getInstance().formatCurrency(documentSummary.getTotalVat());
		if (key.equals("DOCUMENT.TOTAL.GROSS")) return DataUtils.getInstance().formatCurrency(documentSummary.getTotalGross());
		if (key.equals("DOCUMENT.ITEMS.COUNT")) return String.format("%d", document.getItems().size());

		if (key.equals("DOCUMENT.DEPOSIT.DEPOSIT")) return DataUtils.getInstance().formatCurrency(documentSummary.getDeposit());
		if (key.equals("DOCUMENT.DEPOSIT.FINALPAYMENT")) return DataUtils.getInstance().formatCurrency(documentSummary.getFinalPayment());
		if (key.equals("DOCUMENT.DEPOSIT.DEP_TEXT")) return  preferences.getString("DEPOSIT_TEXT");
		if (key.equals("DOCUMENT.DEPOSIT.FINALPMT_TEXT")) return  preferences.getString("FINALPAYMENT_TEXT");
		if (key.equals("DOCUMENT.DEPOSIT.DEP_TEXT")) return  preferences.getString("DEPOSIT_TEXT");
		if (key.equals("DOCUMENT.DEPOSIT.FINALPMT_TEXT")) return  preferences.getString("FINALPAYMENT_TEXT");

		if (key.equals("ITEMS.DISCOUNT.PERCENT")) return DataUtils.getInstance().DoubleToFormatedPercent(document.getItemsRebate());
		if (key.equals("ITEMS.DISCOUNT.NET")) return DataUtils.getInstance().formatCurrency(documentSummary.getDiscountNet());
		if (key.equals("ITEMS.DISCOUNT.GROSS")) return DataUtils.getInstance().formatCurrency(documentSummary.getDiscountGross());

		if (key.equals("ITEMS.DISCOUNT.DAYS")) return document.getPayment().getDiscountDays().toString();
		if (key.equals("ITEMS.DISCOUNT.DUEDATE")) {
			return getDiscountDueDate(document);
		}
		if (key.equals("ITEMS.DISCOUNT.DISCOUNTPERCENT")) return DataUtils.getInstance().DoubleToFormatedPercent(document.getPayment().getDiscountValue());
		double percent = document.getPayment().getDiscountValue();
		if (key.equals("ITEMS.DISCOUNT.VALUE")) {
			return DataUtils.getInstance().formatCurrency(documentSummary.getTotalGross().multiply(1 - percent));
		}
		if (key.equals("ITEMS.DISCOUNT.NETVALUE")) {
			return DataUtils.getInstance().formatCurrency(documentSummary.getTotalNet().multiply(1 - percent));
		}
		if (key.equals("ITEMS.DISCOUNT.TARAVALUE")) {
			return DataUtils.getInstance().formatCurrency(documentSummary.getTotalVat().multiply(1 - percent));
		}

		if (key.equals("SHIPPING.NET")) return DataUtils.getInstance().formatCurrency(documentSummary.getShippingNet());
		if (key.equals("SHIPPING.VAT")) return DataUtils.getInstance().formatCurrency(documentSummary.getShippingVat());
		if (key.equals("SHIPPING.GROSS")) return DataUtils.getInstance().formatCurrency(documentSummary.getShippingGross());
//		if (key.equals("SHIPPING.NAME")) return document.getStringValueByKey("shippingname");
		if (key.equals("SHIPPING.DESCRIPTION")) return document.getShipping().getDescription();
		if (key.equals("SHIPPING.VAT.DESCRIPTION")) return document.getShipping().getShippingVat().getDescription();
		if (key.equals("DOCUMENT.DUNNING.LEVEL") && document.getBillingType() == BillingType.DUNNING) return ((Dunning)document).getDunningLevel().toString();


		// Get the reference string to other documents
		if (key.startsWith("DOCUMENT.REFERENCE.")) {
			Transaction transaction = ContextInjectionFactory.make(Transaction.class, context).of(document);
			if (key.equals("DOCUMENT.REFERENCE.OFFER")) return transaction.getReference(DocumentType.OFFER);
			if (key.equals("DOCUMENT.REFERENCE.ORDER")) return transaction.getReference(DocumentType.ORDER);
			if (key.equals("DOCUMENT.REFERENCE.CONFIRMATION")) return transaction.getReference(DocumentType.CONFIRMATION);
			if (key.equals("DOCUMENT.REFERENCE.INVOICE")) return transaction.getReference(DocumentType.INVOICE);
			if (key.equals("DOCUMENT.REFERENCE.INVOICE.DATE")) return transaction.getFirstReferencedDocumentDate(DocumentType.INVOICE);
			if (key.equals("DOCUMENT.REFERENCE.DELIVERY")) return transaction.getReference(DocumentType.DELIVERY);
			if (key.equals("DOCUMENT.REFERENCE.CREDIT")) return transaction.getReference(DocumentType.CREDIT);
			if (key.equals("DOCUMENT.REFERENCE.DUNNING")) return transaction.getReference(DocumentType.DUNNING);
			if (key.equals("DOCUMENT.REFERENCE.PROFORMA")) return transaction.getReference(DocumentType.PROFORMA);

		}
		
		if (key.equals("PAYMENT.TEXT")) {
			// Replace the placeholders in the payment text
			String paymenttext = createPaymentText(document, documentSummary, percent);
			return paymenttext;
		}
		
		//setProperty("PAYMENT.NAME", document.getStringValueByKey("paymentname"));
		if (key.equals("PAYMENT.DESCRIPTION")) return document.getPayment().getDescription();
		if (key.equals("PAYMENT.PAID.VALUE")) return DataUtils.getInstance().DoubleToFormatedPriceRound(document.getPaidValue());
		if (key.equals("PAYMENT.PAID.DATE")) return DataUtils.getInstance().getFormattedLocalizedDate(document.getPayDate());
		if (key.equals("PAYMENT.DUE.DAYS")) return Integer.toString(document.getDueDays());
		if (key.equals("PAYMENT.DUE.DATE")) {
            LocalDateTime newDate = DataUtils.getInstance().addToDate(document.getDocumentDate(), document.getDueDays());
            return newDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        }
		if (key.equals("PAYMENT.PAID")) return document.getPaid().toString();
		
		String key2;
		String addressField;
		
		if (key.startsWith("DELIVERY.")) {
			key2 = key.substring(9);
            addressField = document.getDeliveryContact() != null ? document.getDeliveryContact().getAddress().getManualAddress() : contactUtil
                    .getAddressAsString(document.getDeliveryContact());
		}
		else {
			key2 = key;
			addressField = Optional.ofNullable(document.getBillingContact().getAddress().getManualAddress()).orElse(contactUtil.getAddressAsString(document.getBillingContact()));
		}

		if (key2.equals("ADDRESS.FIRSTLINE")) return getDataFromAddressField(addressField,"addressfirstline");
		
		// There is a reference to a contact. Use this
		if (contact != null) {
		    if (key.equals("ADDRESS")) return contactUtil.getAddressAsString(contact);
			if (key.equals("ADDRESS.GENDER")) return contactUtil.getGenderString(contact);
			if (key.equals("ADDRESS.GREETING")) return contactUtil.getGreeting(contact);
			if (key.equals("ADDRESS.TITLE")) return contact.getTitle();
			if (key.equals("ADDRESS.NAME")) return contact.getName();
			if (key.equals("ADDRESS.BIRTHDAY")) {
				return contact.getBirthday() == null ? "" : DataUtils.getInstance().getFormattedLocalizedDate(contact.getBirthday());
			}
			if (key.equals("ADDRESS.NAMEWITHCOMPANY")) return contactUtil.getNameWithCompany(contact);
			if (key.equals("ADDRESS.FIRSTANDLASTNAME")) return contactUtil.getFirstAndLastName(contact);
			if (key.equals("ADDRESS.FIRSTNAME")) return contact.getFirstName();
			if (key.equals("ADDRESS.LASTNAME")) return contact.getName();
			if (key.equals("ADDRESS.COMPANY")) return contact.getCompany();

			Address address = contact.getAddress();
			if(address != null) {
    			if (key.equals("ADDRESS.STREET")) return address.getStreet();
    			if (key.equals("ADDRESS.STREETNAME")) return getStreetName(address.getStreet());
    			if (key.equals("ADDRESS.STREETNO")) return getStreetNo(address.getStreet());
    			if (key.equals("ADDRESS.ZIP")) return address.getZip();
    			if (key.equals("ADDRESS.CITY")) return address.getCity();
                if (key.equals("ADDRESS.COUNTRY.CODE2")) return address.getCountryCode();
                Optional<Locale> locale = LocaleUtil.getInstance().findByCode(address.getCountryCode());
                if (key.equals("ADDRESS.COUNTRY")) return locale.isPresent() ? locale.get().getDisplayCountry() : "??";
                if (key.equals("ADDRESS.COUNTRY.CODE3")) return locale.isPresent() ? locale.get().getISO3Country() : "???";
			}
			
			BankAccount bankAccount = contact.getBankAccount();
			if(bankAccount != null) {
                if (key.equals("ADDRESS.BANK.ACCOUNT.HOLDER")) return bankAccount.getAccountHolder();
    			if (key.equals("ADDRESS.BANK.ACCOUNT")) return bankAccount.getAccount();
    			if (key.equals("ADDRESS.BANK.CODE")) return Optional.ofNullable(bankAccount.getBankCode()).orElse(Integer.valueOf(0)).toString();
    			if (key.equals("ADDRESS.BANK.NAME")) return bankAccount.getBankName();
    			if (key.equals("ADDRESS.BANK.IBAN")) return bankAccount.getIban();
    			if (key.equals("ADDRESS.BANK.BIC")) return bankAccount.getBic();
			}
			if (key.equals("ADDRESS.NR")) return contact.getCustomerNumber();
			if (key.equals("ADDRESS.PHONE")) return contact.getPhone();
			if (key.equals("ADDRESS.PHONE.PRE")) return getTelPrePost(contact.getPhone(), true);
			if (key.equals("ADDRESS.PHONE.POST")) return getTelPrePost(contact.getPhone(), false);
			if (key.equals("ADDRESS.FAX")) return contact.getFax();
			if (key.equals("ADDRESS.FAX.PRE")) return getTelPrePost(contact.getFax(), true);
			if (key.equals("ADDRESS.FAX.POST")) return getTelPrePost(contact.getFax(), false);
			if (key.equals("ADDRESS.MOBILE")) return contact.getMobile();
			if (key.equals("ADDRESS.MOBILE.PRE")) return getTelPrePost(contact.getMobile(), true);
			if (key.equals("ADDRESS.MOBILE.POST")) return getTelPrePost(contact.getMobile(), false);
			if (key.equals("ADDRESS.SUPPLIER.NUMBER")) return contact.getSupplierNumber();
			if (key.equals("ADDRESS.EMAIL")) return contact.getEmail();
			if (key.equals("ADDRESS.WEBSITE")) return contact.getWebsite();
			if (key.equals("ADDRESS.VATNR")) return contact.getVatNumber();
			if (key.equals("ADDRESS.NOTE")) return contact.getNote();
			if (key.equals("ADDRESS.DISCOUNT")) return Optional.ofNullable(contact.getDiscount()).orElse(Double.valueOf(0)).toString();
			
			// now switch to delivery contact, if any
			if(document.getDeliveryContact() != null) {
			    contact = document.getDeliveryContact();
			    // if no delivery contact is available, use billing contact
			}
			if (key.equals("DELIVERY.ADDRESS")) return contactUtil.getAddressAsString(contact);
			if (key.equals("DELIVERY.ADDRESS.GENDER")) return contactUtil.getGenderString(contact);
			if (key.equals("DELIVERY.ADDRESS.GREETING")) return contactUtil.getGreeting(contact);
			if (key.equals("DELIVERY.ADDRESS.TITLE")) return contact.getTitle();
			if (key.equals("DELIVERY.ADDRESS.NAME")) return contact.getName();
			if (key.equals("DELIVERY.ADDRESS.BIRTHDAY")) {
				return contact.getBirthday() == null ? "" : DataUtils.getInstance().getFormattedLocalizedDate(contact.getBirthday());
			}
			if (key.equals("DELIVERY.ADDRESS.NAMEWITHCOMPANY")) return contactUtil.getNameWithCompany(contact);
			if (key.equals("DELIVERY.ADDRESS.FIRSTNAME")) return contact.getFirstName();
			if (key.equals("DELIVERY.ADDRESS.LASTNAME")) return contact.getName();
			if (key.equals("DELIVERY.ADDRESS.COMPANY")) return contact.getCompany();

            address = contact.getAddress();
            if(address != null) {
    			if (key.equals("DELIVERY.ADDRESS.STREET")) return address.getStreet();
    			if (key.equals("DELIVERY.ADDRESS.STREETNAME")) return getStreetName(address.getStreet());
    			if (key.equals("DELIVERY.ADDRESS.STREETNO")) return getStreetNo(address.getStreet());
    			if (key.equals("DELIVERY.ADDRESS.ZIP")) return address.getZip();
    			if (key.equals("DELIVERY.ADDRESS.CITY")) return address.getCity();
    			if (key.equals("DELIVERY.ADDRESS.COUNTRY.CODE2")) return address.getCountryCode();
    			Optional<Locale> locale = LocaleUtil.getInstance().findByCode(address.getCountryCode());
    			if (key.equals("DELIVERY.ADDRESS.COUNTRY")) return locale.isPresent() ? locale.get().getDisplayCountry() : "??";
   			    if (key.equals("DELIVERY.ADDRESS.COUNTRY.CODE3")) return locale.isPresent() ? locale.get().getISO3Country() : "???";
            }
		}
		// There is no reference - Try to get the information from the address field
		else {
			if (key2.equals("ADDRESS.GENDER")) return "";
			if (key2.equals("ADDRESS.TITLE")) return "";
			if (key2.equals("ADDRESS.NAME")) return getDataFromAddressField(addressField,"name");
			if (key2.equals("ADDRESS.FIRSTNAME")) return getDataFromAddressField(addressField,"firstname");
			if (key2.equals("ADDRESS.LASTNAME")) return getDataFromAddressField(addressField,"lastname");
			if (key2.equals("ADDRESS.COMPANY")) return getDataFromAddressField(addressField,"company");
			if (key2.equals("ADDRESS.STREET")) return getDataFromAddressField(addressField,"street");
			if (key2.equals("ADDRESS.STREETNAME")) return getDataFromAddressField(addressField,"streetname");
			if (key2.equals("ADDRESS.STREETNO")) return getDataFromAddressField(addressField,"streetno");
			if (key2.equals("ADDRESS.ZIP")) return getDataFromAddressField(addressField,"zip");
			if (key2.equals("ADDRESS.CITY")) return getDataFromAddressField(addressField,"city");
			String country = getDataFromAddressField(addressField,"country");
			if (key2.equals("ADDRESS.COUNTRY")) return country;
            Optional<Locale> locale = LocaleUtil.getInstance().findLocaleByDisplayCountry(country);
			if (key2.equals("ADDRESS.COUNTRY.CODE2")) {
				return locale.isPresent() ? locale.get().getCountry() : "??";
			}
			if (key2.equals("ADDRESS.COUNTRY.CODE3")) {
				return locale.isPresent() ? locale.get().getISO3Country() : "???";
			}

			if (key2.equals("ADDRESS.GREETING")) return contactUtil.getCommonGreeting();

			if (key.equals("ADDRESS.BANK.ACCOUNT.HOLDER")
			    || key.equals("ADDRESS.BANK.ACCOUNT")
                || key.equals("ADDRESS.BANK.CODE")
                || key.equals("ADDRESS.BANK.NAME")
                || key.equals("ADDRESS.BANK.IBAN")
                || key.equals("ADDRESS.BANK.BIC")
                || key.equals("ADDRESS.NR")
			    || key.equals("ADDRESS.PHONE")
                || key.equals("ADDRESS.PHONE.PRE")
                || key.equals("ADDRESS.PHONE.POST")
                || key.equals("ADDRESS.FAX")
                || key.equals("ADDRESS.FAX.PRE")
                || key.equals("ADDRESS.FAX.POST")
                || key.equals("ADDRESS.MOBILE")
                || key.equals("ADDRESS.MOBILE.PRE")
                || key.equals("ADDRESS.MOBILE.POST")
                || key.equals("ADDRESS.EMAIL")
                || key.equals("ADDRESS.WEBSITE")
                || key.equals("ADDRESS.VATNR")
                || key.equals("ADDRESS.NOTE")
                || key.equals("ADDRESS.DISCOUNT")) return "";
		}

		return null;
	}

	/**
     * @param document
     * @param percent
     * @return
     */
    private String createPaymentText(Document document, DocumentSummary documentSummary, double percent) {
	    String paymenttext = document.getPayment().getPaidText();
	    paymenttext = paymenttext.replace("<PAID.VALUE>", DataUtils.getInstance().DoubleToFormatedPriceRound(document.getPaidValue()));
	    paymenttext = paymenttext.replace("<PAID.DATE>", DataUtils.getInstance().getFormattedLocalizedDate(document.getPayDate()));
	    paymenttext = paymenttext.replace("<DUE.DAYS>", Integer.toString(document.getDueDays()));
	    LocalDateTime dueDate = DataUtils.getInstance().addToDate(document.getDocumentDate(), document.getDueDays());
	    paymenttext = paymenttext.replace("<DUE.DATE>", dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
	    
	    paymenttext = paymenttext.replace("<DUE.DISCOUNT.PERCENT>", DataUtils.getInstance().DoubleToFormatedPercent(document.getPayment().getDiscountValue()));
	    paymenttext = paymenttext.replace("<DUE.DISCOUNT.DAYS>", document.getPayment().getDiscountDays().toString());
	    paymenttext = paymenttext.replace("<DUE.DISCOUNT.VALUE>", DataUtils.getInstance().formatCurrency(documentSummary.getTotalGross().multiply(1 - percent)));
	    paymenttext = paymenttext.replace("<DUE.DISCOUNT.DATE>", getDiscountDueDate(document));

// FIXME doesn't exist!	    paymenttext = paymenttext.replace("<BANK.ACCOUNT.HOLDER>", preferences.getString("BANK_ACCOUNT_HOLDER"));
	    paymenttext = paymenttext.replace("<BANK.ACCOUNT>", 
	    		preferences.getString("YOURCOMPANY_COMPANY_BANKACCOUNTNR"));
	    paymenttext = paymenttext.replace("<BANK.IBAN>", 
	    		preferences.getString(Constants.PREFERENCES_YOURCOMPANY_COMPANY_IBAN));
	    paymenttext = paymenttext.replace("<BANK.BIC>", 
	    		preferences.getString(Constants.PREFERENCES_YOURCOMPANY_COMPANY_BIC));
	    paymenttext = paymenttext.replace("<BANK.NAME>", 
	    		preferences.getString(Constants.PREFERENCES_YOURCOMPANY_COMPANY_BANK));
	    paymenttext = paymenttext.replace("<BANK.CODE>", 
	    		preferences.getString("YOURCOMPANY_COMPANY_BANKCODE"));
	    paymenttext = paymenttext.replace("<YOURCOMPANY.CREDITORID>", 
	    		preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CREDITORID));
	    
	    // Additional placeholder for censored bank account
	    String censoredAccount = censorAccountNumber(preferences.getString("YOURCOMPANY_COMPANY_BANKACCOUNTNR"));
	    paymenttext = paymenttext.replace("<BANK.ACCOUNT.CENSORED>", censoredAccount);
	    censoredAccount = censorAccountNumber(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_COMPANY_IBAN));
	    paymenttext = paymenttext.replace("<BANK.IBAN.CENSORED>", censoredAccount);
	    
	    Contact contact = document.getBillingContact();
        if(contact != null && contact.getBankAccount() != null) {
    	    // debitor's bank account
    	    paymenttext = paymenttext.replace("<DEBITOR.BANK.ACCOUNT.HOLDER>", 
    	            contact.getBankAccount().getAccountHolder());
    	    paymenttext = paymenttext.replace("<DEBITOR.BANK.IBAN>", 
    	            contact.getBankAccount().getIban());
    	    paymenttext = paymenttext.replace("<DEBITOR.BANK.BIC>", 
    	            contact.getBankAccount().getBic());
    	    paymenttext = paymenttext.replace("<DEBITOR.BANK.NAME>", 
    	            contact.getBankAccount().getBankName());
    	    paymenttext = paymenttext.replace("<DEBITOR.MANDATREF>", 
    	            contact.getMandateReference());
    	    // Additional placeholder for censored bank account
    	    censoredAccount = censorAccountNumber(contact.getBankAccount().getIban());
    	    paymenttext = paymenttext.replace("<DEBITOR.BANK.IBAN.CENSORED>", censoredAccount);
	    }
	    
	    // placeholder for total sum
	    paymenttext = paymenttext.replace("<DOCUMENT.TOTAL>", DataUtils.getInstance().formatCurrency(documentSummary.getTotalGross()));
	    return paymenttext;
    }

	/**
	 * @param paymenttext
	 * @param bankAccountLength
	 * @return
	 */
	private String censorAccountNumber(String accountNumber) {
		String retval = "";
		Integer bankAccountLength = accountNumber.length();			
		// Only set placeholder if bank account exists
		if( bankAccountLength > 0 ) {				
			// Show only the last 3 digits
			Integer bankAccountCensoredLength = bankAccountLength - 3;
			String censoredDigits = "";				
			for( int i = 1; i <= bankAccountCensoredLength; i++ ) {
				censoredDigits += "*";
			}				
			retval = censoredDigits + accountNumber.substring( bankAccountCensoredLength );
		}
		return retval;
	}
	
	public static void main(String[] args) {
	    Placeholders ph = new Placeholders();
//	    System.out.println("mit null: " + censorAccountNumber(null));
	    System.out.println("mit 12: " + ph.censorAccountNumber("12"));
	    System.out.println("mit 123: " + ph.censorAccountNumber("123"));
	    System.out.println("mit 123456789: " + ph.censorAccountNumber("123456789"));
	    System.out.println("mit 9999999999999999999999999999999999: " + ph.censorAccountNumber("9999999999999999999999999999999999"));
    }

	/**
	 * @param document
	 * @return
	 */
	private String getDiscountDueDate(Document document) {
		LocalDateTime date = LocalDateTime.ofInstant(document.getDocumentDate().toInstant(), ZoneId.systemDefault());
		date = date.plusDays(document.getPayment().getDiscountDays());
		// if another Locale than the system's default Locale should be used then we
		// have to write "DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(...))" here 
		return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
	}

	/**
	 * Getter for all placeholders
	 * @return
	 * 	String array with all placeholders
	 */
	String[] getPlaceholders() {
		return placeholders;
	}
	
//	/**
//	 * Test, if the name is in the list of all placeholders
//	 * 
//	 * @param testPlaceholder
//	 * 		The placeholder to test
//	 * @return
//	 * 		TRUE, if the placeholder is in the list
//	 */
//	private boolean isPlaceholder (String testPlaceholder) {
//		
//		String placeholderName = extractPlaceholderName(testPlaceholder);
//		
//		// Test all placeholders
//		for (String placeholder : placeholders) {
//			if (placeholderName.equals(placeholder))
//				return true;
//		}
//		return false;
//	}
}

