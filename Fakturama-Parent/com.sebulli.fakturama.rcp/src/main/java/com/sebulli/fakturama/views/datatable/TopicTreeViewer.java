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



import static com.sebulli.fakturama.Translate._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import com.sebulli.fakturama.model.Documents;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * This is the topic tree viewer that displays an tree of the categories of all
 * the data sets.
 * 
 * @author Gerd Bartelt
 */
public class TopicTreeViewer<T> extends TreeViewer {

	private TopicTreeViewer me = this;

	// Stores the dot image
	private ImageDescriptor dotImage;
	
	// Image map that stores all the used icons
	private Map<String,ImageDescriptor> imageMap = new HashMap<>();
	
	protected TreeParent root;
	private TreeParent all;

	// Display a transaction item, only if it is a tree of documents
	private TreeParent transactionItem;
	// Display a contact item, only if it is a tree of documents
	private TreeParent contactItem;

	// The input
	private List<T> inputElement;

	// The selected item
	private TreeObject selectedItem;

	// True, if there is a entry "show all"
	final boolean useAll;

	// The corresponding table
	private AbstractViewDataTable viewDataSetTable;

	
	/**
	 * Constructor Creates a
	 * 
	 * @param parent
	 * @param style
	 * @param useDocumentAndContactFilter
	 * @param useAll
	 */
	public TopicTreeViewer(Composite parent, int style, final Class<?> elementClass, boolean useDocumentAndContactFilter, boolean useAll) {
		super(parent, style);
		this.useAll = useAll;
		
		// Create a dot Image
//		LocalResourceManager resources = new LocalResourceManager(JFaceResources.getResources());
//		dotImage = resources.createImage(Activator.getImageDescriptor("/icons/10/dot_10.png"));
//		dotImage = resourcePool.getImageUnchecked(ResourceProvider.IMG_DOT_10);
		dotImage = Icon.TREE_DOT.getImageDescriptor(IconSize.MiniIconSize);
		
		// Create a new root element
		root = new TreeParent("");
		// select nothing
		selectedItem = null;

		// Add a "show all" entry
		if (useAll) {
			//T: Tree viewer entry for "show all"
			all = new TreeParent(_("all"));
			root.addChild(all);
		}

		// Add a transaction and contact entry
		if (useDocumentAndContactFilter) {
			transactionItem = new TreeParent("---", "document_10.png");
			//T: Tool Tip Text
			transactionItem.setToolTip(_("Show all documents that belong to this transaction."));

			contactItem = new TreeParent("---", "contact_10.png");
			//T: Tool Tip Text
			contactItem.setToolTip(_("Show all documents that belong to this customer."));
			

		}

		// If an element of the tree is selected, update the filter
		this.addSelectionChangedListener(new ISelectionChangedListener() {

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

				// Set a reference to the tree object to use the
				// tool tip hint for displaying the total sum.
				if (elementClass.equals(Documents.class))
					viewDataSetTable.setTreeObject(treeObject);
				
				if (contactFilter >= 0)

					// Set the contact filter
					viewDataSetTable.setContactFilter(contactFilter);

				else if (transactionFilter >= 0)

					// Set the transaction filter
					viewDataSetTable.setTransactionFilter(transactionFilter);

				else {
					if (!me.useAll && categoryFilter.isEmpty())
						// Show nothing
						viewDataSetTable.setCategoryFilter("$shownothing");
					else
						// Set the category filter
						viewDataSetTable.setCategoryFilter(categoryFilter);
				}
			}
		});
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
		if (inputElement != null) {
			// FIXME implement
			//inputElement.resetCategoryChanged();
		}
	}

	/**
	 * This class represents a tree object of the Topic Tree
	 * 
	 * @author Gerd Bartelt
	 */
	class TreeObject<T> {
		private String name;
		private String command;
		private String toolTip;
		private TreeParent parent;
		private String icon;
		private int transactionId = -1;
		private int contactId = -1;

		/**
		 * Constructor Create a tree object by name
		 * 
		 * @param name
		 *            Name of the tree object
		 */
		public TreeObject(String name) {
			this.command = null;
			this.name = name;
			this.icon = null;
			this.toolTip = null;
		}

		/**
		 * Constructor Create a tree object by name and icon
		 * 
		 * @param name
		 *            Name of the tree object
		 * @param icon
		 *            Icon of the tree object
		 */
		public TreeObject(String name, String icon) {
			this.command = null;
			this.name = name;
			this.icon = icon;
			this.toolTip = null;
		}

		/**
		 * Constructor Create a tree object by name and icon
		 * 
		 * @param name
		 *            Name of the tree object
		 * @param command
		 *            of the tree object
		 * @param icon
		 *            Icon of the tree object
		 */
		public TreeObject(String name, String command, String icon) {
			this.name = name;
			this.command = command;
			this.icon = icon;
			this.toolTip = null;
		}

		/**
		 * Returns the tool tip text of the tree object
		 * 
		 * @return The name
		 */
		public String getToolTip() {
			return toolTip;
		}

		/**
		 * Returns the name of the tree object
		 * 
		 * @return The name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the icon of the tree object
		 * 
		 * @return The icon
		 */
		public String getIcon() {
			return icon;
		}

		/**
		 * Returns the command of the tree object
		 * 
		 * @return The command
		 */
		public String getCommand() {
			return command;
		}

		/**
		 * Returns the ID of the transaction
		 * 
		 * @return Transaction ID
		 */
		public int getTransactionId() {
			return this.transactionId;
		}

		/**
		 * Returns the ID of the contact
		 * 
		 * @return Contact ID
		 */
		public int getContactId() {
			return this.contactId;
		}

		/**
		 * Sets the tool tip text
		 * 
		 * @param toolTip
		 *            the tool tip text
		 */
		public void setToolTip(String toolTip) {
			this.toolTip = toolTip;
		}

		/**
		 * Sets the Transaction ID
		 * 
		 * @param transactionId
		 *            ID to set
		 */
		public void setTransactionId(int transactionId) {
			this.transactionId = transactionId;
		}

		/**
		 * Sets the contact ID
		 * 
		 * @param contactId
		 *            ID to set
		 */
		public void setContactId(int contactId) {
			this.contactId = contactId;
		}

		/**
		 * Sets the name of the tree object
		 * 
		 * @param name
		 *            The name of the tree object
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Sets the parent object
		 * 
		 * @param parent
		 */
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}

		/**
		 * Returns the parent object
		 * 
		 * @return The parent object
		 */
		public TreeParent getParent() {
			return parent;
		}

		/**
		 * Returns a string representation of the object.
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getName();
		}

		/**
		 * Returns the full path of the tree element. This is the name of the
		 * element plus the name of all parent elements, separated by a slash
		 * "/"
		 * 
		 * @return The full path of a tree element
		 */
		public String getFullPathName() {

			// Get the name of the element
			String fullPathName = getName();

			// Root and all have an empty path
			if ((this == root) || (this == all))
				return "";

			TreeObject p = this;
			p = p.getParent();

			//Add the name of all parent elements
			while ((p != null) && (p != all) && (p != root)) {
				fullPathName = p.getName() + "/" + fullPathName;
				p = p.getParent();
			}

			// The full path name
			return fullPathName;
		}
	}

	/**
	 * This class represents a tree object, that can be the parent of an other
	 * tree object
	 * 
	 * @author Gerd Bartelt
	 */
	class TreeParent extends TreeObject {

		// List with all children
		private ArrayList<TreeObject> children;

		/**
		 * Constructor Create a parent element by a name
		 * 
		 * @param name
		 *            the name of the new object
		 */
		public TreeParent(String name) {
			super(name);
			children = new ArrayList<TreeObject>();
		}

		/**
		 * Constructor Create a parent element by a name and an icon
		 * 
		 * @param name
		 *            The name of the new object
		 * @icon The name of the icon
		 */
		public TreeParent(String name, String icon) {
			super(name, icon);
			children = new ArrayList();
		}

		/**
		 * Add a child to the tree object
		 * 
		 * @param child
		 *            The child to add
		 */
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}

		/**
		 * Remove a child from the tree object
		 * 
		 * @param child
		 *            to remove
		 */
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}

		/**
		 * Returns all children
		 * 
		 * @return Array with all children
		 */
		public TreeObject[] getChildren() {
			return children.toArray(new TreeObject[children.size()]);
		}

		/**
		 * Returns whether the parent has children or not
		 * 
		 * @return True, if there are children
		 */
		public boolean hasChildren() {
			return children.size() > 0;
		}

		/**
		 * Remove the object's icon
		 */
		public void removeIcon() {
			((TreeObject) this).icon = null;
		}

		/**
		 * Remove all children
		 */
		public void clear() {
			children.clear();
		}

	}

	/**
	 * Content provider for the tree view
	 * 
	 * @author Gerd Bartelt
	 */
	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		/**
		 * Notifies this content provider that the given viewer's input has been
		 * switched to a different element.
		 */
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {

		}

		/**
		 * Disposes of this content provider.
		 */
		@Override
		public void dispose() {
		}

		/**
		 * Returns the elements to display in the viewer when its input is set
		 * to the given element.
		 */
		@Override
		public Object[] getElements(Object parent) {
			int entryCnt = 0;

//			// Get the elements
//			if (parent == root) {
//
//				// Rebuild the elements, if some strings have changed
//				if (inputElement.getCategoryStringsChanged()) {
//
//					// Clear the tree
//					clear();
//					if (inputElement instanceof IDataSetArray) {
//
//						// Get all category strings
//						Object[] entries = inputElement.getCategoryStrings().toArray();
//						for (Object entry : entries) {
//							addEntry(entry.toString());
//						}
//					}
//				}
//			}
//
//			// Count the category strings
//			if (inputElement instanceof IDataSetArray) {
//				Object[] entries = inputElement.getCategoryStrings().toArray();
//				entryCnt = entries.length;
//			}
//
//			// Hide the Tree viewer, if there is no tree element
//			if (entryCnt != 0) {
//				me.getTree().setVisible(true);
//				GridDataFactory.fillDefaults().hint(150, -1).grab(false, true).applyTo(me.getTree());
//				me.getTree().getParent().layout(true);
//			}
//			else {
//				me.getTree().setVisible(false);
//				GridDataFactory.fillDefaults().hint(1, -1).grab(false, true).applyTo(me.getTree());
//				me.getTree().getParent().layout(true);
//			}

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
//			if (parent instanceof TreeParent) { return ((TreeParent) parent).getChildren(); }
			return new Object[0];
		}

		/**
		 * Returns, if the element has children
		 */
		@Override
		public boolean hasChildren(Object parent) {
//			if (parent instanceof TreeParent)
//				return ((TreeParent) parent).hasChildren();
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
			if (viewDataSetTable instanceof AbstractViewDataTable)
				return "mpf!"; //DataSetListNames.NAMES.getLocalizedName(obj.toString());
			else
				return obj.toString();
		}

		/**
		 * The LabelProvider implementation of this ILabelProvider method
		 * returns null
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		//@Override
		public ImageDescriptor getImage(Object obj) {

			// Get the icon string of the element
			String icon = ((TreeObject) obj).getIcon();
			if (icon != null) {
				try {
					//Store the icon in the image map, if it not already there
					if (!imageMap.containsKey(icon)) {
//						LocalResourceManager resources = new LocalResourceManager(JFaceResources.getResources());
//						imageMap.put(icon, resources.createImage(Activator.getImageDescriptor("/icons/10/" + icon)));
//						imageMap.put(icon, resourcePool.getImageUnchecked(ResourceProvider.IMG_DOT_10));
						imageMap.put(icon, Icon.TREE_DOT.getImageDescriptor(IconSize.MiniIconSize));
					}
					// Load the icon by the icon name from the image map
					return imageMap.get(icon);

				}
				catch (AssertionFailedException e) {
//					Logger.logError(e, "Icon not found: " + icon);
//					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
				}

			}

			// Return a "dot" icon for parent elements
//			if (obj instanceof TreeParent) { return dotImage; }
//
			// Return no icon
			return null; //PlatformUI.getWorkbench().getSharedImages().getImage(null);
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
			cell.setImage(getImage(cell.getElement()).createImage());
		}

		
	}

	/**
	 * Add a new entry to the tree
	 * 
	 * @param entry
	 *            The new entry to add
	 * @return
	 */
	private TreeParent addEntry(String entry) {

		// Split the input into string parts, all separated  by "/"
		// Every slash separates the elements and will generate a new
		// tree branch.
		String[] entryParts = entry.split("/");

		TreeParent me;

		// Start with the root or all element
		if (all != null)
			me = all;
		else
			me = root;

		TreeParent newMe;
		boolean found;

		// Use all string parts to build the tree elements
		for (String entryPart : entryParts) {
			found = false;

			// Get all children
			if (me.hasChildren()) {
				for (TreeObject treeObject : me.children) {

					// Check, whether there is already an entry with the
					// same name. If there is one, do not create a new.
					if (treeObject.getName().equalsIgnoreCase(entryPart)) {
						me = (TreeParent) treeObject;
						found = true;
						break;
					}
				}
			}

			// Create only a new entry, if there is not already an old one with
			// the same name
			if (!found) {
				me.addChild(newMe = new TreeParent(entryPart));
				me = newMe;
			}
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
		transactionItem.setName(_("Transaction"));
		
		refresh();
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
		children = me.getTree().getItems();

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
			me.getTree().setSelection(newParent);
			me.setSelection(me.getSelection(), true);
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
		refresh();
	}

	/**
	 * Sets the input of the tree
	 * 
	 * @param input
	 */
	public void setInput(List<T> input) {
		this.inputElement = input;
//		this.inputElement.resetCategoryChanged();
		this.setContentProvider(new ViewContentProvider());
		this.setLabelProvider(new ViewLabelProvider());
		this.setInput(root);

		// Expand the tree only to level 2
		this.expandToLevel(2);
		ColumnViewerToolTipSupport.enableFor(this);

	}

	/**
	 * Set the table that corresponds to this tree viewer
	 * 
	 * @param viewDataSetTable
	 *            The table of the view
	 */
	public void setTable(AbstractViewDataTable viewDataSetTable) {
		this.viewDataSetTable = viewDataSetTable;
	}

}
