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
 
package com.sebulli.fakturama.model;

import com.sebulli.fakturama.misc.DocumentType;

/**
 * Used for displaying the "category" tree of the document list view.
 *
 */
public class DummyStringCategory extends AbstractCategory {
    
    private DocumentType docType;
    
    /**
     * 
     */
    private static final long serialVersionUID = 7755323170063298175L;

    public DummyStringCategory() {
        super();
    }
    
    public DummyStringCategory(String name, DocumentType docType) {
        super();
        this.setName(name);
        this.docType = docType;
    }

    /**
     * @return the docType
     */
    public DocumentType getDocType() {
        return docType;
    }

    /**
     * @param docType the docType to set
     */
    public void setDocType(DocumentType docType) {
        this.docType = docType;
    }

}
