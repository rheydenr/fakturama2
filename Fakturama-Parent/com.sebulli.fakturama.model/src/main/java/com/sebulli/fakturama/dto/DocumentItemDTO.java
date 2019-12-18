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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.IEntity;

/**
 * Container object for {@link DocumentItem}s. This container contains a
 * {@link DocumentItem} and a {@link Price} object. It is used in the
 * {@link com.sebulli.fakturama.parts.itemlist.DocumentItemListTable} for
 * holding the displayed values.
 *
 */
public class DocumentItemDTO implements IEntity {
	private DocumentItem documentItem;
	private boolean dirty;
	private List<String> ignoredAttributes = new ArrayList<>();

	/**
	 * Creates a new DTO based on a given {@link DocumentItem}.
	 * 
	 * @param documentItem
	 */
	public DocumentItemDTO(DocumentItem documentItem) {
		this.documentItem = documentItem;
		ignoredAttributes.add("dateAdded");
		ignoredAttributes.add("validFrom");
		ignoredAttributes.add("modifiedBy");
		ignoredAttributes.add("originQuantity");
		this.documentItem.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (!ignoredAttributes.contains(evt.getPropertyName())) {
//					System.out.format("changed [%s] from [%s] to [%s]%n", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
					dirty = true;
//				} else {
//					System.out.format("ignored [%s]%n", evt.getPropertyName());
				}
			}
		});
	}

	public boolean isDocumentItemDirty() {
		return dirty;
	}

	public void setDocumentItemDirty(boolean dirtyState) {
		this.dirty = dirtyState;
	}

	/**
	 * @return the documentItem
	 */
	public DocumentItem getDocumentItem() {
		return documentItem;
	}
	
	public Double getWeight() {
		return documentItem != null && documentItem.getWeight() != null ? documentItem.getWeight() : Double.valueOf(0.0);
	}

	/**
	 * @return the price
	 */
	public Price getPrice() {
		return getPrice(false);
	}

	/**
	 * 
	 * @param useSET use sales equalization tax, if any
	 * @return the price
	 */
	public Price getPrice(boolean useSET) {
		return new Price(documentItem, 1.0, useSET);
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
		return documentItem.getValidFrom();
	}

	@Override
	public void setValidFrom(Date newValidFrom) {
		documentItem.setValidFrom(newValidFrom);
	}

	@Override
	public Date getValidTo() {
		return documentItem.getValidTo();
	}

	@Override
	public void setValidTo(Date newValidTo) {
		documentItem.setValidTo(newValidTo);
	}
}
