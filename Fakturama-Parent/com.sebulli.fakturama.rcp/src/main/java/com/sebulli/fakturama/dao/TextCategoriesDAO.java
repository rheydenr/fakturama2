package com.sebulli.fakturama.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextCategory_;

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
