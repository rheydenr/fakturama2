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
 
package com.sebulli.fakturama.parts;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

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
    public Object getValue() {
        Number val = (Number) super.getValue();
        if ( val != null ) {
            val = new Double(val.doubleValue());
        }
        return val;
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
