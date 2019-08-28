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

package com.sebulli.fakturama.views.datatable.tree.ui;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * This is the topic tree viewer that displays a tree of the categories of all
 * the data sets.
 * 
 */
public class TopicTreeViewer<T extends AbstractCategory> {

protected static final String TABLEDATA_CATEGORY_FILTER = "CategoryFilter";
protected static final String TABLEDATA_TRANSACTION_FILTER = "TransactionFilter";
protected static final String TABLEDATA_CONTACT_FILTER = "ContactFilter";
protected static final String TABLEDATA_TREE_OBJECT = "TreeObject";

    private Messages msg;

    TreeViewer internalTreeViewer;
	
	protected TreeObject root;
	TreeObject all;

	// Display a transaction item, only if it is a tree of documents
	private TreeObject transactionItem;
	// Display a contact item, only if it is a tree of documents
	private TreeObject contactItem;

	// The input
	EventList<T> inputElement;

	// The selected item
	private TreeObject selectedItem;

//	// True, if there is a entry "show all"
//	private final boolean useAll;

	// The corresponding table
	private AbstractViewDataTable<? extends IEntity, T> viewDataSetTable;
    private TreeObjectContentProvider<T> contentProvider;
    
	/**
	 * Constructor Creates a
	 * 
	 * @param parent
	 * @param msg2 
	 * @param style
	 * @param elementClass the concrete Class of this TreeViewer
	 * @param useDocumentAndContactFilter
	 * @param useAll
	 */
	public TopicTreeViewer(Composite parent, Messages msg, /*int style, */boolean useDocumentAndContactFilter, final boolean useAll) {
		this.internalTreeViewer = new TreeViewer(parent, SWT.BORDER /*style*/);
		// Messages can't be injected because this class is not called via application context
		this.msg = msg;
//		this.useAll = useAll;
		
		// Create a new root element
		root = new TreeObject("");
		root.setNodeType(TreeObjectType.ROOT_NODE);
		// select nothing
		selectedItem = null;

		// Add a "show all" entry
		if (useAll) {
			//T: Tree viewer entry for "show all"
			all = new TreeObject(msg.topictreeAll);
			all.setNodeType(TreeObjectType.ALL_NODE);
			root.addChild(all);
		}

		// Add a transaction and contact entry
		if (useDocumentAndContactFilter) {
			transactionItem = new TreeObject(TreeObjectType.TRANSACTIONS_ROOTNODE.getDefaultName(), Icon.TREE_DOCUMENT);
			transactionItem.setNodeType(TreeObjectType.TRANSACTIONS_ROOTNODE);
			//T: Tool Tip Text
			transactionItem.setToolTip(msg.topictreeAllDocumentsTooltip);
			root.addChild(transactionItem);
			
			contactItem = new TreeObject(TreeObjectType.CONTACTS_ROOTNODE.getDefaultName(), Icon.TREE_CONTACT);
			contactItem.setNodeType(TreeObjectType.CONTACTS_ROOTNODE);
			//T: Tool Tip Text
			contactItem.setToolTip(msg.topictreeAllCustomersTooltip);
			root.addChild(contactItem);
		}
		
		
		// If an element of the tree is selected, update the filter
		internalTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {

				TreeObject treeObject = selectedItem; // initially it's equal...
				
				// Update the category, transaction and contact filter
				String categoryFilter = "";
				long transactionFilter = -1;
				long contactFilter = -1;
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

				// Set a reference to the tree object to use the
				// tool tip hint for displaying the total sum.
//				if (elementClass.equals(TreeObject.class)) // TODO
//					viewDataSetTable.setData(TABLEDATA_TREE_OBJECT, treeObject);
				
				if (contactFilter >= 0)
					// Set the contact filter
					viewDataSetTable.setContactFilter(contactFilter);

				else if (transactionFilter >= 0)
					// Set the transaction filter
					viewDataSetTable.setTransactionFilter(transactionFilter, treeObject);

				else {
					if (!useAll && categoryFilter.isEmpty()) {
						// Show nothing
						viewDataSetTable.setCategoryFilter("$shownothing", treeObject.getNodeType());
					} else {
						// Set the category filter
						viewDataSetTable.setCategoryFilter(categoryFilter, treeObject.getNodeType());
					}
				}
				
				// change toolbar / popup menu
			    viewDataSetTable.changeToolbarItem(treeObject);
			}
		});
		
		internalTreeViewer.setComparator(new TreePathViewerSorter());
	}
		
	public Tree getTree() {
		return internalTreeViewer.getTree();
	}
	
	/**
	 * Clear the tree
	 */
	void clear() {
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
		if (inputElement != null) {
//		    internalTreeViewer.refresh();
//			inputElement.resetCategoryChanged();
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
//			if (viewDataSetTable instanceof VATListTable)
////				return DataSetListNames.NAMES.getLocalizedName(obj.toString());
//				return "loc: " + obj.toString();
//			else
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
			Icon icon = ((TreeObject) obj).getIcon();
			if (icon != null) {
				// Load the icon by the icon name from the image map
				return icon.getImage(IconSize.DefaultIconSize);
			}

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
	 * Set the transaction Filter
	 * 
	 * @param name
	 *            Name of the transaction
	 * @param transactionId
	 *            ID of the transaction
	 */
	public void setTransaction(long transactionId) {
		if (transactionItem == null)
			return;

		// Set the filter ID
		transactionItem.setTransactionId(transactionId);

		// Set the name
		if(transactionId > 0) {
			//T: Topic Tree Viewer transaction title
			transactionItem.setName(msg.topictreeLabelThistransaction);
		} else {
			transactionItem.setName(TreeObjectType.TRANSACTIONS_ROOTNODE.getDefaultName());
		}
		
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
	 * @param pName
	 *            of the item to select
	 */
	public void selectItemByName(final String pName) {
		boolean found = false;
		boolean allScanned = true;
		String name = pName;

		//Split the name into parts, separated by a slash "/"
		String[] nameParts = name.split("/");

		boolean childfound = false;
		TreeObject newParent = null;
		TreeObject[] children;
		children = root.getChildren();

		// Scan all parts of the input string
		for (String namePart : nameParts) {
			found = true;

			// Reached the end of the tree
			if (children.length == 0)
				allScanned = false;

			// Search all tree items for one with the same name
			Optional<TreeObject> foundResult = Arrays.stream(children).filter(i -> i.getName().equalsIgnoreCase(namePart)).findAny();
			if(foundResult.isPresent()) {
			    childfound = true;
			    newParent = foundResult.get();
			}

			// No child was found
			if (!childfound)
				found = false;

			// Get the next children
			if (newParent != null)
				children = newParent.getChildren();
		}

        // Select the item, if it was found
        if (found && allScanned) {
            internalTreeViewer.setSelection(new StructuredSelection(newParent), true);
            internalTreeViewer.reveal(newParent);
        } else {
            TreeObject firstNode = findFirstNamedNode();
            if(firstNode == null) {
                name = TreeObjectType.TRANSACTIONS_ROOTNODE.getDefaultName();
            } else {
                internalTreeViewer.setSelection(new StructuredSelection(firstNode), true);
                name = firstNode.getFullPathName();
            }
        }

		// Reset the filter to the new entry
		viewDataSetTable.setCategoryFilter(name, TreeObjectType.DEFAULT_NODE);
	}
	
    /**
     * Looks for the first named node in a Tree (the root node my be an unnamed
     * node).
     * 
     * @return <code>null</code> if no node is available
     */
    private TreeObject findFirstNamedNode() {
        TreeObject node = ((TreeObject) internalTreeViewer.getInput());
        if (node != null && node.getName().isEmpty() && node.getChildren().length > 0) {
            node = node.getChildren()[0];
        } else {
            node = null;
        }
        return node;
    }

	/**
	 * Set the contact Filter
	 * 
	 * @param name
	 *            Name of the contact
	 * @param contactId
	 *            ID of the contact
	 */
	public void setContactFromDocument(Document selectedDocument) {
		if (contactItem == null || selectedDocument == null)
			return;
		
		DocumentReceiver billingContact = selectedDocument.getReceiver().stream().filter(rcv -> rcv.getBillingType().isINVOICE()).findFirst().get();
		DocumentReceiver deliveryContact = selectedDocument.getReceiver().stream().filter(rcv -> rcv.getBillingType().isDELIVERY()).findFirst().get();
		DocumentReceiver contact = selectedDocument.getBillingType().isDELIVERY() ? deliveryContact : billingContact;
		String name = selectedDocument.getAddressFirstLine(); 
		contactItem.setContactId(contact.getId());
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
        contentProvider = new TreeObjectContentProvider<T>(this);
        internalTreeViewer.setContentProvider(contentProvider);
		internalTreeViewer.setLabelProvider(new ViewLabelProvider());
		internalTreeViewer.setInput(root);
		
		/*
		 * If an update of the underlying list occurs, the inputElement
		 * gets informed. Then it has to update the complete tree. This
		 * can only(!) be done in UI thread (else you get an ugly Exception).
		 * Therefore we have to run the refresh() method inside a separate 
		 * Runnable class.
		 * 
		 * see http://www.eclipsezone.com/eclipse/forums/t24195.html
		 * and http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fswt_threading.htm
		 */
		this.inputElement.addListEventListener(new ListEventListener<T>() {
		    /* (non-Javadoc)
		     * @see ca.odell.glazedlists.event.ListEventListener#listChanged(ca.odell.glazedlists.event.ListEvent)
		     */
		    @Override
		    public void listChanged(ListEvent<T> listChanges) {
		        // TODO alternative: use UISynchronize
		        internalTreeViewer.getControl().getDisplay().asyncExec(() -> {
                        if(!internalTreeViewer.getControl().getDisplay().isDisposed()) {
                            internalTreeViewer.refresh();
                        }
                    }
                );
		    }
        });

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
	public void setTable(AbstractViewDataTable<? extends IEntity, T> viewDataSetTable) {
		this.viewDataSetTable = viewDataSetTable;
	}

	public void setLabelProvider(TreeCategoryLabelProvider treeTableLabelProvider) {
		internalTreeViewer.setLabelProvider(treeTableLabelProvider);
	}
	
}
