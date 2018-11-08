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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.widgets.formattedtext.ITextFormatter;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;

import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.INumberFormatterService;

/**
 *
 */
public class DoubleValueFormatter extends NumberFormatter implements ITextFormatter {
	
    @Inject @Optional
    protected IPreferenceStore defaultValuePrefs;
    
	@Inject
	private ILocaleService localeUtil;
    
	@Inject
	private INumberFormatterService numberFormatterService;

    @PostConstruct
    public void init() {
        DecimalFormat format = new DecimalFormat();
        // the edit pattern has to be a normal number pattern
        DecimalFormat editFormat = (DecimalFormat) NumberFormat.getNumberInstance(localeUtil.getDefaultLocale());
        editFormat.setMaximumIntegerDigits(32);
        format.setMaximumIntegerDigits(32);
        if(defaultValuePrefs != null) {
        	format.setMinimumFractionDigits(defaultValuePrefs.getInt(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES));
        	editFormat.setMaximumFractionDigits(defaultValuePrefs.getInt(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES));
        	format.setGroupingUsed(defaultValuePrefs.getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR));
        }
        // add some more digits before the decimal point (default is 4 digits, this is too less)
        // Because the content is not interpreted by DecimalFormat, but by Formatter (nebula),
        // we can't use a normal currency pattern.
        String editFormatPattern =/* "###,##" + */editFormat.toPattern();
        setPatterns(StringUtils.substringBefore(editFormatPattern, ";"), format.toPattern(), localeUtil.getDefaultLocale());
        setFixedLengths(false, true);
    }
//    
//    @Override
//    public void setValue(Object value) {
//        if(value != null && value instanceof Number) {
//            super.setValue(((MonetaryAmount)value).getNumber());
//        } else {
//            super.setValue(value);
//        }
//    }

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
    	Object widgetValue = getValue();
    	Double convertedWidgetValue = null;
    	if(widgetValue != null) {
			if (widgetValue instanceof Integer) {
				convertedWidgetValue = ((Integer) widgetValue) / 1.0;
			} else if (widgetValue instanceof Double) {
				convertedWidgetValue = (Double) widgetValue;
			} else {
				convertedWidgetValue = Double.valueOf(0.0);
			}
    	}
    	return numberFormatterService.doubleToFormattedQuantity(convertedWidgetValue);
    }
//
//    /**
//     * Sets the patterns and initializes the technical attributes used to manage
//     * the operations.
//     * <p>
//     * Override the NumberFormatter implementation to add the currency symbol to
//     * the masks.
//     * 
//     * @param edit
//     *            edit pattern
//     * @param display
//     *            display pattern
//     * @param loc
//     *            Locale to use
//     * @throws IllegalArgumentException
//     *             if a pattern is invalid
//     */
//    protected void setPatterns(String edit, String display, Locale loc) {
//        super.setPatterns(edit, display, loc);
//    }
}
