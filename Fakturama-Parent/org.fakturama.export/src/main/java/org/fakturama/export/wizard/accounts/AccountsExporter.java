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

package org.fakturama.export.wizard.accounts;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

import javax.money.MonetaryAmount;

import org.eclipse.swt.SWT;
import org.fakturama.export.wizard.OOCalcExporter;

import com.sebulli.fakturama.misc.DataUtils;


/**
 * This class exports all accounts in an OpenOffice.org 
 * Calc table. 
 * 
 */
public class AccountsExporter extends OOCalcExporter {

	/**
	 * Constructor Sets the begin and end date
	 * 
	 * @param startDate
	 *            Begin date
	 * @param endDate
	 *            Begin date
	 */
	public AccountsExporter(GregorianCalendar startDate, GregorianCalendar endDate,
			 boolean doNotUseTimePeriod) {
		super(startDate, endDate, doNotUseTimePeriod);
		
	}

	/**
	 * 	Do the export job.
	 * 
	 * @return
	 * 			True, if the export was successful
	 */
	public boolean export(String account, GregorianCalendar date, MonetaryAmount monetaryAmount) {

//		// Array with all entries of one account
//		ArrayList<DataSetAccountEntry> accountEntries;
//
//		if (!doNotUseTimePeriod) {
//			if (date.after(startDate)) {
//
//				//T: account exporter dialog 
//				Workspace.showMessageBox(SWT.OK, _("Warning"),
//						//T: account exporter dialog 
//						_("The date must not be after the start date!"));
//				return false;
//				
//			}
//		}
//		
//		// Try to generate a spreadsheet
//		if (!createSpreadSheet())
//			return false;
//
//		// Collect all documents and vouchers to export
//		AccountSummary accountSummary = new AccountSummary();
//		accountSummary.collectEntries(account);
//		accountEntries = accountSummary.getAccountEntries();
//		
//		// Sort the vouchers by category and date
//		Collections.sort(accountEntries, new UniDataSetSorter("date"));
//
//		// Fill the first 4 rows with the company information
//		fillCompanyInformation(0);
//		fillTimeIntervall(5);
		
		// Counter for the current row and columns in the Calc document
		int row = 9;
		int col = 0;
//
//		// Set the title
//		setCellTextInBold(row++, 0, account);
//		row++;
//
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Date"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Name"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Text"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Value"));
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, _("Balance"));
//		
//		// Draw a horizontal line (set the border of the top and the bottom
//		// of the table).
//		for (col = 0; col < 5; col++) {
//			setBorder(row, col, 0x000000, false, false, true, false);
//		}
//		
//		row++;
//
//		double balance = monetaryAmount;
//		setCellText(row, 0, DataUtils.DateAsLocalString(DataUtils.getDateTimeAsString(date)));
//
//		//T: Cell text of the account exporter
//		//setCellText(row, 1, _("Account balance"));
//		
//		setCellValueAsLocalCurrency(row, 4,balance);
//		
//		setBold(row, 4);
//		setBackgroundColor( 0, row, 4, row, 0x00e8ebed);
//
//		row += 2;
//
//		boolean somethingExported = false;
//		
//		// The vouchers are exported in 2 runs.
//		// First, only the summary of all vouchers is calculated and
//		// the columns are created.
//		// Later all the vouchers are analyzed a second time and then they
//		// are exported voucher by voucher into the table.
//		for (DataSetAccountEntry accountEntry : accountEntries) {
//
//			// calculate the balance of all vouchers and documents,
//			// also of those, which are not in the time intervall
//			double value = accountEntry.getDoubleValueByKey("value");
//
//			
//			// Get the date of the voucher and convert it to a
//			// GregorianCalendar object.
//			GregorianCalendar documentDate = new GregorianCalendar();
//			try {
//				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//				documentDate.setTime(formatter.parse(accountEntry.getStringValueByKey("date")));
//			}
//			catch (ParseException e) {
//				Logger.logError(e, "Error parsing Date");
//			}
//
//			boolean inIntervall = isInTimeIntervall(accountEntry);
//			
//			// Display the balance before one entry was exported
//			if (inIntervall && !somethingExported) {
//				setCellText(row, 0, DataUtils.DateAsLocalString(accountEntry.getStringValueByKey("date")));
//				setCellValueAsLocalCurrency(row, 4,balance);
//				setBold(row, 4);
//				row ++;
//			}
//			
//			// Add it to the balance only, if it is not before the date
//			if (!documentDate.before(date)) {
//				balance += value;
//			}
//			
//			if (inIntervall) {
//
//				// Set a flag, that at least one entry was exported
//				somethingExported = true;
//				
//				// Fill the row with the accountEntry data
//				col = 0;
//				
//				setCellText(row, col++, DataUtils.DateAsLocalString(accountEntry.getStringValueByKey("date")));
//				setCellText(row, col++, accountEntry.getStringValueByKey("name"));
//				setCellText(row, col++, accountEntry.getStringValueByKey("text"));
//				setCellValueAsLocalCurrency(row, col++,value);
//				setCellValueAsLocalCurrency(row, col++,balance);
//				
//				// Set the background of the table rows. Use an light and
//				// alternating blue color.
//				if ((row % 2) == 0)
//					setBackgroundColor( 0, row, 4, row, 0x00e8ebed);
//
//				row++;
//				
//			}
//		}

		// Draw a horizontal line
		for (col = 0; col < 5; col++) {
//			setBorder(row - 1, col, 0x000000, false, false, true, false);
		}
		

		// True = Export was successful
		return true;
	}


}
