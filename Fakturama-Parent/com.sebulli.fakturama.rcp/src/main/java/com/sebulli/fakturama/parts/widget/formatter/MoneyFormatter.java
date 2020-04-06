/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.parts.widget.formatter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.widgets.formattedtext.ITextFormatter;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.ULocale;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.INumberFormatterService;

/**
 *
 */
public class MoneyFormatter extends NumberFormatter implements ITextFormatter {
	
    @Inject @Optional
    protected IPreferenceStore defaultValuePrefs;
    
	@Inject
	private ILocaleService localeUtil;
    
	@Inject
	private INumberFormatterService numberFormatterService;

	public MoneyFormatter() {
		super();
	}

    @PostConstruct
    public void init() {
        DecimalFormat format = (DecimalFormat) numberFormatterService.getCurrencyFormat();
        // the edit pattern has to be a normal number pattern
        DecimalFormat editFormat = (DecimalFormat) NumberFormat.getNumberInstance(localeUtil.getCurrencyLocale());
        editFormat.setMaximumIntegerDigits(6);
        if(defaultValuePrefs != null) {
        	format.setMinimumFractionDigits(defaultValuePrefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES));
        	editFormat.setMaximumFractionDigits(defaultValuePrefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES));
        }
        // add some more digits before the decimal point (default is 4 digits, this is too less)
        // Because the content is not interpreted by DecimalFormat, but by Formatter (nebula),
        // we can't use a normal currency pattern.
        String editFormatPattern = "-###,##" + editFormat.toPattern();
        //RHE: I don't know why I wrote this, really! Perhaps sometimes I get a lightning in my mind...
//        if(editFormat.getMaximumFractionDigits() > 2) {
//        	editFormatPattern += StringUtils.repeat("#", editFormat.getMaximumFractionDigits()-2);
//        }
        setPatterns(StringUtils.substringBefore(editFormatPattern, ";"), format.toPattern(), localeUtil.getCurrencyLocale().toLocale());
//        setFixedLengths(false, true);
    }
    
    @Override
    public void setValue(Object value) {
        if(value != null && value instanceof MonetaryAmount) {
            super.setValue(((MonetaryAmount)value).getNumber());
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        Number val = (Number) super.getValue();
        if ( val != null ) {
            val = new Double(val.doubleValue());
        }
        return val;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.formattedtext.NumberFormatter#getDisplayString()
     */
    @Override
    public String getDisplayString() {
    	return numberFormatterService.doubleToFormattedPrice((Double) getValue());
    }

    /**
     * Sets the patterns and initializes the technical attributes used to manage
     * the operations.
     * <p>
     * Override the NumberFormatter implementation to add the currency symbol to
     * the masks.
     * 
     * @param edit
     *            edit pattern
     * @param display
     *            display pattern
     * @param loc
     *            ULocale to use
     * @throws IllegalArgumentException
     *             if a pattern is invalid
     */
    protected void setPatterns(String edit, String display, ULocale loc) {
        super.setPatterns(edit, display, loc.toLocale());
    }
}
