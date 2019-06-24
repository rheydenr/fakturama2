package com.sebulli.fakturama.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Contact_;
import com.sebulli.fakturama.oldmodel.OldContacts;

@Creatable
public class ContactsDAO extends AbstractDAO<Contact> {
    
    /**
     * We have to override this method since we want to only find "real" contacts, i.e., no "alternate" contacts.
     */
    @Override
    public List<Contact> findAll() {
        return findAll(false);
    }
    
    @Override
    public List<Contact> findAll(boolean forceRead) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Contact> query = cb.createQuery(getEntityClass());
        Root<Contact> root = query.from(getEntityClass());
        /*
         * A "real" contact has _always_ a contact number, even if it's a space...
         */
        query.where(
        		cb.and(
        				cb.not(root.<Boolean> get(Contact_.deleted)),
        				cb.isNotNull(root.get(Contact_.customerNumber)))
        		);
        TypedQuery<Contact> q = getEntityManager().createQuery(query);
        if(forceRead) {
            q.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
        }
        return q.getResultList();
    }
    
    @Override
    protected Set<Predicate> getRestrictions(Contact object, CriteriaBuilder cb, Root<Contact> root) {
        /* Customer number, first
         * name, name and ZIP are compared. Customer number is only compared, if it
         * is set.
         */
    	
    	throw new RuntimeException("HIER BITTE NOCHMAL NACHSEHEN!!!");
//        Set<Predicate> restrictions = new HashSet<>();
//        // Compare customer number, only if it is set.
//        if(StringUtils.isNotBlank(object.getCustomerNumber())) {
//            restrictions.add(cb.equal(root.get(Contact_.customerNumber), object.getCustomerNumber()));
//        }
//        // if the value is not set (null), then we use the empty String for comparison. 
//        // Then we get no result (which is correct).
//        restrictions.add(cb.equal(root.get(Contact_.firstName), StringUtils.defaultString(object.getFirstName())));
//        restrictions.add(cb.equal(root.get(Contact_.name), StringUtils.defaultString(object.getName())));
//        if (object.getAddresses() != null) {
//            restrictions.add(cb.equal(root.get(Contact_.address).get(Address_.zip), StringUtils.defaultString(object.getAddresses().getZip())));
//        } else {
//            // set to an undefined value so we get no result (then the contact is not found in the database)
//            restrictions.add(cb.equal(root.get(Contact_.address).get(Address_.zip), "-1"));
//        }
//        
//        // and, finally, filter all deleted contacts (or contacts that are'nt valid anymore)
//        restrictions.add(cb.and(
//                cb.not(root.get(Contact_.deleted)),
//                cb.or(
//                    cb.isNull(root.get(Contact_.validTo)),
//                    cb.greaterThanOrEqualTo(root.get(Contact_.validTo), cb.currentDate())
//                    )));
//        return restrictions;
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
                Contact_.company.getName(), Address_.zip.getName(), Address_.city.getName()};
    }

	/**
	 * Checks if a {@link Contact} with the same values exists.
	 * 
	 * @param name
	 * @param firstName
	 * @param street
	 * @return
	 */
	public Contact checkContactWithSameValues(String name, String firstName, String street) {
		Set<Predicate> restrictions = new HashSet<>();
		Contact retval = null;
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Contact> query = cb.createQuery(getEntityClass());
        Root<Contact> root = query.from(getEntityClass());
        Join<Contact, Address> address = root.join(Contact_.addresses);
        restrictions.add(cb.equal(root.get(Contact_.firstName), StringUtils.defaultString(firstName)));
        restrictions.add(cb.equal(root.get(Contact_.name), StringUtils.defaultString(name)));
        restrictions.add(cb.not(root.get(Contact_.deleted)));
        restrictions.add(cb.equal(address.get(Address_.street), StringUtils.defaultString(street)));
        CriteriaQuery<Contact> select = query.select(root);
        select.where(restrictions.toArray(new Predicate[]{}));
        List<Contact> resultList = getEntityManager().createQuery(select).getResultList();
        if(!resultList.isEmpty()) {
        	retval = resultList.get(0);
        }
		return retval;
	}

	/**
	 * Checks if a {@link Contact} with the same number exists.
	 * 
	 * @param contactNumber 
	 * @return
	 */
	public Contact getContactWithSameNumber(String contactNumber) {
		Set<Predicate> restrictions = new HashSet<>();
		Contact retval = null;
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Contact> query = cb.createQuery(getEntityClass());
        Root<Contact> root = query.from(getEntityClass());
        restrictions.add(cb.equal(root.get(Contact_.customerNumber), StringUtils.defaultString(contactNumber)));
        restrictions.add(cb.not(root.get(Contact_.deleted)));
        CriteriaQuery<Contact> q = query.select(root).where(restrictions.toArray(new Predicate[]{}));
        List<Contact> resultList = getEntityManager().createQuery(q).getResultList();
        if(!resultList.isEmpty()) {
        	retval = resultList.get(0);
        }
		return retval;
	}	
}
