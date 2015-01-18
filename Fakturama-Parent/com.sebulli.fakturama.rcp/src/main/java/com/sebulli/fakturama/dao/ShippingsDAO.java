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

import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.Shipping_;
import com.sebulli.fakturama.oldmodel.OldShippings;

@Creatable
public class ShippingsDAO extends AbstractDAO<Shipping> {

	// calculate the shipping's vat with a fix vat value
	public static final int SHIPPINGVATFIX = 0;
	// calculate the shipping's vat with the same vat of the items. The shipping vat is a gross value.
	public static final int SHIPPINGVATGROSS = 1;
	// calculate the shipping's vat with the same vat of the items. The shipping vat is a net value.
	public static final int SHIPPINGVATNET = 2;

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

    protected Class<Shipping> getEntityClass() {
    	return Shipping.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }

    public Shipping findByOldShipping(OldShippings oldShipping) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Shipping> criteria = cb.createQuery(Shipping.class);
        Root<Shipping> root = criteria.from(Shipping.class);
        CriteriaQuery<Shipping> cq = criteria.where(cb.and(cb.equal(root.get(Shipping_.description), oldShipping.getDescription()),
                cb.equal(root.get(Shipping_.name), oldShipping.getName())));
        return getEntityManager().createQuery(cq).getSingleResult();
    }
    
    @Override
    protected Set<Predicate> getRestrictions(Shipping object, CriteriaBuilder criteriaBuilder, Root<Shipping> root) {
        Set<Predicate> restrictions = new HashSet<>();
        /*
         * Only the names and the values are compared.
         */
        restrictions.add(criteriaBuilder.equal(root.get(Shipping_.name), StringUtils.defaultString(object.getName())));
        restrictions.add(criteriaBuilder.equal(root.get(Shipping_.shippingValue), object.getShippingValue() != null ? object.getShippingValue() : Double.valueOf(0.0)));
        return restrictions;
    }

    /**
     * Gets the all visible properties of this Shipping object.
     * 
     * @return String[] of visible Shipping properties
     */
    public String[] getVisibleProperties() {
        return new String[] { Shipping_.name.getName(), Shipping_.description.getName(), Shipping_.shippingValue.getName() };
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
