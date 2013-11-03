package com.sebulli.fakturama.dbconnector;

/**
 * Provides the implementation for an HSQL DB.
 * 
 * @author rheydenr
 *
 */
public class HsqlConnectionProvider implements IDbConnection {

	@Override
	public String getKey() {
		return "HSQL";
	}

	@Override
	public String getJdbcUrlPattern() {
		return "jdbc:hsqldb:file:///path/to/Database";
	}

}
