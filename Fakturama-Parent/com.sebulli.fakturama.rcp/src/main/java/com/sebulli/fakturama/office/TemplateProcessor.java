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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.regex.Matcher;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.ULocale;
import com.sebulli.fakturama.dao.AddressDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Transaction;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.model.IDocumentAddressManager;
//import com.sebulli.fakturama.model.ModelObject;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.DocumentTypeUtil;


public class TemplateProcessor {
	
	private static final String PARAMETER_SEPARATOR = "$";

    public static final int COUNT_OF_LAST_SHOWN_DIGITS = 3; 
	    
    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private IEclipseContext context;
    
	@Inject
	private ContactsDAO contactsDAO;

	@Inject
	private AddressDAO addressDAO;
    
    @Inject
    private IDateFormatterService dateFormatterService;
    
	@Inject
	private INumberFormatterService numberFormatterService;

	@Inject
	private ILocaleService localeUtil;

    @Inject
    private IPreferenceStore preferences;
    
    @Inject
    private IDocumentAddressManager addressManager;

	
	private static NumberFormat localizedNumberFormat = NumberFormat.getInstance(ULocale.getDefault());

    private ContactUtil contactUtil;

	/**
	 * Get a part of the telephone number
	 * 
	 * @param pre
	 * 		TRUE, if the area code should be returned
	 * @return
	 * 		Part of the telephone number
	 */
	private String getTelPrePost(final String phoneNo, final boolean pre) {
		String phoneNumber = StringUtils.defaultString(phoneNo);
		// if phoneNo contains "/" or " " (space) then split there
		if(phoneNumber.startsWith("+")) {
			// phoneNo has a country dialing number, therefore we have to chain it with area code
			phoneNumber = phoneNumber.replaceFirst(" ", "_");
		}
		String parts[] = phoneNumber.trim().split("[ |/]", 2);
		
		// Split the number
		if (parts.length < 2) {
			String tel = parts[0];
			// divide the number at the 4th position
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
			String[] area = StringUtils.split(parts[0], "_");  // phone number with country code
			return pre ? StringUtils.join(area, " ") : parts[1];
		}
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
				
				//Escape sequences...
				twoStrings[0] = encodeEntities(removeQuotationMarks(twoStrings[0]));
			    
				// Replace the value, if it is equal to the entry
				if (DataUtils.getInstance().replaceAllAccentedChars(encodeEntities(value)).equalsIgnoreCase(
				        DataUtils.getInstance().replaceAllAccentedChars(twoStrings[0]))) {
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
	 * 		The value modified by the parameters
	 */
	String interpretParameters(final String placeholder, final String pValue) {
		String par;
		String retval = pValue;
		
		if (retval == null)
			return retval;
		
		// The parameters "PRE" and "POST" are only used, if the
		// placeholder value is not empty
		if (!retval.isEmpty()) {
			
			// Parameter "PRE"
			par = extractParam(placeholder,"PRE");
			if (!par.isEmpty())
					retval =  removeQuotationMarks(par) + retval;

			// Parameter "POST"
			par = extractParam(placeholder,"POST");
			if (!par.isEmpty())
					retval += removeQuotationMarks(par);

			// Parameter "INONELINE"
			par = extractParam(placeholder,"INONELINE");
			if (!par.isEmpty())
				retval = StringInOneLine(retval, removeQuotationMarks(par));

			// Parameter "REPLACE"
			par = extractParam(placeholder,"REPLACE");
			if (!par.isEmpty())
				retval = replaceValues(removeQuotationMarks(par) , retval);

			// Parameter "FORMAT"
			par = extractParam(placeholder,"FORMAT");
			if (!par.isEmpty()) {
				try {
					Double parsedDouble = localizedNumberFormat.parse(retval).doubleValue();
					retval = numberFormatterService.DoubleToDecimalFormatedValue(parsedDouble, par);
				}
				catch (ParseException e) {
					retval = "### NVL ###";
				}
			}

			// Parameter "DFORMAT"
			par = extractParam(placeholder, "DFORMAT");
			if (!par.isEmpty()) {
				try {
					GregorianCalendar checkDate = dateFormatterService.getCalendarFromDateString(retval);
					SimpleDateFormat sdf = new SimpleDateFormat(par);
					retval = sdf.format(checkDate.getTime());
				} catch (IllegalArgumentException e) {
					retval = "### NVL ###";
				}
			}
			
			// extract first n characters from string
			par = extractParam(placeholder, "FIRST");
			if (!par.isEmpty()) {
				Integer length = extractLengthFromParameter(par, retval.length());
				if (length.compareTo(Integer.valueOf(0)) >= 0) {
					int len = length.compareTo(retval.length()) < 0 ? length : retval.length();
					retval = retval.substring(0, len);
				}
			}
			
			// extract last n characters from string
			par = extractParam(placeholder, "LAST");
			if (!par.isEmpty()) {
				Integer length = extractLengthFromParameter(par, retval.length());
				if (length.compareTo(Integer.valueOf(0)) >= 0) {
					int len = length.compareTo(retval.length()) < 0 ? length : retval.length();
					retval = retval.substring(retval.length() - len);
				}
			}
			
			// extract range from n to m characters from string
			par = extractParam(placeholder, "RANGE");
			if(!par.isEmpty()) {
				String[] boundaries = par.split(",");
				if(boundaries.length == 2) {
					// for customer convenience we start counting from 1
					Integer start = extractLengthFromParameter(boundaries[0], 0) - 1;
					Integer end = extractLengthFromParameter(boundaries[1], retval.length());
					if (end.compareTo(Integer.valueOf(0)) >= 0 ) {
						int len = end.compareTo(retval.length()) < 0 ? end : retval.length();
						retval = len == 0 ? "" : retval.substring(start, len);
					}
				}
			}
			
			// extract without range from n to m characters from string
			par = extractParam(placeholder, "EXRANGE");
			if (!par.isEmpty()) {
				String[] boundaries = par.split(",");
				if (boundaries.length == 2) {
					// for customer convenience we start counting from 1
					Integer start = extractLengthFromParameter(boundaries[0], 0) - 1;
					Integer end = extractLengthFromParameter(boundaries[1], retval.length());
					if (end.compareTo(Integer.valueOf(0)) >= 0) {
						int len = end.compareTo(retval.length()) < 0 ? end : retval.length();
						if (len == 0) {
							retval = "";
						} else {
							String first = retval.substring(0, Math.max(0, start));
							String last = retval.substring(len, retval.length());
							retval = first + last;
						}
					}
				}
			}
		}
		else {
			// Parameter "EMPTY"
			par = extractParam(placeholder,"EMPTY");
			if (!par.isEmpty())
				retval = removeQuotationMarks(par);
		}
		
		// Encode some special characters
		return encodeEntities(retval);
	}

	private Integer extractLengthFromParameter(String par, Integer defaultValue) {
		Integer length;
		try {
			length = Integer.parseInt(par);
		} catch (NumberFormatException e) {
			length = defaultValue;
		}
		return length;
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
	
	public String fill(Document document, String template) {
	    
	    return "";
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
		param = PARAMETER_SEPARATOR + param + ":";
		
		// Return, if parameter was not in placeholder's name
		if (!placeholder.contains(param))
			return "";

		// Extract the string after the parameter name
		s = placeholder.substring(placeholder.indexOf(param)+param.length());

		// Extract the string until the next parameter, or the end
		int i;
		i = s.indexOf(PARAMETER_SEPARATOR);
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
		return s.split("\\$", 2)[0];
	}
	
	/**
	 * Decode the special characters
	 * 
	 * @param s
	 * 	String to convert
	 * @return
	 *  Converted
	 */
	private String encodeEntities(String s) {
		if (StringUtils.length(s) > 0) {
			s = s.replaceAll("%LT", "<");
			s = s.replaceAll("%GT", ">");
			s = s.replaceAll("%NL", "\n");
			s = s.replaceAll("%TAB", "\t");
			s = s.replaceAll("%SPACE", " ");
			s = s.replaceAll("%DOLLAR", Matcher.quoteReplacement(PARAMETER_SEPARATOR));
			s = s.replaceAll("%COMMA", Matcher.quoteReplacement(","));
			s = s.replaceAll("%EURO", Matcher.quoteReplacement("€"));
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
			s = s.replaceAll("%%", Matcher.quoteReplacement("%"));
		}
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
			if (key.equals("YOURCOMPANY.COMPANY")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME);

			String owner = preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER);
			if (key.equals("YOURCOMPANY.OWNER")) return  owner;
			if (key.equals("YOURCOMPANY.OWNER.FIRSTNAME")) return  contactUtil.getFirstName(owner);
			if (key.equals("YOURCOMPANY.OWNER.LASTNAME")) return  contactUtil.getLastName(owner);

			String streetWithNo = preferences.getString(Constants.PREFERENCES_YOURCOMPANY_STREET);
			if (key.equals("YOURCOMPANY.STREET")) return  streetWithNo;
			if (key.equals("YOURCOMPANY.STREETNAME")) return  contactUtil.getStreetName(streetWithNo);
			if (key.equals("YOURCOMPANY.STREETNO")) return  contactUtil.getStreetNo(streetWithNo);

			if (key.equals("YOURCOMPANY.ZIP")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_ZIP);
			if (key.equals("YOURCOMPANY.CITY")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY);
			if (key.equals("YOURCOMPANY.COUNTRY")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_COUNTRY);
			if (key.equals("YOURCOMPANY.EMAIL")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_EMAIL);
			if (key.equals("YOURCOMPANY.PHONE")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TEL);
			if (key.equals("YOURCOMPANY.PHONE.PRE")) return  getTelPrePost(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TEL), true);
			if (key.equals("YOURCOMPANY.PHONE.POST")) return  getTelPrePost(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TEL), false);
			if (key.equals("YOURCOMPANY.MOBILE")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_MOBILE);
			if (key.equals("YOURCOMPANY.FAX")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_FAX);
			if (key.equals("YOURCOMPANY.FAX.PRE")) return  getTelPrePost(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_FAX), true);
			if (key.equals("YOURCOMPANY.FAX.POST")) return  getTelPrePost(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_FAX), false);
			if (key.equals("YOURCOMPANY.WEBSITE")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_WEBSITE);
			if (key.equals("YOURCOMPANY.VATNR")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_VATNR);
			if (key.equals("YOURCOMPANY.TAXNR")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TAXNR);
			if (key.equals("YOURCOMPANY.TAXOFFICE")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TAXOFFICE);
			if (key.equals("YOURCOMPANY.BANKACCOUNTNR")) return  preferences.getString("YOURCOMPANY_COMPANY_BANKACCOUNTNR");
			if (key.equals("YOURCOMPANY.BANKCODE")) return  preferences.getString("YOURCOMPANY_COMPANY_BANKCODE");
			if (key.equals("YOURCOMPANY.BANK")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_BANK);
			if (key.equals("YOURCOMPANY.IBAN")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_IBAN);
			if (key.equals("YOURCOMPANY.BIC")) return  preferences.getString(Constants.PREFERENCES_YOURCOMPANY_BIC);
		}

		if (document == null)
			return null;

		if (key.equals("DOCUMENT.DATE")) return dateFormatterService.getFormattedLocalizedDate(document.getDocumentDate());
		if (key.equals("DOCUMENT.ADDRESSES.EQUAL")) {
            return (contactUtil.deliveryAddressEqualsBillingAddress(document)).toString();
        }

		// Get address and delivery address
		// with option "DIFFERENT" and without
		String deliverystring;
		String differentstring;
		// address and delivery address
		DocumentReceiver billingAdress = addressManager.getBillingAdress(document);
		DocumentReceiver deliveryAdress = addressManager.getDeliveryAdress(document);
		for (int i = 0;i<2 ; i++) {
		    String s;
			deliverystring = i==1 ? "delivery" : "";
			if(i == 1) {
				s = contactUtil.getAddressAsString(deliveryAdress);
			} else {
				s = contactUtil.getAddressAsString(billingAdress);
			}
			
			//  with option "DIFFERENT" and without
			for (int ii = 0 ; ii<2; ii++) {
				differentstring = ii==1 ? ".DIFFERENT" : "";
				if (ii==1 && contactUtil.deliveryAddressEqualsBillingAddress(document))
					s="";
				if (key.equals("DOCUMENT" + differentstring +"."+ deliverystring.toUpperCase()+ "ADDRESS")) return s;
			}
		}
		
		// Get information from the document
		if (key.equals("DOCUMENT.TYPE")) return msg.getMessageFromKey(DocumentTypeUtil.findByBillingType(document.getBillingType()).getSingularKey());
		if (key.equals("DOCUMENT.NAME")) return document.getName();
		if (key.equals("DOCUMENT.CUSTOMERREF")) return document.getCustomerRef();
		if (key.equals("DOCUMENT.CONSULTANT")) return billingAdress.getConsultant();
		if (key.equals("DOCUMENT.SERVICEDATE")) return dateFormatterService.getFormattedLocalizedDate(document.getServiceDate());
		if (key.equals("DOCUMENT.MESSAGE")) return document.getMessage();
		if (key.equals("DOCUMENT.MESSAGE1")) return document.getMessage();
		if (key.equals("DOCUMENT.MESSAGE2")) return document.getMessage2();
		if (key.equals("DOCUMENT.MESSAGE3")) return document.getMessage3();
		if (key.equals("DOCUMENT.TRANSACTION")) return Optional.ofNullable(document.getTransactionId()).orElse(Integer.valueOf(0)).toString();
		if (key.equals("DOCUMENT.INVOICE")) return document.getInvoiceReference() != null ? document.getInvoiceReference().getName() : "";
		if (key.equals("DOCUMENT.WEBSHOP.ID")) return document.getWebshopId();
		if (key.equals("DOCUMENT.WEBSHOP.DATE")) return dateFormatterService.getFormattedLocalizedDate(document.getWebshopDate());
		if (key.equals("DOCUMENT.ORDER.DATE")) return dateFormatterService.getFormattedLocalizedDate(document.getOrderDate());
		if (key.equals("DOCUMENT.VESTINGPERIOD.START")) return dateFormatterService.getFormattedLocalizedDate(document.getVestingPeriodStart());
		if (key.equals("DOCUMENT.VESTINGPERIOD.END")) return dateFormatterService.getFormattedLocalizedDate(document.getVestingPeriodEnd());
		if (key.equals("DOCUMENT.ITEMS.GROSS")) return numberFormatterService.formatCurrency(documentSummary.getItemsGross());
		
		if (key.equals("DOCUMENT.ITEMS.NET")) return numberFormatterService.formatCurrency(documentSummary.getItemsNet());
		
		// FAK-432
		// discount is negative
		if (key.equals("DOCUMENT.ITEMS.NET.DISCOUNTED")) return numberFormatterService.formatCurrency(documentSummary.getItemsNet().add(documentSummary.getDiscountNet()));
		if (key.equals("DOCUMENT.TOTAL.NET")) return numberFormatterService.formatCurrency(documentSummary.getTotalNet());
		if (key.equals("DOCUMENT.TOTAL.VAT")) return numberFormatterService.formatCurrency(documentSummary.getTotalVat());
		if (key.equals("DOCUMENT.TOTAL.GROSS")) return numberFormatterService.formatCurrency(documentSummary.getTotalGross());
		if (key.equals("DOCUMENT.TOTAL.QUANTITY")) return Double.toString(documentSummary.getTotalQuantity()); // FAK-410
		if (key.equals("DOCUMENT.ITEMS.COUNT")) return String.format("%d", document.getItems().size());

		if (key.startsWith("DOCUMENT.WEIGHT")) {
			if (key.equals("DOCUMENT.WEIGHT.TARA"))
				return numberFormatterService.doubleToFormattedQuantity(document.getTara());
			double netWeightValue = document.getItems().stream().mapToDouble(d -> d.getWeight() != null ? d.getWeight() : Double.valueOf(0.0)).sum();
			if (key.equals("DOCUMENT.WEIGHT.NET"))
				return numberFormatterService.doubleToFormattedQuantity(netWeightValue);
			Double taraValue = document.getTara() != null ? document.getTara() : Double.valueOf(0.0);
			if (key.equals("DOCUMENT.WEIGHT.TOTAL"))
				return numberFormatterService.doubleToFormattedQuantity(netWeightValue + taraValue);
		}		
		
		if (key.equals("DOCUMENT.DEPOSIT.DEPOSIT")) return numberFormatterService.formatCurrency(documentSummary.getDeposit());
		if (key.equals("DOCUMENT.DEPOSIT.FINALPAYMENT")) return numberFormatterService.formatCurrency(documentSummary.getFinalPayment());
		if (key.equals("DOCUMENT.DEPOSIT.DEP_TEXT")) return  preferences.getString(Constants.PREFERENCES_DEPOSIT_TEXT);
		if (key.equals("DOCUMENT.DEPOSIT.FINALPMT_TEXT")) return  preferences.getString(Constants.PREFERENCES_FINALPAYMENT_TEXT);

		if (key.equals("ITEMS.DISCOUNT.PERCENT") && Optional.ofNullable(document.getItemsRebate()).orElse(NumberUtils.DOUBLE_ZERO).compareTo(NumberUtils.DOUBLE_ZERO) != 0) {
			Double itemsRebate = document.getItemsRebate();
            if(itemsRebate != null && itemsRebate < NumberUtils.DOUBLE_ZERO) {
            	itemsRebate *= NumberUtils.DOUBLE_MINUS_ONE; // make rebate positive (see https://bugs.fakturama.info/view.php?id=937)
            }

			return numberFormatterService.DoubleToFormatedPercent(itemsRebate);
		}
		if (key.equals("ITEMS.DISCOUNT.NET")) return numberFormatterService.formatCurrency(documentSummary.getDiscountNet());
		if (key.equals("ITEMS.DISCOUNT.GROSS")) return numberFormatterService.formatCurrency(documentSummary.getDiscountGross());

		if(document.getPayment() != null) {
			if (key.equals("ITEMS.DISCOUNT.DAYS")) return document.getPayment().getDiscountDays().toString();
			if (key.equals("ITEMS.DISCOUNT.DUEDATE")) {
				return getDiscountDueDate(document);
			}
			double percent = document.getPayment().getDiscountValue();
			if (key.equals("ITEMS.DISCOUNT.DISCOUNTPERCENT")) return numberFormatterService.DoubleToFormatedPercent(percent);
			if (key.equals("ITEMS.DISCOUNT.VALUE")) {
				return numberFormatterService.formatCurrency(documentSummary.getTotalGross().multiply(1 - percent));
			}
			if (key.equals("ITEMS.DISCOUNT.NETVALUE")) {
				return numberFormatterService.formatCurrency(documentSummary.getTotalNet().multiply(1 - percent));
			}
			if (key.equals("ITEMS.DISCOUNT.TARAVALUE")) {
				return numberFormatterService.formatCurrency(documentSummary.getTotalVat().multiply(1 - percent));
			}
			
			if (key.equals("PAYMENT.TEXT")) {
			    
				// Replace the placeholders in the payment text
				return createPaymentText(document, documentSummary, percent);
			}
		}

		if (key.equals("SHIPPING.NET")) return numberFormatterService.formatCurrency(documentSummary.getShippingNet());
		if (key.equals("SHIPPING.VAT")) return numberFormatterService.formatCurrency(documentSummary.getShippingVat());
		if (key.equals("SHIPPING.GROSS")) return numberFormatterService.formatCurrency(documentSummary.getShippingGross());
		if (key.equals("SHIPPING.NAME")) return document.getShipping() != null ? document.getShipping().getName() : "";
		if (key.equals("SHIPPING.DESCRIPTION")) return document.getShipping() != null ? document.getShipping().getDescription() : document.getAdditionalInfo().getShippingDescription();
		if (key.equals("SHIPPING.VAT.DESCRIPTION")) return document.getShipping() != null ? document.getShipping().getShippingVat().getDescription() : "";
		if (key.equals("DOCUMENT.DUNNING.LEVEL") && document.getBillingType() == BillingType.DUNNING) return ((Dunning)document).getDunningLevel().toString();

		// Get the reference string to other documents
		if (key.startsWith("DOCUMENT.REFERENCE.")) {
			Transaction transaction = ContextInjectionFactory.make(Transaction.class, context).of(document);
			if (transaction != null) {
				switch(key) {
				case "DOCUMENT.REFERENCE.OFFER":
					return transaction.getReference(DocumentType.OFFER);
				case "DOCUMENT.REFERENCE.ORDER":
					return transaction.getReference(DocumentType.ORDER);
				case "DOCUMENT.REFERENCE.CONFIRMATION":
					return transaction.getReference(DocumentType.CONFIRMATION);
				case "DOCUMENT.REFERENCE.INVOICE":
					return transaction.getReference(DocumentType.INVOICE);
				case "DOCUMENT.REFERENCE.INVOICE.DATE":
					return transaction.getFirstReferencedDocumentDate(DocumentType.INVOICE);
//				case "DOCUMENT.REFERENCE.INVOICE.DUEDATE":
//					return transaction.getFirstReferencedDocumentDueDate(DocumentType.INVOICE);
				case "DOCUMENT.REFERENCE.DELIVERY":
					return transaction.getReference(DocumentType.DELIVERY);
				case "DOCUMENT.REFERENCE.CREDIT":
					return transaction.getReference(DocumentType.CREDIT);
				case "DOCUMENT.REFERENCE.DUNNING":
					return transaction.getReference(DocumentType.DUNNING);
				case "DOCUMENT.REFERENCE.PROFORMA":
					return transaction.getReference(DocumentType.PROFORMA);
				}
			}
		}
		
		//setProperty("PAYMENT.NAME", document.getStringValueByKey("paymentname"));
		if (key.equals("PAYMENT.DESCRIPTION")) {
			return document.getPayment() != null ? document.getPayment().getDescription() : document.getAdditionalInfo().getPaymentDescription();
		}
		if (key.equals("PAYMENT.PAID.VALUE")) return numberFormatterService.DoubleToFormatedPriceRound(document.getPaidValue());
		if (key.equals("PAYMENT.PAID.DATE")) return dateFormatterService.getFormattedLocalizedDate(document.getPayDate());
		if (key.equals("PAYMENT.DUE.DAYS")) return Integer.toString(document.getDueDays());
		if (key.equals("PAYMENT.DUE.DATE")) {
            LocalDateTime newDate = DataUtils.getInstance().addToDate(document.getDocumentDate(), document.getDueDays());
            return newDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        }
		if (key.equals("PAYMENT.PAID")) return BooleanUtils.toStringTrueFalse(document.getPaid());
		
		String key2;
		String addressField;
		
		if (key.startsWith("DELIVERY.")) {
			key2 = key.substring(9);
            addressField = deliveryAdress != null 
            		? contactUtil.getAddressAsString(deliveryAdress) 
            		: contactUtil.getAddressAsString(billingAdress);
		}
		else {
			key2 = key;
			addressField = contactUtil.getAddressAsString(billingAdress);
		}

		if (key2.equals("ADDRESS.FIRSTLINE")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_ADDRESSFIRSTLINE);
		
		// Get the contact of the UniDataSet document
	
		DocumentReceiver contact = billingAdress;
		// There is a reference to a contact. Use this (but only if it's a valid contact!)
		if (contact != null && (key.startsWith("ADDRESS") || key.startsWith("DELIVERY"))) {
		    Optional<String> checked = checkAddressPlaceholders(contact, key, ContactType.BILLING);
		    if(checked.isPresent()) {
		        return checked.get();
		    }
			
			// now switch to delivery contact, if any
			if(deliveryAdress != null) {
			    contact = deliveryAdress;
			    // if no delivery contact is available, use billing contact
			}
            checked = checkAddressPlaceholders(contact, key, ContactType.DELIVERY);
            if(checked.isPresent()) {
                return checked.get();
            }
  		}
		// There is no reference - Try to get the information from the address field
		else {
			if (key2.equals("ADDRESS.GENDER")) return "";
			if (key2.equals("ADDRESS.TITLE")) return "";
			if (key2.equals("ADDRESS.NAME")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_NAME);
			if (key2.equals("ADDRESS.FIRSTNAME")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_FIRSTNAME);
			if (key2.equals("ADDRESS.LASTNAME")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_LASTNAME);
			if (key2.equals("ADDRESS.COMPANY")) return contactUtil.getDataFromAddressField(addressField,"company");
			if (key2.equals("ADDRESS.STREET")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_STREET);
			if (key2.equals("ADDRESS.STREETNAME")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_STREETNAME);
			if (key2.equals("ADDRESS.STREETNO")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_STREETNO);
			if (key2.equals("ADDRESS.ZIP")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_ZIP);
			if (key2.equals("ADDRESS.CITY")) return contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_CITY);
			String country = contactUtil.getDataFromAddressField(addressField, ContactUtil.KEY_COUNTY);
			if (key2.equals("ADDRESS.COUNTRY")) return country;
            Optional<ULocale> locale = localeUtil.findLocaleByDisplayCountry(country);
			if (key2.equals("ADDRESS.COUNTRY.CODE2")) {
				return locale.isPresent() ? locale.get().getCountry() : localeUtil.getDefaultLocale().getCountry();
			}
			if (key2.equals("ADDRESS.COUNTRY.CODE3")) {
				return locale.isPresent() ? locale.get().getISO3Country() : localeUtil.getDefaultLocale().getISO3Country();
			}

			if (key2.equals("ADDRESS.GREETING")) return contactUtil.getCommonGreeting();

			// indeterminable fields in this case
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

	private Optional<String> checkAddressPlaceholders(DocumentReceiver contact, String key, ContactType billing) {
	    if(key.startsWith(billing.getName())) {
	        key = key.replaceAll(billing.getName() + "\\.", "");
	    }
        if (key.equals("ADDRESS")) return Optional.ofNullable(contactUtil.getAddressAsString(contact));
        if (key.equals("ADDRESS.GENDER")) return Optional.ofNullable(contactUtil.getGenderString(contact));
        if (key.equals("ADDRESS.GREETING")) return Optional.ofNullable(contactUtil.getGreeting(contact));
        if (key.equals("ADDRESS.TITLE")) return Optional.ofNullable(contact.getTitle());
        if (key.equals("ADDRESS.NAME")) return Optional.ofNullable(contactUtil.getFirstAndLastName(contact));
        if (key.equals("ADDRESS.NAMEWITHCOMPANY")) return Optional.ofNullable(contactUtil.getNameWithCompany(contact));
        if (key.equals("ADDRESS.FIRSTANDLASTNAME")) return Optional.ofNullable(contactUtil.getFirstAndLastName(contact));
        if (key.equals("ADDRESS.FIRSTNAME")) return Optional.ofNullable(contact.getFirstName());
        if (key.equals("ADDRESS.LASTNAME")) return Optional.ofNullable(contact.getName());
        if (key.equals("ADDRESS.COMPANY")) return Optional.ofNullable(contact.getCompany());

        if (key.equals("ADDRESS.STREET")) return Optional.ofNullable(contact.getStreet());
        if (key.equals("ADDRESS.STREETNAME")) return Optional.ofNullable(contactUtil.getStreetName(contact.getStreet()));
        if (key.equals("ADDRESS.STREETNO")) return Optional.ofNullable(contactUtil.getStreetNo(contact.getStreet()));
        if (key.equals("ADDRESS.ZIP")) return Optional.ofNullable(contact.getZip());
        if (key.equals("ADDRESS.CITY")) return Optional.ofNullable(contact.getCity());
        if (key.equals("ADDRESS.COUNTRY.CODE2")) return Optional.ofNullable(contact.getCountryCode());
        
        Optional<ULocale> locale = localeUtil.findByCode(contact.getCountryCode());
        if (key.equals("ADDRESS.COUNTRY")) return Optional.ofNullable(locale.isPresent() ? locale.get().getDisplayCountry() : "??");
        if (key.equals("ADDRESS.COUNTRY.CODE3")) return Optional.ofNullable(locale.isPresent() ? locale.get().getISO3Country() : "???");

        Contact originContact;
        originContact = contact.getOriginContactId() != null ? contactsDAO.findById(contact.getOriginContactId()) : null;
        if(originContact != null) {
            switch (key) {
            case "ADDRESS.ALIAS":
                return Optional.ofNullable(originContact.getAlias());
            case "ADDRESS.REGISTERNUMBER":
                return Optional.ofNullable(originContact.getRegisterNumber());
            case "ADDRESS.WEBSITE":
                return Optional.ofNullable(originContact.getWebsite());
            case "ADDRESS.VATNR":
                return Optional.ofNullable(originContact.getVatNumber());
            case "ADDRESS.NOTE":
                return Optional.ofNullable(originContact.getNote());
                
            // to be continued...
            default:
                break;
            }
            if (key.equals("ADDRESS.BIRTHDAY")) return Optional.ofNullable(originContact.getBirthday() == null ? "" : dateFormatterService.getFormattedLocalizedDate(originContact.getBirthday()));
            if (key.equals("ADDRESS.DISCOUNT")) return Optional.ofNullable(Optional.ofNullable(originContact.getDiscount()).orElse(Double.valueOf(0)).toString());
            if (key.equals("ADDRESS.MANDATEREFERENCE")) return Optional.ofNullable(originContact.getMandateReference());

            if(contact.getOriginAddressId() != null) {
                Address address = addressDAO.findById(contact.getOriginAddressId());
                if (key.equals("ADDRESS.NAMESUFFIX")) return Optional.ofNullable(address == null ? "" : address.getName());
                if (key.equals("ADDRESS.CITYADDON")) return Optional.ofNullable(address == null ? "" : address.getCityAddon());
                if (key.equals("ADDRESS.ADDRESSADDON")) return Optional.ofNullable(address == null ? "" : address.getAddressAddon());
                if (key.equals("ADDRESS.PHONE2")) return Optional.ofNullable(address == null ? "" : address.getAdditionalPhone());
            }
            BankAccount bankAccount = originContact.getBankAccount();
            if(bankAccount != null) {
                if (key.equals("ADDRESS.BANK.ACCOUNT.HOLDER")) return Optional.ofNullable(bankAccount.getAccountHolder());
                if (key.equals("ADDRESS.BANK.ACCOUNT")) return Optional.ofNullable(bankAccount.getName());
                if (key.equals("ADDRESS.BANK.CODE")) return Optional.ofNullable(Optional.ofNullable(bankAccount.getBankCode()).orElse(Integer.valueOf(0)).toString());
                if (key.equals("ADDRESS.BANK.NAME")) return Optional.ofNullable(bankAccount.getBankName());
                if (key.equals("ADDRESS.BANK.IBAN")) return Optional.ofNullable(bankAccount.getIban());
                if (key.equals("ADDRESS.BANK.BIC")) return Optional.ofNullable(bankAccount.getBic());
            }
        }
        if (key.equals("ADDRESS.NR")) return Optional.ofNullable(contact.getCustomerNumber());
        if (key.equals("ADDRESS.PHONE")) return Optional.ofNullable(contact.getPhone());
        if (key.equals("ADDRESS.PHONE.PRE")) return Optional.ofNullable(getTelPrePost(contact.getPhone(), true));
        if (key.equals("ADDRESS.PHONE.POST")) return Optional.ofNullable(getTelPrePost(contact.getPhone(), false));
        if (key.equals("ADDRESS.FAX")) return Optional.ofNullable(contact.getFax());
        if (key.equals("ADDRESS.FAX.PRE")) return Optional.ofNullable(getTelPrePost(contact.getFax(), true));
        if (key.equals("ADDRESS.FAX.POST")) return Optional.ofNullable(getTelPrePost(contact.getFax(), false));
        if (key.equals("ADDRESS.MOBILE")) return Optional.ofNullable(contact.getMobile());
        if (key.equals("ADDRESS.MOBILE.PRE")) return Optional.ofNullable(getTelPrePost(contact.getMobile(), true));
        if (key.equals("ADDRESS.MOBILE.POST")) return Optional.ofNullable(getTelPrePost(contact.getMobile(), false));
        if (key.equals("ADDRESS.SUPPLIER.NUMBER")) return Optional.ofNullable(contact.getSupplierNumber());
        if (key.equals("ADDRESS.EMAIL")) return Optional.ofNullable(contact.getEmail());
        
        if (key.equals("ADDRESS.GLN")) return Optional.ofNullable(Optional.ofNullable(contact.getGln()).orElse(Long.valueOf(0)).toString());
        return Optional.empty();
    }
	
    /**
	 * Creates the {@link Payment} text according to the document infos. 
	 * 
     * @param document the {@link Document} which contains the {@link Payment} information
     * @param documentSummary the {@link DocumentSummary} for some additional information
     * @param percent the discount, if any
     * @return fully formatted {@link Payment} text
     */
    public String createPaymentText(Document document, DocumentSummary documentSummary, double percent) {
	    // String paymenttext = document.getPayment().getPaidText();
	    String paymenttext = document.getAdditionalInfo().getPaymentText();
	    if(paymenttext == null && document.getPayment() != null) {
	    	// try to get the default payment text from payment entry, if one exists
	    	paymenttext = BooleanUtils.toBoolean(document.getPaid()) ? document.getPayment().getPaidText() : document.getPayment().getUnpaidText();
	    }
	    paymenttext = StringUtils.replaceEach(paymenttext, new String[]{"<PAID.VALUE>", "<PAID.DATE>", "<DUE.DAYS>"}, 
	    		new String[]{
	    				numberFormatterService.DoubleToFormatedPriceRound(document.getPaidValue()), 
	    				dateFormatterService.getFormattedLocalizedDate(document.getPayDate()), 
	    				Integer.toString(document.getDueDays())});
	    LocalDateTime dueDate = DataUtils.getInstance().addToDate(document.getDocumentDate(), document.getDueDays());
	    paymenttext = StringUtils.replace(paymenttext, "<DUE.DATE>", dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
	    
	    paymenttext = StringUtils.replace(paymenttext, "<DUE.DISCOUNT.PERCENT>", numberFormatterService.DoubleToFormatedPercent(document.getPayment().getDiscountValue()));
	    paymenttext = StringUtils.replace(paymenttext, "<DUE.DISCOUNT.DAYS>", document.getPayment().getDiscountDays().toString());
	    paymenttext = StringUtils.replace(paymenttext, "<DUE.DISCOUNT.VALUE>", numberFormatterService.formatCurrency(documentSummary.getTotalGross().multiply(1 - percent)));
	    paymenttext = StringUtils.replace(paymenttext, "<DUE.DISCOUNT.DATE>", getDiscountDueDate(document));

// FIXME doesn't exist!	    paymenttext = StringUtils.replace(paymenttext, "<BANK.ACCOUNT.HOLDER>", preferences.getString("BANK_ACCOUNT_HOLDER"));
	    paymenttext = StringUtils.replace(paymenttext, "<BANK.ACCOUNT>", 
	    		preferences.getString("YOURCOMPANY_COMPANY_BANKACCOUNTNR"));
	    paymenttext = StringUtils.replace(paymenttext, "<BANK.IBAN>", 
	    		preferences.getString(Constants.PREFERENCES_YOURCOMPANY_IBAN));
	    paymenttext = StringUtils.replace(paymenttext, "<BANK.BIC>", 
	    		preferences.getString(Constants.PREFERENCES_YOURCOMPANY_BIC));
	    paymenttext = StringUtils.replace(paymenttext, "<BANK.NAME>", 
	    		preferences.getString(Constants.PREFERENCES_YOURCOMPANY_BANK));
	    paymenttext = StringUtils.replace(paymenttext, "<BANK.CODE>", 
	    		preferences.getString("YOURCOMPANY_COMPANY_BANKCODE"));
	    paymenttext = StringUtils.replace(paymenttext, "<YOURCOMPANY.CREDITORID>", 
	    		preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CREDITORID));
	    
	    // Additional placeholder for censored bank account
	    String censoredAccount = censorAccountNumber(preferences.getString("YOURCOMPANY_COMPANY_BANKACCOUNTNR"));
	    paymenttext = StringUtils.replace(paymenttext, "<BANK.ACCOUNT.CENSORED>", censoredAccount);
	    censoredAccount = censorAccountNumber(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_IBAN));
	    paymenttext = StringUtils.replace(paymenttext, "<BANK.IBAN.CENSORED>", censoredAccount);
	    
	    DocumentReceiver documentReceiver = addressManager.getBillingAdress(document);
        if(documentReceiver != null && documentReceiver.getOriginContactId() != null) {
        	Contact contact = contactsDAO.findById(documentReceiver.getOriginContactId());
        	if(contact != null && contact.getBankAccount() != null) {
	    	    // debitor's bank account
	    	    paymenttext = StringUtils.replace(paymenttext, "<DEBITOR.BANK.ACCOUNT.HOLDER>", 
	    	            contact.getBankAccount().getAccountHolder());
	    	    paymenttext = StringUtils.replace(paymenttext, "<DEBITOR.BANK.IBAN>", 
	    	            contact.getBankAccount().getIban());
	    	    paymenttext = StringUtils.replace(paymenttext, "<DEBITOR.BANK.BIC>", 
	    	            contact.getBankAccount().getBic());
	    	    paymenttext = StringUtils.replace(paymenttext, "<DEBITOR.BANK.NAME>", 
	    	            contact.getBankAccount().getBankName());
	    	    paymenttext = StringUtils.replace(paymenttext, "<DEBITOR.MANDATREF>", 
	    	            contact.getMandateReference());
	    	    // Additional placeholder for censored bank account
	    	    censoredAccount = censorAccountNumber(contact.getBankAccount().getIban());
	    	    paymenttext = StringUtils.replace(paymenttext, "<DEBITOR.BANK.IBAN.CENSORED>", censoredAccount);
        	}
	    }
	    
	    // placeholder for total sum
	    paymenttext = StringUtils.replace(paymenttext, "<DOCUMENT.TOTAL>", numberFormatterService.formatCurrency(documentSummary.getTotalGross()));
	    paymenttext = StringUtils.replace(paymenttext, "<DOCUMENT.NAME>", document.getName());
	    return paymenttext;
    }

	/**
	 * @param paymenttext
	 * @param bankAccountLength
	 * @return
	 */
	private String censorAccountNumber(String accountNumber) {
		String retval = "";
		if(accountNumber != null) {
			Integer bankAccountLength = accountNumber.length();			
			// Only set placeholder if bank account exists
			if( bankAccountLength > COUNT_OF_LAST_SHOWN_DIGITS ) {				
				// Show only the last COUNT_OF_LAST_SHOWN_DIGITS digits
				Integer bankAccountCensoredLength = bankAccountLength - COUNT_OF_LAST_SHOWN_DIGITS;
				retval = StringUtils.leftPad(accountNumber.substring(bankAccountCensoredLength), bankAccountCensoredLength, "*");
			} else {
				retval = "***";
			}
		}
		return retval;
	}
	
	public static void main(String[] args) {
		
	    TemplateProcessor ph = new TemplateProcessor();	    
//	    ph.extractPlaceholderName("$INONELINE:,$DOCUMENT.ADDRESS");
//	    ph.interpretParameters("$INONELINE:,$DOCUMENT.ADDRESS", "Erdrich\nTester\nFakestreet 22");
	    
//	    System.out.println("is 'DOCUMENT.ADDRESS' placeholder? " + ph.isPlaceholder("DOCUMENT.ADDRESS"));
//	    System.out.println("is 'NO.PLACEHOLDER' placeholder? " + ph.isPlaceholder("NO.PLACEHOLDER"));
//	    
//	    System.out.println("mit null: " + ph.censorAccountNumber(null));
//	    System.out.println("mit 12: " + ph.censorAccountNumber("12"));
//	    System.out.println("mit 123: " + ph.censorAccountNumber("123"));
//	    System.out.println("mit 123456789: " + ph.censorAccountNumber("123456789"));
//	    System.out.println("mit 9999999999999999999999999999999999: " + ph.censorAccountNumber("9999999999999999999999999999999999"));
	    
	    // test phone numbers
	    String[] testphones = new String[]{
	    		"02031/4775864",
	    		"+49 (0)2031/4775864",
	    		"020315 75864",
	    		"020315/75864",
	    		"+49 (0)20315 75864",
	    		"02031 4775864",
	    		"030/44775864",
	    		"030 44775864",
	    		"+49 (0)30 44775864",
	    		"03726 2824",
	    		"03726 781-0",
	    		"03726 781",
	    };
	    for (String phone : testphones) {
	    	System.out.println(String.format("PHONE: [%s]; PRE: [%s]; POST: [%s]", phone, ph.getTelPrePost(phone, true), ph.getTelPrePost(phone, false)));
		}
    }

	/**
	 * @param document
	 * @return
	 */
	private String getDiscountDueDate(Document document) {
		LocalDateTime date = LocalDateTime.ofInstant(document.getDocumentDate().toInstant(), ZoneId.systemDefault());
		date = date.plusDays(document.getPayment().getDiscountDays());
		// if another ULocale than the system's default ULocale should be used then we
		// have to write "DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(...))" here 
		return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
	}
	
	/**
	 * Test, if the name is in the list of all placeholders
	 * 
	 * @param testPlaceholder
	 * 		The placeholder to test
	 * @return
	 * 		TRUE, if the placeholder is in the list
	 */
	public boolean isPlaceholder(String testPlaceholder) {
		String placeholderName = extractPlaceholderName(testPlaceholder);
		
		// Test all placeholders
		return Arrays.stream(Placeholder.values()).anyMatch(p -> placeholderName.equals(p.getKey()));
	}
}

