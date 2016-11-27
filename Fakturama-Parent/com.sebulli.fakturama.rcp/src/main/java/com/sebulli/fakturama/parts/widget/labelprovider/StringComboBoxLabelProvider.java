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
 
package com.sebulli.fakturama.parts.widget.labelprovider;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;

import com.sebulli.fakturama.i18n.LocaleUtil;

/**
 * This {@link LabelProvider} shows a String value which is mapped to a String
 * key. If the element list contains {@link Locale} objects or the given element
 * is <code>null</code> then the current country is displayed (according to
 * defaultLocale).
 */
public class StringComboBoxLabelProvider extends LabelProvider {

    /**
     * The values.
     */
    private Map<String, String> values;

    /**
     * @param values the possible label values that this 
     * <code>ILabelProvider</code> may return.
     */
    public StringComboBoxLabelProvider(Map<String, String> values) {
        this.values = values;
    }

    /**
     * Returns the <code>String</code> that maps to the given 
     * <code>Integer</code>.
     * 
     * @param element an <code>Integer</code> object
     * @return a <code>String</code> from the provided values array, or the 
     * empty <code>String</code> 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        String retval = "";
        if (element != null && element instanceof String) {
            retval = values.get(element);
        } else {
            retval = values.get(LocaleUtil.getInstance().getDefaultLocale().getLanguage());
        }
        
        return retval;
    }
}
