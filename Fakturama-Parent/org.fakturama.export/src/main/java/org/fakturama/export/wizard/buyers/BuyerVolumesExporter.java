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

package org.fakturama.export.wizard.buyers;

import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.BuyersAndTotal;
import org.fakturama.export.wizard.ExportWizardPageStartEndDate;
import org.fakturama.export.wizard.OOCalcExporter;
import org.fakturama.export.wizard.TotalSoldAndQuantity;
import org.odftoolkit.odfdom.type.Color;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;


/**
 * This class generates a list with all items and
 * all the buyers 
 * 
 * @author Gerd Bartelt
 */
public class BuyerVolumesExporter extends OOCalcExporter {

	@Inject
	@Translation
	private Messages msg;
	
	@Inject
	@Translation
	private ExportMessages exportMessages;
	
	@Inject
	private DocumentsDAO documentsDao;
    
	@Inject
	@Preference(nodePath = "/instance/com.sebulli.fakturama.rcp")
	private IEclipsePreferences eclipsePrefs;

	// List with all buyers
	private BuyersAndTotal buyersAndTotal = new BuyersAndTotal();
	
	/**
	 * Comparator to sort the List of buyers by volume or by quantity
	 * 
	 * @author Gerd Bartelt
	 */
	private class BuyersTotalSoldComparer implements Comparator<Object> {
		
		// List with all buyers
		private Map<String, TotalSoldAndQuantity> data = null;

		/**
		 * Constructor
		 * 
		 * @param data 
		 * 			The data to sort
		 */
		public BuyersTotalSoldComparer (Map<String, TotalSoldAndQuantity> data){
			super();
			this.data = data;
		}

		/**
		 * Compare two objects by quantity or by volume
		 *
		 * @param o1
		 * 			The first object
		 * @param o2
		 * 			The second object
		 */
         public int compare(Object o1, Object o2) {
        	 int result;
        	 
           	 MonetaryAmount e1 = this.data.get(o1).getTotalSold();
           	 MonetaryAmount e2 = this.data.get(o2).getTotalSold();
           	 result = e2.compareTo(e1);
        	 
        	 // Two items must not be equal. If they were, they would be
        	 // replaces in the map
        	 if (result == 0)
        		 result = 1;

        	 return result;
         }
	}
	
	
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
		doNotUseTimePeriod = (boolean) ctx.get(ExportWizardPageStartEndDate.WIZARD_DATESELECT_DONTUSETIMEPERIOD);
		startDate = null;
		endDate = null;
		
		if(!doNotUseTimePeriod) {
			if(ctx.get(Constants.PARAM_START_DATE) != null) {
				startDate = (GregorianCalendar) ctx.get(Constants.PARAM_START_DATE);
			}
			
			if(ctx.get(Constants.PARAM_END_DATE) != null) {
				endDate = (GregorianCalendar) ctx.get(Constants.PARAM_END_DATE);
			}
		}
	}


	/**
	 * 	Do the export job.
	 * 
	 * @return
	 * 			True, if the export was successful
	 */
	public boolean export() {

		// Try to generate a spreadsheet
		if (!createSpreadSheet()) {
			return false;
		}
		
		usePaidDate = eclipsePrefs.getBoolean(Constants.PREFERENCES_EXPORTSALES_PAIDDATE, true);
		
		// Get all undeleted documents
		List<Document> documents = documentsDao.findPaidDocumentsInRange(usePaidDate, 
				(startDate != null ? startDate.getTime() : null), 
				(endDate != null ? endDate.getTime() : null));

		setCellTextInBold(0, 0, exportMessages.wizardExportBuyersTabletitle);

		// Fill the first 4 rows with the company information
		fillCompanyInformation(2);
		fillTimeIntervall(7);

		// Counter for the current row and columns in the Calc document
		int row = 11;
		int col = 0;

		//T: Used as heading of a table. Keep the word short.
		setCellTextInBold(row, col++, msg.exporterDataBuyers);
		//T: Used as heading of a table. Keep the word short.
		setCellTextInBold(row, col++, msg.exporterDataVolume);

		// Draw a horizontal line
		for (col = 0; col < 2; col++) {
			setBorder(row, col, Color.BLACK, false, false, true, false);
		}
		row++;
		
		// Export the document data
		// (the documents are already in correct order since we 
		// retrieved them from database with a filter)
		for (Document document : documents) {

			// Get all items by ID from the item string
			List<DocumentItem> itemsStringParts = document.getItems();

			// Get the name of the buyer
			itemsStringParts.forEach(item -> buyersAndTotal.add(document.getAddressFirstLine(), item));
		}
		
		SortedMap<String, TotalSoldAndQuantity> sortedBuyers = new TreeMap<String, TotalSoldAndQuantity>(new BuyersTotalSoldComparer(buyersAndTotal.getBuyers()));
		sortedBuyers.putAll(buyersAndTotal.getBuyers());

		// Get through the list of all buyers
		for (Iterator<?> iteratorBuyer = sortedBuyers.entrySet().iterator(); iteratorBuyer.hasNext(); ) {

			// Get the next buyer
			@SuppressWarnings("unchecked")
			Entry<String, TotalSoldAndQuantity> buyer = (Entry<String, TotalSoldAndQuantity>)iteratorBuyer.next();
			col = 0;
			
			// Place the buyer's name, the quantity and the volume into
			// the next columns
			setCellText(row, col++, buyer.getKey());
			setCellValueAsLocalCurrency(row, col++, buyer.getValue().getTotalSold());
			
			// Alternate the background color
			if ((row % 2) == 0) {
				setBackgroundColor( 0, row, col-1, row, "#e8ebed");
			}

			row++;
		}

		save();
		
		// True = Export was successful
		return true;
	}
	
	@Override
	protected String getOutputFileName() {
		return exportMessages.wizardExportBuyersDefaultfilename;
	}

}
