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

import com.sebulli.fakturama.handlers.MarkOrderAsActionHandler;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.parts.converter.CommonConverter;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 *
 */
public class DocumentMatcher implements Matcher<Document> {
    final String documentCategoryName;
    final boolean isRootNode;
    private final String rootNodeName;
    private Messages msg;
    
    /**
     * Constructor
     * 
     * @param pDocumentCategoryName category name which is selected in tree viewer
     * @param treeObjectType the selected {@link TreeObjectType}
     * @param rootNodeName the name of the root node (needed for building the complete category path of an item) 
     * @param msg 
     */
    public DocumentMatcher(String pDocumentCategoryName, TreeObjectType treeObjectType, String rootNodeName, Messages msg) {
        this.documentCategoryName = StringUtils.prependIfMissing(pDocumentCategoryName, "/", "/");
        this.isRootNode = treeObjectType == TreeObjectType.ALL_NODE || treeObjectType == TreeObjectType.ROOT_NODE;
        this.rootNodeName = "/" + rootNodeName + "/";
        this.msg = msg;
    }

    @Override
    public boolean matches(Document item) {
        boolean found = false;
        if(!isRootNode) {
            DocumentType obj = DocumentType.findDocumentTypeByClass(item.getClass());
            if (obj != null) {
                switch (obj) {
//                case LETTER:
//                    return Icon.ICON_LETTER;
//                case OFFER:
//                    return Icon.ICON_OFFER;
                case ORDER:
                    String fullCategoryName = getCategory(item); //CommonConverter.getCategoryName(item.getCategory(), rootNodeName);
                    if(fullCategoryName.startsWith(documentCategoryName)) {
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
                    switch (item.getProgress()) {
                    case 0:
                    case MarkOrderAsActionHandler.PENDING:
                    case MarkOrderAsActionHandler.PROCESSING:
                        category += "/" + msg.documentOrderStateNotshipped;
                        break;
                    case MarkOrderAsActionHandler.SHIPPED:
                    case MarkOrderAsActionHandler.COMPLETED:
                        category += "/" + msg.documentOrderStateShipped;
                        break;
                    }
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
