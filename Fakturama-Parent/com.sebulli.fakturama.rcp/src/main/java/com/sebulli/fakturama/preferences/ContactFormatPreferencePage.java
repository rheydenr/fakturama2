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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the greetings
 * 
 * @author Gerd Bartelt
 */
public class ContactFormatPreferencePage extends FieldEditorPreferencePage {
    
    @Inject
    @Translation
    protected Messages msg;

	/**
     * 
     */
    public static final String PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES = "CONTACT_FORMAT_HIDE_COUNTRIES";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_FORMAT_ADDRESS = "CONTACT_FORMAT_ADDRESS";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY = "CONTACT_FORMAT_GREETING_COMPANY";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_FORMAT_GREETING_MS = "CONTACT_FORMAT_GREETING_MS";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_FORMAT_GREETING_MR = "CONTACT_FORMAT_GREETING_MR";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_FORMAT_GREETING_COMMON = "CONTACT_FORMAT_GREETING_COMMON";

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
		addField(new StringFieldEditor(PREFERENCES_CONTACT_FORMAT_GREETING_COMMON, msg.preferencesContactFormatSalutation, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "Salutation for men"
		addField(new StringFieldEditor(PREFERENCES_CONTACT_FORMAT_GREETING_MR, msg.preferencesContactFormatSalutationMen, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "Salutation for woman"
		addField(new StringFieldEditor(PREFERENCES_CONTACT_FORMAT_GREETING_MS, msg.preferencesContactFormatSalutationWomen, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "Salutation for companies"
		addField(new StringFieldEditor(PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY, msg.preferencesContactFormatSalutationCompany, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "Format of the address field"
		addField(new StringFieldEditor(PREFERENCES_CONTACT_FORMAT_ADDRESS, msg.preferencesContactFormatAddressfieldlabel, getFieldEditorParent()));

		//T: Preference page "Contact Format" - label "List of the countries whose names are not printed in the address label"
		addField(new StringFieldEditor(PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES, msg.preferencesContactFormatHidecountries, getFieldEditorParent()));

	}

/* (non-Javadoc)
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
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_FORMAT_GREETING_COMMON, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_FORMAT_GREETING_MR, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_FORMAT_GREETING_MS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_FORMAT_ADDRESS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES, write);
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
		
		//T: Preference page "Contact Format" - Example format Strings (Common Salutation)
		node.setDefault(PREFERENCES_CONTACT_FORMAT_GREETING_COMMON, msg.dataDefaultContactFormatSalutation);

		//T: Preference page "Contact Format" - Example format Strings (Salutation Men) - do not translate the placeholders
		node.setDefault(PREFERENCES_CONTACT_FORMAT_GREETING_MR, msg.dataDefaultContactFormatSalutationMen);

		//T: Preference page "Contact Format" - Example format Strings (Salutation Women) - do not translate the placeholders
		node.setDefault(PREFERENCES_CONTACT_FORMAT_GREETING_MS, msg.dataDefaultContactFormatSalutationWomen);

		//T: Preference page "Contact Format" - Example format Strings (Salutation Company)
		node.setDefault(PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY, msg.dataDefaultContactFormatSalutation);
		
		//T: Preference page "Contact Format" - Example format Strings (Address format)
		node.setDefault(PREFERENCES_CONTACT_FORMAT_ADDRESS, "{company}<br>{title} {firstname} {lastname}<br>{street}<br>{countrycode}{zip} {city}<br>{country}");
		
		//T: Preference page "Contact Format" - Example format Strings (Hidden countries)
		//T: Separate the country by a comma. 
		//T: If the county name is one in this list, it won't be displayed in the address
		//T: field. E.g. for a German language you should enter "Deutschland,Germany".
		//T: There should be at least 2 names, separated by a comma. So that the user
		//T: can see the format. Even if 2 countries don't make much sense like 
		//T: USA,U.S.A. for the English language.
		node.setDefault(PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES, msg.dataDefaultContactFormatExcludedcountries);

	}

}
