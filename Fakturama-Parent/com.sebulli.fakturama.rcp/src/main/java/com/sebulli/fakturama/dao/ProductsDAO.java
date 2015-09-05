package com.sebulli.fakturama.dao;

import java.util.HashSet;
import java.util.List;
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

import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.Product_;
import com.sebulli.fakturama.oldmodel.OldProducts;

@Creatable
public class ProductsDAO extends AbstractDAO<Product> {

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

    protected Class<Product> getEntityClass() {
    	return Product.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }

	/**
	 * Finds a {@link Product} by a given {@link OldProducts}.
	 * 
	 * @param oldVat
	 * @return
	 */
	public Product findByOldVat(OldProducts oldProduct) {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<Product> criteria = cb.createQuery(Product.class);
    	Root<Product> root = criteria.from(Product.class);
		CriteriaQuery<Product> cq = criteria.where(
				cb.and(
						cb.equal(root.<String>get(Product_.description), oldProduct.getDescription()),
						cb.equal(root.<String>get(Product_.name), oldProduct.getName())));
    	return getEntityManager().createQuery(cq).getSingleResult();
	}

    /**
     * @param object
     * @param cb
     * @param product
     * @return
     */
    protected Set<Predicate> getRestrictions(Product object, CriteriaBuilder cb, Root<Product> product) {
        Set<Predicate> restrictions = new HashSet<>();
        if (object.getWebshopId() != null && object.getWebshopId() > 0) {
            restrictions.add(cb.equal(product.get(Product_.webshopId), object.getWebshopId()));
        }
        restrictions.add(cb.equal(product.get(Product_.itemNumber), StringUtils.defaultString(object.getItemNumber())));
        restrictions.add(cb.equal(product.get(Product_.name), StringUtils.defaultString(object.getName())));
        return restrictions;
    }
	
	public Product findById(Product object) {
		return em.find(Product.class, object.getId());
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

    public List<Product> findSelectedProducts(List<Long> selectedIds) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Product> criteria = cb.createQuery(Product.class);
        Root<Product> root = criteria.from(Product.class);
        CriteriaQuery<Product> cq = criteria.where(root.get(Product_.id).in(selectedIds));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Gets the all visible properties of this VAT object.
     * 
     * @return String[] of visible VAT properties
     */
    public String[] getVisibleProperties() {
        return new String[] { 
                Product_.itemNumber.getName(), 
                Product_.name.getName(), 
                Product_.description.getName(),
                Product_.quantity.getName(), 
                Product_.price1.getName(), 
                Product_.vat.getName() };
    }

}
