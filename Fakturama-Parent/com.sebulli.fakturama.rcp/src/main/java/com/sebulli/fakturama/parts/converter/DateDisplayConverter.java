/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.parts.converter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

import com.ibm.icu.util.Calendar;

/**
 *
 */
public class DateDisplayConverter extends DisplayConverter  {
	
	DateFormat sdf = SimpleDateFormat.getDateInstance();

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#canonicalToDisplayValue(java.lang.Object)
	 */
	@Override
	public Object canonicalToDisplayValue(Object canonicalValue) {
		String retval = "";
        if (canonicalValue != null) {
        	retval = sdf.format(canonicalValue);
        }
        return retval; //$NON-NLS-1$
    }

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
    	Date retval = Calendar.getInstance().getTime();
        String displayString = (String) displayValue;
        displayString = displayString.trim();
        if (displayString.endsWith("%")) { //$NON-NLS-1$
            displayString = displayString.substring(0,
                    displayString.length() - 1);
        }
        displayString = displayString.trim();
        int displayInt = Integer.valueOf(displayString).intValue();
        double percentageValue = (double) displayInt / 100;
        return retval;
    }

}
