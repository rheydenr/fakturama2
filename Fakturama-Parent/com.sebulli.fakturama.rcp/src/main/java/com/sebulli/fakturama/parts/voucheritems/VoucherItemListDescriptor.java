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
 
package com.sebulli.fakturama.parts.voucheritems;


/**
 *
 */
public enum VoucherItemListDescriptor {
    
    // enums with empty property name have no counterparts in database
    // (these are calculated values)
//    POSITION("id", "", 0, 55), // $Row
    TEXT("name", "common.field.text", 0, 200),
    ACCOUNTTYPE("accountType", "exporter.data.accounttype", 1, 55),
    VAT("vat", "common.field.vat", 2, 30), // $ItemVatPercent
    DISCOUNT("itemRebate", "common.field.discount", 3, 20),
    PRICE("price", "product.data.net", 4, 20),
    TOTAL("$VoucherItemGrossPrice", "product.data.gross", 5, 70)
    ;

    private String propertyName, messageKey;
    private int position, defaultWidth;
    
    /**
     * @param propertyName
     * @param messageKey
     * @param position
     * @param defaultWidth
     */
    private VoucherItemListDescriptor(String propertyName, String messageKey, int position, int defaultWidth) {
        this.propertyName = propertyName;
        this.messageKey = messageKey;
        this.position = position;
        this.defaultWidth = defaultWidth;
    }

    /**
     * @return the propertyName
     */
    public final String getPropertyName() {
        return propertyName;
    }

    /**
     * @return the position
     */
    public final int getPosition() {
        return position;
    }

    /**
     * @return the defaultWidth
     */
    public final int getDefaultWidth() {
        return defaultWidth;
    }
    
    /**
     * @return the messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }
    
    public static final String[] getVoucherItemPropertyNames() {
        return new String[]{
//                VoucherItemListDescriptor.POSITION.getPropertyName(),
                VoucherItemListDescriptor.TEXT.getPropertyName(),
                VoucherItemListDescriptor.ACCOUNTTYPE.getPropertyName(),
                VoucherItemListDescriptor.VAT.getPropertyName(),
                VoucherItemListDescriptor.PRICE.getPropertyName()
        };
    }
}
