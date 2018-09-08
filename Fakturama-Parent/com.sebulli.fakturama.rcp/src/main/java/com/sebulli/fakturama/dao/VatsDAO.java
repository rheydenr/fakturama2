package com.sebulli.fakturama.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.nls.Translation;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.VAT_;
import com.sebulli.fakturama.oldmodel.OldVats;

@Creatable
public class VatsDAO extends AbstractDAO<VAT> {
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private VatCategoriesDAO vatCategoriesDAO;

	protected Class<VAT> getEntityClass() {
		return VAT.class;
	}
	
	@PostConstruct
	public void init() {
	    // *** THIS IS A TEST ONLY!!! To show how the Texo JPA access works **********
//	        EntityManagerProvider.getInstance().setEntityManagerFactory(em.getEntityManagerFactory());
	        FakturamaModelPackage.initialize(); 	  //  objectStore.get(VAT.class, 1L);    
//	        VATDao vatDao = DaoRegistry.getInstance().getDao(VATDao.class);
//	        VAT vat2 = vatDao.get(1L);
	}
	
	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.dao.AbstractDAO#getAlwaysIncludeAttributes()
	 */
	@Override
	protected Map<Class<VAT>, Vector<String>> getAlwaysIncludeAttributes() {
	    Map<Class<VAT>, Vector<String>> map = new HashMap<>();
	    Vector<String> attribVector = new Vector<String>();
	    attribVector.addElement(VAT_.taxValue.getName());
	    map.put(getEntityClass(), attribVector);
	    return map;
	}
	
	@Override
	protected Set<Predicate> getRestrictions(VAT object, CriteriaBuilder cb, Root<VAT> vat) {
        Set<Predicate> restrictions = new HashSet<>();
	    // Only the name and the
	    // values are compared If the name is not set, only the values are used.
	    // If the name of the DataSet to test is empty, than search for an entry with at least the same VAT value
	    if(StringUtils.isNotBlank(object.getName())) {
	        restrictions.add(cb.equal(vat.get(VAT_.name), object.getName()));
	    }
	    // if tax value is not set we create an irregular value to compare
	    // (force creating a new object)
        restrictions.add(cb.equal(vat.get(VAT_.taxValue), object.getTaxValue() != null ? object.getTaxValue() : Double.valueOf(-10.0)));
	    return restrictions;
	}

	/**
	 * Finds a {@link VAT} by a given {@link OldVats}.
	 * 
	 * @param oldVat
	 * @return
	 */
	public VAT findByOldVat(OldVats oldVat) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<VAT> criteria = cb.createQuery(VAT.class);
		Root<VAT> root = criteria.from(VAT.class);
		criteria.where(cb.and(cb.equal(root.get(VAT_.description), oldVat.getDescription()),
				cb.equal(root.get(VAT_.name), oldVat.getName())));
		return getEntityManager().createQuery(criteria).getSingleResult();
	}
	
	/**
	 * Counts all entities with the given category.
	 * 
	 * @param cat count of entities which have the given category
	 */
	public long countByCategory(AbstractCategory cat) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<VAT> root = criteria.from(getEntityClass());
		criteria.select(cb.count(root)).where(
				cb.and(
						cb.equal(root.get(VAT_.category), cat),
						cb.isFalse(root.get(VAT_.deleted))
						)
				);
		return getEntityManager().createQuery(criteria).getSingleResult();
	}

	/**
     * Get all active (undeleted) data sets with a specified category.
     * 
     * Return only those elements that are in the specified category and those
     * where no category is set.
     * 
     * @param category
     *            The preferred category. If it's empty, return all.
     * 
     * @return ArrayList with all undeleted data sets
     */
	public List<VAT> findVATPreferredCategory(String category) {
	    VATCategory vATCategory = vatCategoriesDAO.findCategoryByName(category);
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<VAT> criteria = cb.createQuery(VAT.class);
        Root<VAT> root = criteria.from(VAT.class);
        criteria.where(
                cb.or(
                        cb.equal(root.get(VAT_.category), vATCategory),
                        cb.isNull(root.get(VAT_.category))));
        return getEntityManager().createQuery(criteria).getResultList();
	}

	/**
	 * Provides a list with all VAT entries with a value of "0%"
	 * 
	 * @return
	 */
	public List<VAT> findNoVATEntries() {
        //T: Name of a VAT entry that indicates that VAT is not 0%
        VAT dummyVat = new VAT();
        dummyVat.setName(msg.widgetNovatproviderWithvatLabel);
        dummyVat.setTaxValue(Double.valueOf(0.0));
	    VATCategory vATCategory = vatCategoriesDAO.findCategoryByName(msg.dataVatSalestax);
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<VAT> criteria = cb.createQuery(VAT.class);
        Root<VAT> root = criteria.from(VAT.class);
        criteria.where(
                cb.and(
                    cb.equal(root.get(VAT_.taxValue), Double.valueOf(0.0)),
                    cb.or(
                            cb.equal(root.get(VAT_.category), vATCategory),
                            cb.isNull(root.get(VAT_.category))))
                
                );
        List<VAT> resultList = new ArrayList<>();
        resultList.add(dummyVat);
        resultList.addAll(getEntityManager().createQuery(criteria).getResultList());
        return resultList;
	}
	     
	/**
	 * Gets the all visible properties of this VAT object.
	 * 
	 * @return String[] of visible VAT properties
	 */
	public String[] getVisibleProperties() {
		return new String[] { VAT_.name.getName(), VAT_.description.getName(), VAT_.taxValue.getName()};
	}
}
