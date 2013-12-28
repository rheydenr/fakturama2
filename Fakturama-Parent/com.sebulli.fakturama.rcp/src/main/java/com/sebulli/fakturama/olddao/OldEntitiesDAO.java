package com.sebulli.fakturama.olddao;

import java.util.Collection;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.oldmodel.OldContacts;
import com.sebulli.fakturama.oldmodel.OldList;
import com.sebulli.fakturama.oldmodel.OldProperties;
import com.sebulli.fakturama.oldmodel.OldShippings;
import com.sebulli.fakturama.oldmodel.OldTexts;
import com.sebulli.fakturama.oldmodel.OldVats;

/**
 * DAO for the old entites. This dao is for ALL old entities, since we use it
 * only for migration and therefore we only need some basic finder methods.
 * 
 * @author R. Heydenreich
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
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.LOGGING_LEVEL, value = "FINE"),
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
		return em.createQuery("select c from OldContacts c where c.deleted = false", OldContacts.class).getResultList();
	}
	
	public Long countAllContacts() {
		return em.createQuery("select count(c) from OldContacts c where c.deleted = false", Long.class).getSingleResult();
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
	public Collection<String> getContactCategoryStrings() {
		List<String> result = em.createQuery("select distinct c.category from OldContacts c where c.deleted = false and c.category <> ''", String.class).getResultList();
		return result;
	}

	
	/* * * * * * * * * * * * * * * * * * [Properties] * * * * * * * * * * * * * * * * * * * * * */ 

	public Long countAllProperties() {
		return em.createQuery("select count(p) from OldProperties p", Long.class).getSingleResult();
	}
	
	public List<OldProperties> findAllProperties() {
		return em.createQuery("select c from OldProperties c", OldProperties.class).getResultList();
	}
	
	/* * * * * * * * * * * * * * * * * * [Shippings] * * * * * * * * * * * * * * * * * * * * * */ 
	
	public Long countAllShippings() {
		return em.createQuery("select count(s) from OldShippings s", Long.class).getSingleResult();
	}
	
	public List<OldShippings> findAllShippings() {
		return em.createQuery("select s from OldShippings s", OldShippings.class).getResultList();
	}
	
	public List<String> findAllShippingCategories() {
		return em.createQuery("select distinct s.category from OldShippings s where s.deleted = false and  s.category <> ''", String.class).getResultList();
	}
	
	/* * * * * * * * * * * * * * * * * * [VATs] * * * * * * * * * * * * * * * * * * * * * */ 

	public Long countAllVats() {
		return em.createQuery("select count(t) from OldVats t", Long.class).getSingleResult();
	}
	
	public List<OldVats> findAllVats() {
		return em.createQuery("select v from OldVats v", OldVats.class).getResultList();
	}

	public List<String> findAllVatCategories() {
		return em.createQuery("select distinct s.category from OldVats s where s.deleted = false and s.category <> ''", String.class).getResultList();
	}
	
	/* * * * * * * * * * * * * * * * * * [Lists] * * * * * * * * * * * * * * * * * * * * * */ 
	
	public Long countAllLists() {
		return em.createQuery("select count(l) from OldList l", Long.class).getSingleResult();
	}
	
	public List<OldList> findAllCountryCodes() {
		// SELECT * FROM "PUBLIC"."LIST" where category like 'country%' order by value, name
		return em.createQuery("select l from OldList l where l.deleted = false and l.category like 'country%' order by l.value, l.id", OldList.class).getResultList();
	}
	
	public List<String> findAllListCategories() {
		return em.createQuery("select distinct s.category from OldList s where s.deleted = false and s.category <> ''", String.class).getResultList();
	}
	
	/* * * * * * * * * * * * * * * * * * [Texts] * * * * * * * * * * * * * * * * * * * * * */ 

	public Long countAllTexts() {
		return em.createQuery("select count(t) from OldTexts t", Long.class).getSingleResult();
	}
	
	public List<OldTexts> findAllTexts() {
		return em.createQuery("select v from OldTexts v", OldTexts.class).getResultList();
	}

	public List<String> findAllTextCategories() {
		return em.createQuery("select distinct s.category from OldTexts s where s.deleted = false and s.category <> ''", String.class).getResultList();
	}


	/* * * * * * * * * * * * * * * * * * [Documents section] * * * * * * * * * * * * * * * * * * * * * */ 


}
