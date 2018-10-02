/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2018 The Fakturama Team
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
package com.sebulli.fakturama.misc;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.widgets.DateTime;

/**
 * Date formatting service with various possibilities.
 *
 */
public interface IDateFormatterService {

	/**
	 * Convert a date and time string from the format YYYY-MM-DD HH:MM:SS to to
	 * localized format.
	 * 
	 * @param s
	 *            Date and time String
	 * @return Date and time as formated String
	 */
	String DateAndTimeAsLocalString(String s);

	/**
	 * Convert a date string into the format ISO 8601 YYYY-MM-DD.
	 * 
	 * @param s
	 *            Date String
	 * @return Date as formated String
	 */
	String DateAsISO8601String(String s);

	/**
	 * Returns the date now in the format ISO 8601 YYYY-MM-DD
	 * 
	 * @return Date as formatted String
	 */
	String DateAsISO8601String();

	/**
	 * Returns the date and time of now in a localized format.
	 * 
	 * @return Date and time as formated String
	 */
	String DateAndTimeOfNowAsISO8601String();

	/**
	 * Returns the date and time of now in a localized format.
	 * 
	 * @return Date and time as formated String
	 */
	String DateAndTimeOfNowAsLocalString();

	/**
	 * Convert date strings from the following format to a calendar
	 * 
	 * @param date
	 *            Date as string
	 * @return GregorianCalendar
	 */
	GregorianCalendar getCalendarFromDateString(String date);

	/**
	 * Gets the formatted localized date.
	 *
	 * @param date
	 *            the date
	 * @return the formatted localized date
	 */
	String getFormattedLocalizedDate(Date date);

	/**
	 * Get the date from a Calendar object in the localized format.
	 * 
	 * @param calendar
	 *            calendar Gregorian Calendar object
	 * @return Date as formated String
	 */
	String getDateTimeAsLocalString(GregorianCalendar calendar);

	/**
	 * Get the date from a Calendar object in the format: YYYY-MM-DD
	 * 
	 * @param calendar
	 *            Calendar object
	 * @return Date as formatted String
	 */
	String getDateTimeAsString(Calendar calendar);

	/**
	 * Get the date and time from a Calendar object in the format: YYYY-MM-DD
	 * HH:MM:SS
	 * 
	 * @param calendar
	 *            Calendar object
	 * @return Date and Time as formatted String
	 */
	String getDateAndTimeAsString(Calendar calendar);

	/**
	 * Get the date from a SWT DateTime widget in the format: YYYY-MM-DD
	 * 
	 * @param dtDate
	 *            SWT DateTime widget
	 * @return Date as formated String
	 */
	String getDateTimeAsString(DateTime dtDate);

}