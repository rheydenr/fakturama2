package com.sebulli.fakturama.i18n;

import java.util.Map;
import java.util.Optional;

import com.ibm.icu.util.ULocale;

public interface ILocaleService {

	/**
	 * Finds a {@link ULocale} by its display name. E.g., 
	 * @param country
	 * @return
	 */
	String findCodeByDisplayCountry(String country, String lang);

	/**
	 * Finds a {@link ULocale} by its display country name.
	 * 
	 * @param country
	 * @return
	 */
	Optional<ULocale> findLocaleByDisplayCountry(String country);

	/**
	 * Finds a {@link ULocale} by country code.
	 * 
	 * @param code Country code (e.g., "DE" for German {@link ULocale})
	 * @return the {@link ULocale} for this code
	 */
	Optional<ULocale> findByCode(String code);

	/**
	 * The default {@link ULocale} currently in use.
	 * @return
	 */
	ULocale getDefaultLocale();

	/**
	 * Contains all {@link ULocale}s with its accompanying localized country names.
	 * 
	 * I.e., Kroatien=hr_HR etc.
	 * 
	 * @return the countryLocaleMap
	 */
	Map<String, ULocale> getCountryLocaleMap();

	/**
	 * Contains a {@link Map} with all country abbreviations and the according {@link ULocale}.
	 * @return {@link Map} with all country abbreviations
	 */
	Map<String, String> getLocaleCountryMap();

	/**
	 * The {@link ULocale} for the currently selected currency. If no currency locale is found in preferences, 
	 * the default locale is returned. Note that the locales in preferences are stored with {@link ULocale#US}.
	 * @return {@link ULocale} for the currently selected currency
	 */
	ULocale getCurrencyLocale();

}