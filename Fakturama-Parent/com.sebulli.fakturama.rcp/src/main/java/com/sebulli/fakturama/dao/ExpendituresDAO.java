package com.sebulli.fakturama.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.dto.AccountEntry;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.model.VoucherCategory_;
import com.sebulli.fakturama.model.VoucherType;
import com.sebulli.fakturama.model.Voucher_;

@Creatable
public class ExpendituresDAO extends AbstractDAO<Voucher> {

    protected Class<Voucher> getEntityClass() {
    	return Voucher.class;
    }
    
    /**
     * The following attributes are compared: name, category, date, nr, documentnr.
     * 
     * @see com.sebulli.fakturama.dao.AbstractDAO#getRestrictions(java.lang.Object, javax.persistence.criteria.CriteriaBuilder, javax.persistence.criteria.Root)
     */
    @Override
    protected Set<Predicate> getRestrictions(Voucher object, CriteriaBuilder criteriaBuilder, Root<Voucher> root) {
    	Set<Predicate> restrictions = new HashSet<>();
    	restrictions.add(criteriaBuilder.equal(root.get(Voucher_.name), StringUtils.defaultString(object.getName())));
    	if(object.getAccount() != null) {
	    	restrictions.add(criteriaBuilder.equal(root.get(Voucher_.account).get(VoucherCategory_.name), 
	    			 object.getAccount().getName()));
    	}
    	restrictions.add(criteriaBuilder.equal(root.get(Voucher_.voucherDate), object.getVoucherDate()));
    	restrictions.add(criteriaBuilder.not(root.get(Voucher_.deleted)));
    	restrictions.add(criteriaBuilder.equal(root.get(Voucher_.documentNumber), StringUtils.defaultString(object.getDocumentNumber())));
    	restrictions.add(criteriaBuilder.equal(root.get(Voucher_.voucherNumber), StringUtils.defaultString(object.getVoucherNumber())));
    	return restrictions;
    }
 
    /* (non-Javadoc)
     * @see com.sebulli.fakturama.dao.AbstractDAO#findAll()
     */
    @Override
	public List<Voucher> findAll(boolean forceRead) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Voucher> criteria = cb.createQuery(getEntityClass());
		Root<Voucher> root = criteria.from(getEntityClass());
		TypedQuery<Voucher> query = getEntityManager().createQuery(
				criteria.where(cb.and(
								cb.not(root.get(Voucher_.deleted)),
								cb.equal(root.get(Voucher_.voucherType), VoucherType.EXPENDITURE)))
				);
        if(forceRead) {
            query.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
        }
		return query.getResultList();
	}
    
	/**
	 * Finds Vouchers having a given account. An account is a {@link VoucherCategory}.
	 * 
	 * @param account which account should be used for filtering
	 * @return List of {@link AccountEntry}s, sorted by Voucher date
	 */
	public List<AccountEntry> findAccountedExpenditures(VoucherCategory account) {
		return findAccountedExpenditures(account, null, null);
	}
		
	/**
	 * Finds {@link Voucher}s having a given account. An account is a {@link VoucherCategory}. 
	 * The {@link Voucher}s can be filtered for a certain date range.
	 * 
	 * @param account which account should be used for filtering
	 * @param startDate Date for filtering (can be <code>null</code>)
	 * @param endDate Date for filtering (can be <code>null</code>)
	 * @return List of {@link AccountEntry}s, sorted by {@link Voucher}'s date
	 */
	public List<AccountEntry> findAccountedExpenditures(VoucherCategory account, Date startDate, Date endDate) {
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	    CriteriaQuery<Voucher> criteria = cb.createQuery(getEntityClass());
	    Root<Voucher> root = criteria.from(getEntityClass());
	    Predicate predicate = cb.and(
				cb.not(root.get(Voucher_.deleted)),
				cb.equal(root.get(Voucher_.account), account),
				cb.equal(root.get(Voucher_.voucherType), VoucherType.EXPENDITURE)
		);
	    if(startDate != null && endDate != null) {
	    	// if startDate is after endDate we switch the two dates silently
	    	predicate = cb.and(predicate,
	    			cb.between(root.get(Voucher_.voucherDate), startDate.before(endDate) ? startDate : endDate, 
	    					endDate.after(startDate) ? endDate : startDate)
	    		);
	    }
		CriteriaQuery<Voucher> cq = criteria.where(predicate).orderBy(cb.asc(root.get(Voucher_.voucherDate)));
	    TypedQuery<Voucher> query = getEntityManager().createQuery(cq);
		List<Voucher> documentList = query.getResultList();
		List<AccountEntry> resultList = new ArrayList<>();
		for (Voucher document : documentList) {
			AccountEntry accountEntry = new AccountEntry(document, AccountEntry.EXPENDITURE_SIGN);
			resultList.add(accountEntry);
		}
		return resultList;
	}
	
	/**
	 * Finds all undeleted {@link Voucher}s sorted by {@link ItemAccountType} and date.
	 * @return List of {@link Voucher}s, sorted by account and voucher date
	 */
	public List<Voucher> findAllExpendituresSorted() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Voucher> criteria = cb.createQuery(getEntityClass());
        Root<Voucher> root = criteria.from(getEntityClass());
	    Predicate predicate = cb.and(
				cb.not(root.get(Voucher_.deleted)),
				cb.equal(root.get(Voucher_.voucherType), VoucherType.EXPENDITURE)
		);
        CriteriaQuery<Voucher> cq = criteria.where(predicate)
        		.orderBy(cb.asc(root.get(Voucher_.account)), cb.asc(root.get(Voucher_.voucherDate)));
        TypedQuery<Voucher> query = getEntityManager().createQuery(cq);
        return query.getResultList();
		
	}

	/**
	 * Finds all Vouchers within a given date range. If one of the dates (or both) is <code>null</code> then all
	 * Vouchers will be in the returned List.
	 * 
	 * @param startDate start of date range
	 * @param endDate end of date range
	 * @return List of Vouchers
	 */
	public List<Voucher> findVouchersInDateRange(GregorianCalendar startDate, GregorianCalendar endDate) {
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	    CriteriaQuery<Voucher> criteria = cb.createQuery(getEntityClass());
	    Root<Voucher> root = criteria.from(getEntityClass());
	    Predicate predicate = cb.and(
				cb.not(root.get(Voucher_.deleted)),
				cb.equal(root.get(Voucher_.voucherType), VoucherType.EXPENDITURE)
		);
	    if(startDate != null && endDate != null) {
	    	// if startDate is after endDate we switch the two dates silently
	    	predicate = cb.and(predicate,
	    			cb.between(root.get(Voucher_.voucherDate), startDate.before(endDate) ? startDate.getTime() : endDate.getTime(), 
	    					endDate.after(startDate) ? endDate.getTime() : startDate.getTime())
	    		);
	    }
		CriteriaQuery<Voucher> cq = criteria.where(predicate).orderBy(cb.asc(root.get(Voucher_.voucherDate)));
	    TypedQuery<Voucher> query = getEntityManager().createQuery(cq);
		return query.getResultList();
	}
	
	
	/**
	 * Get all {@link Voucher} names as String array. Used for content proposals.
	 * 
	 * @param maxLength max count of result items
	 * @return array of voucher names.
	 */
	public String[] getVoucherNames(int maxLength) {
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	    CriteriaQuery<Voucher> criteria = cb.createQuery(getEntityClass());
	    Root<Voucher> root = criteria.from(getEntityClass());
	    Predicate predicate = cb.and(
				cb.not(root.get(Voucher_.deleted)),
				cb.equal(root.get(Voucher_.voucherType), VoucherType.EXPENDITURE),
				cb.isNotNull(root.get(Voucher_.name))
		);
	    
		CriteriaQuery<Voucher> cq = criteria.where(predicate).orderBy(cb.asc(root.get(Voucher_.voucherDate)));
	    TypedQuery<Voucher> query = getEntityManager().createQuery(cq).setMaxResults(maxLength);
        return query.getResultList().stream().map(v -> v.getName()).sorted().collect(Collectors.toList()).toArray(new String[] {});
	}
  
    /**
    * Gets the all visible properties of this Voucher object.
    * 
    * @return String[] of visible Voucher properties
    */
    public String[] getVisibleProperties() {
       return new String[] { Voucher_.doNotBook.getName(), 
               Voucher_.voucherDate.getName(), 
               Voucher_.voucherNumber.getName(), 
               Voucher_.documentNumber.getName(),
               Voucher_.name.getName(), 
               Voucher_.paidValue.getName() 
               };
    }
}
