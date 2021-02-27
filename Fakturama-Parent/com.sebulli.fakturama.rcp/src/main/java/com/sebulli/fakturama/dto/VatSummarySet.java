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
import java.util.List;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.function.MonetaryFunctions;
import org.javamoney.moneta.spi.MoneyUtils;

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
	    MonetaryAmount retval = Money.of(Double.valueOf(0.0), currencyCode);
	    
	    Iterator<VatSummaryItem> iterator = this.iterator();
		while( iterator.hasNext()) {
			VatSummaryItem item = iterator.next();
			retval = MonetaryFunctions.sum(retval, item.getNet());
		}
	    
		// Round the net value
		return retval.with(rounding);
//		return this.stream().reduce(zero, 
//				(s, v) -> MonetaryFunctions.sum(zero, v.getNet()), MonetaryFunctions.sum());
	}

	/**
	 * Round all items of the VatSummarySet
	 */
	public void roundAllEntries() {
	    MonetaryAmount zero = Money.of(Double.valueOf(0.0), currencyCode);
		MonetaryAmount netSum = Money.from(zero);
		MonetaryAmount vatSum = Money.from(zero);
		MonetaryAmount netSumOfRounded = Money.from(zero);
		MonetaryAmount vatSumOfRounded = Money.from(zero);
		MonetaryAmount netRoundedSum = Money.from(zero);
		MonetaryAmount vatRoundedSum = Money.from(zero);
		int missingCents = 0;
		Double oneCent;
		Double roundingError;
		boolean searchForMaximum;

//		// First, add all values to get the sum of net and vat
//		for (Iterator<VatSummaryItem> iterator = this.iterator(); iterator.hasNext();) {
//			VatSummaryItem item = iterator.next();
//
//			//Add all values
//			netSum = netSum.add(item.getNet());
//			vatSum = vatSum.add(item.getVat());
//		}
//
//		// Round the sum
//		netRoundedSum = netSum.with(rounding);
//		vatRoundedSum = vatSum.with(rounding);

		// round all items
//		for (Iterator<VatSummaryItem> iterator = this.iterator(); iterator.hasNext();) {
//			VatSummaryItem item = iterator.next();
//
//			item.round();
//
//			// calculate the sum of rounded values
//			netSumOfRounded = netSumOfRounded.add(item.getNet());
//			vatSumOfRounded = vatSumOfRounded.add(item.getVat());
//		}

//		fixRoundingError(netSumOfRounded, vatSumOfRounded, netRoundedSum, vatRoundedSum);
	}

//	@Deprecated
//	private void fixRoundingError(MonetaryAmount netSumOfRounded, MonetaryAmount vatSumOfRounded,
//			MonetaryAmount netRoundedSum, MonetaryAmount vatRoundedSum) {
//		int missingCents;
//		Double oneCent;
//		Double roundingError;
//		boolean searchForMaximum;
//		// Calculate the rounding error in cent
//		roundingError = (netRoundedSum.subtract(netSumOfRounded)).getNumber().doubleValue() * 100.000001;
//		missingCents = roundingError.intValue();
//
//		// Decrease or increase the entries
//		if (missingCents >= 0) {
//			searchForMaximum = true;
//			oneCent = 0.01;
//		}
//		else {
//			searchForMaximum = false;
//			missingCents = -missingCents;
//			oneCent = -0.01;
//		}
//
//		// Dispense the missing cents to those values with the maximum
//		// rounding error.
//		for (int i = 0; i < missingCents; i++) {
//
//			Double maxRoundingError = -oneCent;
//			VatSummaryItem maxItem = null;
//
//			// Search for the item with the maximum error
//			for (Iterator<VatSummaryItem> iterator = this.iterator(); iterator.hasNext();) {
//				VatSummaryItem item = iterator.next();
//
//				// Search for maximum or minimum
//				if (searchForMaximum) {
//					if (item.getNetRoundingError() > maxRoundingError) {
//						maxRoundingError = item.getNetRoundingError();
//						maxItem = item;
//					}
//				}
//				else {
//					// If found, mark it
//					if (item.getNetRoundingError() < maxRoundingError) {
//						maxRoundingError = item.getNetRoundingError();
//						maxItem = item;
//					}
//				}
//
//			}
//
//			// Correct the item be one cent
//			if (maxItem != null) {
//			    MonetaryAmount tmpVal = Money.of(maxItem.getNet().getNumber().doubleValue() + oneCent, currencyCode);
//				maxItem.setNet(tmpVal);
//				maxItem.setNetRoundingError(maxItem.getNetRoundingError() - oneCent);
//			}
//		}
//
//		// Do the same with the vat entry
//
//		// Calculate the rounding error in cent
//		roundingError = vatRoundedSum.subtract(vatSumOfRounded).getNumber().doubleValue() * 100.000001;
//		missingCents = roundingError.intValue();
//
//		// Decrease or increase the entries
//        if (missingCents >= 0) {
//            searchForMaximum = true;
//            oneCent = 0.01;
//        } else {
//            searchForMaximum = false;
//            missingCents = -missingCents;
//            oneCent = -0.01;
//        }
//        
//		// dispense the missing cents to those values with the maximum
//		// rounding error.
//		for (int i = 0; i < missingCents; i++) {
//
//			Double maxRoundingError = -oneCent;
//			VatSummaryItem maxItem = null;
//
//			// Search for the item with the maximum error
//			for (Iterator<VatSummaryItem> iterator = this.iterator(); iterator.hasNext();) {
//				VatSummaryItem item = iterator.next();
//
//				// Search for maximum or minimum
//				if (searchForMaximum) {
//					// If found, mark it
//					if (item.getVatRoundingError() > maxRoundingError) {
//						maxRoundingError = item.getVatRoundingError();
//						maxItem = item;
//					}
//				}
//				else {
//					// If found, mark it
//					if (item.getVatRoundingError() < maxRoundingError) {
//						maxRoundingError = item.getVatRoundingError();
//						maxItem = item;
//					}
//				}
//			}
//
//			// Correct the item by one cent
//			if (maxItem != null) {
//				maxItem.setVat(Money.of(maxItem.getVat().getNumber().doubleValue() + oneCent, maxItem.getVat().getCurrency()));
//				maxItem.setVatRoundingError(maxItem.getVatRoundingError() - oneCent);
//			}
//		}
//	}

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
