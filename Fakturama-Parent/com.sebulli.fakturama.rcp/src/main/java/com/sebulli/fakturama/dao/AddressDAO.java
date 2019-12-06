/**
 * 
 */
package com.sebulli.fakturama.dao;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Contact_;

/**
 *
 */
@Creatable
public class AddressDAO extends AbstractDAO<Address> {

	@Override
	protected Class<Address> getEntityClass() {
		return Address.class;
	}
    
    @Override
    protected Set<Predicate> getRestrictions(Address object, CriteriaBuilder cb, Root<Address> root) {
        /* Customer number, first
         * name, name and ZIP are compared. Customer number is only compared, if it
         * is set.
         */
        Set<Predicate> restrictions = new HashSet<>();
        // Compare customer number, only if it is set.
        if(StringUtils.isNotBlank(object.getContact().getCustomerNumber())) {
            restrictions.add(cb.equal(root.get(Address_.contact).get(Contact_.customerNumber), object.getContact().getCustomerNumber()));
        }
        // if the value is not set (null), then we use the empty String for comparison. 
        // Then we get no result (which is correct).
        restrictions.add(cb.equal(root.get(Address_.contact).get(Contact_.firstName), StringUtils.defaultString(object.getContact().getFirstName())));
        restrictions.add(cb.equal(root.get(Address_.contact).get(Contact_.name), StringUtils.defaultString(object.getName())));
        if (object.getZip() != null) {
            restrictions.add(cb.equal(root.get(Address_.zip), StringUtils.defaultString(object.getZip())));
        } else {
            // set to an undefined value so we get no result (then the contact is not found in the database)
            restrictions.add(cb.equal(root.get(Address_.zip), "-1"));
        }
        return restrictions;
    }

}
