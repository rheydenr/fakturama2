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
    private static final Map<String, Locale> countryLocaleMap = new HashMap<>();
    private Locale defaultLocale = Locale.getDefault();

    private static LocaleUtil instance;
    
    /** Returns a reference to the {@link LocaleUtil}. */
    public static LocaleUtil getInstance() {
        return getInstance(Activator.getContext() == null ? System.getProperty(PROP_NL) : Activator.getContext().getProperty(PROP_NL));
    }
    
    public static LocaleUtil getInstance(String lang) {
        if(instance == null) {
            instance = new LocaleUtil(lang);
        }
        return instance;
    }
    
    /**
     * hidden constructor.
     * 
     */
    private LocaleUtil() {}
    
    private LocaleUtil(String lang) {
        String[] locales = Locale.getISOCountries();
        if(!StringUtils.isEmpty(lang)) {
            // the language code are the letters before "_"
            lang = StringUtils.substringBefore(lang, "_");
            Locale b = new Locale.Builder().setLanguage(lang).build();
            if(b != null) {
                defaultLocale = b;
            }
        }
        for (String countryCode : locales) {
            Locale obj = new Locale("", countryCode);
            localeLookUp.put(countryCode, obj);
            countryLocaleMap.put(obj.getDisplayCountry(defaultLocale), obj);
//            System.out.println("Country Code = " + obj.getCountry() + ", Country Name = " + obj.getDisplayCountry(defaultLocale));
        }
    }
    
    public String findCodeByDisplayCountry(String country) {
        Locale retval = countryLocaleMap.get(country);
        return retval != null ? retval.getCountry() : null;
    }

    /**
     * Finds a (locale) string for a given country. The name is given back as specified in 
     * -nl environment.
     * 
     * @param country
     * @return
     */
    public String findByName(String country) {
        Locale countryLocaleString = localeLookUp.get(country);
        return countryLocaleString != null ? countryLocaleString.getDisplayCountry(defaultLocale) : null;
    }
    
    public Locale getDefaultLocale() {
        return defaultLocale;
    }
    
//    public static void main(String[] args) {
//        LocaleUtil t = new LocaleUtil();
//    }
//
}
