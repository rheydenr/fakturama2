/**
 * Utility for handling different Locale and language codes
 */
package com.sebulli.fakturama.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.common.Activator;

/**
 *
 */
public class LocaleUtil {
    public static final String PROP_NL = "osgi.nl"; 
    private static final Map<String, Locale> localeLookUp = new HashMap<>();
    private Locale defaultLocale = Locale.getDefault();

    private static LocaleUtil instance;
    
    /** Returns a reference to the {@link LocaleUtil}. */
    public static LocaleUtil getInstance() {
        if(instance == null) {
            instance = new LocaleUtil();
        }
        return instance;
    }
    
    private LocaleUtil() {
        String[] locales = Locale.getISOCountries();Locale.getDefault();
        String l = Activator.getContext() == null ? System.getProperty(PROP_NL) : Activator.getContext().getProperty(PROP_NL);
        if(!StringUtils.isEmpty(l)) {
            Locale b = new Locale.Builder().setLanguage(l).build();
            if(b != null) {
                defaultLocale = b;
            }
        }
        for (String countryCode : locales) {
            Locale obj = new Locale("", countryCode);
            localeLookUp.put(countryCode, obj);
            System.out.println("Country Code = " + obj.getCountry() + ", Country Name = " + obj.getDisplayCountry(defaultLocale));
        }
    }

    /**
     * Finds a (locale) string for a given country. The name is given back as specified in 
     * -nl environment.
     * 
     * @param country
     * @return
     */
    public static String findByName(String country) {
        Locale countryLocaleString = localeLookUp.get(country);
        return countryLocaleString.getDisplayCountry(Locale.GERMAN);
    }
    
    public static void main(String[] args) {
        LocaleUtil t = new LocaleUtil();
    }

}
