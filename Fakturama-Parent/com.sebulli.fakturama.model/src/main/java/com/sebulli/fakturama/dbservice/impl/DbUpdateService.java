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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

import com.opcoach.e4.preferences.IPreferenceStoreProvider;
import com.sebulli.fakturama.dbconnector.IActivateDbServer;
import com.sebulli.fakturama.dbconnector.IDbConnection;
import com.sebulli.fakturama.dbservice.IDbUpdateService;
import com.sebulli.fakturama.misc.Constants;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.osgi.OSGiResourceAccessor;

/**
 * Implementation of {@link IDbUpdateService}.
 *
 */
public class DbUpdateService implements IDbUpdateService {

	private static final String SYS_PROP_DATABASE_PORT = "hsql.database.port";
	private IPreferenceStore preferenceStore;
	private IActivateDbServer currentService;
    
	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.dbservice.IDbUpdateService#updateDatabase()
	 */
	@Override
	public boolean updateDatabase() {
		boolean retval = true;
		
		// get the preferences for this application
//        EclipseContextFactory.getServiceContext(context).set(IPreferenceStore.class, preferenceStore);
        
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference<IPreferenceStoreProvider> serviceReference = context.getServiceReference(IPreferenceStoreProvider.class);
        preferenceStore = context.getService(serviceReference).getPreferenceStore();
		try (java.sql.Connection connection = openConnection(context);) {
			if(connection == null) {
				throw new SQLException("can't create database connection!");
			}
		
    		// emergency switch: turn off this feature with NODBUPDATE=true
    		if(Boolean.getBoolean("NODBUPDATE")) {
    			return retval;
    		}
    		
			Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
			Liquibase liquibase = new liquibase.Liquibase("/changelog/db.changelog-master.xml", 
					new OSGiResourceAccessor(context.getBundle()), database);

			liquibase.update(new Contexts(), new LabelExpression());
		} catch (ValidationFailedException exc) {
    		System.err.println("Database has not the correct version! " + exc.getMessage());
			retval = false;
		} catch (LiquibaseException | SQLException | NullPointerException ex) {
			System.err.println("Failed to create the database connection: " + ex);
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
					org.osgi.service.jdbc.DataSourceFactory.class.getName(),
					String.format("(%s=%s)", DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
							preferenceStore.getString(PersistenceUnitProperties.JDBC_DRIVER)));
			ServiceReference<DataSourceFactory> serviceReference;
			if(allServiceReferences != null && allServiceReferences.length > 0) {
				serviceReference = (ServiceReference<DataSourceFactory>) allServiceReferences[0];
			} else {
				serviceReference = null;
				System.err.println("No service reference found for database connection!");
			}
		    Properties prop = new Properties();
		    prop.put(DataSourceFactory.JDBC_URL, preferenceStore.getString(PersistenceUnitProperties.JDBC_URL));
		    prop.put(DataSourceFactory.JDBC_USER, preferenceStore.getString(PersistenceUnitProperties.JDBC_USER));
		    prop.put(DataSourceFactory.JDBC_PASSWORD, preferenceStore.getString(PersistenceUnitProperties.JDBC_PASSWORD));
	    	
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
			if (dataSource.contains("hsqldb") ) {
				allServiceReferences = context.getAllServiceReferences(IActivateDbServer.class.getName(), null);
				if(allServiceReferences != null && allServiceReferences.length > 0) {
					ServiceReference<IActivateDbServer> serviceDbRef = (ServiceReference<IActivateDbServer>) allServiceReferences[0];
					prop.put(DataSourceFactory.JDBC_DATASOURCE_NAME, preferenceStore.getString(DataSourceFactory.JDBC_DATASOURCE_NAME));
					
					String sysPropPort = System.getProperty(SYS_PROP_DATABASE_PORT, preferenceStore.getString(DataSourceFactory.JDBC_PORT_NUMBER));
					if(StringUtils.isNumeric(sysPropPort)) {
						prop.put(DataSourceFactory.JDBC_PORT_NUMBER, sysPropPort);
					}
					prop.put("encoding", "UTF-8");
					prop.put(Constants.GENERAL_WORKSPACE, preferenceStore.getString(Constants.GENERAL_WORKSPACE));
					
                    currentService = context.getService(serviceDbRef);
                    if (!isDbAlive()) {
                        Properties activateProps = currentService.activateServer(prop);
                        preferenceStore.putValue(PersistenceUnitProperties.JDBC_URL,
                                String.format("jdbc:hsqldb:hsql://localhost:%s/%s", activateProps.get(DataSourceFactory.JDBC_PORT_NUMBER), activateProps.get(DataSourceFactory.JDBC_DATABASE_NAME)));
                        prop.put(DataSourceFactory.JDBC_URL, preferenceStore.getString(PersistenceUnitProperties.JDBC_URL));
                        preferenceStore.putValue(DataSourceFactory.JDBC_DATASOURCE_NAME, (String) activateProps.get(DataSourceFactory.JDBC_DATASOURCE_NAME));
                    } else {
                        System.err.println("DB was already started");
                    }
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
            System.err.println("Invalid syntax: " + e.getMessage());
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

    @Override
    public boolean isDbAlive() {
        return currentService != null && currentService.isAlive();
    }
}
