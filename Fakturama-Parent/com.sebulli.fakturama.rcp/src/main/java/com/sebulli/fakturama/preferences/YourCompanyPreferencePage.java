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
 * Preference page for the company settings
 * 
 * @author Gerd Bartelt
 */
public class YourCompanyPreferencePage extends FieldEditorPreferencePage {

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_CREDITORID = "YOURCOMPANY_CREDITORID";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_BIC = "YOURCOMPANY_COMPANY_BIC";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_IBAN = "YOURCOMPANY_COMPANY_IBAN";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_BANK = "YOURCOMPANY_COMPANY_BANK";
//    /**
//     * 
//     */
//    public static final String PREFERENCES_YOURCOMPANY_COMPANY_BANKCODE = "YOURCOMPANY_COMPANY_BANKCODE";
//    /**
//     * 
//     */
//    public static final String PREFERENCES_YOURCOMPANY_COMPANY_BANKACCOUNTNR = "YOURCOMPANY_COMPANY_BANKACCOUNTNR";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_TAXOFFICE = "YOURCOMPANY_COMPANY_TAXOFFICE";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_VATNR = "YOURCOMPANY_COMPANY_VATNR";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_WEBSITE = "YOURCOMPANY_COMPANY_WEBSITE";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_EMAIL = "YOURCOMPANY_COMPANY_EMAIL";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_FAX = "YOURCOMPANY_COMPANY_FAX";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_TEL = "YOURCOMPANY_COMPANY_TEL";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_COUNTRY = "YOURCOMPANY_COMPANY_COUNTRY";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_CITY = "YOURCOMPANY_COMPANY_CITY";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_ZIP = "YOURCOMPANY_COMPANY_ZIP";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_STREET = "YOURCOMPANY_COMPANY_STREET";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_OWNER = "YOURCOMPANY_COMPANY_OWNER";
    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_NAME = "YOURCOMPANY_COMPANY_NAME";
    @Inject
    @Translation
    protected Messages msg;

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
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_NAME, msg.preferencesYourcompanyName, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_OWNER, msg.preferencesYourcompanyOwner, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_STREET, msg.preferencesYourcompanyStreet, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_ZIP, msg.commonFieldZipcode, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_CITY, msg.commonFieldCity, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_COUNTRY, msg.commonFieldCountry, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_TEL, msg.exporterDataTelephone, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_FAX, msg.exporterDataTelefax, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_EMAIL, msg.exporterDataEmail, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_WEBSITE, msg.exporterDataWebsite, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_VATNR, msg.preferencesYourcompanyVatno, getFieldEditorParent()));

		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_TAXOFFICE, msg.preferencesYourcompanyTaxoffice, getFieldEditorParent()));

//		//T: Preference page "Your company"
//		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_BANKACCOUNTNR, msg.preferencesYourcompanyBankaccount, getFieldEditorParent()));
//		
//		//T: Preference page "Your company"
//		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_BANKCODE, msg.preferencesYourcompanyBankcode, getFieldEditorParent()));
		
		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_BANK, msg.preferencesYourcompanyBankname, getFieldEditorParent()));
		
		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_IBAN, msg.exporterDataIban, getFieldEditorParent()));
		
		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_COMPANY_BIC, msg.exporterDataBic, getFieldEditorParent()));
		
		//T: Preference page "Your company"
		addField(new StringFieldEditor(PREFERENCES_YOURCOMPANY_CREDITORID, msg.preferencesYourcompanyCreditorid, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.preferencesYourcompany;
	}

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_NAME, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_OWNER, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_STREET, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_ZIP, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_CITY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_COUNTRY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_TEL, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_FAX, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_EMAIL, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_WEBSITE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_VATNR, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_TAXOFFICE, write);
//		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_BANKACCOUNTNR, write);
//		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_BANKCODE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_BANK, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_IBAN, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_COMPANY_BIC, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_YOURCOMPANY_CREDITORID, write);
	}

    /**
     * @param node
     */
    public static void setInitValues(IPreferenceStore node) {
        // no values to initialize
        
    }

}
