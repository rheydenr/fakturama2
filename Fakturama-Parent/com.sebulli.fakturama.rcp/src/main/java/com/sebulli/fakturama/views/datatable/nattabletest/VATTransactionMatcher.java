package com.sebulli.fakturama.views.datatable.nattabletest;

import ca.odell.glazedlists.matchers.Matcher;

import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;

final class VATTransactionMatcher implements Matcher<VAT> {
	final String vatTransactionCode;
	final boolean isRootNode;
	
	public VATTransactionMatcher(String pVatCategoryName) {
		this.vatTransactionCode = pVatCategoryName;
		this.isRootNode = pVatCategoryName.equals(RHENatTable.ROOT_NODE_NAME);
	}

	@Override
	public boolean matches(VAT item) {
		boolean found = false;
		if(!isRootNode) {
			for (VATCategory vatCategory : item.getCategories()) {
				if(vatCategory.getName().equals(vatTransactionCode)) {
					found = true;
					break;
				}
			}
		}
		return isRootNode || found;
	}
}