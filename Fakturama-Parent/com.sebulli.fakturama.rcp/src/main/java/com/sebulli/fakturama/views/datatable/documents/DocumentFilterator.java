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
 
package com.sebulli.fakturama.views.datatable.documents;

import java.util.List;

import ca.odell.glazedlists.TextFilterator;

import com.sebulli.fakturama.model.Document;

/**
 *
 */
public class DocumentFilterator implements TextFilterator<Document> {

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.TextFilterator#getFilterStrings(java.util.List, java.lang.Object)
     */
    @Override
    public void getFilterStrings(List<String> baseList, Document element) {
/*
        // Mark the columns that are used by the search function.
        searchColumns = new String[4];
        searchColumns[0] = "name";
        searchColumns[1] = "date";
        searchColumns[2] = "addressfirstline";
        searchColumns[3] = "total";
 */
        baseList.add(element.getName());
//        baseList.add(element.getDocumentDate());
        baseList.add(element.getAddressFirstLine());
//        baseList.add(element.getTotalValue());
    }

}
