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

package com.sebulli.fakturama.views.datatable.nattabletest;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ca.odell.glazedlists.EventList;

import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.views.datatable.nattabletest.RHENatTable.RHEViewTableLabelProvider;

/**
 * This is the topic tree viewer that displays a tree of the categories of all
 * the data sets.
 * 
 * @author Gerd Bartelt
 */
public class TopicTreeViewer<T extends AbstractCategory> {

protected static final String TABLEDATA_CATEGORY_FILTER = "CategoryFilter";

protected static final String TABLEDATA_TRANSACTION_FILTER = "TransactionFilter";

protected static final String TABLEDATA_CONTACT_FILTER = "ContactFilter";

protected static final String TABLEDATA_TREE_OBJECT = "TreeObject";

	//	private TopicTreeViewer me = this;
	private TreeViewer internalTreeViewer;
	
	protected TreeObject root;
	private TreeObject all;

	// Display a transaction item, only if it is a tree of documents
	private TreeObject transactionItem;
	// Display a contact item, only if it is a tree of documents
	private TreeObject contactItem;

	// The input
	private EventList<T> inputElement;

	// The selected item
	private TreeObject selectedItem;

	// True, if there is a entry "show all"
	final boolean useAll;

	// The corresponding table
	private RHENatTable viewDataSetTable;
	
	/**
	 * the concrete Class of this TreeViewer
	 */
	private Class<T> elementClass;

	/**
	 * Constructor Creates a
	 * 
	 * @param parent
	 * @param style
	 * @param elementClass the concrete Class of this TreeViewer
	 * @param useDocumentAndContactFilter
	 * @param useAll
	 */
	public TopicTreeViewer(Composite parent, /*int style, */final Class<T> elementClass, boolean useDocumentAndContactFilter, final boolean useAll) {
		this.internalTreeViewer = new TreeViewer(parent, SWT.BORDER /*style*/);
		this.useAll = useAll;
		this.elementClass = elementClass;
		
		// Create a new root element
		root = new TreeObject("");
		root.setNodeType(TreeObjectType.ROOT_NODE);
		// select nothing
		selectedItem = null;

		// Add a "show all" entry
		if (useAll) {
			//T: Tree viewer entry for "show all"
			all = new TreeObject("all");
			all.setNodeType(TreeObjectType.ALL_NODE);
			root.addChild(all);
		}

		// Add a transaction and contact entry
		if (useDocumentAndContactFilter) {
			transactionItem = new TreeObject("---", "document_10.png");
			transactionItem.setNodeType(TreeObjectType.TRANSACTIONS_ROOTNODE);
			//T: Tool Tip Text
			transactionItem.setToolTip("Show all documents that belong to this transaction.");

			contactItem = new TreeObject("---", "contact_10.png");
			contactItem.setNodeType(TreeObjectType.CONTACTS_ROOTNODE);
			//T: Tool Tip Text
			contactItem.setToolTip("Show all documents that belong to this customer.");
		}
		
		
		// If an element of the tree is selected, update the filter
		internalTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {

				TreeObject treeObject = null;
				
				// Update the category, transaction and contact filter
				String categoryFilter = "";
				int transactionFilter = -1;
				int contactFilter = -1;
				ISelection selection = event.getSelection();

				// Get the selection
				if (selection != null && selection instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection) selection).getFirstElement();
					if (obj != null) {

						// Get the selected object
						treeObject = (TreeObject) obj;
						selectedItem = treeObject;

						// Update the category, transaction and contact filter
						categoryFilter = treeObject.getFullPathName();

						transactionFilter = treeObject.getTransactionId();
						contactFilter = treeObject.getContactId();
					}
				}

//				// Set a reference to the tree object to use the
//				// tool tip hint for displaying the total sum.
//				if (elementClass.equals(TreeObject.class)) // TODO
//					viewDataSetTable.setData(TABLEDATA_TREE_OBJECT, treeObject);
				
				if (contactFilter >= 0)
					// Set the contact filter
					viewDataSetTable.setContactFilter(contactFilter);

				else if (transactionFilter >= 0)
					// Set the transaction filter
					viewDataSetTable.setTransactionFilter(transactionFilter);

				else {
					if (!useAll && categoryFilter.isEmpty()) {
						// Show nothing
						viewDataSetTable.setCategoryFilter("$shownothing");
					} else {
						// Set the category filter
						viewDataSetTable.setCategoryFilter(categoryFilter);
					}
				}
			}
		});
	}
		
	public Tree getTree() {
		return internalTreeViewer.getTree();
	}
	/**
	 * Clear the tree
	 */
	private void clear() {
		if (all != null)
			all.clear();

		root.clear();

		// Add the transaction item
		if (transactionItem != null)
			root.addChild(transactionItem);

		// Add the contact item
		if (contactItem != null)
			root.addChild(contactItem);

		// Add the "show all" item
		if (all != null)
			root.addChild(all);

		// Reset the marker for "category has changed"
		// FIXME ???
//		if (inputElement != null)
//			inputElement.resetCategoryChanged();
	}

	/**
	 * Content provider for the tree view
	 * 
	 * @author Gerd Bartelt
	 */
	class ViewContentProvider extends TreeNodeContentProvider {

		/**
		 * Returns the elements to display in the viewer when its input is set
		 * to the given element.
		 */
		@Override
		public Object[] getElements(Object parent) {
			int entryCnt = 0;

			// Get the elements
			if (parent == root) {

				// Rebuild the elements, if some strings have changed
				// => this replaces "getCategoryStringsChanged()"
				// FIXME zur Zeit ändert sich hier nichts...
//				internalTreeViewer.aL
//				inputElement.addListEventListener(new ListEventListener<MyRowObject>() {
//
//					@Override
//					public void listChanged(ListEvent<MyRowObject> listChanges) {
					// Clear the tree
					clear();
					if (inputElement instanceof List) {

						// Get all category strings
						for (T entry : inputElement) {
							// Start with the root or all element
							addEntry(all != null ? all : root, entry);
						}
					}
//						
//				}});
			}
			
			// Count the category strings
			if (inputElement instanceof List) {
				Object[] entries = inputElement.toArray();
				entryCnt = entries.length;
			}

			// Hide the Tree viewer, if there is no tree element
			if (entryCnt != 0) {
				internalTreeViewer.getTree().setVisible(true);
				GridDataFactory.fillDefaults().hint(150, -1).grab(false, true).applyTo(internalTreeViewer.getTree());
				internalTreeViewer.getTree().getParent().layout(true);
			} else {
				internalTreeViewer.getTree().setVisible(false);
				GridDataFactory.fillDefaults().hint(1, -1).grab(false, true).applyTo(internalTreeViewer.getTree());
				internalTreeViewer.getTree().getParent().layout(true);
			}

			// Return the children elements
			return getChildren(parent);
		}

		/**
		 * Returns the parent element
		 */
		@Override
		public Object getParent(Object child) {
			if (child instanceof TreeObject) { return ((TreeObject) child).getParent(); }
			return null;
		}

		/**
		 * Returns the children elements
		 */
		@Override
		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeObject) { return ((TreeObject) parent).getChildren(); }
			return new Object[0];
		}

		/**
		 * Returns, if the element has children
		 */
		@Override
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeObject)
				return ((TreeObject) parent).hasChildren();
			return false;
		}
	}

	/**
	 * The label provider for the topic tree
	 * 
	 * @author Gerd Bartelt
	 */
	class ViewLabelProvider extends CellLabelProvider {

		/**
		 * The LabelProvider implementation of this ILabelProvider method
		 * returns the element's toString string
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		//@Override
		public String getText(Object obj) {

			// Display the localizes list names.
			if (viewDataSetTable instanceof RHENatTable)
//				return DataSetListNames.NAMES.getLocalizedName(obj.toString());
				return "loc: " + obj.toString();
			else
				return obj.toString();
		}

		/**
		 * The LabelProvider implementation of this ILabelProvider method
		 * returns null
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object obj) {

			// Get the icon string of the element
			String icon = ((TreeObject) obj).getIcon();
			if (icon != null) {
//					// Load the icon by the icon name from the image map
//					return imageMap.get(icon);
			}

			// Return a "dot" icon for parent elements
			/*
			 * FIXME
			 * This causes the expand button beside the parent node ("all") appearing very small.
			 */
//			if (obj instanceof TreeObject) { return Icon.TREE_DOT.getImage(IconSize.MiniIconSize); }

			// Return no icon
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipText(java.lang.Object)
		 */
		@Override
		public String getToolTipText(Object element) {
			return ((TreeObject) element).getToolTip();
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
		 */
		@Override
		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
			cell.setImage(getImage(cell.getElement()));
		}
	}

	/**
	 * Add a new entry to the tree
	 * 
	 * @param entry
	 *            The new entry to add
	 * @return
	 */
	private TreeObject addEntry(TreeObject parent, AbstractCategory entry) {
//TODO anpassen auf Category!!!!
		// das sind die parent-Beziehungen!
//		String[] entryParts = entry.split("/");
		TreeObject me = parent, newMe;

		// Use all string parts to build the tree elements
		if(entry.getParent() != null) {
			newMe = addEntry(me, entry.getParent());
		} else {
			newMe = new TreeObject(entry.getName());
			me.addChild(newMe);
			me = newMe;
		}
		return me;
	}

	/**
	 * Set the transaction Filter
	 * 
	 * @param name
	 *            Name of the transaction
	 * @param transactionId
	 *            ID of the transaction
	 */
	public void setTransaction(String name, int transactionId) {

		if (transactionItem == null)
			return;

		// Set the filter ID
		transactionItem.setTransactionId(transactionId);

		// Set the name
		//T: Topic Tree Viewer transaction title
		transactionItem.setName("Transaction");
		
		internalTreeViewer.refresh();
	}

	/**
	 * Returns the name of the selected item
	 * 
	 * @return The name of the item
	 */
	public String getSelectedItemName() {
		if (selectedItem != null)
			return selectedItem.getFullPathName();
		else
			return "";
	}

	/**
	 * Select an item by its name
	 * 
	 * @param name
	 *            of the item to select
	 */
	public void selectItemByName(String name) {
		boolean found = false;
		boolean allScanned = true;

		//Split the name into parts, separated by a slash "/"
		String[] nameParts = name.split("/");

		boolean childfound = false;
		TreeItem newParent = null;
		TreeItem[] children;
		children = internalTreeViewer.getTree().getItems();

		// Scan all parts of the input string
		for (String namePart : nameParts) {
			found = true;

			// Reached the end of the tree
			if (children.length == 0)
				allScanned = false;

			// Search all tree items for one with the same name
			for (TreeItem item : children) {
				if (item.getText().equalsIgnoreCase(namePart)) {
					childfound = true;
					newParent = item;
				}
			}

			// No child was found
			if (!childfound)
				found = false;

			// Get the next children
			if (newParent != null)
				children = newParent.getItems();
		}

		// Select the item, if it was found
		if (found && allScanned) {
			internalTreeViewer.getTree().setSelection(newParent);
			internalTreeViewer.setSelection(internalTreeViewer.getSelection(), true);
		}

		// Reset the filter to the new entry
		viewDataSetTable.setCategoryFilter(name);
	}

	/**
	 * Set the contact Filter
	 * 
	 * @param name
	 *            Name of the contact
	 * @param contactId
	 *            ID of the contact
	 */
	public void setContact(String name, int contactId) {
		if (contactItem == null)
			return;
		contactItem.setContactId(contactId);
		contactItem.setName(name);
		internalTreeViewer.refresh();
	}

	/**
	 * Sets the input of the tree
	 * 
	 * @param input
	 */
	public void setInput(EventList<T> input) {
		this.inputElement = input;
//		this.inputElement.resetCategoryChanged();
		internalTreeViewer.setContentProvider(new ViewContentProvider());
		internalTreeViewer.setLabelProvider(new ViewLabelProvider());
		internalTreeViewer.setInput(root);

		// Expand the tree only to level 2
		internalTreeViewer.expandToLevel(2);
		ColumnViewerToolTipSupport.enableFor(this.internalTreeViewer);

	}

	/**
	 * Set the table that corresponds to this tree viewer.
	 * Used for updating the filter for the table.
	 *  
	 * @param viewDataSetTable
	 *            The table of the view
	 */
	public void setTable(RHENatTable viewDataSetTable) {
		this.viewDataSetTable = viewDataSetTable;
	}

	public void setLabelProvider(RHEViewTableLabelProvider rheViewTableLabelProvider) {
		internalTreeViewer.setLabelProvider(rheViewTableLabelProvider);
	}
}
