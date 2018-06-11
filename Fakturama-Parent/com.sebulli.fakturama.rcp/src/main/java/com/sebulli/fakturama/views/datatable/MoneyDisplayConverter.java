/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.views.datatable;

import javax.money.MonetaryAmount;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

import com.sebulli.fakturama.misc.INumberFormatterService;

/**
 * Converter for displaying money values in a NatTable.
 */
public class MoneyDisplayConverter extends DisplayConverter {
	
//	@Inject
	private INumberFormatterService numberFormatterService;

    /**
     * Constructor.
     * @param numberFormatterService2 
     * 
     */
    public MoneyDisplayConverter(INumberFormatterService numberFormatterService) {
    	this.numberFormatterService = numberFormatterService;
    }

    public Object canonicalToDisplayValue(Object canonicalValue) {
        String retval = "";
        if (canonicalValue != null) {
            if(canonicalValue instanceof MonetaryAmount) {
                MonetaryAmount value = (MonetaryAmount)canonicalValue;
                retval = numberFormatterService.formatCurrency(value);
            } else {
                Double value = (Double) canonicalValue;
                retval = numberFormatterService.doubleToFormattedPrice(value);
            }
        }
        return retval;
    }

    public Object displayToCanonicalValue(Object displayValue) {
        String displayString = (String) displayValue;
        displayString = displayString.trim();
        return numberFormatterService.formattedPriceToDouble(displayString);
    }
}
