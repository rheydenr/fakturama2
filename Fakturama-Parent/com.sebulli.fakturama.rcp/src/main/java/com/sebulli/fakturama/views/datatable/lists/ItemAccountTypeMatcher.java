package com.sebulli.fakturama.views.datatable.lists;

import ca.odell.glazedlists.matchers.Matcher;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * {@link Matcher} class for filtering the ItemAccountType list entries. The {@link ItemAccountTypeMatcher} checks if
 * an item has the selected category (selected from tree viewer).
 *
 */
final class ItemAccountTypeMatcher implements Matcher<ItemAccountType> {
	final String vatCategoryName;
	final boolean isRootNode;
    private final String rootNodeName;
	
    /**
     * Constructor
     * 
     * @param pVatCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     */
	public ItemAccountTypeMatcher(String pVatCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
		this.vatCategoryName = pVatCategoryName;
		this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
		this.rootNodeName = "/" + rootNodeName + "/";
	}

	@Override
	public boolean matches(ItemAccountType item) {
		boolean found = false;
		if(!isRootNode) {
		    String fullCategoryName = CommonConverter.getCategoryName(item.getCategory(), rootNodeName);
			if(fullCategoryName.startsWith(vatCategoryName)) {
				found = true;
			}
		}
		return isRootNode || found;
	}
}