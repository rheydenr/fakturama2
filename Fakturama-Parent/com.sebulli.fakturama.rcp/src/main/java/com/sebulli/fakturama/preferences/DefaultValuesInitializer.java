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

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.preference.IPreferenceStore;

import com.opcoach.e4.preferences.ScopedPreferenceStore;
import com.sebulli.fakturama.i18n.Messages;

/**
 * Initializes the preference pages with default values
 * 
 * @author Gerd Bartelt
 */
public class DefaultValuesInitializer extends AbstractPreferenceInitializer {

    @Inject
    protected IEclipseContext context;

    private Messages msg;
    private Logger log;
    
    public DefaultValuesInitializer() {}
    
	/**
     * @param log
	 * @param msg 
     */
    public DefaultValuesInitializer(Logger log, Messages msg) {
        this.log = log;
        this.msg = msg;
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
		
        log.info("Enter in default Preference Initializer");
	    // look at Constants.DEFAULT_PREFERENCES_NODE)
	    IPreferenceStore defaultValuesNode = new ScopedPreferenceStore(InstanceScope.INSTANCE, "com.sebulli.fakturama.preferences");   

	    // TODO Later on we use registered preference pages which register itself on a registry.
	    // But for now we use the old style way...
	    
//		for (DefaultPreferencesInitializerListener listener : preferencesListeners) {
//			listener.setInitValues(defaultValuesNode);
//		}
	    
	    // Initialize every single preference page
		new ToolbarPreferencePage().setInitValues(defaultValuesNode);
		new ContactPreferencePage().setInitValues(defaultValuesNode);
		new ContactFormatPreferencePage().setInitValues(defaultValuesNode, msg);
		new DocumentPreferencePage().setInitValues(defaultValuesNode, msg);
		new GeneralPreferencePage().setDefaultValues(defaultValuesNode);
		new NumberRangeValuesPreferencePage().setInitValues(defaultValuesNode);
		new NumberRangeFormatPreferencePage().setInitValues(defaultValuesNode, msg);
		new OfficePreferencePage().setInitValues(defaultValuesNode);
		new ProductPreferencePage().setInitValues(defaultValuesNode);
		new WebShopImportPreferencePage().setInitValues(defaultValuesNode, msg);
		new YourCompanyPreferencePage().setInitValues(defaultValuesNode);
		new ExportPreferencePage().setInitValues(defaultValuesNode);
		new OptionalItemsPreferencePage().setInitValues(defaultValuesNode, msg);
		new WebShopAuthorizationPreferencePage().setInitValues(defaultValuesNode);
		new BrowserPreferencePage().setInitValues(defaultValuesNode);
	}
}
