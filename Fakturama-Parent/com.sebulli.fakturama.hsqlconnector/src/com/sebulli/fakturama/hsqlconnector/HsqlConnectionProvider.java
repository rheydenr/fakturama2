package com.sebulli.fakturama.hsqlconnector;

import com.sebulli.fakturama.dbconnector.IDbConnection;

/**
 * Provides the implementation for an HSQL DB.
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
