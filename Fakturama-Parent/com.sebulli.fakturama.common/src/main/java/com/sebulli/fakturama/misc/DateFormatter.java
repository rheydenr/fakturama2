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

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;

import com.ibm.icu.text.DateFormat;

/**
 * Date formatting with various possibilities.
 *
 */
public class DateFormatter implements IDateFormatterService {
	// /**
	// * Convert a date string from the format YYYY-MM-DD to to localized format.
	// *
	// * @param s
	// * Date String
	// * @return Date as formated String
	// */
	// public static String DateAsLocalString(String s) {
	// if(s.equals(ZERO_DATE)) {
	// return "";
	// }
	//
	// GregorianCalendar calendar = new GregorianCalendar();
	// try {
	// DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	// calendar.setTime(formatter.parse(s));
	// }
	// catch (ParseException e) {
	//// Logger.logError(e, "Error parsing Date");
	// }
	// DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
	// return df.format(calendar.getTime());
	// }

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * com.sebulli.fakturama.misc.IDataFormatter#DateAndTimeAsLocalString(java.lang.
//	 * String)
//	 */
//	@Override
//	public String DateAndTimeAsLocalString(String s) {
//		String retval = "";
//		if (s == null) {
//			return retval;
//		}
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        TemporalAccessor parsedDateTime = formatter.parse(s);
//        LocalDateTime localDateTime = LocalDateTime.from(parsedDateTime);
//        retval = localDateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.MEDIUM));
//        return retval;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sebulli.fakturama.misc.IDataFormatter#DateAsISO8601String(java.lang.
	 * String)
	 */
	@Override
	public String DateAsISO8601String(String s) {
		GregorianCalendar calendar = null;
		String retval;
		if (s != null && s != "") {
			calendar = getCalendarFromDateString(s);
			retval = getDateTimeAsString(calendar);
		} else {
			retval = "";
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sebulli.fakturama.misc.IDataFormatter#DateAsISO8601String()
	 */
	@Override
	public String DateAsISO8601String() {
		GregorianCalendar calendar = new GregorianCalendar();
		return getDateAndTimeAsString(calendar);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.misc.IDataFormatter#DateAndTimeOfNowAsISO8601String()
	 */
	@Override
	public String DateAndTimeOfNowAsISO8601String() {
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.misc.IDataFormatter#getDateAndTimeAsString()
	 */
	@Override
	public String getDateAndTimeAsString(Calendar calendar) {
		return String.format("%1$tF %1$tT", calendar.getTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.misc.IDataFormatter#getDateTimeAsString()
	 */
	@Override
	public String getDateTimeAsString(Calendar calendar) {
		return String.format("%tF", calendar.getTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.misc.IDataFormatter#getDateTimeAsLocalString()
	 */
	@Override
	public String getDateTimeAsLocalString(GregorianCalendar calendar) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return df.format(calendar.getTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.misc.IDataFormatter#getFormattedLocalizedDate()
	 */
	@Override
	public String getFormattedLocalizedDate(Date date) {
		if (date != null) {
			LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
			return localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
		} else {
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.misc.IDataFormatter#getCalendarFromDateString()
	 */
	@Override
	public GregorianCalendar getCalendarFromDateString(String date) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.clear();

		// try to parse the input date string
		// use also localized formats
		try {
		    Date d = DateUtils.parseDate(date, "yyyy-MM-dd","yyyy/MM/dd", "dd.MM.yyyy","MM/dd/yyyy");
			calendar.setTime(d);
		} catch (ParseException e) {
					// Logger.logError(e3, "Error parsing Date:" + date);
		}
		return calendar;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * com.sebulli.fakturama.misc.IDataFormatter#DateAndTimeOfNowAsLocalString()
//	 */
//	@Override
//	public String DateAndTimeOfNowAsLocalString() {
//		GregorianCalendar calendar = new GregorianCalendar();
//		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
//		return df.format(calendar.getTime());
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * com.sebulli.fakturama.misc.IDataFormatter#getDateTimeAsString()
//	 */
//    @Override
//	public String getDateTimeAsString(DateTime dtDate) {
//        return String.format("%04d-%02d-%02d", dtDate.getYear(), dtDate.getMonth() + 1, dtDate.getDay());
//    }

}
