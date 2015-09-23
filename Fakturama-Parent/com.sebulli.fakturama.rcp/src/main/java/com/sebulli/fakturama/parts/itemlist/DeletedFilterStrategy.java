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

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;

import com.sebulli.fakturama.model.IEntity;

/**
 *
 */
public class DeletedFilterStrategy implements IFilterStrategy<IEntity> {

    @Override
    public void applyFilter(Map<Integer, Object> filterIndexToObjectMap) {
        if (filterIndexToObjectMap.isEmpty()) {
            return;
        }
        
    }

}
