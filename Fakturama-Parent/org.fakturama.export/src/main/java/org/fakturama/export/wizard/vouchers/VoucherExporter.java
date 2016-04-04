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

package org.fakturama.export.wizard.vouchers;


import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.inject.Inject;

import org.fakturama.export.wizard.OOCalcExporter;
import org.odftoolkit.odfdom.type.Color;

import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dao.ReceiptVouchersDAO;
import com.sebulli.fakturama.misc.DataUtils;


/**
 * This class exports all vouchers in an OpenOffice.org 
 * Calc table. 
 * 
 */
public class VoucherExporter extends OOCalcExporter {
	
	public static final int SUPPLIER = 1;
	public static final int CUSTOMER = 2;
    
    @Inject
    private ExpendituresDAO expendituresDAO;
    
    @Inject
    private ReceiptVouchersDAO receiptVouchersDAO;

	// Settings from the preference page
	private boolean showVoucherSumColumn;
	private boolean showZeroVatColumn;


	/**
	 * Constructor Sets the begin and end date
	 * 
	 * @param startDate
	 *            Begin date
	 * @param endDate
	 *            Begin date
	 */
	public VoucherExporter(GregorianCalendar startDate, GregorianCalendar endDate,
			 boolean doNotUseTimePeriod,
			 boolean showVoucherSumColumn,
			boolean showZeroVatColumn) {
		super(startDate, endDate, doNotUseTimePeriod);
		this.showVoucherSumColumn = showVoucherSumColumn;
		this.showZeroVatColumn = showZeroVatColumn;
		
	}

	/**
	 * 	Do the export job.
	 * 
	 * @return
	 * 			True, if the export was successful
	 */
	public boolean export(String title, int type) {
		
		String customerSupplier = "";
		switch (type) {
		case 1:
			customerSupplier = msg.expenditurevoucherFieldSupplier;
			break;
		default:
			customerSupplier = msg.expenditurevoucherFieldSupplier;
			break;
		}

		// Try to generate a spreadsheet
		if (!createSpreadSheet())
			return false;
//
//		// Sort the vouchers by category and date
//		Collections.sort(vouchers, new UniDataSetSorter("category", "date"));
//
//
//		// Count the columns that contain a VAT and net value 
//		int columnsWithVatHeading = 0;
//		int columnsWithNetHeading = 0;
//
//		// Count the columns that contain a VAT value of 0% 
//		int zeroVatColumns = 0;
//
//		// Fill the first 4 rows with the company information
//		fillCompanyInformation(0);
//		fillTimeIntervall(5);
//		
//		// Counter for the current row and columns in the Calc document
//		int row = 9;
//		int col = 0;
//
//
//		// Create a voucher summary set manager that collects all voucher VAT
//		// values of all vouchers
//		VoucherSummarySetManager voucherSummarySetAllVouchers = new VoucherSummarySetManager();
//
//		// Set the title
//		setCellTextInBold(row++, 0, title);
//		row++;
//
//		// Table column headings
//		int headLine = row;
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, msg.commonFieldCategory);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, msg.commonFieldDate);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, msg.exporterDataVoucher);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, msg.exporterDataDocno);
//		// Customer or supplier
//		setCellTextInBold(row, col++, customerSupplier);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, msg.commonFieldText);
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, msg.exporterDataAccounttype);
//
//		if (showVoucherSumColumn) {
//			//T: Used as heading of a table. Keep the word short.
//			setCellTextInBold(row, col++, msg.productDataNet);
//			//T: Used as heading of a table. Keep the word short.
//			setCellTextInBold(row, col++, msg.productDataGross);
//		}
//
//		row++;
//		int columnOffset = col;
//
//		// The vouchers are exported in 2 runs.
//		// First, only the summary of all vouchers is calculated and
//		// the columns are created.
//		// Later all the vouchers are analyzed a second time and then they
//		// are exported voucher by voucher into the table.
//		for (DataSetVoucher voucher : vouchers) {
//
//			if (isInTimeIntervall(voucher)) {
//				voucherSummarySetAllVouchers.add(voucher, false);
//			}
//		}
//
//		boolean vatIsNotZero = false;
//
//		vatIsNotZero = false;
//		col = columnOffset;
//		columnsWithVatHeading = 0;
//		columnsWithNetHeading = 0;
//
//		// A column for each Vat value is created 
//		// The VAT summary items are sorted. So first ignore the VAT entries
//		// with 0%. 
//		// If the VAT value is >0%, create a column with heading.
//		for (Iterator<VatSummaryItem> iterator = voucherSummarySetAllVouchers.getVoucherSummaryItems().iterator(); iterator.hasNext();) {
//			VatSummaryItem item = iterator.next();
//
//			// Create a column, if the value is not 0%
//			if ((item.getVat().doubleValue() > 0.001) || vatIsNotZero || showZeroVatColumn) {
//
//				// If the first non-zero VAT column is created,
//				// do not check the value any more.
//				vatIsNotZero = true;
//
//				// Count the columns
//				columnsWithVatHeading++;
//
//				// Create a column heading in bold
//				int column = voucherSummarySetAllVouchers.getIndex(item) - zeroVatColumns;
//
//				// Add VAT name and description and use 2 lines
//				String text = item.getVatName();
//				String description = item.getDescription();
//
//				if (!description.isEmpty())
//					text += "\n" + description;
//
//				setCellTextInBold(headLine, column + columnOffset, text);
//
//			}
//			else
//				// Count the columns with 0% VAT
//				zeroVatColumns++;
//		}
//
//		// A column for each Net value is created 
//		// The Net summary items are sorted. 
//		for (Iterator<VatSummaryItem> iterator = voucherSummarySetAllVouchers.getVoucherSummaryItems().iterator(); iterator.hasNext();) {
//			VatSummaryItem item = iterator.next();
//
//			// Count the columns
//			columnsWithNetHeading++;
//
//			// Create a column heading in bold
//			int column = voucherSummarySetAllVouchers.getIndex(item);
//
//			// Add VAT name and description and use 2 lines
//			//T: Used as heading of a table. Keep the word short.
//			String text = msg.productDataNet + "\n" + item.getVatName();
//			String description = item.getDescription();
//
//			if (!description.isEmpty())
//				text += "\n" + description;
//
//			setCellTextInBold(headLine, columnsWithVatHeading + column + columnOffset, text);
//		}
//
//		int voucherIndex = 0;
//
//		// Second run.
//		// Export the voucher data
//		for (DataSetVoucher voucher : vouchers) {
//
//			if (isInTimeIntervall(voucher)) {
//
//				for (int voucherItemIndex = 0; voucherItemIndex < voucher.getItems().getDatasets().size(); voucherItemIndex++) {
//
//					DataSetVoucherItem voucherItem = voucher.getItem(voucherItemIndex);
//
//					// Now analyze voucher by voucher
//					VoucherSummarySetManager vatSummarySetOneVoucher = new VoucherSummarySetManager();
//					voucher.calculate();
//
//					// Add the voucher to the VAT summary
//					vatSummarySetOneVoucher.add(voucher, false, voucherItemIndex);
//
//					// Fill the row with the voucher data
//					col = 0;
//
//					if (voucherItemIndex == 0) {
//						setCellText(row, col++, voucher.getStringValueByKey("category"));
//						setCellText(row, col++, DataUtils.DateAsLocalString(voucher.getStringValueByKey("date")));
//						setCellText(row, col++, voucher.getStringValueByKey("nr"));
//						setCellText(row, col++, voucher.getStringValueByKey("documentnr"));
//						setCellText(row, col++, voucher.getStringValueByKey("name"));
//					}
//
//					col = 5;
//					setCellText(row, col++, voucherItem.getStringValueByKey("name"));
//					setCellText(row, col++, voucherItem.getStringValueByKey("category"));
//
//					//setCellValueAsLocalCurrency(xSpreadsheetDocument, spreadsheet, row, col++, document.getDoubleValueByKey("total"));
//
//					// Calculate the total VAT of the voucher
//					PriceValue totalVat = new PriceValue(0.0);
//
//					// Get all VAT entries of this voucher and place them into the
//					// corresponding column.
//					for (Iterator<VatSummaryItem> iterator = vatSummarySetOneVoucher.getVoucherSummaryItems().iterator(); iterator.hasNext();) {
//						VatSummaryItem item = iterator.next();
//
//						// Get the column
//						int column = voucherSummarySetAllVouchers.getIndex(item) - zeroVatColumns;
//
//						// If column is <0, it was a VAT entry with 0%
//						if (column >= 0) {
//
//							// Round the VAT and add fill the table cell
//							PriceValue vat = new PriceValue(item.getVat());
//							totalVat.add(vat.asRoundedDouble());
//							setCellValueAsLocalCurrency(row, column + columnOffset, vat.asRoundedDouble());
//						}
//					}
//
//					// Get all net entries of this voucher and place them into the
//					// corresponding column.
//					for (Iterator<VatSummaryItem> iterator = vatSummarySetOneVoucher.getVoucherSummaryItems().iterator(); iterator.hasNext();) {
//						VatSummaryItem item = iterator.next();
//
//						// Get the column
//						int column = voucherSummarySetAllVouchers.getIndex(item);
//
//						// If column is <0, it was a VAT entry with 0%
//						if (column >= 0) {
//
//							// Round the net and add fill the table cell
//							PriceValue net = new PriceValue(item.getNet());
//							//totalVat.add(net.asRoundedDouble());
//							setCellValueAsLocalCurrency(row, columnsWithVatHeading + column + columnOffset,
//									net.asRoundedDouble());
//						}
//					}
//
//					// Display the sum of an voucher only in the row of the first
//					// voucher item
//					if (showVoucherSumColumn) {
//						if (voucherItemIndex == 0) {
//							col = columnOffset - 2;
//							// Calculate the vouchers net and gross total 
//							setCellValueAsLocalCurrency(row, col++, voucher.getSummary().getTotalNet().asDouble());
//							setCellValueAsLocalCurrency(row, col++, voucher.getSummary().getTotalGross().asDouble());
//						}
//					}
//
//					// Set the background of the table rows. Use an light and
//					// alternating blue color.
//					if ((voucherIndex % 2) == 0)
//						setBackgroundColor(0, row, columnsWithVatHeading + columnsWithNetHeading + columnOffset - 1, row,
//								0x00e8ebed);
//
//					row++;
//
//				}
//				voucherIndex++;
//			}
//		}
//
//		// Insert a formula to calculate the sum of a column.
//		// "sumrow" is the row under the table.
//		int sumrow = row;
//
//		// Show the sum only, if there are values in the table
//		if (sumrow > (headLine + 1)) {
//			for (int i = (showVoucherSumColumn ? -2 : 0); i < (columnsWithVatHeading + columnsWithNetHeading); i++) {
//				col = columnOffset + i;
//				try {
//					// Create formula for the sum. 
//					String cellNameBegin = CellFormatter.getCellName(headLine + 1, col);
//					String cellNameEnd = CellFormatter.getCellName(row - 1, col);
//					setFormula(col, sumrow, "=SUM(" + cellNameBegin + ":" + cellNameEnd + ")");
//					setBold(sumrow, col);
//				}
//				catch (IndexOutOfBoundsException e) {
//				}
//			}
//		}
//
//		// Draw a horizontal line (set the border of the top and the bottom
//		// of the table).
//		for (col = 0; col < (columnsWithVatHeading + columnsWithNetHeading) + columnOffset; col++) {
//			setBorder(headLine, col, 0x000000, false, false, true, false);
//			setBorder(sumrow, col, 0x000000, true, false, false, false);
//		}
//
//		// Create a voucher summary set manager that collects all 
//		// categories of voucher items
//		VoucherSummarySetManager voucherSummaryCategories = new VoucherSummarySetManager();
//
//		// Calculate the summary
//		for (DataSetVoucher voucher : vouchers) {
//
//			if (isInTimeIntervall(voucher)) {
//				voucherSummaryCategories.add(voucher, true);
//			}
//		}
//
//		row += 3;
//		// Table heading
//		
//		//T: Sales Exporter - Text in the Calc document
//		setCellTextInBold(row++, 0, exportMessages.wizardExportVouchersSummary);
//		row++;
//
//		col = 0;
//
//		//Heading for the categories
//		//T: Used as heading of a table. Keep the word short.
//		setCellTextInBold(row, col++, msg.exporterDataAccounttype);
//		setCellTextInBold(row, col++, DataSetVAT.getPurchaseTaxString());
//		setCellTextInBold(row, col++, DataSetVAT.getPurchaseTaxString());
//		setCellTextInBold(row, col++, msg.productDataNet);
//
//		// Draw a horizontal line
//		for (col = 0; col < 4; col++) {
//			setBorder(row, col, Color.BLACK, false, false, true, false);
//		}
//
//		row++;
//
//		// A column for each Vat value is created 
//		// The VAT summary items are sorted. So first ignore the VAT entries
//		// with 0%. 
//		// If the VAT value is >0%, create a column with heading.
//		for (Iterator<VatSummaryItem> iterator = voucherSummaryCategories.getVoucherSummaryItems().iterator(); iterator.hasNext();) {
//			VatSummaryItem item = iterator.next();
//
//			col = 0;
//			// Round the net and add fill the table cell
//			PriceValue vat = new PriceValue(item.getVat());
//			PriceValue net = new PriceValue(item.getNet());
//
//			setCellText(row, col++, item.getDescription());
//			setCellText(row, col++, item.getVatName());
//			setCellValueAsLocalCurrency(row, col++, vat.asRoundedDouble());
//			setCellValueAsLocalCurrency(row, col++, net.asRoundedDouble());
//
//			// Set the background of the table rows. Use an light and
//			// alternating blue color.
//			if ((row % 2) == 0)
//				setBackgroundColor(0, row, 3, row, "#e8ebed");
//
//			row++;
//
//		}
//
//		// Draw a horizontal line
//		for (col = 0; col < 4; col++) {
//			setBorder(row - 1, col, Color.BLACK, false, false, true, false);
//		}

		// True = Export was successful
		return true;
	}
}
