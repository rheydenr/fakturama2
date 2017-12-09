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
import javax.money.MonetaryRounding;

import org.apache.commons.lang3.StringUtils;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.ItemAccountType;

/**
 * This Class represents one entry in the VatSummarySet. It contains a net and
 * VAT value and the VAT name.
 * 
 * @author Gerd Bartelt
 */
public class VatSummaryItem implements Comparable<VatSummaryItem> {

	// Absolute Net and Vat value
	// This can be the sum of more than one item
	private MonetaryAmount net;
	private MonetaryAmount vat;
	
	// sales equalization tax (could be zero)
	private MonetaryAmount salesEqTax;
	private Double salesEqTaxPercent;

	// Rounding errors
	private Double netRoundingError;
	private Double vatRoundingError;

	// Vat Name and Percent Value. These values identify the VatSummaryItem
	private String vatName;
	private String description;
	private Double vatPercent;
	
	private ItemAccountType accountType;

	/**
	 * Constructor Creates a VatSummaryItem from a net and vat value and the vat
	 * name.
	 * 
	 * @param vatName
	 *            Vat name
	 * @param vatPercent
	 *            Vat value in percent
	 * @param net
	 *            Absolute Net value
	 * @param vat
	 *            Absolute Vat value
	 */
	public VatSummaryItem(String vatName, Double vatPercent, MonetaryAmount net, MonetaryAmount vat) {
		this(vatName, vatPercent, net, vat, "");
	}

	/**
	 * Constructor Creates a VatSummaryItem from a net and vat value and the vat
	 * name with an additional description
	 * 
	 * @param vatName
	 *            Vat name
	 * @param vatPercent
	 *            Vat value in percent
	 * @param net
	 *            Absolute Net value
	 * @param vat
	 *            Absolute Vat value
	 * @param description
	 *            Additional description
	 */
	public VatSummaryItem(String vatName, Double vatPercent, MonetaryAmount net, MonetaryAmount vat, String description) {
		this.vatName = vatName;
		this.vatPercent = vatPercent;
		this.net = net;
		this.vat = vat;
		this.salesEqTax = Money.zero(net.getCurrency());
//		this.netRoundingError = 0.0;
//		this.vatRoundingError = 0.0;
		this.description = description;
	}

	public VatSummaryItem(String vatName, Double vatPercent, MonetaryAmount totalNet, MonetaryAmount itemVat, ItemAccountType accountType) {
		this.vatName = vatName;
		this.vatPercent = vatPercent;
 		this.net = totalNet;
		this.vat = itemVat;
		this.salesEqTax = Money.zero(net.getCurrency());
		this.accountType = accountType;
		this.description = accountType.getName();
   }

    /**
	 * Creates a {@link VatSummaryItem} from an existing {@link VatSummaryItem}.
	 * 
	 * @param vatSummaryItem
	 */
	public static VatSummaryItem of(VatSummaryItem vatSummaryItem) {
	    VatSummaryItem vatSummaryItemCopy = new VatSummaryItem(vatSummaryItem.getVatName(), vatSummaryItem.getVatPercent(), vatSummaryItem.getNet(), vatSummaryItem.getVat(), vatSummaryItem.getDescription());
	    if(vatSummaryItem.getSalesEqTaxPercent() != null) {
	    	vatSummaryItemCopy.setSalesEqTax(vatSummaryItem.getSalesEqTax());
	    	vatSummaryItemCopy.setSalesEqTaxPercent(vatSummaryItem.getSalesEqTaxPercent());
	    }
		return vatSummaryItemCopy;
//		this.netRoundingError = 0.0;
//		this.vatRoundingError = 0.0;
	}

	/**
	 * Add the net and vat value from an other VatSummaryItem.
	 * 
	 * @param other
	 *            The other VatSummaryItem
	 */
	public void add(VatSummaryItem other) {
	    this.net = this.net.add(other.net);
	    this.vat = this.vat.add(other.vat);
	    this.salesEqTax = this.salesEqTax != null && other.salesEqTax != null ? this.salesEqTax.add(other.salesEqTax) : Money.zero(DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale()));
	}

	/**
	 * Round the net and vat value and store the rounding error in the property
	 * "xxRoundingError"
	 */
	public void round() {
        CurrencyUnit currencyUnit = DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale());
        MonetaryRounding rounding = DataUtils.getInstance().getRounding(currencyUnit);  

		// Round the net value
		netRoundingError = this.net.getNumber().doubleValue() - this.net.with(rounding).getNumber().doubleValue();
		this.net = this.net.with(rounding);

		// Round the vat value
		vatRoundingError = this.vat.getNumber().doubleValue() - this.vat.with(rounding).getNumber().doubleValue();
		this.vat = this.vat.with(rounding);
	}

	/**
	 * Sets the absolute net value
	 * 
	 * @param Net
	 *            value
	 */
	public void setNet(MonetaryAmount value) {
		this.net = value;
	}

	/**
	 * Sets the absolute vat value
	 * 
	 * @param Vat
	 *            value
	 */
	public void setVat(MonetaryAmount value) {
		this.vat = value;
	}

	/**
	 * Get the absolute net value
	 * 
	 * @return Net value as Double
	 */
	public MonetaryAmount getNet() {
		return net;
	}

	/**
	 * Get the absolute vat value
	 * 
	 * @return Vat value as Double
	 */
	public MonetaryAmount getVat() {
		return vat;
	}

	/**
	 * Get the name of the vat
	 * 
	 * @return Vat name as string
	 */
	public String getVatName() {
		return vatName;
	}

	/**
	 * Get the description
	 * 
	 * @return Vat name as string
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Percent value of this VatSummaryItem
	 * 
	 * @return Vat in percent
	 */
	public Double getVatPercent() {
		return vatPercent;
	}

	/**
	 * Get the rounding error of the net value
	 * 
	 * @return rounding error as Double
	 */
	public Double getNetRoundingError() {
		return netRoundingError;
	}

	/**
	 * Get the rounding error of the vat value
	 * 
	 * @return rounding error as Double
	 */
	public Double getVatRoundingError() {
		return vatRoundingError;
	}

	/**
	 * Sets the rounding error of the net value
	 * 
	 * @param new rounding error value
	 */
	public void setNetRoundingError(Double value) {
		netRoundingError = value;
	}

	/**
	 * Sets the rounding error of the vat value
	 * 
	 * @param new rounding error value
	 */
	public void setVatRoundingError(Double value) {
		vatRoundingError = value;
	}

	/**
	 * @return the accountType
	 */
	public final ItemAccountType getAccountType() {
		return accountType;
	}

	/**
	 * @return the salesEqTax
	 */
	public final MonetaryAmount getSalesEqTax() {
		return salesEqTax;
	}

	/**
	 * @param salesEqTax the salesEqTax to set
	 */
	public final void setSalesEqTax(MonetaryAmount salesEqTax) {
		this.salesEqTax = salesEqTax;
	}

	/**
	 * @return the salesEqTaxPercent
	 */
	public final Double getSalesEqTaxPercent() {
		return salesEqTaxPercent;
	}

	/**
	 * @param salesEqTaxPercent the salesEqTaxPercent to set
	 */
	public final void setSalesEqTaxPercent(Double salesEqTaxPercent) {
		this.salesEqTaxPercent = salesEqTaxPercent;
		
		// recalculate Sales Equalization Tax
		if(this.net != null && salesEqTaxPercent != null) {
			this.salesEqTax = this.net.multiply(this.salesEqTaxPercent);
		}
	}

	/**
	 * Compares this VatSummaryItem with an other Compares vat percent value and
	 * vat name.
	 * 
	 * @param o
	 *            The other VatSummaryItem
	 * @return result of the comparison
	 */
	@Override
	public int compareTo(VatSummaryItem other) {
//		VatSummaryItem other = (VatSummaryItem) o;

		// First compare the vat value in percent
		if (this.vatPercent < other.vatPercent) {
			return -1;
		}
		if (this.vatPercent > other.vatPercent) {
			return 1;
		}

		// Then the vat name
		int i = StringUtils.defaultString(this.vatName, "").compareTo(StringUtils.defaultString(other.vatName, ""));
		if (i != 0) {
			return i;
		}

		// Then the description
		return this.description.compareToIgnoreCase(other.description);
	}
	
    @Override
    public String toString() {
        return new StringBuilder("[Amount (net): ").append(net)
        		.append("; VAT: ").append(vat).append(" (").append(vatPercent*100).append("%) - ")
        		.append(StringUtils.defaultIfBlank(vatName, "(no name)"))
        		.append("; SET: ").append(salesEqTaxPercent != null ? salesEqTaxPercent : "0").append("%").append(salesEqTax != null ? " (" + salesEqTax + ")" : "")
        		.append(']').toString();
    }
}
