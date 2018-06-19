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
import com.sebulli.fakturama.dbconnector.IDbConnection;
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

    private static final String PROP_HSQLFILEDB = "hsqlfiledb";
	private Preferences eclipsePrefs;
	private IActivateDbServer currentService;

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
//			if(!eclipsePrefs.get("GENERAL_WORKSPACE_REQUEST", "").isEmpty()) {
//				System.err.println("dropping old database schema for workspace request");
//				CatalogAndSchema cat = new CatalogAndSchema("", database.getDefaultSchemaName());
//				database.dropDatabaseObjects(cat);
//			}
			this.getClass().getResourceAsStream("/changelog/db.changelog-master.xml");
			Liquibase liquibase = new liquibase.Liquibase("/changelog/db.changelog-master.xml", 
					new OSGiResourceAccessor(context.getBundle()), database);
//			liquibase.forceReleaseLocks();   // workaround!
			liquibase.update(new Contexts(), new LabelExpression());
		} catch (LiquibaseException | SQLException | NullPointerException ex) {
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
			/*
			 * This part is for optimizing performance. Since most users don't want to create an HSQL server database,
			 * we use the standard database (under working dir's Database directory) and start them in server mode.
			 * This is fully transparent to the user. The database is closed after the application shut down.
			 * The problem is, if the user switches the workspace, we have to switch the database, too.
			 * Therefore we have to shutdown the old one and start the database in the correct working directory.
			 * 
			 * ONLY(!!!) important if we use HSQL in the standard way. All other possibilities use an extra database which is 
			 * independent of working directory. 
			 */
			if(dataSource.startsWith("jdbc:hsqldb:file") || dataSource.endsWith("fakdbneu")) {
				allServiceReferences = context.getAllServiceReferences(IActivateDbServer.class.getName(), null);
				if(allServiceReferences.length > 0) {
					ServiceReference<IActivateDbServer> serviceDbRef = (ServiceReference<IActivateDbServer>) allServiceReferences[0];
					prop.put(PROP_HSQLFILEDB, eclipsePrefs.get(PROP_HSQLFILEDB, ""));
					prop.put("encoding", "UTF-8");
					prop.put("shutdown", "true");
					if(currentService != null) {
						try {
							currentService.stopServer();
						} catch (Exception e) {
							// ignore any exception
						}
					}
					currentService = context.getService(serviceDbRef);
					Properties activateProps = currentService.activateServer(prop);
					eclipsePrefs.put(PersistenceUnitProperties.JDBC_URL, String.format("jdbc:hsqldb:hsql://localhost:9002/%s", activateProps.get("runningfakdb")));
					prop.put(DataSourceFactory.JDBC_URL, eclipsePrefs.get(PersistenceUnitProperties.JDBC_URL, ""));
					eclipsePrefs.put(PROP_HSQLFILEDB, (String) activateProps.get(PROP_HSQLFILEDB));

					ServiceReference<IDbConnection> serviceDbRef2 = (ServiceReference<IDbConnection>) allServiceReferences[0];
					IDbConnection dbConn = context.getService(serviceDbRef2);
					conn = dbConn.getConnection();
				}				
			}
			
			if(conn == null) {
				conn = context.getService(serviceReference).createDataSource(prop).getConnection();
			}
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

	
	@Override
	public void shutDownDb() {
		if(currentService != null) {
			try {
				currentService.stopServer();
			} catch (Exception e) {
				// ignore any exception
			}
		}
	}
}
