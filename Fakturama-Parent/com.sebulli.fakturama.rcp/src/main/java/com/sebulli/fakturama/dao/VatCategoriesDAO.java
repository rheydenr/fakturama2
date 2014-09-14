package com.sebulli.fakturama.dao;

import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.VATCategory_;

@Creatable
public class VatCategoriesDAO extends AbstractDAO<VATCategory> {

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

    protected Class<VATCategory> getEntityClass() {
    	return VATCategory.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }
    
    /**
     * Get all {@link VATCategory}s from Database.
     *
     * @return List<VATCategory> 
     */
    public List<VATCategory> findAll() {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<VATCategory> cq = cb.createQuery(VATCategory.class);
    	CriteriaQuery<VATCategory> selectQuery = cq.select(cq.from(VATCategory.class));
    	return getEntityManager().createQuery(selectQuery).getResultList();
//    	return getEntityManager().createQuery("select p from VATCategory p", VATCategory.class).getResultList();
    }
    
    /**
     * Finds a VATCategory by its name.
     * 
     * @param vatCategory
     * @return
     */
    public VATCategory findVATCategoryByName(String vatCategory) {
        VATCategory result = null;
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<VATCategory> cq = cb.createQuery(VATCategory.class);
    	Root<VATCategory> rootEntity = cq.from(VATCategory.class);
		CriteriaQuery<VATCategory> selectQuery = cq.select(rootEntity)
		        .where(/*cb.and(*/
    		                cb.equal(rootEntity.get(VATCategory_.name), vatCategory)/*,
    		                cb.equal(rootEntity.get(VATCategory_.deleted), false))*/);
        try {
            result = getEntityManager().createQuery(selectQuery).getSingleResult();
        }
        catch (NoResultException nre) {
            // no result means we return a null value 
        }
        return result;
//    	return getEntityManager().createQuery("select p from VATCategory p", VATCategory.class).getResultList();
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
