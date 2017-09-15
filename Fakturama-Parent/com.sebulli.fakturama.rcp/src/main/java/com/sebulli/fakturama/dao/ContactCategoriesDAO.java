package com.sebulli.fakturama.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ContactCategory_;

@Creatable
public class ContactCategoriesDAO extends AbstractCategoriesDAO<ContactCategory> {

    protected Class<ContactCategory> getEntityClass() {
    	return ContactCategory.class;
    }
    
    /**
     * Get all {@link ContactCategory}s from Database.
     *
     * @return List<ContactCategory> 
     */
    public List<ContactCategory> findAll() {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<ContactCategory> cq = cb.createQuery(ContactCategory.class);
    	CriteriaQuery<ContactCategory> selectQuery = cq.select(cq.from(ContactCategory.class));
    	return getEntityManager().createQuery(selectQuery).getResultList();
//    	return getEntityManager().createQuery("select p from ContactCategory p", ContactCategory.class).getResultList();
    }
//    
//    /**
//     * Finds a {@link ContactCategory} by its name. Category in this case is a String separated by 
//     * slashes, e.g. "/fooCat/barCat". Searching starts with the rightmost value
//     * and then check the parent. 
//     * 
//     * @param pContactCategory the Category to search
//     * @return {@link ContactCategory}
//     */
//    public ContactCategory findContactCategoryByName(String pContactCategory) {
//        ContactCategory result = null;
//        if(StringUtils.isNotEmpty(pContactCategory)) {
//        	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
//        	CriteriaQuery<ContactCategory> cq = cb.createQuery(ContactCategory.class);
//        	Root<ContactCategory> rootEntity = cq.from(ContactCategory.class);
//        	// extract the rightmost value
//            String[] splittedCategories = pContactCategory.split("/");
//        	String leafCategory = splittedCategories[splittedCategories.length - 1];       	
//    		CriteriaQuery<ContactCategory> selectQuery = cq.select(rootEntity)
//    		        .where(cb.and(
//        		                cb.equal(rootEntity.get(ContactCategory_.name), leafCategory) /*,
//        		                cb.equal(rootEntity.get(ContactCategory_.parent), ContactCategory.class)
//        		               ,
//        		                cb.equal(rootEntity.get(ContactCategory_.deleted), false)*/));
//            try {
//                List<ContactCategory> tmpResultList = getEntityManager().createQuery(selectQuery).getResultList();
//                // remove leading slash
//                String testCat = StringUtils.removeStart(pContactCategory, "/");
//                for (ContactCategory contactCategoryEntry : tmpResultList) {
//                    if(StringUtils.equals(CommonConverter.getCategoryName(contactCategoryEntry, ""), testCat)) {
//                        result = contactCategoryEntry;
//                        break;
//                    }
//                }
//            }
//            catch (NoResultException nre) {
//                // no result means we return a null value 
//            }
//        }
//        return result;
//    }
    

    /**
     * Find a {@link ContactCategory} by its name. If one of the part categories doesn't exist we create it 
     * (if {@code withPersistOption} is set to <code>true</code>).
     * 
     * @param testCat the category to find
     * @param withPersistOption persist a (part) category if it doesn't exist
     * @return found category
     */
    public ContactCategory getCategory(String testCat, boolean withPersistOption) {
        // to find the complete category we have to start with the topmost category
        // and then lookup each of the child categories in the given path
        String[] splittedCategories = testCat.split("/");
        ContactCategory parentCategory = null;
        String category = "";
        try {
            for (int i = 0; i < splittedCategories.length; i++) {
            	if(StringUtils.isBlank(splittedCategories[i])) {
            		continue;
            	}
                category += "/" + splittedCategories[i];
                ContactCategory searchCat = findContactCategoryByName(category);
                if (searchCat == null) {
                    // not found? Then create a new one.
                    ContactCategory newCategory = new ContactCategory();
                    newCategory.setName(splittedCategories[i]);
                    newCategory.setParent(parentCategory);
                    newCategory = save(newCategory);
                    searchCat = newCategory;
                }
                // save the parent and then dive deeper...
                parentCategory = searchCat;
            }
        }
        catch (FakturamaStoringException e) {
            getLog().error(e);
        }
        return parentCategory;
    }
}
