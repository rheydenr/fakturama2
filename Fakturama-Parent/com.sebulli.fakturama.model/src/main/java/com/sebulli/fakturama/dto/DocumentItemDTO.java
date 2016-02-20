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

import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.IEntity;

/**
 * Container object for {@link DocumentItem}s. This container contains a {@link DocumentItem}
 * and a {@link Price} object. It is used in the {@link com.sebulli.fakturama.parts.itemlist.DocumentItemListTable} for
 * holding the displayed values. 
 *
 */
public class DocumentItemDTO implements IEntity {
    private DocumentItem documentItem;
//    private Price price;
    
    /**
     * Creates a new DTO based on a given {@link DocumentItem}.
     * 
     * @param documentItem
     */
    public DocumentItemDTO(DocumentItem documentItem) {
        this.documentItem = documentItem;
//        this.price = new Price(documentItem);
    }

    /**
     * @return the documentItem
     */
    public DocumentItem getDocumentItem() {
        return documentItem;
    }

    /**
     * @return the price
     */
    public Price getPrice() {
        return new Price(documentItem);
    }

    @Override
    public String getName() {
        return documentItem.getName();
    }

    @Override
    public void setName(String newName) {
        documentItem.setName(newName);
    }

    @Override
    public Date getDateAdded() {
        return documentItem.getDateAdded();
    }

    @Override
    public void setDateAdded(Date newDateAdded) {
        documentItem.setDateAdded(newDateAdded);
    }

    @Override
    public String getModifiedBy() {
        return documentItem.getModifiedBy();
    }

    @Override
    public void setModifiedBy(String newModifiedBy) {
        documentItem.setModifiedBy(newModifiedBy);
    }

    @Override
    public Date getModified() {
        return documentItem.getModified();
    }

    @Override
    public void setModified(Date newModified) {
        documentItem.setModified(newModified);
    }

    @Override
    public long getId() {
        return documentItem.getId();
    }

    @Override
    public void setId(long newId) {
        throw new UnsupportedOperationException("object is only a wrapper object!");
    }

    @Override
    public Boolean getDeleted() {
        return documentItem.getDeleted();
    }

    @Override
    public void setDeleted(Boolean newDeleted) {
        documentItem.setDeleted(newDeleted);
    }

	@Override
	public Date getValidFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValidFrom(Date newValidFrom) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Date getValidTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValidTo(Date newValidTo) {
		// TODO Auto-generated method stub
		
	}
}
