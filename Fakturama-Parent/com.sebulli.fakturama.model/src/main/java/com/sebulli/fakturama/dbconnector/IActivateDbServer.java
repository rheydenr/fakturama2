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

package com.sebulli.fakturama.dbconnector;

import java.util.Properties;

/**
 *
 */
public interface IActivateDbServer {

    /**
	 * Checks the connection string and starts the database silently in server mode.
	 * This is because the in-memory databases often are <i>extremely</i> slow (namely HSQLDB). 
     * @return a {@link Properties} file with some additional information
	 */
	public Properties activateServer(Properties props);

	default void stopServer() {};

}