/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.dialogs;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.DebitorAddress;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * {@link Matcher} class for filtering the Contact list entries. The {@link ContactTreeMatcher} checks if
 * an item has the selected category (selected from tree viewer).
 *
 */
final public class ContactTreeMatcher<K extends DebitorAddress> implements Matcher<K> {
    final String contactCategoryName;
    final boolean isRootNode;
    private final String rootNodeName;
    
    /**
     * Constructor
     * 
     * @param pContactCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     */
    public ContactTreeMatcher(String pContactCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.contactCategoryName = (treeObjectType == TreeObjectType.LEAF_NODE) ? pContactCategoryName : StringUtils.appendIfMissing(pContactCategoryName, "/");
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
        this.rootNodeName = "/" + rootNodeName + "/";
    }

    @Override
    public boolean matches(K item) {
        boolean found = false;
        if(!isRootNode) {
            String fullCategoryName = CommonConverter.getCategoryName(item.getCategory(), rootNodeName);
            found = fullCategoryName.startsWith(contactCategoryName);
        }
        return isRootNode || found;
    }

}
