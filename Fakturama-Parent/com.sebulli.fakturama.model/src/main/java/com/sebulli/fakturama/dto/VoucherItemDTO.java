/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.dto;

import java.util.Date;

import com.sebulli.fakturama.model.VoucherItem;

/**
 * Container object for link {@link VoucherItem}s
 */
public class VoucherItemDTO extends VoucherItem {
    
    /**
     * 
     */
    private static final long serialVersionUID = -5800206738482783749L;
    private VoucherItem voucherItem;
    
    /**
     * @param expenditureItem
     */
    public VoucherItemDTO(VoucherItem expenditureItem) {
        this.setVoucherItem(expenditureItem);
    }
    
    /**
     * @return the expenditureItem
     */
    public VoucherItem getVoucherItem() {
        return voucherItem;
    }

    /**
     * @param expenditureItem the expenditureItem to set
     */
    public void setVoucherItem(VoucherItem expenditureItem) {
        this.voucherItem = expenditureItem;
    }

    @Override
    public String getName() {
        return voucherItem.getName();
    }

    @Override
    public void setName(String newName) {
        voucherItem.setName(newName);
    }

    @Override
    public Date getDateAdded() {
        return voucherItem.getDateAdded();
    }

    @Override
    public void setDateAdded(Date newDateAdded) {
        voucherItem.setDateAdded(newDateAdded);
    }

    @Override
    public String getModifiedBy() {
        return voucherItem.getModifiedBy();
    }

    @Override
    public void setModifiedBy(String newModifiedBy) {
        voucherItem.setModifiedBy(newModifiedBy);
    }

    @Override
    public Date getModified() {
        return voucherItem.getModified();
    }

    @Override
    public void setModified(Date newModified) {
        voucherItem.setModified(newModified);
    }

    @Override
    public long getId() {
        return voucherItem.getId();
    }

    @Override
    public void setId(long newId) {
        throw new UnsupportedOperationException("object is only a wrapper object!");
    }

    @Override
    public Boolean getDeleted() {
        return voucherItem.getDeleted();
    }

    @Override
    public void setDeleted(Boolean newDeleted) {
        voucherItem.setDeleted(newDeleted);
    }
}
