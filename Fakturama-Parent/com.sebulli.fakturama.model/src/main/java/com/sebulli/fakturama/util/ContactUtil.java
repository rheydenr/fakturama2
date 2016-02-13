/**
 * 
 */
package com.sebulli.fakturama.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;

/**
 * Utility class for some additional useful methods for the {@link Contact}s.
 *
 */
@Singleton
public class ContactUtil {
    
    @Inject
    @Translation
    protected Messages msg;

    @Inject @org.eclipse.e4.core.di.annotations.Optional
	private IPreferenceStore eclipsePrefs;
    
    private FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;

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
         * If we have a manually entered address then that first line is relevant.
         * Therefore we've to skip the following in case we use a manual address
         * (which is determined by a missing customer number).
         */
        if(contact.getCustomerNumber() == null && contact.getAddress().getManualAddress() != null) {
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
		if(contact != null && (/*contact.getCustomerNumber() != null ||*/ contact.getAddress() != null)) {
		    
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
	
	public Address createAddressFromString(String address) {
		Address retval = modelFactory.createAddress();
		retval.setStreet(getDataFromAddressField(address, "street"));
		retval.setCity(getDataFromAddressField(address, "city"));
		retval.setZip(getDataFromAddressField(address, "zip"));
		String country = getDataFromAddressField(address, "county");
		Optional<Locale> locale = determineCountryCode(country);
		if(locale.isPresent() && StringUtils.isNotBlank(locale.get().getCountry())) {
			retval.setCountryCode(locale.get().getCountry());
		}
		
		// if all fields are empty we must not create a new address object
		if(retval.getStreet().isEmpty() && retval.getCity().isEmpty() && retval.getZip().isEmpty()) {
			retval = null;
		}
		return retval;
	}
    

	public Optional<Locale> determineCountryCode(String country) {
		/*
		 * Since the country may be given as localized string (e.g., "Deutschland") or as non-localized string (e.g., "Germany"),
		 * we have to look up the whole Locales  
		 */
		Optional<Locale> locale = StringUtils.isEmpty(country) ? Optional.of(LocaleUtil.getInstance().getDefaultLocale()) : LocaleUtil.getInstance().findLocaleByDisplayCountry(country);
		// if not found we try to find it in localized form
		if (!locale.isPresent()) {
		    Locale[] availableLocales = Locale.getAvailableLocales();
		    for (Locale locale2 : availableLocales) {
		        // don't try to make it parallel() because then it takes longer than a single stream!
		        locale = Arrays.stream(availableLocales)
		                .filter(l -> l.getDisplayCountry(locale2).equalsIgnoreCase(country))
		                .findFirst();
		        if (locale.isPresent())
		            break;
		    }
		}
		return locale;
	}

	/**
	 * Extracts an address from a String value (if you entered an address in the text field
	 * without using a {@link Contact}).
	 * 
	 * @param address the address as String (separated by '\n')
	 * @param key which part of Address should be extracted
	 * @return
	 */
	public String getDataFromAddressField(String address, String key) {
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
								addressCity = line.length() > 5 ? line.substring(matcher.end()+1).trim() : "";
								
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
	 * Returns the first name of a complete name
	 * 
	 * @param name
	 * 		First name and last name
	 * @return
	 * 		Only the first name
	 */
	public String getFirstName (String name) {
		String s = StringUtils.defaultString(name).trim();
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
	public String getLastName (String name) {
		String s = StringUtils.defaultString(name).trim();
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
	public String getStreetName (String streetWithNo) {
		if(streetWithNo == null) {
			return "";
		}
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
	public String getStreetNo (String streetWithNo) {
		if(streetWithNo == null) {
			return "";
		}
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
	 * Replaces the placeholders of a string with information from the given {@link Contact}.
	 * 
	 * @param formatString
	 *            The string with placeholders
	 * @param contact the given {@link Contact}           
	 * @return the formatted string.
	 */
	public String replaceFormatString(String formatString, Contact contact) {
		if(contact == null) {
			return formatString;
		}
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
			} else {
    			formatString = replaceAllWithSpace(formatString, "\\{country\\}", "");
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
