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

package com.sebulli.fakturama.misc;

/**
 * Enumeration of all 8 data types a document can be.
 * 
 * @author Gerd Bartelt
 */
public enum DocumentType {
 // all 8 data types
 NONE(0, "None", "None", false, false, false, false, 1, "none"),
 LETTER(1, "toolbar.new.letter.name" /*"Letter"*/, "document.type.letter.plural", false, false, false, false, 1, "main.menu.new.letter"),
 OFFER(2, "toolbar.new.offer.name" /*"Offer"*/, "document.type.offer.plural", true, true, false, false, 1, "main.menu.new.offer"),
 ORDER(3, "toolbar.new.order.name" /*"Order"*/, "document.type.order.plural", true, true, false, false, 1, "main.menu.new.order"),
 CONFIRMATION(4, "toolbar.new.confirmation.name" /*"Confirmation"*/, "document.type.confirmation.plural", true, true, false, false, 1, "main.menu.new.confirmation"),
 INVOICE(5, "toolbar.new.invoice.name" /*"Invoice"*/, "document.type.invoice.plural", true, true, true, false, 1, "main.menu.new.invoice"),
 DELIVERY(6, "toolbar.new.deliverynote.name" /*"Delivery Note"*/, "document.type.deliverynote.plural", true, false, false, true, 1, "main.menu.new.deliverynote"),
 CREDIT(7, "toolbar.new.document.credit.name" /*"Credit"*/, "document.type.credititems.plural", true, true, true, true, -1, "main.menu.new.credit"),
 DUNNING(8, "toolbar.new.document.dunning.name" /*"Dunning"*/, "document.type.dunning.plural", false, false, false, true, 1, "main.menu.new.dunning"),
 PROFORMA(9, "document.type.proforma" /*"Proforma"*/, "document.type.proforma.plural", true, true, false, false, 1, "main.menu.new.proforma");
	
	// 9 types.
	public final static int MAXID = DocumentType.values().length;
	private int key, sign;
	
	/**
	 * the l10n key for a new document (e.g., "main.menu.new.letter" => "New Letter")
	 */
	private String newTextKey;

	/**
	 * If {@link DocumentType} has an item table
	 */
	private boolean itemTable;

	/**
	 * * Defines all {@link DocumentType}s that contains a price
	 */
	private boolean price;

	/**
	 * Defines all Document Types that can be marked as paid
	 */
	private boolean paid;

	private boolean invoiceReference;

	/**
	 * Type of the document (singular)
	 */
	private String singularKey, pluralDescription;

	private DocumentType(int key, String description, String plural,
			boolean itemTable, boolean price, boolean paid,
			boolean invoiceReference, int sign, String newText) {
		this.key = key;
		this.singularKey = description;
		this.pluralDescription = plural;
		this.itemTable = itemTable;
		this.price = price;
		this.paid = paid;
		this.invoiceReference = invoiceReference;
		this.sign = sign;
		this.newTextKey = newText;
	}

	/**
	 * Gets the corresponding integer of an DocumentType
	 * 
	 * @return The integer that corresponds to the DocumentType
	 */
	public final int getKey() {
		return key;
	}

	/**
	 * @return the description
	 */
	public final String getSingularKey() {
		return singularKey;
	}

	/**
	 * @return the itemTable
	 */
	public final boolean hasItems() {
		return itemTable;
	}

	/**
	 * @return the price
	 */
	public final boolean hasPrice() {
		return price;
	}

	/**
	 * @return the paid
	 */
	public final boolean canBePaid() {
		return paid;
	}

	/**
	 * @return the invoiceReference
	 */
	public final boolean hasInvoiceReference() {
		return invoiceReference;
	}

	/**
	 * @return the sign
	 */
	public final int getSign() {
		return sign;
	}

	/**
	 * @return the pluralDescription
	 */
	public final String getPluralDescription() {
		return pluralDescription;
	}

	/**
	 * Convert from a document type string to a DocumentType
	 * 
	 * @param documentType
	 *            String to convert
	 * @return The DocumentType that corresponds to the String
	 */
	public static DocumentType findDocumentTypeByDescription(String documentType) {
		DocumentType retval = DocumentType.NONE;
		for (DocumentType selfDocumentType : values()) {
			if (selfDocumentType.getSingularKey().equalsIgnoreCase(
					documentType)) {
				retval = selfDocumentType;
				break;
			}
		}
		return retval;
	}

	/**
	 * Convert from a document type String to the corresponding integer
	 * 
	 * @param documentType
	 *            Document type as string to convert
	 * @return The integer that corresponds to the DocumentType
	 */
	public static int getInt(String documentType) {
		int retval = -1;
		DocumentType found = DocumentType
				.findDocumentTypeByDescription(documentType);
		if (found != null) {
			retval = found.getKey();
		}
		return retval;
	}

	/**
	 * Convert from an integer to a DocumentType
	 * 
	 * @param key
	 *            Integer to convert
	 * @return The DocumentType
	 */
	public static DocumentType findByKey(int key) {
		DocumentType retval = DocumentType.NONE;
		for (DocumentType selfDocumentType : values()) {
			if (selfDocumentType.getKey() == key) {
				retval = selfDocumentType;
				break;
			}
		}
		return retval;
	}

	/**
	 * Convert from an integer to a document type non-localized string The
	 * singular style is used.
	 * 
	 * @param i
	 *            Integer to convert
	 * @return The DocumentType as non-localized string
	 */
	public static String getTypeAsString(int i) {
		// do not translate !!
		switch (i) {
		case 1:
			return "Letter";
		case 2:
			return "Offer";
		case 3:
			return "Order";
		case 4:
			return "Confirmation";
		case 5:
			return "Invoice";
		case 6:
			return "Delivery";
		case 7:
			return "Credit";
		case 8:
			return "Dunning";
		case 9:
			return "Proforma";
		}
		return "NONE";
	}

	/**
	 * Convert from Document Type to a document type non-localized string The
	 * singular style is used.
	 * 
	 * @param documentType
	 *            DocumentType to convert
	 * @return The DocumentType as non-localized string
	 */
	public static String getTypeAsString(DocumentType documentType) {
		return documentType.toString();
		// getTypeAsString(getInt(documentType));
	}

	/**
	 * Get the type as non-localized string
	 * 
	 * @return The {@link DocumentType} as non-localized string
	 */
	public String getTypeAsString() {
		return toString();
	}

	/**
	 * Convert from {@link DocumentType} to a document type localized string. The
	 * singular style is used.
	 * 
	 * @param documentType
	 *            {@link DocumentType} to convert
	 * @return The {@link DocumentType} as localized string
	 */
	public static String getString(DocumentType documentType) {
		return documentType.getSingularKey();
	}

	/**
	 * Convert from {@link DocumentType} to a document type localized string. The plural
	 * style is used.
	 * 
	 * @param documentType
	 *            {@link DocumentType} to convert
	 * @return The {@link DocumentType} as localized string
	 */
	public static String getPluralString(DocumentType documentType) {
		return documentType.getPluralDescription();
	}

	/**
	 * Get the text to create a new instance of this document
	 * 
	 * @return Text as localized string.
	 */
	public String getNewText() {
		return newTextKey;
	}
    
    /**
     * Get the text key to create labels with "new" documents
     * 
     * @param i
     *      The document type index
     * @return 
     * Text as localized string.
     */
    public static String getNewTextKey(DocumentType documentType) {
        return documentType.getNewText();
    }	
    
}
