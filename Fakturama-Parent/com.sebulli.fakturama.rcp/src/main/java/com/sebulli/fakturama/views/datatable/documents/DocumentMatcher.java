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

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.util.DocumentTypeUtil;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.matchers.Matcher;

/**
 *
 */
public class DocumentMatcher implements Matcher<Document> {
    private static final String NO_SELECTION_ROOT = "/---";
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
                found = documentCategoryName.contentEquals(NO_SELECTION_ROOT) || item.getTransactionId() != null && item.getTransactionId() == parsedTransactionId;
            } else if(treeObjectType == TreeObjectType.CONTACTS_ROOTNODE) {
                found = documentCategoryName.contentEquals(NO_SELECTION_ROOT) || StringUtils.equals(item.getAddressFirstLine(), documentCategoryName);
            } else {
                DocumentType docType = DocumentTypeUtil.findByBillingType(item.getBillingType());
                if (docType != null) {
                        String fullCategoryName = getCategory(item, docType); //CommonConverter.getCategoryName(item.getCategory(), rootNodeName);
                        if(fullCategoryName.startsWith(documentCategoryName) || documentCategoryName.contentEquals(NO_SELECTION_ROOT)) {
                            found = true;
                        }
                }
            }
        }
        return isRootNode || found;
    }
    
    private String getCategory(Document item, DocumentType documentType) {
        try {
            if (item != null && documentType != null) {
                String category = "/" + msg.getMessageFromKey(DocumentType.getPluralString(documentType));

                // use the document type to generate the category string ..
                switch (documentType) {
                case INVOICE:
                case CREDIT:
                case DUNNING:
                    // .. the state of the payment ..
                    if (item.getPayDate() != null && BooleanUtils.toBoolean(item.getPaid()))
                        category += "/" + msg.documentOrderStatePaid;
                    else
                        category += "/" + msg.documentOrderStateUnpaid;
                    break;
                case DELIVERY:
                    // .. the state of the delivery document ..
                    if (item.getSourceDocument() != null && item.getSourceDocument().getBillingType().isINVOICE())
                        category += "/" + msg.documentDeliveryStateHasinvoice;
                    else
                        category += "/" + msg.documentDeliveryStateHasnoinvoice;
                    break;
                case ORDER:
                    // .. and the state of the shipping
                    OrderState progress = OrderState.findByProgressValue(Optional.of(item.getProgress()));
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
                case OFFER:
                case LETTER:
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
