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
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;

/**
 * Preference page for the contact settings
 * 
 * @author Gerd Bartelt
 */
public class ContactPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference  {

	@Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

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

		//T: Preference page "Contact" - Label checkbox "Use bank account"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_CONTACT_USE_BANK, msg.preferencesContactUsebankaccount, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use miscellaneous"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_CONTACT_USE_MISC, msg.preferencesContactUsemiscpage, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use page notice"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_CONTACT_USE_NOTE, msg.preferencesContactUsenotepage, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use gender"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_CONTACT_USE_GENDER, msg.preferencesContactUsegender, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use title"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_CONTACT_USE_TITLE, msg.preferencesContactUsetitle, getFieldEditorParent()));

		//T: Preference page "Contact" - Label format of the name
		addField(new RadioGroupFieldEditor(Constants.PREFERENCES_CONTACT_NAME_FORMAT, msg.preferencesContactNameformat, 2, 
		        new String[][] { { msg.preferencesContactFirstlastname, Integer.toString(Constants.CONTACT_FORMAT_FIRSTNAME_LASTNAME) },
				{ msg.preferencesContactLastfirstname, Integer.toString(Constants.CONTACT_FORMAT_LASTNAME_FIRSTNAME)  } }, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use company field"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_CONTACT_USE_COMPANY, msg.preferencesContactUsecompany, getFieldEditorParent()));

		//T: Preference page "Contact" - Label checkbox "Use Country Field"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_CONTACT_USE_COUNTRY, msg.preferencesContactUsecountry, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX, msg.preferencesDocumentUsesalesequalizationtax, getFieldEditorParent()));

	}

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_DELIVERY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_BANK, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_MISC, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_NOTE, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_GENDER, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_TITLE, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_NAME_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_COMPANY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_COUNTRY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX, write);
	}
    
    @Override
    @Synchronize
    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
        if(preferencesInDatabase != null) {
            Boolean isWrite = (Boolean)context.get(PreferencesInDatabase.LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE);
            syncWithPreferencesFromDatabase(BooleanUtils.toBoolean(isWrite));
        }
    }

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.preferences.DefaultPreferencesInitializerListener#setInitValues(org.eclipse.jface.preference.IPreferenceStore)
	 */
	@Override
	public void setInitValues(IPreferenceStore node) {
//	    getPreferenceStore(); geht nich :-(
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_DELIVERY, true);
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_BANK, false);
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_MISC, true);
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_NOTE, true);
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_GENDER, true);
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_TITLE, false);
		node.setDefault(Constants.PREFERENCES_CONTACT_NAME_FORMAT, "0");
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_COMPANY, true);
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_COUNTRY, true);
		node.setDefault(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX, false);
	}
}
