package com.sebulli.fakturama.views.datatable.payments;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.IDescribableEntity;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.matchers.Matcher;

public class AbstractListViewMatcher<T extends IDescribableEntity> implements Matcher<T> {

	protected final String paymentCategoryName;
	protected final boolean isRootNode;
	protected final String rootNodeName;

    /**
     * Constructor
     * 
     * @param pPaymentCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     */
	public AbstractListViewMatcher(String pPaymentCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.paymentCategoryName = (treeObjectType == TreeObjectType.LEAF_NODE) ? pPaymentCategoryName : StringUtils.appendIfMissing(pPaymentCategoryName, "/");
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
        this.rootNodeName = "/" + rootNodeName + "/";
	}

	@Override
	public boolean matches(T item) {
	    boolean found = false;
	    if(!isRootNode) {
	        String fullCategoryName = CommonConverter.getCategoryName(null/*item.getCategory()*/, rootNodeName);
	        found = fullCategoryName.startsWith(paymentCategoryName);
	    }
	    return isRootNode || found;
	}

}