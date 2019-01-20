package com.sebulli.fakturama.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.ItemAccountType_;
import com.sebulli.fakturama.model.ItemListTypeCategory;

@Creatable
public class ItemListTypeCategoriesDAO extends AbstractCategoriesDAO<ItemListTypeCategory> {
    
    protected Class<ItemListTypeCategory> getEntityClass() {
    	return ItemListTypeCategory.class;
    }
    
    
    @Override
    protected void updateObsoleteEntities(ItemListTypeCategory oldCat) {
		// at first update all (deleted) entries in this category and set the category entry to null
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaUpdate<ItemAccountType> updateItemAccountTypes = getEntityManager().getCriteriaBuilder().createCriteriaUpdate(ItemAccountType.class);
        Root<ItemAccountType> root = updateItemAccountTypes.from(ItemAccountType.class);	        
		updateItemAccountTypes.set(root.get(ItemAccountType_.category),(ItemListTypeCategory) null);
		updateItemAccountTypes.where(cb.and(
				cb.equal(root.get(ItemAccountType_.category), oldCat),
				cb.isTrue(root.get(ItemAccountType_.deleted))));
		getEntityManager().createQuery(updateItemAccountTypes).executeUpdate();
    }

}
