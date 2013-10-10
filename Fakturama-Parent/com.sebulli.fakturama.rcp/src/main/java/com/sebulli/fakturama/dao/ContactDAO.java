package com.sebulli.fakturama.dao;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.dto.ContactsDataSet;
import com.sebulli.fakturama.model.Contacts;
import com.sebulli.fakturama.model.IDataSetArray;

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

    public void save(Contacts dataObj) throws SQLException {
        checkConnection();
        EntityTransaction trx = em.getTransaction();
        trx.begin();
        em.persist(dataObj);
        trx.commit();
    }

    @PreDestroy
    public void destroy() {
        if (em != null) {
            em.close();
        }
    }
    
    public List<Contacts> getContacts() {
    	// Use only the undeleted entries
    	return em.createQuery("select c from Contacts c where c.deleted = false").getResultList();
    }

    private void checkConnection() throws SQLException {
        if (em == null) {
            throw new SQLException("EntityManager is null. Not connected to database!");
        }
    }

    /**
     * Get all {@link Contacts} from Database which are not deleted.
     *
     * @return IDataSetArray<Contacts> 
     */
	public IDataSetArray<Contacts> getContactsDataSet() {
		Query query = em.createQuery("select c from Contacts c where deleted = false");
		IDataSetArray<Contacts> retval = new ContactsDataSet(query.getResultList());
		return retval;
	}
}
