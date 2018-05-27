package com.sebulli.fakturama.dao;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.persistence.config.BatchWriting;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.VATCategory_;
import com.sebulli.fakturama.model.VAT_;

@Creatable
public class VatCategoriesDAO extends AbstractCategoriesDAO<VATCategory> {

    protected Class<VATCategory> getEntityClass() {
    	return VATCategory.class;
    }
//    
//    /**
//     * Get all {@link VATCategory}s from Database.
//     *
//     * @return List<VATCategory> 
//     */
//    public List<VATCategory> findAll() {
//    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
//    	CriteriaQuery<VATCategory> cq = cb.createQuery(VATCategory.class);
//    	CriteriaQuery<VATCategory> selectQuery = cq.select(cq.from(VATCategory.class));
//    	return getEntityManager().createQuery(selectQuery).getResultList();
////    	return getEntityManager().createQuery("select p from VATCategory p", VATCategory.class).getResultList();
//    }
//
//	/**
//	 * Find a {@link VATCategory} by its name. If one of the part categories doesn't exist we create it 
//	 * (if {@code withPersistOption} is set to <code>true</code>).
//	 * 
//	 * @param testCat the category to find
//	 * @param withPersistOption persist a (part) category if it doesn't exist
//	 * @return found category
//	 */
//    public VATCategory getOrCreateCategory(String testCat, boolean withPersistOption) {
//        // to find the complete category we have to start with the topmost category
//        // and then lookup each of the child categories in the given path
//        String[] splittedCategories = testCat.split("/");
//        VATCategory parentCategory = null;
//        String category = "";
//        try {
//            for (int i = 0; i < splittedCategories.length; i++) {
//            	if(StringUtils.isBlank(splittedCategories[i])) {
//            		continue;
//            	}
//                category += "/" + splittedCategories[i];
//                VATCategory searchCat = findCategoryByName(category);
//                if (searchCat == null) {
//                    // not found? Then create a new one.
//                    VATCategory newCategory = modelFactory.createVATCategory();
//                    newCategory.setName(splittedCategories[i]);
//                    newCategory.setParent(parentCategory);
//                    newCategory = save(newCategory);
//                    searchCat = newCategory;
//                }
//                // save the parent and then dive deeper...
//                parentCategory = searchCat;
//            }
//        }
//        catch (FakturamaStoringException e) {
//            getLog().error(e);
//        }
//        return parentCategory;
//    }
}
