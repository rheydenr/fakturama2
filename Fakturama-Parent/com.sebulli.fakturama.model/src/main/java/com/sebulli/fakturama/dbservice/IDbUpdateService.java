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
 
package com.sebulli.fakturama.dbservice;

/**
 * Service for updating the database. This service loads the liquibase changelogs
 * and applies them to the database. Thus, the tables are created and updated, if necessary.
 * The service is launched by LifecycleManager while checking the DB.
 * The changelogs are in the resources directory in this package.  
 */
public interface IDbUpdateService {

	/**
	 * Updates the database with liquibase changelogs. 
	 * @return <code>true</code> if the update was successful. 
	 */
	public boolean updateDatabase();

	void shutDownDb();

	/**
	 * Checks if the database is reachable.
	 * @return <code>true</code>, if database responds
	 */
    public boolean isDbAlive();
}
