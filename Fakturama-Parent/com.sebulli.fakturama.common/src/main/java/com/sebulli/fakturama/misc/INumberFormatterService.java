package com.sebulli.fakturama.misc;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.ULocale;
import com.sebulli.fakturama.money.CurrencySettingEnum;

public interface INumberFormatterService {

	/**
	 * Convert a double to a formated string value. 
	 * 
	 * @param d
	 *            Double value to convert
	 * @param format
	 *            the format of the string
	 * @return Converted value as String
	 */
	String DoubleToDecimalFormatedValue(Double d, String format);

	/**
	 * Convert a double to a formatted price value. Same as conversion to a
	 * formatted value. But use always 2 decimals and add the currency sign.
	 * 
	 * @param value
	 *            Value to convert to a price string.
	 * @return Converted value as string
	 */
	String doubleToFormattedPrice(Double value);

	/**
	 * Convert a double to a formated percent value. Same as conversion to a
	 * formated value. But do not use 2 decimals and add the percent sign, and
	 * scale it by 100
	 * 
	 * @param d
	 *            Value to convert to a percent string.
	 * @return Converted value as string
	 */
	String DoubleToFormatedPercent(Double d);

	/**
	 * Convert a double to a formated quantity value. Same as conversion to a
	 * formated value. But do not use 2 decimals.
	 * 
	 * @param d
	 *            Value to convert to a quantity string.
	 * @return Converted value as string
	 */
	String doubleToFormattedQuantity(Double d);

	String doubleToFormattedQuantity(Double d, int scale);

	/**
	 * Convert a double to a formated price value. Same as conversion to a
	 * formated price value. But round the value to full cent values
	 * 
	 * @param d
	 *            Value to convert to a price string.
	 * @return Converted value as string
	 */
	String DoubleToFormatedPriceRound(Double d);

	/**
	 * Parse a price (given as String) and return its value as Double.
	 * Uses the current Money format.
	 * 
	 * @param value
	 * @return
	 */
	Double formattedPriceToDouble(String value);

	String formatCurrency(MonetaryAmount amount);

	String formatCurrency(MonetaryAmount amount, ULocale locale, CurrencySettingEnum useCurrencySymbol,
			boolean cashRounding, boolean useSeparator);

	/**
	 * The currently used currency symbol, according to the preferences (symbol, ISO code or nothing at all).
	 * @return symbol, ISO code or nothing at all
	 */
	String getCurrencySymbol(MonetaryAmount monetaryAmount);
		
	/**
	 * Formats a number as currency.
	 *
	 * @param myNumber the number to format
	 * @param locale the locale
	 * @param useCurrencySymbol the use currency symbol
	 * @param cashRounding the cash rounding
	 * @return the formatted string
	 */
	String formatCurrency(double myNumber, ULocale locale, CurrencySettingEnum useCurrencySymbol, boolean cashRounding,
			boolean useSeparator);

	NumberFormat getCurrencyFormat();

	MonetaryAmountFormat getMonetaryAmountFormat();

	CurrencyUnit getCurrencyUnit(ULocale currencyLocale);

	/**
	 * Updates some internal values if preferences have been changed.
	 * 
	 */
	void update();
}