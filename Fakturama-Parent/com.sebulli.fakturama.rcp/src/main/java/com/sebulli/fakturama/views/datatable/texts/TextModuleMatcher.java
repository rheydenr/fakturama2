package com.sebulli.fakturama.views.datatable.texts;

import ca.odell.glazedlists.matchers.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * {@link Matcher} class for filtering the VAT list entries. The {@link VATMatcher} checks if
 * an item has the selected category (selected from tree viewer).
 *
 */
final class TextModuleMatcher implements Matcher<TextModule> {
	final String textCategoryName;
	final boolean isRootNode;
    private final String rootNodeName;
	
    /**
     * Constructor
     * 
     * @param pTextCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     */
	public TextModuleMatcher(String pTextCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.textCategoryName = (treeObjectType == TreeObjectType.LEAF_NODE) ? pTextCategoryName : StringUtils.appendIfMissing(pTextCategoryName, "/");
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
		this.rootNodeName = "/" + rootNodeName + "/";
	}

	@Override
	public boolean matches(TextModule item) {
		boolean found = false;
		if(!isRootNode) {
		    // TODO change if we use real lists!
		    String fullCategoryName = CommonConverter.getCategoryName(item.getCategories(), rootNodeName);
		    found = fullCategoryName.startsWith(textCategoryName);
		}
		return isRootNode || found;
	}
}