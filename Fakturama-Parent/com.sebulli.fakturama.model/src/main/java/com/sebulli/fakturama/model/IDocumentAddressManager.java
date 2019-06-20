package com.sebulli.fakturama.model;

public interface IDocumentAddressManager {

	/**
	 * Create a new {@link DocumentReceiver} from a {@link Contact} for a given {@link BillingType}.
	 * Nearly all fields are copied from {@link Contact} except <code>manualAddress</code>.
	 * 
	 * @param contact the {@link Contact} to copy
	 * @param billingType {@link BillingType} of {@link DocumentReceiver}
	 * @return {@link DocumentReceiver}
	 */
	DocumentReceiver createDocumentReceiverFromContact(Contact contact, BillingType billingType);

	Address getAddressFromContact(Contact contact, BillingType billingType);

	DocumentReceiver getBillingAdress(Document document);

	DocumentReceiver getDeliveryAdress(Document document);

	DocumentReceiver getAdressForBillingType(Document document, BillingType billingType);

}