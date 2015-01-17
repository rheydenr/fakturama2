package com.sebulli.fakturama.views.datatable.shippings;

import ca.odell.glazedlists.matchers.Matcher;

import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.parts.converter.CommonConverter;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * {@link Matcher} class for filtering the VAT list entries. The {@link ShippingMatcher} checks if
 * an item has the selected category (selected from tree viewer).
 *
 */
final class ShippingMatcher implements Matcher<Shipping> {
	final String shippingCategoryName;
	final boolean isRootNode;
    private final String rootNodeName;
	
    /**
     * Constructor
     * 
     * @param pShippingCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     */
	public ShippingMatcher(String pShippingCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
		this.shippingCategoryName = pShippingCategoryName;
		this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
		this.rootNodeName = "/" + rootNodeName + "/";
	}

	@Override
	public boolean matches(Shipping item) {
		boolean found = false;
		if(!isRootNode) {
		    String fullCategoryName = CommonConverter.getCategoryName(item.getCategories(), rootNodeName);
			if(fullCategoryName.startsWith(shippingCategoryName)) {
				found = true;
			}
		}
		return isRootNode || found;
	}
}