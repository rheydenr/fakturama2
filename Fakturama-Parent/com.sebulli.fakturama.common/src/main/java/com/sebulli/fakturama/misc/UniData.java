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
 * This class is container for an value in different data types. If you read the
 * value in an other type than it was set, the value will be converted.
 * 
 * @author Gerd Bartelt
 * 
 * @deprecated
 */
public class UniData {
	final private UniDataType dataType;
	// private UniDataType lastDataType;
	private Integer i = 0;
	private Boolean b = false;
	private Double d = 0.0;
	private String s = "";

	/**
	 * Constructor with invalid initial values
	 * 
	 * @param i
	 *            initial value
	 */
	public UniData() {
		this.dataType = UniDataType.NONE;
		// this.lastDataType = UniDataType.NONE;
	}

	/**
	 * Constructor with initial value as int.
	 * 
	 * @param i
	 *            initial value
	 */
	public UniData(final UniDataType dataType, int i) {
		this.dataType = dataType;
		setValue(i);
	}

	/**
	 * Constructor with initial value as boolean.
	 * 
	 * @param b
	 *            initial value
	 */
	public UniData(final UniDataType dataType, boolean b) {
		this.dataType = dataType;
		setValue(b);
	}

	/**
	 * Constructor with initial value as double.
	 * 
	 * @param d
	 *            initial value
	 */
	public UniData(final UniDataType dataType, double d) {
		this.dataType = dataType;
		setValue(d);
	}

	/**
	 * Constructor with initial value as String.
	 * 
	 * @param s
	 *            initial value
	 */
	UniData(final UniDataType dataType, String s) {
		this.dataType = dataType;
		setValue(s);
	}

	public UniDataType getUniDataType() {
		return this.dataType;
	}

	/**
	 * sets the value as integer.
	 * 
	 * @param b
	 *            new value
	 */
	public void setValue(Integer i) {
		switch (this.dataType) {
		case ID:
		case INT:
			this.i = i;
			break;
		case BOOLEAN:
			this.b = (i != 0);
			break;
		case PRICE:
		case PERCENT:
		case QUANTITY:
		case DOUBLE:
			this.d = i.doubleValue();
			break;
		case DATE:
		case STRING:
		case TEXT:
			this.s = i.toString();
			break;
		}
	}

	/**
	 * sets the value as boolean.
	 * 
	 * @param b
	 *            new value
	 */
	public void setValue(Boolean b) {
		switch (this.dataType) {
		case ID:
		case INT:
			this.i = b ? 1 : 0;
			break;
		case BOOLEAN:
			this.b = b;
			break;
		case PRICE:
		case PERCENT:
		case QUANTITY:
		case DOUBLE:
			this.d = b ? 1.0 : 0.0;
			break;
		case DATE:
		case STRING:
		case TEXT:
			this.s = b.toString();
			break;
		}

	}

	/**
	 * sets the value as double.
	 * 
	 * @param b
	 *            new value
	 */
	public void setValue(Double d) {
		switch (this.dataType) {
		case ID:
		case INT:
			this.i = d.intValue();
			break;
		case BOOLEAN:
			this.b = (d != 0);
			break;
		case PRICE:
		case PERCENT:
		case QUANTITY:
		case DOUBLE:
			this.d = d;
			break;
		case DATE:
		case STRING:
		case TEXT:
			this.s = d.toString();
			break;
		}
	}

	/**
	 * sets the value as string.
	 * 
	 * @param b
	 *            new value
	 */
	public void setValue(String s) {
		if (s == null)
			s = "";
		switch (this.dataType) {
		case ID:
		case INT:
			try {
				this.i = Integer.parseInt(s);
			}
			catch (NumberFormatException e) {
				this.i = 0;
			}
			break;

		case BOOLEAN:
			this.b = s.equalsIgnoreCase("true");
			break;
		case PRICE:
		case PERCENT:
		case QUANTITY:
		case DOUBLE:
			this.d = DataUtils.StringToDouble(s);
			break;
		case DATE:
		case STRING:
		case TEXT:
			this.s = s;
			break;
		}
	}

	/**
	 * Returns the value as Integer.
	 * 
	 * @return value as Integer
	 */
	public int getValueAsInteger() {
		switch (this.dataType) {
		case ID:
		case INT:
			return this.i;
		case BOOLEAN:
			return this.b ? 1 : 0;
		case PRICE:
		case PERCENT:
		case QUANTITY:
		case DOUBLE:
			return this.d.intValue();
		case DATE:
		case STRING:
		case TEXT:
			try {
				return Integer.parseInt(this.s);
			}
			catch (NumberFormatException e) {
				return 0;
			}
		default:
			return -1;
		}
	}

	/**
	 * Returns the value as Boolean.
	 * 
	 * @return value as Boolean
	 */
	public boolean getValueAsBoolean() {
		switch (this.dataType) {
		case ID:
		case INT:
			return (this.i != 0);
		case BOOLEAN:
			return this.b;
		case PRICE:
		case PERCENT:
		case QUANTITY:
		case DOUBLE:
			return (this.d != 0);
		case DATE:
		case STRING:
		case TEXT:
			return this.s.equalsIgnoreCase("true");
		default:
			return false;
		}
	}

	/**
	 * Returns the value as Double.
	 * 
	 * @return value as Double
	 */
	public double getValueAsDouble() {
		switch (this.dataType) {
		case ID:
		case INT:
			return this.i.doubleValue();
		case BOOLEAN:
			return this.b ? 1.0 : 0.0;
		case PRICE:
		case PERCENT:
		case QUANTITY:
		case DOUBLE:
			return this.d;
		case DATE:
		case STRING:
		case TEXT:
			return DataUtils.StringToDouble(s);
		default:
			return 0.0;
		}
	}

	/**
	 * Returns the value as String.
	 * 
	 * @return value as String
	 */
	public String getValueAsString() {
		switch (this.dataType) {
		case ID:
		case INT:
			return this.i.toString();
		case BOOLEAN:
			return this.b.toString();
		case PRICE:
		case PERCENT:
		case QUANTITY:
		case DOUBLE:
			return this.d.toString();
		case DATE:
		case STRING:
		case TEXT:
			return this.s;
		default:
			return "invalid";
		}
	}

	/**
	 * Returns the value as formated String.
	 * 
	 * @return value as formated String
	 */
	public String getValueAsFormatedString() {
		
		switch (this.dataType) {
		case PRICE:
			return DataUtils.DoubleToFormatedPrice(this.getValueAsDouble());
		case PERCENT:
			return DataUtils.DoubleToFormatedPercent(this.getValueAsDouble());
		case QUANTITY:
			return DataUtils.DoubleToFormatedQuantity(this.getValueAsDouble());
		case DATE:
			return DataUtils.DateAsLocalString(getValueAsString());
		default:
			return getValueAsString();
		}
	}

	/**
	 * Returns the data type
	 * 
	 * @return dataType
	 */
	public UniDataType getDataType() {
		return dataType;
	}

}
