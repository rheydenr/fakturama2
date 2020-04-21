package com.sebulli.fakturama.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Creditor_;
import com.sebulli.fakturama.model.Debitor;

@Creatable
public class CreditorsDAO extends AbstractDAO<Creditor> {
    
    @Override
    public List<Creditor> findAll() {
        return findAll(false);
    }
    
    @Override
    protected Set<Predicate> getRestrictions(Creditor object, CriteriaBuilder criteriaBuilder, Root<Creditor> root) {
    	throw new RuntimeException("HIER BITTE NOCHMAL NACHSEHEN!!!");
    }
    
    @Override
    public List<Creditor> findAll(boolean forceRead) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Creditor> criteria = cb.createQuery(getEntityClass());
        Root<Creditor> root = criteria.from(getEntityClass());
        /*
         * Since referenced contacts are stored as own data set we have to
         * test for NULL customer number. If customer number is NULL we have
         * an alternate contact which belongs to a "legal" contact and thus we 
         * don't have to show them up.
         */
        CriteriaQuery<Creditor> cq = criteria.where(
                cb.and(
                        cb.not(root.get(Creditor_.deleted)),
                        cb.isNotNull(root.get(Creditor_.customerNumber))
                        )
                );
        TypedQuery<Creditor> query = getEntityManager().createQuery(cq);
        if(forceRead) {
            query.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
        }
        return query.getResultList();
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
                Creditor_.company.getName(), Creditor_.addresses.getName() + "." +Address_.zip.getName(), Creditor_.addresses.getName() + "." +Address_.city.getName()};
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
        CriteriaQuery<Creditor> query = cb.createQuery(getEntityClass());
        Root<Creditor> debitorQuery = query.from(getEntityClass());
        // filter all Debitors with matching addresses
        query.distinct(true).select(debitorQuery)
                .where(cb.and(
                        debitorQuery.get(Creditor_.customerNumber).isNotNull(), 
                        cb.not(debitorQuery.get(Creditor_.deleted))/*
                                                                     * , cb.or(
                                                                     * cb.isEmpty(debitorQuery.join(Contact_.addresses).
                                                                     * get(Address_.contactTypes)),
                                                                     * debitorQuery.join(Contact_.addresses).get(
                                                                     * Address_.contactTypes).in(contactType) )
                                                                     */
                        ))
                .orderBy(cb.asc(debitorQuery.get(Creditor_.customerNumber)));
        TypedQuery<Creditor> q = getEntityManager().createQuery(query);
        q.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
//        q.setHint(QueryHints.REFRESH, HintValues.TRUE); 
        q.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
        debitorQuery.fetch(Creditor_.categories);
        debitorQuery.fetch(Creditor_.addresses).fetch(Address_.contactTypes);
        List<Creditor> debitorsFromDb = q.getResultList();
        List<DebitorAddress> treeItems = new ArrayList<>();
        
        /*
         * Create a list of DebitorAddresses. This is done by creating at least one
         * entry (for the main address) and some child entries for other matching addresses.
         */
        for (Creditor debitor : debitorsFromDb) {
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
     
     private DebitorAddress createDebitorTreeItem(Creditor debitor2, Address adr) {
         return new DebitorAddress(debitor2, adr);
     }    

}
