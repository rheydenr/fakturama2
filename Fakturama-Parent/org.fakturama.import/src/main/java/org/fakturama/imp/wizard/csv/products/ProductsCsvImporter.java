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

package org.fakturama.imp.wizard.csv.products;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptionPage;

import com.opencsv.CSVReader;
import com.sebulli.fakturama.dao.ProductCategoriesDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.VAT;

/**
 * CSV importer
 * 
 */
public class ProductsCsvImporter {
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	private ProductsDAO productsDAO;
	
	@Inject
	private ProductCategoriesDAO productCategoriesDAO;
	
	@Inject
	private VatsDAO vatsDAO;
    
    @Inject
    protected Logger log;
    
	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory;

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { "itemnr", "name", "category", "description", "price1", "price2", "price3", "price4", "price5",
			 "block1", "block2", "block3", "block4", "block5", "vat", "options", "weight", "unit", 
			 "date_added", /*"picturename",*/ "quantity", "webshopid", "qunit" };

	// The result string
	String result = " ";
	
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
		return Arrays.stream(requiredHeaders).anyMatch(col -> columnName.equalsIgnoreCase(col));
	}


	/**
	 * The import procedure
	 * 
	 * @param fileName
	 *            Name of the file to import
	 * @param test
	 *            if true, the dataset are not imported (currently not used)
	 * @param updateExisting
	 *            if true, also existing entries will be updated
	 * @param importEmptyValues
	 *            if true, also empty values will be updated
	 */
	public void importCSV(final String fileName, boolean test, ImportOptionPage optionPage) {
		boolean updateExisting = optionPage.getUpdateExisting(); 
		//boolean importEmptyValues = optionPage.getUpdateWithEmptyValues();
		char separator = StringUtils.defaultIfBlank(optionPage.getSeparator(), ";").charAt(0);
		char quoteChar = StringUtils.isNotBlank(optionPage.getQuoteChar()) ? optionPage.getQuoteChar().charAt(0) : '"';
		modelFactory = FakturamaModelPackage.MODELFACTORY;
		Date today = Calendar.getInstance().getTime();

		// Result string
		//T: Importing + .. FILENAME
		result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

		// Count the imported products
		int importedProducts = 0;
		int updatedProducts = 0;

		// Count the line of the import file
		int lineNr = 0;

		String[] columns;

		// Open the existing file
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
				 BufferedReader in = new BufferedReader(isr);
				 CSVReader csvr = new CSVReader(in, separator, quoteChar);	) {
				
				// Read next CSV line
				columns = csvr.readNext();
				
				if (columns.length < 5) {
					//T: Error message
					result += NL + importMessages.wizardImportErrorFirstline;
					return;
				}

		// Read the existing file and store it in a buffer
		// with a fixed size. Only the newest lines are kept.

			// Read line by line
			String[] cells;
			while ((cells = csvr.readNext()) != null) {
				lineNr++;

				Product product = modelFactory.createProduct();
				Properties prop = new Properties();

				// Dispatch all the cells into a property
				for (int col = 0; col < cells.length; col++) {
					if (col < columns.length && isRequiredColumn(StringUtils.trim(columns[col]))) {
						prop.setProperty(StringUtils.trim(columns[col]).toLowerCase(), cells[col]);
					}
				}

				// Test, if all columns are used
				if (prop.size() > 0 && (prop.size() != requiredHeaders.length)) {
					for (int i = 0; i < requiredHeaders.length; i++) {
						if (!prop.containsKey(requiredHeaders[i]))
							//T: Format: LINE: xx: NO DATA IN COLUMN yy FOUND.
							result += NL 
							+ MessageFormat.format(importMessages.wizardImportErrorNodatafound, Integer.toString(lineNr) 
						    + "\"" + requiredHeaders[i] + "\""); 
					}
				} else {
					product.setItemNumber(prop.getProperty("itemnr"));
					product.setName(prop.getProperty("name"));
					product.setWebshopId(StringUtils.isNumeric(prop.getProperty("webshopid")) ? Long.parseLong(prop.getProperty("webshopid")) : Long.valueOf(1));
					if(updateExisting) {
					    product = productsDAO.findOrCreate(product);
					}
					ProductCategory category = productCategoriesDAO.getCategory(prop.getProperty("category"), false);
					product.setCategories(category);
					product.setDescription(prop.getProperty("description"));
					product.setPrice1(DataUtils.getInstance().StringToDouble(prop.getProperty("price1")));
					product.setPrice2(DataUtils.getInstance().StringToDouble(prop.getProperty("price2")));
					product.setPrice3(DataUtils.getInstance().StringToDouble(prop.getProperty("price3")));
					product.setPrice4(DataUtils.getInstance().StringToDouble(prop.getProperty("price4")));
					product.setPrice5(DataUtils.getInstance().StringToDouble(prop.getProperty("price5")));
					product.setBlock1(StringUtils.isNumeric(prop.getProperty("block1")) ? Integer.parseInt(prop.getProperty("block1")) : null);
					product.setBlock2(StringUtils.isNumeric(prop.getProperty("block2")) ? Integer.parseInt(prop.getProperty("block2")) : null);
					product.setBlock3(StringUtils.isNumeric(prop.getProperty("block3")) ? Integer.parseInt(prop.getProperty("block3")) : null);
					product.setBlock4(StringUtils.isNumeric(prop.getProperty("block4")) ? Integer.parseInt(prop.getProperty("block4")) : null);
					product.setBlock5(StringUtils.isNumeric(prop.getProperty("block5")) ? Integer.parseInt(prop.getProperty("block5")) : null);

// FIXME implement!
//					ProductOptions productOption = modelFactory.createProductOptions();
//					productOption.setAttributeValue(prop.getProperty("options"));
//					List<ProductOptions> productOptions = new ArrayList<>();
//					productOptions.add(productOption);
//					product.setAttributes(productOptions);
					product.setWeight(DataUtils.getInstance().StringToDouble(prop.getProperty("weight")));
					product.setSellingUnit(StringUtils.isNumeric(prop.getProperty("unit")) ? Integer.parseInt(prop.getProperty("unit")) : Integer.valueOf(1));

					if (prop.getProperty("date_added").isEmpty()) {
						product.setDateAdded(today);
					} else {
						product.setDateAdded(DataUtils.getInstance().getCalendarFromDateString(prop.getProperty("date_added")).getTime());
						product.setModified(today);
					}
					
//					product.setPictureName(prop.getProperty("picturename"));
					product.setQuantity(DataUtils.getInstance().StringToDouble(prop.getProperty("quantity")));
					product.setQuantityUnit(prop.getProperty("qunit"));

					String vatName = prop.getProperty("item vat");

					Double vatValue = DataUtils.getInstance().StringToDouble(prop.getProperty("vat"));
					VAT prodVat = modelFactory.createVAT();
					prodVat.setName(vatName);
					prodVat.setTaxValue(vatValue);
					prodVat.setDescription(msg.getPurchaseTaxString());
					prodVat = vatsDAO.findOrCreate(prodVat);
					
					product.setVat(prodVat);

					// Add the product to the data base
					if (DateUtils.isSameDay(product.getDateAdded(), today)) {
						importedProducts++;
					} else if (updateExisting || DateUtils.isSameDay(product.getModified(), today)) {
						// Update data
						updatedProducts++;
					}
					// Update the modified product data
					productsDAO.update(product);
				}
			}

			// The result string
			//T: Message: xx Products HAVE BEEN IMPORTED 
			result += NL + Integer.toString(importedProducts) + " " + importMessages.wizardImportInfoProductsimported;
			if (updatedProducts > 0)
				result += NL + Integer.toString(updatedProducts) + " " + importMessages.wizardImportInfoProductsupdated;

		}
		catch (IOException e) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorOpenfile;
		}
		catch (FakturamaStoringException e) {
			log.error(e, "cant't store import data.");
		}
	}

	public String getResult() {
		return result;
	}

}
