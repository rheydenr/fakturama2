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
 * Enumeration of all 8 data types, a document can be.
 * 
 * @author Gerd Bartelt
 */
public enum DocumentType {
	// all 8 data types
	// TODO localize!!!
	NONE(0, "None", "None", false, false, true, false, 1, "none"),
 LETTER(1, "Letter", "Letters", false, false, false, false, 1, "New Letter"),
 OFFER(2, "Offer", "Offers", true, true, false, false, 1, "New Offer"),
 ORDER(3, "Order", "Orders", true, true, false, false, 1, "New Order"),
 CONFIRMATION(4, "Confirmation", "Confirmations", true, true, false, false, 1, "New Confirmation"),
 INVOICE(5, "Invoice", "Invoices", true, true, true, false, 1, "New Invoice"),
 DELIVERY(6, "Delivery Note", "Delivery Notes", true, false, false, true, 1, "New Delivery Note"),
 CREDIT(7, "Credit", "Credit Items", true, true, true, true, -1, "New Credit"),
 DUNNING(8, "Dunning", "Dunning Letters", false, false, false, true, 1, "New Dunning"),
 PROFORMA(9, "Proforma", "Proforma Invoices", true, true, false, false, 1, "New Proforma Invoice");
	
	// 9 types.
	public final static int MAXID = DocumentType.values().length;
	private int key;

	private int sign;

	private String newText;

	/**
	 * If {@link DocumentType} has an item table
	 */
	private boolean itemTable;

	/**
	 * * Defines all Document Types that contains a price
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
	private String singularDescription, pluralDescription;

	private DocumentType(int key, String description, String plural,
			boolean itemTable, boolean price, boolean paid,
			boolean invoiceReference, int sign, String newText) {
		this.key = key;
		this.singularDescription = description;
		this.pluralDescription = plural;
		this.itemTable = itemTable;
		this.paid = paid;
		this.invoiceReference = invoiceReference;
		this.sign = sign;
		this.newText = newText;
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
	public final String getSingularDescription() {
		return singularDescription;
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
		DocumentType retval = null;
		for (DocumentType selfDocumentType : values()) {
			if (selfDocumentType.getSingularDescription().equalsIgnoreCase(
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
		DocumentType retval = null;
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
	 * @return The DocumentType as non-localized string
	 */
	public String getTypeAsString() {
		return toString();
	}

	/**
	 * Convert from DocumentType to a document type localized string The
	 * singular style is used.
	 * 
	 * @param documentType
	 *            DocumentType to convert
	 * @return The DocumentType as localized string
	 */
	public static String getString(DocumentType documentType) {
		return documentType.getSingularDescription();
	}

	/**
	 * Convert from DocumentType to a document type localized string The plural
	 * style is used.
	 * 
	 * @param documentType
	 *            DocumentType to convert
	 * @return The DocumentType as localized string
	 */
	public static String getPluralString(DocumentType documentType) {
		return DocumentType.getPluralString(documentType);
	}

	// /**
	// * JFace DocumentType content provider Provides all Document types as an
	// * String array
	// *
	// * @author Gerd Bartelt
	// */
	// public static class DocumentTypeContentProvider implements
	// IStructuredContentProvider {
	// public Object[] getElements(Object inputElement) {
	//
	// // Get all document types
	// ArrayList<String> strings = new ArrayList<String>();
	// for (int i = 1; i <= MAXID; i++)
	// strings.add(getString(i));
	//
	// // Convert them to an Array
	// return strings.toArray();
	// }
	//
	// @Override
	// public void dispose() {
	// }
	//
	// @Override
	// public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	// {
	// viewer.refresh();
	// }
	//
	// }

	/**
	 * Get the text to create a new instance of this document
	 * 
	 * @return Text as localized string.
	 */
	public String getNewText() {
		return newText;
	}

}
