/**
 * Utility for handling different Locale and language codes
 */
package com.sebulli.fakturama.i18n;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.adaptor.EclipseStarter;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;

/**
 *
 */
public class LocaleUtil {
    
    private static LocaleUtil instance;
    private static Locale currencyLocale = null;

    private static final Map<String, Locale> countryLocaleMap = new HashMap<>();

    private Locale defaultLocale = Locale.getDefault();
    private SortedMap<String, String> localeCountryMap;
    private static final Map<String, Locale> localeLookUp = new HashMap<>();

    /** Returns a reference to the {@link LocaleUtil}. */
    public static LocaleUtil getInstance() {
        return getInstance(Activator.getContext() == null ? System.getProperty(EclipseStarter.PROP_NL) : 
            Activator.getContext().getProperty(EclipseStarter.PROP_NL));
    }
    
    /**
     * Returns a reference to the {@link LocaleUtil}. Used for initialization with a language code.
     * @param lang the language code to be used
     * @return a {@link LocaleUtil} instance
     */
    public static LocaleUtil getInstance(String lang) {
        if(instance == null || !instance.getDefaultLocale().getLanguage().contentEquals(lang)) {
            instance = new LocaleUtil(lang);
        }
        return instance;
    }
    
    /**
     * hidden constructor.
     * 
     */
    private LocaleUtil() {}
    
    /**
     * Private constructor initializes the Locale hashmap.
     * 
     * @param lang
     */
    private LocaleUtil(String lang) {
        Locale[] availableLocales = Locale.getAvailableLocales();
        // only countries are relevant
//        String[] locales = Locale.getISOCountries();
        if(!StringUtils.isEmpty(lang)) {
            // the language code are the letters before "_"
            String splittedString[] = lang.split("_");
            Builder builder = new Locale.Builder()
                .setLanguage(splittedString[0]);
            if(splittedString.length > 1) {
                builder.setRegion(splittedString[1]);
            }
            Locale b = builder.build();
            if(b != null) {
                defaultLocale = b;
            }
        }
        
//        for (String countryCode : locales) {
//            Locale obj = new Locale("", countryCode);
//            localeLookUp.put(countryCode, obj);
//            countryLocaleMap.put(obj.getDisplayCountry(defaultLocale), obj);
//        }
        // fill some helper maps
        for (Locale locale : availableLocales) {
            if(locale != null && StringUtils.length(locale.getCountry()) > 0) {
                localeLookUp.put(locale.getCountry(), locale);
                countryLocaleMap.put(locale.getDisplayCountry(defaultLocale), locale);
//            System.out.println("Country Code = " + obj.getCountry() + ", Country Name = " + obj.getDisplayCountry(defaultLocale));
            }
        }
    }
    
    /**
     * <p>Refreshes the settings of this class.</p><p>
     * <i>Caution:</i> This method makes the {@link DataUtils} class not thread safe, anymore.
     * If multiple threads are try to refresh this class the state becomes indeterminable.
     */
    public static void refresh() {
        instance = null;
        currencyLocale = null;
    }
    
    /**
     * Finds a {@link Locale} by its display name. E.g., 
     * @param country
     * @return
     */
    public String findCodeByDisplayCountry(String country) {
        Locale retval = countryLocaleMap.get(country);
        // hint: countryLocaleString.getDisplayCountry(defaultLocale) gives
        // the country as localized string
        return retval != null ? retval.getCountry() : null;
    }
    
    /**
     * Finds a {@link Locale} by its display country name.
     * 
     * @param country
     * @return
     */
    public Optional<Locale> findLocaleByDisplayCountry(String country) {
         return Optional.ofNullable(countryLocaleMap.get(country));
    }

   /**
     * Finds a {@link Locale} by country code.
     * 
     * @param code Country code (e.g., "DE" for German {@link Locale})
     * @return the {@link Locale} for this code
     */
    public Optional<Locale> findByCode(String code) {
        return Optional.ofNullable(localeLookUp.get(StringUtils.upperCase(code)));
    }
    
    /**
     * The default {@link Locale} currently in use.
     * @return
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Contains all {@link Locale}s with its accompanying localized country names.
     * 
     * I.e., Kroatien=hr_HR etc.
     * 
     * @return the countryLocaleMap
     */
    public Map<String, Locale> getCountryLocaleMap() {
        return countryLocaleMap;
    }

    public Map<String, String> getLocaleCountryMap() {
        if(localeCountryMap == null) {
            Map<String, String> tmpMap =  countryLocaleMap.entrySet().stream().collect(
                    Collectors.toMap((Entry<String, Locale> e) -> e.getValue().getCountry(), 
                                     (Entry<String, Locale> e) -> e.getKey()));
            ValueComparator bvc = new ValueComparator(tmpMap);
            localeCountryMap = new TreeMap<>(bvc);
            localeCountryMap.putAll(tmpMap);
        }
        return localeCountryMap;
    }
    
    public Locale getCurrencyLocale() {
        if(currencyLocale == null) {
            String localeString = Activator.getPreferences().get(Constants.PREFERENCE_CURRENCY_LOCALE, Locale.US.getDisplayCountry());
            Pattern pattern = Pattern.compile("(\\w{2})/(\\w{2})");
            Matcher matcher = pattern.matcher(localeString);
            if (matcher.matches() && matcher.groupCount() > 1) {
                String s = matcher.group(1);
                String s2 = matcher.group(2);
                currencyLocale = new Locale(s, s2);
            } else {
                currencyLocale = getDefaultLocale();
            }
        }
        return currencyLocale;
    }
    
    public static void main(String[] args) {
        System.out.println(getInstance().findCodeByDisplayCountry("Deutschland"));
        Optional<Locale> code = getInstance().findByCode("LT");
        System.out.println(code);
        System.out.println(code.get().getDisplayCountry(new Locale("lt")));

        String testCountry = "Lietuva";
//        Locale lt = new Locale("lt");
        System.out.println(Runtime.getRuntime().availableProcessors());
//
        Optional<Locale> locale = LocaleUtil.getInstance().findLocaleByDisplayCountry(testCountry);
        // if not found we try to find it in localized form
        if (!locale.isPresent()) {
            Locale[] availableLocales = Locale.getAvailableLocales();
            long nanoTime = System.nanoTime();
            for (Locale locale2 : availableLocales) {
//                if(StringUtils.isEmpty(locale2.getCountry())) continue;
                locale = Arrays.stream(availableLocales)
                        .filter(l -> l.getDisplayCountry(locale2).equalsIgnoreCase(testCountry))
                        .findFirst();
                if (locale.isPresent())
                    break;
            }
            System.out.println("End: " + ((System.nanoTime() - nanoTime)/1_000_000)+ "ms");
        }
        if (locale.isPresent()) {
            System.out.println(locale.get());
        }

    }
}
