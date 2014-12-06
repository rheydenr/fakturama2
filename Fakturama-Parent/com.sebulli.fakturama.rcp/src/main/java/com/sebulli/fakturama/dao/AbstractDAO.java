/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2013 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ralf Heydenreich - initial API and implementation
 */ 
package com.sebulli.fakturama.dao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.QueryByExamplePolicy;
import org.eclipse.persistence.queries.ReadAllQuery;


/**
 * Abstract superclass for all DAOs. Used for finding, saving or updating certain entities.
 *
 */
public abstract class AbstractDAO<T> {
	
    public T save(T object) throws SQLException {
        
/*
 * BESSER: 
 * 
@PersistenceUnit EntityManagerFactory factory;
protected void doPost(HttpServlet req, ...) {
EntityManager em = factory.createEntityManager();
Order order order = ...;
em.persist(order);


TODO (in den einzelnen Entities:
@Version Timestamp timestamp;

use TypedQuery<Employee>

CriteriaQuery<Order> o = cb.createQuery(Order.class);
Root<Order> ord = o.from(Order.class);
o.select(ord).where(cb.gt(ord.get(Order_.total), 100));
TypeQuery<Order.class> q = en.createQuery(o);
List<Person> result = q.getResultList();

bei read-only-entities (results): @TransactionAttribute(NOT_SUPPORTED)

Transaktionen
- neue Tx:
@Resource UserTransaction utx;
. . .
utx.begin();
EntityManager em = emf.createEntityManager();
//em is now JTA
 * 
 * 
 * Verbinden zu bestehender: 
 * @Resource UserTransaction utx;
. . .
EntityManager em = emf.createEntityManager();
//em is is RESOURCE_LOCAL
utx.begin();
em.joinTransaction();

} */
        
        checkConnection();
        EntityTransaction trx = getEntityManager().getTransaction();
        trx.begin();
        // merge before persist since we could have referenced entities which are already persisted 
        object = getEntityManager().merge(object);
        getEntityManager().persist(object);
        trx.commit();
        return object;
    }
    
    public T update(T object) throws SQLException {
        checkConnection();
        EntityTransaction trx = getEntityManager().getTransaction();
        trx.begin();
        object = getEntityManager().merge(object);
//        getEntityManager().persist(object);
        trx.commit();
        return object;
    }

/* * * * * * * * * [some common finders] * * * * * * * * * * * * * * * * * * * * * /
    /**
     * Get all {@link T} from Database.
     *
     * @return List<T>
     */
    public List<T> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(getEntityClass());
        Root<T> root = criteria.from(getEntityClass());
        CriteriaQuery<T> cq = criteria.where(cb.not(root.<Boolean> get("deleted")));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Finds a t with its name.
     * 
     * @param name the name of the entity
     * @return T
     */
    public T findByName(String entityName) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(getEntityClass());
        Root<T> root = criteria.from(getEntityClass());
        CriteriaQuery<T> cq = criteria.where(cb.equal(root.<String> get("name"), entityName));
        return getEntityManager().createQuery(cq).getSingleResult();
    }

    /**
     * Finds a {@link T} by id.
     * 
     * @param id
     * @return
     */
    public T findById(long id) {
    	return getEntityManager().find(getEntityClass(), id);
    }
    
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */    
    
    /**
     * Adds a new entity if it doesn't exist. I.e., if an semantically equal object is in the database,
     * no saving is done (this is useful e.g. for webshop import).
     * 
     * @param obj the entity to write
     * @return the refreshed entity
     */
    public T addIfNew(T obj) throws SQLException {
        T retval = findByExample(obj);
        if(retval == null) {
            retval = save(obj);
        }
        return retval;
    }
  
    @SuppressWarnings("unchecked")
    public T findByExample(T example) {
        ReadAllQuery query = new ReadAllQuery(getEntityClass());
        query.setExampleObject(example);
        QueryByExamplePolicy policy = new QueryByExamplePolicy();
        policy.addSpecialOperation(String.class, "containsSubstring");
        policy.setAttributesToAlwaysInclude(getAlwaysIncludeAttributes());
        policy.setShouldUseEqualityForNulls(true);
        query.setQueryByExamplePolicy(policy);
        List<T> resultList = JpaHelper.createQuery(query, getEntityManager()).getResultList();
        if(resultList.isEmpty()) {
            return null;
        } else {
            return resultList.get(0);
        }
    }
    
    /**
     * Contains all the attributes which are always necessary for comparing.
     * 
     * @return
     */
    protected Map<Class<T>, Vector<String>> getAlwaysIncludeAttributes() {return Collections.emptyMap();}

    /**
     * Gets the count of all entities of this sort.
     * 
     * @return count of entities
     */
    public Long getCount() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> cq1 = cb.createQuery(Long.class);
        cq1.select(cb.count(cq1.from(getEntityClass())));
        TypedQuery<Long> qry = getEntityManager().createQuery(cq1);
        return qry.getSingleResult();
    }

    /**
     * checks if connection is alive
     * 
     * @throws SQLException
     */
    private void checkConnection() throws SQLException {
        if (getEntityManager() == null) {
            throw new SQLException("EntityManager is null. Not connected to database!");
        }
    }

	protected abstract EntityManager getEntityManager();
	protected abstract Class<T> getEntityClass();
}
