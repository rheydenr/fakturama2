package com.sebulli.fakturama.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.money.CurrencyUnit;

public interface ILocaleService {

	/**
	 * Finds a {@link Locale} by its display name. E.g., 
	 * @param country
	 * @return
	 */
	String findCodeByDisplayCountry(String country, String lang);

	/**
	 * Finds a {@link Locale} by its display country name.
	 * 
	 * @param country
	 * @return
	 */
	Optional<Locale> findLocaleByDisplayCountry(String country);

	/**
	 * Finds a {@link Locale} by country code.
	 * 
	 * @param code Country code (e.g., "DE" for German {@link Locale})
	 * @return the {@link Locale} for this code
	 */
	Optional<Locale> findByCode(String code);

	/**
	 * The default {@link Locale} currently in use.
	 * @return
	 */
	Locale getDefaultLocale();

	/**
	 * Contains all {@link Locale}s with its accompanying localized country names.
	 * 
	 * I.e., Kroatien=hr_HR etc.
	 * 
	 * @return the countryLocaleMap
	 */
	Map<String, Locale> getCountryLocaleMap();

	/**
	 * Contains a {@link Map} with all country abbreviations and the according {@link Locale}.
	 * @return {@link Map} with all country abbreviations
	 */
	Map<String, String> getLocaleCountryMap();

	/**
	 * The {@link Locale} for the currently selected currency. If no currency locale is found in preferences, 
	 * the default locale is returned. Note that the locales in preferences are stored with {@link Locale#US}.
	 * @return {@link Locale} for the currently selected currency
	 */
	Locale getCurrencyLocale();

	void refresh();

	CurrencyUnit getDefaultCurrencyUnit();

}