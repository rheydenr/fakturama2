package com.sebulli.fakturama.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.dto.AccountEntry;
import com.sebulli.fakturama.model.AbstractVoucher;
import com.sebulli.fakturama.model.Expenditure;
import com.sebulli.fakturama.model.Expenditure_;
import com.sebulli.fakturama.model.VoucherCategory;

@Creatable
public class ExpendituresDAO extends AbstractDAO<Expenditure> {

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

    protected Class<Expenditure> getEntityClass() {
    	return Expenditure.class;
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
	 * Finds Vouchers having a given account. An account is a {@link VoucherCategory}. 
	 * The Vouchers can be filtered for a certain date range.
	 * 
	 * @param account which account should be used for filtering
	 * @param startDate Date for filtering (can be <code>null</code>)
	 * @param endDate Date for filtering (can be <code>null</code>)
	 * @return List of {@link AccountEntry}s, sorted by Voucher date
	 */
	public List<AccountEntry> findAccountedExpenditures(VoucherCategory account, Date startDate, Date endDate) {
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	    CriteriaQuery<Expenditure> criteria = cb.createQuery(getEntityClass());
	    Root<Expenditure> root = criteria.from(getEntityClass());
	    Predicate predicate = cb.and(
				cb.not(root.get(Expenditure_.deleted)),
				cb.equal(root.get(Expenditure_.account), account)
		);
	    if(startDate != null && endDate != null) {
	    	// if startDate is after endDate we switch the two dates silently
	    	predicate = cb.and(predicate,
	    			cb.between(root.get(Expenditure_.voucherDate), startDate.before(endDate) ? startDate : endDate, 
	    					endDate.after(startDate) ? endDate : startDate)
	    		);
	    }
		CriteriaQuery<Expenditure> cq = criteria.where(predicate).orderBy(cb.asc(root.get(Expenditure_.voucherDate)));
	    TypedQuery<Expenditure> query = getEntityManager().createQuery(cq);
		List<Expenditure> documentList = query.getResultList();
		List<AccountEntry> resultList = new ArrayList<>();
		for (Expenditure document : documentList) {
			AccountEntry accountEntry = new AccountEntry(document, AccountEntry.EXPENDITURE_SIGN);
			resultList.add(accountEntry);
		}
		return resultList;
	}

	/**
	 * Finds all Vouchers within a given date range. If one of the dates (or both) is <code>null</code> then all
	 * Vouchers will be in the returned List.
	 * 
	 * @param startDate start of date range
	 * @param endDate end of date range
	 * @return List of Vouchers
	 */
	public List<Expenditure> findVouchersInDateRange(GregorianCalendar startDate, GregorianCalendar endDate) {
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	    CriteriaQuery<Expenditure> criteria = cb.createQuery(getEntityClass());
	    Root<Expenditure> root = criteria.from(getEntityClass());
	    Predicate predicate = cb.not(root.get(Expenditure_.deleted));
	    if(startDate != null && endDate != null) {
	    	// if startDate is after endDate we switch the two dates silently
	    	predicate = cb.and(predicate,
	    			cb.between(root.get(Expenditure_.voucherDate), startDate.before(endDate) ? startDate.getTime() : endDate.getTime(), 
	    					endDate.after(startDate) ? endDate.getTime() : startDate.getTime())
	    		);
	    }
		CriteriaQuery<Expenditure> cq = criteria.where(predicate).orderBy(cb.asc(root.get(Expenditure_.voucherDate)));
	    TypedQuery<Expenditure> query = getEntityManager().createQuery(cq);
		return query.getResultList();
	}
  
    /**
    * Gets the all visible properties of this Expenditure object.
    * 
    * @return String[] of visible Expenditure properties
    */
    public String[] getVisibleProperties() {
       return new String[] { Expenditure_.doNotBook.getName(), 
               Expenditure_.voucherDate.getName(), 
               Expenditure_.voucherNumber.getName(), 
               Expenditure_.documentNumber.getName(),
               Expenditure_.name.getName(), 
               Expenditure_.paidValue.getName() 
               };
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
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
