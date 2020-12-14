package com.sebulli.fakturama.hsqlconnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.hsqldb.Database;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerAcl.AclFormatException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

import com.sebulli.fakturama.dbconnector.IActivateDbServer;
import com.sebulli.fakturama.dbconnector.IDbConnection;

/**
 * Provides the implementation for an HSQL DB.
 * 
 */
public class HsqlConnectionProvider implements IDbConnection, IActivateDbServer {

	private static final String DEFAULT_HSQL_DATABASEPORT = "9002";
	private static final String DEFAULT_HSQL_DATABASENAME = "fakdbneu";
	private Server server;
	private String workspace;

	@Override
	public String getKey() {
		return "HSQL";
	}

	@Override
	public String getJdbcUrlPattern() {
		return "jdbc:hsqldb:file:///path/to/Database";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.hsqlconnector.IActivateDbServer#activateServer(java
	 * .util.Properties)
	 */
	@Override
	public Properties activateServer(Properties props) {
		server = new Server();
		HsqlProperties hsqlProps = new HsqlProperties();
		String url = props.getProperty("url");
		workspace = props.getProperty("GENERAL_WORKSPACE");
		Pattern patt = Pattern.compile(".*?:file:(.*?);.*");
		Matcher m = patt.matcher(url);
		if (m.matches() && m.groupCount() > 0) {
			String dbFileName = m.group(1);
			hsqlProps.setProperty("server.database.0", dbFileName);
			props.put("hsqlfiledb", dbFileName);
			props.put("newfakdbname", DEFAULT_HSQL_DATABASENAME);
			// set up the rest of properties
		} else if(props.getProperty("url").endsWith(DEFAULT_HSQL_DATABASENAME)) {
			hsqlProps.setProperty("server.database.0", (String)props.get("hsqlfiledb"));
		}
		hsqlProps.setProperty("server.dbname.0", DEFAULT_HSQL_DATABASENAME);
		hsqlProps.setProperty("hsqldb.lob_compressed", "true");
		hsqlProps.setProperty("server.port", DEFAULT_HSQL_DATABASEPORT);
		hsqlProps.setProperty("hsqldb.lob_file_scale", "1");
		hsqlProps.setProperty("hsqldb.shutdown", "true");

		try {
			server.setProperties(hsqlProps);
			server.start();
			props.put("runningfakdb", server.getDatabaseName(0, false));
		} catch (IOException | AclFormatException e) {
			e.printStackTrace();
		}
		
		return props;
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public Connection getConnection() {
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		DataSourceFactory factory;	
	    Connection connection = null;
		try {
			ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(
					org.osgi.service.jdbc.DataSourceFactory.class.getName(), "(osgi.jdbc.driver.class=org.hsqldb.jdbc.JDBCDriver)");
			ServiceReference<DataSourceFactory> serviceReference;
			if(allServiceReferences.length > 0) {
				serviceReference = (ServiceReference<DataSourceFactory>) allServiceReferences[0];
			} else {
				serviceReference = null;
				System.err.println("No service reference found for database connection!");
			}
			factory = bundleContext.getService(serviceReference);
		    Properties prop = new Properties();
		    String url = "jdbc:hsqldb:hsql://localhost:"+DEFAULT_HSQL_DATABASEPORT+"/"+server.getDatabaseName(0, false);
		    prop.put(DataSourceFactory.JDBC_DATABASE_NAME, "test");
		    prop.put(DataSourceFactory.JDBC_URL, url);
		    prop.put(DataSourceFactory.JDBC_USER, "SA");
		    DataSource source = factory.createDataSource(prop);
			connection = source.getConnection();
		} catch (InvalidSyntaxException | SQLException e1) {
            System.err.println("Invalid syntax: " + e1.getMessage());
		}

		return connection;
	}

	@Override
	public void stopServer() {
		server.shutdownCatalogs(Database.CLOSEMODE_COMPACT);
		server.stop();
		
//		// #0000604: Create a database backup
//		BackupManager backupManager = ContextInjectionFactory.make(BackupManager.class, context);
		
		BackupManager bm = new BackupManager();
		bm.createBackup(workspace);
	}
	
	@Override
	public boolean isAlive() {
	    return server != null && !server.isNotRunning();
	}
}
