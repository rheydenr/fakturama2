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
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Confirmation;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Credit;
import com.sebulli.fakturama.model.Delivery;
import com.sebulli.fakturama.model.Delivery_;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Document_;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.model.Dunning_;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.Invoice_;
import com.sebulli.fakturama.model.Letter;
import com.sebulli.fakturama.model.Offer;
import com.sebulli.fakturama.model.Order;
import com.sebulli.fakturama.model.Proforma;

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
     * Gets the all visible properties of this Documents object.
     * 
     * @return String[] of visible Documents properties
     */
    public String[] getVisibleProperties() {
        return new String[] { Document_.name.getName(), Document_.addressFirstLine.getName(), 
                Document_.serviceDate.getName(), Document_.totalValue.getName() };
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
        @SuppressWarnings("unchecked")
        List<Class<? extends Document>> typeList = q.getResultList();
        for (Class<? extends Document> document : typeList) {
            
            // Letters
            if (document.getName().contentEquals(Letter.class.getName())) {
                // add letter documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.LETTER,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.LETTER)));
                resultList.addAll(cats);
            }
            
            if (document.getName().contentEquals(Offer.class.getName())) {
                // add order documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.OFFER,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.OFFER)));
                resultList.addAll(cats);
            }
            
            // Orders
            if (document.getName().contentEquals(Order.class.getName())) {
                // add order documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.ORDER,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.ORDER)),
                        msg.documentOrderStateNotshipped, 
                        msg.documentOrderStateShipped);
                resultList.addAll(cats);
            }
            
            if (document.getName().contentEquals(Confirmation.class.getName())) {
                // add letter documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.CONFIRMATION,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.CONFIRMATION)));
                resultList.addAll(cats);
            }
            
            // Invoices
            if (document.getName().contentEquals(Invoice.class.getName())) {
                // add invoice documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.INVOICE,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.INVOICE)),
                        msg.documentOrderStateUnpaid,
                        msg.documentOrderStatePaid);
                resultList.addAll(cats);
            }
            
            // Deliveries
            if (document.getName().contentEquals(Delivery.class.getName())) {
                // add dunning documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.DELIVERY,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.DELIVERY)),
                        msg.documentDeliveryStateHasinvoice,
                        msg.documentDeliveryStateHasnoinvoice);
                resultList.addAll(cats);
            }
            
            // Credits
            if (document.getName().contentEquals(Credit.class.getName())) {
                // add credit documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.CREDIT,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.CREDIT)),
                        msg.documentOrderStateUnpaid,
                        msg.documentOrderStatePaid);
                resultList.addAll(cats);
            }
            
            // Dunnings
            if (document.getName().contentEquals(Dunning.class.getName())) {
                // add dunning documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.DUNNING,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.DUNNING)),
                        msg.documentOrderStateUnpaid,
                        msg.documentOrderStatePaid);
                resultList.addAll(cats);
            }
            
            if (document.getName().contentEquals(Proforma.class.getName())) {
                // add letter documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.PROFORMA,
                        msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.PROFORMA)));
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
    private List<DummyStringCategory> createDummyCategories(DocumentType docType, String... pCategory) {
        List<DummyStringCategory> retList = new ArrayList<>();
        DummyStringCategory parent = null;
        for (String string : pCategory) {
            if(parent == null) {
                parent = new DummyStringCategory(string, docType);
                retList.add(parent);
            } else {
                DummyStringCategory cat = new DummyStringCategory(string, docType);
                cat.setParent(parent);
                retList.add(cat);
            }
        }
        return retList;
    }

    /**
     * Finds all paid {@link Invoice}s.
     * 
     * @return List of paid {@link Invoice}s
     */
    public List<Invoice> findPaidInvoices() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Invoice> criteria = cb.createQuery(Invoice.class);
        Root<Invoice> root = criteria.from(Invoice.class);
        CriteriaQuery<Invoice> cq = criteria.where(cb.equal(root.<Boolean>get(Invoice_.paid), true));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Finds all paid {@link Invoice}s by a given {@link Contact}.
     * 
     * @param contact the {@link Contact} to look up
     * @return List of paid {@link Invoice}s
     */
    public List<Invoice> findPaidInvoicesForContact(Contact contact) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Invoice> criteria = cb.createQuery(Invoice.class);
        Root<Invoice> root = criteria.from(Invoice.class);
        CriteriaQuery<Invoice> cq = criteria.where(
                cb.and(
                        cb.equal(root.<Boolean>get(Invoice_.paid), true),
                        cb.equal(root.<Contact>get(Invoice_.contact), contact))
                      );
        return getEntityManager().createQuery(cq).getResultList();
    }    

    /**
     * Update {@link Dunning}s which are related to a certain invoice.
     * 
     * @param document the invoice which is related
     * @param isPaid is it paid?
     * @param paidDate paid date
     * @param paidValue paid value
     */
    public void updateDunnings(Document document, boolean isPaid, Date paidDate, Double paidValue) {
//      UPDATE dunning SET paid, paidValue, paidDate WHERE dunning.invoiceid = invid  
//      // TODO What if "payvalue" is not the total sum? Is it paid?
//          dunning.setPaid(bPaid.getSelection());
//          dunning.setStringValueByKey("paydate", DataUtils.getDateTimeAsString(dtPaidDate));
//          dunning.setDoubleValueByKey("payvalue", paidValue.getValueAsDouble());
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Dunning> criteria = cb.createCriteriaUpdate(Dunning.class);
        criteria
            .set(Dunning_.paid, isPaid)
            .set(Dunning_.payDate, paidDate)
            .set(Dunning_.paidValue, paidValue)
            .where(cb.equal(criteria.from(Dunning.class).get(Dunning_.invoiceReference), document))
            ;
        executeCriteria(criteria);
    }

    /**
     * Executes a given {@link CriteriaUpdate} within a separate {@link EntityTransaction}.
     * @param criteria the Criteria to execute
     */
    private void executeCriteria(CriteriaUpdate<?> criteria) {
        EntityTransaction tx = getEntityManager().getTransaction();
        tx.begin();
        try {
            getEntityManager().createQuery(criteria).executeUpdate();
            tx.commit();
        } catch (PersistenceException e) {
            tx.rollback();
        }
    }

    /**
     * Selects all given Deliveries (which don't have an invoice reference) by ID.
     * 
     * @param selectedIds
     * @return
     */
    public List<Delivery> findSelectedDeliveries(List<Long> selectedIds) {
        // setCategoryFilter(DocumentType.getPluralString(DocumentType.DELIVERY) + "/" + DataSetDocument.getStringHASNOINVOICE());
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Delivery> criteria = cb.createQuery(Delivery.class);
        Root<Delivery> root = criteria.from(Delivery.class);
        CriteriaQuery<Delivery> cq = criteria.where(
                cb.and(
                        cb.isNull(root.get(Delivery_.invoiceReference)),
                        root.get(Delivery_.id).in(selectedIds)
                       )
                );
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Update the invoice references in all documents within the same transaction.
     * 
     * @param document
     */
    public void updateInvoiceReferences(Invoice document) {
/*
            Transaction trans = new Transaction(document);
            List<DataSetDocument> docs = trans.getDocuments();
            for (DataSetDocument doc : docs) {
                if(doc.getIntValueByKey("invoiceid") < 0) {
                    doc.setIntValueByKey("invoiceid", documentId );
                    Data.INSTANCE.updateDataSet(doc);
                }
            }
 */
        // update documents set fk_invoiceref = document where fk_invoiceref = null and transactionid = ?
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Document> criteria = cb.createCriteriaUpdate(Document.class);
        Root<Document> root = criteria.from(Document.class);
        criteria
            .set(Document_.invoiceReference, document)
            .where(
                    cb.and(
                            cb.isNull(root.get(Document_.invoiceReference)),
                            cb.equal(root.get(Document_.transactionId), document.getTransactionId())
                  ))
            ;
        executeCriteria(criteria);
    }

    /**
     * Updates the {@link Delivery} entities that are contained in the given document (as part of
     * a collecting invoice). Update contains setting the invoice reference and merging the transactions 
     * (if needed). 
     * 
     * @param importedDeliveryNotes List of {@link Delivery} IDs 
     * @param document {@link Invoice} document
     */
    public void updateDeliveries(List<Long> importedDeliveryNotes, Invoice document) {
/*        for (Long importedDeliveryNote : importedDeliveryNotes) {
            if (importedDeliveryNote >= 0) {
                DataSetDocument deliveryNote = Data.INSTANCE.getDocuments().getDatasetById(importedDeliveryNote);
                deliveryNote.setIntValueByKey("invoiceid", documentId );
                Data.INSTANCE.updateDataSet(deliveryNote);
                
                // Change also the transaction id of the imported delivery note
                Transaction.mergeTwoTransactions(document, deliveryNote);
            }
        }
*/    
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Delivery> criteria = cb.createCriteriaUpdate(Delivery.class);
        Root<Delivery> root = criteria.from(Delivery.class);
        criteria.set(Delivery_.invoiceReference, document).where(root.get(Delivery_.id).in(importedDeliveryNotes));
        executeCriteria(criteria);
        mergeTwoTransactions(document, importedDeliveryNotes);
    }
 
    /**
     * Merge 2 transactions into one single
     * 
     * @param mainDocument the main {@link Document}
     * @param otherDocument the {@link Document} which gets the id of the main {@link Document}
     */
    public void mergeTwoTransactions(Document mainDocument, Document otherDocument) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Document> criteria = cb.createCriteriaUpdate(Document.class);
        Root<Document> root = criteria.from(Document.class);
        criteria.set(Document_.transactionId, mainDocument.getTransactionId()).where(cb.equal(root.get(Document_.id), otherDocument.getId()));
        executeCriteria(criteria);
    }    
 
    /**
     * Merge 2 transactions into one single for a given List of {@link Document}s.
     * 
     * @param mainDocument the main {@link Document}
     * @param otherDocument the list of {@link Document}s which gets the id of the main {@link Document}
     */
    public void mergeTwoTransactions(Document mainDocument, List<Long> importedDeliveryNotes) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Document> criteria = cb.createCriteriaUpdate(Document.class);
        Root<Document> root = criteria.from(Document.class);
        criteria.set(Document_.transactionId, mainDocument.getTransactionId()).where(root.get(Document_.id).in(importedDeliveryNotes));
        executeCriteria(criteria);
    }

    /**
     * Tests if an other entity with the same name exists.
     * 
     * @param document the {@link Document} to test
     * @return 
     */
    public boolean existsOther(Document document) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteria = cb.createQuery(Document.class);
        Root<Document> root = criteria.from(Document.class);
        CriteriaQuery<Document> cq = criteria.where(
                cb.and(cb.notEqual(root.<Long>get(Document_.id), document.getId()),
                       cb.equal(root.<String>get(Document_.name), document.getName())));
        return !getEntityManager().createQuery(cq).getResultList().isEmpty();
    }
    
}
