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
 
package com.sebulli.fakturama.parts.itemlist;



/**
 *
 */
public enum DocumentItemListDescriptor {
    // enums with empty property name have no counterparts in database
    // (these are calculated values)
    POSITION("posNr", "editor.document.field.position", 0, 55), // $Row
    OPTIONAL("optional", "common.field.optional", 1, 55),          // $Optional
    QUANTITY("quantity", "common.field.quantity", 2, 55),
    QUNIT("quantityUnit", "editor.document.field.qunit", 3, 70),
    ITEMNUMBER("itemNumber", "product.field.itemno", 4, 20),
    PICTURE("pictureName", "common.field.picture", 5, 50), // $ProductPictureSmall
    NAME("name", "common.field.name", 6, 120),
    DESCRIPTION("description", "common.field.description", 7, 200),
    VAT("itemVat", "common.field.vat", 8, 30), // $ItemVatPercent
    UNITPRICE("price", "common.field.unitprice", 9, 30), // was $ItemGrossPrice if $useGross is set, else "price"
    DISCOUNT("itemRebate", "common.field.discount", 10, 20),
    TOTALPRICE("", "common.field.price", 11, 20)  // was $ItemGrossTotal if $useGross is set, else $ItemNetTotal
    ;

    private String propertyName, messageKey;
    private int position, defaultWidth;
    
    /**
     * @param propertyName
     * @param position
     * @param defaultWidth
     */
    private DocumentItemListDescriptor(String propertyName, String messageKey, int position, int defaultWidth) {
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
//
//    public static DocumentItemListDescriptor getDescriptorFromColumn(int columnIndex) {
//        for (DocumentItemListDescriptor descriptor : values()) {
//            if(descriptor.getPosition() == columnIndex) {
//                return descriptor;
//            }
//        }
//        return null;
//    }
    
    public static final String[] getDocumentItemPropertyNames() {
//        Arrays.stream(DocumentItemListDescriptor.values()).(d -> d.getPropertyName()).collect(Collectors.toList());
        return new String[]{
        DocumentItemListDescriptor.POSITION.getPropertyName(), 
        DocumentItemListDescriptor.OPTIONAL.getPropertyName(), 
        DocumentItemListDescriptor.QUANTITY.getPropertyName(), 
        DocumentItemListDescriptor.QUNIT.getPropertyName(), 
        DocumentItemListDescriptor.ITEMNUMBER.getPropertyName(), 
        DocumentItemListDescriptor.PICTURE.getPropertyName(), 
        DocumentItemListDescriptor.NAME.getPropertyName(), 
        DocumentItemListDescriptor.DESCRIPTION.getPropertyName(), 
        DocumentItemListDescriptor.VAT.getPropertyName(), 
        DocumentItemListDescriptor.UNITPRICE.getPropertyName(), 
        DocumentItemListDescriptor.DISCOUNT.getPropertyName(), 
        DocumentItemListDescriptor.TOTALPRICE.getPropertyName()};
    }

}
