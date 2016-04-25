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

package org.fakturama.export.wizard.sales;


import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.CellFormatter;
import org.fakturama.export.wizard.ExportWizardPageStartEndDate;
import org.fakturama.export.wizard.OOCalcExporter;
import org.javamoney.moneta.Money;
import org.odftoolkit.odfdom.type.Color;

import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.VatSummaryItem;
import com.sebulli.fakturama.dto.VatSummarySetManager;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;


/**
 * This class exports all invoices in an OpenOffice.org 
 * Calc table. 
 * 
 */
public class SalesExporter extends OOCalcExporter {

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;
	    
	@Inject
	@Preference(nodePath = "/instance/com.sebulli.fakturama.rcp")
	private IEclipsePreferences eclipsePrefs;
	
	@Inject
	private DocumentsDAO documentsDao;

    @Inject
    private ExpendituresDAO expendituresDAO;

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
	 * @param paid 
	 * @param showZeroVatColumn 
	 * 
	 * @return
	 * 			True, if the export was successful
	 */
	public boolean export(boolean showZeroVatColumn, boolean paid) {

		// Try to generate a spreadsheet
		if (!createSpreadSheet())
			return false;
		
		usePaidDate = eclipsePrefs.getBoolean(Constants.PREFERENCES_EXPORTSALES_PAIDDATE, true);
		this.exportPaid = paid;

		// Get all undeleted documents (sorted)
		List<Document> documents;
		if (this.exportPaid) {
			documents = documentsDao.findPaidDocumentsInRange(usePaidDate,
					(startDate != null && !doNotUseTimePeriod ? startDate.getTime() : null),
					(endDate != null && !doNotUseTimePeriod ? endDate.getTime() : null));
		} else {
			documents = documentsDao.findUnpaidDocumentsInRange(usePaidDate,
					(startDate != null && !doNotUseTimePeriod ? startDate.getTime() : null),
					(endDate != null && !doNotUseTimePeriod ? endDate.getTime() : null));
		}
		
		
		// Get all undeleted expenditures
//		List<Voucher> expenditures = expendituresDAO.findAllExpendituresSorted();

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

		// Table heading
		if (this.exportPaid)
			//T: Sales Exporter - Text in the Calc document for the Earnings
			setCellTextInBold(row++, 0, msg.exporterDataEarnings);
		else
			//T: Sales Exporter - Text in the Calc document for the Earnings
			setCellTextInBold(row++, 0, msg.exporterDataInvoicesUnpaid);
		
		
		row++;

		// Create a VAT summary set manager that collects all VAT
		// values of all documents
		VatSummarySetManager vatSummarySetAllDocuments = new VatSummarySetManager();

		// Table column headings
		int headLine = row;
		setCellTextInBold(row, col++, msg.exporterDataPayday);
		setCellTextInBold(row, col++, msg.exporterDataInvoiceno);
		setCellTextInBold(row, col++, msg.exporterDataInvoicedate);
		setCellTextInBold(row, col++, msg.commonFieldFirstname);
		setCellTextInBold(row, col++, msg.commonFieldLastname);
		setCellTextInBold(row, col++, msg.commonFieldCompany);
		setCellTextInBold(row, col++, msg.exporterDataVatid);
		setCellTextInBold(row, col++, msg.commonFieldCountry);
		setCellTextInBold(row, col++, msg.exporterDataInvoiceValue);
		setCellTextInBold(row, col++, msg.exporterDataPayvalue);
		setCellTextInBold(row, col++, msg.exporterDataNetval);
		row++;

		// The documents are exported in 2 runs.
		// First, only the VAT summary of all documents is calculated and
		// the columns are created.
		// Later all the documents are analyzed a second time and then they
		// are exported document by document into the table.
		DocumentSummaryCalculator dsc = new DocumentSummaryCalculator();
//		for (Document document : documents) {
//			if (documentShouldBeExported(document)) {
//				dsc.calculate(document);
//				vatSummarySetAllDocuments.add(document, 1.0);
//			}
//		}
		documents.forEach(doc -> vatSummarySetAllDocuments.add(doc, Double.valueOf(1.0)));

		col = 11;
		columnsWithVatHeading = 0;
		columnsWithNetHeading = 0;
		boolean vatIsNotZero = false;

		// A column for each Vat value is created 
		// The VAT summary items are sorted. So first ignore the VAT entries
		// with 0%. 
		// If the VAT value is >0%, create a column with heading.
		for (VatSummaryItem item : vatSummarySetAllDocuments.getVatSummaryItems()) {

			// Create a column, if the value is not 0%
			if ((item.getVat().getNumber().doubleValue() > 0.001) || vatIsNotZero || showZeroVatColumn) {

				// If the first non-zero VAT column is created,
				// do not check the value any more.
				vatIsNotZero = true;

				// Count the columns
				columnsWithVatHeading++;

				// Create a column heading in bold
				int column = vatSummarySetAllDocuments.getIndex(item) - zeroVatColumns;
				setCellTextInBold(headLine, column + col, item.getVatName());

			}
			else
				// Count the columns with 0% VAT
				zeroVatColumns++;
		}

		// A column for each Net value is created 
		// The Net summary items are sorted. 
		for (VatSummaryItem item : vatSummarySetAllDocuments.getVatSummaryItems()) {

			// Count the columns
			columnsWithNetHeading++;

			// Create a column heading in bold
			int column = vatSummarySetAllDocuments.getIndex(item);
			setCellTextInBold(headLine, columnsWithVatHeading + column + col, msg.productDataNet +"\n" + item.getVatName());
		}

		// Second run.
		// Export the document data
		DocumentSummary documentSummary;
		for (Document document : documents) {
			documentSummary = null;
			if (documentShouldBeExported(document)) {

				// Now analyze document by document
				VatSummarySetManager vatSummarySetOneDocument = new VatSummarySetManager();
				documentSummary = dsc.calculate(document);

				// Calculate the relation between paid value and the value
				// of the invoice. This is used to calculate the VAT.
				// Example.
				// The net sum of the invoice is 100€.
				// Plus 20% VAT: +20€ = Total: 120€.
				//
				// The customer pays only 115€.
				// 
				// Then the paidFactor is 115/120 = 0.9583333..
				// The VAT value in the invoice is also scaled by this 0.958333...
				// to 19.17€
				
				Double paidFactor = Double.valueOf(0.0);
				if(Optional.ofNullable(document.getTotalValue()).orElse(Double.valueOf(0.0)) > 0) {
					paidFactor = document.getPaidValue() / document.getTotalValue();
				}

				// Use the paid value
				vatSummarySetOneDocument.add(document, paidFactor);

				// Fill the row with the document data
				col = 0;
				setCellText(row, col++, DataUtils.getInstance().getFormattedLocalizedDate(document.getPayDate()));
				setCellText(row, col++, document.getName());
				setCellText(row, col++, DataUtils.getInstance().getFormattedLocalizedDate(document.getDocumentDate()));
				Contact addressid = document.getBillingContact();

				// Fill the address columns with the contact that corresponds to the addressid
				if (addressid != null) {
					setCellText(row, col++, addressid.getFirstName());
					setCellText(row, col++, addressid.getName());
					setCellText(row, col++, addressid.getCompany());
					setCellText(row, col++, addressid.getVatNumber());
					if(addressid.getAddress() != null) {
						setCellText(row, col++, addressid.getAddress().getCountryCode());
					} else {
						setCellText(row, col++, " ");
					}
				}
				// ... or use the documents first line
				else {
					setCellText(row, col++, document.getAddressFirstLine());
					col += 4;
				}

				setCellValueAsLocalCurrency(row, col++, Optional.ofNullable(document.getTotalValue()).orElse(Double.valueOf(0.0)));
				setCellValueAsLocalCurrency(row, col++, Optional.ofNullable(document.getPaidValue()).orElse(Double.valueOf(0.0)));

				// Calculate the total VAT of the document
				MonetaryAmount totalVat = Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit());

				// Get all VAT entries of this document and place them into the
				// corresponding column.
				for (VatSummaryItem item : vatSummarySetOneDocument.getVatSummaryItems()) {

					// Get the column
					int column = vatSummarySetAllDocuments.getIndex(item) - zeroVatColumns;

					// If column is <0, it was a VAT entry with 0%
					if (column >= 0) {

						// Round the VAT and add fill the table cell
						totalVat = totalVat.add(item.getVat());
						setCellValueAsLocalCurrency(row, column + (col + 1), item.getVat());
					}
				}

				// Get all net entries of this document and place them into the
				// corresponding column.
				for (VatSummaryItem item : vatSummarySetOneDocument.getVatSummaryItems()) {
					// Get the column
					int column = vatSummarySetAllDocuments.getIndex(item);

					// If column is <0, it was a VAT entry with 0%
					if (column >= 0) {

						// Round the net and add fill the table cell
						//totalVat.add(net.asRoundedDouble());
						setCellValueAsLocalCurrency(row, columnsWithVatHeading + column + (col + 1), item.getNet());
					}
				}

				// Calculate the documents net total (incl. shipping) 
				// by the documents total value and the sum of all VAT values.
				Double net = document.getPaidValue() - totalVat.getNumber().doubleValue();
				setCellValueAsLocalCurrency(row, col++, net);

				// Calculate the documents net total (incl. shipping)
				// a second time, but now use the documents net value,
				// and scale it by the scale factor.
				MonetaryAmount totalNet = documentSummary != null ? documentSummary.getTotalNet() : Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit()); 
				//totalNet += document.getSummary().getShipping().getUnitNet().asDouble();

				Double roundingError = totalNet.getNumber().doubleValue() * paidFactor - net;

				// Normally both results must be equal.
				// If the difference is grater than 1 Cent, display a warning.
				// It could be a rounding error.
				if (Math.abs(roundingError) > 0.01)
					setCellTextInRedBold(row, col + columnsWithVatHeading + columnsWithNetHeading, "Runden prüfen");

				// Set the background of the table rows. Use a light and
				// alternating blue color.
				if ((row % 2) == 0)
					setBackgroundColor( 0, row, col + columnsWithVatHeading + columnsWithNetHeading - 1, row, "#e8ebed");

				row++;
			}
		}

		// Insert a formula to calculate the sum of a column.
		// "sumrow" is the row under the table.
		int sumrow = row;
		int startColumn;

		// If paid documents are exported,
		if (this.exportPaid)
			// show also sum of columns with net value
			startColumn = -1;
		else
			// show also sum of columns with net value, paid value and value on invoice
			startColumn = -3;
		
		
		// Show the sum only, if there are values in the table
		if (sumrow > (headLine + 1)) {
			for (int i = startColumn; i < (columnsWithVatHeading + columnsWithNetHeading); i++) {
				col = 11 + i;
				try {
					// Create formula for the sum. 
					String cellNameBegin = CellFormatter.getCellName(headLine + 1, col);
					String cellNameEnd = CellFormatter.getCellName(row - 1, col);
					setFormula(col, sumrow, "=SUM(" + cellNameBegin + ":" + cellNameEnd + ")");
					setBold(sumrow, col);
				}
				catch (IndexOutOfBoundsException e) {
				}
			}
		}

		// Draw a horizontal line (set the border of the top and the bottom
		// of the table).
		for (col = 0; col < (columnsWithVatHeading + columnsWithNetHeading) + 11; col++) {
			setBorder(headLine, col, Color.BLACK, false, false, true, false);
			setBorder(sumrow, col, Color.BLACK, true, false, false, false);
		}
		
		save();

		// True = Export was successful
		return true;
	}
	
	@Override
	protected String getOutputFileName() {
		return this.exportPaid ? "SalesListExport" : "UnpaidSalesListExport";
	}
}
