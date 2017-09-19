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
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

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
    	try {
			return sdf.parse((String) displayValue);
		} catch (ParseException e) {
			return "00.00.0000";
		}
    }

}
