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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;


/**
 * @author rheydenr
 *
 */
public abstract class AbstractDAO<T> {

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
    private EntityManager entityManager;
	
    public T save(T properties) throws SQLException {
        
/*
 * BESSER: 
 * 
@PersistenceUnit EntityManagerFactory factory;
protected void doPost(HttpServlet req, ...) {
EntityManager em = factory.createEntityManager();
Order order order = ...;
em.persist(order);


TODO (in en einzelnen Entities:
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
        getEntityManager().persist(properties);
        trx.commit();
        return properties;
    }
    
    public T update(T properties) throws SQLException {
        checkConnection();
        EntityTransaction trx = getEntityManager().getTransaction();
        trx.begin();
        getEntityManager().merge(properties);
        trx.commit();
        return properties;
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
	
	protected EntityManager getEM2() {
		return entityManager;
	}
}
