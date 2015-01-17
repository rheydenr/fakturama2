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
 
package com.sebulli.fakturama.views.datatable.shippings;

import com.sebulli.fakturama.model.Shipping_;

/**
 * Enum for describing a product list. This contains the name of the displayed values, the position of
 * the columns and a default width of each column (copied from old ColumnWidth*PreferencePages). 
 *
 */
public enum ShippingListDescriptor {
    DEFAULT("default", "common.label.default", 0, 55),
    NAME(Shipping_.name.getName(), "common.field.name", 1, 120),
    DESCRIPTION(Shipping_.description.getName(), "common.field.description", 2, 200),
    VALUE(Shipping_.shippingValue.getName(), "common.field.value", 3, 70)
    ;

    private String propertyName, messageKey;
    private int position, defaultWidth;
    
    /**
     * @param propertyName
     * @param position
     * @param defaultWidth
     */
    private ShippingListDescriptor(String propertyName, String messageKey, int position, int defaultWidth) {
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

    public static ShippingListDescriptor getDescriptorFromColumn(int columnIndex) {
        for (ShippingListDescriptor descriptor : values()) {
            if(descriptor.getPosition() == columnIndex) {
                return descriptor;
            }
        }
        return null;
    }

    public static final String[] getShippingPropertyNames() {
        return new String[]{
        ShippingListDescriptor.NAME.getPropertyName(), 
        ShippingListDescriptor.DESCRIPTION.getPropertyName(), 
        ShippingListDescriptor.DEFAULT.getPropertyName(),
        ShippingListDescriptor.VALUE.getPropertyName(),
        };
    }

}
