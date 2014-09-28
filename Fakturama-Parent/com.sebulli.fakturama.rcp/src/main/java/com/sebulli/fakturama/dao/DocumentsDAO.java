package com.sebulli.fakturama.dao;

import java.util.Date;
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

import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Document_;

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

    /**
     * @param order
     * @param order_id
     * @param dateAsISO8601String
     * @return
     */
    public List<Document> findDocumentByDocIdAndDocDate(DocumentType type, String webshopId, Date webshopDate) {
        BillingType billingType = getBillingTypeFromDocumentType(type);
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteria = cb.createQuery(Document.class);
        Root<Document> root = criteria.from(Document.class);
        CriteriaQuery<Document> cq = criteria.where(
                cb.and(
                        cb.equal(root.<BillingType> get(Document_.billingType), billingType),
                        cb.equal(root.<String> get(Document_.webshopId), webshopId),
                        cb.equal(root.<Date> get(Document_.webshopDate), webshopDate)
                      )
            );
        return getEntityManager().createQuery(cq).getResultList();
    }
    
    /**
     * The BillingType is added here because that was for the database modeling. The Document types
     * are merely for UI actions. But we have to put these two enums together.
     */
    private BillingType getBillingTypeFromDocumentType(DocumentType type) {
        BillingType retval = null;
        for (BillingType billingType : BillingType.values()) {
            if(billingType.name().contentEquals(type.getTypeAsString())) {
                retval = billingType;
            }
        }
        return retval;
    }
}
