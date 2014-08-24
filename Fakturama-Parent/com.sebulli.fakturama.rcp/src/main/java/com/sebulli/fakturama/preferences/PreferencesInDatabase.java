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


/**
 * Write or read preference settings to or from the data base
 * 
 * @author Gerd Bartelt
 */
public class PreferencesInDatabase {

	/**
	 * Load one preference from the data base
	 * 
	 * @param key
	 *            The key of the preference value
	 */
	private static void loadPreferenceValue(String key) {
//		if (Data.INSTANCE.isExistingProperty(key))
//			Activator.getDefault().getPreferenceStore().setValue(key, Data.INSTANCE.getProperty(key));
	}

	/**
	 * Save one preference to the data base
	 * 
	 * @param key
	 *            The key of the preference value
	 */
	private static void savePreferenceValue(String key) {
//		String s = Activator.getDefault().getPreferenceStore().getString(key);
//		if (s != null && Data.INSTANCE != null)
//			Data.INSTANCE.setProperty(key, s);
	}

	/**
	 * Write to or read from the data base
	 * 
	 * @param key
	 *            The key to read or to write
	 * @param write
	 *            True, if the value should be written
	 */
	public static void syncWithPreferencesFromDatabase(String key, boolean write) {
		if (write)
			savePreferenceValue(key);
		else
			loadPreferenceValue(key);
	}

	/**
	 * Load or save all preference values from database of the following
	 * preference pages.
	 */
	public static void loadOrSavePreferencesFromOrInDatabase(boolean save) {
		ToolbarPreferencePage.syncWithPreferencesFromDatabase(save);
//		ContactFormatPreferencePage.syncWithPreferencesFromDatabase(save);
//		ContactPreferencePage.syncWithPreferencesFromDatabase(save);
//		DocumentPreferencePage.syncWithPreferencesFromDatabase(save);
		GeneralPreferencePage.syncWithPreferencesFromDatabase(save);
//		NumberRangeFormatPreferencePage.syncWithPreferencesFromDatabase(save);
//		NumberRangeValuesPreferencePage.syncWithPreferencesFromDatabase(save);
//		OfficePreferencePage.syncWithPreferencesFromDatabase(save);
		ProductPreferencePage.syncWithPreferencesFromDatabase(save);
//		WebShopImportPreferencePage.syncWithPreferencesFromDatabase(save);
//		YourCompanyPreferencePage.syncWithPreferencesFromDatabase(save);
//		ExportPreferencePage.syncWithPreferencesFromDatabase(save);
//		OptionalItemsPreferencePage.syncWithPreferencesFromDatabase(save);
//		WebShopAuthorizationPreferencePage.syncWithPreferencesFromDatabase(save);
//		BrowserPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthDialogContactsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthDialogProductsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthDialogTextsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthContactsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthDocumentsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthVouchersPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthVoucherItemsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthItemsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthListPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthPaymentsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthProductsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthShippingsPreferencePage.syncWithPreferencesFromDatabase(save);
//		ColumnWidthTextsPreferencePage.syncWithPreferencesFromDatabase(save);
	}

	/**
	 * Load all preference values from database of the following preference
	 * pages.
	 */
	public static void loadPreferencesFromDatabase() {
		loadOrSavePreferencesFromOrInDatabase(false);
	}

	/**
	 * Write all preference values to database of the following preference
	 * pages.
	 */
	public static void savePreferencesInDatabase() {
		loadOrSavePreferencesFromOrInDatabase(true);
	}

}
