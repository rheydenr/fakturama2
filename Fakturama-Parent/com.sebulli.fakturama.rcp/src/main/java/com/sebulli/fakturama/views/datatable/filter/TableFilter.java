/**
 * 
 */
package com.sebulli.fakturama.views.datatable.filter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @author Administrator
 *
 */
public class TableFilter<T> extends ViewerFilter {

	private String searchColumns[];
	private String searchString;

	/**
	 * Constructor Set the search columns. Only these columns are compared with
	 * the search filter.
	 * 
	 * @param searchColumns
	 */
	public TableFilter(String searchColumns[]) {
		this.searchColumns = searchColumns;
	}

	/**
	 * Set the search string and add a wildcard character to the beginning and
	 * the end
	 * 
	 * @param s
	 *            The search string
	 */
	public void setSearchText(String s) {
		this.searchString = ".*" + s + ".*";
	}

	/**
	 * Returns whether the given element makes it through this filter.
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {

		// If the filter is empty, show all elements
		if (StringUtils.isEmpty(searchString)) { return true; }

		// Get the element
		@SuppressWarnings("unchecked")
		T uds = (T) element;

		// Search all the columns
		for (int i = 0; i < searchColumns.length; i++) {
			// TODO use service!
//			if (DataUtils.getSingleLine(uds.getStringValueByKey(searchColumns[i])).matches(searchString)) { return true; }
		}

		return false;
	}
}
