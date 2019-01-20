package com.sebulli.fakturama.views.datatable.vats;

import ca.odell.glazedlists.matchers.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * {@link Matcher} class for filtering the VAT list entries. The {@link VATMatcher} checks if
 * an item has the selected category (selected from tree viewer).
 *
 */
final class VATMatcher implements Matcher<VAT> {
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
	public VATMatcher(String pVatCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.vatCategoryName = (treeObjectType == TreeObjectType.LEAF_NODE) ? pVatCategoryName : StringUtils.appendIfMissing(pVatCategoryName, "/");
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
		this.rootNodeName = "/" + rootNodeName + "/";
	}

	@Override
	public boolean matches(VAT item) {
		boolean found = false;
		if(!isRootNode) {
		    String fullCategoryName =  StringUtils.appendIfMissing(CommonConverter.getCategoryName(item.getCategory(), rootNodeName), "/");
		    found = fullCategoryName.startsWith(vatCategoryName);
		}
		return isRootNode || found;
	}
}