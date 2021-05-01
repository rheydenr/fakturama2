/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2019 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
package com.sebulli.fakturama.model;

import java.util.Comparator;

import com.sebulli.fakturama.converter.CommonConverter;

public final class CategoryComparator<T extends AbstractCategory> implements Comparator<T> {
	
	@Override
	public int compare(T cat1, T cat2) {
		// oh no... the names could be equal in different branches,
		// therefore we have to compare with an another attribute
	    
	    return Comparator.comparing(AbstractCategory::getName).thenComparing(c -> CommonConverter.getCategoryName(c, "")).compare(cat1, cat2);
	}
}
