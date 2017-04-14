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
 
package com.sebulli.fakturama.dbservice.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.prefs.Preferences;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.dbconnector.IActivateDbServer;
import com.sebulli.fakturama.dbservice.IDbUpdateService;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.osgi.OSGiResourceAccessor;

/**
 * Implementation of {@link IDbUpdateService}.
 *
 */
public class DbUpdateService implements IDbUpdateService {

    private Preferences eclipsePrefs;

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.dbservice.IDbUpdateService#updateDatabase()
	 */
	@Override
	public boolean updateDatabase() {
		boolean retval = true;
		
		// emergency switch: turn off this feature with NODBUPDATE=true
		if(BooleanUtils.toBoolean(System.getProperty("NODBUPDATE"))) {
			return retval;
		}
		
		// get the preferences for this application from common plugin
		this.eclipsePrefs = Activator.getPreferences();
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		try (java.sql.Connection connection = openConnection(context);) {
			Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
			this.getClass().getResourceAsStream("/changelog/db.changelog-master.xml");
			Liquibase liquibase = new liquibase.Liquibase("/changelog/db.changelog-master.xml", 
					new OSGiResourceAccessor(context.getBundle()), database);
			liquibase.update(new Contexts(), new LabelExpression());
		} catch (LiquibaseException | SQLException ex) {
			ex.printStackTrace();
			retval = false;
		}
		return retval;
	}

	/**
	 * @param context 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Connection openConnection(BundleContext context) {
		Connection conn = null;
		try {
			ServiceReference<?>[] allServiceReferences = context.getAllServiceReferences(
					org.osgi.service.jdbc.DataSourceFactory.class.getName(), "(osgi.jdbc.driver.class="+eclipsePrefs.get(PersistenceUnitProperties.JDBC_DRIVER, "")+")");
			ServiceReference<DataSourceFactory> serviceReference;
			if(allServiceReferences.length > 0) {
				serviceReference = (ServiceReference<DataSourceFactory>) allServiceReferences[0];
			} else {
				serviceReference = null;
				System.err.println("No service reference found for database connection!");
			}
		    Properties prop = new Properties();
		    prop.put(DataSourceFactory.JDBC_URL, eclipsePrefs.get(PersistenceUnitProperties.JDBC_URL, ""));
		    prop.put(DataSourceFactory.JDBC_USER, eclipsePrefs.get(PersistenceUnitProperties.JDBC_USER, "fakturama"));
		    prop.put(DataSourceFactory.JDBC_PASSWORD, eclipsePrefs.get(PersistenceUnitProperties.JDBC_PASSWORD, "fakturama"));
	    	
			String dataSource = (String)prop.get(DataSourceFactory.JDBC_URL);
			if(dataSource.startsWith("jdbc:hsqldb:file") || dataSource.endsWith("fakdbneu")) {
				allServiceReferences = context.getAllServiceReferences(IActivateDbServer.class.getName(), null);
				if(allServiceReferences.length > 0) {
					ServiceReference<IActivateDbServer> serviceDbRef;
					serviceDbRef = (ServiceReference<IActivateDbServer>) allServiceReferences[0];
					prop.put("hsqlfiledb", eclipsePrefs.get("hsqlfiledb", ""));
					Properties activateProps= context.getService(serviceDbRef).activateServer(prop);
					eclipsePrefs.put(PersistenceUnitProperties.JDBC_URL, String.format("jdbc:hsqldb:hsql://localhost:9002/%s", activateProps.get("runningfakdb")));
					prop.put(DataSourceFactory.JDBC_URL, eclipsePrefs.get(PersistenceUnitProperties.JDBC_URL, ""));
					eclipsePrefs.put("hsqlfiledb", (String) activateProps.get("hsqlfiledb"));
				}
			}

			conn = context.getService(serviceReference).createDataSource(prop).getConnection();
		} catch (SQLException ex) {
		    // handle any errors
		    System.err.println("SQLException: " + ex.getMessage());
		    System.err.println("SQLState: " + ex.getSQLState());
		    System.err.println("VendorError: " + ex.getErrorCode());
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
}
