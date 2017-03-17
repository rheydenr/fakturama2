package com.sebulli.fakturama.dao;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

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

    protected Class<Shipping> getEntityClass() {
    	return Shipping.class;
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
}
