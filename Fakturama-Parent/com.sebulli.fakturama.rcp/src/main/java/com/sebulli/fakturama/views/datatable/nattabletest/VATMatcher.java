package com.sebulli.fakturama.views.datatable.nattabletest;

import ca.odell.glazedlists.matchers.Matcher;

import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;

final class VATMatcher implements Matcher<VAT> {
	final String vatCategoryName;
	final boolean isRootNode;
	
	public VATMatcher(String pVatCategoryName, TreeObjectType treeObjectType) {
		this.vatCategoryName = pVatCategoryName;
		this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
	}

	@Override
	public boolean matches(VAT item) {
		boolean found = false;
		if(!isRootNode) {
			for (VATCategory vatCategory : item.getCategories()) {
				if(vatCategory.getName().equals(vatCategoryName)) {
					found = true;
					break;
				}
			}
		}
		return isRootNode || found;
	}
}