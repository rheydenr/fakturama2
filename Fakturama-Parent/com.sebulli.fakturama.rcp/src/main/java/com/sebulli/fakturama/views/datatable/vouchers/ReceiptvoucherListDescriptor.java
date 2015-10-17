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
 
package com.sebulli.fakturama.views.datatable.vouchers;

import com.sebulli.fakturama.model.AbstractVoucher_;

/**
 * Enum for describing a Voucher list. This contains the name of the displayed values, the position of
 * the columns and a default width of each column (copied from old ColumnWidth*PreferencePages). 
 *
 */
public enum ReceiptvoucherListDescriptor {
    
    DONOTBOOK(AbstractVoucher_.doNotBook.getName(), null, 0, 20),
    DATE(AbstractVoucher_.voucherDate.getName(), "common.field.date", 1, 80),
    VOUCHER(AbstractVoucher_.voucherNumber.getName(), "receiptvoucher.field.voucher", 2, 100),
    DOCUMENT(AbstractVoucher_.documentNumber.getName(), "partdesc.docview", 3, 150),
    CUSTOMER(AbstractVoucher_.name.getName(), "receiptvoucher.field.customer", 4, 200),
    TOTAL(AbstractVoucher_.totalValue.getName(), "common.field.total", 5, 80)
    ;

    private String propertyName, messageKey;
    private int position, defaultWidth;
    
    /**
     * @param propertyName
     * @param position
     * @param defaultWidth
     */
    private ReceiptvoucherListDescriptor(String propertyName, String messageKey, int position, int defaultWidth) {
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

    public static ReceiptvoucherListDescriptor getDescriptorFromColumn(int columnIndex) {
        for (ReceiptvoucherListDescriptor descriptor : values()) {
            if(descriptor.getPosition() == columnIndex) {
                return descriptor;
            }
        }
        return null;
    }

    public static final String[] getVoucherPropertyNames() {
        return new String[]{
        ReceiptvoucherListDescriptor.DONOTBOOK.getPropertyName(), 
        ReceiptvoucherListDescriptor.DATE.getPropertyName(), 
        ReceiptvoucherListDescriptor.VOUCHER.getPropertyName(), 
        ReceiptvoucherListDescriptor.DOCUMENT.getPropertyName(),
        ReceiptvoucherListDescriptor.CUSTOMER.getPropertyName(),
        ReceiptvoucherListDescriptor.TOTAL.getPropertyName(),};
    }

}
