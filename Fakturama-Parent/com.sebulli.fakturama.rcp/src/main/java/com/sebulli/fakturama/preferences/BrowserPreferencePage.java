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

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;

/**
 * Preference page for the document settings
 * 
 * @author Gerd Bartelt
 */
public class BrowserPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

	/**
	 * Constructor
	 */
	public BrowserPreferencePage() {
		super(GRID);

	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.BROWSER_PREFERENCE_PAGE);

		//T: Preference page "Webbrowser" - URL of the start page
		addField(new StringFieldEditor(Constants.PREFERENCES_GENERAL_WEBBROWSER_URL, msg.preferencesBrowserUrl, getFieldEditorParent()));
		
		//T: Preference page "Webbrowser" 
		addField(new ComboFieldEditor(Constants.PREFERENCES_BROWSER_TYPE, msg.preferencesBrowserType, new String[][] { { "---", "0" }, { "WebKit", "1" }, { "Mozilla", "2" }
			 }, getFieldEditorParent()));
		
		//T: Preference page "Webbrowser" 
		addField(new BooleanFieldEditor(Constants.PREFERENCES_BROWSER_SHOW_URL_BAR, msg.preferencesBrowserShowaddressbar, getFieldEditorParent()));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.preferencesGeneral;
	}
	
	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_WEBBROWSER_URL, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_BROWSER_TYPE, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_BROWSER_SHOW_URL_BAR, write);
		
	}
    @Synchronize
    public void loadUserValuesFromDB() {
        syncWithPreferencesFromDatabase(false);
    }

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
	    node.setDefault(Constants.PREFERENCES_GENERAL_WEBBROWSER_URL, "");
	    node.setDefault(Constants.PREFERENCES_BROWSER_TYPE, "0");
	    node.setDefault(Constants.PREFERENCES_BROWSER_SHOW_URL_BAR, true);
	}


}
