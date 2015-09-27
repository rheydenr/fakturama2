/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable.products;

import ca.odell.glazedlists.matchers.Matcher;

import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.parts.converter.CommonConverter;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 *
 */
public class ProductMatcher implements Matcher<Product> {
    final String productCategoryName;
    final boolean isRootNode;
    private final String rootNodeName;
    
    /**
     * Constructor
     * 
     * @param pProductCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     */
    public ProductMatcher(String pProductCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.productCategoryName = pProductCategoryName;
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
        this.rootNodeName = "/" + rootNodeName + "/";
    }

    @Override
    public boolean matches(Product item) {
        boolean found = false;
        if(!isRootNode) {
            String fullCategoryName = CommonConverter.getCategoryName(item.getCategories() == null/*.isEmpty()*/ ? null : item.getCategories()/*.get(0)*/, rootNodeName);
            if(fullCategoryName.startsWith(productCategoryName)) {
                found = true;
            }
        }
        return isRootNode || found;
    }

}
