/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package org.fakturama.export.facturx;

import java.util.HashMap;
import java.util.Map;

import javax.money.MonetaryAmount;

import com.sebulli.fakturama.model.VAT;

/**
 *
 */
public class DocumentAllowances {
    private Map<VAT, MonetaryAmount> itemAllowances = new HashMap<>();

    public void add(VAT itemVat, MonetaryAmount itemPrice) {
        if(!itemAllowances.containsKey(itemVat)) {
            itemAllowances.put(itemVat, itemPrice);
        } else {
            itemAllowances.put(itemVat, itemAllowances.get(itemVat).add(itemPrice));
        }
    }

    public Map<VAT, MonetaryAmount> getItemAllowances() {
        return itemAllowances;
    }
}
