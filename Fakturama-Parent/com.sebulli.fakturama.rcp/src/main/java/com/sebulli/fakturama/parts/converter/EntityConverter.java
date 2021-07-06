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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.conversion.Converter;

import com.sebulli.fakturama.model.IDescribableEntity;
import com.sebulli.fakturama.model.IEntity;

/**
 * Common Converter for {@link IEntity}.
 */
public class EntityConverter<T extends IEntity> extends Converter<T, String> {

    private Class<T> type;
    
    public EntityConverter(Class<T> type) {
        super(type, String.class);
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public String convert(IEntity fromObject) {
        String result = null;
        if(type.equals(getFromType()) && fromObject != null) {
            if(fromObject instanceof IDescribableEntity) {
                result = StringUtils.defaultString(((IDescribableEntity)fromObject).getDescription(), fromObject.getName());
            } else {
                result = fromObject.getName();
            }
        }
        return result;
    }
}
