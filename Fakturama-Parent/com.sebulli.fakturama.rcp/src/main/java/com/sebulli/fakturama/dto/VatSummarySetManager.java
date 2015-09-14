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

package com.sebulli.fakturama.dto;


import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;

import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 * Stores one VatSummarySet object and provides some methods e.g. to add an
 * UniDataSet document
 * 
 * @author Gerd Bartelt
 */
public class VatSummarySetManager {
	private VatSummarySet vatSummarySet;

	/**
	 * Constructor Creates a new VatSummarySet
	 */
	public VatSummarySetManager() {
		vatSummarySet = new VatSummarySet();
	}

	/**
	 * Add an UniDataSet document to the VatSummarySet
	 * 
	 * @param document
	 *            Document to add
	 */
	public void add(Document document, Double scaleFactor) {
		int parentSign =DocumentTypeUtil.findByBillingType(document.getBillingType()).getSign();
		CurrencyUnit currencyCode = DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale());
		MonetaryAmount deposit = Money.of(document.getPaidValue(), currencyCode);
		// Create a new summary object and start the calculation.
		// This will add all the entries to the VatSummarySet
		new DocumentSummaryCalculator().calculate(vatSummarySet, document.getItems(), document.getShippingValue() * parentSign,
				document.getShipping().getShippingVat(),
				document.getShippingAutoVat(), document.getItemsRebate(), document.getNoVatReference(),
 			        scaleFactor, document.getNetGross(), deposit);
	}
	
	
	
	/**
	 * Getter for the VatSummarySet
	 * 
	 * @return The VatSummarySet
	 */
	public VatSummarySet getVatSummaryItems() {
		return vatSummarySet;
	}

	/**
	 * Get the size of the
	 * 
	 * @return The size of the VatSummarySet
	 */
	public int size() {
		return vatSummarySet.size();
	}

	/**
	 * Get the index of a VatSummaryItem
	 * 
	 * @param vatSummaryItem
	 *            Item to search for
	 * @return Index of the item or -1, of none was found
	 */
	public int getIndex(VatSummaryItem vatSummaryItem) {
		return vatSummarySet.getIndex(vatSummaryItem);
	}
}
