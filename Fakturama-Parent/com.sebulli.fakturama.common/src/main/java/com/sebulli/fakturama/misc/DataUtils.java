/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.misc;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;
import javax.money.RoundingQueryBuilder;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.swt.widgets.DateTime;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.RoundedMoney;
import org.javamoney.moneta.format.CurrencyStyle;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.money.CurrencySettingEnum;
import com.sebulli.fakturama.money.FakturamaMonetaryRoundingProvider;
import com.sebulli.fakturama.money.internal.FakturamaFormatProviderSpi;
import com.sebulli.fakturama.money.internal.FakturamaMonetaryAmountFormat;

/**
 * This class provides static functions to convert and format data like double
 * values, dates or strings.
 * 
 * @author Gerd Bartelt
 */
public class DataUtils {
	
	@Inject
	private ILocaleService localeUtil;

//    private static final String ZERO_DATE = "2000-01-01";
    protected static final double EPSILON = 0.00000001;
    static boolean useThousandsSeparator = false;
    private static DataUtils instance = null;
    private static NumberFormat currencyFormat;
    private static MonetaryRounding mro = null;
    private static MonetaryAmountFormat monetaryAmountFormat;
    private static Locale currencyLocale = Locale.getDefault();
    
    /**
     * @return the instance
     */
    public static DataUtils getInstance() {
        if(instance == null) {
            instance = new DataUtils();
            instance.initialize();
        }
        return instance;
    }
    
    /**
     * <p>Refreshes the settings of this class.</p><p>
     * <i>Caution:</i> This method makes the {@link DataUtils} class not thread safe, anymore.
     * If multiple threads are try to refresh this class the state becomes indeterminable.
     */
    public void refresh() {
        instance = null;
        useThousandsSeparator = false;
//        LocaleUtil.refresh();
    }

    /**
     * Update the currency symbol and the thousands separator from the preferences
     */
    private void initialize() {
        useThousandsSeparator = Activator.getPreferences().getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false);
        CurrencySettingEnum currencyCheckboxEnabled = CurrencySettingEnum.valueOf(Activator.getPreferences().get(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, 
        		CurrencySettingEnum.SYMBOL.name()));
        
    	this.localeUtil = ContextInjectionFactory.make(LocaleUtil.class, EclipseContextFactory.getServiceContext(Activator.getContext()));
        
        currencyFormat = NumberFormat.getCurrencyInstance();
        currencyLocale = localeUtil.getCurrencyLocale();

        if(currencyCheckboxEnabled != CurrencySettingEnum.NONE) {
            mro = Monetary.getRounding(RoundingQueryBuilder.of()
                    .setCurrency(Monetary.getCurrency(currencyLocale))
                    .setProviderName(FakturamaMonetaryRoundingProvider.DEFAULT_ROUNDING_ID)
                    .setScale(Activator.getPreferences().getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))
                    // das ist für die Schweizer Rundungsmethode auf 0.05 SFr.!
                    .set("cashRounding", Activator.getPreferences().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false))
                    .build());
        }
        monetaryAmountFormat = MonetaryFormats.getAmountFormat(
                AmountFormatQueryBuilder.of(currencyLocale)
	                // scale wird nur verwendet, wenn kein Pattern angegeben ist
                        .set(FakturamaMonetaryAmountFormat.KEY_SCALE, Activator.getPreferences().getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))                    
                        .set(currencyCheckboxEnabled)
                        .set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, 
                    Activator.getPreferences().getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false))
                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)          // wichtig, damit das eigene Format gefunden wird und nicht das DEFAULT-Format
                        .build());
    }
    
    public CurrencyUnit getDefaultCurrencyUnit() {
        return Monetary.getCurrency(currencyLocale);
    }
    
    public CurrencyUnit getCurrencyUnit(Locale currencyLocale) {
        return Monetary.getCurrency(currencyLocale);
    }
    
    public MonetaryRounding getRounding(CurrencyUnit currencyUnit, boolean cashRounding) {
        return Monetary.getRounding(RoundingQueryBuilder.of()
                .setCurrency(currencyUnit)
                .setProviderName(FakturamaMonetaryRoundingProvider.DEFAULT_ROUNDING_ID)
                .setScale(Activator.getPreferences().getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))
                .set("cashRounding", cashRounding)
                .build());
    }
    
    public MonetaryRounding getRounding(CurrencyUnit currencyUnit) {
        return getRounding(currencyUnit, Activator.getPreferences().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false));
    }
    
    /**
     * @return the monetaryAmountFormat
     */
    public MonetaryAmountFormat getMonetaryAmountFormat() {
        return monetaryAmountFormat;
    }

    public NumberFormat getCurrencyFormat() {
        if (currencyFormat == null) {
            initialize();
        }
        return currencyFormat;
    }
    
/* * * * * * * * * * * * [Price and Number calculations] * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

//    
//    /**
//     * Test, if a value is rounded to cent values. e.g. 39,43000 € is a rounded
//     * value 39,43200 € is not.
//     * 
//     * @param d
//     *            Double value to test
//     * @return true, if the value is rounded to cent values.
//     */
//    public static boolean isRounded(Double d) {
//        return DoublesAreEqual(d, round(d));
//    }

    /**
     * Test, if two double values are equal. Because of rounding errors during
     * calculation, two values with a difference of only 0.0001 are interpreted
     * as "equal"
     * 
     * @param d1
     *            First value
     * @param d2
     *            Second value
     * @return True, if the values are equal.
     */
    public boolean DoublesAreEqual(final Double par1, final Double par2) {
        Double d1 = Optional.ofNullable(par1).orElse(Double.valueOf(0.0));
        Double d2 = Optional.ofNullable(par2).orElse(Double.valueOf(0.0));
        return Math.abs(d1 - d2) < EPSILON;
    }

//    /**
//     * Test, if 2 values are equal. One value is a double and one is string.
//     * 
//     * @param s1
//     *            First value as String
//     * @param d2
//     *            Second value as double
//     * @return True, if the values are equal.
//     */
//    public static boolean DoublesAreEqual(String s1, Double d2) {
//        return DoublesAreEqual(StringToDouble(s1), d2);
//    }
//
//    /**
//     * Test, if 2 values are equal. Both values are doubles as formated string.
//     * 
//     * @param s1
//     *            First value as String
//     * @param s2
//     *            Second value as String
//     * @return True, if the values are equal.
//     */
//    public static boolean DoublesAreEqual(String s1, String s2) {
//        return DoublesAreEqual(StringToDouble(s1), StringToDouble(s2));
//    }

    /**
     * Convert a String to a double value If there is a "%" Sign, the values are
     * scales by 0.01 If there is a "," - it is converted to a "." Only numbers
     * are converted
     * 
     * @param s
     *            String to convert
     * @return converted value
     */
    // TODO Check if it can be replaced by NumberUtils.createDouble((String) newValue);
    public Double StringToDouble(String s) {
        Double d = Double.valueOf(0.0);
        
        // Remove leading and trailing spaces
        s = s.trim();
        
        // Test, if it is a percent value
        boolean isPercent = s.contains("%");

        // replace the localizes decimal separators
        s = s.replaceAll(",", ".");

        // Use this flag to search for the digits
        boolean digitFound = false;

        // Remove trailing characters that are not part of the number
        // e.g. a "sFr." with the decimal point
        while (!digitFound && (s.length()>0) ) {
            
            // Get the first character
            char firstChar = s.charAt(0);
            
            if (Character.isDigit(firstChar) ||
                    (firstChar == '-')  || (firstChar == '+') )
                digitFound = true;
            else
                //remove the first character
                s = s.substring(1);
        }
        
        digitFound = false;
        // Remove trailing characters that are not part of the number
        // e.g. a "sFr." with the decimal point
        while (!digitFound && (s.length()>0) ) {
            
            // Get the length
            int l = s.length();
            
            // Get the last character
            char lastChar = s.charAt(l-1);
            
            if (Character.isDigit(lastChar))
                digitFound = true;
            else
                //remove the last character
                s = s.substring(0, l-1);
        }
    
        // Test, if it is a negative value
        boolean isNegative = s.startsWith("-");
        
        // Use only one point
        int firstPoint;
        int lastPoint;
        boolean twoPointsFound;

        do {
            firstPoint = s.indexOf('.');
            lastPoint = s.lastIndexOf('.');
            
            // If there is more than 1 point
            twoPointsFound = (firstPoint >= 0) && (lastPoint >= 0) && (firstPoint != lastPoint);
            if ( twoPointsFound ) {
                // Remove the first
                s = s.replaceFirst("\\.", "");
            }
            
        } while (twoPointsFound);

        // use only numbers
        Pattern p = Pattern.compile("[^\\d]*(\\d*\\.?\\d*E?\\d*).*");
        Matcher m = p.matcher(s);

        if (m.find()) {
            // extract the number
            s = m.group(1);

            // add a "-", if d is negative
            if (isNegative)
                s = "-" + s;

            //s = s.substring(m.start(), m.end());
            try {
                // try to convert it to a double value
                d = Double.parseDouble(s);

                // scale it by 0.01, if it was a percent value
                if (isPercent)
                    d = d / 100;

            }
            catch (NumberFormatException e) {
            }
        }
        return d;
    }

    /**
     * Round a value to full cent values. Add an offset of 0.01 cent. This is,
     * because there may be double values like 0.004999999999999 which should be
     * rounded to 0.01
     * 
     * @param d
     *            value to round.
     * @return Rounded value
     */
    public Double round(Double d) {
        return round(d, 2);
    }
    
    public Double round(Double d, int scale) {
    	Double floorValue = null;
    	double factor = Math.pow(10, scale);
    	if (d != null) {
	        if(d >= 0)
	            floorValue = Math.floor(d * factor + EPSILON) / factor;
	        else
	            floorValue = Math.ceil(d * factor - EPSILON) / factor;
    	}
    	
        return floorValue;
    }
    
   

    /**
     * Convert a double to a formatted string value. If the value has parts of a
     * cent, add ".."
     * 
     * @param d
     *            Double value to convert
     * @param twoDecimals
     *            <code>true</code>, if the value is displayed in the format 0.00
     * @return Converted value as String
     */
    private String doubleToFormattedValue(Double d, int scale) {
        String s = "";

        // Calculate the floor cent value.
        // for negative values, use the ceil
        if(d != null) {
        	Double floorValue = round(d, scale);
        
	        // Format as "0.00"
	        NumberFormat numberFormat = NumberFormat.getNumberInstance();
	        numberFormat.setGroupingUsed(useThousandsSeparator);
	        numberFormat = new DecimalFormat((useThousandsSeparator ? ",##0." : "0.") + (scale < 0 ? StringUtils.repeat('#', scale) : StringUtils.repeat('0', scale)));
			s = numberFormat.format(floorValue);
	
	        // Are there parts of a cent ? Add ".."
	        double epsilon = 2*Math.pow(10, -1*(scale+2));
	        if (Math.abs(d - floorValue) > epsilon) {
	            s += "..";
	        }
        }
        return s;
    }

    /**
     * Convert a double to a formated string value. 
     * 
     * @param d
     *            Double value to convert
     * @param format
     *            the format of the string
     * @return Converted value as String
     */
    public String DoubleToDecimalFormatedValue(final Double d, String format) {
    	Double value = (d != null) ? d : Double.valueOf(0.0);

        // Format as ...
        DecimalFormat decimalFormat = new DecimalFormat(format);
        return decimalFormat.format(value);
    }

    /**
     * Convert a double to a formatted price value. Same as conversion to a
     * formatted value. But use always 2 decimals and add the currency sign.
     * 
     * @param value
     *            Value to convert to a price string.
     * @return Converted value as string
     */
    public String doubleToFormattedPrice(Double value) {
        CurrencyUnit currUnit = getCurrencyUnit(currencyLocale);
        MonetaryAmount rounded = RoundedMoney.of(value, currUnit);
        return formatCurrency(rounded);
    }

    /**
     * Convert a double to a formated percent value. Same as conversion to a
     * formated value. But do not use 2 decimals and add the percent sign, and
     * scale it by 100
     * 
     * @param d
     *            Value to convert to a percent string.
     * @return Converted value as string
     */
    public String DoubleToFormatedPercent(Double d) {
		String retval = "";
		if (d != null) {
			NumberFormat percentageFormat = NumberFormat.getPercentInstance();
			percentageFormat.setMinimumFractionDigits(1);
			retval = percentageFormat.format(d);
		}
		return retval;
    }

    /**
     * Convert a double to a formated quantity value. Same as conversion to a
     * formated value. But do not use 2 decimals.
     * 
     * @param d
     *            Value to convert to a quantity string.
     * @return Converted value as string
     */
    public String doubleToFormattedQuantity(Double d) {
        final int scale = Activator.getPreferences().getInt(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES, 2);
        return doubleToFormattedValue(d, scale);
    }
    
    public String doubleToFormattedQuantity(Double d, int scale) {
        return doubleToFormattedValue(d, scale);
    }

    /**
     * Convert a double to a formated price value. Same as conversion to a
     * formated price value. But round the value to full cent values
     * 
     * @param d
     *            Value to convert to a price string.
     * @return Converted value as string
     */
    public String DoubleToFormatedPriceRound(Double d) {
        return doubleToFormattedPrice(round(d));
    }
    
    /**
     * Parse a price (given as String) and return its value as Double.
     * Uses the current Money format.
     * 
     * @param value
     * @return
     */
    public Double formattedPriceToDouble(String value) {
        Double retval = NumberUtils.DOUBLE_ZERO;
        try {
            Number amount = currencyFormat.parse(value);
            retval = amount.doubleValue();
        }
        catch (ParseException e) {
//            LOG.error(String.format("Can't parse '%s' as money. Please check the format!", value));
        }
        return retval;
    }
    
    public String formatCurrency(MonetaryAmount amount) {
        return getMonetaryAmountFormat().format(mro != null ? amount.with(mro) : amount);
    }
    
    @Deprecated
    public String formatCurrency(MonetaryAmount amount, Locale locale, boolean useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
        CurrencyUnit usd = getCurrencyUnit(locale);
        MonetaryRounding mro = getRounding(usd, cashRounding);
        MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
                AmountFormatQueryBuilder.of(locale)
                        .set(useCurrencySymbol ? CurrencyStyle.SYMBOL : CurrencyStyle.CODE)
//                .set(CurrencySettingEnum.NONE)
                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)
                        .set(FakturamaMonetaryAmountFormat.KEY_SCALE, 
                        		Activator.getPreferences().getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))
                        .set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, useSeparator)
                .build());
        return format.format(amount.with(mro));
    }
    
    public String formatCurrency(MonetaryAmount amount, Locale locale, CurrencySettingEnum useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
    	CurrencyUnit usd = getCurrencyUnit(locale);
    	MonetaryRounding mro = getRounding(usd, cashRounding);
    	MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
    			AmountFormatQueryBuilder.of(locale)
    			.set(useCurrencySymbol)
    			.setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)
    			.set(FakturamaMonetaryAmountFormat.KEY_SCALE, 
    					Activator.getPreferences().getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))
    			.set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, useSeparator)
    			.build());
    	return format.format(amount.with(mro));
    }
    
    public MonetaryRounding getDefaultRounding() {
    	return getRounding(getDefaultCurrencyUnit());
    }
    
    public String formatCurrency(MonetaryAmount amount, Locale locale, boolean useCurrencySymbol) {
        return formatCurrency(amount, locale, useCurrencySymbol, 
                Activator.getPreferences().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false),
                Activator.getPreferences().getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false));
    }
     
//	/**
//	 * @param currencyCheckboxEnabled
//	 */
//	private MonetaryAmountFormat buildMonetaryAmountFormat(Locale locale, CurrencySettingEnum currencySetting, boolean useSeparator) {
//
//        NumberFormat form = NumberFormat.getCurrencyInstance(currencyLocale);
//        form.setGroupingUsed(useThousandsSeparator);
//        if (currencyLocale.getCountry().equals("CH")) {
//            if(currencySetting != CurrencySettingEnum.NONE) {
//                CurrencyUnit chf = Monetary.getCurrency(currencyLocale);
//                mro = Monetary.getRounding(RoundingQueryBuilder.of()
//                        .setCurrency(chf)
//                        // das ist für die Schweizer Rundungsmethode auf 0.05 SFr.!
//                        .set("cashRounding", Activator.getPreferences().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, true)) 
//                        .build());
//            }
//        }
//        monetaryAmountFormat = MonetaryFormats.getAmountFormat(
//                AmountFormatQueryBuilder.of(currencyLocale)
//	                // scale wird nur verwendet, wenn kein Pattern angegeben ist
//                        .set(FakturamaMonetaryAmountFormat.KEY_SCALE, Activator.getPreferences().getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))                    
//                        .set(currencySetting)
//                        .set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, 
//                    Activator.getPreferences().getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false))
//                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)          // wichtig, damit das eigene Format gefunden wird und nicht das DEFAULT-Format
//                        .build());
//        return monetaryAmountFormat;
//	}   
    
    
    /**
     * Formats a number as currency.
     *
     * @param myNumber the number to format
     * @param locale the locale
     * @param useCurrencySymbol the use currency symbol
     * @param cashRounding the cash rounding
     * @param useSeparator the use separator
     * @return the formatted string
     * 
     * @deprecated use {@link DataUtils#formatCurrency(double, Locale, CurrencySettingEnum, boolean, boolean)}
     */
    public String formatCurrency(double myNumber, Locale locale, boolean useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
        CurrencyUnit usd = getCurrencyUnit(locale);
        MonetaryAmount rounded = RoundedMoney.of(myNumber, usd);
        return formatCurrency(rounded, locale, useCurrencySymbol, cashRounding, useSeparator);
    }
    
    public String formatCurrency(double myNumber, Locale locale, CurrencySettingEnum useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
        CurrencyUnit usd = getCurrencyUnit(locale);
        MonetaryAmount rounded = RoundedMoney.of(myNumber, usd);
        return formatCurrency(rounded, locale, useCurrencySymbol, cashRounding, useSeparator);
    }

    
    /**
     * Formats a number as currency.
     *
     * @param myNumber the number to format
     * @param locale the locale
     * @return the formatted string
     */
    public String formatCurrency(double myNumber, Locale locale) {
        return formatCurrency(myNumber, locale, 
                Activator.getPreferences().getBoolean(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, true), 
                Activator.getPreferences().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false),
                Activator.getPreferences().getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false));
    }

    
//    /**
//     * Calculates the gross value based on a net value and the vat
//     * 
//     * @param net
//     *            Net value as String
//     * @param vat
//     *            Vat as double
//     * @param netValue
//     *            Net value as UniData. This is modified with the net value.
//     * @return Gross value as string
//     */
//    public String CalculateGrossFromNet(String net, Double vat, MonetaryAmount netValue) {
//        netValue = new Double(net);
//        return CalculateGrossFromNet(netValue, vat);
//    }

    /**
     * Calculates the gross value based on a net value and the vat
     * 
     * @param netValue
     *            Net value as double
     * @param vat
     *            Vat as double
     * @return Gross value as string
     */
    public MonetaryAmount CalculateGrossFromNet(MonetaryAmount netValue, Double vat) {
        return netValue.multiply(1 + vat);
    }
    
    public Double CalculateGrossFromNet(Double netValue, Double vat) {
        return netValue * (1 + vat);
    }

    /**
     * Convert a gross value to a net value.
     * 
     * @param gross
     *            Gross value as String
     * @param vat
     *            Vat as double
     * @param netvalue
     *            Net value as UniData. This is modified with the new net value.
     * @return Net value as string
     */
    public MonetaryAmount calculateNetFromGross(String gross, Double vat) {
        return calculateNetFromGross(StringToDouble(gross), vat);
    }

    /**
     * Convert a gross value to a net value.
     * 
     * @param gross
     *            Gross value as Double
     * @param vat
     *            Vat as double
     * @param netvalue
     *            Net value as UniData. This is modified with the new net value.
     * @return Net value as string
     */
    public MonetaryAmount calculateNetFromGross(Double gross, Double vat) {
        return Money.of(gross / (1 + vat), getDefaultCurrencyUnit());
    }

    /**
     * Calculates the net value based on a gross value and the vat. Uses the
     * gross value from a SWT text field and write the result into a net SWT
     * text field
     * 
     * @param gross
     *            This value is used as gross value.
     * @param vat
     *            Vat as double
     * @param netvalue
     *            Net value as UniData. This is modified with the net value.
     */
    public MonetaryAmount calculateNetFromGross(String gross, Double vat, MonetaryAmount netvalue) {
    	// If there is a gross SWT text field specified, its value is used
    	// In the other case: do not convert. Just format the netvalue.
        return gross != null ? calculateNetFromGross(gross, vat) : netvalue;
    }
    
    /**
     * Calculate net from gross as double.
     *
     * @param gross the gross
     * @param vat the vat
     * @return the double
     */
    public Double calculateNetFromGrossAsDouble(Double gross, Double vat) {
        Double s = NumberUtils.DOUBLE_ZERO;

        // If there is a gross SWT text field specified, its value is used
        if (gross != null) {
            s = gross / (1 + vat);
            // In the other case: do not convert. Just format the net value.
        }
//        else {
//            s = doubleToFormattedPrice(netvalue);
//        }
        return s;
    }
    
/* * * * * * * * * * * * [Date and Time methods] * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Get the date from a SWT DateTime widget in the format: YYYY-MM-DD
     * 
     * @param dtDate
     *            SWT DateTime widget
     * @return Date as formated String
     */
    public String getDateTimeAsString(DateTime dtDate) {
        return String.format("%04d-%02d-%02d", dtDate.getYear(), dtDate.getMonth() + 1, dtDate.getDay());
    }

    
    
    /**
     * Get the date from a Calendar object in the format: YYYY-MM-DD
     * 
     * @param calendar
     *            Calendar object
     * @return Date as formatted String
     */
    public String getDateTimeAsString(Calendar calendar) {
        return String.format("%tF", calendar.getTime()); 
    }

    /**
     * Get the date and time from a Calendar object in the format: YYYY-MM-DD HH:MM:SS
     * 
     * @param calendar
     *            Calendar object
     * @return Date and Time as formatted String
     */
    public String getDateAndTimeAsString(Calendar calendar) {
        return String.format("%1$tF %1$tT", calendar.getTime());
    }

    /**
     * Get the date from a Calendar object in the localized format.
     * 
     * @param calendar
     *            calendar Gregorian Calendar object
     * @return Date as formated String
     */
    public String getDateTimeAsLocalString(GregorianCalendar calendar) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return df.format(calendar.getTime());
    }

//    /**
//     * Convert a date string from the format YYYY-MM-DD to to localized format.
//     * 
//     * @param s
//     *            Date String
//     * @return Date as formated String
//     */
//    public static String DateAsLocalString(String s) {
//        if(s.equals(ZERO_DATE)) {
//            return "";
//        }
//
//        GregorianCalendar calendar = new GregorianCalendar();
//        try {
//            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//            calendar.setTime(formatter.parse(s));
//        }
//        catch (ParseException e) {
////            Logger.logError(e, "Error parsing Date");
//        }
//        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
//        return df.format(calendar.getTime());
//    }

    /**
     * Convert a date and time string from the format YYYY-MM-DD HH:MM:SS to to
     * localized format.
     * 
     * @param s
     *            Date and time String
     * @return Date and time as formated String
     */
    public String DateAndTimeAsLocalString(String s) {
    	String retval = "";
    	if(s == null) {
    		return retval;
    	}
        GregorianCalendar calendar = new GregorianCalendar();
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            calendar.setTime(formatter.parse(s));
	        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM);
	        retval = df.format(calendar.getTime());
        }
        catch (ParseException e) {
//            Logger.logError(e, "Error parsing Date and Time");
        }
        return retval;
    }

    /**
     * Convert a date string into the format ISO 8601 YYYY-MM-DD.
     * 
     * @param s
     *            Date String
     * @return Date as formated String
     */
    public String DateAsISO8601String(String s) {
        GregorianCalendar calendar = null;
        String retval;
        if(s != null && s != "") {
            calendar = getCalendarFromDateString(s);
            retval = getDateTimeAsString(calendar);
        } else {
            retval = "";
        }
        return retval;
    }

    /**
     * Returns the date now in the format ISO 8601 YYYY-MM-DD
     * 
     * @return Date as formatted String
     */
    public String DateAsISO8601String() {

        GregorianCalendar calendar = new GregorianCalendar();
        return getDateAndTimeAsString(calendar);
    }

    /**
     * Returns the date and time of now in a localized format.
     * 
     * @return Date and time as formated String
     */
    public String DateAndTimeOfNowAsISO8601String() {

        LocalDateTime dateTime = LocalDateTime.now();
        return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }
    
    /**
     * Gets the formatted localized date.
     *
     * @param date the date
     * @return the formatted localized date
     */
    public String getFormattedLocalizedDate(Date date) {
        if(date != null) {
            LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        } else { return ""; }
    }
    
    /**
     * Convert date strings from the following format to a calendar
     * 
     * @param date
     *            Date as string
     * @return GregorianCalendar
     */
    public GregorianCalendar getCalendarFromDateString(String date) {
        GregorianCalendar calendar = new GregorianCalendar();

        // try to parse the input date string
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            calendar.setTime(formatter.parse(date));
        }
        catch (ParseException e) {

            // use also localized formats
            try {
                DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                calendar.setTime(formatter.parse(date));
            }
            catch (ParseException e2) {

                // use also localized formats
                try {
                    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                    calendar.setTime(formatter.parse(date));
                }
                catch (ParseException e3) {
//                    Logger.logError(e3, "Error parsing Date:" + date);
                }
            }
        }
        return calendar;
    }

    /**
     * Returns the date and time of now in a localized format.
     * 
     * @return Date and time as formated String
     */
    public String DateAndTimeOfNowAsLocalString() {
        GregorianCalendar calendar = new GregorianCalendar();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        return df.format(calendar.getTime());
    }

//    /**
//     * Adds days to a date string.
//     * 
//     * @param date
//     *            Days to add
//     * @param days
//     *            Date as string
//     * @return Calculated date
//     */
//    public static String AddToDate(String date, int days) {
//        GregorianCalendar calendar = getCalendarFromDateString(date);
//
//        // Add the days
//        calendar.add(Calendar.DAY_OF_MONTH, days);
//
//        // And convert it back to a String value
//        return getDateTimeAsString(calendar);
//    }
    
    public LocalDateTime addToDate(Date date, int days) {
        LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDate.plusDays(days);
    }

    /**
     * Calculates the similarity of two string.
     * 
     * The result is a value from 0.0 to 1.0. Returns 1.0, if both strings are
     * equal.
     * 
     * @param sA
     *            First String value
     * @param sB
     *            Second String value
     * @return Similarity from 0.0 to 1.0
     */
    public double similarity(String sA, String sB) {
        return StringUtils.getJaroWinklerDistance(sA, sB);
    }

//    /**
//     * Convert a discount string to a double value The input string is
//     * interpreted as a percent value. Positive values are converted to
//     * negative, because a discount is always negative.
//     * 
//     * "-3%" is converted to -0.03 "-3" is converted to -0.03 "3" is converted
//     * to -0.03
//     * 
//     * @param s
//     *            String to convert
//     * @return Result as double from -0.999 to 0.0
//     */
//    public static double StringToDoubleDiscount(String s) {
//
//        // The input String is always a percent value
//        s = s + "%";
//
//        // convert it
//        double d = StringToDouble(s);
//
//        // Convert it to negative values
//        if (d > 0)
//            d = -d;
//
//        // A discount of more than -99.9% is invalid.
//        if (d < -0.999)
//            d = 0.0;
//
//        return d;
//    }
    
    /**
     * Remove all carriage returns from a string
     * 
     * @param s
     *      The string with the carriage returns
     * @return
     *      The new string without them
     */
    public String removeCR(String s) {
        return s.replaceAll("\r", "");
    }

    /**
     * convert all LineFeeds to OS dependent LineFeeds
     * 
     * @param s
     *      The string with the carriage returns
     * @return
     *      The new string without them
     */
    public String makeOSLineFeeds(String s) {
        return s.replaceAll("\n", System.lineSeparator());
    }
    
    /**
     * Compare two strings but ignore carriage returns
     * 
     * @param s1
     *          First String to compare
     * @param s2
     *          Second String to compare
     * @return
     *          True, if bothe are equal
     */
    public boolean MultiLineStringsAreEqual (String s1, String s2) {
        return removeCR(s1).equals(removeCR(s2));
    }
    
    /**
     * Converts all \r\n to \n
     * \r\n are Generated by SWT text controls on a windows system.
     * 
     * @param s
     *      The string to convert
     * @return
     *      The converted string
     */
    public String convertCRLF2LF(String s){
        s = StringUtils.defaultString(s).replaceAll("\\r\\n", "\n");
        return s;
    }
    
    /**
     * If the string is a multi line string, extract only one line 
     * 
     * @param s
     * @return
     */
    public String getSingleLine(String s) {
        String newline = System.lineSeparator();
        return s != null ? s.split(newline)[0] : "";
    }
    
    public static void main(String[] args) {  
        System.out.println("Start Tests...");
        String newline = System.lineSeparator();
        String s = "Hello"+newline+"World";
        System.out.println(String.format("String before: [%s]", s));
//        System.out.println(String.format("String after: [%s]", getSingleLine(s)));
        
        Calendar calendar = Calendar.getInstance();
        System.out.println("Date: " + String.format("%tF", calendar.getTime()));
        System.out.println("Date and Time: " + String.format("%1$tF %1$tT", calendar.getTime()));
    }

    
    /**
     * Replace all accented characters
     * 
     * @param s
     *      The string to convert
     * @return
     *      The converted string
     */
    public String replaceAllAccentedChars(String s) {
        
	    s = s.replace("À", "A");
	    s = s.replace("Á", "A");
	    s = s.replace("Â", "A");
	    s = s.replace("Ã", "A");
	    s = s.replace("Ä", "Ae");
	    s = s.replace("â", "a");
	    s = s.replace("ã", "a");
	    s = s.replace("ä", "ae");
	    s = s.replace("à", "a");
	    s = s.replace("á", "a");

	    s = s.replace("È", "E");
	    s = s.replace("É", "E");
	    s = s.replace("Ê", "E");
	    s = s.replace("Ë", "E");
	    s = s.replace("ê", "e");
	    s = s.replace("ë", "e");
	    s = s.replace("è", "e");
	    s = s.replace("é", "e");

	    s = s.replace("Ì", "I");
	    s = s.replace("Í", "I");
	    s = s.replace("Î", "I");
	    s = s.replace("Ï", "I");
	    s = s.replace("î", "i");
	    s = s.replace("ï", "i");
	    s = s.replace("ì", "i");
	    s = s.replace("í", "i");

	    s = s.replace("Ò", "O");
	    s = s.replace("Ó", "O");
	    s = s.replace("Ô", "O");
	    s = s.replace("Õ", "O");
	    s = s.replace("Ö", "Oe");
	    s = s.replace("ô", "o");
	    s = s.replace("õ", "o");
	    s = s.replace("ö", "oe");
	    s = s.replace("ò", "o");
	    s = s.replace("ó", "o");

	    s = s.replace("Ù", "U");
	    s = s.replace("Ú", "U");
	    s = s.replace("Û", "U");
	    s = s.replace("Ü", "Ue");
	    s = s.replace("û", "u");
	    s = s.replace("ü", "ue");
	    s = s.replace("ù", "u");
	    s = s.replace("ú", "u");

	    s = s.replace("Ý", "Y");
	    s = s.replace("ý", "y");
	    s = s.replace("ñ", "n");
	    s = s.replace("ß", "ss");

        return s;
    }


}
