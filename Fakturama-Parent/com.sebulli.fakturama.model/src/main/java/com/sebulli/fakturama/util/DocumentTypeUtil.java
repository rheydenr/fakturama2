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

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.texo.converter.ObjectCopier;
import org.eclipse.emf.texo.model.AbstractModelObject;

import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelFactory.DocumentModelObject;
import com.sebulli.fakturama.model.FakturamaModelFactory.OfferModelObject;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Offer;

/**
 * Helper for {@link DocumentType}
 *
 */
public class DocumentTypeUtil {
	
	public <T extends Document> T clone(T document) {
		T clonedDocument = null;
		if(document != null) {
			ObjectCopier objectCopier = new ObjectCopier();
			objectCopier.setCopyChildren(true);
			objectCopier.setCopyReferences(true);
			clonedDocument = (T) objectCopier.copy(document);
			
			/* Modify some references.
			 * Note, that VAT, Payment and the like are entities which are always referenced only. But entities
			 * like DocumentReceiver or DocumentItem have to be created newly for each copy of a document. Therefore
			 * we iterate through all relevant entity collections and set their item's id to 0. This causes the 
			 * Entity Manager to store the complete list as new entities.
			 */
			// VAT, Shipment, Payment can be left unchanged
			if(clonedDocument != null) {
				// reset some attributes 
				clonedDocument.getAdditionalInfo().setId(0);
				clonedDocument.setInvoiceReference(null);
				clonedDocument.setSourceDocument(null);
				clonedDocument.setTransactionId(null);
				clonedDocument.setVersion(Integer.valueOf(1));
				
				// set DocumentReceiver to new
				clonedDocument.getReceiver().forEach(r -> r.setId(0));
				
				// set DocumentItems to new
				clonedDocument.getItems().forEach(r -> r.setId(0));
			
		     // make the new object really "new" :-)
		     clonedDocument.setId(0);
			}
		}
		return clonedDocument;
	}

	/**
     * Finds a {@link DocumentType} by it corresponding {@link BillingType}.
     * 
     * @param billingType
     * @return
     */
    public static DocumentType findByBillingType(BillingType billingType) {
        DocumentType retval = DocumentType.NONE;
        if(billingType != null) {
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
        }
        return retval;
    }
    
    public static Document createDocumentByBillingType(BillingType documentType) {
    	DocumentType targetType = findByBillingType(documentType);
    	return createDocumentByDocumentType(targetType);
    }
    
    public static Document createDocumentByDocumentType(DocumentType documentType) {
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
        document.setBillingType(BillingType.getByName(documentType.name()));
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
