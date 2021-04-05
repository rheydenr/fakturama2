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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;
import javax.money.RoundingQueryBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.javamoney.moneta.Money;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.money.FakturamaMonetaryRoundingProvider;

/**
 * This class provides static functions to convert and format data like double
 * values, dates or strings.
 * 
 * @author Gerd Bartelt
 */
public class DataUtils {
	
	@Inject @org.eclipse.e4.core.di.annotations.Optional
	private ILocaleService localeUtil;
	
	@Inject @org.eclipse.e4.core.di.annotations.Optional
	private IPreferenceStore preferenceStore;
	
//	private static ULocale currencyLocale;

//    private static final String ZERO_DATE = "2000-01-01";
    protected static final double EPSILON = 0.00000001;
    private static DataUtils instance = null;

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
    }

    /**
     * Update the currency symbol and the thousands separator from the preferences
     * @param localeUtil 
     */
    private void initialize() {
//        currencyLocale = getLocaleUtil().getCurrencyLocale();
    }
    
    public ILocaleService getLocaleUtil() {
        if (localeUtil == null) {
            ServiceReference<ILocaleService> serviceReference = Activator.getContext().getServiceReference(ILocaleService.class);
            this.setLocaleUtil(Activator.getContext().getService(serviceReference));
        }
        return localeUtil;
    }

    public void setLocaleUtil(ILocaleService localeUtil) {
        this.localeUtil = localeUtil;
    }

    public CurrencyUnit getDefaultCurrencyUnit() {
        return Monetary.getCurrency(getLocaleUtil().getCurrencyLocale().toLocale());
    }
    
    public MonetaryRounding getDefaultRounding() {
    	return getRounding(getDefaultCurrencyUnit());
    }

    public MonetaryRounding getRounding(CurrencyUnit currencyUnit, boolean cashRounding) {
        return Monetary.getRounding(RoundingQueryBuilder.of()
                .setCurrency(currencyUnit)
                .setProviderName(FakturamaMonetaryRoundingProvider.DEFAULT_ROUNDING_ID)
                .setScale(getPreferenceStore().getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES))
                .set("cashRounding", cashRounding)
                .build());
    }
    public MonetaryRounding getRounding() {
        return getRounding(getDefaultCurrencyUnit());
    }
    
    public MonetaryRounding getRounding(CurrencyUnit currencyUnit) {
        return getRounding(currencyUnit, getPreferenceStore().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING));
    }
    
    private IPreferenceStore getPreferenceStore() {
        if(preferenceStore == null) {
            preferenceStore = Activator.getPreferenceStore();
        }
        return preferenceStore;
    }
    
    public void setPreferenceStore(IPreferenceStore preferenceStore) {
        this.preferenceStore = preferenceStore;
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
	            floorValue = Math.round(d * factor + EPSILON) / factor;
	        else
	            floorValue = Math.round(d * factor - EPSILON) / factor;
    	}
    	
        return floorValue;
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
    @Deprecated
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
     * Adds days to a date string.
     * 
     * @param date
     *            Days to add
     * @param days
     *            Date
     * @return Calculated date
     */
    public LocalDateTime addToDate(Date date, int days) {
        LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDate.plusDays(days);
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
        return s != null ? s.replaceAll(System.lineSeparator()+"|\\r|\\n", "") : "";
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
     *          <code>true</code> if both are equal
     */
    public boolean MultiLineStringsAreEqual(String s1, String s2) {
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
	    
	    s = s.replace("\u00a0", " ");
	    		
        return s;
    }

	public boolean isDoubleZero(Double doubleToTest) {
		return DoublesAreEqual(doubleToTest, NumberUtils.DOUBLE_ZERO);
	}
}
