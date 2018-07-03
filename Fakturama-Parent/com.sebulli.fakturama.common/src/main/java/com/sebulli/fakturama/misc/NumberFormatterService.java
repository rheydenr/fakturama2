/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2018 The Fakturama Team
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
package com.sebulli.fakturama.misc;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.javamoney.moneta.RoundedMoney;
import org.javamoney.moneta.format.CurrencyStyle;
import org.osgi.service.prefs.Preferences;

import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.money.CurrencySettingEnum;
import com.sebulli.fakturama.money.internal.FakturamaFormatProviderSpi;
import com.sebulli.fakturama.money.internal.FakturamaMonetaryAmountFormat;

/**
 * Formatter service for numbers and currency values.
 *
 */
public class NumberFormatterService implements INumberFormatterService {
	@Inject
	private ILocaleService localeUtil;
    
    @Inject 
    private ILogger log;

    @Inject
    protected IPreferencesService defaultValuePrefs;
    private Preferences prefs;

    private NumberFormat currencyFormat;
    private MonetaryRounding mro = null;
    private MonetaryAmountFormat monetaryAmountFormat;
    private boolean useThousandsSeparator = false;
   
//    @PostConstruct
    public void initialize() {
    	useThousandsSeparator = prefs.getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false);
        CurrencySettingEnum currencyCheckboxEnabled = CurrencySettingEnum.valueOf(prefs.get(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, 
        		CurrencySettingEnum.SYMBOL.name()));
        
//    	this.localeUtil = ContextInjectionFactory.make(LocaleUtil.class, EclipseContextFactory.getServiceContext(Activator.getContext()));
        
        Locale currencyLocale = getLocaleUtil().getCurrencyLocale();

        currencyFormat = NumberFormat.getCurrencyInstance();
        if(currencyCheckboxEnabled != CurrencySettingEnum.NONE) {
            mro = DataUtils.getInstance().getRounding(Monetary.getCurrency(currencyLocale), 
                    // das ist für die Schweizer Rundungsmethode auf 0.05 SFr.!
            		prefs.getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false));
        }
        monetaryAmountFormat = MonetaryFormats.getAmountFormat(
                AmountFormatQueryBuilder.of(currencyLocale)
	                // scale wird nur verwendet, wenn kein Pattern angegeben ist
                        .set(FakturamaMonetaryAmountFormat.KEY_SCALE, prefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))                    
                        .set(currencyCheckboxEnabled)
                        .set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, 
                              prefs.getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false))
                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)          // wichtig, damit das eigene Format gefunden wird und nicht das DEFAULT-Format
                        .build());
    }


    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#DoubleToDecimalFormatedValue(java.lang.Double, java.lang.String)
	 */
    @Override
	public String DoubleToDecimalFormatedValue(final Double d, String format) {
    	Double value = (d != null) ? d : Double.valueOf(0.0);

        // Format as ...
        DecimalFormat decimalFormat = new DecimalFormat(format);
        return decimalFormat.format(value);
    }

    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#doubleToFormattedPrice(java.lang.Double)
	 */
    @Override
	public String doubleToFormattedPrice(Double value) {
        CurrencyUnit currUnit = getCurrencyUnit(getLocaleUtil().getCurrencyLocale());
        MonetaryAmount rounded = RoundedMoney.of(value, currUnit);
        return formatCurrency(rounded);
    }

    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#DoubleToFormatedPercent(java.lang.Double)
	 */
    @Override
	public String DoubleToFormatedPercent(Double d) {
		String retval = "";
		if (d != null) {
			NumberFormat percentageFormat = NumberFormat.getPercentInstance();
			percentageFormat.setMinimumFractionDigits(1);
			retval = percentageFormat.format(d);
		}
		return retval;
    }

    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#doubleToFormattedQuantity(java.lang.Double)
	 */
    @Override
	public String doubleToFormattedQuantity(Double d) {
        final int scale = prefs.getInt(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES, 2);
        return doubleToFormattedValue(d, scale);
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#doubleToFormattedQuantity(java.lang.Double, int)
	 */
    @Override
	public String doubleToFormattedQuantity(Double d, int scale) {
        return doubleToFormattedValue(d, scale);
    }

    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#DoubleToFormatedPriceRound(java.lang.Double)
	 */
    @Override
	public String DoubleToFormatedPriceRound(Double d) {
        return doubleToFormattedPrice(DataUtils.getInstance().round(d));
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formattedPriceToDouble(java.lang.String)
	 */
    @Override
	public Double formattedPriceToDouble(String value) {
        Double retval = NumberUtils.DOUBLE_ZERO;
        try {
            Number amount = currencyFormat.parse(value);
            retval = amount.doubleValue();
        }
        catch (ParseException e) {
            getLog().info(String.format("Can't parse '%s' as money. Please check the format!", value));
        }
        return retval;
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(javax.money.MonetaryAmount)
	 */
    @Override
	public String formatCurrency(MonetaryAmount amount) {
        return getMonetaryAmountFormat().format(mro != null ? amount.with(mro) : amount);
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(javax.money.MonetaryAmount, java.util.Locale, boolean, boolean, boolean)
	 */
    @Override
	@Deprecated
    public String formatCurrency(MonetaryAmount amount, Locale locale, boolean useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
        CurrencyUnit usd = getCurrencyUnit(locale);
        MonetaryRounding mro = DataUtils.getInstance().getRounding(usd, cashRounding);
        
        MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
                AmountFormatQueryBuilder.of(locale)
                        .set(useCurrencySymbol ? CurrencyStyle.SYMBOL : CurrencyStyle.CODE)
//                .set(CurrencySettingEnum.NONE)
                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)
                        .set(FakturamaMonetaryAmountFormat.KEY_SCALE, 
                        		prefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))
                        .set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, useSeparator)
                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE) 
                .build());
        return format.format(amount.with(mro));
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(javax.money.MonetaryAmount, java.util.Locale, com.sebulli.fakturama.money.CurrencySettingEnum, boolean, boolean)
	 */
    @Override
	public String formatCurrency(MonetaryAmount amount, Locale locale, CurrencySettingEnum useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
    	CurrencyUnit usd = getCurrencyUnit(locale);
    	MonetaryRounding mro = DataUtils.getInstance().getRounding(usd, cashRounding);
    	MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
    			AmountFormatQueryBuilder.of(locale)
    			.set(useCurrencySymbol)
    			.setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)
    			.set(FakturamaMonetaryAmountFormat.KEY_SCALE, 
    					prefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))
    			.set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, useSeparator)
    			.build());
    	return format.format(amount.with(mro));
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(javax.money.MonetaryAmount, java.util.Locale, boolean)
	 */
    @Override
	public String formatCurrency(MonetaryAmount amount, Locale locale, boolean useCurrencySymbol) {
        return formatCurrency(amount, locale, useCurrencySymbol, 
                prefs.getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false),
                prefs.getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false));
    }
     
//	/**
//	 * @param currencyCheckboxEnabled
//	 */
//	private MonetaryAmountFormat buildMonetaryAmountFormat(Locale locale, CurrencySettingEnum currencySetting, boolean useSeparator) {
//
//        NumberFormat form = NumberFormat.getCurrencyInstance(localeUtil.getCurrencyLocale());
//        form.setGroupingUsed(useThousandsSeparator);
//        if (localeUtil.getCurrencyLocale().getCountry().equals("CH")) {
//            if(currencySetting != CurrencySettingEnum.NONE) {
//                CurrencyUnit chf = Monetary.getCurrency(localeUtil.getCurrencyLocale());
//                mro = Monetary.getRounding(RoundingQueryBuilder.of()
//                        .setCurrency(chf)
//                        // das ist für die Schweizer Rundungsmethode auf 0.05 SFr.!
//                        .set("cashRounding", prefs.getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, true)) 
//                        .build());
//            }
//        }
//        monetaryAmountFormat = MonetaryFormats.getAmountFormat(
//                AmountFormatQueryBuilder.of(localeUtil.getCurrencyLocale())
//	                // scale wird nur verwendet, wenn kein Pattern angegeben ist
//                        .set(FakturamaMonetaryAmountFormat.KEY_SCALE, prefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))                    
//                        .set(currencySetting)
//                        .set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, 
//                    prefs.getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false))
//                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)          // wichtig, damit das eigene Format gefunden wird und nicht das DEFAULT-Format
//                        .build());
//        return monetaryAmountFormat;
//	}   
    
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(double, java.util.Locale, boolean, boolean, boolean)
	 */
    @Override
	public String formatCurrency(double myNumber, Locale locale, boolean useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
        CurrencyUnit usd = getCurrencyUnit(locale);
        MonetaryAmount rounded = RoundedMoney.of(BigDecimal.valueOf(myNumber), usd);
        return formatCurrency(rounded, locale, useCurrencySymbol, cashRounding, useSeparator);
    }
    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(double, java.util.Locale, com.sebulli.fakturama.money.CurrencySettingEnum, boolean, boolean)
	 */
    @Override
	public String formatCurrency(double myNumber, Locale locale, CurrencySettingEnum useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
        CurrencyUnit usd = getCurrencyUnit(locale);
        MonetaryAmount rounded = RoundedMoney.of(myNumber, usd);
        return formatCurrency(rounded, locale, useCurrencySymbol, cashRounding, useSeparator);
    }

    
    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(double, java.util.Locale)
	 */
    @Override
	public String formatCurrency(double myNumber, Locale locale) {
        return formatCurrency(myNumber, locale, 
                prefs.getBoolean(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, true), 
                prefs.getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false),
                prefs.getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false));
    }

    @Override
	public CurrencyUnit getCurrencyUnit(Locale currencyLocale) {
        return Monetary.getCurrency(currencyLocale);
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
        	Double floorValue = DataUtils.getInstance().round(d, scale);
        
	        // Format as "0.00"
	        NumberFormat numberFormat = NumberFormat.getNumberInstance();
	        numberFormat.setGroupingUsed(isUseThousandsSeparator());
	        numberFormat = new DecimalFormat((isUseThousandsSeparator() ? ",##0." : "0.") + (scale < 0 ? StringUtils.repeat('#', scale) : StringUtils.repeat('0', scale)));
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
     * @return the monetaryAmountFormat
     */
    @Override
	public MonetaryAmountFormat getMonetaryAmountFormat() {
    	if(monetaryAmountFormat == null) {
    		initialize();
    	}
        return monetaryAmountFormat;
    }

    @Override
	public NumberFormat getCurrencyFormat() {
        if (currencyFormat == null) {
            initialize();
        }
        return currencyFormat;
    }


	/**
	 * @return the localeUtil
	 */
	public ILocaleService getLocaleUtil() {
		return this.localeUtil;
	}

	public void unbindLocaleService(ILocaleService localeUtil) {
		this.localeUtil = null;
	}

	/**
	 * @param localeUtil the localeUtil to set
	 */
	public void bindLocaleService(ILocaleService localeUtil) {
		this.localeUtil = localeUtil;
	}
	
	public void bindPreferenceService(IPreferencesService defaultValuePrefs) {
		this.defaultValuePrefs = defaultValuePrefs;
    	this.prefs = defaultValuePrefs.getRootNode().node("/instance/com.sebulli.fakturama.rcp");
	}

	public void unbindPreferenceService(IPreferencesService defaultValuePrefs) {
		this.defaultValuePrefs = null;
		this.prefs = null;
	}

	/**
	 * @return the log
	 */
	public ILogger getLog() {
		return log;
	}

	public void unbindLog(ILogger log) {
		this.log = null;
	}

	/**
	 * @param log the log to set
	 */
	public void bindLog(ILogger log) {
		this.log = log;
	}


	/**
	 * @return the useThousandsSeparator
	 */
	private boolean isUseThousandsSeparator() {
		return useThousandsSeparator;
	}

}
