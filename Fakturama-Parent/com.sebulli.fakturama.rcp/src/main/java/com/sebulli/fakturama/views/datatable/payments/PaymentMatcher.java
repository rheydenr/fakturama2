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
 
package com.sebulli.fakturama.views.datatable.payments;

import ca.odell.glazedlists.matchers.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 *
 */
public class PaymentMatcher implements Matcher<Payment> {
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
    public PaymentMatcher(String pPaymentCategoryName, TreeObjectType treeObjectType, String rootNodeName) {
        this.paymentCategoryName = (treeObjectType == TreeObjectType.LEAF_NODE) ? pPaymentCategoryName : StringUtils.appendIfMissing(pPaymentCategoryName, "/");
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
        this.rootNodeName = "/" + rootNodeName + "/";
    }

    @Override
    public boolean matches(Payment item) {
        boolean found = false;
        if(!isRootNode) {
            String fullCategoryName = CommonConverter.getCategoryName(item.getCategory(), rootNodeName);
            found = fullCategoryName.startsWith(paymentCategoryName);
        }
        return isRootNode || found;
    }

}
