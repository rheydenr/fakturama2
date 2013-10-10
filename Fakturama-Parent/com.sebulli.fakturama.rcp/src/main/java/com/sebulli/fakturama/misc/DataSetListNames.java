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

package com.sebulli.fakturama.misc;

import static com.sebulli.fakturama.Translate._;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// TODO REFACTOR!!!
public enum DataSetListNames {
	NAMES;

	Properties listNames = new Properties();
	Properties lokalizedListNames = new Properties();

	/**
	 * Constructor Fill the list with name pairs
	 */
	DataSetListNames() {
		//T: Title of the list with country codes
		setNamePair("countrycodes_2", _("2 digit country codes"));
		//T: Title of the list with country codes
		setNamePair("countrycodes_3", _("3 digit country codes"));
		//T: Title of the list with billing accounts
		setNamePair("billing_accounts", _("Billing Accounts"));
	}

	/**
	 * Create 2 Lists, one with the localizes and one with the non-lokalized
	 * name.
	 * 
	 * @param name
	 * @param lokalizedName
	 */
	private void setNamePair(String name, String lokalizedName) {
		lokalizedListNames.setProperty(name, lokalizedName);
		listNames.setProperty(lokalizedName, name);
	}

	/**
	 * Return the localized list name by the name
	 * 
	 * @param name
	 * @return
	 */
	public String getLocalizedName(String name) {
		return lokalizedListNames.getProperty(name, name);
	}

	/**
	 * Return the list name by the localized name
	 * 
	 * @param name
	 * @return
	 */
	public String getName(String name) {
		return listNames.getProperty(name, name);
	}

	/**
	 * Return the list with the localized names as set
	 * 
	 * @return
	 */
	public Set<Map.Entry<String, String>> getLocalizedNames() {

		// Convert properties to set
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, String> listNames = new HashMap<String, String>((Map) lokalizedListNames);
		Set<Map.Entry<String, String>> propertySet = listNames.entrySet();

		return propertySet;
	}

	/**
	 * Returns, whether the a list with this name exists
	 * 
	 * @param name
	 *            of the List
	 * @return TRUE, if there is list with this name
	 */
	public boolean exists(String name) {
		return listNames.containsValue(name);
	}

}
