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
import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextCategory_;

@Creatable
public class TextCategoriesDAO extends AbstractDAO<TextCategory> {

    protected Class<TextCategory> getEntityClass() {
    	return TextCategory.class;
    }
    
    /**
     * Get all {@link TextCategory}s from Database.
     *
     * @return List<TextCategory> 
     */
    public List<TextCategory> findAll() {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<TextCategory> cq = cb.createQuery(TextCategory.class);
    	CriteriaQuery<TextCategory> selectQuery = cq.select(cq.from(TextCategory.class));
    	return getEntityManager().createQuery(selectQuery).getResultList();
    }
    
    /**
     * Finds a {@link TextCategory} by its name. Category in this case is a String separated by 
     * slashes, e.g. "/fooCat/barCat". Searching starts with the rightmost value
     * and then check the parent. 
     * 
     * @param textCategory the Category to search
     * @return {@link TextCategory}
     */
    public TextCategory findTextCategoryByName(String textCategory) {
        TextCategory result = null;
        if(StringUtils.isNotEmpty(textCategory)) {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<TextCategory> cq = cb.createQuery(getEntityClass());
            Root<TextCategory> rootEntity = cq.from(getEntityClass());
            // extract the rightmost value
            String[] splittedCategories = textCategory.split("/");
            String leafCategory = splittedCategories[splittedCategories.length - 1];        
            CriteriaQuery<TextCategory> selectQuery = cq.select(rootEntity)
                    .where(cb.and(
                                cb.equal(rootEntity.get(TextCategory_.name), leafCategory) /*,
                                cb.equal(rootEntity.get(TextCategory_.parent), TextCategory.class)
                               ,
                                cb.equal(rootEntity.get(TextCategory_.deleted), false)*/));
            try {
                List<TextCategory> tmpResultList = getEntityManager().createQuery(selectQuery).getResultList();
                // remove leading slash
                String testCat = StringUtils.removeStart(textCategory, "/");
                for (TextCategory textCategory2 : tmpResultList) {
                    if(StringUtils.equals(CommonConverter.getCategoryName(textCategory2, ""), testCat)) {
                        result = textCategory2;
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
     * Find a {@link TextCategory} by its name. If one of the part categories doesn't exist we create it 
     * (if {@code withPersistOption} is set to <code>true</code>).
     * 
     * @param testCat the category to find
     * @param withPersistOption persist a (part) category if it doesn't exist
     * @return found category
     */
    public TextCategory getOrCreateCategory(final String testCat, final boolean withPersistOption) {
    	// if testcat starts with a "/" then we have to remove it
    	String myCategory = StringUtils.removeStart(testCat, "/");
    	
        // to find the complete category we have to start with the topmost category
        // and then lookup each of the child categories in the given path
        String[] splittedCategories = myCategory.split("/");
        TextCategory parentCategory = null;
        String category = "";
        try {
            for (int i = 0; i < splittedCategories.length; i++) {
                category += "/" + splittedCategories[i];
                TextCategory searchCat = findTextCategoryByName(category);
                if (searchCat == null) {
                    // not found? Then create a new one.
                    TextCategory newCategory = new TextCategory();
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
