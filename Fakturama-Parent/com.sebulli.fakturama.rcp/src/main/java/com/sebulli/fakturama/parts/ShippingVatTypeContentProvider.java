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
 
package com.sebulli.fakturama.parts;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.sebulli.fakturama.model.ShippingVatType;

/**
 *
 */
public class ShippingVatTypeContentProvider implements IStructuredContentProvider {

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
            return ((List<ShippingVatType>) inputElement).toArray();
        }
        return new Object[0];
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing.
    }

    @Override
    public void dispose() {
        // do nothing.
    }

}
