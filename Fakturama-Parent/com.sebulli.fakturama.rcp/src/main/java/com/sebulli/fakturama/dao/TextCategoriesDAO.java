package com.sebulli.fakturama.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.TextCategory;

@Creatable
public class TextCategoriesDAO extends AbstractCategoriesDAO<TextCategory> {

    protected Class<TextCategory> getEntityClass() {
    	return TextCategory.class;
    }
    
    /**
     * Get all {@link TextCategory}s from Database.
     *
     * @return List<TextCategory> 
     */
    public List<TextCategory> findAll() {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    	CriteriaQuery<TextCategory> cq = cb.createQuery(TextCategory.class);
    	CriteriaQuery<TextCategory> selectQuery = cq.select(cq.from(TextCategory.class));
    	return getEntityManager().createQuery(selectQuery).getResultList();
    }
}
