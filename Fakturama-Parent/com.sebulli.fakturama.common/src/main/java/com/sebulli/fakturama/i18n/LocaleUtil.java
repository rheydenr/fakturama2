/**
 * Utility for handling different Locale and language codes
 */
package com.sebulli.fakturama.i18n;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;

/**
 * Utility class for handling {@link Locale}s. 
 */
public class LocaleUtil implements ILocaleService {
    
    private Locale currencyLocale = null;

    private final Map<String, Locale> countryLocaleMap = new HashMap<>();

    private Locale defaultLocale = Locale.getDefault();
    private SortedMap<String, String> localeCountryMap;
    private final Map<String, Locale> localeLookUp = new HashMap<>();
    
    /**
     * Returns a reference to the {@link LocaleUtil}. Used for initialization with a language code.
     * @param lang the language code to be used. If <code>null</code>, then "en_US" is used.
     * @return a {@link LocaleUtil} instance
     */
    @PostConstruct
	public void getInstance() {
    	String lang = (Activator.getContext() == null ? System.getProperty(EclipseStarter.PROP_NL) : 
            Activator.getContext().getProperty(EclipseStarter.PROP_NL));
    	
		if (lang == null) {
			initLocaleUtil("en_US");
		}
		// We have to track different language settings (e.g., from command line) which
		// aren't equal to the default locale.
		if (lang != null && !getDefaultLocale().getLanguage().contentEquals(lang)) {
			/*
			 * If a two-letter locale is given try to interpret it (because we need it later
			 * for determining currency etc.)
			 */
			if (lang.length() < 3) {
				List<Locale> countriesByLanguage = LocaleUtils.countriesByLanguage(lang);
				// try to get the locale from language, use the first fitting country
				if (!countriesByLanguage.isEmpty()) {
					Locale tmpLocale = countriesByLanguage.get(0);
					initLocaleUtil(String.format("%s_%s", tmpLocale.getCountry(), tmpLocale.getLanguage()));
				} else {
					// if none found, try to guess it from country code (very uncertain!)
					initLocaleUtil(String.format("%s_%s", lang, lang.toUpperCase()));
				}
			} else {
				initLocaleUtil(lang);
			}
		}
	}
    
    /**
     * constructor.
     * 
     */
    public LocaleUtil() {}
    
    /**
     * Private constructor initializes the Locale hashmap.
     * 
     * @param lang
     */
    private void initLocaleUtil(String lang) {
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
            if(locale != null && StringUtils.length(locale.getCountry()) > 0 && localeLookUp.get(locale.getCountry()) == null) {
                localeLookUp.put(locale.getCountry(), locale);
                countryLocaleMap.put(locale.getDisplayCountry(defaultLocale), locale);
//            System.out.println("Country Code = " + obj.getCountry() + ", Country Name = " + obj.getDisplayCountry(defaultLocale));
            }
        }
    }
        
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.i18n.ILocaleService#findCodeByDisplayCountry(java.lang.String)
	 */
    @Override
	public String findCodeByDisplayCountry(String country, String lang) {
    	// TODO use lang, but without interfering with other methods!
        Locale retval = countryLocaleMap.get(country);
        // hint: countryLocaleString.getDisplayCountry(defaultLocale) gives
        // the country as localized string
        return retval != null ? retval.getCountry() : null;
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.i18n.ILocaleService#findLocaleByDisplayCountry(java.lang.String)
	 */
    @Override
	public Optional<Locale> findLocaleByDisplayCountry(String country) {
         return Optional.ofNullable(countryLocaleMap.get(country));
    }

   /* (non-Javadoc)
 * @see com.sebulli.fakturama.i18n.ILocaleService#findByCode(java.lang.String)
 */
    @Override
	public Optional<Locale> findByCode(String code) {
        return Optional.ofNullable(localeLookUp.get(StringUtils.upperCase(code)));
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.i18n.ILocaleService#getDefaultLocale()
	 */
    @Override
	public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.i18n.ILocaleService#getCountryLocaleMap()
	 */
    @Override
	public Map<String, Locale> getCountryLocaleMap() {
        return countryLocaleMap;
    }

    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.i18n.ILocaleService#getLocaleCountryMap()
	 */
    @Override
	public Map<String, String> getLocaleCountryMap() {
        if(localeCountryMap == null) {
            Map<String, String> tmpMap = countryLocaleMap.entrySet().stream().collect(
                    Collectors.toMap((Entry<String, Locale> e) -> e.getValue().getCountry(), 
                                     (Entry<String, Locale> e) -> e.getKey()));
            ValueComparator bvc = new ValueComparator(tmpMap, defaultLocale);
            localeCountryMap = new TreeMap<>(bvc);
            localeCountryMap.putAll(tmpMap);
        }
        return localeCountryMap;
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.i18n.ILocaleService#getCurrencyLocale()
	 */
    @Override
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
    
    /**
     * Main method. For tests only.
     * 
     * @param args
     */
    public static void main(String[] args) {
//    	ILocaleService localeService = new LocaleUtil();
//        System.out.println(findCodeByDisplayCountry("Deutschland"));
//        Optional<Locale> code = findByCode("LT");
//        System.out.println(code);
//        System.out.println(code.get().getDisplayCountry(new Locale("lt")));
//
//        String testCountry = "Lietuva";
////        Locale lt = new Locale("lt");
//        System.out.println(Runtime.getRuntime().availableProcessors());
////
//        Optional<Locale> locale = LocaleUtil.getInstance().findLocaleByDisplayCountry(testCountry);
//        // if not found we try to find it in localized form
//        if (!locale.isPresent()) {
//            Locale[] availableLocales = Locale.getAvailableLocales();
//            long nanoTime = System.nanoTime();
//            for (Locale locale2 : availableLocales) {
////                if(StringUtils.isEmpty(locale2.getCountry())) continue;
//                locale = Arrays.stream(availableLocales)
//                        .filter(l -> l.getDisplayCountry(locale2).equalsIgnoreCase(testCountry))
//                        .findFirst();
//                if (locale.isPresent())
//                    break;
//            }
//            System.out.println("End: " + ((System.nanoTime() - nanoTime)/1_000_000)+ "ms");
//        }
//        if (locale.isPresent()) {
//            System.out.println(locale.get());
//        }
    }
}
