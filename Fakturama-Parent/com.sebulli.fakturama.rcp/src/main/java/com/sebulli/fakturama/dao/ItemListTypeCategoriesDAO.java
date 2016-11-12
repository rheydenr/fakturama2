package com.sebulli.fakturama.dao;

import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.ItemListTypeCategory;
import com.sebulli.fakturama.model.ItemListTypeCategory_;

@Creatable
public class ItemListTypeCategoriesDAO extends AbstractDAO<ItemListTypeCategory> {

    @Inject
    @GeminiPersistenceContext(unitName = "unconfigured2", properties = {
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference(PersistenceUnitProperties.JDBC_DRIVER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference(PersistenceUnitProperties.JDBC_URL)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, valuePref = @Preference(PersistenceUnitProperties.JDBC_USER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, valuePref = @Preference(PersistenceUnitProperties.JDBC_PASSWORD)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.LOGGING_LEVEL, value = "INFO"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
//    @GeminiPersistenceContext(unitName = "mysql-datasource")
//    @GeminiPersistenceContext(unitName = "origin-datasource")
    private EntityManager em;

    private FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;
    
    protected Class<ItemListTypeCategory> getEntityClass() {
    	return ItemListTypeCategory.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
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
//    	return getEntityManager().createQuery("select p from ItemListTypeCategory p", ItemListTypeCategory.class).getResultList();
    }
    
    /**
     * Finds a {@link ItemListTypeCategory} by its name. Category in this case is a String separated by 
     * slashes, e.g. "/fooCat/barCat". Searching starts with the rightmost value
     * and then check the parent. 
     * 
     * @param vatCategory the Category to search
     * @return {@link ItemListTypeCategory}
     */
    public ItemListTypeCategory findItemListTypeCategoryByName(String vatCategory) {
        ItemListTypeCategory result = null;
        if(StringUtils.isNotEmpty(vatCategory)) {
        	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        	CriteriaQuery<ItemListTypeCategory> cq = cb.createQuery(getEntityClass());
        	Root<ItemListTypeCategory> rootEntity = cq.from(getEntityClass());
        	// extract the rightmost value
            String[] splittedCategories = vatCategory.split("/");
        	String leafCategory = splittedCategories[splittedCategories.length - 1];       	
    		CriteriaQuery<ItemListTypeCategory> selectQuery = cq.select(rootEntity)
    		        .where(cb.and(
        		                cb.equal(rootEntity.get(ItemListTypeCategory_.name), leafCategory) /*,
        		                cb.equal(rootEntity.get(ItemListTypeCategory_.parent), ItemListTypeCategory.class)
        		               ,
        		                cb.equal(rootEntity.get(ItemListTypeCategory_.deleted), false)*/));
            try {
                List<ItemListTypeCategory> tmpResultList = getEntityManager().createQuery(selectQuery).getResultList();
                // remove leading slash
                String testCat = StringUtils.removeStart(vatCategory, "/");
                for (ItemListTypeCategory vatCategory2 : tmpResultList) {
                    if(StringUtils.equals(CommonConverter.getCategoryName(vatCategory2, ""), testCat)) {
                        result = vatCategory2;
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
	 * @return the em
	 */
	protected EntityManager getEntityManager() {
		return em;
	}

	/**
	 * @param em the em to set
	 */
	protected void setEntityManager(EntityManager em) {
		this.em = em;
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
                ItemListTypeCategory searchCat = findItemListTypeCategoryByName(category);
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
