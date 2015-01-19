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

import com.sebulli.fakturama.model.Account;
import com.sebulli.fakturama.model.Account_;
import com.sebulli.fakturama.parts.converter.CommonConverter;

/**
 *
 */
@Creatable
public class AccountDAO extends AbstractDAO<Account> {
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

    protected Class<Account> getEntityClass() {
        return Account.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }
    
    /**
     * Get all {@link Account}s from Database.
     *
     * @return List<Account> 
     */
    public List<Account> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        CriteriaQuery<Account> selectQuery = cq.select(cq.from(Account.class));
        return getEntityManager().createQuery(selectQuery).getResultList();
//      return getEntityManager().createQuery("select p from Account p", Account.class).getResultList();
    }
    
    /**
     * Finds a {@link Account} by its name. Category in this case is a String separated by 
     * slashes, e.g. "/fooCat/barCat". Searching starts with the rightmost value
     * and then check the parent. 
     * 
     * @param account the Category to search
     * @return {@link Account}
     */
    public Account findAccountByName(String account) {
        Account result = null;
        if(StringUtils.isNotEmpty(account)) {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Account> cq = cb.createQuery(Account.class);
            Root<Account> rootEntity = cq.from(Account.class);
            // extract the rightmost value
            String[] splittedCategories = account.split("/");
            String leafCategory = splittedCategories[splittedCategories.length - 1];        
            CriteriaQuery<Account> selectQuery = cq.select(rootEntity)
                    .where(cb.and(
                                cb.equal(rootEntity.get(Account_.name), leafCategory) /*,
                                cb.equal(rootEntity.get(Account_.parent), Account.class)
                               ,
                                cb.equal(rootEntity.get(Account_.deleted), false)*/));
            try {
                List<Account> tmpResultList = getEntityManager().createQuery(selectQuery).getResultList();
                // remove leading slash
                String testCat = StringUtils.removeStart(account, "/");
                for (Account tmpAccount : tmpResultList) {
                    if(StringUtils.equals(CommonConverter.getCategoryName(tmpAccount, ""), testCat)) {
                        result = tmpAccount;
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
     * Find a {@link Account} by its name. If one of the part categories doesn't exist we create it 
     * (if withPersistOption is set).
     * 
     * @param testCat the category to find
     * @param withPersistOption persist a (part) category if it doesn't exist
     * @return found category
     */
    public Account getCategory(String testCat, boolean withPersistOption) {
        // to find the complete category we have to start with the topmost category
        // and then lookup each of the child categories in the given path
        String[] splittedCategories = testCat.split("/");
        Account parentCategory = null;
        String category = "";
        try {
            for (int i = 0; i < splittedCategories.length; i++) {
                category += "/" + splittedCategories[i];
                Account searchCat = findAccountByName(category);
                if (searchCat == null) {
                    // not found? Then create a new one.
                    Account newCategory = new Account();
                    newCategory.setName(splittedCategories[i]);
                    newCategory.setParent(parentCategory);
//                    save(newCategory);
                    searchCat = newCategory;
                }
                // save the parent and then dive deeper...
                parentCategory = searchCat;
            } 
            if(!getEntityManager().contains(parentCategory)) {
                parentCategory = save(parentCategory);
            }
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return parentCategory;
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
