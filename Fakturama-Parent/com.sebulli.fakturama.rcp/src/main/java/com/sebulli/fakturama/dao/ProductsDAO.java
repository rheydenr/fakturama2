package com.sebulli.fakturama.dao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.Product_;
import com.sebulli.fakturama.oldmodel.OldProducts;

@Creatable
public class ProductsDAO extends AbstractDAO<Product> {
    
    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;

    protected Class<Product> getEntityClass() {
    	return Product.class;
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
	 * Counts all entities with the given category.
	 * 
	 * @param cat count of entities which have the given category
	 */
	public long countByCategory(AbstractCategory cat) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<Product> root = criteria.from(getEntityClass());
		criteria.select(cb.count(root)).where(
						cb.and(
								cb.equal(root.get(Product_.categories), cat),
								cb.isFalse(root.get(Product_.deleted))
								)
				);
		return getEntityManager().createQuery(criteria).getSingleResult();
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
    	// remove invisible properties
    	List<String> resultList = Arrays.asList(Product_.itemNumber.getName(), 
            Product_.name.getName(), 
            Product_.description.getName(),
            Product_.quantity.getName(), 
            Product_.price1.getName(), 
            Product_.vat.getName()).stream().filter((String prop) -> {
    	
		    	if(eclipsePrefs != null) {
		    		if(prop.equalsIgnoreCase(Product_.quantity.getName()) && !eclipsePrefs.getBoolean(Constants.PREFERENCES_PRODUCT_USE_QUANTITY, true)) {
		    			return false;
		    		} else if(prop.equalsIgnoreCase(Product_.vat.getName()) && !eclipsePrefs.getBoolean(Constants.PREFERENCES_PRODUCT_USE_VAT, true)) {
		    			return false;
		    		} else if(prop.equalsIgnoreCase(Product_.description.getName()) && !eclipsePrefs.getBoolean(Constants.PREFERENCES_PRODUCT_USE_DESCRIPTION, true)) {
		    			return false;
		    		} else if(prop.equalsIgnoreCase(Product_.itemNumber.getName()) && !eclipsePrefs.getBoolean(Constants.PREFERENCES_PRODUCT_USE_ITEMNR, true)) {
		    			return false;
		    		}
		    	}
		    	return true;
            }).collect(Collectors.toList());

        return resultList.toArray(new String[resultList.size()]);
    }

	public Product findByItemNumber(String itemNo) {
		Product result = null;
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(getEntityClass());
        Root<Product> product = query.from(getEntityClass());
        query.select(product).where(
        		cb.and(
        				cb.equal(product.get(Product_.itemNumber), itemNo),
        				cb.not(product.get(Product_.deleted)))
        		);
        TypedQuery<Product> q = getEntityManager().createQuery(query);
        q.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
        q.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			// no result means we return a null value
		} catch (Exception e) {
			// multiple results mean we have a problem with numbering of products
		}
		return result;
	}
}
