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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.VoucherCategory;

public class AccountSummaryCalculator {
	
	@Inject
	private PaymentsDAO paymentsDAO;
	
    @Inject
    private VoucherCategoriesDAO accountDAO;

	HashMap<String,Integer> paymentIds = new HashMap<String,Integer>();  
	
	// Set with all accounts
	private SortedSet<VoucherCategory> accounts; 
	
	// Array with all entries of one account
	private List<VoucherCategory> accountEntries;
	
	/**
	 * Constructor
	 */
	public AccountSummaryCalculator() {
		accounts = new TreeSet<VoucherCategory>(new Comparator<VoucherCategory>() {
            @Override
            public int compare(VoucherCategory cat1, VoucherCategory cat2) {
                return cat1.getName().compareTo(cat2.getName());
            }
        });
		accountEntries = new ArrayList<>() ;

	}
	
	/**
	 * Return a set of all accounts
	 * Collect them from the payments and the vouchers
	 */
	public void collectAccounts() {
        // Collect all category strings as a sorted Set
		accounts.addAll(accountDAO.findAll());
	}
	
	/**
	 * Getter for the collected accounts
	 * 
	 * @return
	 * 		Tree set with all accounts
	 */
	public SortedSet<VoucherCategory> getAccounts() {
		return accounts;
	}
	
	/**
	 * Collects account entries
	 * 
	 * @param account
	 * @param vouchers
	 * @param sign
	 * 	The sign (+1 for receipts or for expenditures -1)
	 */
//	private void collectVouchers (String account, DataSetArray<?> vouchers , double sign) {
//
//		ArrayList<?> entries = vouchers.getActiveDatasets();
//		
//		for (Object entry : entries) {
//			DataSetVoucher voucher = (DataSetVoucher)entry;
//			// Add only vouchers with this account
//			if (voucher.getStringValueByKey("category").equalsIgnoreCase(account)) {
//				DataSetAccountEntry accountEntry = new DataSetAccountEntry(voucher, sign);
//				accountEntries.add(accountEntry);
//			}
//			
//		}
//	}
//
//	private void collectDocuments (String account, DataSetArray<DataSetDocument> documents,
//			 DataSetArray<DataSetPayment> payments) {
//
//		paymentIds = new HashMap<String,Integer>();
//		
//		for (DataSetPayment payment : payments.getActiveDatasets()) {
//			paymentIds.put(payment.getStringValueByKey("description"), payment.getIntValueByKey("id"));
//		}
//		
//
//		
//		ArrayList<DataSetDocument> entries = documents.getActiveDatasets();
//		
//		for (DataSetDocument document : entries) {
//			
//			// Only invoices and credits in the interval
//			// will be exported.
//			boolean isInvoiceOrCredit = ((document.getIntValueByKey("category") == DocumentType.INVOICE.getInt()) || (document.getIntValueByKey("category") == DocumentType.CREDIT
//					.getInt())) ;
//
//			if (isInvoiceOrCredit) {
//				int paymentid = document.getIntValueByKey("paymentid");
//				String paymentdescription = document.getStringValueByKey("paymentdescription");
//
//				if (paymentid < 0) {
//					if (paymentIds.containsKey(paymentdescription))
//						paymentid = paymentIds.get(paymentdescription);
//				}
//				
//				if (paymentid >= 0) {
//					String paymentAccount = payments.getDatasetById(paymentid).getStringValueByKey("category");
//					if (paymentAccount.equalsIgnoreCase(account)) {
//						DataSetAccountEntry accountEntry = new DataSetAccountEntry(document);
//						accountEntries.add(accountEntry);
//					}
//				}
//			}
//		}
//	}
//	
//	/**
//	 * Getter for account entries
//	 * 
//	 * @return
//	 */
//	public ArrayList<DataSetAccountEntry> getAccountEntries() {
//		return accountEntries;
//	}
	
//	/**
//	 * Collects all entries from all vouchers
//	 * 
//	 * @param account
//	 * 		The account name
//	 */
//	public void collectEntries(String account) {
//		
//		accountEntries = new ArrayList<VoucherCategory>() ;
//
//		collectDocuments( account, Data.INSTANCE.getDocuments(), Data.INSTANCE.getPayments());
//		collectVouchers( account, Data.INSTANCE.getReceiptVouchers(), 1.0);
//		collectVouchers( account, Data.INSTANCE.getExpenditureVouchers(), -1.0);
//	}
	
	
}
