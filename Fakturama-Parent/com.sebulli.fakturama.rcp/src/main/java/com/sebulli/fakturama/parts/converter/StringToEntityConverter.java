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

import java.util.List;

import org.eclipse.core.databinding.conversion.Converter;

import com.sebulli.fakturama.model.IEntity;

/**
 * @author rheydenr
 *
 */
public class StringToEntityConverter<T extends IEntity> extends Converter {

    private final List<T> categories;
    
    public StringToEntityConverter(List<T> categories, Class<T> clazz) {
        super(String.class, clazz);
        this.categories = categories;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public Object convert(Object fromObject) {
        // in: "Umsatzsteuer"
        // out: VAT
        // TODO Look for a better approach! ==> ComboBoxLabelProvider??
        String searchString = (String)fromObject;
        for (T category : categories) {
            if(category.getName().equals(searchString)) {
                return category;
            }
        }
        return null;
    }

}
