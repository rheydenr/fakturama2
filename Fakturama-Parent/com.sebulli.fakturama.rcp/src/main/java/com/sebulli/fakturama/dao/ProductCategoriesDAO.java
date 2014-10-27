package com.sebulli.fakturama.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.ProductCategory_;
import com.sebulli.fakturama.parts.converter.CommonConverter;

@Creatable
public class ProductCategoriesDAO extends AbstractDAO<ProductCategory> {

    @Inject
    @GeminiPersistenceContext(unitName = "unconfigured2", properties = {
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference(PersistenceUnitProperties.JDBC_DRIVER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference(PersistenceUnitProperties.JDBC_URL)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, valuePref = @Preference(PersistenceUnitProperties.JDBC_USER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, valuePref = @Preference(PersistenceUnitProperties.JDBC_PASSWORD)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.LOGGING_LEVEL, value = "INFO"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
    private EntityManager em;

    protected Class<ProductCategory> getEntityClass() {
    	return ProductCategory.class;
    }
    
    @Override
    public ProductCategory findByExample(ProductCategory example) {
    	ProductCategory result = null;
		// Two Categories are equal if they have the same name and the same parent string
    	
// von ReadAll()        query.setHierarchicalQueryClause(startWith, connectBy, orderSiblingsExpressions);

    	
		String exampleCategory = CommonConverter.getCategoryName(example, "");
		List<ProductCategory> foundCandidates = findAllByCategoryName(exampleCategory);
		Optional<ProductCategory> s = foundCandidates.stream().filter(prodCategory -> prodCategory.getName().equalsIgnoreCase(example.getName())).findFirst();
		result = s.isPresent() ? s.get() : null;
		return result;
    }
    
    /**
     * Finds a {@link ProductCategory} by its name. Category in this case is a String separated by 
     * slashes, e.g. "/fooCat/barCat". Searching starts with the rightmost value
     * and then check the parent. 
     * 
     * @param productCategory the {@link ProductCategory} to search
     * @return {@link ProductCategory}
     */
    public List<ProductCategory> findAllByCategoryName(String productCategory) {
    	List<ProductCategory> result = new ArrayList<ProductCategory>();
        if(StringUtils.isNotEmpty(productCategory)) {
        	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        	CriteriaQuery<ProductCategory> cq = cb.createQuery(getEntityClass());
        	Root<ProductCategory> rootEntity = cq.from(getEntityClass());
        	// extract the rightmost value
            String[] splittedCategories = productCategory.split("/");
        	String leafCategory = splittedCategories[splittedCategories.length - 1];       	
    		CriteriaQuery<ProductCategory> selectQuery = cq.select(rootEntity)
    		        .where(cb.and(
        		                cb.equal(rootEntity.get(ProductCategory_.name), leafCategory),
        		                cb.equal(rootEntity.get(ProductCategory_.deleted), false)));
            try {
                List<ProductCategory> tmpResultList = getEntityManager().createQuery(selectQuery).getResultList();
                // remove leading slash
                String testCat = StringUtils.removeStart(productCategory, "/");
                for (ProductCategory vatCategory2 : tmpResultList) {
                    if(StringUtils.equals(CommonConverter.getCategoryName(vatCategory2, ""), testCat)) {
                        result.add(vatCategory2);
                    }
                }
            }
            catch (NoResultException nre) {
                // no result means we return a null value 
            }
        }
        return result;
    }
    
    
    
    

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
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
}
