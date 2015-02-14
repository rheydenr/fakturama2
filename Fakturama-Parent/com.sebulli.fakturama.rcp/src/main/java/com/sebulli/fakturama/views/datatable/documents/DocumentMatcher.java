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
 
package com.sebulli.fakturama.views.datatable.documents;

import org.apache.commons.lang3.StringUtils;

import ca.odell.glazedlists.matchers.Matcher;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 *
 */
public class DocumentMatcher implements Matcher<Document> {
    final String documentCategoryName;
    final boolean isRootNode;
    private final TreeObjectType treeObjectType;
    private Messages msg;
    private long parsedTransactionId = 0L; // only for convenience & performance
    
    /**
     * Constructor
     * 
     * @param pDocumentCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     * @param msg 
     */
    public DocumentMatcher(String pDocumentCategoryName, TreeObjectType treeObjectType, Messages msg) {
        if(treeObjectType != TreeObjectType.CONTACTS_ROOTNODE && treeObjectType != TreeObjectType.TRANSACTIONS_ROOTNODE) {
            this.documentCategoryName = StringUtils.prependIfMissing(pDocumentCategoryName, "/", "/");
        } else {
            this.documentCategoryName = pDocumentCategoryName;
        }
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
        this.msg = msg;
        this.treeObjectType = treeObjectType;
        if(StringUtils.isNumeric(pDocumentCategoryName)) {
            parsedTransactionId = Long.parseLong(pDocumentCategoryName);
        }
    }

    @Override
    public boolean matches(Document item) {
        boolean found = false;
        if(!isRootNode) {
            if(treeObjectType == TreeObjectType.TRANSACTIONS_ROOTNODE) {
                // treat the filter as a transaction ID
                found = documentCategoryName.contentEquals("/---") || item.getTransactionId() == parsedTransactionId;
            } else if(treeObjectType == TreeObjectType.CONTACTS_ROOTNODE) {
                found = documentCategoryName.contentEquals("/---") || StringUtils.equals(item.getAddressFirstLine(), documentCategoryName);
            } else {
                DocumentType docType = DocumentType.findDocumentTypeByClass(item.getClass());
                if (docType != null) {
                    switch (docType) {
    //                case LETTER:
    //                    return Icon.ICON_LETTER;
    //                case OFFER:
    //                    return Icon.ICON_OFFER;
                    case ORDER:
                        String fullCategoryName = getCategory(item); //CommonConverter.getCategoryName(item.getCategory(), rootNodeName);
                        if(fullCategoryName.startsWith(documentCategoryName) || documentCategoryName.contentEquals("/---")) {
                            found = true;
                        }
                         break;
                        
    //                    return Icon.ICON_ORDER;
    //                case CONFIRMATION:
    //                    return Icon.ICON_CONFIRMATION;
    //                case INVOICE:
    //                    return Icon.ICON_INVOICE;
    //                case DELIVERY:
    //                    return Icon.ICON_DELIVERY;
    //                case CREDIT:
    //                    return Icon.ICON_CREDIT;
    //                case DUNNING:
    //                    return Icon.ICON_DUNNING;
    //                case PROFORMA:
    //                    return Icon.ICON_PROFORMA;
                    default:
                        break;
                    }
                }
            }
        }
        return isRootNode || found;
    }
    
    public String getCategory(Document item) {
        try {
            DocumentType documentType = DocumentType.findDocumentTypeByClass(item.getClass());
            if (documentType != null) {
                String category = "/" + msg.getMessageFromKey(DocumentType.getPluralString(documentType));

                // use the document type to generate the category string ..
                switch (documentType) {
                case INVOICE:
                case CREDIT:
                case DUNNING:
                    // .. the state of the payment ..
                    if (item.getPayDate() != null)
                        category += "/" + msg.documentOrderStatePaid;
                    else
                        category += "/" + msg.documentOrderStateUnpaid;
                    break;
                case DELIVERY:
                    // .. the state of the delivery document ..
                    if (item.getSourceDocument() != null)
                        category += "/" + msg.documentDeliveryStateHasinvoice;
                    else
                        category += "/" + msg.documentDeliveryStateHasnoinvoice;
                    break;
                case ORDER:
                    // .. and the state of the shipping
                    OrderState progress = OrderState.findByProgressValue(item.getProgress());
                    switch (progress) {
                    case NONE:
                    case PENDING:
                    case PROCESSING:
                        category += "/" + msg.documentOrderStateNotshipped;
                        break;
                    case SHIPPED:
                    case COMPLETED:
                        category += "/" + msg.documentOrderStateShipped;
                        break;
                    }
                    break;
                    default:
                        break;
                }
                return category;
            }
        }
        catch (Exception e) {
//            Logger.logError(e, "Error getting key category.");
        }
        return "";
    }

}
