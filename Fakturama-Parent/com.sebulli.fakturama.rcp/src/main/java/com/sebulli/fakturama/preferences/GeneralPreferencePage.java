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

import java.text.NumberFormat;
import java.util.Locale;

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;
//import com.sebulli.fakturama.ContextHelpConstants;
import com.sebulli.fakturama.misc.DataUtils;

/**
 * Preference page for the document settings. The preferences are written to the Fakturama common preferences store
 * (usually in {program}\.metadata\.plugins\org.eclipse.core.runtime\.settings\com.sebulli.fakturama.rcp.prefs
 * 
 * @author Gerd Bartelt
 */
public class GeneralPreferencePage extends FieldEditorPreferencePage {
    
    public static final String PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR = "GENERAL_HAS_THOUSANDS_SEPARATOR";
    public static final String PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR = "GENERAL_COLLAPSE_EXPANDBAR";
    public static final String PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS = "GENERAL_CLOSE_OTHER_EDITORS";
    public static final String PREFERENCE_GENERAL_CURRENCY = "GENERAL_CURRENCY";
    
    @Inject
    @Translation
    protected Messages msg;

	/**
	 * Constructor
	 */
	public GeneralPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.GENERAL_PREFERENCE_PAGE);

		//T: Preference page "General"
		addField(new BooleanFieldEditor(PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR, msg.preferencesGeneralCollapsenavbar, getFieldEditorParent()));

		//T: Preference page "General"
		addField(new BooleanFieldEditor(PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS, msg.preferencesGeneralCloseeditors, getFieldEditorParent()));

		//T: Preference page "General"
		addField(new StringFieldEditor(PREFERENCE_GENERAL_CURRENCY, msg.preferencesGeneralCurrency, getFieldEditorParent()));
		
		//T: Preference page "General"
		addField(new BooleanFieldEditor(PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, msg.preferencesGeneralThousendseparator, getFieldEditorParent()));
	}
	
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
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCE_GENERAL_CURRENCY, write);
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public static void setDefaultValues(IPreferenceStore node) {
		node.setDefault(PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR, false);
		node.setDefault(PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS, false);

		//Set the currency symbol of the default locale
		String currency = "$";
		try {
			NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
			currency = numberFormatter.getCurrency().getSymbol();
		}
		catch (Exception e) {
		}
		node.setDefault(PREFERENCE_GENERAL_CURRENCY, currency);
	}

	/**
	 * Update the currency Symbol for the whole application
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		DataUtils.updateCurrencySymbol();
		return super.performOk();
	}

	/**
	 * Update the currency Symbol for the whole application
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
		DataUtils.updateCurrencySymbol();
		super.performApply();
	}

	/**
	 * Update the currency Symbol for the whole application
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		DataUtils.updateCurrencySymbol();
		super.performDefaults();
	}

}
