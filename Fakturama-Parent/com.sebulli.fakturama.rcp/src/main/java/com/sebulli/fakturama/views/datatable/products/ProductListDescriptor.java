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
 
package com.sebulli.fakturama.views.datatable.products;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum for describing a product list. This contains the name of the displayed values, the position of
 * the columns and a default width of each column (copied from old ColumnWidth*PreferencePages). 
 *
 */
public enum ProductListDescriptor {
    ITEMNO("itemNumber", "product.field.itemno", 0, 55),
    NAME("name", "common.field.name", 1, 120),
    DESCRIPTION("description", "common.field.description", 2, 200),
    SUPPNO("supplierItemNumber", "editor.product.field.supplier.itemnumber", 3, 55), // GS/ add supplier art nbr
    QUANTITY("quantity", "common.field.quantity", 4, 70),
    PRICE("price1", "common.field.price", 5, 70),  // but sometimes it's the gross price!!!
    VAT("vat", "common.field.vat", 6, 70)
    ;

    private String propertyName, messageKey;
    private int position, defaultWidth;
    
    /**
     * @param propertyName
     * @param position
     * @param defaultWidth
     */
    private ProductListDescriptor(String propertyName, String messageKey, int position, int defaultWidth) {
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

    public static ProductListDescriptor getDescriptorFromColumn(int columnIndex) {
        for (ProductListDescriptor descriptor : values()) {
            if(descriptor.getPosition() == columnIndex) {
                return descriptor;
            }
        }
        return null;
    }
    
    public static final Optional<ProductListDescriptor> getDescriptorForProperty(String propertyName) {
    	return Arrays.stream(values()).filter(descriptor -> StringUtils.equalsIgnoreCase(descriptor.getPropertyName(), propertyName)).findFirst();
    }

    public static final String[] getProductPropertyNames() {
        return new String[]{
        ProductListDescriptor.ITEMNO.getPropertyName(),
        ProductListDescriptor.NAME.getPropertyName(), 
        ProductListDescriptor.DESCRIPTION.getPropertyName(), 
        ProductListDescriptor.SUPPNO.getPropertyName(),
        ProductListDescriptor.QUANTITY.getPropertyName(),
        ProductListDescriptor.PRICE.getPropertyName(),
        ProductListDescriptor.VAT.getPropertyName()
        };
    }

}
