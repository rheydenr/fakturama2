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


import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.ExportWizardPageStartEndDate;
import org.fakturama.export.wizard.OOCalcExporter;
import org.javamoney.moneta.Money;
import org.odftoolkit.odfdom.type.Color;

import com.sebulli.fakturama.calculate.AccountSummaryCalculator;
import com.sebulli.fakturama.dto.AccountEntry;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;


/**
 * This class exports all accounts in an OpenOffice.org 
 * Calc table. 
 * 
 */
public class AccountsExporter extends OOCalcExporter {

	@Inject
	private IEclipseContext ctx;

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;
	
	@Inject
	private Shell shell;

	/**
	 * Constructor Sets the begin and end date
	 * 
	 * @param startDate
	 *            Begin date
	 * @param endDate
	 *            Begin date
	 */
	@PostConstruct
	public void initialize(IEclipseContext ctx) {
		if(ctx.get(Constants.PARAM_START_DATE) != null) {
			startDate = (GregorianCalendar) ctx.get(Constants.PARAM_START_DATE);
		} else {
			startDate = null;
		}
		
		if(ctx.get(Constants.PARAM_END_DATE) != null) {
			endDate = (GregorianCalendar) ctx.get(Constants.PARAM_END_DATE);
		}
		doNotUseTimePeriod = (boolean) ctx.get(ExportWizardPageStartEndDate.WIZARD_DATESELECT_DONTUSETIMEPERIOD);
	}

	/**
	 * 	Do the export job.
	 * 
	 * @return
	 * 			True, if the export was successful
	 */
	public boolean export(String account, GregorianCalendar date, MonetaryAmount monetaryAmount) {
		// Array with all entries of one account
		List<AccountEntry> accountEntries;

		if (!doNotUseTimePeriod) {
			if (date.after(startDate)) {

				//T: account exporter dialog 
				MessageDialog.openWarning(shell, msg.dialogMessageboxTitleWarning,
						//T: account exporter dialog 
						exportMessages.wizardExportAccountsStartvalueError);
				return false;
				
			}
		}
		
		// Try to generate a spreadsheet
		if (!createSpreadSheet()) {
			return false;
		}

		// Collect all documents and vouchers to export
		AccountSummaryCalculator accountSummary = ContextInjectionFactory.make(AccountSummaryCalculator.class, ctx);
		accountSummary.collectEntries(account);
		accountEntries = accountSummary.getAccountEntries();
		
		// Sort the vouchers by category and date
//		Collections.sort(accountEntries, new UniDataSetSorter("date"));
		accountEntries.sort((AccountEntry entry1, AccountEntry entry2) -> {return entry1.date.before(entry2.date) ? -1 : 1;});

		// Fill the first 4 rows with the company information
		fillCompanyInformation(0);
		fillTimeIntervall(5);
		
		// Counter for the current row and columns in the Calc document
		int row = 9;
		int col = 0;

		// Set the title
		setCellTextInBold(row++, 0, account);
		row++;

		//T: Used as heading of a table. Keep the word short.
		setCellTextInBold(row, col++, msg.commonFieldDate);
		setCellTextInBold(row, col++, msg.commonFieldName);
		setCellTextInBold(row, col++, msg.commonFieldText);
		setCellTextInBold(row, col++, msg.commonFieldValue);
		setCellTextInBold(row, col++, msg.commonFieldBalance);
		
		// Draw a horizontal line (set the border of the top and the bottom
		// of the table).
		for (col = 0; col < 5; col++) {
			setBorder(row, col, Color.BLACK, false, false, true, false);
		}
		
		row++;

		MonetaryAmount balance = monetaryAmount;
		setCellText(row, 0, DataUtils.getInstance().getDateTimeAsLocalString(date));

		//T: Cell text of the account exporter
		setCellText(row, 1, exportMessages.wizardExportAccountsStartvalue);
		
		setCellValueAsLocalCurrency(row, 4,balance);
		
		setBold(row, 4);
		setBackgroundColor( 0, row, 4, row, "#e8ebed");

		row += 2;

		boolean somethingExported = false;
		
		// The vouchers are exported in 2 runs.
		// First, only the summary of all vouchers is calculated and
		// the columns are created.
		// Later all the vouchers are analyzed a second time and then they
		// are exported voucher by voucher into the table.
		for (AccountEntry accountEntry : accountEntries) {

			// calculate the balance of all vouchers and documents,
			// also of those, which are not in the time intervall
			MonetaryAmount value = Money.of(accountEntry.value, DataUtils.getInstance().getDefaultCurrencyUnit());

			// Get the date of the voucher and convert it to a
			// GregorianCalendar object.
			GregorianCalendar documentDate = new GregorianCalendar();
			documentDate.setTime(accountEntry.date);

			boolean inIntervall = isInTimeIntervall(accountEntry);
			
			// Display the balance before one entry was exported
			if (inIntervall && !somethingExported) {
				setCellText(row, 0, DataUtils.getInstance().getFormattedLocalizedDate(accountEntry.date));
				setCellValueAsLocalCurrency(row, 4,balance);
				setBold(row, 4);
				row ++;
			}
			
			// Add it to the balance only, if it is not before the date
			if (!documentDate.before(date)) {
				balance = balance.add(value);
			}
			
			if (inIntervall) {

				// Set a flag, that at least one entry was exported
				somethingExported = true;
				
				// Fill the row with the accountEntry data
				col = 0;
				
				setCellText(row, col++, DataUtils.getInstance().getFormattedLocalizedDate(accountEntry.date));
				setCellText(row, col++, accountEntry.name);
				setCellText(row, col++, accountEntry.text);
				setCellValueAsLocalCurrency(row, col++,value);
				setCellValueAsLocalCurrency(row, col++,balance);
				
				// Set the background of the table rows. Use an light and
				// alternating blue color.
				if ((row % 2) == 0) {
					setBackgroundColor( 0, row, 4, row, "#e8ebed");
				}

				row++;
				
			}
		}

		// Draw a horizontal line
		for (col = 0; col < 5; col++) {
			setBorder(row - 1, col, Color.BLACK, false, false, true, false);
		}
		
		save();

		// True = Export was successful
		return true;
	}
	
	@Override
	protected String getOutputFileName() {
		return "AccountListExport";
	}

}
