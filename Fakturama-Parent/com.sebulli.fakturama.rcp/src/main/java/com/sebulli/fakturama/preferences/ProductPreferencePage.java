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
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;

/**
 * Preference page for the product settings
 * 
 * @author Gerd Bartelt
 */
public class ProductPreferencePage extends FieldEditorPreferencePage {
    
    @Inject
    @Translation
    protected Messages msg;

    /**
	 * Constructor
	 */
	public ProductPreferencePage() {
		super(GRID);

	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		
//		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.PRODUCT_PREFERENCE_PAGE);

		//T: Preference page "Product" - Label "Use item No."
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_ITEMNR, msg.preferencesProductUseitemno, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use Quantity Unit"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_QUNIT, msg.preferencesProductUsequantityunit, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use description"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_DESCRIPTION, msg.preferencesProductUsedescription, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use gross or net or both"
		addField(new RadioGroupFieldEditor(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS, msg.preferencesProductNetorgrossprices, 3, new String[][] { 
				{ msg.productDataNet, "1" },
				{ msg.productDataGross, "2" },
				//T: Preference page "Product" - Label "Use both: net and gross"
				{ msg.preferencesProductNetandgross, "0" } }, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use scaled prices"
		addField(new ComboFieldEditor(Constants.PREFERENCES_PRODUCT_SCALED_PRICES, msg.preferencesProductScaledprices, new String[][] { { "--", "1" }, { "2", "2" }, { "3", "3" }, { "4", "4" },
				{ "5", "5" } }, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Possibility to select the VAT"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_VAT, msg.preferencesProductSelectvat, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use weight"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_WEIGHT, msg.preferencesProductUseweight, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use quantity"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_QUANTITY, msg.preferencesProductUsequantity, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use product picture"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_PICTURE, msg.preferencesProductUsepicture, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.preferencesProduct;
	}
	
	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_ITEMNR, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_QUNIT, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_DESCRIPTION, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_SCALED_PRICES, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_VAT, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_WEIGHT, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_QUANTITY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_PICTURE, write);
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_ITEMNR, true);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_QUNIT, false);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_DESCRIPTION, true);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS, 2);
		node.setDefault(Constants.PREFERENCES_PRODUCT_SCALED_PRICES, 1);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_VAT, true);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_WEIGHT, false);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_QUANTITY, true);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_PICTURE, true);
	}

}
