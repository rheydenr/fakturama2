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
 * Preference page for the company settings
 * 
 * @author Gerd Bartelt
 */
public class YourCompanyPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

	/**
	 * Constructor
	 */
	public YourCompanyPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.YOUR_COMPANY_PREFERENCE_PAGE);

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_NAME, msg.pageCompanyName, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_OWNER, msg.pageCompanyOwner, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_STREET, msg.pageCompanyStreet, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_ZIP, msg.commonFieldZipcode, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_CITY, msg.commonFieldCity, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_COUNTRY, msg.commonFieldCountry, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_TEL, msg.exporterDataTelephone, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_FAX, msg.exporterDataTelefax, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_EMAIL, msg.exporterDataEmail, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_WEBSITE, msg.exporterDataWebsite, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_VATNR, msg.pageCompanyVatno, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_TAXOFFICE, msg.pageCompanyTaxoffice, getFieldEditorParent()));
		
		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_BANK, msg.pageCompanyBankname, getFieldEditorParent()));
		
		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_IBAN, msg.exporterDataIban, getFieldEditorParent()));
		
		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_COMPANY_BIC, msg.exporterDataBic, getFieldEditorParent()));
		
		//T: Preference page "Your company"
		addField(new StringFieldEditor(Constants.PREFERENCES_YOURCOMPANY_CREDITORID, msg.pageCompanyCreditorid, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.pageCompany;
	}

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_NAME, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_OWNER, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_STREET, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_ZIP, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_CITY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_COUNTRY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_TEL, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_FAX, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_EMAIL, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_WEBSITE, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_VATNR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_TAXOFFICE, write);
//		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_BANKACCOUNTNR, write);
//		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_BANKCODE, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_BANK, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_IBAN, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_COMPANY_BIC, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_YOURCOMPANY_CREDITORID, write);
	}

    @Override
    @Synchronize
    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
        if(preferencesInDatabase != null) {
            Boolean isWrite = (Boolean)context.get(PreferencesInDatabase.LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE);
            syncWithPreferencesFromDatabase(BooleanUtils.toBoolean(isWrite));
        }
    }

    /**
     * @param node
     */
    public void setInitValues(IPreferenceStore node) {
        // no values to initialize
        
    }

}

