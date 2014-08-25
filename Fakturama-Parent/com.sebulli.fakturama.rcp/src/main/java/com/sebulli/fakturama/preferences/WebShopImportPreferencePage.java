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
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the webshop settings
 * 
 * @author Gerd Bartelt
 */
public class WebShopImportPreferencePage extends FieldEditorPreferencePage {

    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR = "WEBSHOP_USE_EAN_AS_ITEMNR";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS = "WEBSHOP_ONLY_MODIFIED_PRODUCTS";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_MAX_PRODUCTS = "WEBSHOP_MAX_PRODUCTS";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_NOTIFY_SHIPPED = "WEBSHOP_NOTIFY_SHIPPED";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_NOTIFY_PROCESSING = "WEBSHOP_NOTIFY_PROCESSING";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_SHIPPING_CATEGORY = "WEBSHOP_SHIPPING_CATEGORY";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_CONTACT_CATEGORY = "WEBSHOP_CONTACT_CATEGORY";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_PRODUCT_CATEGORY = "WEBSHOP_PRODUCT_CATEGORY";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_PASSWORD = "WEBSHOP_PASSWORD";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_USER = "WEBSHOP_USER";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_URL = "WEBSHOP_URL";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_ENABLED = "WEBSHOP_ENABLED";

    @Inject
    @Translation
    protected Messages msg;

	/**
	 * Constructor
	 */
	public WebShopImportPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.WEBSHOP_IMPORT_PREFERENCE_PAGE);

		//T: Preference page "Web Shop Import" - Label checkbox "web shop enabled"
		addField(new BooleanFieldEditor(PREFERENCES_WEBSHOP_ENABLED, msg.preferencesWebshopEnabled, getFieldEditorParent()));
		
		//T: Preference page "Web Shop Import" - Label
		addField(new StringFieldEditor(PREFERENCES_WEBSHOP_URL, msg.preferencesWebshopUrl, getFieldEditorParent()));

		//T: Preference page "Web Shop Import" - Label
		addField(new StringFieldEditor(PREFERENCES_WEBSHOP_USER, msg.preferencesWebshopUser, getFieldEditorParent()));

		//T: Preference page "Web Shop Import" - Label
		StringFieldEditor passwordEditor = new StringFieldEditor(PREFERENCES_WEBSHOP_PASSWORD, msg.preferencesWebshopPassword, getFieldEditorParent());
		passwordEditor.getTextControl(getFieldEditorParent()).setEchoChar('*');
		addField(passwordEditor);

		//T: Preference page "Web Shop Import" - Label
		addField(new StringFieldEditor(PREFERENCES_WEBSHOP_PRODUCT_CATEGORY, msg.preferencesWebshopLabelProductsincategory, getFieldEditorParent()));

		//T: Preference page "Web Shop Import" - Label
		addField(new StringFieldEditor(PREFERENCES_WEBSHOP_CONTACT_CATEGORY, msg.preferencesWebshopLabelCustomersincategory, getFieldEditorParent()));

		//T: Preference page "Web Shop Import" - Label
		addField(new StringFieldEditor(PREFERENCES_WEBSHOP_SHIPPING_CATEGORY, msg.preferencesWebshopLabelShippingsincategory, getFieldEditorParent()));

		//T: Preference page "Web Shop Import" - Label
		addField(new BooleanFieldEditor(PREFERENCES_WEBSHOP_NOTIFY_PROCESSING, msg.preferencesWebshopNoifycustomerOnprogress, getFieldEditorParent()));
		//T: Preference page "Web Shop Import" - Label
		addField(new BooleanFieldEditor(PREFERENCES_WEBSHOP_NOTIFY_SHIPPED, msg.preferencesWebshopNoifycustomerOnshipped, getFieldEditorParent()));
		//T: Preference page "Web Shop Import" - Label
		addField(new IntegerFieldEditor(PREFERENCES_WEBSHOP_MAX_PRODUCTS, msg.preferencesWebshopMaxproducts, getFieldEditorParent()));
		//T: Preference page "Web Shop Import" - Label
		addField(new BooleanFieldEditor(PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS, msg.preferencesWebshopModifiedproducts, getFieldEditorParent()));
		//T: Preference page "Web Shop Import" - Label
		addField(new BooleanFieldEditor(PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR, msg.preferencesWebshopEanasitemno, getFieldEditorParent()));

	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.preferencesWebshop;
	}


	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_ENABLED, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_URL, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_USER, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_PASSWORD, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_PRODUCT_CATEGORY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_CONTACT_CATEGORY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_SHIPPING_CATEGORY, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_NOTIFY_PROCESSING, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_NOTIFY_SHIPPED, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_MAX_PRODUCTS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR, write);
		
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
		node.setDefault(PREFERENCES_WEBSHOP_ENABLED, true);
		
		//T: Preference page "Web Shop Import" - Country specific URL of demo shop
		node.setDefault(PREFERENCES_WEBSHOP_URL, msg.preferencesWebshopDefaulturl);
		node.setDefault(PREFERENCES_WEBSHOP_USER, "user");
		node.setDefault(PREFERENCES_WEBSHOP_PASSWORD, "password");
		//T: Preference page "Web Shop Import" - Default value "Product Category"
		node.setDefault(PREFERENCES_WEBSHOP_PRODUCT_CATEGORY, msg.preferencesWebshop);
		//T: Preference page "Web Shop Import" - Default value "Contact Category"
		node.setDefault(PREFERENCES_WEBSHOP_CONTACT_CATEGORY, msg.preferencesWebshopCustomer);
		//T: Preference page "Web Shop Import" - Default value "Shipping Category"
		node.setDefault(PREFERENCES_WEBSHOP_SHIPPING_CATEGORY, msg.preferencesWebshop);
		node.setDefault(PREFERENCES_WEBSHOP_NOTIFY_PROCESSING, false);
		node.setDefault(PREFERENCES_WEBSHOP_NOTIFY_SHIPPED, true);
		node.setDefault(PREFERENCES_WEBSHOP_MAX_PRODUCTS, "1000");
		node.setDefault(PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS, false);
		node.setDefault(PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR, false);
		
	}
	
}
