package com.sebulli.fakturama.dao;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.ShippingCategory;

@Creatable
public class ShippingCategoriesDAO extends AbstractCategoriesDAO<ShippingCategory> {

    protected Class<ShippingCategory> getEntityClass() {
    	return ShippingCategory.class;
    }
    
}
