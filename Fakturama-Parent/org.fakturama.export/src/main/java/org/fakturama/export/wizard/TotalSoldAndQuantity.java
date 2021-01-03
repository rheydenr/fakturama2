/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */
package org.fakturama.export.wizard;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.javamoney.moneta.RoundedMoney;

import com.sebulli.fakturama.misc.DataUtils;

/**
 * This class contains the total sum of volume and quantity
 * 
 * @author Gerd Bartelt
 */
public class TotalSoldAndQuantity {

    private MonetaryAmount totalSoldNet = RoundedMoney.of(BigDecimal.ZERO, getDataUtils().getDefaultCurrencyUnit());
    private Double totalQuantity = Double.valueOf(0.0);
    private MonetaryAmount totalSoldGross;
    private MonetaryAmount totalVat;
    private MonetaryAmount totalRebate;
    private MonetaryAmount totalShipping;

    private DataUtils dataUtils;

    /**
     * Returns the total sum
     * 
     * @return The total sum
     */
    public MonetaryAmount getTotalSoldNet() {
        return totalSoldNet;
    }

    /**
     * Sets the total sum
     * 
     * @param totalSold
     *            The new value of the total sum
     */
    public void setTotalSoldNet(MonetaryAmount totalSold) {
        this.totalSoldNet = totalSold;
    }

    /**
     * Add a new value to the total sum
     * 
     * @param totalSold
     *            The new value to add
     */
    public void addTotalSold(MonetaryAmount totalSold) {
        setTotalSoldNet(this.totalSoldNet.add(totalSold));
    }

    /**
     * Returns the total quantity
     * 
     * @return The total quantity
     */
    public Double getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * Sets the total quantity
     * 
     * @param totalQuantity
     *            The new value of the total quantity
     */
    public void setTotalQuantity(Double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    /**
     * Add a new value to the total sum
     * 
     * @param totalQuantity
     *            The new value to add
     */
    public void addTotalQuantity(Double totalQuantity) {
        this.totalQuantity += totalQuantity;
    }

    public void addTotalVat(MonetaryAmount totalVat) {
        if (this.totalVat == null) {
            this.totalVat = Money.zero(getDataUtils().getDefaultCurrencyUnit());
        }

        if (totalVat != null) {
            setTotalVat(this.totalVat.add(totalVat));
        }
    }

    private void setTotalVat(MonetaryAmount totalVat) {
        this.totalVat = totalVat;
    }

    public void addTotalRebate(MonetaryAmount totalRebate) {
        if (this.totalRebate == null) {
            this.totalRebate = Money.zero(getDataUtils().getDefaultCurrencyUnit());
        }
        if (totalRebate != null) {
            setTotalRebate(this.totalRebate.add(totalRebate));
        }
    }

    private void setTotalRebate(MonetaryAmount totalRebate) {
        this.totalRebate = totalRebate;
    }

    public void addTotalSoldGross(MonetaryAmount totalSoldGross) {
        if (this.totalSoldGross == null) {
            this.totalSoldGross = Money.zero(getDataUtils().getDefaultCurrencyUnit());
        }
        if (totalSoldGross != null) {
            setTotalSoldGross(this.totalSoldGross.add(totalSoldGross));
        }
    }

    public void addTotalShipping(MonetaryAmount totalShipping) {
        if (this.totalShipping == null) {
            this.totalShipping = Money.zero(getDataUtils().getDefaultCurrencyUnit());
        }

        if (totalShipping != null) {
            setTotalShipping(this.totalShipping.add(totalShipping));
        }
    }

    public void setTotalShipping(MonetaryAmount totalShipping) {
        this.totalShipping = totalShipping;
    }

    public void setTotalSoldGross(MonetaryAmount totalSoldGross) {
        this.totalSoldGross = totalSoldGross;
    }

    /**
     * Add a new object to this one
     * 
     * @param totalSoldAndQuantity
     *            The new object to add
     */
    public void add(TotalSoldAndQuantity totalSoldAndQuantity) {
        addTotalSold(totalSoldAndQuantity.getTotalSoldNet());
        addTotalQuantity(totalSoldAndQuantity.getTotalQuantity());
        addTotalVat(totalSoldAndQuantity.getTotalVat());
        addTotalRebate(totalSoldAndQuantity.getTotalRebate());
        addTotalShipping(totalSoldAndQuantity.getTotalShipping());
        addTotalSoldGross(totalSoldAndQuantity.getTotalSoldGross());
    }

    /**
     * @return the dataUtils
     */
    public DataUtils getDataUtils() {
        if (dataUtils == null) {
            dataUtils = DataUtils.getInstance();
        }
        return dataUtils;
    }

    public MonetaryAmount getTotalVat() {
        return totalVat;
    }

    public MonetaryAmount getTotalRebate() {
        return totalRebate;
    }

    public MonetaryAmount getTotalSoldGross() {
        return totalSoldGross;
    }

    public MonetaryAmount getTotalShipping() {
        return totalShipping;
    }
}
