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
 
package com.sebulli.fakturama.parts.widget.labelprovider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;

import com.sebulli.fakturama.model.IDescribableEntity;
import com.sebulli.fakturama.model.IEntity;

/**
 *
 */
public class EntityLabelProvider extends LabelProvider {

    /**
     * Returns the <code>String</code> that maps to the given 
     * <code>Entity</code>.
     * 
     * @param element an {@link IEntity} object
     * @return a <code>String</code> from the provided values array, or the 
     * empty <code>String</code> 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        String retval = "";
        if (element != null) {
            if(element instanceof IDescribableEntity) {
                retval = ((IDescribableEntity)element).getDescription();
            } 

            if (element instanceof IEntity || StringUtils.isBlank(retval)) {
                retval = ((IEntity)element).getName();
            }
        }
        return retval;
    }
}
