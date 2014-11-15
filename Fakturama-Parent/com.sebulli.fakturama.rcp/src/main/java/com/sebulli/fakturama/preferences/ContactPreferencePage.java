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
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the contact settings
 * 
 * @author Gerd Bartelt
 */
public class ContactPreferencePage extends FieldEditorPreferencePage implements IPreferencesInitializerListener  {

	/**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_COUNTRY = "CONTACT_USE_COUNTRY";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_COMPANY = "CONTACT_USE_COMPANY";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_NAME_FORMAT = "CONTACT_NAME_FORMAT";
    /**
     * 
     */
    private static final String PREFERENCES_CONTACT_USE_TITLE = "CONTACT_USE_TITLE";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_GENDER = "CONTACT_USE_GENDER";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_NOTE = "CONTACT_USE_NOTE";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_MISC = "CONTACT_USE_MISC";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_BANK = "CONTACT_USE_BANK";
    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_DELIVERY = "CONTACT_USE_DELIVERY";

    @Inject
    @Translation
    protected Messages msg;

    /**
	 * Constructor
	 */
	public ContactPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.CONTACT_PREFERENCE_PAGE);

		//T: Preference page "Contact" - Label checkbox "Use delivery address"
		addField(new BooleanFieldEditor(PREFERENCES_CONTACT_USE_DELIVERY, msg.preferencesContactUsedelivery, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use bank account"
		addField(new BooleanFieldEditor(PREFERENCES_CONTACT_USE_BANK, msg.preferencesContactUsebankaccount, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use miscellaneous"
		addField(new BooleanFieldEditor(PREFERENCES_CONTACT_USE_MISC, msg.preferencesContactUsemiscpage, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use page notice"
		addField(new BooleanFieldEditor(PREFERENCES_CONTACT_USE_NOTE, msg.preferencesContactUsenotepage, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use gender"
		addField(new BooleanFieldEditor(PREFERENCES_CONTACT_USE_GENDER, msg.preferencesContactUsegender, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use title"
		addField(new BooleanFieldEditor(PREFERENCES_CONTACT_USE_TITLE, msg.preferencesContactUsetitle, getFieldEditorParent()));

		//T: Preference page "Contact" - Label format of the name
		addField(new RadioGroupFieldEditor(PREFERENCES_CONTACT_NAME_FORMAT, msg.preferencesContactNameformat, 2, 
		        new String[][] { { msg.preferencesContactFirstlastname, "0" },
				{ msg.preferencesContactLastfirstname, "1" } }, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use company field"
		addField(new BooleanFieldEditor(PREFERENCES_CONTACT_USE_COMPANY, msg.preferencesContactUsecompany, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use Country Field"
		addField(new BooleanFieldEditor(PREFERENCES_CONTACT_USE_COUNTRY, msg.preferencesContactUsecountry, getFieldEditorParent()));

	}

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
     */
    @Override
    public String getDescription() {
        return msg.preferencesContact;
    }

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_USE_DELIVERY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_USE_BANK, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_USE_MISC, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_USE_NOTE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_USE_GENDER, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_USE_TITLE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_NAME_FORMAT, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_USE_COMPANY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_CONTACT_USE_COUNTRY, write);
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.preferences.DefaultPreferencesInitializerListener#setInitValues(org.eclipse.jface.preference.IPreferenceStore)
	 */
	@Override
	public void setInitValues(IPreferenceStore node) {
		node.setDefault(PREFERENCES_CONTACT_USE_DELIVERY, true);
		node.setDefault(PREFERENCES_CONTACT_USE_BANK, false);
		node.setDefault(PREFERENCES_CONTACT_USE_MISC, true);
		node.setDefault(PREFERENCES_CONTACT_USE_NOTE, true);
		node.setDefault(PREFERENCES_CONTACT_USE_GENDER, true);
		node.setDefault(PREFERENCES_CONTACT_USE_TITLE, false);
		node.setDefault(PREFERENCES_CONTACT_NAME_FORMAT, "0");
		node.setDefault(PREFERENCES_CONTACT_USE_COMPANY, true);
		node.setDefault(PREFERENCES_CONTACT_USE_COUNTRY, true);
	}

}
