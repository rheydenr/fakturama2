package com.sebulli.fakturama.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.ContactCategory;

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
    }

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
                ContactCategory searchCat = findCategoryByName(category);
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
