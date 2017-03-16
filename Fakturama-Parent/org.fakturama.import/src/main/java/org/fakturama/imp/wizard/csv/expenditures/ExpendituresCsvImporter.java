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

package org.fakturama.imp.wizard.csv.expenditures;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.imp.ImportMessages;

import com.ibm.icu.text.MessageFormat;
import com.opencsv.CSVReader;
import com.sebulli.fakturama.calculate.VoucherSummaryCalculator;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dao.ItemAccountTypeDAO;
import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.dto.VoucherSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.model.VoucherItem;
import com.sebulli.fakturama.model.VoucherType;

/**
 * CSV importer for expenditures
 * 
 */
public class ExpendituresCsvImporter {
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	@Translation
	protected Messages msg;
    
    @Inject
    protected Logger log;
    
    @Inject
    private VoucherCategoriesDAO voucherCategoriesDAO;
    
    @Inject
    private ExpendituresDAO expendituresDAO;
    
    @Inject
    private VatCategoriesDAO vatCategoriesDAO;
    
    @Inject
    private ItemAccountTypeDAO itemAccountTypeDAO;
    
    @Inject
    private VatsDAO vatsDAO;
    
	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory;

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { "category", "date", "nr", "documentnr", "name", "item name", "item category", "item price", "item vat", "vat" };
	// The result string
	String result = "";

	// NewLine
	String NL = System.lineSeparator();

	/**
	 * Returns, if a column is in the list of required columns
	 * 
	 * @param columnName
	 *            The name of the columns to test
	 * @return TRUE, if this column is in the list of required columns
	 */
	private boolean isRequiredColumn(String columnName) {

		// Test all columns
		for (int i = 0; i < requiredHeaders.length; i++) {
			if (columnName.equalsIgnoreCase(requiredHeaders[i]))
				return true;
		}
		return false;
	}


	/**
	 * The import procedure
	 * 
	 * @param fileName
	 *            Name of the file to import
	 * @param test
	 *            if true, the dataset are not imported (currently not used)
	 */
	public void importCSV(final String fileName, boolean test) {

		modelFactory = FakturamaModelPackage.MODELFACTORY;
		VATCategory vatCategory = vatCategoriesDAO.findVATCategoryByName(msg.getPurchaseTaxString());
		try {
			if(vatCategory == null) {
				VAT newVat = modelFactory.createVAT();
				newVat.setName(msg.getPurchaseTaxString());
				newVat.setDescription(msg.getPurchaseTaxString());
				newVat.setTaxValue(Double.valueOf(0.0));
				vatCategory = modelFactory.createVATCategory();
				vatCategory.setName(msg.getPurchaseTaxString());
				newVat.setCategory(vatCategory);
				newVat = vatsDAO.save(newVat);
			}
		} catch (FakturamaStoringException e) {
			log.error("can't save new VAT");
			return;
		}

		// Result string
		//T: Importing + .. FILENAME
		result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

		// Count the imported expenditures
		int importedExpenditures = 0;

		// Count the line of the import file
		int lineNr = 0;

		String[] columns;
	
		// Open the existing file
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			 BufferedReader in = new BufferedReader(isr);
			 CSVReader csvr = new CSVReader(in, ';');	) {

			// Read next CSV line
			columns = csvr.readNext();
			
			if (columns.length < 5) {
				//T: Error message
				result += NL + importMessages.wizardImportErrorFirstline;
				return;
			}

		// Read the existing file and store it in a buffer
		// with a fix size. Only the newest lines are kept.

			// Store the last expenditure. This is used to import
			// 2 lines with 2 expenditure items but only one expenditure.
			Voucher lastExpenditure = null;

			// Read line by line
			String[] cells;
			while ((cells = csvr.readNext()) != null) {
				lineNr++;

				Voucher expenditure = modelFactory.createVoucher();
				expenditure.setVoucherType(VoucherType.EXPENDITURE);
				VoucherItem expenditureItem = modelFactory.createVoucherItem();
				expenditureItem.setItemVoucherType(VoucherType.EXPENDITURE);
				Properties prop = new Properties();

				// Dispatch all the cells into a property
				for (int col = 0; col < cells.length; col++) {
					if (col < columns.length && isRequiredColumn(columns[col])) {
						prop.setProperty(columns[col].toLowerCase(), cells[col]);
					}
				}

				// Test if all columns are used
				if ((prop.size() > 0) && (prop.size() != requiredHeaders.length)) {
					for (int i = 0; i < requiredHeaders.length; i++) {
						if (!prop.containsKey(requiredHeaders[i]))
							//T: Format: LINE: xx: NO DATA IN COLUMN yy FOUND.
							result += NL 
							+ MessageFormat.format(importMessages.wizardImportErrorNodatafound, Integer.toString(lineNr) 
						    + "\"" + requiredHeaders[i] + "\""); 
					}
				} else {
					// Date is a must.
					if (!prop.getProperty("date").isEmpty()) {

						// Fill the expenditure data set
						expenditure.setName(prop.getProperty("name"));
						VoucherCategory account = voucherCategoriesDAO.findByName(prop.getProperty("category"));
						if(account == null) {
							account = modelFactory.createVoucherCategory();
							account.setName(prop.getProperty("category"));
						}
						expenditure.setAccount(account);
						expenditure.setVoucherDate(DataUtils.getInstance().getCalendarFromDateString(prop.getProperty("date")).getTime());
						expenditure.setVoucherNumber(prop.getProperty("nr"));
						expenditure.setDocumentNumber(prop.getProperty("documentnr"));

						// Test, if the last line was the same expenditure
						boolean repeatedExpenditure = false;

						if (lastExpenditure != null && lastExpenditure.isSameAs(expenditure)) {
							repeatedExpenditure = true;
						}

						// If the data set is already existing, stop the CSV import
						if (!repeatedExpenditure) {
							Voucher testExpenditure = expendituresDAO.findOrCreate(expenditure, true);
							if (testExpenditure != null) {
								//T: Error message Dataset is already imported
								String message = MessageFormat.format(importMessages.wizardImportErrorAlreadyimported, prop.getProperty("name"), prop.getProperty("date"));
								result += NL + message;
								break;
							}
						}

						// Fill the expenditure item with data
						expenditureItem.setName(prop.getProperty("item name"));
						ItemAccountType newAccountType = itemAccountTypeDAO.findByName(prop.getProperty("item category"));
						if(newAccountType == null) {
							newAccountType = modelFactory.createItemAccountType();
							newAccountType.setName(prop.getProperty("item category"));
						}
						expenditureItem.setAccountType(newAccountType);
						expenditureItem.setPrice(DataUtils.getInstance().StringToDouble(prop.getProperty("item price")));

						String vatName = prop.getProperty("item vat");

						Double vatValue = DataUtils.getInstance().StringToDouble(prop.getProperty("vat"));
						VAT vat = modelFactory.createVAT();
						vat.setTaxValue(vatValue);
						vat.setName(vatName);
						vat.setDescription(vatName);
						vat.setCategory(vatCategory);
						vat = vatsDAO.findOrCreate(vat);
						expenditureItem.setVat(vat);

						// Add the expenditure and expenditure item to the data base
//						expenditureItem = Data.INSTANCE.getExpenditureVoucherItems().addNewDataSet(expenditureItem);
//						VoucherEditor.updateBillingAccount (expenditureItem);
						expenditure.addToItems(expenditureItem);
						expenditure = expendituresDAO.save(expenditure);

						// Add the item to the item string
//						String oldItems = expenditure.getStringValueByKey("items");
//						String newItem = expenditureItem.getStringValueByKey("id");
						if (DateUtils.isSameDay(expenditure.getDateAdded(), Calendar.getInstance().getTime())) {
//							oldItems += ",";
						} else {
							importedExpenditures++;
						}

						// Recalculate the total sum of all items and set the total value
						VoucherSummaryCalculator calculator = new VoucherSummaryCalculator();
						VoucherSummary summary = calculator.calculate(expenditure);
						// Get the total result
						Double total = summary.getTotalGross().getNumber().doubleValue();

						// Update the text widget
						expenditure.setTotalValue(total);
						expenditure.setPaidValue(total);

						expendituresDAO.update(expenditure);

						// Set the reference of the last expenditure to this one
						lastExpenditure = expenditure;
					}
				}
			}
			
			// The result string
			//T: Message: xx VOUCHERS HAVE BEEN IMPORTED 
			result += NL + Integer.toString(importedExpenditures) + " " + importMessages.wizardImportInfoVouchersimported;

		}
		catch (UnsupportedEncodingException e) {
			log.error(e, "Unsupported UTF-8 encoding");
			result += NL + "Unsupported UTF-8 encoding";
			return;
		}
		catch (FileNotFoundException e) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorFilenotfound;
			return;
		}
		catch (IOException e1) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorFirstline;
			return;
		} catch (FakturamaStoringException e) {
			log.error("can't save or update imported expenditure");
		}
	}

	public String getResult() {
		return result;
	}

}
