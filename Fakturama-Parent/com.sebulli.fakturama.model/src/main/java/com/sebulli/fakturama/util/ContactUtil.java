/**
 * 
 */
package com.sebulli.fakturama.util;

import java.util.Locale;
import java.util.Optional;
import java.util.SplittableRandom;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;

/**
 * Utility class for some additional useful methods for the {@link Contact}s.
 *
 */
@Singleton
public class ContactUtil {
    
    @Inject
    @Translation
    protected Messages msg;

    @Inject
	private IPreferenceStore eclipsePrefs;

	/**
     * the name of the company (if any) and the name of the contact
     * 
     * @param contact the {@link Contact}
     * @return the concatenated name of the company and the contact
     */
    public String getNameWithCompany(Contact contact) {
        String line = "";
        if (StringUtils.isNotBlank(contact.getCompany())) {
            line = DataUtils.getInstance().getSingleLine(contact.getCompany());
            if (StringUtils.isNotBlank(contact.getFirstName()) || 
                StringUtils.isNotBlank(contact.getName()) )
                line +=", ";
        }
        /*
         * If we have a manual entered address then that first line is relevant.
         * Therefore we've to skip the following in case we using a manual address
         * (which is determined by a missing customer number)
         */
        if(contact.getCustomerNumber() == null) {
            String manualAddress = contact.getAddress().getManualAddress();
            String[] splitted = manualAddress.split("\\n");
            line += splitted[0];
        } else {
            line += getFirstAndLastName(contact);
        }
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
     * Get the gender String
     * 
     * @return Gender as String
     */
    public String getGenderString(Contact contact) {
        return getGenderString(contact.getGender());
    }

    /**
     * Get the name with gender String
     * @return Gender and name as String
     */
    public String getNameWithGenderString(Contact contact) {
        String genderString = "";

        genderString = getGenderString(contact);
        if (!genderString.isEmpty())
            genderString+=" ";

        return genderString + contact.getName();
    }
    
    
    /**
     * Get the gender String by the gender number
     * 
     * @param i
     *            Gender number
     * @return Gender as string
     */
    public String getGenderString(int i) {
        return getGenderString(i, true);
    }


    /**
     * Get the gender String by the gender number
     * 
     * @param i
     *            Gender number
     * @param translate
     *            <code>true</code> if the string should be translated
     * @return Gender as string
     */
    public String getGenderString(int i, boolean translate) {
        switch (i) {
        case 0:
            return "---";
        case 1:
            return msg.contactFieldMrName;
        case 2:
            return msg.contactFieldMsName;
        case 3:
            return msg.commonFieldCompany;
        }
        return "";
    }

    /**
     * Get the gender number by the string
     * 
     * @param s
     *          Gender string
     * @return
     *          The number
     */
    public int getGenderID(String s) {
        // Test all strings
        for (int i = 0;i < 4 ; i++) {
            if (getGenderString(i,false).equalsIgnoreCase(s)) return i;
            if (getGenderString(i,true).equalsIgnoreCase(s)) return i;
        }
        // Default = "---"
        return 0;
    }

    /**
	 * Get the address
	 *
	 * @return Complete address
	 */
	public String getAddressAsString(Contact contact) {
		String addressFormat = "";
		String address = "";
		if(contact != null && (contact.getCustomerNumber() != null || contact.getAddress().getManualAddress() != null)) {
		    
		    if(contact.getAddress().getManualAddress() != null) {
		        // if a manual address is set we use this one
		        address = contact.getAddress().getManualAddress();
		    } else {
		        // else we build an address string from address fields
		        
        		// Get the format string
        		addressFormat = eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_ADDRESS);
        
        		// Hide the following countries
        		String hideCountriesString = eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES);
        		String[] hideCountries = hideCountriesString.split(",");
        		for (String hideCountry : hideCountries) {
        			if (StringUtils.defaultString(contact.getAddress().getCountryCode()).equalsIgnoreCase(hideCountry)) {
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
		    }
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
			if(address.getCountryCode() != null) {
    			Locale cLocale = new Locale.Builder().setRegion(address.getCountryCode()).build();
    			formatString = replaceAllWithSpace(formatString, "\\{country\\}", cLocale.getDisplayCountry());
			}
			
			String countrycode = StringUtils.defaultString(address.getCountryCode());
	
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
			greeting = eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MR);
			break;
		case 2:
			greeting = eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MS);
			break;
		case 3:
			greeting = eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY);
			break;
		default:
			greeting = eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMMON);
			break;
		}

		// Replace the placeholders
		greeting = replaceFormatString(greeting, contact);

		return greeting;
	}
	
    /**
     * Returns <code>true</code> if billing and delivery address are equal
     * 
     * @return
     *  <code>true</code>, if both are equal
     */
    public Boolean deliveryAddressEqualsBillingAddress(Document document) {
        String billingAddress = getAddressAsString(document.getBillingContact());
        String deliveryAddress = getAddressAsString(document.getDeliveryContact());

//        if (oldContact.getGender() != oldContact.getDeliveryGender()) { return false; }
//        if (!oldContact.getDeliveryTitle().equals(oldContact.getTitle())) { return false; }
//        if (!oldContact.getDeliveryFirstname().equals(oldContact.getFirstname())) { return false; }
//        if (!oldContact.getDeliveryName().equals(oldContact.getName())) { return false; }
//        if (!oldContact.getDeliveryCompany().equals(oldContact.getCompany())) { return false; }
//        if (!oldContact.getDeliveryStreet().equals(oldContact.getStreet())) { return false; }
//        if (!oldContact.getDeliveryZip().equals(oldContact.getZip())) { return false; }
//        if (!oldContact.getDeliveryCity().equals(oldContact.getCity())) { return false; }
//        if (!oldContact.getDeliveryCountry().equals(oldContact.getCountry())) { return false; }
//        
        return deliveryAddress.equalsIgnoreCase(billingAddress);
    }
	

	/**
	 * Return a common greeting string.
	 * 
	 * @return The greeting string
	 */
	public String getCommonGreeting() {

		// Get the common greeting string from the preference page.
		return eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMMON);
	}

	private String replaceAllWithSpace(final String s, final String exp, final String replacement) {
		String replacedString = StringUtils.isBlank(replacement) ? s.replaceAll(exp + " ", "") : s;
		return replacedString.replaceAll(exp, Optional.ofNullable(replacement).orElse(""));
	}

    /**
     * @param msg the msg to set
     */
    public final void setMsg(Messages msg) {
        this.msg = msg;
    }

    /**
     * @param eclipsePrefs the eclipsePrefs to set
     */
    public final void setEclipsePrefs(IPreferenceStore eclipsePrefs) {
        this.eclipsePrefs = eclipsePrefs;
    }
    
}
