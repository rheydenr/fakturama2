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

import javax.inject.Inject;

import org.eclipse.jface.viewers.LabelProvider;

import com.sebulli.fakturama.i18n.ILocaleService;

/**
 * This {@link LabelProvider} shows a String value which is mapped to a String
 * key. If the element list contains {@link Locale} objects or the given element
 * is <code>null</code> then the current country is displayed (according to
 * defaultLocale).
 */
public class StringComboBoxLabelProvider extends LabelProvider {
    
	@Inject
	private ILocaleService localeUtil;

    /**
     * @param countryNames the possible label values that this 
     * <code>ILabelProvider</code> may return.
     */
    private Map<String, String> countryNames;
//
//    public StringComboBoxLabelProvider(Map<String, String> countryNames, ILocaleService localeUtil) {
//        this.countryNames = countryNames;
//        this.localeUtil = localeUtil;
//    }

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
            retval = countryNames.get(element);
        } else {
            retval = countryNames.get(localeUtil.getDefaultLocale().getLanguage());
        }
        
        return retval;
    }

	public Map<String, String> getCountryNames() {
		return countryNames;
	}

	public void setCountryNames(Map<String, String> countryNames) {
		this.countryNames = countryNames;
	}
}
