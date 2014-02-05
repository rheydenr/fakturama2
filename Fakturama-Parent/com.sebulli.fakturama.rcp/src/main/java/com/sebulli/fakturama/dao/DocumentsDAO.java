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

import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Expenditure;

@Creatable
public class DocumentsDAO extends AbstractDAO<Document> {

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

    protected Class<Document> getEntityClass() {
    	return Document.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }
    
    /**
     * Get all {@link Expenditure}s from Database.
     *
     * @return List<Expenditure> 
     */
    public List<Document> findAll() {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<Document> cq = cb.createQuery(Document.class);
    	cq.select(cq.from(Document.class));
    	return em.createQuery(cq).getResultList();
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

	public Document findByName(String name) {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<Document> criteria = cb.createQuery(Document.class);
    	Root<Document> root = criteria.from(Document.class);
		CriteriaQuery<Document> cq = criteria.where(cb.equal(root.<String>get("name"), name));
    	return getEntityManager().createQuery(cq).getSingleResult();
		
	}
}
