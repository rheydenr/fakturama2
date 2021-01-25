/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.dto;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;

import org.apache.commons.lang3.BooleanUtils;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.DocumentItem;

/**
 * Builder for {@link Price}s
 *
 */
public class PriceBuilder {
    private DocumentItem documentItem;
    private MonetaryAmount amount;
    private boolean useSET = false;
    private double scaleFactor = 1.0;
    private Double vatPercent;
    private boolean noVat;
    private Double quantity;
    private MonetaryAmount unitPrice;
    private Double discount;
    private Double salesEqualizationTax;
    private boolean useAsGross;

    /**
     * Build a {@link Price} from a {@link DocumentItem}. If a
     * {@link DocumentItem} is set it takes precedence over single values set.
     * 
     * @param documentItem
     *            the {@link DocumentItem} to use
     * @return {@link PriceBuilder}
     */
    public PriceBuilder withDocumentItem(DocumentItem documentItem) {
        this.documentItem = documentItem;
        return this;
    }

    public PriceBuilder withAmount(MonetaryAmount amount) {
        this.amount = amount;
        return this;
    }

    public PriceBuilder withUseSET(boolean useSET) {
        this.useSET = useSET;
        return this;
    }

    public PriceBuilder withScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        return this;
    }
    
    public PriceBuilder withVatPercent(Double vatPercent) {
        this.vatPercent = vatPercent;
        return this;
    }
    
    public PriceBuilder withDiscount(Double discount) {
       this.discount = discount;
       return this;
    }

    public Price build() {
        Price price;
        if (documentItem != null) {
            vatPercent = documentItem.getItemVat().getTaxValue();
            noVat = BooleanUtils.toBoolean(documentItem.getNoVat());
            // if noVat is set, the vat value is set to 0.0

            this.quantity = BooleanUtils.toBoolean(documentItem.getOptional()) ? Double.valueOf(0.0) : documentItem.getQuantity();
            this.unitPrice = Money.of(documentItem.getPrice(), DataUtils.getInstance().getDefaultCurrencyUnit()).multiply(scaleFactor);
            this.discount = documentItem.getItemRebate();

            this.salesEqualizationTax = useSET ? documentItem.getItemVat().getSalesEqualizationTax() : null;

            this.useAsGross = false;

            //     public Price(Double quantity, MonetaryAmount unitPrice, Double vatPercent, Double discount, boolean noVat, 
            // boolean asGross, Double salesEqualizationTax) {
        }
        
        CurrencyUnit currencyUnit = DataUtils.getInstance().getDefaultCurrencyUnit();
        MonetaryRounding rounding = DataUtils.getInstance().getRounding(currencyUnit);  
        this.vatPercent = (noVat) ? Double.valueOf(0.0) : vatPercent;

        price = new Price(currencyUnit, rounding, useAsGross);
        // set attributes
        price.setQuantity(quantity);
        price.setUnitPrice(unitPrice);
        price.setVatPercent(vatPercent);
        price.setDiscount(discount);
        price.setSalesEqTaxPercent(salesEqualizationTax);
        
        price.calculate();

        return price;
    }
}
