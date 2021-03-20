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

import java.util.Optional;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.javamoney.moneta.Money;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.VoucherItem;

/**
 * Price class.
 * 
 * Calculate gross values from net values. Rounding of all values.
 * 
 */
public class Price {
	
	private Double quantity;
	private Double vatPercent;
	private Double salesEqTaxPercent;
	private Double discount;

	// unit values
	private MonetaryAmount unitPrice;
	private MonetaryAmount unitNet;
	private MonetaryAmount unitVat;
	private MonetaryAmount unitSalesEqTax;
	private MonetaryAmount unitGross;

	// unit values but with discount
	private MonetaryAmount unitNetDiscounted;
	private MonetaryAmount unitVatDiscounted;
	private MonetaryAmount unitSalesEqTaxDiscounted;
	private MonetaryAmount unitGrossDiscounted;

	// total values
	private MonetaryAmount totalNet;
	private MonetaryAmount totalVat;
	private MonetaryAmount totalSalesEqTax;
	private MonetaryAmount totalGross;
	
	private INumberFormatterService numberFormatterService;

	// unit values rounded
	private MonetaryAmount unitNetRounded;
	private MonetaryAmount unitVatRounded;
	private MonetaryAmount unitSalesEqTaxRounded;
	private MonetaryAmount unitGrossRounded;
	
	// unit values but with discount rounded
	private MonetaryAmount unitNetDiscountedRounded;
	private MonetaryAmount unitVatDiscountedRounded;
	private MonetaryAmount unitSalesEqTaxDiscountedRounded;
	private MonetaryAmount unitGrossDiscountedRounded;
	
	// total values rounded
	private MonetaryAmount totalNetRounded;
	private MonetaryAmount totalVatRounded;
	private MonetaryAmount totalSalesEqTaxRounded;
	private MonetaryAmount totalGrossRounded;
    private MonetaryAmount totalAllowance;
    private MonetaryAmount unitAllowance;
    private CurrencyUnit currencyUnit;
    private MonetaryRounding rounding;
    private boolean asGross;
	
    // only visible for PriceBuilder
    protected Price(CurrencyUnit currencyUnit, MonetaryRounding rounding, boolean asGross) {
        this.currencyUnit = currencyUnit;
        this.setRounding(rounding);
        this.asGross = asGross;
    }
	
	public Price(DocumentItem item) {
		this(item, false);
	}

	/**
	 * Create a price value from an item. Remember: Each item's price is given as net value.
	 * 
	 * @param item
	 *            Item as DocumentItem
	 */
	public Price(DocumentItem item, boolean useSET) {
		this(   BooleanUtils.isTrue(item.getOptional()) ? Double.valueOf(0.0) : item.getQuantity(), 
		        Money.of(item.getPrice(), DataUtils.getInstance().getDefaultCurrencyUnit()), 
		        item.getItemVat().getTaxValue(), 
		        item.getItemRebate(), 
		        BooleanUtils.toBoolean(item.getNoVat()), 
		        false, 
		        useSET ? item.getItemVat().getSalesEqualizationTax() : null);
	}
	
//    public Price(DocumentItem item, Double scaleFactor, boolean useSET) {
//    	this(item, scaleFactor, useSET, false);
//    }

	/**
	 * Create a price value from an item and a scale factor
	 * 
	 * @param item
	 *            Item as DocumentItem
	 *            
	 * @param scaleFactor
	 * 				Scale factor of this item
	 */
    public Price(DocumentItem item, Double scaleFactor, boolean useSET, boolean useGross) {
        this(BooleanUtils.toBoolean(item.getOptional()) ? Double.valueOf(0.0) : item.getQuantity(), 
        	 Money.of(item.getPrice(), DataUtils.getInstance().getDefaultCurrencyUnit()).multiply(scaleFactor), 
        	 item.getItemVat().getTaxValue(), 
        	 item.getItemRebate(), 
        	 BooleanUtils.toBoolean(item.getNoVat()), 
        	 useGross,
        	 useSET ? item.getItemVat().getSalesEqualizationTax() : null);
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
	public Price(VoucherItem item, Double scaleFactor) {
		this(Double.valueOf(1.0), 
		     Money.of(item.getPrice(), DataUtils.getInstance().getDefaultCurrencyUnit()).multiply(scaleFactor), 
		     item.getVat().getTaxValue(), 
		     Double.valueOf(0.0), 
		     false, 
		     false);
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

	public Price(MonetaryAmount net, Double vatPercent, Double salesEqualizationTax) {
		this(Double.valueOf(1.0), net, vatPercent, Double.valueOf(0.0), false, false, salesEqualizationTax);
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
	 * @param salesEqualizationTax
	 *            the sales equalization tax in %, if any
	 */
    public Price(Double quantity, MonetaryAmount unitPrice, Double vatPercent, Double discount, boolean noVat, boolean asGross, Double salesEqualizationTax) {

        // if noVat is set, the vat value is set to 0.0
        this.vatPercent = (noVat) ? Double.valueOf(0.0) : vatPercent;

        this.quantity = quantity != null ? quantity : Double.valueOf(0);
        this.unitPrice = unitPrice;
        this.discount = discount == null ? Double.valueOf(0.0) : discount;
        this.asGross = asGross;

        if (salesEqualizationTax != null) {
            this.salesEqTaxPercent = salesEqualizationTax;
        }

        // do the calculation
        calculate(asGross);
    }

	public Price(Double quantity, MonetaryAmount unitPrice, Double vatPercent, Double discount, boolean noVat, boolean asGross) {
		this(quantity, unitPrice, vatPercent, discount, noVat, asGross, null);
	}
	
	protected void calculate() {
	    calculate(asGross);
	}
	
	/**
	 * Calculate the price and round all values
	 * 
	 * @param asGross
	 *            <code>true</code> if price is a gross value
	 */
	private void calculate(boolean asGross) {
	    
		// Calculate net from gross...
		if (asGross) {
			this.unitGross = this.unitPrice;
			this.unitNetDiscounted = this.unitGross.multiply(1-this.discount).divide(1 -this.discount + vatPercent + Optional.ofNullable(salesEqTaxPercent).orElse(NumberUtils.DOUBLE_ZERO)); //.with(getRounding());
		}
		else {
		    // ...or gross from net
			this.unitGross = this.unitPrice.multiply(1 + vatPercent + Optional.ofNullable(salesEqTaxPercent).orElse(NumberUtils.DOUBLE_ZERO));
			this.unitNet = this.unitPrice;
		}
		
		// SalesEqTax
		if(salesEqTaxPercent != null) {
			this.unitSalesEqTax = this.unitNet.multiply(salesEqTaxPercent);
		} else {
			this.unitSalesEqTax = Money.zero(getCurrencyUnit());
		}

		// Calculate the discount factor.
		// Discount factor is a value between 0.0 and 1.0.
		// If the discount is -30% (-0.3), the discount factor is 0.7
		// Only discount values in the range -100% to -0% are allowed
		Double discountFactor = (1 + this.discount);
		if (discountFactor > NumberUtils.DOUBLE_ONE || discountFactor < NumberUtils.DOUBLE_ZERO) {
// TODO			Logger.logError("Discount value out of range: " + String.valueOf(this.discount));
			discountFactor = NumberUtils.DOUBLE_ONE;
		}

		// Normally, the VAT and gross value is rounded,
		// and the net value is the difference.
		// But only if the Net value is still a rounded value and the gross is not,
		// then the rounded gross value is calculated from rounded net and VAT. 
		if (asGross) {

			// Calculate the discounted values and use the quantity
			this.unitNet = this.unitNetDiscounted.divide(discountFactor);
			this.unitNetRounded = this.unitNet.with(getRounding());
			this.unitNetDiscountedRounded = unitNetDiscounted.with(getRounding());

			// Calculate the absolute VAT value from gross value and VAT in percent
			this.unitVat = this.unitGross.multiply(vatPercent).divide(1+vatPercent);
			this.unitVatRounded = unitVat.with(getRounding());  // w/o discount!
			this.unitVatDiscounted = unitVat.multiply(discountFactor);
			this.unitVatDiscountedRounded = unitNetDiscountedRounded.multiply(vatPercent).with(getRounding());
			
			this.unitSalesEqTaxRounded = unitSalesEqTax.with(getRounding());
			this.unitSalesEqTaxDiscounted = this.unitSalesEqTax.multiply(discountFactor);
			this.unitSalesEqTaxDiscountedRounded = unitSalesEqTaxDiscounted.with(getRounding());
		
			this.unitAllowance = this.unitNetDiscountedRounded.multiply(this.discount).divide(discountFactor).with(getRounding());
			this.totalAllowance = this.unitNetDiscountedRounded.multiply(this.quantity).multiply(this.discount).with(getRounding());

			this.unitGrossRounded = unitGross.with(getRounding());
			this.unitGrossDiscounted = this.unitGross.multiply(discountFactor);
			this.unitGrossDiscountedRounded = this.unitGross.add(unitAllowance).with(getRounding());

			// Calculate the total values and use the quantity
			this.totalNet = unitNetDiscountedRounded.multiply(this.quantity);
			this.totalNetRounded = this.unitNetDiscountedRounded.multiply(quantity).with(getRounding());
			this.totalGross = unitGrossDiscountedRounded.multiply(this.quantity);
			this.totalGrossRounded =  (unitNetDiscountedRounded.add(unitVatRounded)).multiply(this.quantity);
			this.totalVat = this.unitVatDiscountedRounded.multiply(this.quantity);
		} else {

			// Calculate the absolute VAT value from net value and VAT in percent
			this.unitVat = this.unitNet.multiply(vatPercent);
			this.unitVatRounded = unitVat.with(getRounding());
			this.unitVatDiscounted = this.unitVat.multiply(discountFactor);
			this.unitVatDiscountedRounded = unitVatDiscounted.with(getRounding());

			// Calculate the discounted values and use the quantity
			this.unitNetRounded = unitNet.with(getRounding());
			this.unitNetDiscounted = this.unitNet.multiply(discountFactor);
			this.unitNetDiscountedRounded = unitNetDiscounted.with(getRounding());
			
			this.unitSalesEqTaxRounded = unitSalesEqTax.with(getRounding());
			this.unitSalesEqTaxDiscounted = this.unitSalesEqTax.multiply(discountFactor);
			this.unitSalesEqTaxDiscountedRounded = unitSalesEqTaxDiscounted.with(getRounding());
			
			this.unitGrossRounded = this.unitNetRounded.add(this.unitVatRounded).add(this.unitSalesEqTax).with(getRounding());
			this.unitGrossDiscounted = this.unitGross.multiply(discountFactor);
			this.unitGrossDiscountedRounded = this.unitNetDiscounted.add(this.unitVatDiscounted).add(this.unitSalesEqTaxDiscounted).with(getRounding());

			this.unitAllowance = this.unitNet.multiply(this.discount).with(getRounding());
			this.totalAllowance = this.unitAllowance.multiply(this.quantity);

			// Calculate the total values and use the quantity
			this.totalNet = this.unitNetDiscounted.multiply(this.quantity);
			this.totalNetRounded = totalNet.with(getRounding());
			this.totalGross = this.unitGrossDiscounted.multiply(this.quantity).with(getRounding());
			this.totalGrossRounded = this.unitGrossDiscountedRounded.multiply(this.quantity).with(getRounding());
			this.totalVat = this.unitVatDiscounted.multiply(this.quantity);
		}
		this.totalSalesEqTax = this.unitSalesEqTax.multiply(this.quantity * discountFactor);
		this.totalSalesEqTaxRounded = totalSalesEqTax.with(getRounding());
		this.totalVatRounded = totalGrossRounded.subtract(totalNetRounded).subtract(totalSalesEqTaxRounded);
	}

	/**
	 * Get the VAT value in percent
	 * 
	 * @return VAT as formated string
	 */
	public String getVatPercent() {
		return getNumberFormatterService().DoubleToFormatedPercent(vatPercent);
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
		return this.unitNetRounded;
	}

	/**
	 * Get the VAT value of one unit as rounded value.
	 * 
	 * @return VAT value as PriceValue
	 */
	public MonetaryAmount getUnitVatRounded() {
		return this.unitVatRounded;
	}

	/**
	 * Get the gross value of one unit as rounded value.
	 * 
	 * @return Gross value as PriceValue
	 */
	public MonetaryAmount getUnitGrossRounded() {
		return this.unitGrossRounded;
	}

	/**
	 * Get the discounted net value of one unit as rounded value.
	 * 
	 * @return discounted Net value as PriceValue
	 */
	public MonetaryAmount getUnitNetDiscountedRounded() {
		return this.unitNetDiscountedRounded;
	}

	/**
	 * Get the discounted VAT value of one unit as rounded value.
	 * 
	 * @return discounted VAT value as PriceValue
	 */
	public MonetaryAmount getUnitVatDiscountedRounded() {
		return this.unitVatDiscountedRounded;
	}

	/**
	 * Get the discounted gross value of one unit as rounded value.
	 * 
	 * @return discounted gross value as PriceValue
	 */
	public MonetaryAmount getUnitGrossDiscountedRounded() {
		return this.unitGrossDiscountedRounded;
	}

	/**
	 * Get the total net as rounded value.
	 * 
	 * @return Net value as PriceValue
	 */
	public MonetaryAmount getTotalNetRounded() {
		return this.totalNetRounded;
	}

	/**
	 * Get the total vat as rounded value.
	 * 
	 * @return Vat value as PriceValue
	 */
	public MonetaryAmount getTotalVatRounded() {
		return this.totalVatRounded;
	}

	/**
	 * Get the total gross as rounded value.
	 * 
	 * @return Gross value as PriceValue
	 */
	public MonetaryAmount getTotalGrossRounded() {
		return this.totalGrossRounded;
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
	}

	/**
	 * @return the unitSalesEqTax
	 */
	public final MonetaryAmount getUnitSalesEqTax() {
		return unitSalesEqTax;
	}

	/**
	 * @param unitSalesEqTax the unitSalesEqTax to set
	 */
	public final void setUnitSalesEqTax(MonetaryAmount unitSalesEqTax) {
		this.unitSalesEqTax = unitSalesEqTax;
	}

	/**
	 * @return the unitSalesEqTaxDiscounted
	 */
	public final MonetaryAmount getUnitSalesEqTaxDiscounted() {
		return unitSalesEqTaxDiscounted;
	}

	/**
	 * @param unitSalesEqTaxDiscounted the unitSalesEqTaxDiscounted to set
	 */
	public final void setUnitSalesEqTaxDiscounted(MonetaryAmount unitSalesEqTaxDiscounted) {
		this.unitSalesEqTaxDiscounted = unitSalesEqTaxDiscounted;
	}

	/**
	 * @return the totalSalesEqTax
	 */
	public final MonetaryAmount getTotalSalesEqTax() {
		return totalSalesEqTax;
	}

	/**
	 * @param totalSalesEqTax the totalSalesEqTax to set
	 */
	public final void setTotalSalesEqTax(MonetaryAmount totalSalesEqTax) {
		this.totalSalesEqTax = totalSalesEqTax;
	}

	/**
	 * @return the unitSalesEqTaxRounded
	 */
	public final MonetaryAmount getUnitSalesEqTaxRounded() {
		return unitSalesEqTaxRounded;
	}

	/**
	 * @param unitSalesEqTaxRounded the unitSalesEqTaxRounded to set
	 */
	public final void setUnitSalesEqTaxRounded(MonetaryAmount unitSalesEqTaxRounded) {
		this.unitSalesEqTaxRounded = unitSalesEqTaxRounded;
	}

	/**
	 * @return the unitSalesEqTaxDiscountedRounded
	 */
	public final MonetaryAmount getUnitSalesEqTaxDiscountedRounded() {
		return unitSalesEqTaxDiscountedRounded;
	}

	/**
	 * @param unitSalesEqTaxDiscountedRounded the unitSalesEqTaxDiscountedRounded to set
	 */
	public final void setUnitSalesEqTaxDiscountedRounded(MonetaryAmount unitSalesEqTaxDiscountedRounded) {
		this.unitSalesEqTaxDiscountedRounded = unitSalesEqTaxDiscountedRounded;
	}

	/**
	 * @return the totalSalesEqTaxRounded
	 */
	public final MonetaryAmount getTotalSalesEqTaxRounded() {
		return totalSalesEqTaxRounded;
	}

	/**
	 * @param totalSalesEqTaxRounded the totalSalesEqTaxRounded to set
	 */
	public final void setTotalSalesEqTaxRounded(MonetaryAmount totalSalesEqTaxRounded) {
		this.totalSalesEqTaxRounded = totalSalesEqTaxRounded;
	}

	public MonetaryAmount getTotalAllowance() {
        return totalAllowance;
    }

    public MonetaryAmount getUnitAllowance() {
        return unitAllowance;
    }

    /**
	 * @return the numberFormatterService
	 */
	public INumberFormatterService getNumberFormatterService() {
		if(numberFormatterService == null) {
			ServiceReference<INumberFormatterService> servRef = Activator.getContext().getServiceReference(INumberFormatterService.class);
			numberFormatterService = Activator.getContext().getService(servRef);
		}
		return numberFormatterService;
	}

	/**
	 * @param numberFormatterService the numberFormatterService to set
	 */
	public void setNumberFormatterService(INumberFormatterService numberFormatterService) {
		this.numberFormatterService = numberFormatterService;
	}

	protected CurrencyUnit getCurrencyUnit() {
	    if(currencyUnit == null) {
	        currencyUnit = DataUtils.getInstance().getDefaultCurrencyUnit();
	    }
	    return currencyUnit;
	}
	
	protected void setCurrencyUnit(CurrencyUnit currencyUnit) {
        this.currencyUnit = currencyUnit;
    }

    protected void setRounding(MonetaryRounding rounding) {
        this.rounding = rounding;
    }

    protected Double getQuantity() {
        return quantity;
    }

    protected void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    protected Double getDiscount() {
        return discount;
    }

    protected void setDiscount(Double discount) {
        this.discount = discount;
    }

    protected MonetaryAmount getUnitPrice() {
        return unitPrice;
    }

    protected void setUnitPrice(MonetaryAmount unitPrice) {
        this.unitPrice = unitPrice;
    }

    protected void setVatPercent(Double vatPercent) {
        this.vatPercent = vatPercent;
    }

    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }

	public void multiply(int sign) {
		this.unitPrice = this.unitPrice.multiply(sign);		
		calculate(false);
	}

    private MonetaryRounding getRounding() {
        if(rounding == null) {
            rounding = DataUtils.getInstance().getRounding(getCurrencyUnit());
        }
        return rounding;
    }
}
