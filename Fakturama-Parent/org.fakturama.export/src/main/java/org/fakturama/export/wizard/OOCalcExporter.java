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

package org.fakturama.export.wizard;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.export.ExportMessages;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;

import com.sebulli.fakturama.dto.AccountEntry;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.OSDependent;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Voucher;

/**
 * The sales exporter. This class collects all the sales and fills a Calc table
 * with the data
 * 
 */
public class OOCalcExporter {

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;

    @Inject
    protected Logger log;
	
	@Inject
	protected Shell shell;
	
    @Inject
    @Preference(nodePath = "com.sebulli.fakturama.rcp")
    private IEclipsePreferences eclipsePrefs;

	public final static boolean PAID = true;
	public final static boolean UNPAID = false;
	// The begin and end date to specify the export periode
	protected GregorianCalendar startDate;
	protected GregorianCalendar endDate;
	
	// Use start and end date or export all
	protected boolean doNotUseTimePeriod;

	// the date key to sort the documents
//	protected String documentDateKey;
	// Settings from the preference page
	protected boolean usePaidDate;

	// The "Export" spreadsheet
	protected Table spreadsheet = null;

	// export paid or unpaid invoices
	protected boolean exportPaid = true;

	private SpreadsheetDocument oOdocument;

	
	/**
	 * Default constructor
	 */
	public OOCalcExporter() {
		this.startDate = null;
		this.endDate = null;
		this.doNotUseTimePeriod = true;
	}

	/**
	 * Constructor Sets the begin and end date
	 * 
	 * @param startDate
	 *            Begin date
	 * @param endDate
	 *            Begin date
	 */
	public OOCalcExporter(GregorianCalendar startDate, GregorianCalendar endDate, boolean doNotUseTimePeriod) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.doNotUseTimePeriod = doNotUseTimePeriod;
	}

	protected void fillCompanyInformation(int row) {
		
		// Fill the first cells with company data
		setCellTextInItalic(row++, 0, eclipsePrefs.get(Constants.PREFERENCES_YOURCOMPANY_COMPANY_NAME, ""));
		setCellTextInItalic(row++, 0, eclipsePrefs.get(Constants.PREFERENCES_YOURCOMPANY_COMPANY_OWNER, ""));
		setCellTextInItalic(row++, 0, eclipsePrefs.get(Constants.PREFERENCES_YOURCOMPANY_COMPANY_STREET, ""));
		setCellTextInItalic(row++, 0, eclipsePrefs.get(Constants.PREFERENCES_YOURCOMPANY_COMPANY_ZIP, "") + " "
				+ eclipsePrefs.get(Constants.PREFERENCES_YOURCOMPANY_COMPANY_CITY, ""));
	}
	
	protected void fillTimeIntervall(int row) {

		// Do not display a time period
		if (doNotUseTimePeriod) {
			return;
		}
		
		// Display the time interval
		//T: Sales Exporter - Text in the Calc document for the period
		setCellTextInBold(row++, 0, exportMessages.wizardExportOutputPeriod);
		//T: Sales Exporter - Text in the Calc document for the period
		setCellText(row, 0, exportMessages.wizardExportOutputStartdate);
		setCellText(row++, 1, DataUtils.getInstance().getDateTimeAsLocalString(startDate));
		//T: Sales Exporter - Text in the Calc document for the period
		setCellText(row, 0, exportMessages.wizardExportOutputEnddate);
		setCellText(row++, 1, DataUtils.getInstance().getDateTimeAsLocalString(endDate));
	}
	
	
	/**
	 * Returns if a given document should be used to export. Only invoices and
	 * credit documents that are paid in the specified time interval are
	 * exported.
	 * 
	 * @param document
	 *            The document that is tested
	 * @return True, if the document should be exported
	 * @deprecated Do it with database queries!
	 */
	protected boolean documentShouldBeExported(Document document) {

		// By default, the document will be exported.
		boolean isInIntervall = true;

		// Use the time period
		if (!doNotUseTimePeriod) {
			// Get the date of the document and convert it to a
			// GregorianCalendar object.
			GregorianCalendar documentDate = new GregorianCalendar();
			
			// Use pay date or document date
			Date documentDateString = usePaidDate ? document.getPayDate() : document.getDocumentDate();
			documentDate.setTime(documentDateString);

			// Test, if the document's date is in the interval
			if ((startDate != null) && (endDate != null)) {
				if (startDate.after(documentDate))
					isInIntervall = false;
				if (endDate.before(documentDate))
					isInIntervall = false;
			}
		}

		// Only invoices and credits in the interval
		// will be exported.
		boolean isInvoiceOrCreditInIntervall = ((document.getBillingType() == BillingType.INVOICE) 
				|| (document.getBillingType() == BillingType.CREDIT
				)) && isInIntervall;
		
		// Export paid or unpaid documents
		if (exportPaid)
			// export paid
			return isInvoiceOrCreditInIntervall && document.getPaid();
		else
			// export unpaid
			return isInvoiceOrCreditInIntervall && !document.getPaid();
	}
	
	/**
	 * Returns if a given data set should be used to export. Only
	 * entries in the specified time interval are exported.
	 * 
	 * @param uds
	 *            The uni data set that is tested
	 * @return <code>true</code> if the uni data set should be exported
	 */
	protected boolean isInTimeIntervall(AccountEntry uds) {
		return isInTimeIntervall(uds.date);
	}
	
	/**
	 * Returns if a given data set should be used to export. Only
	 * entries in the specified time interval are exported.
	 * 
	 * @param uds
	 *            The uni data set that is tested
	 * @return <code>true</code> if the uni data set should be exported
	 */
	protected boolean isInTimeIntervall(Voucher uds) {
		return isInTimeIntervall(uds.getVoucherDate());
	}

	private boolean isInTimeIntervall(Date testDate) {

		// By default, the document will be exported.
		boolean isInIntervall = true;

		// Use the time period
		if (doNotUseTimePeriod) {
			return true;
		}
		
		// Get the date of the voucher and convert it to a
		// GregorianCalendar object.
//		GregorianCalendar documentDate = new GregorianCalendar();
//		try {
//			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//
//			String dateString = "";
//
//			// Use date  
//			dateString = uds.getStringValueByKey("date");
//
//			documentDate.setTime(formatter.parse(dateString));
//		}
//		catch (ParseException e) {
//			Logger.logError(e, "Error parsing Date");
//		}

		// Test, if the voucher's date is in the interval
		if ((startDate != null) && (endDate != null)) {
			if (startDate.after(testDate))
				isInIntervall = false;
			if (endDate.before(testDate))
				isInIntervall = false;
		}

		// Return, if voucher is in the interval
		return isInIntervall;
	}
	
	protected boolean createSpreadSheet() {

		// Create a new OpenOffice Calc document

		oOdocument = null;
		try {
			oOdocument = SpreadsheetDocument.newSpreadsheetDocument();
		} catch (Exception e) {
			log.error(e, "OO Error opening CALC");
			return false;
		}

		// Get the spreadsheets

		// T: Name of the Table
		String tableName = msg.pageExport;

		// Get a reference to the Export sheet
		spreadsheet = oOdocument.getSheetByIndex(0);
		spreadsheet.setTableName(tableName);
		return true;

	}	

	/**
	 * Fill a cell with a text
	 * 
	 * @param spreadsheet
	 *            The spreadsheet that contains the cell
	 * @param row
	 *            The cell row
	 * @param column
	 *            The cell column
	 * @param text
	 *            The text that will be insert
	 *            
	 * @return for your convenience, it returns the changed cell
	 */
	protected Cell setCellText(int row, int column, String text) {
		
		Cell cellText = CellFormatter.getCell(spreadsheet, row, column);
		if(text != null && text.contains("\n")) {
			cellText.setTextWrapped(true);
			cellText.addParagraph(text.substring(0, text.indexOf('\n')));
			cellText.addParagraph(text.substring(text.indexOf('\n')+1));
		} else {
			cellText.setStringValue(text);
		}
		return cellText;
	}

	/**
	 * Fill a cell with a text. Use a bold font.
	 * 
	 * @param spreadsheet
	 *            The spreadsheet that contains the cell
	 * @param row
	 *            The cell row
	 * @param column
	 *            The cell column
	 * @param text
	 *            The text that will be insert
	 */
	protected void setCellTextInBold(int row, int column, String text) {
		setCellText(row, column, text);
		CellFormatter.setBold(spreadsheet, row, column);
	}

	/**
	 * Fill a cell with a text. Use an italic font style.
	 * 
	 * @param spreadsheet
	 *            The spreadsheet that contains the cell
	 * @param row
	 *            The cell row
	 * @param column
	 *            The cell column
	 * @param text
	 *            The text that will be insert
	 */
	protected void setCellTextInItalic(int row, int column, String text) {
		Cell cell = setCellText(row, column, text);
		cell.getStyleHandler().getTextPropertiesForWrite().setFontStyle(FontStyle.ITALIC);
	}

	/**
	 * Fill a cell with a text. Use a red and bold font.
	 * 
	 * @param row
	 *            The cell row
	 * @param column
	 *            The cell column
	 * @param text
	 *            The text that will be inserted
	 */
	protected void setCellTextInRedBold(int row, int column, String text) {
		Cell cell = setCellText(row, column, text);
		cell.getStyleHandler().getTextPropertiesForWrite().setFontStyle(FontStyle.BOLD);
		cell.getStyleHandler().getTextPropertiesForWrite().setFontColor(Color.RED);
	}

	/**
	 * Set a cell to a double value and format it with the local currency.
	 * 
	 * @param row
	 *            The cell row
	 * @param column
	 *            The cell column
	 * @param amount
	 *            The value that will be inserted.
	 */
	protected void setCellValueAsLocalCurrency(int row, int column, MonetaryAmount amount) {
		Cell cell = CellFormatter.getCell(spreadsheet, row, column);
		cell.setCurrencyValue(amount.getNumber().doubleValue(), amount.getCurrency().getCurrencyCode());
		String currencyCode = amount.getCurrency().getCurrencyCode(); 
		// DataUtils.getInstance().getDefaultCurrencyUnit().getCurrencyCode();
		cell.setCurrencyCode(currencyCode);
//		String formattedValue = DataUtils.getInstance().formatCurrency(amount);
//		cell.setDisplayText(formattedValue);
		// TODO make it more flexible!
		cell.setCurrencyFormat(currencyCode, "#,##0."+StringUtils.repeat("0", amount.getCurrency().getDefaultFractionDigits())+" " + currencyCode);
	}
	
	protected void setCellValueAsLocalCurrency(int row, int column, Double amount) {
		Cell cell = CellFormatter.getCell(spreadsheet, row, column);
		String currencyCode = DataUtils.getInstance().getDefaultCurrencyUnit().getCurrencyCode();
		cell.setCurrencyValue(amount, currencyCode);
		cell.setCurrencyCode(currencyCode);
		cell.setCurrencyFormat(currencyCode, "#,##0."+StringUtils.repeat("0", DataUtils.getInstance().getDefaultCurrencyUnit().getDefaultFractionDigits())+" " + currencyCode);
	}
	
	protected void setCellValueAsPercent( int row, int column, Double d) {
		Cell cell = CellFormatter.getCell(spreadsheet, row, column);
		cell.setPercentageValue(d != null ? d : Double.valueOf(0.0));
	}
	
	protected void setCellValueAsDouble( int row, int column, Double d) {
		Cell cell = CellFormatter.getCell(spreadsheet, row, column);
		cell.setDoubleValue(d != null ? d : Double.valueOf(0.0));
	}
	
	protected void setCellValueAsBoolean( int row, int column, Boolean b) {
		Cell cell = CellFormatter.getCell(spreadsheet, row, column);
		cell.setBooleanValue(BooleanUtils.isTrue(b));
	}
	
	/**
	 * Sets the background color.
	 *
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 * @param color
	 *            the color
	 */
	protected void setBackgroundColor(int row, int column, String color) {
		CellFormatter.setBackgroundColor(spreadsheet, row, column, color);
	}

	/**
	 * Sets the background color in a given cell range.
	 *
	 * @param left the leftmost column in this range
	 * @param top the topmost row in this range
	 * @param right the rightmost column in this range
	 * @param bottom the bottom row in this range
	 * @param color the color to set (as String, see W3C colors)
	 */
	protected void setBackgroundColor(int left, int top, int right, int bottom, String color) {
		CellFormatter.setBackgroundColor(spreadsheet, left, top, right, bottom, color);
	}

	protected void setBold(int row, int column) {
		CellFormatter.setBold(spreadsheet, row, column);
	}
	protected void setBorder(int row, int column, Color color, boolean top, boolean right, boolean bottom, boolean left) {
		CellFormatter.setBorder(spreadsheet, row, column, color, top, right, bottom, left); 
	}

	protected void setFormula(int column, int row, String formula) {
		try {
			spreadsheet.getCellByPosition(column, row).setFormula(formula);
		}
		catch (IndexOutOfBoundsException e) {
			log.error(e, "No access to cell: " + column + ":" +row);
		}
	}
	
	public void save() {
		boolean answer = true;
		try {
			do {
				String fileName = createOutputFileDialog();
				if (StringUtils.isNotBlank(fileName)) {
					oOdocument.save(fileName);
					MessageDialog.openInformation(shell, msg.dialogMessageboxTitleInfo,
							String.format(exportMessages.wizardCommonSaveInfo, fileName));
				}
			} while(!answer);
		} catch (Exception e) {
			log.error(e, "Could not store exported document.");
			MessageDialog.openError(shell, msg.dialogMessageboxTitleError, "can't save your document: " + e.getMessage());
		}
	}

	private String createOutputFileDialog() {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String[] filterNames = new String[] { "OpenOffice Calc Files", exportMessages.wizardCommonMaskAllfiles + " (*)" };
		String[] filterExtensions = new String[] { "*.ods", "*" };
		String filterPath = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, "/");
		if (OSDependent.isWin()) {
			filterNames = new String[] { "OpenOffice Calc Files", exportMessages.wizardCommonMaskAllfiles + " (*.*)" };
			filterExtensions = new String[] { "*.ods", "*.*" };
			filterPath = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, "c:\\");
		}
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFilterPath(filterPath);
		dialog.setFileName(getOutputFileName());
		dialog.setOverwrite(true);
		return dialog.open();
	}

	/**
	 * Returns the output file name. Can be overwritten.
	 *
	 * @return the output file name
	 */
	protected String getOutputFileName() {
		return "DEFAULT";
	}

}
