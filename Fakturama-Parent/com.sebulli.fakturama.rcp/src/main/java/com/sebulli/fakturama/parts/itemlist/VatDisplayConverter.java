/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.parts.itemlist;

import java.text.NumberFormat;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

import com.sebulli.fakturama.model.VAT;

/**
 * Converter for displaying {@link VAT} values in a combo box inside a NatTable.
 */
public class VatDisplayConverter extends DisplayConverter {
    
    private NumberFormat nf = NumberFormat.getPercentInstance();

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#canonicalToDisplayValue(java.lang.Object)
     */
    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        String retval = "";
        if (canonicalValue != null) {
            if (canonicalValue instanceof VAT) {
                double percentageValue = ((VAT) canonicalValue).getTaxValue();
                retval = nf.format(percentageValue);
            }
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#displayToCanonicalValue(java.lang.Object)
     */
    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        return null;  // TODO don't know where it is used...
    }

}
