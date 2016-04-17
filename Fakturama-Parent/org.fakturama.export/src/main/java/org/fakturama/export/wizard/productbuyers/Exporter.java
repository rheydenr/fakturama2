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

package org.fakturama.export.wizard.productbuyers;

import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
public class Exporter extends OOCalcExporter {

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

	// export Options
	private boolean sortByQuantity = false;
	
	/**
	 * Comparator to sort the List of items by volume or by quantity
	 * 
	 * @author Gerd Bartelt
	 */
	private class TotalSoldComparer implements Comparator<Object> {
		
		// List with all items
		private Map<String, BuyersAndTotal> data = null;
		// Sort by volume or by quantity
		private boolean sortByQuantity;
		
		/**
		 * Constructor
		 * 
		 * @param data 
		 * 			The data to sort
		 * @param sortByQuantity 
		 * 			How to sort the data
		 */
		public TotalSoldComparer (Map<String, BuyersAndTotal> data, boolean sortByQuantity){
			super();
			this.data = data;
			this.sortByQuantity = sortByQuantity;
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
        	 if (sortByQuantity) {
        		 Double e1 = this.data.get(o1).getTotalQuantity();
        		 Double e2 = this.data.get(o2).getTotalQuantity();
                 result = e2.compareTo(e1);
        	 }
        	 else {
            	 MonetaryAmount e1 = this.data.get(o1).getTotalSold();
            	 MonetaryAmount e2 = this.data.get(o2).getTotalSold();
                 result = e2.compareTo(e1);
        	 }
        	 
        	 // Two items must not be equal. If they were, they would be
        	 // replaces in the map
        	 if (result == 0)
        		 result = 1;
        	 
        	 return result;
         }
	}
	
	/**
	 * Comparator to sort the List of buyers by volume or by quantity
	 * 
	 * @author Gerd Bartelt
	 */
	private class BuyersTotalSoldComparer implements Comparator<Object> {
		
		// List with all buyers
		private Map<String, TotalSoldAndQuantity>  data = null;
		// Sort by volume or by quantity
		private boolean sortByQuantity;

		/**
		 * Constructor
		 * 
		 * @param data 
		 * 			The data to sort
		 * @param sortByQuantity 
		 * 			How to sort the data
		 */
		public BuyersTotalSoldComparer (Map<String, TotalSoldAndQuantity> data, boolean sortByQuantity){
			super();
			this.data = data;
			this.sortByQuantity = sortByQuantity;
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
        	 if (sortByQuantity) {
        		 Double e1 = this.data.get(o1).getTotalQuantity();
        		 Double e2 = this.data.get(o2).getTotalQuantity();
        		 result =  e2.compareTo(e1);
        	 }
        	 else {
            	 MonetaryAmount e1 = this.data.get(o1).getTotalSold();
            	 MonetaryAmount e2 = this.data.get(o2).getTotalSold();
            	 result = e2.compareTo(e1);
        	 }
        	 
        	 // Two items must not be equal. If they were, they would be
        	 // replaces in the map
        	 if (result == 0)
        		 result = 1;

        	 return result;
         }
	}
	
	
	// (unsorted) List with all items
	private Map<String, BuyersAndTotal> itemMap = new HashMap<String, BuyersAndTotal>();

	
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
		this.sortByQuantity = (boolean) ctx.get(ExportOptionPage.WIZARD_SORT_BY_QUANTITY);
	}

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
		
		usePaidDate = eclipsePrefs.getBoolean(Constants.PREFERENCES_EXPORTSALES_PAIDDATE, true);

		// Get all undeleted documents
		// Use pay date or document date
		List<Document> documents = documentsDao.findPaidDocumentsInRange(usePaidDate,
				(startDate != null && !doNotUseTimePeriod ? startDate.getTime() : null),
				(endDate != null && !doNotUseTimePeriod ? endDate.getTime() : null));

		//T: Title of the exported table
		setCellTextInBold(0, 0, exportMessages.wizardExportProductandbuyersTableTitle);

		// Fill the first 4 rows with the company information
		fillCompanyInformation(2);
		fillTimeIntervall(7);

		// Counter for the current row and columns in the Calc document
		int row = 11;
		int col = 0;

		setCellTextInBold(row, col++, msg.exporterDataProduct);
		setCellTextInBold(row, col++, msg.commonFieldQuantity);
		setCellTextInBold(row, col++, msg.exporterDataVolume);
		setCellTextInBold(row, col++, msg.exporterDataBuyers);
		setCellTextInBold(row, col++, msg.commonFieldQuantity);
		setCellTextInBold(row, col++, msg.exporterDataVolume);

		// Draw a horizontal line
		for (col = 0; col < 6; col++) {
			setBorder(row, col, Color.BLACK, false, false, true, false);
		}
		row++;
		
		// Export the document data
		for (Document document : documents) {

			if (documentShouldBeExported(document)) {
				
				// Get the name of the buyer
				String buyerName = document.getAddressFirstLine();
				
				// Parse the item string ..
				for (DocumentItem item : document.getItems()) {
					if (itemMap.containsKey(item.getName())) {
						itemMap.get(item.getName()).add(buyerName, item);
					}
					else {
						itemMap.put(item.getName(), new BuyersAndTotal(buyerName, item));
					}
				}
			}
		}
		
		// Sort the list of all items by quantity or by volume
		SortedMap<String, BuyersAndTotal> sortedItemMap = new TreeMap<String, BuyersAndTotal>(new TotalSoldComparer(itemMap, sortByQuantity));
		sortedItemMap.putAll(itemMap);
		
		// Alternate the background color every new item 
		int altrow =0;
		
		// Get all items of the list
		for ( Iterator<Entry<String, BuyersAndTotal>> iterator = sortedItemMap.entrySet().iterator(); iterator.hasNext(); ) {
			
			// Get the item
			Entry<String, BuyersAndTotal> entry = iterator.next();
			
			// Place the item, the total quantity and volume in the first 3 columns
			col = 0;
			setCellText(row, col++, entry.getKey());
			setCellText(row, col++, Double.toString(entry.getValue().getTotalQuantity()));
			setCellValueAsLocalCurrency(row, col++, entry.getValue().getTotalSold());

			// Get the buyers and sort them
			Map<String, TotalSoldAndQuantity> buyers = entry.getValue().getBuyers();
			SortedMap<String, TotalSoldAndQuantity> sortedBuyers = new TreeMap<String, TotalSoldAndQuantity>(new BuyersTotalSoldComparer(buyers, sortByQuantity));
			sortedBuyers.putAll(buyers);

			// Get through the list of all buyers
			for (Iterator<?> iteratorBuyer = sortedBuyers.entrySet().iterator(); iteratorBuyer.hasNext(); ) {

				// Get the next buyer
				@SuppressWarnings("unchecked")
				Entry<String, TotalSoldAndQuantity> buyer = (Entry<String, TotalSoldAndQuantity>)iteratorBuyer.next();
				col = 3;
				
				// Place the buyer's name, the quantity and the volume into
				// the next columns
				setCellText(row, col++, buyer.getKey());
				setCellText(row, col++, Double.toString(buyer.getValue().getTotalQuantity()));
				setCellValueAsLocalCurrency(row, col++, buyer.getValue().getTotalSold());
				
				// Alternate the background color
				if ((altrow % 2) == 0)
					setBackgroundColor( 0, row, col-1, row, "#e8ebed");

				row++;
			}
			altrow ++;
		}
		
		save();
		
		// True = Export was successful
		return true;
	}

}
