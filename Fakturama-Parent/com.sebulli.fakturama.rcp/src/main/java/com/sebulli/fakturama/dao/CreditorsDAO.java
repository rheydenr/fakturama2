package com.sebulli.fakturama.dao;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Creditor_;

@Creatable
public class CreditorsDAO extends AbstractDAO<Creditor> {

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
    
    @Override
    protected Set<Predicate> getRestrictions(Creditor object, CriteriaBuilder cb, Root<Creditor> root) {
        /* Customer number, first
         * name, name and ZIP are compared. Customer number is only compared, if it
         * is set.
         */
        Set<Predicate> restrictions = new HashSet<>();
        // Compare customer number, only if it is set.
        if(StringUtils.isNotBlank(object.getCustomerNumber())) {
            restrictions.add(cb.equal(root.get(Creditor_.customerNumber), object.getCustomerNumber()));
        }
        // if the value is not set (null), then we use the empty String for comparison. 
        // Then we get no result (which is correct).
        restrictions.add(cb.equal(root.get(Creditor_.firstName), StringUtils.defaultString(object.getFirstName())));
        restrictions.add(cb.equal(root.get(Creditor_.name), StringUtils.defaultString(object.getName())));
        if (object.getAddress() != null) {
            restrictions.add(cb.equal(root.get(Creditor_.address).get(Address_.zip), StringUtils.defaultString(object.getAddress().getZip())));
        } else {
            // set to an undefined value so we get no result (then the contact is not found in the database)
            restrictions.add(cb.equal(root.get(Creditor_.address).get(Address_.zip), "-1"));
        }
        return restrictions;
    }

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Class<Creditor> getEntityClass() {
		return Creditor.class;
	}
	
    /**
     * Gets the all visible properties of this VAT object.
     * 
     * @return String[] of visible VAT properties
     */
    public String[] getVisibleProperties() {
        return new String[] { Creditor_.customerNumber.getName(), Creditor_.firstName.getName(), Creditor_.name.getName(),
                Creditor_.company.getName(), Creditor_.address.getName() + "." +Address_.zip.getName(), Creditor_.address.getName() + "." +Address_.city.getName()};
    }
}
