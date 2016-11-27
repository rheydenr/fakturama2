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
 
package com.sebulli.fakturama.util;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;

/**
 * Helper for {@link DocumentType}
 *
 */
public class DocumentTypeUtil {

    
    /**
     * Finds a {@link DocumentType} by it corresponding {@link BillingType}.
     * 
     * @param billingType
     * @return
     */
    public static DocumentType findByBillingType(BillingType billingType) {
        DocumentType retval = DocumentType.NONE;
        switch (billingType) {
        case ORDER:
            retval = DocumentType.ORDER;
            break;
        case OFFER:
            retval = DocumentType.OFFER;
            break;
        case INVOICE:
            retval = DocumentType.INVOICE;
            break;
        case PROFORMA:
            retval = DocumentType.PROFORMA;
            break;
        case DUNNING:
            retval = DocumentType.DUNNING;
            break;
        case CONFIRMATION:
            retval = DocumentType.CONFIRMATION;
            break;
        case CREDIT:
            retval = DocumentType.CREDIT;
            break;
        case DELIVERY:
            retval = DocumentType.DELIVERY;
            break;
        case LETTER:
            retval = DocumentType.LETTER;
            break;
        default:
            break;
        }
        return retval;
    }

    public static Document createDocumentByType(DocumentType documentType) {
        FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;
        Document document = null;
        // create a new data set with this document type
        switch (documentType) {
        case INVOICE:
            document = modelFactory.createInvoice();
            break;
        case PROFORMA:
            document = modelFactory.createProforma();
            break;
        case DUNNING:
            document = modelFactory.createDunning();
            break;
        case DELIVERY:
            document = modelFactory.createDelivery();
            break;
        case OFFER:
            document = modelFactory.createOffer();
            break;
        case ORDER:
            document = modelFactory.createOrder();
            break;
        case CONFIRMATION:
            document = modelFactory.createConfirmation();
            break;
        case CREDIT:
            document = modelFactory.createCredit();
            break;
        case LETTER:
            document = modelFactory.createLetter();
            break;
        default:
            document = modelFactory.createOrder();
            break;
        }
        // some initializations
        document.setTransactionId(getNewTransactionId());
        document.setValidFrom(Date.from(Instant.now()));
        return document;
    }
    
	
	/**
	 * Generates a random transaction number
	 * 
	 * @return new random ID
	 */
	public static int getNewTransactionId () {
		return Math.abs(UUID.randomUUID().hashCode());
	}

}
