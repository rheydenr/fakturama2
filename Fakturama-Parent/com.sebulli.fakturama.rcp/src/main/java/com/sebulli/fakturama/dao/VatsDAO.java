package com.sebulli.fakturama.dao;

import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.oldmodel.OldVats;

@Creatable
public class VatsDAO extends AbstractDAO<VAT> {

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

    protected Class<VAT> getEntityClass() {
    	return VAT.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }
    
    /**
     * Get all {@link VAT} from Database.
     *
     * @return List<VAT> 
     */
    public List<VAT> findAll() {
    	//getEM2();
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<VAT> criteria = cb.createQuery(VAT.class);
    	Root<VAT> root = criteria.from(VAT.class);
		CriteriaQuery<VAT> cq = criteria.where(cb.not(root.<Boolean>get("deleted")));
    	return getEntityManager().createQuery(cq).getResultList();
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
	 * Finds a {@link VAT} by a given {@link OldVats}.
	 * 
	 * @param oldVat
	 * @return
	 */
	public VAT findByOldVat(OldVats oldVat) {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<VAT> criteria = cb.createQuery(VAT.class);
    	Root<VAT> root = criteria.from(VAT.class);
		CriteriaQuery<VAT> cq = criteria.where(
				cb.and(
						cb.equal(root.<String>get("description"), oldVat.getDescription()),
						cb.equal(root.<String>get("name"), oldVat.getName())));
    	return getEntityManager().createQuery(cq).getSingleResult();
	}

	public VAT findByName(String novatname) {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<VAT> criteria = cb.createQuery(VAT.class);
    	Root<VAT> root = criteria.from(VAT.class);
		CriteriaQuery<VAT> cq = criteria.where(
						cb.equal(root.<String>get("name"), novatname));
    	return getEntityManager().createQuery(cq).getSingleResult();
		
	}
}
