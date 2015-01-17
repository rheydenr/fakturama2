/**
 * Utility for handling different Locale and language codes
 */
package com.sebulli.fakturama.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.misc.Constants;

/**
 *
 */
public class LocaleUtil {
    public static final String PROP_NL = "osgi.nl"; 
    private static final Map<String, Locale> localeLookUp = new HashMap<>();
    private static final Map<String, Locale> countryLocaleMap = new HashMap<>();
    private Locale defaultLocale = Locale.getDefault();

    private static LocaleUtil instance;
    private SortedMap<String, String> localeCountryMap;
    
    /** Returns a reference to the {@link LocaleUtil}. */
    public static LocaleUtil getInstance() {
        return getInstance(Activator.getContext() == null ? System.getProperty(PROP_NL) : Activator.getContext().getProperty(PROP_NL));
    }
    
    /**
     * Returns a reference to the {@link LocaleUtil}. Used for initialization with a language code.
     * @param lang the language code to be used
     * @return a {@link LocaleUtil} instance
     */
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
    
    /**
     * The default {@link Locale} currently in use.
     * @return
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * @return the countryLocaleMap
     */
    public Map<String, Locale> getCountryLocaleMap() {
        return countryLocaleMap;
    }

    public Map<String, String> getLocaleCountryMap() {
        if(localeCountryMap == null) {
            Map<String, String> tmpMap = new HashMap<String, String>();
            for (Entry<String, Locale> entry : countryLocaleMap.entrySet()) {
                tmpMap.put(entry.getValue().getCountry(), entry.getKey());
            }
            ValueComparator bvc = new ValueComparator(tmpMap);
            localeCountryMap = new TreeMap<>(bvc);
            localeCountryMap.putAll(tmpMap);
        }
        return localeCountryMap;
    }
    
    public Locale getCurrencyLocale() {
        String localeString = Activator.getPreferences().get(Constants.PREFERENCE_CURRENCY_LOCALE, "en/US");
        Pattern pattern = Pattern.compile("(\\w{2})/(\\w{2})");
        Locale currencyLocale = getDefaultLocale();
        Matcher matcher = pattern.matcher(localeString);
        if (matcher.matches() && matcher.groupCount() > 1) {
            String s = matcher.group(1);
            String s2 = matcher.group(2);
            currencyLocale = new Locale(s, s2);
        }
        return currencyLocale;
        
    }
}
