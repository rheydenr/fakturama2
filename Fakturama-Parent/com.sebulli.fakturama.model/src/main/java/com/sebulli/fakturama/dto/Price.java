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
 *     Ralf Heydenreich - enhancement for JavaMoney
 */

package com.sebulli.fakturama.dto;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;

import org.apache.commons.lang3.BooleanUtils;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.RoundedMoney;

import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.AbstractVoucherItem;
import com.sebulli.fakturama.model.DocumentItem;

/**
 * Price class.
 * 
 * Calculate gross values from net values. Rounding of all values.
 * 
 */
public class Price {

	private Double quantity;
	private Double vatPercent;
	private Double discount;

	// unit values
	private MonetaryAmount unitPrice;
	private MonetaryAmount unitNet;
	private MonetaryAmount unitVat;
	private MonetaryAmount unitGross;

	// unit values but with discount
	private MonetaryAmount unitNetDiscounted;
	private MonetaryAmount unitVatDiscounted;
	private MonetaryAmount unitGrossDiscounted;

	// total values
	private MonetaryAmount totalNet;
	private MonetaryAmount totalVat;
	private MonetaryAmount totalGross;
	
	private DataUtils dataUtils;

	// unit values rounded
	private MonetaryAmount unitNetRounded;
	private MonetaryAmount unitVatRounded;
	private MonetaryAmount unitGrossRounded;
	
	// unit values but with discount rounded
	private MonetaryAmount unitNetDiscountedRounded;
	private MonetaryAmount unitVatDiscountedRounded;
	private MonetaryAmount unitGrossDiscountedRounded;
	
	// total values rounded
	private MonetaryAmount totalNetRounded;
	private MonetaryAmount totalVatRounded;
	private MonetaryAmount totalGrossRounded;

	/**
	 * Create a price value from an item. Remember: Each item's price is given as net value.
	 * 
	 * @param item
	 *            Item as DocumentItem
	 */
	public Price(DocumentItem item) {
		this(BooleanUtils.isTrue(item.getOptional()) ? Double.valueOf(0.0) : item.getQuantity(), 
		        Money.of(item.getPrice(), DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale())), 
		        item.getItemVat().getTaxValue(), item.getItemRebate(), BooleanUtils.toBoolean(item.getNoVat()), false);
	}

	/**
	 * Create a price value from an item and a scale factor
	 * 
	 * @param item
	 *            Item as DocumentItem
	 *            
	 * @param scaleFactor
	 * 				Scale factor of this item
	 */
    public Price(DocumentItem item, Double scaleFactor) {
        this(BooleanUtils.toBoolean(item.getOptional()) ? Double.valueOf(0.0) : item.getQuantity(), Money.of(item.getPrice(), DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale())).multiply(scaleFactor), item
                .getItemVat().getTaxValue(), item.getItemRebate(), BooleanUtils.toBoolean(item.getNoVat()), false);
    }

//	/**
//	 * Constructor Create a price value from an expenditure item
//	 * 
//	 * @param item
//	 *            Item as UniDataSet
//	 */
//	public Price(DataSetVoucherItem item) {
//		this(1.0, item.getPrice(), item.getItemVat().getTaxValue(), Double.valueOf(0.0), false, false);
//	}

	/**
	 * Constructor Create a price value from an expenditure item and a scale factor
	 * 
	 * @param item
	 *            Item as UniDataSet
	 *            
	 * @param scaleFactor
	 * 				Scale factor of this expenditure item
	 */
	public Price(AbstractVoucherItem item, Double scaleFactor) {
		this(1.0, Money.of(item.getPrice(), DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale())).multiply(scaleFactor), item.getVat().getTaxValue(), Double.valueOf(0.0), false, false);
	}

	
	/**
	 * Constructor Create a price value from a net value
	 * 
	 * @param net
	 *            Net value
	 */
	public Price(MonetaryAmount net) {
		this(net, Double.valueOf(0.0));
	}

	/**
	 * Constructor Create a price value from a net value and a vat value
	 * 
	 * @param net
	 *            Net value
	 * @param vatPercent
	 *            VAT
	 */
	public Price(MonetaryAmount net, Double vatPercent) {
		this(Double.valueOf(1.0), net, vatPercent, Double.valueOf(0.0), false, false);
	}

	/**
	 * Constructor Create a price value from a value where value can be a net or
	 * a gross value
	 * 
	 * @param price
	 *            Value (can be net or gross)
	 * @param vatPercent
	 *            VAT value
	 * @param noVat
	 *            true, if VAT should be Double.valueOf(0.0)
	 * @param asGross
	 *            true, if price is a gross value
	 */
	public Price(MonetaryAmount price, Double vatPercent, boolean noVat, boolean asGross) {
		this(Double.valueOf(1.0), price, vatPercent, Double.valueOf(0.0), noVat, asGross);
	}

	/**
	 * Constructor Create a price value from a price value with quantity, vat
	 * and discount
	 * 
	 * @param quantity
	 *            Quantity
	 * @param unitPrice
	 *            Unit price (can be net or gross)
	 * @param vatPercent
	 *            VAT value
	 * @param discount
	 *            Discount value
	 * @param noVat
	 *            <code>true</code> if VAT is 0.0
	 * @param asGross
	 *            <code>true</code> if price is a gross value
	 */
	public Price(Double quantity, MonetaryAmount unitPrice, Double vatPercent, Double discount, boolean noVat, boolean asGross) {

		// if noVat is set, the vat value is set to 0.0
        if (noVat) {
            this.vatPercent = Double.valueOf(0.0);
        } else {
            this.vatPercent = vatPercent;
        }
		
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.discount = discount;

		// do the calculation
		calculate(asGross);
	}

	/**
	 * Calculate the price and round all values
	 * 
	 * @param asGross
	 *            <code>true</code> if price is a gross value
	 */
	private void calculate(boolean asGross) {
         CurrencyUnit currencyUnit = getDataUtils().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale());
         MonetaryRounding rounding = getDataUtils().getRounding(currencyUnit);  

		// Calculate net from gross
		if (asGross) {
			this.unitGross = this.unitPrice;
			this.unitNet = this.unitPrice.divide(1 + vatPercent);
		}
		// or gross from net
		else {
			this.unitGross = this.unitPrice.multiply(1 + vatPercent);
			this.unitNet = this.unitPrice;
		}

		// Calculate the absolute VAT value from net value and VAT in percent
		this.unitVat = this.unitNet.multiply(vatPercent);

		// Calculate the discount factor.
		// Discount factor is a value between 0.0 and 1.0.
		// If the discount is -30% (-0.3), the discount factor is 0.7
		// Only discount values in the range -100% to -0% are allowed
		Double discountFactor = (1 + this.discount);
		if ((discountFactor > Double.valueOf(1.0)) || (discountFactor <= Double.valueOf(0.0))) {
// TODO			Logger.logError("Discount value out of range: " + String.valueOf(this.discount));
			discountFactor = Double.valueOf(1.0);
		}

		// Calculate the discounted values and use the quantity
		this.unitNetDiscounted = this.unitNet.multiply(discountFactor);
		this.unitVatDiscounted =  this.unitVat.multiply(discountFactor);
		this.unitGrossDiscounted =  this.unitGross.multiply(discountFactor);

		// Calculate the total values and use the quantity
		this.totalNet = this.unitNet.multiply(discountFactor).multiply(this.quantity);
		this.totalVat = this.unitVat.multiply(this.quantity * discountFactor);
		this.totalGross = this.unitGross.multiply(this.quantity * discountFactor);

		// Normally, the vat and gross value is rounded,
		// and the net value is the difference.
		// But only if the Net value is still a rounded value and the gross is not,
		// then the rounded gross value is calculated from rounded net and vat. 
		if (!asGross) {
		    
			this.unitNetRounded = unitNet.with(rounding);
			this.unitVatRounded = unitVat.with(rounding);
			this.unitGrossRounded = this.unitNetRounded.add(this.unitVatRounded);

			this.unitNetDiscountedRounded = unitNetDiscounted.with(rounding);
			this.unitVatDiscountedRounded = unitVatDiscounted.with(rounding);
			this.unitGrossDiscountedRounded = this.unitNetDiscountedRounded.add(this.unitVatDiscountedRounded);

			this.totalNetRounded = totalNet.with(rounding);
			this.totalVatRounded = totalVat.with(rounding);
			this.totalGrossRounded = this.totalNetRounded.add(this.totalVatRounded).with(rounding);
		}
		else {
			this.unitGrossRounded = unitGross.with(rounding);
			this.unitVatRounded = unitVat.with(rounding);
			this.unitNetRounded = this.unitGrossRounded.subtract(this.unitVatRounded);

			this.unitGrossDiscountedRounded = unitGrossDiscounted.with(rounding);
			this.unitVatDiscountedRounded = unitVatDiscounted.with(rounding);
			this.unitNetDiscountedRounded = this.unitGrossDiscountedRounded.subtract(this.unitVatDiscountedRounded).with(rounding);

			this.totalGrossRounded = totalGross.with(rounding);
			this.totalVatRounded = totalVat.with(rounding);
			this.totalNetRounded = this.totalGrossRounded.subtract(this.totalVatRounded).with(rounding);
		}
	}

	/**
	 * Get the VAT value in percent
	 * 
	 * @return VAT as formated string
	 */
	public String getVatPercent() {
		return getDataUtils().DoubleToFormatedPercent(vatPercent);
	}

	/**
	 * Get the discounted net value of one unit.
	 * 
	 * @return Net value as PriceValue
	 */
	public MonetaryAmount getUnitNetDiscounted() {
		return unitNetDiscounted;
	}

	/**
	 * Get the discounted VAT value of one unit
	 * 
	 * @return VAT value as PriceValue
	 */
	public MonetaryAmount getUnitVatDiscounted() {
		return unitVatDiscounted;
	}

	/**
	 * Get the discounted gross value of one unit.
	 * 
	 * @return Gross value as MonetaryAmount
	 */
	public MonetaryAmount getUnitGrossDiscounted() {
		return unitGrossDiscounted;
	}

	/**
	 * Get the net value of one unit.
	 * 
	 * @return Net value as PriceValue
	 */
	public MonetaryAmount getUnitNet() {
		return unitNet;
	}

	/**
	 * Get the VAT value of one unit
	 * 
	 * @return VAT value as PriceValue
	 */
	public MonetaryAmount getUnitVat() {
		return unitVat;
	}

	/**
	 * Get the gross value of one unit.
	 * 
	 * @return Gross value as PriceValue
	 */
	public MonetaryAmount getUnitGross() {
		return unitGross;
	}

	/**
	 * Get the total net value.
	 * 
	 * @return Net value as PriceValue
	 */
	public MonetaryAmount getTotalNet() {
		return totalNet;
	}

	/**
	 * Get the total vat value.
	 * 
	 * @return Vat value as PriceValue
	 */
	public MonetaryAmount getTotalVat() {
		return totalVat;
	}

	/**
	 * Get the total gross value.
	 * 
	 * @return Gross value as PriceValue
	 */
	public MonetaryAmount getTotalGross() {
		return totalGross;
	}

	/**
	 * Get the net value of one unit as rounded value.
	 * 
	 * @return Net value as PriceValue
	 */
	public MonetaryAmount getUnitNetRounded() {
		return RoundedMoney.from(unitNet);
	}

	/**
	 * Get the VAT value of one unit as rounded value.
	 * 
	 * @return VAT value as PriceValue
	 */
	public MonetaryAmount getUnitVatRounded() {
		return RoundedMoney.from(unitVat);
	}

	/**
	 * Get the gross value of one unit as rounded value.
	 * 
	 * @return Gross value as PriceValue
	 */
	public MonetaryAmount getUnitGrossRounded() {
		return RoundedMoney.from(unitGross);
	}

	/**
	 * Get the discounted net value of one unit as rounded value.
	 * 
	 * @return discounted Net value as PriceValue
	 */
	public MonetaryAmount getUnitNetDiscountedRounded() {
		return RoundedMoney.from(unitNetDiscounted);
	}

	/**
	 * Get the discounted VAT value of one unit as rounded value.
	 * 
	 * @return discounted VAT value as PriceValue
	 */
	public MonetaryAmount getUnitVatDiscountedRounded() {
		return RoundedMoney.from(unitVatDiscounted);
	}

	/**
	 * Get the discounted gross value of one unit as rounded value.
	 * 
	 * @return discounted gross value as PriceValue
	 */
	public MonetaryAmount getUnitGrossDiscountedRounded() {
		return RoundedMoney.from(unitGrossDiscounted);
	}

	/**
	 * Get the total net as rounded value.
	 * 
	 * @return Net value as PriceValue
	 */
	public MonetaryAmount getTotalNetRounded() {
		return RoundedMoney.from(totalNet);
	}

	/**
	 * Get the total vat as rounded value.
	 * 
	 * @return Vat value as PriceValue
	 */
	public MonetaryAmount getTotalVatRounded() {
		return RoundedMoney.from(totalVat);
	}

	/**
	 * Get the total gross as rounded value.
	 * 
	 * @return Gross value as PriceValue
	 */
	public MonetaryAmount getTotalGrossRounded() {
		return RoundedMoney.from(totalGross);
	}

    /**
     * @return the dataUtils
     */
    public DataUtils getDataUtils() {
        if(dataUtils == null) {
            dataUtils = DataUtils.getInstance();
        }
        return dataUtils;
    }

    /**
     * @param dataUtils the dataUtils to set
     */
    public void setDataUtils(DataUtils dataUtils) {
        this.dataUtils = dataUtils;
    }

}
