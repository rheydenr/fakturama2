/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.parts.widget.labelprovider;

import org.eclipse.jface.viewers.LabelProvider;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.ShippingVatType;

/**
 *
 */
public class ShippingVatTypeLabelProvider extends LabelProvider {
    
    private Messages msg;
    
    /**
     * @param msg
     */
    public ShippingVatTypeLabelProvider(Messages msg) {
        this.msg = msg;
    }

    @Override
    public String getText(Object element) {
        String retval = "";
        if(element instanceof ShippingVatType) {
            ShippingVatType type = (ShippingVatType)element;
            switch (type) {
            case SHIPPINGVATFIX:
                retval = msg.editorShippingFieldAutovatConstantName;
                break;
            case SHIPPINGVATGROSS:
                retval = msg.editorShippingFieldAutovatFromvalueGross;
                break;
            case SHIPPINGVATNET:
                retval = msg.editorShippingFieldAutovatFromvalueNet;
                break;
            default:
                retval = "invalid";
                break;
            }
        }
        return retval;
    }
}
