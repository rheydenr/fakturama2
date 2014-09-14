/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.preference.IPreferenceStore;

import com.opcoach.e4.preferences.ScopedPreferenceStore;

/**
 * Initializes the preference pages with default values
 * 
 * @author Gerd Bartelt
 */
public class DefaultValuesInitializer extends AbstractPreferenceInitializer {

    private Logger log;
    
    public DefaultValuesInitializer() {}
    
	/**
     * @param log
     */
    public DefaultValuesInitializer(Logger log) {
        this.log = log;
    }

    /**
	 * This method is called by the preference initializer to initialize default
	 * preference values. Clients should get the correct node for their bundle
	 * and then set the default values on it. The default values are only in memory 
	 * and not persisted somewhere! Only the "real" values are persisted in PreferenceStore.
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
	 *      #initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() { 
//        log.debug("Enter in default Preference Initializer");
	    IPreferenceStore defaultValuesNode = new ScopedPreferenceStore(InstanceScope.INSTANCE, "com.sebulli.fakturama.preferences");   

		// Initialize every single preference page
		ToolbarPreferencePage.setInitValues(defaultValuesNode);
		ContactPreferencePage.setInitValues(defaultValuesNode);
// TODO static!!!		ContactFormatPreferencePage.setInitValues(node);
// TODO static!!!			DocumentPreferencePage.setInitValues(node);
		GeneralPreferencePage.setDefaultValues(defaultValuesNode);
//		NumberRangeValuesPreferencePage.setInitValues(node);
//		NumberRangeFormatPreferencePage.setInitValues(node);
		OfficePreferencePage.setInitValues(defaultValuesNode);
		ProductPreferencePage.setInitValues(defaultValuesNode);
// TODO static!!!    WebShopImportPreferencePage.setInitValues(node);
		YourCompanyPreferencePage.setInitValues(defaultValuesNode);
		ExportPreferencePage.setInitValues(defaultValuesNode);
//		OptionalItemsPreferencePage.setInitValues(node);
		WebShopAuthorizationPreferencePage.setInitValues(defaultValuesNode);
		BrowserPreferencePage.setInitValues(defaultValuesNode);
	}
}
