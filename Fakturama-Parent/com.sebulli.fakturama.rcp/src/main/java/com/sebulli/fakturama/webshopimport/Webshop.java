/* Fakturama - Free Invoicing Software - https://www.fakturama.info
 * 
 * Copyright (C) 2021 www.fakturama.info
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation */

package com.sebulli.fakturama.webshopimport;

/**
 * Types of web shops.
 */
public enum Webshop {

    SHOPWARE_V5("Shopware v5"), 
    OXID_ESHOP_V6("OXID eShop v6"), 
    MAGENTO_V3("Magento Shop v3"),

    /**
     * This is the connector for the former PHP webshop connector
     */
    LEGACY_WEBSHOP("Legacy PHP connector");

    private String label;

    private Webshop(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
