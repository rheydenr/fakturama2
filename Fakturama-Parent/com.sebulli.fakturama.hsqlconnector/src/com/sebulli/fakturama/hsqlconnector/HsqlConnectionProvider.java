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

	private static final String SYS_PROP_DATABASE_NAME = "hsql.database.name";
	private static final int DEFAULT_HSQL_DATABASEPORT = 9001;
	private static final String DEFAULT_HSQL_DATABASENAME = "fakdbneu";
	
	private Server server;
	private String workspace;
	private int hsqlPort = DEFAULT_HSQL_DATABASEPORT;

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
		String url = props.getProperty(DataSourceFactory.JDBC_URL);
		String hsqlPortProperty = props.getProperty(DataSourceFactory.JDBC_PORT_NUMBER, Integer.toString(DEFAULT_HSQL_DATABASEPORT));
		if(!hsqlPortProperty.isEmpty()) {
			setHsqlPort(hsqlPortProperty);
		}
		workspace = props.getProperty("GENERAL_WORKSPACE");
		Pattern patt = Pattern.compile(".*?:file:(.*?);.*");
		Matcher m = patt.matcher(url);
		String dbFileName;
		if (m.matches() && m.groupCount() > 0) {
			dbFileName = m.group(1);
			props.put(DataSourceFactory.JDBC_DATASOURCE_NAME, dbFileName);
			props.put("newfakdbname", getDatabaseName());
			// set up the rest of properties
		} else/* if(props.getProperty("url").endsWith(getDatabaseName())) */ {
			dbFileName = (String)props.get(DataSourceFactory.JDBC_DATASOURCE_NAME);
		}
		hsqlProps.setProperty("server.database.0", dbFileName);
		hsqlProps.setProperty("server.dbname.0", getDatabaseName());
		hsqlProps.setProperty("hsqldb.lob_compressed", "true");
		hsqlProps.setProperty("hsqldb.lob_file_scale", "1");
		hsqlProps.setProperty("hsqldb.shutdown", "true");

		do {
			hsqlProps.setProperty("server.port", hsqlPort);
			try {
				server.setProperties(hsqlProps);
				server.start();
			} catch (IOException | AclFormatException e) {
				System.err.println("Can't connect to database. Reason: " + e.getMessage());
				break;
			}
		} while (server.isNotRunning() && hsqlPort++ < 20000); // check not all ports
		
		if(!server.isNotRunning()) {
			props.put(DataSourceFactory.JDBC_DATABASE_NAME, server.getDatabaseName(0, false));
			props.put(DataSourceFactory.JDBC_PORT_NUMBER, Integer.toString(server.getPort()));
		}
		
		return props;
	}

	private String getDatabaseName() {
		return System.getProperty(SYS_PROP_DATABASE_NAME, DEFAULT_HSQL_DATABASENAME);
	}
	
	public void setHsqlPort(String hsqlPort) {
		if (hsqlPort.matches("\\d+")) {
			this.hsqlPort = Integer.parseInt(hsqlPort);
		}
	}

	@SuppressWarnings("unchecked")
    @Override
	public Connection getConnection() {
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		DataSourceFactory factory;	
	    Connection connection = null;
		try {
			ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(
					org.osgi.service.jdbc.DataSourceFactory.class.getName(), 
					String.format("(%s=org.hsqldb.jdbc.JDBCDriver)", DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
			ServiceReference<DataSourceFactory> serviceReference;
			if(allServiceReferences.length > 0) {
				serviceReference = (ServiceReference<DataSourceFactory>) allServiceReferences[0];
			} else {
				serviceReference = null;
				System.err.println("No service reference found for database connection!");
			}
			factory = bundleContext.getService(serviceReference);
		    Properties prop = new Properties();
		    String url = String.format("jdbc:hsqldb:hsql://localhost:%d/%s", 
		    		server.getPort(), server.getDatabaseName(0, false));
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
	    if(isAlive()) {
    		server.shutdownCatalogs(Database.CLOSEMODE_COMPACT);
    		server.stop();
    		
    //		// #0000604: Create a database backup
    //		BackupManager backupManager = ContextInjectionFactory.make(BackupManager.class, context);
    		
    		BackupManager bm = new BackupManager();
    		bm.createBackup(workspace);
	    }
	}
	
	@Override
	public boolean isAlive() {
	    return server != null && !server.isNotRunning();
	}
}
