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

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.model.Voucher_;

/**
 *
 */
@Creatable
public class VoucherCategoriesDAO extends AbstractCategoriesDAO<VoucherCategory> {

    protected Class<VoucherCategory> getEntityClass() {
        return VoucherCategory.class;
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
                if(category.contentEquals("/")) {
                	continue;
                }
                VoucherCategory searchCat = findCategoryByName(category);
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
        CriteriaQuery<Voucher> cq = cb.createQuery(Voucher.class);
        Root<Voucher> rootEntity = cq.from(Voucher.class);
        cq.select(rootEntity).orderBy(cb.desc(rootEntity.get(Voucher_.dateAdded.getName())));
        List<Voucher> singleResult = getEntityManager().createQuery(cq).getResultList();
        return singleResult != null && !singleResult.isEmpty() ? singleResult.get(0).getAccount() : null;
    }
    
    public VoucherCategory getLastUsedCategoryForReceiptvoucher() {
        return null;
    }

}
