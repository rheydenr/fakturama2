/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.money;

/**
 * All possible values for currency settings
 *
 */
public enum CurrencySettingEnum {

	/**
	 * don't use any code or symbol 
	 */
	NONE,
	
	/**
	 * use currency symbol
	 */
	SYMBOL,
	
	/**
	 * use currency ISO code
	 */
	CODE,
	
	// these are from JavaMoney CurrencyStyle
	NAME,NUMERIC_CODE
}
