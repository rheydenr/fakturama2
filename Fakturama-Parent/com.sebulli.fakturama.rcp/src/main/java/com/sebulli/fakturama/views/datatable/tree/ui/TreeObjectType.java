/**
 * 
 */
package com.sebulli.fakturama.views.datatable.tree.ui;

/**
 * Types of various tree items within the TopicTreeViewer
 *
 */
public enum TreeObjectType {
	/**
	 * the node of all nodes...
	 */
	ROOT_NODE(null),
	
	/**
	 * used for "all" selection
	 */
	ALL_NODE("all"),
	
	/** 
	 * filter for transactions
	 * (within documents) 
	 */
	TRANSACTIONS_ROOTNODE("---"),
	
	/** 
	 * filter for contacts
	 * (within documents) 
	 */
	CONTACTS_ROOTNODE("---"),
	
	/**
	 * a normal parent or leaf node
	 */
	DEFAULT_NODE("");
	
	private String defaultName;

    /**
     * @param key
     */
    private TreeObjectType(String key) {
        this.defaultName = key;
    }

    /**
     * @return the key
     */
    public String getDefaultName() {
        return defaultName;
    }
	
}
