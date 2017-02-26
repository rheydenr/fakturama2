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

package org.fakturama.export.wizard.products;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.OOCalcExporter;
import org.odftoolkit.odfdom.type.Color;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Product;


/**
 * This class generates a list with all products
 */
public class ProductExporter extends OOCalcExporter {
    
    private static final int MAX_COUNT_OF_PRICES = 6;

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	private ProductsDAO productsDAO;
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;
	
	/**
	 * 	Do the export job.
	 * 
	 * @return
	 * 			True, if the export was successful
	 */
	public boolean export() {

		// Try to generate a spreadsheet
		if (!createSpreadSheet())
			return false;

		// Get all undeleted products
		List<Product> products = productsDAO.findAll();

		// Counter for the current row and columns in the Calc document
		int row = 0;
		int col = 0;

		setCellTextInBold(row, col++, "ID");
		setCellTextInBold(row, col++, msg.exporterDataItemnumber);
		setCellTextInBold(row, col++, msg.commonFieldName);
		setCellTextInBold(row, col++, msg.commonFieldCategory);
		setCellTextInBold(row, col++, msg.commonFieldDescription);
		for (int i = 1; i < MAX_COUNT_OF_PRICES; i++) {
			setCellTextInBold(row, col++, String.format("%s (%d)", msg.commonFieldPrice, i));
		}
		for (int i = 1; i < MAX_COUNT_OF_PRICES; i++) {
			setCellTextInBold(row, col++, String.format("%s (%d)", msg.commonFieldQuantity, i));
		}
		setCellTextInBold(row, col++, msg.commonFieldVat);
		setCellTextInBold(row, col++, msg.exporterDataOptions);
		setCellTextInBold(row, col++, msg.exporterDataWeight);
		setCellTextInBold(row, col++, msg.exporterDataUnit);
		setCellTextInBold(row, col++, msg.commonFieldDate);
//		setCellTextInBold(row, col++, msg.exporterDataPicture);
		setCellTextInBold(row, col++, msg.commonFieldQuantity);
		setCellTextInBold(row, col++, msg.pageWebshop);
		
		// Draw a horizontal line
		for (int hdrline = 0; hdrline < col; hdrline++) {
			setBorder(row, hdrline, Color.BLACK, false, false, true, false);
		}
		row++;
		
		// Export the product data
		for (Product product : products) {
			
			col = 0;
			
			// Place the products information into the table
			setCellText(row, col++, Long.toString(product.getId()));
			setCellText(row, col++, product.getItemNumber());
			setCellText(row, col++, product.getName());
			setCellText(row, col++, CommonConverter.getCategoryName(product.getCategories(), ""));
			setCellText(row, col++, product.getDescription());
			setCellValueAsLocalCurrency(row, col++, product.getPrice1());
			setCellValueAsLocalCurrency(row, col++, product.getPrice2());
			setCellValueAsLocalCurrency(row, col++, product.getPrice3());
			setCellValueAsLocalCurrency(row, col++, product.getPrice4());
			setCellValueAsLocalCurrency(row, col++, product.getPrice5());
			setCellText(row, col++, Integer.toString(product.getBlock1()));
			setCellText(row, col++, Integer.toString(product.getBlock2()));
			setCellText(row, col++, Integer.toString(product.getBlock3()));
			setCellText(row, col++, Integer.toString(product.getBlock4()));
			setCellText(row, col++, Integer.toString(product.getBlock5()));
			setCellText(row, col++, Double.toString(product.getVat().getTaxValue()));
			setCellText(row, col++, StringUtils.join(product.getAttributes(), ','));
			setCellText(row, col++, Double.toString(product.getWeight() != null ? product.getWeight() : 0));
			setCellText(row, col++, product.getQuantityUnit());
			setCellText(row, col++, DataUtils.getInstance().getFormattedLocalizedDate(product.getDateAdded()));
//			setCellText(row, col++, product.getFormatedStringValueByKey("picturename"));
			setCellText(row, col++, Double.toString(product.getQuantity() != null ? product.getQuantity() : 0));
			setCellText(row, col++, Long.toString(product.getWebshopId() != null ? product.getWebshopId() : 0));
			
			// Alternate the background color
			if ((row % 2) == 0)
				setBackgroundColor( 0, row, col-1, row, "#e8ebed");

			row++;
		}
		
		save();

		// True = Export was successful
		return true;
	}

	@Override
	protected String getOutputFileName() {
		return exportMessages.wizardExportProductandbuyersDefaultfilename;
	}
}
