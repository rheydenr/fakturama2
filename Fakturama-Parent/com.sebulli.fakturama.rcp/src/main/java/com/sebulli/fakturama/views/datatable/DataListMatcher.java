/**
 * 
 */
package com.sebulli.fakturama.views.datatable;

import com.sebulli.fakturama.model.IDescribableEntity;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.matchers.Matcher;

/**
 *
 */
public class DataListMatcher<T extends IDescribableEntity> implements Matcher<T>{

	    final String paymentCategoryName;
	    final boolean isRootNode;
	    private final String rootNodeName;
	    
	    /**
	     * Constructor
	     * 
	     * @param pPaymentCategoryName category name which is selected in tree viewer
	     * @param treeObjectType the selected {@link TreeObjectType}
	     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
	     */
	    public DataListMatcher(String pPaymentCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
	        this.paymentCategoryName = pPaymentCategoryName;
	        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
	        this.rootNodeName = "/" + rootNodeName + "/";
	    }

	    @Override
	    public boolean matches(T item) {
	        boolean found = false;
	        if(!isRootNode) {
	        	// FIXME: Doesn't work because Texo can't create a concrete category on each IEntity.
	        	// Therefore we can't determine the item category. 
	            String fullCategoryName = null; //CommonConverter.getCategoryName(item.getCategory(), rootNodeName);
	            if(fullCategoryName.startsWith(paymentCategoryName)) {
	                found = true;
	            }
	        }
	        return isRootNode || found;
	    }

}
