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

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the optional items settings
 * 
 * @author Gerd Bartelt
 */
public class OptionalItemsPreferencePage extends FieldEditorPreferencePage {

	
	/* TRANSLATORS: The placeholder indicates the bug-reporting address
    for this package.  Please add _another line_ saying
    "Report translation bugs to <...>\n" with the address for translation
    bugs (typically your translation team's web or email address).  */

    
    /**
     * 
     */
    public static final String PREFERENCES_OPTIONALITEMS_OPTIONALITEM_TEXT = "OPTIONALITEMS_OPTIONALITEM_TEXT";
    /**
     * 
     */
    public static final String PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT = "OPTIONALITEMS_PRICE_REPLACEMENT";
    /**
     * 
     */
    public static final String PREFERENCES_OPTIONALITEMS_REPLACE_PRICE = "OPTIONALITEMS_REPLACE_PRICE";
    /**
     * 
     */
    public static final String PREFERENCES_OPTIONALITEMS_USE = "OPTIONALITEMS_USE";
    @Inject
    @Translation
    protected Messages msg;
	
	
	/**
	 * Constructor
	 */
	public OptionalItemsPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.OPTIONAL_ITEMS_PREFERENCE_PAGE);

		//T: Preference page "Optional items" 
		addField(new BooleanFieldEditor(PREFERENCES_OPTIONALITEMS_USE, msg.preferencesOptionalitemsUse, getFieldEditorParent()));
		//T: Preference page "Optional items" 
		addField(new BooleanFieldEditor(PREFERENCES_OPTIONALITEMS_REPLACE_PRICE, msg.preferencesOptionalitemsReplaceprice, getFieldEditorParent()));
		//T: Preference page "Optional items" 
		addField(new StringFieldEditor(PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT, msg.preferencesOptionalitemsPricereplacement, getFieldEditorParent()));
		//T: Preference page "Optional items" 
		addField(new StringFieldEditor(PREFERENCES_OPTIONALITEMS_OPTIONALITEM_TEXT, msg.preferencesOptionalitemsLabel, getFieldEditorParent()));

	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
     */
    @Override
    public String getDescription() {
        return msg.preferencesOptionalitems;
    }

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_OPTIONALITEMS_USE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_OPTIONALITEMS_REPLACE_PRICE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_OPTIONALITEMS_OPTIONALITEM_TEXT, write);
		}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IEclipsePreferences node) {
		node.putBoolean(PREFERENCES_OPTIONALITEMS_USE, false);
		node.putBoolean(PREFERENCES_OPTIONALITEMS_REPLACE_PRICE, true);
		node.put(PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT, "---");
		//T: Preference page "Optional Items" - placeholder text for "optional item"
		node.put(PREFERENCES_OPTIONALITEMS_OPTIONALITEM_TEXT, msg.preferencesOptionalitemsItemlabel);
	}

}
