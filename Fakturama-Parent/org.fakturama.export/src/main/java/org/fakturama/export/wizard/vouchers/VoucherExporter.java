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


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.fakturama.export.wizard.CellFormatter;
import org.fakturama.export.wizard.ExportWizardPageStartEndDate;
import org.fakturama.export.wizard.OOCalcExporter;
import org.javamoney.moneta.Money;
import org.odftoolkit.odfdom.type.Color;

import com.sebulli.fakturama.calculate.VoucherSummaryCalculator;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dao.ReceiptVouchersDAO;
import com.sebulli.fakturama.dto.VatSummaryItem;
import com.sebulli.fakturama.dto.VoucherSummary;
import com.sebulli.fakturama.dto.VoucherSummarySetManager;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherItem;


/**
 * This class exports all vouchers in an OpenOffice.org 
 * Calc table. 
 * 
 */
public class VoucherExporter extends OOCalcExporter {
	
	public static final int SUPPLIER = 1;
	public static final int CUSTOMER = 2;
	public static final int ALL = 3;

	@Inject
	private IEclipseContext ctx;

    @Inject
    private ExpendituresDAO expendituresDAO;
    
    @Inject
    private ReceiptVouchersDAO receiptVouchersDAO;
    
    @Inject
    private IDateFormatterService dateFormatterService;

	// Settings from the preference page
	private boolean showVoucherSumColumn;
	private boolean showZeroVatColumn;
	private String outputFileName;


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
		}
		
		if(ctx.get(Constants.PARAM_END_DATE) != null) {
			endDate = (GregorianCalendar) ctx.get(Constants.PARAM_END_DATE);
		}
		doNotUseTimePeriod = (boolean) ctx.get(ExportWizardPageStartEndDate.WIZARD_DATESELECT_DONTUSETIMEPERIOD);

		this.showVoucherSumColumn = (Boolean)ctx.get(VoucherExportOptionPage.SHOW_VOUCHER_SUM_COLUMN);
		this.showZeroVatColumn = (Boolean)ctx.get(VoucherExportOptionPage.SHOW_ZERO_VAT_COLUMN);
	}

	/**
	 * 	Do the export job.
	 * 
	 * @return
	 * 			True, if the export was successful
	 */
	public boolean export(String title, int type) {
		
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
		try {
			progressMonitorDialog.run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					// TODO skeleton for further implementation with progress monitor
					
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String customerSupplier = "";
		List<Voucher> vouchers = new ArrayList<>();
		if(BooleanUtils.toBoolean((Boolean) ctx.get(ExportWizardPageStartEndDate.WIZARD_DATESELECT_DONTUSETIMEPERIOD))) {
			startDate = null;
			endDate = null;
		}
		
		switch (type) {
		case SUPPLIER:
			customerSupplier = msg.editorVoucherExpenditureFieldSupplier;
			vouchers.addAll(expendituresDAO.findVouchersInDateRange(startDate, endDate));
			outputFileName = msg.commandExpenditurevouchersName;
			break;
		case CUSTOMER:
			customerSupplier = msg.editorVoucherReceiptFieldCustomer;
			vouchers.addAll(receiptVouchersDAO.findVouchersInDateRange(startDate, endDate));
			outputFileName = msg.commandReceiptvouchersName;
			break;
		default:
			customerSupplier = String.format("%s / %s", msg.editorVoucherExpenditureFieldSupplier, msg.editorVoucherReceiptFieldCustomer);
			vouchers.addAll(expendituresDAO.findVouchersInDateRange(startDate, endDate));
			vouchers.addAll(receiptVouchersDAO.findVouchersInDateRange(startDate, endDate));
			
		// Sort the vouchers by category and date --> this is normally done by database,
		// but in this case we have to do this manually 
			vouchers.stream().sorted(Comparator.comparing(
					Voucher::getAccount, (v1, v2) -> {
						if(v1 == null) return (v2 == null) ? 0 : -1;
						else if(v2 == null) return 1;
						else return v1.getName().compareTo(v2.getName());
					}).thenComparing(Voucher::getVoucherDate));
			outputFileName = String.format("%s_%s", msg.commandExpenditurevouchersName, msg.commandReceiptvouchersName);
			break;
		}
		
		// if no data, return immediately
		if(vouchers.isEmpty()) {
			MessageDialog.openInformation(shell, msg.dialogMessageboxTitleInfo, exportMessages.wizardCommonNodata);
			return true;
		}

		// Try to generate a spreadsheet
		if (!createSpreadSheet())
			return false;

		// Count the columns that contain a VAT and net value 
		int columnsWithVatHeading = 0;
		int columnsWithNetHeading = 0;

		// Count the columns that contain a VAT value of 0% 
		int zeroVatColumns = 0;

		// Fill the first 4 rows with the company information
		fillCompanyInformation(0);
		fillTimeIntervall(5);
		
		// Counter for the current row and columns in the Calc document
		int row = 9;
		int col = 0;

		// Set the title
		setCellTextInBold(row++, 0, title);
		row++;

		// Table column headings
		int headLine = row;
		//T: Used as heading of a table. Keep the word short.
		setCellTextInBold(row, col++, msg.commonFieldCategory);
		setCellTextInBold(row, col++, msg.commonFieldDate);
		setCellTextInBold(row, col++, msg.exporterDataVoucher);
		setCellTextInBold(row, col++, msg.exporterDataDocno);

		// Customer or supplier
		setCellTextInBold(row, col++, customerSupplier);
		setCellTextInBold(row, col++, msg.commonFieldText);
		setCellTextInBold(row, col++, msg.exporterDataAccounttype);

		if (showVoucherSumColumn) {
			setCellTextInBold(row, col++, msg.productDataNet);
			setCellTextInBold(row, col++, msg.productDataGross);
		}

		row++;
		int columnOffset = col;

		// The vouchers are exported in 2 runs.
		// First, only the summary of all vouchers is calculated and
		// the columns are created.
		// Later all the vouchers are analyzed a second time and then they
		// are exported voucher by voucher into the table.
		VoucherSummarySetManager voucherSummarySetAllVouchers = createSummarySet(vouchers, false);

		boolean vatIsNotZero = false;

		col = columnOffset;
		columnsWithVatHeading = 0;
		columnsWithNetHeading = 0;

		// A column for each VAT value is created 
		// The VAT summary items are sorted. So first ignore the VAT entries
		// with 0%. 
		// If the VAT value is >0%, create a column with heading.
		for (VatSummaryItem item : voucherSummarySetAllVouchers.getVatSummaryItems()) {

			// Create a column, if the value is not 0%
			if (vatIsNotZero || showZeroVatColumn || item.getVat() != null && Math.abs(item.getVat().getNumber().doubleValue()) > 0.001) {

				// If the first non-zero VAT column is created,
				// do not check the value any more.
				vatIsNotZero = true;

				// Count the columns
				columnsWithVatHeading++;

				// Create a column heading in bold
				int column = voucherSummarySetAllVouchers.getIndex(item) - zeroVatColumns;

				// Add VAT name and description and use 2 lines
				String text = item.getVatName();
				String description = item.getDescription();

				if (!description.isEmpty())
					text += "\n" + description;

				setCellTextInBold(headLine, column + columnOffset, text);
			}
			else {
				// Count the columns with 0% VAT
				zeroVatColumns++;
			}
		}

		// A column for each Net value is created 
		// The Net summary items are sorted. 
		for (VatSummaryItem item : voucherSummarySetAllVouchers.getVatSummaryItems()) {

			// Count the columns
			columnsWithNetHeading++;

			// Create a column heading in bold
			int column = voucherSummarySetAllVouchers.getIndex(item);

			// Add VAT name and description and use 2 lines
			String text = msg.productDataNet + "\n" + item.getVatName();
			String description = item.getDescription();

			if (!description.isEmpty())
				text += "\n" + description;

			setCellTextInBold(headLine, columnsWithVatHeading + column + columnOffset, text);
		}
		
		setOptimalheight(headLine);

		int voucherIndex = 0;
		VoucherSummaryCalculator calc = ContextInjectionFactory.make(VoucherSummaryCalculator.class, ctx);

		// Second run.
		// Export the voucher data
		for (Voucher voucher : vouchers) {
			if (isInTimeIntervall(voucher)) {
				List<VoucherItem> items = voucher.getItems().stream().sorted(Comparator.comparingInt(VoucherItem::getPosNr)).collect(Collectors.toList());
				
				for (int voucherItemIndex = 0; voucherItemIndex < items.size(); voucherItemIndex++) {
					VoucherItem voucherItem = items.get(voucherItemIndex);
					// Now analyze voucher by voucher
					VoucherSummarySetManager vatSummarySetOneVoucher = ContextInjectionFactory.make(VoucherSummarySetManager.class, ctx);
					MonetaryAmount paidValue = Money.of(voucher.getPaidValue(), DataUtils.getInstance().getDefaultCurrencyUnit());
					MonetaryAmount totalValue = Money.of(voucher.getTotalValue(), DataUtils.getInstance().getDefaultCurrencyUnit());
					VoucherSummary voucherSummaryValue = calc.calculate(items, paidValue, totalValue, 
							BooleanUtils.toBoolean(voucher.getDiscounted()));
//					voucher.calculate();

					// Add the voucher to the VAT summary
					// +1 because we use the index as posNo which starts at 1 (only necessary for the VoucherSummarySetManager)
					vatSummarySetOneVoucher.add(voucher, false, voucherItemIndex + 1);
					
					// Fill the row with the voucher data
					col = 0;

					if (voucherItemIndex == 0) {
						setCellText(row, col++, voucher.getAccount() != null ? voucher.getAccount().getName() : "");
						setCellText(row, col++, dateFormatterService.getFormattedLocalizedDate(voucher.getVoucherDate()));
						setCellText(row, col++, voucher.getVoucherNumber());
						setCellText(row, col++, voucher.getDocumentNumber());
						setCellText(row, col++, voucher.getName());
					} else {
						col += 5;
					}
					
					setCellText(row, col++, voucherItem.getName());
					if(voucherItem.getAccountType() != null) {
						setCellText(row, col, voucherItem.getAccountType().getName());
					}
					col++;

					//setCellValueAsLocalCurrency(xSpreadsheetDocument, spreadsheet, row, col++, document.getDoubleValueByKey("total"));

					// Calculate the total VAT of the voucher
					MonetaryAmount totalVat = Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit());

					// Get all VAT entries of this voucher and place them into the
					// corresponding column.
					for (VatSummaryItem item : vatSummarySetOneVoucher.getVatSummaryItems()) {

						// Get the column
						int column = voucherSummarySetAllVouchers.getIndex(item) - zeroVatColumns;

						// If column is <0, it was a VAT entry with 0%
						if (column >= 0) {

							// Round the VAT and add fill the table cell
							totalVat = totalVat.add(item.getVat());
							setCellValueAsLocalCurrency(row, column + columnOffset, item.getVat());
						}
					}

					// Get all net entries of this voucher and place them into the
					// corresponding column.
					for (VatSummaryItem item : vatSummarySetOneVoucher.getVatSummaryItems()) {
						// Get the column
						int column = voucherSummarySetAllVouchers.getIndex(item);

						// If column is <0, it was a VAT entry with 0%
						if (column >= 0) {

							// Round the net and add fill the table cell
							//totalVat.add(net.asRoundedDouble());
							setCellValueAsLocalCurrency(row, columnsWithVatHeading + column + columnOffset,
									item.getNet()/*.multiply(itemSign)*/);
						}
					}

					// Display the sum of an voucher only in the row of the first
					// voucher item
					if (showVoucherSumColumn && voucherItemIndex == 0) {
						col = columnOffset - 2;
						// Calculate the vouchers net and gross total 
						setCellValueAsLocalCurrency(row, col++, voucherSummaryValue.getTotalNet());
						setCellValueAsLocalCurrency(row, col++, voucherSummaryValue.getTotalGross());
					}

					// Set the background of the table rows. Use an light and
					// alternating blue color.
					if ((voucherIndex % 2) == 0)
						setBackgroundColor(0, row, columnsWithVatHeading + columnsWithNetHeading + columnOffset - 1, row,
								CellFormatter.ALTERNATE_BACKGROUND_COLOR);

					row++;
				}
				voucherIndex++;
			}
		}

		// Insert a formula to calculate the sum of a column.
		// "sumrow" is the row under the table.
		int sumrow = row;

		// Show the sum only, if there are values in the table
		if (sumrow > (headLine + 1)) {
			for (int i = (showVoucherSumColumn ? -2 : 0); i < (columnsWithVatHeading + columnsWithNetHeading); i++) {
				col = columnOffset + i;
				try {
					// Create formula for the sum. 
					String cellNameBegin = CellFormatter.getCellName(headLine + 1, col);
					String cellNameEnd = CellFormatter.getCellName(row - 1, col);
					formatAsCurrency(sumrow, col);
					setFormula(sumrow, col, "=SUM(" + cellNameBegin + ":" + cellNameEnd + ")");
					setBold(sumrow, col);
				}
				catch (IndexOutOfBoundsException e) {
					log.error(e, "No access to cell: " + sumrow + ":" + col);
				}
			}
		}

		// Draw a horizontal line (set the border of the top and the bottom
		// of the table).
		for (col = 0; col < (columnsWithVatHeading + columnsWithNetHeading) + columnOffset; col++) {
			setBorder(headLine, col, Color.BLACK, false, false, true, false);
			setBorder(sumrow, col, Color.BLACK, true, false, false, false);
		}

		// Create a voucher summary set manager that collects all 
		// categories of voucher items
		VoucherSummarySetManager voucherSummaryCategories = createSummarySet(vouchers, true);

		row += 3;
		// Table heading
		
		//T: Sales Exporter - Text in the Calc document
		setCellTextInBold(row++, 0, exportMessages.wizardExportVouchersSummary);
		row++;

		col = 0;

		//Heading for the categories
		//T: Used as heading of a table. Keep the word short.
		setCellTextInBold(row, col++, msg.exporterDataAccounttype);
		setCellTextInBold(row, col++, msg.getPurchaseTaxString());
		setCellTextInBold(row, col++, msg.getPurchaseTaxString());
		setCellTextInBold(row, col++, msg.productDataNet);

		drawHorizontalLine(row);

		row++;

		// A column for each Vat value is created 
		// The VAT summary items are sorted. So first ignore the VAT entries
		// with 0%. 
		// If the VAT value is >0%, create a column with heading.
		for (VatSummaryItem item : voucherSummaryCategories.getVatSummaryItems()) {
			col = 0;
			setCellText(row, col++, item.getDescription());
			setCellText(row, col++, item.getVatName());
			setCellValueAsLocalCurrency(row, col++, item.getVat());
			setCellValueAsLocalCurrency(row, col++, item.getNet());

			// Set the background of the table rows. Use an light and
			// alternating blue color.
			if ((row % 2) == 0)
				setBackgroundColor(0, row, 3, row, CellFormatter.ALTERNATE_BACKGROUND_COLOR);

			row++;
		}

		drawHorizontalLine(row);

		save();
		
		// True = Export was successful
		return true;
	}

	/**
	 * @param row
	 * @return
	 */
	private void drawHorizontalLine(int row) {
		// Draw a horizontal line
		for (int col = 0; col < 4; col++) {
			setBorder(row - 1, col, Color.BLACK, false, false, true, false);
		}
	}

	/**
	 * @param vouchers
	 * @param useCategories
	 */
	private VoucherSummarySetManager createSummarySet(List<Voucher> vouchers,
			boolean useCategories) {
		// Create a voucher summary set manager that collects all voucher VAT
		// values of all vouchers
		VoucherSummarySetManager voucherSummarySetAllVouchers = ContextInjectionFactory.make(VoucherSummarySetManager.class, ctx);
		
		vouchers.stream().filter(voucher -> isInTimeIntervall(voucher)).forEach(voucher -> voucherSummarySetAllVouchers.add((Voucher) voucher, useCategories));

//		for (Voucher voucher : vouchers) {
//			if (isInTimeIntervall(voucher)) {
//					voucherSummarySetAllVouchers.add((Voucher) voucher, useCategories);
//			}
//		}
		
		return voucherSummarySetAllVouchers;
	}
	
	@Override
	protected String getOutputFileName() {
		return outputFileName;
	}
}
