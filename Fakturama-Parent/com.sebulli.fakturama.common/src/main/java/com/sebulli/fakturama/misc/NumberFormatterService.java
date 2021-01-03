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

import java.text.ParseException;
import java.util.Objects;

import javax.annotation.PostConstruct;
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
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.javamoney.moneta.RoundedMoney;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.ULocale;
import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.money.CurrencySettingEnum;
import com.sebulli.fakturama.money.FakturamaMonetaryRoundingProvider;
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
    
    private IPreferenceStore preferenceStore;

    private NumberFormat currencyFormat;
    private MonetaryRounding mro = null;
    private boolean useThousandsSeparator = false;
    private ULocale currencyLocale = ULocale.getDefault();
   
    @PostConstruct
    protected void initialize() {
		// without preferences nothing makes sense...
    	if(preferenceStore == null) {
    	    preferenceStore = EclipseContextFactory.getServiceContext(Activator.getContext()).get(IPreferenceStore.class);
    	    if(preferenceStore == null) {
    	        log.error("no preference store available, NumberFormatterService can't be initialized!");
    	        return;
    	    }
    	}
    	
    	useThousandsSeparator = preferenceStore.getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR);
        String useCurrencySymbol = preferenceStore.getString(Constants.PREFERENCES_CURRENCY_USE_SYMBOL);
        CurrencySettingEnum currencyCheckboxEnabled;
        if(useCurrencySymbol.isEmpty()) {
            // is no value is found we use symbol as default value
            // (this happens in initialization phase of preferences page)
            currencyCheckboxEnabled = CurrencySettingEnum.SYMBOL;
        } else {
            currencyCheckboxEnabled = CurrencySettingEnum.valueOf(useCurrencySymbol);
        }
        
        currencyLocale = localeUtil.getCurrencyLocale();

        currencyFormat = NumberFormat.getCurrencyInstance(currencyLocale);
        if(currencyCheckboxEnabled != CurrencySettingEnum.NONE) {
            mro = Monetary.getRounding(RoundingQueryBuilder.of()
                    .setCurrency(Monetary.getCurrency(currencyLocale.toLocale()))
                    .setProviderName(FakturamaMonetaryRoundingProvider.DEFAULT_ROUNDING_ID)
                    .setScale(preferenceStore.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES))
                    // das ist für die Schweizer Rundungsmethode auf 0.05 SFr.!
                    .set("cashRounding", preferenceStore.getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING))
                    .build());
        }
    }

    @Override
    public void update() {
    	initialize();
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
	        final int scale = preferenceStore.getInt(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES);
			percentageFormat.setMaximumFractionDigits(scale);
			retval = percentageFormat.format(d);
		}
		return retval;
    }

    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#doubleToFormattedQuantity(java.lang.Double)
	 */
    @Override
	public String doubleToFormattedQuantity(Double d) {
        final int scale = preferenceStore.getInt(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES);
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
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(javax.money.MonetaryAmount, ULocale, com.sebulli.fakturama.money.CurrencySettingEnum, boolean, boolean)
	 */
    @Override
	public String formatCurrency(MonetaryAmount amount, ULocale locale, CurrencySettingEnum useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
    	CurrencyUnit usd = getCurrencyUnit(locale);
    	MonetaryRounding mro = DataUtils.getInstance().getRounding(usd, cashRounding);
    	MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
    			AmountFormatQueryBuilder.of(locale.toLocale())
    			.set(useCurrencySymbol)
    			.setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)
    			.set(FakturamaMonetaryAmountFormat.KEY_SCALE, 
    			        preferenceStore.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES))
    			.set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, useSeparator)
    			.build());
    	return format.format(amount.with(mro));
    }
      
//	/**
//	 * @param currencyCheckboxEnabled
//	 */
//	private MonetaryAmountFormat buildMonetaryAmountFormat(ULocale locale, CurrencySettingEnum currencySetting, boolean useSeparator) {
//
//        NumberFormat form = NumberFormat.getCurrencyInstance(localeUtil.getCurrencyLocale());
//        form.setGroupingUsed(useThousandsSeparator);
//        if (localeUtil.getCurrencyLocale().getCountry().equals("CH")) {
//            if(currencySetting != CurrencySettingEnum.NONE) {
//                CurrencyUnit chf = Monetary.getCurrency(localeUtil.getCurrencyLocale());
//                mro = Monetary.getRounding(RoundingQueryBuilder.of()
//                        .setCurrency(chf)
//                        // das ist für die Schweizer Rundungsmethode auf 0.05 SFr.!
//                        .set("cashRounding", Activator.getPreferences().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, true)) 
//                        .build());
//            }
//        }
//        monetaryAmountFormat = MonetaryFormats.getAmountFormat(
//                AmountFormatQueryBuilder.of(localeUtil.getCurrencyLocale())
//	                // scale wird nur verwendet, wenn kein Pattern angegeben ist
//                        .set(FakturamaMonetaryAmountFormat.KEY_SCALE, Activator.getPreferences().getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, 2))                    
//                        .set(currencySetting)
//                        .set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, 
//                    Activator.getPreferences().getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, false))
//                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)          // wichtig, damit das eigene Format gefunden wird und nicht das DEFAULT-Format
//                        .build());
//        return monetaryAmountFormat;
//	}   
    
    @Override
    public String getCurrencySymbol(MonetaryAmount amount) {
    	return getCurrencySymbol(amount.getCurrency());
    }
	
    private String getCurrencySymbol(CurrencyUnit currency) {
    	String retval = "";
    	String useCurrencySymbol = preferenceStore.getString(Constants.PREFERENCES_CURRENCY_USE_SYMBOL);
    	if(useCurrencySymbol.isEmpty()) {
    	    useCurrencySymbol = CurrencySettingEnum.CODE.name();
    	}
        CurrencySettingEnum currencySymbol = CurrencySettingEnum.valueOf(useCurrencySymbol);
    	switch (currencySymbol) {
		case SYMBOL:
	        Currency jdkCurrency = getCurrency(currency.getCurrencyCode());
	        if (Objects.nonNull(jdkCurrency)) {
	            return jdkCurrency.getSymbol(localeUtil.getCurrencyLocale());
	        }
	        retval = currency.getCurrencyCode();
			break;
		case CODE:
			retval = currency.getCurrencyCode();
		break;
		default:
			break;
		}
        return retval;
    }
    
    private Currency getCurrency(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode);
        } catch (Exception e) {
            return null;
        }
    }

    /* (non-Javadoc)
	 * @see com.sebulli.fakturama.misc.INumberFormatterService#formatCurrency(double, java.util.ULocale, com.sebulli.fakturama.money.CurrencySettingEnum, boolean, boolean)
	 */
    @Override
	public String formatCurrency(double myNumber, ULocale locale, CurrencySettingEnum useCurrencySymbol, boolean cashRounding, boolean useSeparator) {
        CurrencyUnit usd = getCurrencyUnit(locale);
        MonetaryAmount rounded = RoundedMoney.of(myNumber, usd);
        return formatCurrency(rounded, locale, useCurrencySymbol, cashRounding, useSeparator);
    }

    
    @Override
	public CurrencyUnit getCurrencyUnit(ULocale currencyLocale) {
        return Monetary.getCurrency(currencyLocale.toLocale());
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
     * @return the monetaryAmountFormat
     */
    @Override
	public MonetaryAmountFormat getMonetaryAmountFormat() {
        String useCurrencySymbol = preferenceStore.getString(Constants.PREFERENCES_CURRENCY_USE_SYMBOL);
        if(useCurrencySymbol.isEmpty()) {
            useCurrencySymbol = CurrencySettingEnum.SYMBOL.name();
        }
        CurrencySettingEnum currencyCheckboxEnabled = CurrencySettingEnum.valueOf(useCurrencySymbol);
        return MonetaryFormats.getAmountFormat(
                AmountFormatQueryBuilder.of(currencyLocale.toLocale())
	                // scale wird nur verwendet, wenn kein Pattern angegeben ist
                        .set(FakturamaMonetaryAmountFormat.KEY_SCALE, preferenceStore.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES))                    
                        .set(currencyCheckboxEnabled)
                        .set(FakturamaMonetaryAmountFormat.KEY_USE_GROUPING, 
                                preferenceStore.getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR))
                        .setFormatName(FakturamaFormatProviderSpi.DEFAULT_STYLE)          // wichtig, damit das eigene Format gefunden wird und nicht das DEFAULT-Format
                        .build());
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
		return localeUtil;
	}

	public void unbindLocaleService() {
		this.localeUtil = null;
	}

	/**
	 * @param localeUtil the localeUtil to set
	 */
	public void bindLocaleService(ILocaleService localeUtil) {
		this.localeUtil = localeUtil;
		initialize();
	}


	/**
	 * @return the log
	 */
	public ILogger getLog() {
		return log;
	}

	public void unbindLog() {
		this.log = null;
	}

	/**
	 * @param log the log to set
	 */
	public void bindLog(ILogger log) {
		this.log = log;
	}

}
