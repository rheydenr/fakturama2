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
 
package com.sebulli.fakturama.views.datatable.payments;

import java.util.List;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.TextFilterator;

import com.sebulli.fakturama.model.Payment;

/**
 * 
 * This {@link Filterator} is for filtering {@link Payment}s in a list view.
 */
public class PaymentFilterator implements TextFilterator<Payment> {

    /**
     * Which elements are relevant for filtering?
     */
    @Override
    public void getFilterStrings(List<String> baseList, Payment element) {
/*
        // Mark the columns that are used by the search function.
        searchColumns = new String[5];
        searchColumns[0] = "name";
        searchColumns[1] = "description";
        searchColumns[2] = "discountvalue";
        searchColumns[3] = "discountdays";
        searchColumns[4] = "netdays";
 */
        baseList.add(element.getName());
        baseList.add(element.getDescription());
//        baseList.add(element.getDiscountValue());
//        baseList.add(element.getDiscountDays());
//        baseList.add(element.getNetDays());
    }

}
