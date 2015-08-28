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

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;

/**
 * Preference page for the greetings
 * 
 * @author Gerd Bartelt
 */
public class ContactFormatPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

	/**
	 * Constructor
	 */
	public ContactFormatPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.CONTACT_FORMAT_PREFERENCE_PAGE);

		//T: Preference page "Contact Format" - label "Common Salutation"
		addField(new StringFieldEditor(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMMON, msg.preferencesContactFormatSalutation, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "Salutation for men"
		addField(new StringFieldEditor(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MR, msg.preferencesContactFormatSalutationMen, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "Salutation for woman"
		addField(new StringFieldEditor(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MS, msg.preferencesContactFormatSalutationWomen, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "Salutation for companies"
		addField(new StringFieldEditor(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY, msg.preferencesContactFormatSalutationCompany, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "Format of the address field"
		addField(new StringFieldEditor(Constants.PREFERENCES_CONTACT_FORMAT_ADDRESS, msg.preferencesContactFormatAddressfieldlabel, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "List of the countries whose names are not printed in the address label"
		addField(new StringFieldEditor(Constants.PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES, msg.preferencesContactFormatHidecountries, getFieldEditorParent()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
		return msg.preferencesContactFormatAddressfield;
	}

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	private void syncWithPreferencesFromDatabase(boolean write) {
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMMON, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MS, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_FORMAT_ADDRESS, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES, write);
	}
    
    @Override
    @Synchronize
    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
	    /*
	     * If the preferencesInDatabase is <null> then this method is launched from DefaultValuesInitializer.
	     * But at this point the preferencesInDatabase field isn't initialized (from LifeCycleManager),
	     * so that it could produce an ugly NPE. Therefore we test the value and only if it's present we initialize
	     * some properties from DB.
	     * The "loadOrSavePreferencesFromOrInDatabase" property is set in class PreferencesInDatabase (which
	     * in turn is called from LifeCycleManager). 
	     */
	    if(preferencesInDatabase != null) {
	        Boolean isWrite = (Boolean)context.get(PreferencesInDatabase.LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE);
	        syncWithPreferencesFromDatabase(BooleanUtils.toBoolean(isWrite));
	    }
    }

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	@Override
	public void setInitValues(IPreferenceStore node) {
		
		//T: Preference page "Contact Format" - Example format Strings (Common Salutation)
		node.setDefault(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMMON, msg.dataDefaultContactFormatSalutation);

		//T: Preference page "Contact Format" - Example format Strings (Salutation Men) - do not translate the placeholders
		node.setDefault(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MR, msg.dataDefaultContactFormatSalutationMen);

		//T: Preference page "Contact Format" - Example format Strings (Salutation Women) - do not translate the placeholders
		node.setDefault(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_MS, msg.dataDefaultContactFormatSalutationWomen);

		//T: Preference page "Contact Format" - Example format Strings (Salutation Company)
		node.setDefault(Constants.PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY, msg.dataDefaultContactFormatSalutation);
		
		//T: Preference page "Contact Format" - Example format Strings (Address format)
		node.setDefault(Constants.PREFERENCES_CONTACT_FORMAT_ADDRESS, "{company}<br>{title} {firstname} {lastname}<br>{street}<br>{countrycode}{zip} {city}<br>{country}");
		
		node.setDefault(Constants.PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES, msg.dataDefaultContactFormatExcludedcountries);
	}

}
