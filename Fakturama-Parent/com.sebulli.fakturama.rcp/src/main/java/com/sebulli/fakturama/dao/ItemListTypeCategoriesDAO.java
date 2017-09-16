package com.sebulli.fakturama.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.ItemListTypeCategory;

@Creatable
public class ItemListTypeCategoriesDAO extends AbstractCategoriesDAO<ItemListTypeCategory> {
    
    protected Class<ItemListTypeCategory> getEntityClass() {
    	return ItemListTypeCategory.class;
    }
    
    /**
     * Get all {@link ItemListTypeCategory}s from Database.
     *
     * @return List<ItemListTypeCategory> 
     */
    public List<ItemListTypeCategory> findAll() {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<ItemListTypeCategory> cq = cb.createQuery(ItemListTypeCategory.class);
    	CriteriaQuery<ItemListTypeCategory> selectQuery = cq.select(cq.from(ItemListTypeCategory.class));
    	return getEntityManager().createQuery(selectQuery).getResultList();
    }
    
 	/**
	 * Find a {@link ItemListTypeCategory} by its name. If one of the part categories doesn't exist we create it 
	 * (if {@code withPersistOption} is set to <code>true</code>).
	 * 
	 * @param testCat the category to find
	 * @param withPersistOption persist a (part) category if it doesn't exist
	 * @return found category
	 */
    public ItemListTypeCategory getOrCreateCategory(String testCat, boolean withPersistOption) {
        // to find the complete category we have to start with the topmost category
        // and then lookup each of the child categories in the given path
        String[] splittedCategories = testCat.split("/");
        ItemListTypeCategory parentCategory = null;
        String category = "";
        try {
            for (int i = 0; i < splittedCategories.length; i++) {
                category += "/" + splittedCategories[i];
                ItemListTypeCategory searchCat = findCategoryByName(category);
                if (searchCat == null) {
                    // not found? Then create a new one.
                    ItemListTypeCategory newCategory = modelFactory.createItemListTypeCategory();
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
