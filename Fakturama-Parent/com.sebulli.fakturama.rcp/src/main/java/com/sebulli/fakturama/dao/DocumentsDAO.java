package com.sebulli.fakturama.dao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.migration.CategoryBuilder;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Document_;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.Order;

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

    @Inject
    @Translation
    protected Messages msg;

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
		CriteriaQuery<Document> cq = criteria.where(cb.equal(root.<String>get(Document_.name), name));
    	return getEntityManager().createQuery(cq).getSingleResult();
		
	}

    /**
     * @param order
     * @param order_id
     * @param dateAsISO8601String
     * @return
     */
    public List<Document> findDocumentByDocIdAndDocDate(DocumentType type, String webshopId, LocalDateTime calendarWebshopDate) {
        FakturamaModelFactory modelFactory = new FakturamaModelFactory();
        BillingType billingType = modelFactory.createBillingTypeFromString(type.getTypeAsString());
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteria = cb.createQuery(Document.class);
        Root<Document> root = criteria.from(Document.class);
        Instant instant = calendarWebshopDate.atZone(ZoneId.systemDefault()).toInstant();
        Date res = Date.from(instant);
        CriteriaQuery<Document> cq = criteria.where(
                cb.and(
                        cb.equal(root.<BillingType> get(Document_.billingType), billingType),
                        cb.equal(root.<String> get(Document_.webshopId), webshopId),
                        cb.equal(root.<Date> get(Document_.webshopDate), res)
                      )
            );
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Gets the all visible properties of this Shipping object.
     * 
     * @return String[] of visible Shipping properties
     */
    public String[] getVisibleProperties() {
        return new String[] { Document_.name.getName(), Document_.addressFirstLine.getName(), 
                Document_.documentDate.getName(), Document_.totalValue.getName() };
    }

    /**
     * Get an array of strings of all category strings.
     * 
     * Only the categories of the document types are returned, that are in use.
     * e.g. If there is an type "invoice", the categories "invoice/paid" and
     * "invoice/unpaid" are returned.
     * 
     * @return Array of all category strings
     */
    public List<DummyStringCategory> getCategoryStrings() {
        List<DummyStringCategory> resultList = new ArrayList<>();
        Query q = getEntityManager().createQuery("select distinct type(d) from Document d where d.deleted = false");
        List<Class<? extends Document>> typeList = q.getResultList();
        for (Class<? extends Document> document : typeList) {
            
            // Orders
            if (document.getName().contentEquals(Order.class.getName())) {
                // add order documents
                List<DummyStringCategory> cats = createDummyCategories(
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.ORDER)),
                        msg.documentOrderStateNotshipped, 
                        msg.documentOrderStateShipped);
                resultList.addAll(cats);
            }
            
            // Invoices
            if (document.getName().contentEquals(Invoice.class.getName())) {
                // add invoice documents
                List<DummyStringCategory> cats = createDummyCategories(
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.INVOICE)),
                        msg.documentOrderStateUnpaid,
                        msg.documentOrderStatePaid);
                resultList.addAll(cats);
            }
            
        }
        return resultList;
    }

    /**
     * Creates a List of {@link DummyStringCategory}s.
     * 
     * @param category one or more categories which belong together 
     * 
     * @return List of {@link DummyStringCategory}s
     */
    private List<DummyStringCategory> createDummyCategories(String... pCategory) {
        List<DummyStringCategory> retList = new ArrayList<>();
        DummyStringCategory parent = null;
        for (String string : pCategory) {
            if(parent == null) {
                parent = new DummyStringCategory(string);
                retList.add(parent);
            } else {
                DummyStringCategory cat = new DummyStringCategory(string);
                cat.setParent(parent);
                retList.add(cat);
//                parent = cat;
            }
        }
        return retList;
    }
    
}
