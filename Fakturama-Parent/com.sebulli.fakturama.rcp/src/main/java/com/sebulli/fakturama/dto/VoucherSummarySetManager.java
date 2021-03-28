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

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.CurrencyUnit;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.calculate.VoucherSummaryCalculator;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Voucher;


/**
 * Stores one VatSummarySet object and provides some methods e.g. to add an
 * UniDataSet document
 * 
 * @author Gerd Bartelt
 */
public class VoucherSummarySetManager {
	
	@Inject
	private IEclipseContext ctx;

	private VatSummarySet voucherSummarySet;

	/**
	 * Creates a new voucherSummarySet
	 */
	@PostConstruct
	public void init() {
		voucherSummarySet = ContextInjectionFactory.make(VatSummarySet.class, ctx);
	}

	/**
	 * Add a voucher to the voucherSummarySet
	 * 
	 * @param document
	 *            Document to add
	 * @param useCategory
	 *            If true, the category is also used for the vat summary as a
	 *            description
	 */
	public void add(Voucher voucher, boolean useCategory) {
		add(voucher, useCategory, -1);
	}

	/**
	 * Add a voucher to the voucherSummarySet
	 * 
	 * @param document
	 *            Document to add
	 * @param useCategory
	 *            If true, the category is also used for the vat summary as a
	 *            description
	 * @itemNr index of one item
	 */
	public void add(Voucher voucher, boolean useCategory, int itemNr) {
		// Create a new summary object and start the calculation.
		// This will add all the entries to the VatSummarySet
		VoucherSummaryCalculator summary = ContextInjectionFactory.make(VoucherSummaryCalculator.class, ctx);
		CurrencyUnit currencyCode = DataUtils.getInstance().getDefaultCurrencyUnit();
		summary.calculate(voucherSummarySet,
				itemNr > -1 ? voucher.getItems().stream().filter(item -> item.getPosNr().compareTo(itemNr) == 0)
						.collect(Collectors.toList()) : voucher.getItems(),
				useCategory,
				voucher.getPaidValue() != null ? Money.of(voucher.getPaidValue(), currencyCode)
						: Money.zero(currencyCode),
				voucher.getTotalValue() != null ? Money.of(voucher.getTotalValue(), currencyCode)
						: Money.zero(currencyCode),
				BooleanUtils.toBoolean(voucher.getDiscounted()));
	}

	/**
	 * Getter for the voucherSummarySet
	 * 
	 * @return The voucherSummarySet
	 */
	public VatSummarySet getVoucherSummaryItems() {
		return voucherSummarySet;
	}

	/**
	 * Get the size of the
	 * 
	 * @return The size of the voucherSummarySet
	 */
	public int size() {
		return voucherSummarySet.size();
	}

	/**
	 * Get the index of a voucherSummaryItem
	 * 
	 * @param voucherSummaryItem
	 *            Item to search for
	 * @return Index of the item or -1, of none was found
	 */
	public int getIndex(VatSummaryItem voucherSummaryItem) {
		return voucherSummarySet.getIndex(voucherSummaryItem);
	}
}
