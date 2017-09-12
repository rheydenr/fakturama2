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
 
package com.sebulli.fakturama.parts.converter;

import java.util.TreeSet;

import org.eclipse.core.databinding.conversion.Converter;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.AbstractCategory;

/**
 * @author rheydenr
 *
 */
public class MessageKeyToCategoryConverter<T extends AbstractCategory> extends Converter {

    private final Messages msg;
    private final TreeSet<T> categories;
    
    public MessageKeyToCategoryConverter(TreeSet<T> categories, Class<T> clazz, Messages msg) {
        super(String.class, clazz);
        this.categories = categories;
        this.msg = msg;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public Object convert(Object fromObject) {
        // in: "Umsatzsteuer"
        // out: VATCategory
        // TODO Look for a better approach! ==> ComboBoxLabelProvider??
        String searchString = (String)fromObject;
        for (T category : categories) {
            if(msg.getMessageFromKey(category.getName()).equals(searchString)) {
                return category;
            }
        }
        return null;
    }
}
