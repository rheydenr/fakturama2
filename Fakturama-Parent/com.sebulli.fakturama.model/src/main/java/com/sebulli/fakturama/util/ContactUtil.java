/**
 * 
 */
package com.sebulli.fakturama.util;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;

/**
 * Utility class for some additional useful methods for the {@link Contact}s.
 *
 */
public class ContactUtil {
	
	private IEclipsePreferences eclipsePrefs;	
//	private IEclipsePreferences defaultPrefs;	

    /**
	 * @param eclipsePrefs
	 */
	public ContactUtil(IEclipsePreferences eclipsePrefs) {
		this.eclipsePrefs = eclipsePrefs;
//		defaultPrefs = eclipsePrefs.node("");
	}

	/**
     * the name of the company (if any) and the name of the contact
     * 
     * @param contact the {@link Contact}
     * @return the concatenated name of the company and the contact
     */
    public String getNameWithCompany(Contact contact) {
        String line = "";
        if (StringUtils.isNotBlank(contact.getCompany())) {
            line = DataUtils.getSingleLine(contact.getCompany());
            if (StringUtils.isNotBlank(contact.getFirstName()) || 
                StringUtils.isNotBlank(contact.getName()) )
                line +=", ";
        }

        line += getFirstAndLastName(contact);
        return line;
    }
        
    /**
     * Get the first and the last name
     * 
     * @return First and last name
     */
    public String getFirstAndLastName(Contact contact) {
        String line = "";
        if (StringUtils.isNotBlank(contact.getFirstName())) {
            line += contact.getFirstName();
        }
        
        if (StringUtils.isNotBlank(contact.getName())) {
            if (!line.isEmpty())
                line += " ";
            line += contact.getName();
        }

        return line;
    }
    
    
	/**
	 * Get the address
	 *
	 * @return Complete address
	 */
	public String getAddressAsString(Contact contact) {
		String addressFormat = "";
		String address = "";

		// Get the format string
		addressFormat = eclipsePrefs.get(Constants.PREFERENCES_CONTACT_FORMAT_ADDRESS, "");

		// Hide the following countries
		String hideCountriesString = eclipsePrefs.get(Constants.PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES, "");
		String[] hideCountries = hideCountriesString.split(",");
		for (String hideCountry : hideCountries) {
			if (contact.getAddress().getCountry().equalsIgnoreCase(hideCountry)) {
				addressFormat = replaceAllWithSpace(addressFormat, "\\{country\\}", "{removed}");
			}
		}

		// Get each line
		String[] addressFormatLines = addressFormat.split("<br>");
		for (String addressFormatLine : addressFormatLines) {
			String formatedAddressLine = replaceFormatString(addressFormatLine, contact);
			String trimmedAddressLine = formatedAddressLine.trim();

			if (formatedAddressLine.equals(addressFormatLine) || (!trimmedAddressLine.isEmpty())) {
				if (!address.isEmpty())
					address += "\n";
			}

			address += trimmedAddressLine;
		}

		// return the complete address
		return address;
	}
    

	/**
	 * Replaces the placeholders of a string with information from the given {@link Contact}.
	 * 
	 * @param formatString
	 *            The string with placeholders
	 * @param contact the given {@link Contact}           
	 * @return the formatted string.
	 */
	public String replaceFormatString(String formatString, Contact contact) {
		// Replace the placeholders
		formatString = replaceAllWithSpace(formatString, "\\{company\\}", contact.getCompany());
		formatString = replaceAllWithSpace(formatString, "\\{title\\}", contact.getTitle());
		formatString = replaceAllWithSpace(formatString, "\\{firstname\\}", contact.getFirstName());
		formatString = replaceAllWithSpace(formatString, "\\{lastname\\}", contact.getName());
		
		Address address = contact.getAddress();
		if(address != null) {
			formatString = replaceAllWithSpace(formatString, "\\{street\\}", address.getStreet());
			formatString = replaceAllWithSpace(formatString, "\\{zip\\}", address.getZip());
			formatString = replaceAllWithSpace(formatString, "\\{city\\}", address.getCity());
			
			// determine the country from country code
			Locale cLocale = new Locale.Builder().setRegion(address.getCountry()).build();
			formatString = replaceAllWithSpace(formatString, "\\{country\\}", cLocale.getDisplayCountry());
	
			String countrycode = address.getCountry();
	
			if (!countrycode.isEmpty()) {
				countrycode += "-";
			}
			formatString = replaceAllWithSpace(formatString, "\\{countrycode\\}", countrycode);
		}

		formatString = replaceAllWithSpace(formatString, "\\{removed\\}", "");

		return formatString;
	}

	/**
	 * Generate the greeting string, depending on the gender
	 * 
	 * @param useDelivery
	 *            TRUE, if the delivery address should be used
	 * @return The greeting string
	 */
	public String getGreeting(Contact contact) {
		String greeting = "";
		int gender;

		// Use the gender dependent preference settings
		gender = contact.getGender();
		switch (gender) {
		case 1:
			greeting = eclipsePrefs.get(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MR, "");
			break;
		case 2:
			greeting = eclipsePrefs.get(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MS, "");
			break;
		case 3:
			greeting = eclipsePrefs.get(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY, "");
			break;
		default:
			greeting = eclipsePrefs.get(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMMON, "");
			break;
		}

		// Replace the placeholders
		greeting = replaceFormatString(greeting, contact);

		return greeting;
	}

	/**
	 * Return a common greeting string.
	 * 
	 * @return The greeting string
	 */
	public String getCommonGreeting() {

		// Get the common greeting string from the preference page.
		return eclipsePrefs.get(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMMON, "");
	}

	private String replaceAllWithSpace(String s, String exp, String replacement) {
		String replacedString;
		if (replacement.isEmpty())
			replacedString = s.replaceAll(exp + " ", "");
		else
			replacedString = s;
		replacedString = replacedString.replaceAll(exp, replacement);
		return replacedString;
	}
    
}
