package com.sebulli.fakturama.views.datatable.tree.model;

import java.util.ArrayList;

/**
 * This class represents a tree object, that can be the parent of an other
 * tree object
 * 
 * @author Gerd Bartelt
 */
public class TreeParent extends TreeObject {
    
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
		children = new ArrayList<TreeObject>();
	}

	/**
	 * Add a child to the tree object
	 * 
	 * @param child
	 *            The child to add
	 */
	public void addChild(TreeParent child) {
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
		((TreeObject) this).removeIcon();
	}

	/**
	 * Remove all children
	 */
	public void clear() {
		children.clear();
	}
 
}
