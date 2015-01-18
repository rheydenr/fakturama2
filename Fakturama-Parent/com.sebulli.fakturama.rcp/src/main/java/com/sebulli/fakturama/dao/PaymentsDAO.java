package com.sebulli.fakturama.dao;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Payment_;
import com.sebulli.fakturama.oldmodel.OldPayments;

@Creatable
public class PaymentsDAO extends AbstractDAO<Payment> {

    @Inject
    @GeminiPersistenceContext(unitName = "unconfigured2", properties = {
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference(PersistenceUnitProperties.JDBC_DRIVER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference(PersistenceUnitProperties.JDBC_URL)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, valuePref = @Preference(PersistenceUnitProperties.JDBC_USER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, valuePref = @Preference(PersistenceUnitProperties.JDBC_PASSWORD)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.LOGGING_LEVEL, value = "INFO"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
//    @GeminiPersistenceContext(unitName = "mysql-datasource")
//    @GeminiPersistenceContext(unitName = "origin-datasource")
    private EntityManager em;

    protected Class<Payment> getEntityClass() {
    	return Payment.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }

	/**
	 * Finds a {@link Payment} by a given {@link OldPayments}.
	 * 
	 * @param oldVat
	 * @return
	 */
	public Payment findByOldPayment(OldPayments oldPayment) {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<Payment> criteria = cb.createQuery(Payment.class);
    	Root<Payment> root = criteria.from(Payment.class);
		CriteriaQuery<Payment> cq = criteria.where(
				cb.and(
						cb.equal(root.get(Payment_.description), oldPayment.getDescription()),
						cb.equal(root.get(Payment_.name), oldPayment.getName())));
    	return getEntityManager().createQuery(cq).getSingleResult();
	}
	
	@Override
	protected Set<Predicate> getRestrictions(Payment object, CriteriaBuilder criteriaBuilder, Root<Payment> root) {
        Set<Predicate> restrictions = new HashSet<>();
        /*
         * Only the names are compared.
         */
        restrictions.add(criteriaBuilder.equal(root.get(Payment_.name), StringUtils.defaultString(object.getName())));
        return restrictions;
	}

    /**
     * Gets the all visible properties of this Shipping object.
     * 
     * @return String[] of visible Shipping properties
     */
    public String[] getVisibleProperties() {
        return new String[] { Payment_.name.getName(), Payment_.description.getName(), 
                Payment_.discountValue.getName(), Payment_.discountDays.getName(), Payment_.netDays.getName() };
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
}
