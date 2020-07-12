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

package org.fakturama.export.wizard.csv.products;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.fakturama.wizards.ExporterHelper;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductOptions;


/**
 * This class generates a list with all products
 * 
 */
public class ProductExporter {
	
	@Inject
	private ProductsDAO productsDao;
    
	@Inject
	private INumberFormatterService numberFormatterService;

	/**
	 * 	Do the export job.
	 * 
	 * @param filename
	 * 			The name of the export file
	 * @return
	 * 			True, if the export was successful
	 */
	public String export(String filename) {

		String NEW_LINE = System.lineSeparator();
		SimpleDateFormat sdf = new SimpleDateFormat();
		
		// Create a File object
		Path csvFile = Paths.get(filename);

		// Create a new file
		try (BufferedWriter bos = Files.newBufferedWriter(csvFile, StandardOpenOption.CREATE);){
			
			bos.write(
					//T: Used as heading of a table. Keep the word short.
					"\"id\";"+ 
					"\"itemnr\";"+
					"\"name\";"+
					"\"category\";"+
					"\"description\";"+
					"\"price1\";"+
					"\"price2\";"+
					"\"price3\";"+
					"\"price4\";"+
					"\"price5\";"+
					"\"block1\";"+
					"\"block2\";"+
					"\"block3\";"+
					"\"block4\";"+
					"\"block5\";"+
					"\"vat\";"+
					"\"options\";"+
					"\"weight\";"+
					"\"unit\";"+
					"\"date_added\";"+
					"\"picturename\";"+
					"\"quantity\";"+
					"\"webshopid\";"+
					"\"qunit\";"+
					"\"note\";"+
					"\"costprice\""+
					NEW_LINE);
		
			// Get all undeleted products
			List<Product> products = productsDao.findAll();
			
			// Export the product data
			for (Product product : products) {
				
				// Place the products information into the table
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(product.getId()).append(";")
					.append(ExporterHelper.inQuotes(product.getItemNumber())).append(";")
					.append(ExporterHelper.inQuotes(product.getName())).append(";");
				if(product.getCategories() != null) {
					stringBuffer.append(ExporterHelper.inQuotes(CommonConverter.getCategoryName(product.getCategories(), "/")));
				}
                stringBuffer.append(";")
					.append(ExporterHelper.inQuotes(product.getDescription())).append(";")
					.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getPrice1(),"0.000000")).append(";")
					.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getPrice2(),"0.000000")).append(";")
					.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getPrice3(),"0.000000")).append(";")
					.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getPrice4(),"0.000000")).append(";")
					.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getPrice5(),"0.000000")).append(";")
					.append(ExporterHelper.inQuotes(product.getBlock1() != null ? product.getBlock1().toString() : "")).append(";")
					.append(ExporterHelper.inQuotes(product.getBlock2() != null ? product.getBlock2().toString() : "")).append(";")
					.append(ExporterHelper.inQuotes(product.getBlock3() != null ? product.getBlock3().toString() : "")).append(";")
					.append(ExporterHelper.inQuotes(product.getBlock4() != null ? product.getBlock4().toString() : "")).append(";")
					.append(ExporterHelper.inQuotes(product.getBlock5() != null ? product.getBlock5().toString() : "")).append(";");
				if(product.getVat() != null) {
					stringBuffer.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getVat().getTaxValue(),"0.00"));
				}
				stringBuffer.append(";");
				if(product.getAttributes() != null && !product.getAttributes().isEmpty()) {
					for (ProductOptions productOptions : product.getAttributes()) {
						stringBuffer.append(ExporterHelper.inQuotes(productOptions.getName())).append(";");
					}
				}
				stringBuffer.append(";")
					.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getWeight(),"0.00")).append(";")
					.append(product.getSellingUnit() == null ? "" : product.getSellingUnit()).append(";")
					.append(ExporterHelper.inQuotes(sdf.format(product.getDateAdded()))).append(";")
					.append(";") // picturename is not available here
					.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getQuantity(),"0.00")).append(";")
					.append(product.getWebshopId() == null ? "" : product.getWebshopId()).append(";")
					.append(ExporterHelper.inQuotes(product.getQuantityUnit())).append(";")
					.append(ExporterHelper.inQuotes(product.getNote())).append(";")
					.append(numberFormatterService.DoubleToDecimalFormatedValue(product.getCostPrice(),"0.00")).append(";")
					.append(NEW_LINE);
				bos.write(stringBuffer.toString());
			}bos.flush();bos.close();
		}
		catch (IOException e) {
			return e.getMessage();
		}

		// empty retval = Export was successful
		return "";
	}

}
