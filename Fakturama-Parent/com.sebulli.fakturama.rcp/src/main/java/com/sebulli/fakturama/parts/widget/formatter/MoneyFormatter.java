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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.widgets.formattedtext.ITextFormatter;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;

import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;

/**
 *
 */
public class MoneyFormatter extends NumberFormatter implements ITextFormatter {
	
    @Inject @Optional
    protected IPreferenceStore defaultValuePrefs;
    
    public MoneyFormatter() {
    	this(null);
    }
    
    @Inject
    public MoneyFormatter(IPreferenceStore defaultValuePrefs) {
        super();
        DecimalFormat format = (DecimalFormat) DataUtils.getInstance().getCurrencyFormat();
        DecimalFormat editFormat = (DecimalFormat) NumberFormat.getNumberInstance(LocaleUtil.getInstance().getCurrencyLocale());
        if(defaultValuePrefs != null) {
        	format.setMinimumFractionDigits(defaultValuePrefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES));
        	editFormat.setMaximumFractionDigits(defaultValuePrefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES));
        }
        String editFormatPattern = editFormat.toPattern();
        if(editFormat.getMaximumFractionDigits() > 2) {
        	editFormatPattern += StringUtils.repeat("#", editFormat.getMaximumFractionDigits()-2);
        }
        setPatterns(editFormatPattern, format.toPattern(), LocaleUtil.getInstance().getCurrencyLocale());
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
    	return DataUtils.getInstance().doubleToFormattedPrice((Double) getValue());
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
     *            Locale to use
     * @throws IllegalArgumentException
     *             if a pattern is invalid
     */
    protected void setPatterns(String edit, String display, Locale loc) {
        super.setPatterns(edit, display, loc);
    }
}
