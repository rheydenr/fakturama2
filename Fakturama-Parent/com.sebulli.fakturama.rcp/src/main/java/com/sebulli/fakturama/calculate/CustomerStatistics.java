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

package com.sebulli.fakturama.calculate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * This class can generate a customer statistic
 *  
 * @author Gerd Bartelt
 */
public class CustomerStatistics {
    
    @Inject
    private DocumentsDAO documentsDAO;
    
    @Inject
    @Preference
    protected IEclipsePreferences preferences;

	/**
     * @param contact the contact to set
     */
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    // The customer has already ordered something
	private boolean isRegularCustomer = false;
	
	// How many orders
	private Integer ordersCount = 0;
	
	// The last date
	private Calendar lastOrderDate = null;

	// Some of the invoices
	private String invoices = "";
	
	// The total volume
	private Double total = 0.0;
	
	// Customer to test
	private Contact contact = null;
	private String address = "";
	
	public CustomerStatistics() {}
	
	/**
	 * Constructor
	 * 		Generates a statistic
	 * @param 
	 * 		contactID of the customer
	 */
	public CustomerStatistics (Contact contact) {
		
		// Exit, if no customer is set
		if (contact == null)
			return;
		
		this.contact = contact;
		makeStatistics(true);

	}
	
	/**
	 * Constructor
	 * 		Generates a statistic
	 * @param 
	 * 		contactID of the customer
	 * @param 
	 * 		firstAddressLine of the customer
	 */
	public CustomerStatistics (Contact contact, String address) {
		
		this.contact = contact;
		this.address = address;
		makeStatistics(false);

	}
	

	/**
	 * Make the Statistics. Search for other documents from this customer
	 * 
	 * @param 
	 * 		byID TRUE:  Compare contact ID
	 * 		     FLASE: Compare also first line of address
	 */
	public void makeStatistics(boolean byID) {
		// Get all undeleted documents
		// Only paid invoiced from this customer will be used for the statistics
		List<Invoice> documents = documentsDAO.findPaidInvoices();

		// Export the document data
		for (Invoice document : documents) {

			boolean customerFound = false;

			// Compare the customer ID
			if (contact != null && document.getContact().isSameAs(contact)) {
				customerFound = true;
			}
			
			// Compare the the address
			if (!byID && (address.length() > 10) && 
				DataUtils.getInstance().similarity(ContactUtil.getInstance(preferences).getAddressAsString(document.getContact()), address) > 0.7) {
				customerFound = true;
			}
			
			if (customerFound) {
				// It's a regular customer
				isRegularCustomer = true;

				// Add the invoice number to the list of invoices
				// Add maximum 4 invoices
				if (ordersCount < 4) {
					if (!invoices.isEmpty())
						invoices += ", ";
					invoices += document.getName();
				}
				else if (ordersCount == 4) {
					invoices += ", ...";
				}
				
				
				// Increment the count of orders
				ordersCount ++;
				
				// Increase the total
				total += document.getPaidValue();
				
				// Get the date of the document and convert it to a
				// GregorianCalendar object.
				GregorianCalendar documentDate = new GregorianCalendar();
//					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                Date expenditureDateString = null;

                // Use date 
                expenditureDateString = document.getOrderDate();

                // Do only parse non empty strings
                if (expenditureDateString != null) {
                	documentDate.setTime(expenditureDateString);

                	// Set the last order date
                	if (lastOrderDate == null) {
                		lastOrderDate = documentDate;
                	} else {
                		documentDate.after(lastOrderDate);
                		lastOrderDate = documentDate;
                	}
                }
			}
		}
	}
	
	/**
	 * Returns whether the customer has already ordered something
	 * 
	 * @return
	 * 		True, if there are some paid invoices
	 */
	public boolean isRegularCustomer() {
		return isRegularCustomer;
	}
	
	/**
	 * Returns how often the customer has paid an invoice
	 * 
	 * @return
	 * 		The number of the paid invoices
	 */
	public Integer getOrdersCount () {
		return ordersCount;
	}
	
	/**
	 * Returns the total value
	 * 
	 * @return
	 * 		The total value
	 */
	public Double getTotal () {
		return total;
	}
	
	/**
	 * Returns the last date
	 * 
	 * @return
	 * 		The date of the last order
	 */
	public String getLastOrderDate() {
		if (lastOrderDate != null) {
		    LocalDate date = LocalDate.from(lastOrderDate.toInstant());
		    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
			// DataUtils.getInstance().getDateTimeAsLocalString((GregorianCalendar) lastOrderDate);
		} else {
			return "-";
		}
	}
	
	/**
	 * Returns the string with some of the invoices
	 * 
	 * @return
	 * 	String with invoice numbers
	 */
	public String getInvoices () {
		return invoices;
	}
}
