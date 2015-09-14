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

import com.sebulli.fakturama.model.ExpenditureItem;
import com.sebulli.fakturama.model.IEntity;

/**
 * Container object for {@link ExpenditureItem}s
 */
public class VoucherItemDTO implements IEntity {
    
    private ExpenditureItem expenditureItem;
    
    /**
     * @param expenditureItem
     */
    public VoucherItemDTO(ExpenditureItem expenditureItem) {
        this.setExpenditureItem(expenditureItem);
    }
    
    /**
     * @return the expenditureItem
     */
    public ExpenditureItem getExpenditureItem() {
        return expenditureItem;
    }

    /**
     * @param expenditureItem the expenditureItem to set
     */
    public void setExpenditureItem(ExpenditureItem expenditureItem) {
        this.expenditureItem = expenditureItem;
    }



    @Override
    public String getName() {
        return expenditureItem.getName();
    }

    @Override
    public void setName(String newName) {
        expenditureItem.setName(newName);
    }

    @Override
    public Date getDateAdded() {
        return expenditureItem.getDateAdded();
    }

    @Override
    public void setDateAdded(Date newDateAdded) {
        expenditureItem.setDateAdded(newDateAdded);
    }

    @Override
    public String getModifiedBy() {
        return expenditureItem.getModifiedBy();
    }

    @Override
    public void setModifiedBy(String newModifiedBy) {
        expenditureItem.setModifiedBy(newModifiedBy);
    }

    @Override
    public Date getModified() {
        return expenditureItem.getModified();
    }

    @Override
    public void setModified(Date newModified) {
        expenditureItem.setModified(newModified);
    }

    @Override
    public long getId() {
        return expenditureItem.getId();
    }

    @Override
    public void setId(long newId) {
        throw new UnsupportedOperationException("object is only a wrapper object!");
    }

    @Override
    public Boolean getDeleted() {
        return expenditureItem.getDeleted();
    }

    @Override
    public void setDeleted(Boolean newDeleted) {
        expenditureItem.setDeleted(newDeleted);
    }

}
