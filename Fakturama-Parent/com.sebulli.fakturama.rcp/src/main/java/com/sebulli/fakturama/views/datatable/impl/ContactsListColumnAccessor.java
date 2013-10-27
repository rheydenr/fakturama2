/**
 * 
 */
package com.sebulli.fakturama.views.datatable.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;

import com.sebulli.fakturama.model.Contact;

/**
 * @author G527032
 *
 *
 * TODO Die Klasse ähnelt ein bißchen der Klasse DataTableColumn
 */
public class ContactsListColumnAccessor implements IColumnPropertyAccessor<Contact> {
	
	/**
	 * Properties which have to shown
	 */
	public static final String[] CONTACT_PROPERTIES = new String[] { "nr", "firstname",
			"name", "company", "zip", "city" };
	
	/**
	 * Fields which are scanned while filtering with search widget
	 */
	public static final String[] CONTACT_SEARCHFIELDS = new String[] { "nr", "firstname",
		"name", "company", "zip", "city" };
	
	/**
	 * mapping from property to label
	 */
	public static final Map<String, String> PROPERTY_TO_LABEL_MAP = new HashMap<>();
	static {
		PROPERTY_TO_LABEL_MAP.put(CONTACT_PROPERTIES[0], "No.");
		PROPERTY_TO_LABEL_MAP.put(CONTACT_PROPERTIES[1], "First Name");
		PROPERTY_TO_LABEL_MAP.put(CONTACT_PROPERTIES[2], "Last Name");
		PROPERTY_TO_LABEL_MAP.put(CONTACT_PROPERTIES[3], "Company");
		PROPERTY_TO_LABEL_MAP.put(CONTACT_PROPERTIES[4], "ZIP");
		PROPERTY_TO_LABEL_MAP.put(CONTACT_PROPERTIES[5], "City");
	}

	@Override
	public Object getDataValue(Contact rowObject, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return rowObject.getNr();
		case 1:
			return getFirstline(rowObject.getFirstname());
		case 2:
			return getFirstline(rowObject.getName());
		case 3:
			return getFirstline(rowObject.getCompany());
		case 4:
			return rowObject.getZip();
		case 5:
			return getFirstline(rowObject.getCity());
		}
		return null;
	}

	/**
	 * Helper method for reading only the first line of an entry if it has multiple lines.
	 * 
	 * @param value
	 * @return
	 */
	private String getFirstline(String value) {
		String firstline = value;
		if(StringUtils.contains(value, '\n')) {
			firstline = StringUtils.split(value, '\n')[0];			
		}
		return firstline;
	}

	@Override
	public void setDataValue(Contact rowObject, int columnIndex,
			Object newValue) {
		throw new UnsupportedOperationException("this operation is not allowed in this list.");
		
	}

	@Override
	public int getColumnCount() {
		return CONTACT_PROPERTIES.length;
	}

	@Override
	public String getColumnProperty(int columnIndex) {
		return CONTACT_PROPERTIES[columnIndex];
	}

	@Override
	public int getColumnIndex(String propertyName) {
		return Arrays.asList(CONTACT_PROPERTIES).indexOf(propertyName);
	}
}
