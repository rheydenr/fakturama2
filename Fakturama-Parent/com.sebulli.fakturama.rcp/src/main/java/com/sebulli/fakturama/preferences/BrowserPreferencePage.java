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

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the document settings
 * 
 * @author Gerd Bartelt
 */
public class BrowserPreferencePage extends FieldEditorPreferencePage {
    
    /**
     * 
     */
    public static final String PREFERENCES_BROWSER_SHOW_URL_BAR = "BROWSER_SHOW_URL_BAR";
    /**
     * 
     */
    public static final String PREFERENCES_BROWSER_TYPE = "BROWSER_TYPE";
    /**
     * 
     */
    public static final String PREFERENCES_GENERAL_WEBBROWSER_URL = "GENERAL_WEBBROWSER_URL";

    @Inject
    @Translation
    protected Messages msg;

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
		addField(new StringFieldEditor(PREFERENCES_GENERAL_WEBBROWSER_URL, msg.preferencesBrowserUrl, getFieldEditorParent()));
		
		//T: Preference page "Webbrowser" 
		addField(new ComboFieldEditor(PREFERENCES_BROWSER_TYPE, msg.preferencesBrowserType, new String[][] { { "---", "0" }, { "WebKit", "1" }, { "Mozilla", "2" }
			 }, getFieldEditorParent()));
		
		//T: Preference page "Webbrowser" 
		addField(new BooleanFieldEditor(PREFERENCES_BROWSER_SHOW_URL_BAR, msg.preferencesBrowserShowaddressbar, getFieldEditorParent()));

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
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_GENERAL_WEBBROWSER_URL, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_BROWSER_TYPE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_BROWSER_SHOW_URL_BAR, write);
		
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public static void setInitValues(IPreferenceStore node) {
//		node.setDefault(PREFERENCES_GENERAL_WEBBROWSER_URL, OpenBrowserEditorAction.FAKTURAMA_PROJECT_URL);
	    node.setDefault(PREFERENCES_GENERAL_WEBBROWSER_URL, "");
	    node.setDefault(PREFERENCES_BROWSER_TYPE, "0");
	    node.setDefault(PREFERENCES_BROWSER_SHOW_URL_BAR, true);
	}


}
