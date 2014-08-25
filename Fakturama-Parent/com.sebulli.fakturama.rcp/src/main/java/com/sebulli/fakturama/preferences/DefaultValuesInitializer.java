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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.preference.IPreferenceStore;

import com.opcoach.e4.preferences.ScopedPreferenceStore;

/**
 * Initializes the preference pages with default values
 * 
 * @author Gerd Bartelt
 */
public class DefaultValuesInitializer /*extends AbstractPreferenceInitializer */{

    private Logger log;
    
	/**
     * @param log
     */
    public DefaultValuesInitializer(Logger log) {
        this.log = log;
    }



    /**
	 * This method is called by the preference initializer to initialize default
	 * preference values. Clients should get the correct node for their bundle
	 * and then set the default values on it.
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
	 *      #initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
        IPreferenceStore node = new ScopedPreferenceStore(InstanceScope.INSTANCE, "com.sebulli.fakturama.preferences");   
        log.debug("Enter in default Preference Initializer");

		// Initialize every single preference page
		ToolbarPreferencePage.setInitValues(node);
		ContactPreferencePage.setInitValues(node);
// TODO static!!!		ContactFormatPreferencePage.setInitValues(node);
// TODO static!!!			DocumentPreferencePage.setInitValues(node);
		GeneralPreferencePage.setDefaultValues(node);
//		NumberRangeValuesPreferencePage.setInitValues(node);
//		NumberRangeFormatPreferencePage.setInitValues(node);
		OfficePreferencePage.setInitValues(node);
		ProductPreferencePage.setInitValues(node);
// TODO static!!!    WebShopImportPreferencePage.setInitValues(node);
		YourCompanyPreferencePage.setInitValues(node);
		ExportPreferencePage.setInitValues(node);
//		OptionalItemsPreferencePage.setInitValues(node);
		WebShopAuthorizationPreferencePage.setInitValues(node);
		BrowserPreferencePage.setInitValues(node);
//
//		ColumnWidthDialogContactsPreferencePage.setInitValues(node);
//		ColumnWidthDialogProductsPreferencePage.setInitValues(node);
//		ColumnWidthDialogTextsPreferencePage.setInitValues(node);
//
//		ColumnWidthContactsPreferencePage.setInitValues(node);
//		ColumnWidthDocumentsPreferencePage.setInitValues(node);
//		ColumnWidthVouchersPreferencePage.setInitValues(node);
//		ColumnWidthVoucherItemsPreferencePage.setInitValues(node);
//		ColumnWidthItemsPreferencePage.setInitValues(node);
//		ColumnWidthListPreferencePage.setInitValues(node);
//		ColumnWidthPaymentsPreferencePage.setInitValues(node);
//		ColumnWidthProductsPreferencePage.setInitValues(node);
//		ColumnWidthShippingsPreferencePage.setInitValues(node);
//		ColumnWidthTextsPreferencePage.setInitValues(node);

		
	}
}
