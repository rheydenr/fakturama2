/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
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

import java.sql.SQLException;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.Expenditure;
import com.sebulli.fakturama.model.Expenditure_;
import com.sebulli.fakturama.model.VATCategory_;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.parts.converter.CommonConverter;

/**
 *
 */
@Creatable
public class VoucherCategoriesDAO extends AbstractDAO<VoucherCategory> {

    @Inject
    @GeminiPersistenceContext(unitName = "unconfigured2", properties = {
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference(PersistenceUnitProperties.JDBC_DRIVER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference(PersistenceUnitProperties.JDBC_URL)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, valuePref = @Preference(PersistenceUnitProperties.JDBC_USER)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, valuePref = @Preference(PersistenceUnitProperties.JDBC_PASSWORD)),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.LOGGING_LEVEL, value = "INFO"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
//    @GeminiPersistenceContext(unitName = "mysql-datasource")
//    @GeminiPersistenceContext(unitName = "origin-datasource")
    private EntityManager em;

    protected Class<VoucherCategory> getEntityClass() {
        return VoucherCategory.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }
    
    /**
     * Get all {@link VoucherCategory}s from Database.
     *
     * @return List<VoucherCategory> 
     */
    public List<VoucherCategory> findAll() {
/*
        categories.addAll(Data.INSTANCE.getPayments().getCategoryStrings());
        categories.addAll(Data.INSTANCE.getReceiptVouchers().getCategoryStrings());
        categories.addAll(Data.INSTANCE.getExpenditureVouchers().getCategoryStrings());
 */
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<VoucherCategory> cq = cb.createQuery(getEntityClass());
        CriteriaQuery<VoucherCategory> selectQuery = cq.select(cq.from(getEntityClass()));
        return getEntityManager().createQuery(selectQuery).getResultList();
//      return getEntityManager().createQuery("select p from VoucherCategory p", VoucherCategory.class).getResultList();
    }
    
    /**
     * Finds a {@link VoucherCategory} by its name. Category in this case is a String separated by 
     * slashes, e.g. "/fooCat/barCat". Searching starts with the rightmost value
     * and then check the parent. 
     * 
     * @param vatCategory the Category to search
     * @return {@link VoucherCategory}
     */
    public VoucherCategory findVoucherCategoryByName(String vatCategory) {
        VoucherCategory result = null;
        if(StringUtils.isNotEmpty(vatCategory)) {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<VoucherCategory> cq = cb.createQuery(getEntityClass());
            Root<VoucherCategory> rootEntity = cq.from(getEntityClass());
            // extract the rightmost value
            String[] splittedCategories = vatCategory.split("/");
            String leafCategory = splittedCategories[splittedCategories.length - 1];        
            CriteriaQuery<VoucherCategory> selectQuery = cq.select(rootEntity)
                    .where(cb.and(
                                cb.equal(rootEntity.get(VATCategory_.name), leafCategory) /*,
                                cb.equal(rootEntity.get(VATCategory_.parent), VoucherCategory.class)
                               ,
                                cb.equal(rootEntity.get(VATCategory_.deleted), false)*/));
            try {
                List<VoucherCategory> tmpResultList = getEntityManager().createQuery(selectQuery).getResultList();
                // remove leading slash
                String testCat = StringUtils.removeStart(vatCategory, "/");
                for (VoucherCategory vatCategory2 : tmpResultList) {
                    if(StringUtils.equals(CommonConverter.getCategoryName(vatCategory2, ""), testCat)) {
                        result = vatCategory2;
                        break;
                    }
                }
            }
            catch (NoResultException nre) {
                // no result means we return a null value 
            }
        }
        return result;
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

    /**
     * Find a {@link VoucherCategory} by its name. If one of the part categories doesn't exist we create it 
     * (if {@code withPersistOption} is set to <code>true</code>).
     * 
     * @param testCat the category to find
     * @param withPersistOption persist a (part) category if it doesn't exist
     * @return found category
     */
    public VoucherCategory getOrCreateCategory(String testCat, boolean withPersistOption) {
        // to find the complete category we have to start with the topmost category
        // and then lookup each of the child categories in the given path
        String[] splittedCategories = testCat.split("/");
        VoucherCategory parentCategory = null;
        String category = "";
        try {
            for (int i = 0; i < splittedCategories.length; i++) {
                category += "/" + splittedCategories[i];
                VoucherCategory searchCat = findVoucherCategoryByName(category);
                if (searchCat == null) {
                    // not found? Then create a new one.
                    VoucherCategory newCategory = new VoucherCategory();
                    newCategory.setName(splittedCategories[i]);
                    newCategory.setParent(parentCategory);
                    newCategory = save(newCategory);
                    searchCat = newCategory;
                }
                // save the parent and then dive deeper...
                parentCategory = searchCat;
            }
        }
        catch (FakturamaStoringException e) {
            getLog().error(e);
        }
        return parentCategory;
    }

    public VoucherCategory getLastUsedCategoryForExpenditure() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Expenditure> cq = cb.createQuery(Expenditure.class);
        Root<Expenditure> rootEntity = cq.from(Expenditure.class);
        cq.select(rootEntity).orderBy(cb.desc(rootEntity.get(Expenditure_.dateAdded.getName())));
        List<Expenditure> singleResult = getEntityManager().createQuery(cq).getResultList();
        return singleResult != null && !singleResult.isEmpty() ? singleResult.get(0).getAccount() : null;
    }
    
    public VoucherCategory getLastUsedCategoryForReceiptvoucher() {
        return null;
    }

}
