/**
 * 
 */
package com.sebulli.fakturama.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.AbstractCategory;

/**
 *
 */
public abstract class AbstractCategoriesDAO<T extends AbstractCategory> extends AbstractDAO<T> {
    
    /**
     * Finds a Category by its name. Category in this case is a String separated by 
     * slashes, e.g. "/fooCat/barCat". Searching starts with the rightmost value
     * and then check the parent. 
     * 
     * @param pCategory the Category to search
     * @return Category
     */
    public T findContactCategoryByName(String pCategory) {
        T result = null;
        if(StringUtils.isNotEmpty(pCategory)) {
        	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        	CriteriaQuery<T> cq = cb.createQuery(getEntityClass());
        	Root<T> rootEntity = cq.from(getEntityClass());
        	// extract the rightmost value
            String[] splittedCategories = pCategory.split("/");
        	String leafCategory = splittedCategories[splittedCategories.length - 1];       	
    		CriteriaQuery<T> selectQuery = cq.select(rootEntity)
    		        .where(cb.and(
        		                cb.equal(rootEntity.<String> get("name"), leafCategory) /*,
        		                cb.equal(rootEntity.get(ContactCategory_.parent), getEntityClass())
        		               ,
        		                cb.equal(rootEntity.get(ContactCategory_.deleted), false)*/));
            try {
                List<T> tmpResultList = getEntityManager().createQuery(selectQuery).getResultList();
                // remove leading slash
                String testCat = StringUtils.removeStart(pCategory, "/");
                for (T contactCategoryEntry : tmpResultList) {
                    if(StringUtils.equals(CommonConverter.getCategoryName(contactCategoryEntry, ""), testCat)) {
                        result = contactCategoryEntry;
                        break;
                    }
                }
            }
            catch (NoResultException nre) {
                // no result means we return a null value 
            }
        }
        return result;
    }

}
