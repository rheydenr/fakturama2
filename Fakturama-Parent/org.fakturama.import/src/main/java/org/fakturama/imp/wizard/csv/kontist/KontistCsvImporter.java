/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2020 Martin Reinhardt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Martin Reinhardt - initial API and implementation
 */

package org.fakturama.imp.wizard.csv.kontist;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.function.Predicate;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.imp.ImportMessages;

import com.opencsv.CSVReader;
import com.sebulli.fakturama.calculate.VoucherSummaryCalculator;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.CreditorsDAO;
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dao.ItemAccountTypeDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ReceiptVouchersDAO;
import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.dto.VoucherSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Debitor;
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
public class KontistCsvImporter {
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	@Translation
	protected Messages msg;
    
    @Inject
    protected Logger log;
    
    @Inject
    private CreditorsDAO creditorsDAO;
    
    @Inject
    private DebitorsDAO debitorsDAO;
    
    @Inject
    private VoucherCategoriesDAO voucherCategoriesDAO;
    
    @Inject
    private ExpendituresDAO expendituresDAO;
    
    @Inject
    private ReceiptVouchersDAO receiptVouchersDAO;
    
    @Inject
    private PaymentsDAO paymentsDAO;
    
    @Inject
    private VatCategoriesDAO vatCategoriesDAO;
    
    @Inject
    private ItemAccountTypeDAO itemAccountTypeDAO;
    
    @Inject
    private VatsDAO vatsDAO;
    
    @Inject
    private ContactsDAO contactsDAO;
    
    @Inject
    private IDateFormatterService dateFormatterService;

	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory;
	
	private String BOOKING_DATE = "Buchungsdatum";
	private String VALUTA_DATE = "Wertstellungsdatum";
	private String TYPE = "Transaktionstyp";
	private String CATEGORY = "Kategorie";
	private String AMOUNT = "Betrag";
	private String NAME = "Empfänger";
	private String PURPOSE = "Verwendungszweck";
	private String END2END_ID = "end_to_end_id";
	private String BOOKING_STATUS = "Buchungsstatus";

	private String END2END_ID_NOT_SET = "NOTPROVIDED";

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { BOOKING_DATE, VALUTA_DATE, CATEGORY, TYPE, AMOUNT, NAME, PURPOSE, END2END_ID, BOOKING_STATUS };
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
		VATCategory vatCategory = vatCategoriesDAO.findCategoryByName(msg.getPurchaseTaxString());
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
		// TODO use NIO
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

				Properties prop = new Properties();

				// Dispatch all the cells into a property
				for (int col = 0; col < cells.length; col++) {
					if (col < columns.length && isRequiredColumn(columns[col])) {
						prop.setProperty(columns[col].toLowerCase(), cells[col]);
					}
				}
				
				final String end2endId = prop.getProperty(END2END_ID);
				
				boolean isExpenditure = prop.getProperty("betrag").startsWith("-") ? true : false;

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
					if (!prop.getProperty("buchungsdatum").isEmpty()) {
						
						List<Voucher> allItems = expendituresDAO.findAll();
						allItems.addAll(receiptVouchersDAO.findAll());

						List<Debitor> allCustomers = debitorsDAO.findAll();
						
						Predicate<Voucher> voucherFilter;
						// if end2end ID is not set ignore it for compare and use details
						if (end2endId.equalsIgnoreCase(END2END_ID_NOT_SET)) {
							voucherFilter = voucher -> {
								return voucher.getVoucherDate().compareTo(dateFormatterService.getCalendarFromDateString(prop.getProperty("buchungsdatum")).getTime())==0
										//&& voucher.getAccount().getName().equalsIgnoreCase(prop.getProperty("transaktionstyp"))
										&& voucher.getName().equalsIgnoreCase(prop.getProperty("empfänger"))
										&& voucher.getItems() != null && voucher.getItems().size()>0 && voucher.getItems().get(0).getPrice() 
										== Math.abs(DataUtils.getInstance().StringToDouble(prop.getProperty("betrag")));
							};
						} else {
							voucherFilter = voucher -> voucher.getVoucherNumber().equalsIgnoreCase(end2endId);
						}
						
						List<Voucher> foundItems = allItems.stream().filter(voucherFilter).collect(Collectors.toList());
						// import only new items
						if (foundItems == null || foundItems.size() == 0) {
					
							Voucher expenditure = modelFactory.createVoucher();
							expenditure.setVoucherType(isExpenditure? VoucherType.EXPENDITURE : VoucherType.RECEIPTVOUCHER);
							VoucherItem expenditureItem = modelFactory.createVoucherItem();
							expenditureItem.setItemVoucherType(isExpenditure? VoucherType.EXPENDITURE : VoucherType.RECEIPTVOUCHER);
	
							// Fill the expenditure data set
							expenditure.setName(prop.getProperty("empfänger"));
							if (isExpenditure) {
								Creditor vendor = creditorsDAO.findByName(prop.getProperty("empfänger"));
								if(vendor == null) {
									vendor = modelFactory.createCreditor();
									vendor.setName(prop.getProperty("empfänger"));
									vendor = creditorsDAO.save(vendor);
								}
							} else {
								Debitor customer = debitorsDAO.findByName(prop.getProperty("empfänger"));
								if(customer == null) {
									customer = modelFactory.createDebitor();
									customer.setName(prop.getProperty("empfänger"));
									customer = debitorsDAO.save(customer);
								}
								//expenditure.set
							}
							VoucherCategory account = voucherCategoriesDAO.findByName(prop.getProperty("transaktionstyp"));
							if(account == null) {
								account = modelFactory.createVoucherCategory();
								account.setName(prop.getProperty("transaktionstyp"));
								account = voucherCategoriesDAO.save(account);
							}
							expenditure.setAccount(account);
							expenditure.setVoucherDate(dateFormatterService.getCalendarFromDateString(prop.getProperty("buchungsdatum")).getTime());
	
							
							expenditure.setVoucherNumber(prop.getProperty("end_to_end_id"));
							
	
							// Test, if the last line was the same expenditure
							boolean repeatedExpenditure = false;
	
							if (lastExpenditure != null && lastExpenditure.isSameAs(expenditure)) {
								repeatedExpenditure = true;
							}
	
							// If the data set is already existing, stop the CSV import
							if (repeatedExpenditure) {
								Voucher testExpenditure = expendituresDAO.findOrCreate(expenditure, true);
								if (testExpenditure != null) {
									//T: Error message Dataset is already imported
									String message = MessageFormat.format(importMessages.wizardImportErrorAlreadyimported, prop.getProperty("name"), prop.getProperty("date"));
									result += NL + message;
								}
							} else {
								// Fill the expenditure item with data
								expenditureItem.setName(account.getName());
								if(StringUtils.isNotBlank(prop.getProperty("item category"))) {
									ItemAccountType newAccountType = itemAccountTypeDAO.findByName(prop.getProperty("item category"));
									if(newAccountType == null) {
										newAccountType = modelFactory.createItemAccountType();
										newAccountType.setName(prop.getProperty("item category"));
									}
									expenditureItem.setAccountType(newAccountType);
								}
								expenditureItem.setPrice(Math.abs(DataUtils.getInstance().StringToDouble(prop.getProperty("betrag"))));
		
								String category = prop.getProperty("kategorie");
								if (category != null && category.contains("%")) {
									Double vatValue = DataUtils.getInstance().StringToDouble(category.split(" ")[1].split("%")[0]);
									VAT vat = modelFactory.createVAT();
									vat.setTaxValue(vatValue);
									vat.setName(category);
									vat.setDescription(category);
									vat.setCategory(vatCategory);
									vat = vatsDAO.findOrCreate(vat);
									expenditureItem.setVat(vat);
								}
								
								expenditure.addToItems(expenditureItem);
								expenditure = expendituresDAO.save(expenditure);
		
								importedExpenditures++;
		
								// Recalculate the total sum of all items and set the total value
								VoucherSummaryCalculator calculator = new VoucherSummaryCalculator();
								VoucherSummary summary = calculator.calculate(expenditure);
								// Get the total result
								Double total = Math.abs(summary.getTotalGross().getNumber().doubleValue());
		
								// Update the text widget
								expenditure.setTotalValue(total);
								expenditure.setPaidValue(total);
		
								expendituresDAO.update(expenditure);
		
								// Set the reference of the last expenditure to this one
								lastExpenditure = expenditure;
							}
						}
					}
				}
			}
			
			// The result string
			//T: Message: xx VOUCHERS HAVE BEEN IMPORTED 
			if (importedExpenditures>0) {
				result += NL + Integer.toString(importedExpenditures) + " " + importMessages.wizardImportInfoVouchersimported;
			} else {
				result += NL + importMessages.wizardImportErrorAlreadyimported;
			}
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
		} catch (Exception e) {
			log.error(e, "Unknown error occurred");
			result += NL + "Unknown error occurred";
			return;
		}
	}

	public String getResult() {
		return result;
	}

}