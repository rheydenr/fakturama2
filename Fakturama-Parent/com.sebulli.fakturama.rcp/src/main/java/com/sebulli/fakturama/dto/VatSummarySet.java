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
import java.util.Iterator;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.function.MonetaryFunctions;

import com.sebulli.fakturama.misc.DataUtils;

/**
 * This Class can contain multiple VatSummaryItems.
 * 
 * If an item is added, and an other VatSummaryItem with the same name and the
 * same vat value in percent is existing, the absolute net and vat values of
 * this item are added to the existing.
 * 
 * If there is no entry with the same name and vat percent value, a new one is
 * created.
 * 
 * @author Gerd Bartelt
 */
public class VatSummarySet extends TreeSet<VatSummaryItem> {
    
    @Inject
    private IEclipseContext ctx;

	private static final long serialVersionUID = 1L;
    
    private CurrencyUnit currencyCode;
    private MonetaryRounding rounding;

    @PostConstruct
    public void init() {
        DataUtils dataUtils = ContextInjectionFactory.make(DataUtils.class, ctx);
        currencyCode = dataUtils.getDefaultCurrencyUnit();
        rounding = dataUtils.getRounding(currencyCode);  
    }
    
	/**
	 * Add a new VatSummaryItem to this tree
	 * 
	 * @param vatSummaryItem
	 *            The new Item
	 * @return <code>true</code> if it was added as new item
	 */
	@Override
	public boolean add(VatSummaryItem vatSummaryItemTemplate) {

		VatSummaryItem vatSummaryItem = VatSummaryItem.of(vatSummaryItemTemplate);

		// try to add it
		boolean added = super.add(vatSummaryItem);

		// If there was already an item with the same value and name ..
		if (!added) {

			// add the net and vat to the existing one
			VatSummaryItem existing = super.ceiling(vatSummaryItem);
			existing.add(vatSummaryItem);
		}

		return added;
	}

	/**
	 * Returns the index of a VatSummaryItem
	 * 
	 * @param vatSummaryItem
	 *            to Search for
	 * @return index or -1, if it was not found.
	 */
	public int getIndex(VatSummaryItem vatSummaryItem) {
		int i = -1;

		// Search all items
		for (Iterator<VatSummaryItem> iterator = this.iterator(); iterator.hasNext();) {
			i++;
			VatSummaryItem item = iterator.next();

			// Returns the item, if it is the same
			if (item.compareTo(vatSummaryItem) == 0)
				break;
		}
		return i;
	}
	
	public MonetaryAmount getTotalNet() {
		return this.parallelStream().map(v -> v.getNet()).reduce(Money.zero(currencyCode),
				MonetaryFunctions::sum).with(rounding);
	}

	/**
	 * Add all items of an other VatSummarySet
	 * 
	 * @param otherVatSummarySet
	 *            The other VatSummarySet
	 */
	public void addVatSummarySet(VatSummarySet otherVatSummarySet) {
		otherVatSummarySet.forEach(vat -> this.add(vat));
	}

}
