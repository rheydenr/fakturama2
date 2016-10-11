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

/**
 * Calculates the tax, gross and sum of one document. This is the central
 * calculation used by the document editors and the export functions.
 * 
 * @author Gerd Bartelt
 */
public class VoucherSummary {

	// total sum
	private MonetaryAmount totalNet;
	private MonetaryAmount totalVat;
	private MonetaryAmount totalGross;
    
    private CurrencyUnit currencyCode;

	/**
	 * Default constructor. Resets all value to 0.
	 * @param currencyUnit 
	 */
	public VoucherSummary(CurrencyUnit currencyUnit) {
        this.currencyCode = currencyUnit;
		resetValues();
	}

	/**
	 * Reset all values to 0
	 */
	private void resetValues() {
		totalNet = Money.of(Double.valueOf(0.0), currencyCode);
		totalVat = Money.of(Double.valueOf(0.0), currencyCode);
		totalGross = Money.of(Double.valueOf(0.0), currencyCode);
	}

	/**
	 * Getter for total document sum (net)
	 * 
	 * @return Sum as PriceValue
	 */
	public MonetaryAmount getTotalNet() {
		return this.totalNet;
	}

	/**
	 * Getter for total document sum (vat)
	 * 
	 * @return Sum as PriceValue
	 */
	public MonetaryAmount getTotalVat() {
		return this.totalVat;
	}

	/**
	 * Getter for total document sum (gross)
	 * 
	 * @return Sum as PriceValue
	 */
	public MonetaryAmount getTotalGross() {
		return this.totalGross;
	}

    /**
     * @param totalNet the totalNet to set
     */
    public final void setTotalNet(MonetaryAmount totalNet) {
        this.totalNet = totalNet;
    }

    /**
     * @param totalVat the totalVat to set
     */
    public final void setTotalVat(MonetaryAmount totalVat) {
        this.totalVat = totalVat;
    }

    /**
     * @param totalGross the totalGross to set
     */
    public final void setTotalGross(MonetaryAmount totalGross) {
        this.totalGross = totalGross;
    }

}
