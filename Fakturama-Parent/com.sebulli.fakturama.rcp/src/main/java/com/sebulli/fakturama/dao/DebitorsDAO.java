package com.sebulli.fakturama.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.Debitor_;

@Creatable
public class DebitorsDAO extends AbstractDAO<Debitor> {
    
    @Override
    protected Set<Predicate> getRestrictions(Debitor object, CriteriaBuilder cb, Root<Debitor> root) {
        /* Customer number, first
         * name, name and ZIP are compared. Customer number is only compared, if it
         * is set.
         */
        Set<Predicate> restrictions = new HashSet<>();
        // Compare customer number, only if it is set.
        if(StringUtils.isNotBlank(object.getCustomerNumber())) {
            restrictions.add(cb.equal(root.get(Debitor_.customerNumber), object.getCustomerNumber()));
        }
        // if the value is not set (null), then we use the empty String for comparison. 
        // Then we get no result (which is correct).
        restrictions.add(cb.equal(root.get(Debitor_.firstName), StringUtils.defaultString(object.getFirstName())));
        restrictions.add(cb.equal(root.get(Debitor_.name), StringUtils.defaultString(object.getName())));
//    	TODO HIER BITTE NOCHMAL NACHSEHEN!!!
//        if (object.getAddress() != null) {
//            restrictions.add(cb.equal(root.get(Debitor_.address).get(Address_.zip), StringUtils.defaultString(object.getAddress().getZip())));
//        } else {
//            // set to an undefined value so we get no result (then the contact is not found in the database)
//            restrictions.add(cb.equal(root.get(Debitor_.address).get(Address_.zip), "-1"));
//        }
        return restrictions;
    }
    
    @Override
    public List<Debitor> findAll() {
        return findAll(false);
    }
    
    public List<Debitor> findForListView() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Debitor> query = cb.createQuery(getEntityClass());
        Root<Debitor> debitor = query.from(getEntityClass());
        query.select(debitor).where(
        		cb.and(
        				debitor.get(Debitor_.customerNumber).isNotNull(),
        				cb.not(debitor.get(Debitor_.deleted)))
        		).orderBy(cb.asc(debitor.get(Debitor_.customerNumber)));
        TypedQuery<Debitor> q = getEntityManager().createQuery(query);
        q.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
//        q.setHint(QueryHints.REFRESH, HintValues.TRUE); 
        q.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
        debitor.fetch(Debitor_.categories);
        return q.getResultList();
    }
    
    /**
     * Finds all {@link DebitorAddress}es for a given {@link ContactType}. This is used for
     * selection of a certain {@link Contact} in the DocumentEditor's address field.
     * If a {@link Debitor} has only one address then this one is used.
     * If a {@link Debitor} has more than one address and many of them matches the given {@link ContactType},
     * all of these matching {@link Debitor}s are returned.
     * @param contactType 
     * 
     * @return List of {@link DebitorAddress}es for a certain {@link ContactType}.
     */
	public List<DebitorAddress> findForTreeListView(ContactType contactType) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Debitor> query = cb.createQuery(getEntityClass());
		Root<Debitor> debitorQuery = query.from(getEntityClass());
		// filter all Debitors with matching addresses
		query.distinct(true).select(debitorQuery)
				.where(cb.and(
						debitorQuery.get(Debitor_.customerNumber).isNotNull(), 
						cb.not(debitorQuery.get(Debitor_.deleted))/*
																	 * , cb.or(
																	 * cb.isEmpty(debitorQuery.join(Contact_.addresses).
																	 * get(Address_.contactTypes)),
																	 * debitorQuery.join(Contact_.addresses).get(
																	 * Address_.contactTypes).in(contactType) )
																	 */
						))
				.orderBy(cb.asc(debitorQuery.get(Debitor_.customerNumber)));
		TypedQuery<Debitor> q = getEntityManager().createQuery(query);
		q.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
//        q.setHint(QueryHints.REFRESH, HintValues.TRUE); 
		q.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
		debitorQuery.fetch(Debitor_.categories);
		debitorQuery.fetch(Debitor_.addresses).fetch(Address_.contactTypes);
		List<Debitor> debitorsFromDb = q.getResultList();
		List<DebitorAddress> treeItems = new ArrayList<>();
		
		/*
		 * Create a list of DebitorAddresses. This is done by creating at least one
		 * entry (for the main address) and some child entries for other matching addresses.
		 */
		for (Debitor debitor : debitorsFromDb) {
			List<Address> addresses = debitor.getAddresses();
			DebitorAddress treeItemDebitorAddress;
			if (addresses.size() >= 1) {
				// create the first entry for a debitor
				treeItemDebitorAddress = createDebitorTreeItem(debitor, addresses.get(0));
				if(addresses.size() > 1) {
					// if more than one address exists create child entries
					addresses.subList(1, addresses.size())
						.stream()
						.filter(adr -> adr.getContactTypes().isEmpty() || adr.getContactTypes().contains(contactType))
						.forEach(adr -> treeItems.add(createDebitorTreeItem(debitor, adr)));
				}
				treeItems.add(treeItemDebitorAddress);
			}
		}

		return treeItems;
	}    
    
/*

   Müller | Fritz | Bahnhofstraße 3 | 05885 | Friesland      => ContactType.INVOICE
v  Meyer  | Johannes 
     --   | ---   | Hauptstraße 4  | 08554 | Adorf           => ContactType.INVOICE
     --   | ---   | Carolastraße 3 | 18554 | Bedorf          => ContactType.INVOICE, ContactType.DELIVERY
   Emsland | Jaqueline | Karlstraße 9 | 82282 | Rühmkirchen  => ContactType.INVOICE

 */
 	
	private DebitorAddress createDebitorTreeItem(Debitor debitor2, Address adr) {
		return new DebitorAddress(debitor2, adr);
	}    
    
    public Debitor findByDebitorNumber(String debNo) {
        Debitor result = null;
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Debitor> query = cb.createQuery(getEntityClass());
        Root<Debitor> debitor = query.from(getEntityClass());
        query.select(debitor).where(
        		cb.and(
        				cb.equal(debitor.get(Debitor_.customerNumber), debNo),
        				cb.not(debitor.get(Debitor_.deleted)))
        		);
        TypedQuery<Debitor> q = getEntityManager().createQuery(query);
        q.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
        q.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			// no result means we return a null value
		} catch (NonUniqueResultException nurex) {
			// not so good - we prefer to not return any data...
		}
		return result;
    }
    
    @Override
    public List<Debitor> findAll(boolean forceRead) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Debitor> query = cb.createQuery(getEntityClass());
        Root<Debitor> root = query.from(getEntityClass());
        /*
         * Since referenced contacts are stored as own data set we have to
         * test for NULL customer number. If customer number is NULL we have
         * an alternate contact which belongs to a "legal" contact and thus we 
         * don't have to show them up.
         */
        query.where(
                cb.and(
                        cb.not(root.get(Debitor_.deleted)),
                        cb.isNotNull(root.get(Debitor_.customerNumber))
                        )
                );
        TypedQuery<Debitor> q = getEntityManager().createQuery(query);
        if(forceRead) {
            q.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
        }
        return q.getResultList();
    }

    /**
     * Get a list of all categories stored for {@link Debitor}s.
     * 
     * @return list of all categories
     */
	public Collection<String> getCategoryStrings() {
		List<String> result = getEntityManager().createQuery("select distinct c.category from Debitor c where c.deleted = false", String.class).getResultList();
		return result;
	}

	@Override
	protected Class<Debitor> getEntityClass() {
		return Debitor.class;
	}
	
    /**
     * Gets the all visible properties of this VAT object.
     * 
     * @return String[] of visible VAT properties
     */
    public String[] getVisibleProperties() {
        return new String[] { Debitor_.customerNumber.getName(), Debitor_.firstName.getName(), Debitor_.name.getName(),
                Debitor_.company.getName(), Address_.zip.getName(), Address_.city.getName()};
    }
}
