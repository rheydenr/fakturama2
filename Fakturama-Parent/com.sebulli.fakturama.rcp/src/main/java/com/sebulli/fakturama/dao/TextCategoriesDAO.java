package com.sebulli.fakturama.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.TextModule_;

@Creatable
public class TextCategoriesDAO extends AbstractCategoriesDAO<TextCategory> {

    protected Class<TextCategory> getEntityClass() {
    	return TextCategory.class;
    }
    
    @Override
    protected void updateObsoleteEntities(TextCategory oldCat) {
		// at first update all (deleted) entries in this category and set the category entry to null
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaUpdate<TextModule> updateTextModules = getEntityManager().getCriteriaBuilder().createCriteriaUpdate(TextModule.class);
        Root<TextModule> root = updateTextModules.from(TextModule.class);	        
		updateTextModules.set(root.get(TextModule_.categories),(TextCategory) null);
		updateTextModules.where(cb.and(
				cb.equal(root.get(TextModule_.categories), oldCat),
				cb.isTrue(root.get(TextModule_.deleted))));
		getEntityManager().createQuery(updateTextModules).executeUpdate();
    }
  
}
