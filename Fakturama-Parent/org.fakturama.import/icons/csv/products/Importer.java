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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import au.com.bytecode.opencsv.CSVReader;

/**
 * CSV importer
 * 
 */
public class Importer {

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { "itemnr", "name", "category", "description", "price1", "price2", "price3", "price4", "price5",
			 "block1", "block2", "block3", "block4", "block5", "vat", "options", "weight", "unit", 
			 "date_added", "picturename", "quantity", "webshopid", "qunit" };

	
	// The result string
	String result = "";

	// NewLine
	String NL = "";

	/**
	 * Constructor
	 */
	public Importer() {
		// String for a new line
		NL = OSDependent.getNewLine();
	}

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
	 * @param updateExisting
	 *            if true, also existing entries will be updated
	 * @param importEmptyValues
	 *            if true, also empty values will be updated
	 */
	public void importCSV(final String fileName, boolean test,boolean updateExisting, boolean importEmptyValues) {

		// Result string
		//T: Importing + .. FILENAME
		result = _("Importing") + " " + fileName;

		// Count the imported products
		int importedProducts = 0;
		int updatedProducts = 0;

		// Count the line of the import file
		int lineNr = 0;

		// Open the existing file
		InputStreamReader isr;
		BufferedReader in = null;
		CSVReader csvr = null;
		
		try {
			isr = new InputStreamReader(new FileInputStream(fileName),"UTF-8");
			in = new BufferedReader(isr);
			csvr = new CSVReader(in, ';');
		}
		catch (UnsupportedEncodingException e) {
			Logger.logError(e, "Unsupported UTF-8 encoding");
			result += NL + "Unsupported UTF-8 encoding";
			return;
		}
		catch (FileNotFoundException e) {
			//T: Error message
			result += NL + _("File not found.");
			return;
		}

		String[] columns;

		// Read the first line
		try {
			
			// Read next CSV line
			columns = csvr.readNext();
			
			if (columns.length < 5) {
				//T: Error message
				result += NL + _("Error reading the first line");
				return;
			}
		}
		catch (IOException e1) {
			//T: Error message
			result += NL + _("Error reading the first line");
			return;
		}

		// Read the existing file and store it in a buffer
		// with a fix size. Only the newest lines are kept.
		try {

			// Read line by line
			String[] cells;
			while ((cells = csvr.readNext()) != null) {
				lineNr++;

				DataSetProduct product = new DataSetProduct();
				Properties prop = new Properties();

				// Dispatch all the cells into a property
				for (int col = 0; col < cells.length; col++) {
					if (col < columns.length) {

						if (isRequiredColumn(columns[col])) {
							prop.setProperty(columns[col].toLowerCase(), cells[col]);
						}
					}
				}

				// Test, if all columns are used
				if ((prop.size() > 0) && (prop.size() != requiredHeaders.length)) {
					for (int i = 0; i < requiredHeaders.length; i++) {
						if (!prop.containsKey(requiredHeaders[i]))
							//T: Format: LINE: xx: NO DATA IN COLUMN yy FOUND.
							result += NL + _("Line") + ": " + Integer.toString(lineNr) + ": " + 
							//T: Format: LINE: xx: NO DATA IN COLUMN yy FOUND.
							_("No Data in Column") + " \"" + requiredHeaders[i] + "\" " + 
							//T: Format: LINE: xx: NO DATA IN COLUMN yy FOUND.
							_("found.");
					}
				}
				else {

					product.setStringValueByKey("itemnr", prop.getProperty("itemnr"));
					product.setStringValueByKey("name", prop.getProperty("name"));
					product.setStringValueByKey("category", prop.getProperty("category"));
					product.setStringValueByKey("description", prop.getProperty("description"));
					product.setStringValueByKey("price1", prop.getProperty("price1"));
					product.setStringValueByKey("price2", prop.getProperty("price2"));
					product.setStringValueByKey("price3", prop.getProperty("price3"));
					product.setStringValueByKey("price4", prop.getProperty("price4"));
					product.setStringValueByKey("price5", prop.getProperty("price5"));
					product.setStringValueByKey("block1", prop.getProperty("block1"));
					product.setStringValueByKey("block2", prop.getProperty("block2"));
					product.setStringValueByKey("block3", prop.getProperty("block3"));
					product.setStringValueByKey("block4", prop.getProperty("block4"));
					product.setStringValueByKey("block5", prop.getProperty("block5"));

					product.setStringValueByKey("options", prop.getProperty("options"));
					product.setStringValueByKey("weight", prop.getProperty("weight"));
					product.setStringValueByKey("unit", prop.getProperty("unit"));

					if (prop.getProperty("date_added").isEmpty())
						product.setStringValueByKey("date_added", DataUtils.DateAsISO8601String());
					else
						product.setStringValueByKey("date_added", DataUtils.DateAsISO8601String(prop.getProperty("date_added")));
					
					product.setStringValueByKey("picturename", prop.getProperty("picturename"));
					product.setStringValueByKey("quantity", prop.getProperty("quantity"));
					product.setStringValueByKey("webshopid", prop.getProperty("webshopid"));
					product.setStringValueByKey("qunit", prop.getProperty("qunit"));

					String vatName = prop.getProperty("item vat");

					Double vatValue = DataUtils.StringToDouble(prop.getProperty("vat"));
					DataSetVAT vat = new DataSetVAT(vatName, DataSetVAT.getPurchaseTaxString(), vatName, vatValue);
					vat = Data.INSTANCE.getVATs().addNewDataSetIfNew(vat);
					product.setIntValueByKey("vatid", vat.getIntValueByKey("id"));

					// Add the product to the data base
					if (Data.INSTANCE.getProducts().isNew(product)) {
						importedProducts++;
						Data.INSTANCE.getProducts().addNewDataSet(product);
					}
					else if (updateExisting)
					{
						// Update data
						DataSetProduct existingProduct = Data.INSTANCE.getProducts().getExistingDataSet(product);
						UniDataSet.copy(existingProduct, product, importEmptyValues);
						
						updatedProducts ++;
						// Update the modified product data
						Data.INSTANCE.getProducts().updateDataSet(existingProduct);

					}
				}

			}
			
			// Refresh the views
			ApplicationWorkbenchAdvisor.refreshView(ViewVatTable.ID);
			ApplicationWorkbenchAdvisor.refreshView(ViewProductTable.ID);
			
			// The result string
			//T: Message: xx Products HAVE BEEN IMPORTED 
			result += NL + Integer.toString(importedProducts) + " " + _("products have been imported.");
			if (updatedProducts > 0)
				result += NL + Integer.toString(updatedProducts) + " " + _("products have been updated.");
				

		}
		catch (IOException e) {
			//T: Error message
			result += NL + _("Error opening the file.");
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
				if (in != null) {
					in.close();
				}
				if (csvr != null) {
					csvr.close();
				}
			}
			catch (IOException e) {
				result += NL + _("Error closing a file.");
			}
		}

	}

	public String getResult() {
		return result;
	}

}
