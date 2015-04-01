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

import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.ItemAccountType_;

/**
 *
 */
@Creatable
public class ItemAccountTypeDAO extends AbstractDAO<ItemAccountType> {
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

    protected Class<ItemAccountType> getEntityClass() {
        return ItemAccountType.class;
    }

    @PreDestroy
    public void destroy() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }
    
    /**
     * Get all {@link ItemAccountType}s from Database.
     *
     * @return List<ItemAccountType> 
     */
    public List<ItemAccountType> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<ItemAccountType> cq = cb.createQuery(ItemAccountType.class);
        CriteriaQuery<ItemAccountType> selectQuery = cq.select(cq.from(ItemAccountType.class));
        return getEntityManager().createQuery(selectQuery).getResultList();
//      return getEntityManager().createQuery("select p from ItemAccountType p", ItemAccountType.class).getResultList();
    }
    
    /**
     * Finds an {@link ItemAccountType} by its name. An {@link ItemAccountType} doesn't have a hierarchical
     * structure.
     * 
     * @param account the Category to search
     * @return {@link ItemAccountType}
     */
    public ItemAccountType findItemAccountTypeByName(String account) {
        ItemAccountType result = null;
        if(StringUtils.isNotEmpty(account)) {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<ItemAccountType> cq = cb.createQuery(ItemAccountType.class);
            Root<ItemAccountType> rootEntity = cq.from(ItemAccountType.class);
            CriteriaQuery<ItemAccountType> selectQuery = cq.select(rootEntity)
                    .where(cb.and(
                                cb.equal(rootEntity.get(ItemAccountType_.name), account)));
            try {
                result = getEntityManager().createQuery(selectQuery).getSingleResult();
            }
            catch (NoResultException nre) {
                // no result means we return a null value 
            }
        }
        return result;
    }
    
    /**
     * Find a {@link ItemAccountType} by its name. If one of the part categories doesn't exist we create it 
     * (if withPersistOption is set).
     * 
     * @param testCat the category to find
     * @param withPersistOption persist a (part) category if it doesn't exist
     * @return found category
     */
    public ItemAccountType getCategory(String testCat, boolean withPersistOption) {
        // to find the complete category we have to start with the topmost category
        // and then lookup each of the child categories in the given path
        String[] splittedCategories = testCat.split("/");
        ItemAccountType parentCategory = null;
        String category = "";
        try {
            for (int i = 0; i < splittedCategories.length; i++) {
                category += "/" + splittedCategories[i];
                ItemAccountType searchCat = findItemAccountTypeByName(category);
                if (searchCat == null) {
                    // not found? Then create a new one.
                    ItemAccountType newCategory = new ItemAccountType();
                    newCategory.setName(splittedCategories[i]);
//                    newCategory.setParent(parentCategory);
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
