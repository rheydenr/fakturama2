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

import javax.money.MonetaryAmount;

import org.eclipse.nebula.widgets.formattedtext.ITextFormatter;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;

import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.misc.DataUtils;

/**
 *
 */
public class MoneyFormatter extends NumberFormatter implements ITextFormatter {
    
    public MoneyFormatter() {
        super();
        DecimalFormat format = (DecimalFormat) DataUtils.getInstance().getCurrencyFormat();
        setPatterns(((DecimalFormat) NumberFormat.getNumberInstance()).toPattern(), format.toPattern(), LocaleUtil.getInstance().getCurrencyLocale());
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
    
    @Override
    public String getDisplayString() {
        Double value = (Double) getValue();
        String retval = "";
        if(value != null) {
            retval = DataUtils.getInstance().doubleToFormattedPrice(value);
        } else {
            retval = super.getDisplayString();
        }
        return retval;
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
