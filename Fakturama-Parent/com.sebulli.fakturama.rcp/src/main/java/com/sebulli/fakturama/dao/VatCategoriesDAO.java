package com.sebulli.fakturama.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.VAT_;

@Creatable
public class VatCategoriesDAO extends AbstractCategoriesDAO<VATCategory> {

	protected Class<VATCategory> getEntityClass() {
		return VATCategory.class;
	}
    
    @Override
    protected void updateObsoleteEntities(VATCategory oldCat) {
		// at first update all (deleted) entries in this category and set the category entry to null
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaUpdate<VAT> updateVATs = getEntityManager().getCriteriaBuilder().createCriteriaUpdate(VAT.class);
        Root<VAT> root = updateVATs.from(VAT.class);	        
		updateVATs.set(root.get(VAT_.category),(VATCategory) null);
		updateVATs.where(cb.and(
				cb.equal(root.get(VAT_.category), oldCat),
				cb.isTrue(root.get(VAT_.deleted))));
		getEntityManager().createQuery(updateVATs).executeUpdate();
    }

}
