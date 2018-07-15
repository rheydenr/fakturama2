/**
 * 
 */
package com.sebulli.fakturama.dao;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.AbstractCategory_;
import com.sebulli.fakturama.model.VATCategory_;

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
    public T findCategoryByName(String pCategory) {
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
        		                cb.equal(rootEntity.get(AbstractCategory_.name), leafCategory),
        		                cb.equal(rootEntity.get(AbstractCategory_.deleted), false)/*));
        		                /*cb.equal(rootEntity.get(ContactCategory_.parent), getEntityClass())
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
    
    /**
     * Tests if a given Category has child categories.
     * 
     * @param category
     * @return
     */
    public boolean hasChildren(T category) {
    	// select * from abstractcategory where parent_id = category.id;
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(getEntityClass());
        Root<T> root = criteria.from(getEntityClass());
        criteria.where(cb.equal(root.get(VATCategory_.parent), category));
        return !getEntityManager().createQuery(criteria).getResultList().isEmpty();
    }

    /**
     * Checks if the given Category can be deleted. This is the case if no reference to it exists and if the category has no children.
     * @param oldCat the category to delete
     * @throws FakturamaStoringException 
     */
	public void deleteEmptyCategory(T oldCat) throws FakturamaStoringException {
		try {
			if(hasChildren(oldCat)) {
				throw new FakturamaStoringException("category has one or more children and can't be deleted.", new SQLException());
			}
			checkConnection();
			EntityTransaction trx = getEntityManager().getTransaction();
			trx.begin();
			// merge before persist since we could have referenced entities
			// which are already persisted
//			if(withBatch) {
//				entityManager.setProperty(PersistenceUnitProperties.BATCH_WRITING, BatchWriting.JDBC);
//				entityManager.setProperty(PersistenceUnitProperties.BATCH_WRITING_SIZE, 20);
//			}
			oldCat = getEntityManager().merge(oldCat);
//			oldCat.setDeleted(true);
			getEntityManager().remove(oldCat);
			trx.commit();
		} catch (SQLException e) {
			throw new FakturamaStoringException("Error removing category from database.", e, oldCat);
		}
	}

    /**
     * Find a Category by its name. If one of the part categories doesn't exist we create it 
     * (if withPersistOption is set).
     * 
     * @param testCat the category to find
     * @param withPersistOption persist a (part) category if it doesn't exist
     * @return found category
     */
    public T getCategory(String testCat, boolean withPersistOption) {
        // to find the complete category we have to start with the topmost category
        // and then lookup each of the child categories in the given path
        String[] splittedCategories = testCat.split("/");
        T parentCategory = null;
        String category = "";
        try {
            for (int i = 0; i < splittedCategories.length; i++) {
            	if(StringUtils.isBlank(splittedCategories[i])) {
            		continue;
            	}
                category += "/" + splittedCategories[i];
//                ShippingCategory searchCat = findShippingCategoryByName(category);
                T searchCat = findCategoryByName(category);
                if (searchCat == null) {
                    // not found? Then create a new one.
                    T newCategory = getEntityClass().newInstance();
                    newCategory.setName(splittedCategories[i]);
                    newCategory.setParent(parentCategory);
                    newCategory = save(newCategory);
                    searchCat = newCategory;
                }
                // save the parent and then dive deeper...
                parentCategory = searchCat;
            } 
        }
        catch (FakturamaStoringException | InstantiationException | IllegalAccessException e) {
            getLog().error(e);
        }
        return parentCategory;
    }

}
