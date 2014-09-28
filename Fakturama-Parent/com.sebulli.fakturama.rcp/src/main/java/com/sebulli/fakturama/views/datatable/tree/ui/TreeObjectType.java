/**
 * 
 */
package com.sebulli.fakturama.views.datatable.tree.ui;

/**
 * Types of various tree items within the TopicTreeViewer
 * @author rheydenr
 *
 */
public enum TreeObjectType {
	/**
	 * the node of all nodes...
	 */
	ROOT_NODE,
	
	/**
	 * used for "all" selection
	 */
	ALL_NODE,
	
	/** 
	 * filter for transactions
	 * (within documents) 
	 */
	TRANSACTIONS_ROOTNODE,
	
	/** 
	 * filter for contacts
	 * (within documents) 
	 */
	CONTACTS_ROOTNODE,
	
	/**
	 * a normal parent or leaf node
	 */
	DEFAULT_NODE
}
