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

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.dao.PropertiesDAO;
import com.sebulli.fakturama.model.UserProperty;


/**
 * Write or read preference settings to or from the data base
 * 
 */
public class PreferencesInDatabase {

    public static final String LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE = "loadOrSavePreferencesFromOrInDatabase";

    @Inject
    private IPreferenceStore preferences;
    
    @Inject
    @Preference
    private IEclipsePreferences pref;
    
    @Inject
    private PropertiesDAO propertiesDAO;
    
    @Inject
    private IEclipseContext context;

	/**
	 * Load one preference from the data base
	 * 
	 * @param key
	 *            The key of the preference value
	 */
	private void loadPreferenceValue(String key) {
	    UserProperty property = propertiesDAO.findByName(key);
	    if(property != null) {
	        preferences.setValue(property.getName(), property.getValue());
	        pref.put(property.getName(), property.getValue());
	    }
	}

	/**
	 * Save one preference to the data base
	 * 
	 * @param key
	 *            The key of the preference value
	 */
	private void savePreferenceValue(String key) {
		String s = preferences.getString(key);
		if (s != null && propertiesDAO != null)
		    propertiesDAO.setProperty(key, s);
	}

	/**
	 * Write to or read from the data base
	 * 
	 * @param key
	 *            The key to read or to write
	 * @param write
	 *            True, if the value should be written
	 */
	public void syncWithPreferencesFromDatabase(String key, boolean write) {
		if (write)
			savePreferenceValue(key);
		else
			loadPreferenceValue(key);
	}

	/**
	 * Load or save all preference values from database of the following
	 * preference pages.
	 */
	public void loadOrSavePreferencesFromOrInDatabase(boolean save) {
        List<Class<? extends IInitializablePreference>> classesToInit = new ArrayList<>();
        classesToInit.add(ContactFormatPreferencePage.class);
        classesToInit.add(DocumentPreferencePage.class);
        classesToInit.add(NumberRangeFormatPreferencePage.class);
        classesToInit.add(WebShopImportPreferencePage.class);
        classesToInit.add(OptionalItemsPreferencePage.class);
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
        
        context.set(LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE, save);
        // Initialize every single preference page
        for (Class<? extends IInitializablePreference> clazz : classesToInit) {
            IInitializablePreference p = ContextInjectionFactory.make(clazz, context);
            ContextInjectionFactory.invoke(p, Synchronize.class, context);
        }
	}

	/**
	 * Load all preference values from database of the following preference
	 * pages.
	 */
	public void loadPreferencesFromDatabase() {
		loadOrSavePreferencesFromOrInDatabase(false);
	}

	/**
	 * Write all preference values to database of the following preference
	 * pages.
	 */
	public void savePreferencesInDatabase() {
		loadOrSavePreferencesFromOrInDatabase(true);
	}

}
