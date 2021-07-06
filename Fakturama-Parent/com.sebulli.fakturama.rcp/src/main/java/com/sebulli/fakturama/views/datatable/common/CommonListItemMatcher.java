/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable.common;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.ICategorizable;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * Common class for List category matchers.
 *
 */
public class CommonListItemMatcher<T extends ICategorizable<? extends AbstractCategory>> implements Matcher<T>{
    private final String categoryName;
    private final boolean isRootNode;
    private final String rootNodeName;
    
    /**
     * Constructor
     * 
     * @param categoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     */
    public CommonListItemMatcher(String categoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.categoryName = (treeObjectType == TreeObjectType.LEAF_NODE) ? categoryName : StringUtils.appendIfMissing(categoryName, "/");
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
        this.rootNodeName = "/" + rootNodeName + "/";
    }

    @Override
    public boolean matches(T item) {
        boolean found = false;
        if(!isRootNode()) {
            String fullCategoryName =  StringUtils.appendIfMissing(CommonConverter.getCategoryName(item.getCategories(), rootNodeName), "/");
            found = fullCategoryName.startsWith(StringUtils.stripEnd(categoryName, "/"));
        }
        return isRootNode() || found;
    }

    public boolean isRootNode() {
        return isRootNode;
    }

}
