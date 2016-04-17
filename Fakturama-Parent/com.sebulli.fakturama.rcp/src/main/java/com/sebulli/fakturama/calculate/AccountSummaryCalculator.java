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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dao.ReceiptVouchersDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.dto.AccountEntry;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.VoucherCategory;

public class AccountSummaryCalculator {

	@Inject
	private IEclipseContext ctx;
	
    @Inject
    private VoucherCategoriesDAO accountDAO;  
    
    @Inject
    private ExpendituresDAO expendituresDAO;
    
    @Inject
    private ReceiptVouchersDAO receiptVouchersDAO;
    
    @Inject
    private DocumentsDAO documentsDAO;

	HashMap<String,Integer> paymentIds = new HashMap<>();  
	
	// Set with all accounts
	private SortedSet<VoucherCategory> accounts; 
	
	// Array with all entries of one account
	private List<AccountEntry> accountEntries;
	
	/**
	 * Constructor
	 */
	public AccountSummaryCalculator() {
		accounts = new TreeSet<>((cat1, cat2) -> cat1.getName().compareTo(cat2.getName()));
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
	 * 	The sign (+1 for receipts, -1 for expenditures)
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
	
	/**
	 * Getter for account entries
	 * 
	 * @return
	 */
	public List<AccountEntry> getAccountEntries() {
		return accountEntries;
	}
	
	/**
	 * Collects all entries from all vouchers
	 * 
	 * @param account
	 * 		The account name
	 */
	public void collectEntries(String account) {
		
		accountEntries = new ArrayList<AccountEntry>();
		VoucherCategory category = accountDAO.findVoucherCategoryByName(account);

		// collectDocuments( account, Data.INSTANCE.getDocuments(), Data.INSTANCE.getPayments());
		Date endDate = null;
		if(ctx.get(Constants.PARAM_END_DATE) != null) {
			endDate = ((GregorianCalendar)ctx.get(Constants.PARAM_END_DATE)).getTime();
		}
		
		Date startDate = null;
		if(ctx.get(Constants.PARAM_START_DATE) != null) {
			startDate = ((GregorianCalendar)ctx.get(Constants.PARAM_START_DATE)).getTime();
		}
		
		accountEntries.addAll(documentsDAO.findAccountedDocuments(category, 
				startDate, endDate)); 
		accountEntries.addAll(receiptVouchersDAO.findAccountedReceiptVouchers(category,
				startDate, endDate)); // collectVouchers( account, Data.INSTANCE.getReceiptVouchers(), 1.0);
		accountEntries.addAll(expendituresDAO.findAccountedExpenditures(category,
				startDate, endDate)); // collectVouchers( account, Data.INSTANCE.getExpenditureVouchers(), -1.0);
	}
}
