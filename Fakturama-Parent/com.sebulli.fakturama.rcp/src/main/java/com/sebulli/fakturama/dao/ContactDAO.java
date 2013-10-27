package com.sebulli.fakturama.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;

import com.sebulli.fakturama.model.Contact;

@Creatable
public class ContactDAO {

    @Inject
//    @GeminiPersistenceContext(unitName = "unconfigured2", properties = {
//            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference("jdbc_driver")),
//            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference("jdbc_url")),
//            @GeminiPersistenceProperty(name = PersistenceUnitProperties.LOGGING_LEVEL, value = "FINE"),
//            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
//            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
    @GeminiPersistenceContext(unitName = "origin-datasource")
    private EntityManager em;

    public Contact save(Contact contact) throws SQLException {
        checkConnection();
        EntityTransaction trx = em.getTransaction();
        trx.begin();
        em.persist(contact);
        trx.commit();
        return contact;
    }
    
    public Contact update(Contact contact) throws SQLException {
        checkConnection();
        EntityTransaction trx = em.getTransaction();
        trx.begin();
        em.merge(contact);
        trx.commit();
        return contact;
   	
    }

    @PreDestroy
    public void destroy() {
        if (em != null) {
            em.close();
        }
    }
    
    /**
     * Get all {@link Contact} from Database which are not deleted.
     *
     * @return List<Contact> 
     */
    public List<Contact> findAll() {
    	// Use only the undeleted entries
    	return em.createQuery("select c from Contacts c where c.deleted = false").getResultList();
    }
    
    public Contact findById(int id) {
    	return em.find(Contact.class, id);
    }
    
    private void checkConnection() throws SQLException {
        if (em == null) {
            throw new SQLException("EntityManager is null. Not connected to database!");
        }
    }

	public Collection<String> getCategoryStrings() {
		List<String> result = em.createQuery("select distinct c.category from Contacts c where c.deleted = false").getResultList();
		return result;
	}
}
