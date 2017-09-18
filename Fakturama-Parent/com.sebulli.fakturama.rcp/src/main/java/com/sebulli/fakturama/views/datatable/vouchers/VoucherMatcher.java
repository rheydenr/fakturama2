package com.sebulli.fakturama.views.datatable.vouchers;

import ca.odell.glazedlists.matchers.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * {@link Matcher} class for filtering the Voucher list entries. The {@link VoucherMatcher} checks if
 * an item has the selected category (selected from tree viewer).
 *
 */
final class VoucherMatcher implements Matcher<Voucher> {
	final String voucherCategoryName;
	final boolean isRootNode;
    private final String rootNodeName;
	
    /**
     * Constructor
     * 
     * @param pVoucherCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     */
	public VoucherMatcher(String pVoucherCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.voucherCategoryName = (treeObjectType == TreeObjectType.LEAF_NODE) ? pVoucherCategoryName : StringUtils.appendIfMissing(pVoucherCategoryName, "/");
		this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
		this.rootNodeName = "/" + rootNodeName + "/";
	}

	@Override
	public boolean matches(Voucher item) {
		boolean found = false;
		if(!isRootNode) {
		    String fullCategoryName = CommonConverter.getCategoryName(item.getAccount(), rootNodeName);
		    found = fullCategoryName.startsWith(voucherCategoryName);
		}
		return isRootNode || found;
	}
}