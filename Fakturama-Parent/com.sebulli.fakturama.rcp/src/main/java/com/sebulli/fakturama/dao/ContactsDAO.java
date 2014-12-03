package com.sebulli.fakturama.dao;

import java.util.Collection;
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

import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Contact_;
import com.sebulli.fakturama.oldmodel.OldContacts;

@Creatable
public class ContactsDAO extends AbstractDAO<Contact> {

    @Inject
    @GeminiPersistenceContext(unitName = "unconfigured2", properties = {
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference(PersistenceUnitProperties.JDBC_DRIVER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference(PersistenceUnitProperties.JDBC_URL)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, valuePref = @Preference(PersistenceUnitProperties.JDBC_USER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, valuePref = @Preference(PersistenceUnitProperties.JDBC_PASSWORD)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
    private EntityManager em;

    @PreDestroy
    public void destroy() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
//    /**
//     * Finds all {@link Contact}s (only "main" contacts, no delivery contacts!)
//     */
//    @Override
//    public List<Contact> findAll() {
//        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
//        CriteriaQuery<Contact> criteria = cb.createQuery(Contact.class);
//        Root<Contact> root = criteria.from(Contact.class);
//        CriteriaQuery<Contact> cq = criteria.where(
//                cb.and(
//                        cb.equal(root.<String>get(Contact_.firstName), oldContact.getFirstname()),
//                        cb.equal(root.<String>get(Contact_.name), oldContact.getName())));
//        return getEntityManager().createQuery(cq).getSingleResult();
//        
//        return super.findAll();
//    }

    /**
     * Get a list of all categories stored for {@link Contact}s.
     * 
     * @return list of all categories
     */
	public Collection<String> getCategoryStrings() {
		List<String> result = em.createQuery("select distinct c.category from Contact c where c.deleted = false", String.class).getResultList();
		return result;
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Class<Contact> getEntityClass() {
		return Contact.class;
	}
	
	public Contact findByOldContact(OldContacts oldContact) {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<Contact> criteria = cb.createQuery(Contact.class);
    	Root<Contact> root = criteria.from(Contact.class);
		CriteriaQuery<Contact> cq = criteria.where(
				cb.and(
						cb.equal(root.<String>get(Contact_.firstName), oldContact.getFirstname()),
						cb.equal(root.<String>get(Contact_.name), oldContact.getName())));
    	return getEntityManager().createQuery(cq).getSingleResult();
	}
	
    /**
     * Gets the all visible properties of this VAT object.
     * 
     * @return String[] of visible VAT properties
     */
    public String[] getVisibleProperties() {
        return new String[] { Contact_.customerNumber.getName(), Contact_.firstName.getName(), Contact_.name.getName(),
                Contact_.company.getName(), Contact_.address.getName() + "." +Address_.zip.getName(), Contact_.address.getName() + "." +Address_.city.getName()};
    }	
}
