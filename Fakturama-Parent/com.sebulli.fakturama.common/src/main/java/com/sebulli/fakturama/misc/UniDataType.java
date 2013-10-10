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

/**
 * Possible Types for class UniData
 * 
 * @author Gerd Bartelt
 */
public enum UniDataType {
	/**
	 * All types
	 */
	NONE, INT, ID, QUANTITY, BOOLEAN, DOUBLE, STRING, PRICE, PERCENT, DATE, TEXT;

	/**
	 * Test, if a type is a numeric one
	 * 
	 * @return True for numeric types
	 */
	public boolean isNumeric() {
		switch (this) {
		case NONE:
			return false;
		case INT:
			return true;
		case ID:
			return true;
		case QUANTITY:
			return true;
		case BOOLEAN:
			return false;
		case DOUBLE:
			return true;
		case STRING:
			return false;
		case TEXT:
			return false;
		case PRICE:
			return true;
		case PERCENT:
			return true;
		case DATE:
			return true;
		}
		return false;
	}

	/**
	 * Test, if a type is a date
	 * 
	 * @return True for date types
	 */
	public boolean isDate() {
		return (this == DATE);
	}
}
