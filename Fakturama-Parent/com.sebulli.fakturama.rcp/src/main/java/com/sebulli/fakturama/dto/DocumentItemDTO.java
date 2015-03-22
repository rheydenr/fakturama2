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

import com.sebulli.fakturama.model.DocumentItem;

/**
 * Container object for {@link DocumentItem}s. This container contains a {@link DocumentItem}
 * and a {@link Price} object. It is used in the {@link com.sebulli.fakturama.parts.itemlist.DocumentItemListTable} for
 * holding the displayed values. 
 *
 */
public class DocumentItemDTO {
    private DocumentItem documentItem;
    private Price price;
    
    /**
     * Creates a new DTO based on a given {@link DocumentItem}.
     * 
     * @param documentItem
     */
    public DocumentItemDTO(DocumentItem documentItem) {
        this.documentItem = documentItem;
        this.price = new Price(documentItem);
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
        return price;
    }
    

}
