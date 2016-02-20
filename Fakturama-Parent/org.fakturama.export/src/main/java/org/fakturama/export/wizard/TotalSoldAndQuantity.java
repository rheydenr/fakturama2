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
package org.fakturama.export.wizard;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.RoundedMoney;

import com.sebulli.fakturama.misc.DataUtils;

/**
 * This class contains the total sum of volume and quantity
 * 
 * @author Gerd Bartelt
 */
public class TotalSoldAndQuantity {
	
	private MonetaryAmount totalSold = RoundedMoney.of(Double.valueOf(0.0), getDataUtils().getDefaultCurrencyUnit());
	private Double totalQuantity = Double.valueOf(0.0);
	
	private DataUtils dataUtils;

	/**
	 * Returns the total sum
	 * 
	 * @return
	 * 			The total sum
	 */
	public MonetaryAmount getTotalSold() {
		return totalSold;
	}

	/**
	 * Sets the total sum
	 * 
	 * @param totalSold
	 * 			The new value of the total sum
	 */
	public void setTotalSold(MonetaryAmount totalSold) {
		this.totalSold = totalSold;
	}
	
	/**
	 * Add a new value to the total sum
	 * 
	 * @param totalSold
	 * 			The new value to add
	 */
	public void addTotalSold(MonetaryAmount totalSold) {
		setTotalSold(this.totalSold.add(totalSold));
	}
	
	/**
	 * Returns the total quantity
	 * 
	 * @return
	 * 			The total quantity
	 */
	public Double getTotalQuantity() {
		return totalQuantity;
	}
	
	/**
	 * Sets the total quantity
	 * 
	 * @param totalQuantity
	 * 			The new value of the total quantity
	 */
	public void setTotalQuantity(Double totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
	
	/**
	 * Add a new value to the total sum
	 * 
	 * @param totalQuantity
	 * 			The new value to add
	 */
	public void addTotalQuantity(Double totalQuantity) {
		this.totalQuantity += totalQuantity;
	}

	/**
	 * Add a new object to this one
	 * 
	 * @param totalSoldAndQuantity
	 * 			The new object to add
	 */
	public void add (TotalSoldAndQuantity totalSoldAndQuantity) {
		addTotalSold(totalSoldAndQuantity.getTotalSold());
		addTotalQuantity(totalSoldAndQuantity.getTotalQuantity());
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
}
