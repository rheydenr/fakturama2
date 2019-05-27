/**
 * 
 */
package com.sebulli.fakturama.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ibm.icu.util.ULocale;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.ReliabilityType;

/**
 * Utility class for some additional useful methods for the {@link Contact}s.
 *
 */
@Singleton
public class ContactUtil {
	@Inject
	private ILocaleService localeUtil;
    
	/**
	 * Address key for the country field.
	 */
    public static final String KEY_COUNTY = "county";

	/**
	 * Address key for the city field.
	 */
	public static final String KEY_CITY = "city";

	/**
	 * Address key for the zip code field.
	 */
	public static final String KEY_ZIP = "zip";

	/**
	 * Address key for the street number field.
	 */
	public static final String KEY_STREETNO = "streetno";

	/**
	 * Address key for the street with street number field.
	 */
	public static final String KEY_STREETNAME = "streetname";

	/**
	 * Address key for the street field.
	 */
	public static final String KEY_STREET = "street";

	/**
	 * Address key for the first address line field.
	 */
	public static final String KEY_ADDRESSFIRSTLINE = "addressfirstline";

	/**
	 * Address key for the last name field.
	 */
	public static final String KEY_LASTNAME = "lastname";

	/**
	 * Address key for the first name field.
	 */
	public static final String KEY_FIRSTNAME = "firstname";

	/**
	 * Address key for the complete name field.
	 */
	public static final String KEY_NAME = "name";

	/**
	 * Maximal count of salutations.
	 */

    public static final int MAX_SALUTATION_COUNT = 4;

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
        if(contact.getCustomerNumber() == null && contact.getAddress() != null && contact.getAddress().getManualAddress() != null) {
            String manualAddress = contact.getAddress().getManualAddress();
            String[] splitted = manualAddress.split("\\n");
            line += StringUtils.chomp(splitted[0]);
        } else {
            line += getFirstAndLastName(contact);
        }
        return line;
    }
    
    public String getCompanyOrLastname(Contact contact) {
        String line = "";
        if (StringUtils.isNotBlank(contact.getCompany())) {
            line = DataUtils.getInstance().getSingleLine(contact.getCompany());
        } else if (StringUtils.isNotBlank(contact.getName())) {
            line = contact.getName();
        } else if(contact.getAddress() != null && contact.getAddress().getManualAddress() != null) {
        	line = getDataFromAddressField(contact.getAddress().getManualAddress(), KEY_NAME);
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
        return getSalutationString(contact.getGender());
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
     * Returns an "ID" based upon the gender String.<br/>
     * Hereby means<br />
     * <pre>
     * m ... 1
     * f ... 2
     * undefined ... 0
     * </pre>
     * @param genderString String to convert ("m" or "f")
     * @return a (virtual) ID for the gender
     */
    public Integer getGenderIdFromString(String genderString) {
    	Integer retval = Integer.valueOf(0);
		if (genderString.equals("m"))
			retval = Integer.valueOf(1);
		if (genderString.equals("f"))
			retval = Integer.valueOf(2);
		return retval;
    }
    
    
    /**
     * Get the gender String by the gender number
     * 
     * @param i
     *            Gender number
     * @return Gender as string
     */
    public String getSalutationString(int i) {
        return getSalutationString(i, true);
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
    public String getSalutationString(int i, boolean translate) {
    	// TODO: Check why it is not an enum (why Integers are stored?)
        switch (i) {
        case 0:
            return "---";
        case 1:
            return msg.contactFieldMrName;
        case 2:
            return msg.contactFieldMsName;
        case 3:
            return msg.commonFieldCompany;
        case 4:
        	return msg.contactFieldFamilyName;
    	default:
        	return "";
        }
    }

    /**
     * Get the gender number by the string
     * 
     * @param s
     *          Gender string
     * @return
     *          The number
     */
	public int getSalutationID(String s) {
		// Test all strings
		for (int i = 0; i <= MAX_SALUTATION_COUNT; i++) {
			if (getSalutationString(i, false).equalsIgnoreCase(s))
				return i;
			// if (getGenderString(i,true).equalsIgnoreCase(s)) return i;
		}
		// Default = "---"
		return 0;
	}
    
    /**
     * Get the address as one String.
     * 
	 * @param contact the {@link Contact} to use
	 * @return Complete address
     */
	public String getAddressAsString(Contact contact) {
		return getAddressAsString(contact, "\n");
	}

    /**
	 * Get the address as one String. Use a specified separator.
	 *
	 * @param contact the {@link Contact} to use
	 * @param separator the separator
	 * 
	 * @return Complete address
	 */
	public String getAddressAsString(Contact contact, String separator) {
		String addressFormat = "";
		String address = "";
		if(contact != null && contact.getAddress() != null) {
		    
		    if(contact.getAddress().getManualAddress() != null) {
		        // if a manual address is set we use it
		        address = contact.getAddress().getManualAddress();
		    } else {
		        // else we build an address string from address fields
		    	
        		// Get the format string
        		addressFormat = eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_ADDRESS);
        		
        		// Hide the following countries
        		String hideCountriesString = eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES);
        		String[] hideCountries = hideCountriesString.split(",");
        		for (String hideCountry : hideCountries) {
        			String hiddenCountry = "";
        			if(hideCountry.length() <= 3) {
	        			Optional<ULocale> hiddenLocale = localeUtil.findByCode(hideCountry);
						//if(hiddenLocale.isPresent()) {
							hiddenCountry = hiddenLocale.orElse(ULocale.US).getISO3Country();
						//}
        			}
        			if (contact.getAddress() != null && (StringUtils.equalsIgnoreCase(contact.getAddress().getCountryCode(), hideCountry)
        			|| StringUtils.equalsIgnoreCase(localeUtil.findByCode(contact.getAddress().getCountryCode()).orElse(ULocale.US).getISO3Country(), hiddenCountry))) {
        				addressFormat = replaceAllWithSpace(addressFormat, "\\{country\\}", "{removed}");
        				addressFormat = replaceAllWithSpace(addressFormat, "\\{countrycode\\}", "{removed}");
        			}
        		}
        
        		// Get each line
        		String[] addressFormatLines = addressFormat.split("<br>");
        		for (String addressFormatLine : addressFormatLines) {
        			String formatedAddressLine = replaceFormatString(addressFormatLine, contact);
        			String trimmedAddressLine = formatedAddressLine.trim();
        			if ((formatedAddressLine.equals(addressFormatLine) || !trimmedAddressLine.isEmpty()) 
        					&& !address.isEmpty()) {
						address += separator;
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
		retval.setStreet(getDataFromAddressField(address, KEY_STREET));
		retval.setCity(getDataFromAddressField(address, KEY_CITY));
		retval.setZip(getDataFromAddressField(address, KEY_ZIP));
		String country = getDataFromAddressField(address, KEY_COUNTY);
		Optional<ULocale> locale = determineCountryCode(country);
		if(locale.isPresent() && StringUtils.isNotBlank(locale.get().getCountry())) {
			retval.setCountryCode(locale.get().getCountry());
		}
		
		// if all fields are empty we must not create a new address object
		if(retval.getStreet().isEmpty() && retval.getCity().isEmpty() && retval.getZip().isEmpty()) {
			retval = null;
		}
		return retval;
	}
    

	/**
	 * Try to determine the ULocale from a given country string.
	 * 
	 * @param country the country string to look up
	 * @return a {@link ULocale} or an empty Optional, if not found
	 */
	public Optional<ULocale> determineCountryCode(String country) {
		/*
		 * Since the country may be given as localized string (e.g., "Deutschland") or as non-localized string (e.g., "Germany"),
		 * we have to look up the whole Locales
		 * But wait... Sometimes the country is given as code only (e.g., "DE" or even "DEU"). That has to be respected, too.  
		 */
		Optional<ULocale> locale;
		if(StringUtils.length(country) > 3) {
			locale = localeUtil.findLocaleByDisplayCountry(country);
		} else {
			locale = StringUtils.isEmpty(country) ? Optional.of(localeUtil.getDefaultLocale()) : localeUtil.findByCode(country);
		}
		// if not found we try to find it in localized form
		if (!locale.isPresent()) {
		    ULocale[] availableLocales = ULocale.getAvailableLocales();
		    for (ULocale locale2 : availableLocales) {
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
								addressCity = line.length() > 5 && line.length() > matcher.end() ? line.substring(matcher.end()+1).trim() : "";
								
							}
							cityFound = addressCity.length() > 0;
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

		if (key.equals(KEY_NAME)) return addressName;
		if (key.equals(KEY_FIRSTNAME)) return addressFirstName;
		if (key.equals(KEY_LASTNAME)) return addressLastName;
		if (key.equals(KEY_ADDRESSFIRSTLINE)) return addressLine;
		if (key.equals(KEY_STREET)) return addressStreet;
		if (key.equals(KEY_STREETNAME)) return getStreetName(addressStreet);
		if (key.equals(KEY_STREETNO)) return getStreetNo(addressStreet);
		if (key.equals(KEY_ZIP)) return addressZIP;
		if (key.equals(KEY_CITY)) return addressCity;
		if (key.equals(KEY_COUNTY)) return addressCountry;
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
			return "";
		}
		// Replace the placeholders
		formatString = replaceAllWithSpace(formatString, "\\{company\\}", contact.getCompany());
		formatString = replaceAllWithSpace(formatString, "\\{gender\\}", getGenderString(contact).replaceAll("---", ""));
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
    			ULocale cLocale = new ULocale.Builder().setRegion(address.getCountryCode()).build();
    			formatString = replaceAllWithSpace(formatString, "\\{country\\}", cLocale.getDisplayCountry());
			} else {
    			formatString = replaceAllWithSpace(formatString, "\\{country\\}", "");
			}
			
			String countrycode = StringUtils.defaultString(address.getCountryCode());
	
			if (!countrycode.isEmpty() && !StringUtils.containsAny(countrycode, eclipsePrefs.getString(Constants.PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES).split(","))) {
				countrycode += "-";
			}
			formatString = replaceAllWithSpace(formatString, "\\{countrycode\\}", countrycode);
		} else {
			formatString = formatString.replaceAll("\\{street\\}|\\{zip\\}|\\{city\\}|\\{country\\}|\\{countrycode\\}", "");
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
	 * Get the reliability String by the number
	 * 
	 * @param i
	 *            Gender number
	 * @return Gender as string
	 */
	public String getReliabilityString(ReliabilityType type) {
		return getReliabilityString(type, true);
	}
	
	/**
	 * Get the reliability String by the number
	 * 
	 * @param type
	 *            Gender number
	 * @param translate
	 *            TRUE, if the string should be translated
	 * @return Gender as string
	 */
	public String getReliabilityString(ReliabilityType type, boolean translate) {
		switch (type) {
		case NONE:
			return "---";
		case POOR:
			//T: Reliability
			return msg.contactFieldReliabilityPoorName;
		case MEDIUM:
			//T: Reliability
			return msg.contactFieldReliabilityMediumName;
		case GOOD:
			//T: Reliability
			return msg.contactFieldReliabilityGoodName;
		}
		return "";
	}
	
	///**
	// * Get the reliability number by the string
	// * 
	// * @param s
	// *          Reliability string
	// * @return
	// * 			The number
	// */
	//public int getReliabilityID(String s) {
	//	// Test all strings
	//	for (int i = 0;i < 4 ; i++) {
	//		if (getReliabilityString(i,false).equalsIgnoreCase(s)) return i;
	//		if (getReliabilityString(i,true).equalsIgnoreCase(s)) return i;
	//	}
	//	// Default = "---"
	//	return 0;
	//}

    /**
     * Returns <code>true</code> if billing and delivery address are equal
     * 
     * @return
     *  <code>true</code>, if both are equal
     */
    public Boolean deliveryAddressEqualsBillingAddress(Document document) {
        String billingAddress = getAddressAsString(document.getBillingContact());
        String deliveryAddress = getAddressAsString(document.getDeliveryContact() != null 
        		? document.getDeliveryContact() 
        		: (document.getBillingContact() != null && document.getBillingContact().getAlternateContacts() != null) 
        		   ? document.getBillingContact().getAlternateContacts() 
        		   : document.getBillingContact());

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
        return deliveryAddress.isEmpty() || deliveryAddress.equalsIgnoreCase(billingAddress);
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
		// remove \r character because else multi-lined addresses aren't shown correctly
		return replacedString.replaceAll(exp, Optional.ofNullable(Matcher.quoteReplacement(StringUtils.remove(StringUtils.defaultString(replacement), "\r"))).orElse(""));
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
