/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.dto;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 * Organize all document of a specified transaction
 * 
 * @author Gerd Bartelt
 *
 */
public class Transaction {
    
    @Inject
    private DocumentsDAO documentsDAO;
		
	// The transcation no.
	Integer transaction = Integer.valueOf(-1);
	// An list with all documents with the same transaction number
	List<Document> documents = null;
	
	/**
	 * Constructor
	 * 
	 * Collects all documents with the same transaction number
	 * 
	 * @param document
	 * 	The document with the parent transaction number
	 */
	public Transaction of(Document document) {
		
		// Get the transaction number
		transaction = document.getTransactionId();

		// Exit, if there is no number
		if (transaction == -1)
			return null;

		// Create a new list
		documents = new ArrayList<Document>();
		
		// Get all documents
		documents = documentsDAO.findByTransactionId(transaction);
		return this;
	}
	
	/**
	 * Gets the first referenced document's date for this transaction.
	 * 
	 * @return
	 */
	public String getFirstReferencedDocumentDate(DocumentType docType) {
		Document reference = getFirstReferencedDocument(docType);
		
		// Return the reference date
		return reference != null ? DataUtils.getInstance().getFormattedLocalizedDate(reference.getDocumentDate()) : "";
	}
	
	public Document getFirstReferencedDocument(DocumentType docType) {
		Document reference = null;
		
		// Get all documents
		for (Document document: documents) {
			
			// Has this document the same type
			if (DocumentTypeUtil.findByBillingType(document.getBillingType()) == docType) {
				// Add the name to the reference string
				reference = document;
				break;
			}
		}
		return reference;
	}
//	
//	public Date getFirstReferencedDocumentDueDate(DocumentType docType) {
//		Document reference = getFirstReferencedDocument(docType);
//		
//		// Return the reference date
//		return reference != null ? DataUtils.getInstance().getFormattedLocalizedDate(reference.getDocumentDate())) : "";
//	}
    
    /**
     * Returns a string with all documents with the same transaction
     *  
     * @param docType
     *      Only those documents will be returned
     * @return
     *      String with the document names
     */
	public String getReference(DocumentType docType) {
	    return documentsDAO.getReference(transaction, docType);
	}

	/**
     * @return the documents
     */
    public List<Document> getDocuments() {
    	return documents;
    }

    /**
     * @param documents the documents to set
     */
    public final void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
