/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.CEFACTCode;
import com.sebulli.fakturama.model.CEFACTCode_;

/**
 *
 */

@Creatable
public class CEFACTCodeDAO extends AbstractDAO<CEFACTCode> {

	@Override
	protected Class<CEFACTCode> getEntityClass() {
		return CEFACTCode.class;
	}

	/**
	 * Find a {@link CEFACTCode} by abbreviation. This method keeps the {@link Locale} in mind.
	 *
	 * @param userdefinedQuantityUnit the userdefined quantity unit
	 * @param locale the locale
	 * @return the CEFACT code
	 */
	public CEFACTCode findByAbbreviation(String userdefinedQuantityUnit, Locale locale) {
		Set<Predicate> restrictions = new HashSet<>();
		CEFACTCode retval = null;
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<CEFACTCode> query = cb.createQuery(getEntityClass());
        Root<CEFACTCode> root = query.from(getEntityClass());
        if(locale.getCountry().matches("DE")) {
        	restrictions.add(cb.equal(root.get(CEFACTCode_.abbreviation_de), StringUtils.defaultString(userdefinedQuantityUnit)));
        } else {
        	restrictions.add(cb.equal(root.get(CEFACTCode_.abbreviation_en), StringUtils.defaultString(userdefinedQuantityUnit)));
        }
        CriteriaQuery<CEFACTCode> select = query.select(root);
        select.where(restrictions.toArray(new Predicate[]{}));
        List<CEFACTCode> resultList = getEntityManager().createQuery(select).getResultList();
        if(!resultList.isEmpty()) {
        	retval = resultList.get(0);
        }
		return retval;
	}

}
