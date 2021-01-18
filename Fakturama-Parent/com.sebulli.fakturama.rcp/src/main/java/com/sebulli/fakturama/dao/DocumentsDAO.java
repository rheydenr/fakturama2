package com.sebulli.fakturama.dao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.dialogs.SelectDeliveryNoteDialog;
import com.sebulli.fakturama.dto.AccountEntry;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Confirmation;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Credit;
import com.sebulli.fakturama.model.Delivery;
import com.sebulli.fakturama.model.Delivery_;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver_;
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
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Payment_;
import com.sebulli.fakturama.model.Proforma;
import com.sebulli.fakturama.model.VoucherCategory;

@Creatable
public class DocumentsDAO extends AbstractDAO<Document> {

    @Inject
    @Translation
    protected Messages msg;
    
    protected Class<Document> getEntityClass() {
    	return Document.class;
    }

	public Document findByName(String name) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<Document> criteria = cb.createQuery(Document.class);
	    Root<Document> root = criteria.from(Document.class);
		CriteriaQuery<Document> cq = criteria.where(cb.equal(root.<String>get(Document_.name), name));
    	return getEntityManager().createQuery(cq).getSingleResult();
	}
	
@Override
public List<Document> findAll(boolean forceRead) {
	List<Document> resultList;
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Document> criteria = cb.createQuery(getEntityClass());
    Root<Document> root = criteria.from(getEntityClass());
    CriteriaQuery<Document> cq = criteria.where(cb.notEqual(root.get(Document_.deleted), Boolean.TRUE));
    TypedQuery<Document> query = getEntityManager().createQuery(cq);
    if(forceRead) {
        query.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
        query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
    }
	try {
		resultList = query.getResultList();
	} catch (PersistenceException e) {
		System.err.println("First start, no table found. If this problem remains after first start, please contact your administrator.");
		resultList = Collections.emptyList();
	}
	return resultList;
}

/**
 * Finds Documents having a given account. Only {@link BillingType#INVOICE}
 * and {@link BillingType#CREDIT} are considered. An account is a {@link VoucherCategory}
 * from a {@link Payment}.
 * 
 * @param account which account should be used for filtering
 * @return List of {@link AccountEntry}s, sorted by Document date
 */
public List<AccountEntry> findAccountedDocuments(VoucherCategory account) {
	return findAccountedDocuments(account, null, null);
}
	
/**
 * Finds Documents having a given account. Only {@link BillingType#INVOICE}
 * and {@link BillingType#CREDIT} are considered. An account is a {@link VoucherCategory}
 * from a {@link Payment}. The Documents can be filtered for a certain date range.
 * 
 * @param account which account should be used for filtering
 * @param startDate Date for filtering (can be <code>null</code>)
 * @param endDate Date for filtering (can be <code>null</code>)
 * @return List of {@link AccountEntry}s, sorted by Document date
 */
public List<AccountEntry> findAccountedDocuments(VoucherCategory account, Date startDate, Date endDate) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Document> criteria = cb.createQuery(getEntityClass());
    Root<Document> root = criteria.from(getEntityClass());
    Predicate predicate = cb.and(
			cb.not(root.get(Document_.deleted)),
			cb.or(
					cb.equal(root.get(Document_.billingType), BillingType.INVOICE),
					cb.equal(root.get(Document_.billingType), BillingType.CREDIT)
				  ),
			cb.equal(root.get(Document_.payment).get(Payment_.category), account)
	);
    
    // take the paydate into account (NOT the document date!)
    if(startDate != null && endDate != null) {
    	// if startDate is after endDate we switch the two dates silently
    	predicate = cb.and(predicate,
    			cb.between(root.get(Document_.payDate), startDate.before(endDate) ? startDate : endDate, 
    					endDate.after(startDate) ? endDate : startDate)
    		);
    }
	CriteriaQuery<Document> cq = criteria.where(predicate).orderBy(cb.asc(root.get(Document_.payDate)));
    TypedQuery<Document> query = getEntityManager().createQuery(cq);
	List<Document> documentList = query.getResultList();
	List<AccountEntry> resultList = new ArrayList<>();
	for (Document document : documentList) {
		AccountEntry accountEntry = new AccountEntry(document);
		resultList.add(accountEntry);
	}
	return resultList;
}


    /**
     * Find {@link Document}s by type, their webshop ID and a date. 
     *  
     * @param type the {@link DocumentType} of the document
     * @param webshopId the ID from webshop which is assigned to this {@link Document}  
     * @param calendarWebshopDate the dateTime for which this {@link Document} was retrieved from webShop
     * @return a List of {@link Document}s (or an empty List if none was found) 
     */
    public List<Document> findByDocIdAndDocDate(DocumentType type, String webshopId, LocalDateTime calendarWebshopDate) {
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
                        cb.equal(root.<Date> get(Document_.webshopDate), res),
                        cb.notEqual(root.get(Document_.deleted), Boolean.TRUE)
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
        
        if(getEntityManager() == null) {
        	return null;
        }
        
        Query q = getEntityManager().createQuery("select distinct type(d) from Document d where d.deleted = false");
        
        @SuppressWarnings("unchecked")
        List<Class<? extends Document>> typeList = q.getResultList();
        for (Class<? extends Document> document : typeList) {
           
            // Letters
            if (document.getName().contentEquals(Letter.class.getName())) {
                // add letter documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.LETTER);
                resultList.addAll(cats);
            }
            
            if (document.getName().contentEquals(Offer.class.getName())) {
                // add order documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.OFFER);
                resultList.addAll(cats);
            }
            
            // Orders
            if (document.getName().contentEquals(Order.class.getName())) {
                // add order documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.ORDER,
                        msg.documentOrderStateNotshipped, 
                        msg.documentOrderStateShipped);
                resultList.addAll(cats);
            }
            
            if (document.getName().contentEquals(Confirmation.class.getName())) {
                // add letter documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.CONFIRMATION);
                resultList.addAll(cats);
            }
            
            // Invoices
            if (document.getName().contentEquals(Invoice.class.getName())) {
                // add invoice documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.INVOICE,
                        msg.documentOrderStateUnpaid,
                        msg.documentOrderStatePaid);
                resultList.addAll(cats);
            }
            
            // Deliveries
            if (document.getName().contentEquals(Delivery.class.getName())) {
                // add dunning documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.DELIVERY,
                        msg.documentDeliveryStateHasinvoice,
                        msg.documentDeliveryStateHasnoinvoice);
                resultList.addAll(cats);
            }
            
            // Credits
            if (document.getName().contentEquals(Credit.class.getName())) {
                // add credit documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.CREDIT,
                        msg.documentOrderStateUnpaid,
                        msg.documentOrderStatePaid);
                resultList.addAll(cats);
            }
            
            // Dunnings
            if (document.getName().contentEquals(Dunning.class.getName())) {
                // add dunning documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.DUNNING,
                        msg.documentOrderStateUnpaid,
                        msg.documentOrderStatePaid);
                resultList.addAll(cats);
            }
            
            if (document.getName().contentEquals(Proforma.class.getName())) {
                // add letter documents
                List<DummyStringCategory> cats = createDummyCategories(
                        DocumentType.PROFORMA);
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
        if(parent == null) {
            parent = new DummyStringCategory(msg.getMessageFromKey(DocumentType.getPluralString(docType)), docType);
            retList.add(parent);
        } 
        for (String string : pCategory) {
            DummyStringCategory cat = new DummyStringCategory(string, docType);
            cat.setParent(parent);
            retList.add(cat);
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
     * Find all paid {@link Invoice}s by a given {@link Contact}.
     * 
     * @param contact the {@link Contact} to look up
     * @return List of paid {@link Invoice}s
     */
    public List<Invoice> findPaidInvoicesForContact(Contact contact) {
    	if(contact == null) {
    		return Collections.emptyList();
    	}
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Invoice> criteria = cb.createQuery(Invoice.class);
        Root<Invoice> root = criteria.from(Invoice.class);
        
        /*
         *  SELECT distinct d.name
			FROM FKT_DOCUMENTRECEIVER dr ,
			     FKT_DOCUMENT d,
			     FKT_CONTACT c
			WHERE dr.FK_DOCUMENT = d.ID
			  AND dr.ORIGINCONTACTID = c.ID
			  AND d.dtype = 'Invoice'
			  and c.id = 1
         */

        CriteriaQuery<Invoice> cq = criteria.distinct(true).where(
            cb.and(cb.equal(root.<Boolean>get(Invoice_.paid), true),
                   cb.equal(root.<Boolean>get(Invoice_.deleted), false),
                   cb.equal(root.join(Invoice_.receiver).get(DocumentReceiver_.originContactId), contact.getId())
                 ));
        List<Invoice> resultList = getEntityManager().createQuery(cq).getResultList();
		return resultList;
    }    
    
    public void updateDunnings(Document document) {
    	updateDunnings(document, document.getPaid(), document.getPayDate());
    }

    /**
     * Update {@link Dunning}s which are related to a certain invoice.
     * 
     * @param document the invoice which is related
     * @param isPaid is it paid?
     * @param paidDate paid date
     */
    public void updateDunnings(Document document, boolean isPaid, Date paidDate) {
//      UPDATE dunning SET paid, paidValue, paidDate WHERE dunning.invoiceid = invid  
//      // TODO What if "payvalue" is not the total sum? Is it paid?
    	if(!document.getBillingType().isINVOICE()) {
    		// only update dunnings if we have an invoice!
    		return;
    	}
    	Double paidValue = document.getPaidValue();
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
		// setCategoryFilter(DocumentType.getPluralString(DocumentType.DELIVERY)
		// + "/" + DataSetDocument.getStringHASNOINVOICE());
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Delivery> criteria = cb.createQuery(Delivery.class);
		Root<Delivery> root = criteria.from(Delivery.class);
		CriteriaQuery<Delivery> cq;
		Predicate baseClause = cb.and(cb.equal(root.<BillingType> get(Document_.billingType), BillingType.DELIVERY),
				cb.isNull(root.get(Delivery_.invoiceReference)),
				cb.equal(root.<Boolean> get(Document_.deleted), false));
		if (selectedIds != null) {
			cq = criteria.where(cb.and(baseClause, root.get(Delivery_.id).in(selectedIds)));
		} else {
			cq = criteria.where(baseClause);
		}

		return getEntityManager().createQuery(cq).getResultList();
	}
    
    /**
     * Finds all {@link Delivery} documents without an invoice (should be used for
     * {@link SelectDeliveryNoteDialog}). 
     * 
     * @return List of {@link Delivery} documents
     */
    public List<Delivery> findAllDeliveriesWithoutInvoice() {
    	return findSelectedDeliveries(null);
    }

    /**
     * Find all printed documents. This is relevant for reorganizing documents.
	 * @return
	 */
    public List<Document> findAllPrintedDocuments() {
    	List<Document> resultList;
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteria = cb.createQuery(getEntityClass());
        Root<Document> root = criteria.from(getEntityClass());
        CriteriaQuery<Document> cq = criteria.where(
        		cb.and(cb.notEqual(root.get(Document_.deleted), Boolean.TRUE),
        				cb.or(cb.notEqual(root.get(Document_.odtPath), ' '), cb.notEqual(root.get(Document_.pdfPath), ' '))
        		));
        TypedQuery<Document> query = getEntityManager().createQuery(cq);
    	try {
    		resultList = query.getResultList();
    	} catch (PersistenceException e) {
    		resultList = Collections.emptyList();
    	}
    	return resultList;

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
        if(!importedDeliveryNotes.isEmpty()) {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaUpdate<Delivery> criteria = cb.createCriteriaUpdate(Delivery.class);
            Root<Delivery> root = criteria.from(Delivery.class);
            criteria.set(Delivery_.invoiceReference, document).where(root.get(Delivery_.id).in(importedDeliveryNotes));
            executeCriteria(criteria);
            mergeTwoTransactions(document, importedDeliveryNotes);
        }
    }
 
    /**
     * Merge 2 transactions into a single one
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
     * Merge 2 transactions into a single one for a given List of {@link Document}s.
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
     * Search for all documents with the same number
     * 
     * @param transaction
     * @return
     */
    public List<Document> findByTransactionId(Integer transaction) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteria = cb.createQuery(Document.class);
        Root<Document> root = criteria.from(Document.class);
        CriteriaQuery<Document> cq = criteria.where(
                cb.equal(root.<Integer>get(Document_.transactionId), transaction));
        return getEntityManager().createQuery(cq).getResultList();
    }
    
    /**
     * Returns a string with all documents with the same transaction
     *  
     * @param docType
     *      Only those documents will be returned
     * @return
     *      String with the document names
     */
    public String getReference(Integer transaction, DocumentType docType) {
        BillingType billingType = BillingType.get(docType.getKey());
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Document> criteria = cb.createQuery(Document.class);
        Root<Document> root = criteria.from(Document.class);
        CriteriaQuery<Document> cq = criteria.where(
                cb.and(
                		cb.not(root.get(Document_.deleted)),
                        cb.equal(root.<BillingType>get(Document_.billingType), billingType),
                        cb.equal(root.<Integer>get(Document_.transactionId), transaction)));
        List<Document> resultList = getEntityManager().createQuery(cq).getResultList();
        List<String> stringList = resultList.stream().map(d -> d.getName()).collect(Collectors.toList());
        return StringUtils.join(stringList, ",");
    }

    /**
     * Calculates the sum of all document totals in a given {@link DummyStringCategory}. Used for
     * displaying tooltips.
     * 
     * @param category
     * @return sum of all document totals in the given category
     */
    public Optional<Double> sumAllDocumentsWithinCategory(DummyStringCategory category) {
        if(category.getDocType() != DocumentType.INVOICE) {
            return null;
        }
        
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Double> query = cb.createQuery(Double.class);
        Root<Invoice> root = query.from(Invoice.class);
        CriteriaQuery<Double> cq = query.select(cb.sum(root.get(Document_.totalValue)));
        
        Predicate predicate;
        if(category.getName().contentEquals(msg.getMessageFromKey(DocumentType.getPluralString(DocumentType.INVOICE)))) {
            // sum paid and unpaid invoices
            predicate = cb.not(root.get(Document_.deleted));
        } else {
            // only paid or unpaid invoices
            predicate = cb.and(
                cb.not(root.get(Document_.deleted)),
                cb.equal(root.<Boolean> get(Invoice_.paid), !category.getName().contentEquals(msg.documentOrderStateUnpaid))
            );
        }
        
        CriteriaQuery<Double> cq1 = cq.where(predicate);
        return Optional.ofNullable(getEntityManager().createQuery(cq1).getSingleResult());
	}


    /**
     * Finds all {@link Document}s within a given date range (or any document if no date is given).
     * Only unpaid {@link BillingType#INVOICE} and {@link BillingType#CREDIT} are taken into account.
     * 
     * @param usePaidDate use "paid date" (<code>true</code>) or use "document date" (<code>false</code>)
     * @param startDate the start of the date range to retrieve (or <code>null</code>)
     * @param endDate the end of the date range to retrieve (or <code>null</code>)
     * @return List of {@link Document}s (sort by date according to <tt>usePaidDate</tt>)
     */
	public List<Document> findUnpaidDocumentsInRange(boolean usePaidDate, Date startDate,
			Date endDate) {
		return findPaidOrUnpaidDocumentsInRange(usePaidDate, startDate, endDate, false);
	}    

    /**
     * Finds all {@link Document}s within a given date range (or any document if no date is given).
     * Only paid {@link BillingType#INVOICE} and {@link BillingType#CREDIT} are taken into account.
     * 
     * @param usePaidDate use "paid date" (<code>true</code>) or use "document date" (<code>false</code>)
     * @param startDate the start of the date range to retrieve (or <code>null</code>)
     * @param endDate the end of the date range to retrieve (or <code>null</code>)
     * @return List of {@link Document}s (sort by date according to <tt>usePaidDate</tt>)
     */
	public List<Document> findPaidDocumentsInRange(boolean usePaidDate, Date startDate,
			Date endDate) {
		return findPaidOrUnpaidDocumentsInRange(usePaidDate, startDate, endDate, true);
	}
	
	private List<Document> findPaidOrUnpaidDocumentsInRange(boolean usePaidDate, Date startDate,
			Date endDate, boolean paidFlag) {
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	    CriteriaQuery<Document> criteria = cb.createQuery(getEntityClass());
	    Root<Document> root = criteria.from(getEntityClass());
	    Predicate predicate = cb.and(
				cb.not(root.get(Document_.deleted)),
				cb.or(
						cb.equal(root.get(Document_.billingType), BillingType.INVOICE),
						cb.equal(root.get(Document_.billingType), BillingType.CREDIT)
					  )
		);
	    
		if (paidFlag) {
			predicate = cb.and(predicate, cb.equal(root.get(Document_.paid), paidFlag));
		} else {
			// unpaid documents could have a null paid flag
			predicate = cb.and(predicate,
							cb.or(
									cb.isNull(root.get(Document_.paid)), 
									cb.equal(root.get(Document_.paid), paidFlag)));
		}
	    
		if (startDate != null && endDate != null) {
			// if startDate is after endDate we switch the two dates silently
			predicate = cb.and(predicate,
					cb.between(root.get(usePaidDate ? Document_.payDate : Document_.documentDate),
							startDate.before(endDate) ? startDate : endDate,
							endDate.after(startDate) ? endDate : startDate));
		}
	    // take the paydate OR the document date into account
		CriteriaQuery<Document> cq = criteria.where(predicate).orderBy(
				cb.asc(root.get(usePaidDate ? Document_.payDate : Document_.documentDate)));
	    return getEntityManager().createQuery(cq).getResultList();
	}
	
	public Document findDunningByTransactionId(Integer transactionId, int dunninglevel) {
		Document retval = null;
		if(transactionId != null) {
	        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	        CriteriaQuery<Dunning> criteria = cb.createQuery(Dunning.class);
	        Root<Dunning> root = criteria.from(Dunning.class);
	        CriteriaQuery<Dunning> cq = criteria.where(
	        		cb.and(
							cb.equal(root.<Integer>get(Dunning_.transactionId), transactionId),
							cb.equal(root.<BillingType>get(Dunning_.billingType), BillingType.DUNNING),
							cb.not(root.<Boolean>get(Dunning_.deleted)),
						// check for dunnings
							cb.equal(root.<Integer>get(Dunning_.dunningLevel), (dunninglevel > 0) ? dunninglevel : Integer.valueOf(1))));
	        try {
				retval = getEntityManager().createQuery(cq).getSingleResult();
			} catch (NoResultException e) {
				// is ok, we have to return a null value
			}
		}
		return retval;
	}
	

	/**
	 * Finds a document by its transaction id and billing type. Returns <code>null</code> if none is found. 
	 * 
	 * @param transactionId the transaction id to which the document belongs
	 * @param targetype the type of document which is to be searched
	 * @param dunninglevel the dunning level to prove
	 * @throws FakturamaStoringException 
	 */
	public Document findExistingDocumentByTransactionIdAndBillingType(Integer transactionId, BillingType targetype) {
		Document retval = null;
		if(transactionId != null && targetype != null) {
	        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	        CriteriaQuery<Document> criteria = cb.createQuery(getEntityClass());
	        Root<Document> root = criteria.from(getEntityClass());
	        Predicate whereClause = cb.and(
					cb.equal(root.<Integer>get(Document_.transactionId), transactionId),
					cb.equal(root.<BillingType>get(Document_.billingType), targetype),
					cb.not(root.<Boolean>get(Document_.deleted))
				);
			CriteriaQuery<Document> cq = criteria.where(
	        		whereClause);
	        try {
				List<Document> result = getEntityManager().createQuery(cq).getResultList();
				retval = !result.isEmpty() && result.size() > 0 ? result.get(0) : null;
			} catch (NoResultException e) {
				// is ok, we have to return a null value
			}
		}
		return retval;
	}

//	public Set<Long> saveBatch(List<Document> resultList) throws FakturamaStoringException {
//		Set<Long> documentIds = new HashSet<>();
//		Set<Document> docSet = new HashSet<>();
//		Document lastSuccessfulObject = null;
//		try {
//			checkConnection();
//			EntityManager entityManager = getEntityManager();
//			entityManager.setProperty(PersistenceUnitProperties.BATCH_WRITING, BatchWriting.JDBC);
//			entityManager.setProperty(PersistenceUnitProperties.BATCH_WRITING_SIZE, 20);
//			EntityTransaction trx = entityManager.getTransaction();
//			trx.begin();
//			for (Document doc : resultList) {
//				lastSuccessfulObject = entityManager.merge(doc);
//				getEntityManager().persist(lastSuccessfulObject);
//				// documentIds.add(currentDocument.getId());
////				System.out.println("t");
//				docSet.add(lastSuccessfulObject);
//			}
//			trx.commit();
//		} catch (SQLException e) {
//			throw new FakturamaStoringException("Error saving to the database.", e, lastSuccessfulObject);
//		}
//		documentIds = docSet.stream().map(d -> d.getId()).collect(Collectors.toSet());
//		return documentIds;
//	}
//	
//	
	
	
}
