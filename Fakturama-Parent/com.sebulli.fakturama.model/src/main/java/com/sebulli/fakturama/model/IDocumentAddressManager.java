package com.sebulli.fakturama.model;

import java.util.Set;

public interface IDocumentAddressManager {

	/**
	 * Create a new {@link DocumentReceiver} from a {@link Contact} for a given {@link BillingType}.
	 * Nearly all fields are copied from {@link Contact} except <code>manualAddress</code>.
	 * 
	 * @param address the {@link Contact} to copy
	 * @param billingType {@link BillingType} of {@link DocumentReceiver}
	 * @return {@link DocumentReceiver}
	 */
	DocumentReceiver createDocumentReceiverFromContact(Address address, BillingType billingType);

	Address getAddressFromContact(Contact contact, ContactType contactType);

	DocumentReceiver getBillingAdress(Document document);

	DocumentReceiver getDeliveryAdress(Document document);

	/**
	 * Reads the billing address from a {@link Document}. 
	 * 
	 * @param document the {@link Document}
	 * @param billingType
	 * @return
	 */
	DocumentReceiver getAdressForBillingType(Document document, BillingType billingType);

	/**
	 * Add (or replace an existing) {@link DocumentReceiver} to a {@link Document}. This
	 * method is a replacement for the simple {@link Set#add(Object)} method since The {@link DocumentReceiver}'s
	 * <code>equals()</code> method can't be overwritten. Therefore we have to take an extra helper method.
	 * 
	 * @param document the {@link Document}
	 * @param documentReceiver new {@link DocumentReceiver}
	 * @return {@link Document} with the new {@link DocumentReceiver}
	 */
	Document addOrReplaceReceiverToDocument(Document document, DocumentReceiver documentReceiver);

	/**
	 * Creates a {@link DocumentReceiver} for a given {@link Contact} and {@link BillingType}.
	 * 
	 * @param contact {@link Contact} for this {@link DocumentReceiver}
	 * @param billingType {@link BillingType} for this {@link DocumentReceiver}
	 * @return {@link DocumentReceiver}
	 */
	DocumentReceiver createDocumentReceiverForBillingType(Contact contact, BillingType billingType);
}