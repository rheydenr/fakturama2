package com.sebulli.fakturama.dao;

import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Creditor_;

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
}
