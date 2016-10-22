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

import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.misc.DataUtils;

/**
 * Calculates the tax, gross and sum of one document. This is the central
 * calculation used by the document editors and the export functions.
 * 
 * @author Gerd Bartelt
 */
public class DocumentSummary {
    
    /* TODO
auch mal ansehen (für Summenbildung): 

/functional-example/src/main/java/org/javamoney/examples/console/functional/MonetaryGroupOperations.java

hier klingt vor allem das interessant:

             MonetarySummaryStatistics summary = getCurrencies().stream()
                           .filter(MonetaryFunctions.isCurrency(DOLLAR))
                           .collect(MonetaryFunctions.summarizingMonetary(DOLLAR));

     */

	/** The prices are not rounded to net or gross */
	public static final int ROUND_NOTSPECIFIED = 0;
	/** The prices are rounded, that the net values are full cent values. */
	public static final int ROUND_NET_VALUES = 1;
	/** The prices are rounded, that the gross values are full cent values. */
	public static final int ROUND_GROSS_VALUES = 2;
	
	// sum off items
	private MonetaryAmount itemsNet;
	private MonetaryAmount itemsGross;

	// total sum
	private MonetaryAmount totalNet;
	private MonetaryAmount totalVat;
	private MonetaryAmount totalGross;
	private double   	   totalQuantity;

	// discount values
	private MonetaryAmount discountNet;
	private MonetaryAmount discountGross;

	// shipping value
	private MonetaryAmount shippingNet;
	private MonetaryAmount shippingVat;
	private MonetaryAmount shippingGross;

	// deposit value
	private MonetaryAmount deposit;
	private MonetaryAmount finalPayment;
	
	private CurrencyUnit currencyCode;
	
	/**
	 * Default constructor. Resets all value to 0.
	 */
	public DocumentSummary() {
	    currencyCode = DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale());
		resetValues();
	}

	/**
	 * Reset all values to 0
	 */
	private void resetValues() {
		itemsNet = Money.of(Double.valueOf(0.0), currencyCode);
		itemsGross = Money.of(Double.valueOf(0.0), currencyCode);
		totalNet = Money.of(Double.valueOf(0.0), currencyCode);
		totalVat = Money.of(Double.valueOf(0.0), currencyCode);
		totalGross = Money.of(Double.valueOf(0.0), currencyCode);
		discountNet = Money.of(Double.valueOf(0.0), currencyCode);
		discountGross = Money.of(Double.valueOf(0.0), currencyCode);
		shippingNet = Money.of(Double.valueOf(0.0), currencyCode);
		shippingVat = Money.of(Double.valueOf(0.0), currencyCode);
		shippingGross = Money.of(Double.valueOf(0.0), currencyCode);
		deposit = Money.of(Double.valueOf(0.0), currencyCode);
		finalPayment = Money.of(Double.valueOf(0.0), currencyCode);
	}

	/**
	 * Getter for shipping value (net)
	 * 
	 * @return shipping net as MonetaryAmount
	 */
	public MonetaryAmount getShippingNet() {
		return this.shippingNet;
	}

	/**
	 * Getter for shipping Vat value (Vat)
	 * 
	 * @return shipping Vat as MonetaryAmount
	 */
	public MonetaryAmount getShippingVat() {
		return this.shippingVat;
	}

	/**
	 * Getter for shipping value (gross)
	 * 
	 * @return shipping gross as MonetaryAmount
	 */
	public MonetaryAmount getShippingGross() {
		return this.shippingGross;
	}

	/**
	 * Getter for sum of items (net)
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getItemsNet() {
		return this.itemsNet;
	}

	/**
	 * Getter for sum of items (gross)
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getItemsGross() {
		return this.itemsGross;
	}

	/**
	 * Getter for total document sum (net)
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getTotalNet() {
		return this.totalNet;
	}

	/**
	 * Getter for total document sum (vat)
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getTotalVat() {
		return this.totalVat;
	}

	/**
	 * Getter for total document sum (gross)
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getTotalGross() {
		return this.totalGross;
	}

	/**
	 * Getter for discount (net)
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getDiscountNet() {
		return this.discountNet;
	}

	/**
	 * Getter for discount (gross)
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getDiscountGross() {
		return this.discountGross;
	}

	/**
	 * Getter for the deposit
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getDeposit() {
		return this.deposit;
	}
	
	/**
	 * Getter for the final payment
	 * 
	 * @return Sum as MonetaryAmount
	 */
	public MonetaryAmount getFinalPayment() {
		return this.finalPayment;
	}

	/**
	 * @param itemsNet the itemsNet to set
	 */
	public void setItemsNet(MonetaryAmount itemsNet) {
		this.itemsNet = itemsNet;
	}

	/**
	 * @param itemsGross the itemsGross to set
	 */
	public void setItemsGross(MonetaryAmount itemsGross) {
		this.itemsGross = itemsGross;
	}

	/**
	 * @param totalNet the totalNet to set
	 */
	public void setTotalNet(MonetaryAmount totalNet) {
		this.totalNet = totalNet;
	}

	/**
	 * @param totalVat the totalVat to set
	 */
	public void setTotalVat(MonetaryAmount totalVat) {
		this.totalVat = totalVat;
	}

	/**
	 * @param totalGross the totalGross to set
	 */
	public void setTotalGross(MonetaryAmount totalGross) {
		this.totalGross = totalGross;
	}

	/**
	 * @param discountNet the discountNet to set
	 */
	public void setDiscountNet(MonetaryAmount discountNet) {
		this.discountNet = discountNet;
	}

	/**
	 * @param discountGross the discountGross to set
	 */
	public void setDiscountGross(MonetaryAmount discountGross) {
		this.discountGross = discountGross;
	}

	/**
	 * @param shippingNet the shippingNet to set
	 */
	public void setShippingNet(MonetaryAmount shippingNet) {
		this.shippingNet = shippingNet;
	}

	/**
	 * @param shippingVat the shippingVat to set
	 */
	public void setShippingVat(MonetaryAmount shippingVat) {
		this.shippingVat = shippingVat;
	}

	/**
	 * @param shippingGross the shippingGross to set
	 */
	public void setShippingGross(MonetaryAmount shippingGross) {
		this.shippingGross = shippingGross;
	}

	/**
	 * @param deposit the deposit to set
	 */
	public void setDeposit(MonetaryAmount deposit) {
		this.deposit = deposit;
	}

	/**
	 * @param finalPayment the finalPayment to set
	 */
	public void setFinalPayment(MonetaryAmount finalPayment) {
		this.finalPayment = finalPayment;
	}

	/**
	 * @return the totalQuantity
	 */
	public double getTotalQuantity() {
		return totalQuantity;
	}

	/**
	 * @param totalQuantity the totalQuantity to set
	 */
	public void setTotalQuantity(double totalQuantity) {
		this.totalQuantity = totalQuantity;
	} 
}
