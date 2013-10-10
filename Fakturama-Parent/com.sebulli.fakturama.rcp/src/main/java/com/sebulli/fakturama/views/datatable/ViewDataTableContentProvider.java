/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.views.datatable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Documents;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.views.datatable.TopicTreeViewer.TreeObject;

/**
 * Content provider for the UniDataSet table viewer
 * 
 * @author Gerd Bartelt
 * 
 */
public class ViewDataTableContentProvider<T> implements IStructuredContentProvider, PropertyChangeListener {

	// The viewer
	private final Viewer viewer;

	// The filters
	private String categoryFilter = "";
	private int transactionFilter = -1;
	private int contactFilter = -1;

	// The selected tree object
	private TreeObject treeObject = null;

	private double totalSum = 0.0;
	
	/**
	 * Constructor Sets the table viewer
	 * 
	 * @param viewer
	 *            The table viewer
	 */
	public ViewDataTableContentProvider(Viewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Returns the elements to display in the viewer when its input is set to
	 * the given element.
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {

		// Create a 2nd list, which will contain only those elements,
		// that are not deleted and match the filters.
		List<IEntity> contentFiltered = new ArrayList<IEntity>();

		// Get the content
		if (inputElement instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<IEntity> content = (List<IEntity>) inputElement;

			totalSum = 0.0;
			
			// Check all entries
			for (IEntity uds : content) {
				// Use only those entries, that match the filters
				if ((uds.getCategory().toLowerCase().startsWith(categoryFilter.toLowerCase() + "/") || uds.getCategory().equalsIgnoreCase(categoryFilter) || categoryFilter
						.isEmpty())
						&& ((transactionFilter < 0) || (uds.getTransactionKey() == transactionFilter))
						&& ((contactFilter < 0) || (uds.getAddressKey() == contactFilter))) {
					if (uds instanceof Documents) {
						totalSum += ((Documents)uds).getTotal();
					}
					contentFiltered.add(uds);
				}
			}
			
			// Use the tooltip to display the total sum
			if (treeObject != null) {
				treeObject.setToolTip(DataUtils.DoubleToFormatedPriceRound(totalSum));
			}
		}

		// The filters elements
		return contentFiltered.toArray();

	}

	/**
	 * Disposes of this content provider.
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * Notifies this content provider that the given viewer's input has been
	 * switched to a different element.
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * This method gets called when a bound property is changed.
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		viewer.refresh();
	}

	/**
	 * Sets the transaction filter
	 * 
	 * @param filter
	 *            The transaction filter
	 */
	public void setTransactionFilter(int filter) {
		this.transactionFilter = filter;
	}

	
	
	/**
	 * Sets the contact filter
	 * 
	 * @param filter
	 *            The contact filter
	 */
	public void setContactFilter(int filter) {
		this.contactFilter = filter;
	}

	/**
	 * Sets the category filter
	 * 
	 * @param filter
	 *            The category filter
	 */
	public void setCategoryFilter(String filter) {
		this.categoryFilter = filter;
	}

	/**
	 * Returns the transaction filter
	 * 
	 * @return The current transaction filter
	 */
	public int getTransactionFilter() {
		return this.transactionFilter;

	}

	/**
	 * Returns the category filter
	 * 
	 * @return The current category filter
	 */
	public String getCategoryFilter() {
		return this.categoryFilter;
	}
	
//	/**
//	 * Set a reference to the tree object
//	 * 
//	 * @param treeObject
//	 * 		The tree object
//	 */
//	public void setTreeObject(TreeObject treeObject){
//		this.treeObject = treeObject;
//	}

}
