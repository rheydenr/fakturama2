package com.sebulli.fakturama.migration.olddao;

import java.util.List;
import java.util.Locale;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.oldmodel.OldContacts;
import com.sebulli.fakturama.oldmodel.OldDocuments;
import com.sebulli.fakturama.oldmodel.OldExpenditureitems;
import com.sebulli.fakturama.oldmodel.OldExpenditures;
import com.sebulli.fakturama.oldmodel.OldItems;
import com.sebulli.fakturama.oldmodel.OldList;
import com.sebulli.fakturama.oldmodel.OldPayments;
import com.sebulli.fakturama.oldmodel.OldProducts;
import com.sebulli.fakturama.oldmodel.OldProperties;
import com.sebulli.fakturama.oldmodel.OldReceiptvoucheritems;
import com.sebulli.fakturama.oldmodel.OldReceiptvouchers;
import com.sebulli.fakturama.oldmodel.OldShippings;
import com.sebulli.fakturama.oldmodel.OldTexts;
import com.sebulli.fakturama.oldmodel.OldVats;

/**
 * DAO for the old entities. This DAO is for <i>all</i> old entities, since we use it
 * only for migration and therefore we only need some basic finder methods.
 * 
 */
@Creatable
public class OldEntitiesDAO {

	@Inject
	@GeminiPersistenceContext(unitName = "origin-datasource", properties = {
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference("OLD_JDBC_URL")),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, value = "org.hsqldb.jdbc.JDBCDriver"),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, value = "sa"),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, value = ""),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.LOGGING_LEVEL, value = "INFO"),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
	private EntityManager em;

	@PreDestroy
	public void destroy() {
		if (em != null && em.isOpen()) {
			em.close();
		}
	}
	
	/* * * * * * * * * * * * * * * * * * [Contacts section] * * * * * * * * * * * * * * * * * * * * * */ 

	/**
	 * Get all {@link Contact} from Database which are not deleted.
	 * 
	 * @return List<Contact>
	 */
	public List<OldContacts> findAllContacts() {
		// Use only the undeleted entries
		return em.createQuery("select c from OldContacts c where c.deleted = false", OldContacts.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}
	
	public Long countAllContacts() {
		return em.createQuery("select count(c) from OldContacts c where c.deleted = false", Long.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getSingleResult();
	}

	/**
	 * Finds a {@link Contact} by id.
	 * 
	 * @param id
	 * @return
	 */
	public OldContacts findContactById(int id) {
		return em.find(OldContacts.class, id);
	}

	/**
	 * Get a list of all categories stored for {@link Contact}s.
	 * 
	 * @return list of all categories
	 */
	public List<String> findAllContactCategories() {
		List<String> result = em.createQuery("select distinct c.category from OldContacts c where c.deleted = false and c.category <> ''", String.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
		return result;
	}

	
	/* * * * * * * * * * * * * * * * * * [Properties] * * * * * * * * * * * * * * * * * * * * * */ 

	public Long countAllProperties() {
		return em.createQuery("select count(p) from OldProperties p", Long.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getSingleResult();
	}
	
	public List<OldProperties> findAllProperties() {
		return em.createQuery("select p from OldProperties p", OldProperties.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}
	
	public OldProperties findPropertyById(int id) {
	    return em.find(OldProperties.class, id);
	}
	
	public List<OldProperties> findAllPropertiesWithoutColumnWidthProperties() {
	    return em.createQuery("select p from OldProperties p where p.name not like 'COLUMNWIDTH_%' order by p.name", OldProperties.class)
	    		.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}
	
	public List<OldProperties> findAllColumnWidthProperties() {
		em.setProperty("eclipselink.read-only", true);
	    return em.createQuery("select p from OldProperties p where p.name like 'COLUMNWIDTH_%' order by p.name", OldProperties.class)
	    		.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}
	
	
	/* * * * * * * * * * * * * * * * * * [Shippings] * * * * * * * * * * * * * * * * * * * * * */ 
	
	public Long countAllShippings() {// where s.deleted = false
		return em.createQuery("select count(s) from OldShippings s", Long.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getSingleResult();
	}
	
	public List<OldShippings> findAllShippings() { // where s.deleted = false
		return em.createQuery("select s from OldShippings s", OldShippings.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}
	
    /**
     * Finds all Shipping categories from non-deleted old Shipping entries. They are in the form of "/root/cat1/cat2".
     * 
     * @return List of Strings with all old Shipping categories
     */
	public List<String> findAllShippingCategories() {
		return em.createQuery("select distinct s.category from OldShippings s where s.deleted = false and s.category <> ''", String.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}
	
	public OldShippings findShippingById(int shippingId) {
	    return em.find(OldShippings.class, shippingId);
	}
	/* * * * * * * * * * * * * * * * * * [VATs] * * * * * * * * * * * * * * * * * * * * * */ 

	public Long countAllVats() {// where v.deleted = false
		return em.createQuery("select count(v) from OldVats v", Long.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getSingleResult();
	}
	
	public List<OldVats> findAllVats() {// where v.deleted = false
		return em.createQuery("select v from OldVats v", OldVats.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}

	/**
	 * Finds all VAT categories from non-deleted old VAT entries. They are in the form of "/root/cat1/cat2".
	 * 
	 * @return List of Strings with all old VAT categories
	 */
	public List<String> findAllVatCategories() {
		return em.createQuery("select distinct v.category from OldVats v where v.deleted = false and v.category <> ''", String.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}

	public OldVats findVatById(int vatid) {
		return em.find(OldVats.class, vatid);
	}
	
	/* * * * * * * * * * * * * * * * * * [Lists] * * * * * * * * * * * * * * * * * * * * * */ 
	
	public Long countAllLists() {
		return em.createQuery("select count(l) from OldList l where l.deleted = false", Long.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getSingleResult();
	}
	
	public List<OldList> findAllCountryCodes() {
		// SELECT * FROM "PUBLIC"."LIST" where category like 'country%' order by value, name
		return em.createQuery("select l from OldList l where l.deleted = false and l.category like 'country%' order by l.value, l.id", OldList.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}
	
	/**
	 * Finds all entries from {@link OldList} which represent an account. {@link OldList} also
	 * contains the country codes for all countries (ISO codes). These codes are not converted because
	 * we use the country code information from {@link Locale} class.<br>
	 * We can select all (other) entries because in the old Fakturama application <i>each</i> List entry
	 * (which is not a country code entry) has a category named 'billing_accounts'. Fakturama doesn't accept
	 * user defined categories in this area. <br>
	 * Therefore we can select these entries according to this category. <br>
	 * The accounts are used in Payments, ReceiptVouchers and ExpenditureVouchers.
	 * 
	 * @return 
	 */
	public List<OldList> findAllAccounts() {
		return em.createQuery("select a from OldList a where a.category = 'billing_accounts'", 
		        OldList.class).setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}
	
	/* * * * * * * * * * * * * * * * * * [Texts] * * * * * * * * * * * * * * * * * * * * * */ 

	public Long countAllTexts() {
		return em.createQuery("select count(t) from OldTexts t where t.deleted = false", Long.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getSingleResult();
	}
	
	public List<OldTexts> findAllTexts() {
		return em.createQuery("select t from OldTexts t where t.deleted = false", OldTexts.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}

	public List<String> findAllTextCategories() {
		return em.createQuery("select distinct t.category from OldTexts t where t.deleted = false and t.category <> ''", String.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}


	/* * * * * * * * * * * * * * * * * * [Documents section] * * * * * * * * * * * * * * * * * * * * * */ 
	public Long countAllDocuments() {
		return em.createQuery("select count(d) from OldDocuments d where d.deleted = false", Long.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getSingleResult();
	}
	
	public List<OldDocuments> findAllDocuments() {
		return em.createQuery("select d from OldDocuments d where d.deleted = false", OldDocuments.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}

	// there are no categories...
	
	/**
	 * Finds all documents which are invoice related.
	 * @return
	 */
	public List<OldDocuments> findAllInvoiceRelatedDocuments() {
		return em.createQuery("select d from OldDocuments d where d.deleted = false and d.invoiceid >= 0 and d.invoiceid <> d.id ", OldDocuments.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getResultList();
	}

	/**
	 * Finds an {@link OldDocuments} by its ID.
	 * @param id
	 */
	public OldDocuments findDocumentById(int id) {
		TypedQuery<OldDocuments> query = em.createQuery("select d from OldDocuments d where d.id = :id ", OldDocuments.class);
		query.setHint(QueryHints.READ_ONLY, HintValues.TRUE).setParameter("id", id);
		return query.getSingleResult();
	}

	/* * * * * * * * * * * * * * * * * * [Document items section] * * * * * * * * * * * * * * * * * * * * * */

	public OldItems findDocumentItem(int id) {
		TypedQuery<OldItems> query = em.createQuery("select oi from OldItems oi where oi.id = :id ", OldItems.class);
		query.setHint(QueryHints.READ_ONLY, HintValues.TRUE).setParameter("id", id);
		return query.getSingleResult();
	}
	
//  not used!
//	public List<String> findAllDocumentItemCategories() {
//		return em.createQuery("select distinct oi.category from OldItems oi where oi.deleted = false and oi.category <> ''", String.class).getResultList();
//	}

	/* * * * * * * * * * * * * * * * * * [Payments section] * * * * * * * * * * * * * * * * * * * * * */ 

	public Long countAllPayments() {// where p.deleted = false
		return em.createQuery("select count(p) from OldPayments p", Long.class)
				.setHint(QueryHints.READ_ONLY, HintValues.TRUE).getSingleResult();
	}
	
	public List<OldPayments> findAllPayments() {// where p.deleted = false
		return em.createQuery("select p from OldPayments p", OldPayments.class).getResultList();
	}
    
    /**
     * Finds all Payment categories from non-deleted old Payment entries. They are in the form of "/root/cat1/cat2".
     * 
     * @return List of Strings with all old Payment categories
     */
	public List<String> findAllPaymentCategories() {
		return em.createQuery("select distinct p.category from OldPayments p where p.deleted = false and p.category <> ''", String.class).getResultList();
	}

	public OldPayments findPaymentById(int paymentId) {
		return em.find(OldPayments.class, paymentId);
	}
	
	/* * * * * * * * * * * * * * * * * * [Expenditures section] * * * * * * * * * * * * * * * * * * * * * */ 
	
	public Long countAllExpenditures() {
		return em.createQuery("select count(e) from OldExpenditures e where e.deleted = false", Long.class).getSingleResult();
	}
	
	public List<OldExpenditures> findAllExpenditures() {
		return em.createQuery("select e from OldExpenditures e where e.deleted = false", OldExpenditures.class).getResultList();
	}
	
	public List<String> findAllExpenditureVoucherCategories() {
		return em.createQuery("select distinct e.category from OldExpenditures e where e.deleted = false and e.category <> ''", String.class).getResultList();
	}

	/**
	 * Finds an expenditure item per reference number.
	 * 
	 * @param itemRef
	 * @return {@link OldExpenditureitems}
	 */
	public OldExpenditureitems findExpenditureItem(String itemRef) {
		return em.find(OldExpenditureitems.class, Integer.valueOf(itemRef));
	}
	
	/* * * * * * * * * * * * * * * * * * [Receiptvouchers section] * * * * * * * * * * * * * * * * * * * * * */ 
	
	public Long countAllReceiptvouchers() {
		return em.createQuery("select count(r) from OldReceiptvouchers r where r.deleted = false", Long.class).getSingleResult();
	}
	
	public List<OldReceiptvouchers> findAllReceiptvouchers() {
		return em.createQuery("select r from OldReceiptvouchers r where r.deleted = false", OldReceiptvouchers.class).getResultList();
	}
	
	public List<String> findAllReceiptvoucherCategories() {
		return em.createQuery("select distinct r.category from OldReceiptvouchers r where r.deleted = false and r.category <> ''", String.class).getResultList();
	}

	/**
	 * Finds all voucher item categories. These are located in the old LIST table, therefore
	 * we select the values from there and not from Receipt/Expenditure value tables. The 
	 * category is 'billing_accounts' because only these entries are used for item accounts.
	 * 
	 * @return List of distinct accounts for voucher items
	 */
	public List<OldList> findAllVoucherItemCategories() {
		return em.createQuery("select distinct vi from OldList vi where vi.category = 'billing_accounts'", OldList.class).getResultList();
	}

	/**
	 * Finds a {@link OldReceiptvoucheritems} object by its id. 
	 * 
	 * @param itemRef id of {@link OldReceiptvoucheritems} object
	 * @return the {@link OldReceiptvoucheritems} object 
	 */
	public OldReceiptvoucheritems findReceiptvoucherItem(String itemRef) {
		return em.find(OldReceiptvoucheritems.class, Integer.valueOf(itemRef));
	}

	
	/* * * * * * * * * * * * * * * * * * [Products section] * * * * * * * * * * * * * * * * * * * * * */
	
	public Long countAllProducts() {
		return em.createQuery("select count(p) from OldProducts p where p.deleted = false", Long.class).getSingleResult();
	}
	
	public List<OldProducts> findAllProducts() {
		return em.createQuery("select p from OldProducts p where p.deleted = false", OldProducts.class).getResultList();
	}
	
    /**
     * Finds all Product categories from non-deleted old Product entries. They are in the form of "/root/cat1/cat2".
     * 
     * @return List of Strings with all old Product categories
     */
	public List<String> findAllProductCategories() {
		return em.createQuery("select distinct p.category from OldProducts p where p.deleted = false and p.category <> ''", String.class).getResultList();
	}

}
