/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable.contacts;

import java.util.List;

import ca.odell.glazedlists.TextFilterator;

import com.sebulli.fakturama.model.Contact;

/**
 *
 */
public class ContactFilterator implements TextFilterator<Contact> {

    /**
     * Which elements are relevant for filtering?
     */
    @Override
    public void getFilterStrings(List<String> baseList, Contact element) {
/*
        searchColumns[0] = "nr";
        searchColumns[1] = "firstname";
        searchColumns[2] = "name";
        searchColumns[3] = "company";
        searchColumns[4] = "zip";
        searchColumns[5] = "city";
 */
        baseList.add(element.getCustomerNumber());
        baseList.add(element.getFirstName());
        baseList.add(element.getName());
        baseList.add(element.getCompany());
        baseList.add(element.getAddress().getZip());
        baseList.add(element.getAddress().getCity());
    }

}
