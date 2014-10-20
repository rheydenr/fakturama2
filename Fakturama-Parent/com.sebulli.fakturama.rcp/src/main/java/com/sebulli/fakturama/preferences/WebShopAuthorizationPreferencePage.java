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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;

/**
 * Preference page for the webshop settings
 * 
 * @author Gerd Bartelt
 */
public class WebShopAuthorizationPreferencePage extends FieldEditorPreferencePage {
 
    @Inject
    @Translation
    protected Messages msg;

	/**
	 * Constructor
	 */
	public WebShopAuthorizationPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.WEBSHOP_AUTHORIZATION_PREFERENCE_PAGE);

		//T: Preference page "Web Shop Import" - Label checkbox "web shop enabled"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED, msg.preferencesWebshopAuthorizationPasswordproteced, getFieldEditorParent()));
		
		//T: Preference page "Web Shop Import" - Label
		addField(new StringFieldEditor(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_USER, msg.preferencesWebshopUser, getFieldEditorParent()));

		//T: Preference page "Web Shop Import" - Label
		StringFieldEditor passwordEditor = new StringFieldEditor(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD, msg.preferencesWebshopPassword, getFieldEditorParent());
		passwordEditor.getTextControl(getFieldEditorParent()).setEchoChar('*');
		addField(passwordEditor);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.preferencesWebshopAuthorization;
	}

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_USER, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD, write);
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
		node.setDefault(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED, false);
		node.setDefault(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_USER, "user");
		node.setDefault(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD, "password");
	}
	
}
