package com.sebulli.fakturama.dbconnector;

import java.sql.SQLException;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;

/**
 * This class is a helper for the name conversion of data table attributes.
 * The entity classes holds them as camelCase, but the database columns
 * are named with underscores. So, an attribute like accountHolder (from Contacts)
 * is stored as ACCOUNT_HOLDER in database.  

 * @author Ivan Rodriguez Murillo
 */
public class CamelCaseSessionCustomizer implements SessionCustomizer {
	@Override
	public void customize(Session session) throws SQLException {
		for (ClassDescriptor descriptor : session.getDescriptors().values()) {
			/*
			 * not used, this is for tables only. Old Fakturama doesn't use tables
			 * with underscores. 
			 */
			// Only change the table name for non-embedable entities with no
			// @Table already
//			if (!descriptor.getTables().isEmpty() && descriptor.getAlias().equalsIgnoreCase(descriptor.getTableName())) {
//				String tableName = addUnderscores(descriptor.getTableName());
//				descriptor.setTableName(tableName);
//				for (IndexDefinition index : descriptor.getTables().get(0).getIndexes()) {
//					index.setTargetTable(tableName);
//				}
//			}
			for (DatabaseMapping mapping : descriptor.getMappings()) {
				// Only change the column name for non-embedable entities with
				// no @Column already
				if (mapping.getField() != null && !mapping.getAttributeName().isEmpty()
						&& mapping.getField().getName().equalsIgnoreCase(mapping.getAttributeName())) {
					mapping.getField().setName(addUnderscores(mapping.getAttributeName()));
				}
			}
		}
	}

	/**
	 * Converts a camelCase name to a String with underscores.
	 * 
	 * @param name the name to convert
	 * @return the converted name
	 */
	private static String addUnderscores(String name) {
		StringBuffer buf = new StringBuffer(name.replace('.', '_'));
		for (int i = 1; i < buf.length() - 1; i++) {
			if (Character.isLowerCase(buf.charAt(i - 1)) && Character.isUpperCase(buf.charAt(i)) && Character.isLowerCase(buf.charAt(i + 1))) {
				buf.insert(i++, '_');
			}
		}
		return buf.toString().toUpperCase();
	}

}