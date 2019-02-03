/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.parts.itemlist;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

import com.sebulli.fakturama.dao.ItemListTypeCategoriesDAO;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.ItemListTypeCategory;

/**
 * Converter for displaying {@link ItemAccountType} values in a combo box inside a NatTable.
 */
public class ItemAccountTypeDisplayConverter extends DisplayConverter {
	
	@Inject
	private ItemListTypeCategoriesDAO itemListTypeCategoriesDAO;
	private ItemListTypeCategory accountCat;
	
	@PostConstruct
	public void initialize() {
		accountCat = itemListTypeCategoriesDAO.findCategoryByName(Constants.ACCOUNT_LIST_CATEGORY);
	}

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#canonicalToDisplayValue(java.lang.Object)
     */
    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        String retval = "";
        ItemAccountType itemAccountType = (ItemAccountType) canonicalValue;
        if (itemAccountType != null) {
            retval = itemAccountType.getName();
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#displayToCanonicalValue(java.lang.Object)
     */
    @Override
    public Object displayToCanonicalValue(Object displayValue) {
    	ItemAccountType accType = FakturamaModelPackage.MODELFACTORY.createItemAccountType();
    	accType.setCategory(accountCat);
    	accType.setName((String) displayValue);
        return accType;
    }

}
