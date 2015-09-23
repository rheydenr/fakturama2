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
 
package com.sebulli.fakturama.parts.converter;

import com.sebulli.fakturama.model.AbstractCategory;

/**
 * Converter for common conversions (like Category paths and so on) 
 *
 */
public class CommonConverter {
    
    /**
     * determines the "path" of all categories for a given element. Dives recursive into
     * the parent category of each element.
     * 
     * @param element element to process
     * @param leaf current leaf string
     * @return a String representing the category path
     */
    public static String getCategoryName(AbstractCategory element, String leaf) {
        String newLeaf = leaf;
        if (element != null && element.getParent() != null) {
            newLeaf = getCategoryName(element.getParent(), leaf) + '/';
        }
        return newLeaf + (element != null ? element.getName() : "");
    }

}
