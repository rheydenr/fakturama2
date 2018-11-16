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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.log.ILogger;

/**
 * Initializes the preference pages with default values
 * 
 */
public class DefaultValuesInitializer extends AbstractPreferenceInitializer {
    
    private List<IInitializablePreference> classesToInit = new ArrayList<>();
    
    /**
     * Register an initializable preference page to this Initializer.
     *  
     * @param preferencePage
     */
    public void registerPreferencePage(IInitializablePreference preferencePage) {
        classesToInit.add(preferencePage);
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
		ILogger log = EclipseContextFactory.getServiceContext(Activator.getContext()).get(ILogger.class);
		log.info("Entering default Preference Initializer");
		IPreferenceStore defaultValuesNode = FakturamaPreferenceStoreProvider.getInstance().getPreferenceStore(); 		

// TODO Later on we use registered preference pages which register itself on a registry:
//		for (IInitializablePreference iInitializablePreference : classesToInit) {
//		    iInitializablePreference.setInitValues(defaultValuesNode);
//		}

		// But for now we use the old style way...	
		List<Class<? extends IInitializablePreference>> classesToInit = new ArrayList<>();
		classesToInit.add(ContactFormatPreferencePage.class);
        classesToInit.add(DocumentPreferencePage.class);
        classesToInit.add(NumberRangeFormatPreferencePage.class);
        classesToInit.add(WebShopImportPreferencePage.class);
        classesToInit.add(OptionalItemsPreferencePage.class);
        classesToInit.add(ContactFormatPreferencePage.class);
        classesToInit.add(ToolbarPreferencePage.class);
        classesToInit.add(ContactPreferencePage.class);
        classesToInit.add(GeneralPreferencePage.class);
        classesToInit.add(NumberRangeValuesPreferencePage.class);
        classesToInit.add(OfficePreferencePage.class);
        classesToInit.add(ProductPreferencePage.class);
        classesToInit.add(YourCompanyPreferencePage.class);
        classesToInit.add(ExportPreferencePage.class);
        classesToInit.add(WebShopAuthorizationPreferencePage.class);
        classesToInit.add(BrowserPreferencePage.class);
		
	    // Initialize every single preference page
		for (Class<? extends IInitializablePreference> clazz : classesToInit) {
		    IInitializablePreference p = ContextInjectionFactory.make(clazz, EclipseContextFactory.getServiceContext(Activator.getContext()));
    		p.setInitValues(defaultValuesNode);
        }

		EclipseContextFactory.getServiceContext(Activator.getContext()).set(IPreferenceStore.class, defaultValuesNode);
	}
}
