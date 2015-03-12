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
 
package com.sebulli.fakturama.views.datatable.contacts;

import ca.odell.glazedlists.matchers.Matcher;

import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.parts.converter.CommonConverter;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * {@link Matcher} class for filtering the VAT list entries. The {@link ShippingMatcher} checks if
 * an item has the selected category (selected from tree viewer).
 *
 */
final public class ContactMatcher implements Matcher<Contact> {
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
    public ContactMatcher(String pContactCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.contactCategoryName = pContactCategoryName;
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
        this.rootNodeName = "/" + rootNodeName + "/";
    }

    @Override
    public boolean matches(Contact item) {
        boolean found = false;
        if(!isRootNode) {
            // a contact can have more than one category,
            // therefore we have to iterate over all them
            // TODO for now, we have only one category
            String fullCategoryName;
//            for (ContactCategory category : item.getCategories()) {
                fullCategoryName = CommonConverter.getCategoryName(item.getCategories(), rootNodeName);
                if(fullCategoryName.startsWith(contactCategoryName)) {
                    found = true;
//                    break;
                }
//            }
        }
        return isRootNode || found;
    }

}
