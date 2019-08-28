
package com.sebulli.fakturama.model;

import java.util.Optional;

import javax.annotation.PostConstruct;

import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;

public class DocumentAddressManager implements IDocumentAddressManager {
	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;

	@PostConstruct
	public void init() {
		System.out.println("huhu");
	}

	/**
	 * Create a new {@link DocumentReceiver} from a {@link Contact} for a given
	 * {@link BillingType}. Nearly all fields are copied from {@link Contact} except
	 * <code>manualAddress</code>.
	 * 
	 * @param contact     the {@link Contact} to copy
	 * @param billingType {@link BillingType} of {@link DocumentReceiver}
	 * @return {@link DocumentReceiver}
	 */
	@Override
	public DocumentReceiver createDocumentReceiverFromContact(Contact contact, BillingType billingType) {
		DocumentReceiver documentReceiver = modelFactory.createDocumentReceiver();
		documentReceiver.setBillingType(billingType);

		// copy address data
		Address addressFromContact = getAddressFromContact(contact, billingType.isINVOICE() ? ContactType.BILLING : ContactType.DELIVERY);
		documentReceiver.setStreet(addressFromContact.getStreet());
		documentReceiver.setCity(addressFromContact.getCity());
		documentReceiver.setZip(addressFromContact.getZip());
		documentReceiver.setCountryCode(addressFromContact.getCountryCode());

		// copy fields from contact
		documentReceiver.setOriginContactId(contact.getId());
		documentReceiver.setCustomerNumber(contact.getCustomerNumber());
		documentReceiver.setTitle(contact.getTitle());
		documentReceiver.setFirstName(contact.getFirstName());
		documentReceiver.setName(contact.getName());
		documentReceiver.setGender(contact.getGender());
		documentReceiver.setCompany(contact.getCompany());
//		documentReceiver.setBirthday(contact.getBirthday());

		documentReceiver.setEmail(contact.getEmail());
		documentReceiver.setMobile(contact.getPhone());
		documentReceiver.setPhone(contact.getPhone());
		documentReceiver.setFax(contact.getFax());

//		documentReceiver.setDiscount(contact.getDiscount());
//		documentReceiver.setNote(contact.getNote());
//		documentReceiver.setPayment(contact.getPayment());
//		documentReceiver.setReliability(contact.getReliability());
//		documentReceiver.setUseNetGross(contact.getUseNetGross());
//		documentReceiver.setVatNumber(contact.getVatNumber());
//		documentReceiver.setWebsite(contact.getWebsite());
//		documentReceiver.setWebshopName(contact.getWebshopName());

		documentReceiver.setSupplierNumber(contact.getSupplierNumber());
//		documentReceiver.setMandateReference(contact.getMandateReference());
		documentReceiver.setGln(contact.getGln());
//		documentReceiver.setBankAccount(contact.getBankAccount());
//		documentReceiver.setUseSalesEqualizationTax(contact.getUseSalesEqualizationTax());

		return documentReceiver;
	}

	@Override
	public Address getAddressFromContact(Contact contact, ContactType contactType) {
		Address address = null;
		if (contact != null && contactType != null) {
			address = contact.getAddresses().stream()
					.filter(rcv -> rcv.getContactTypes().isEmpty() || rcv.getContactTypes().contains(contactType))
					.findFirst().get();
		}
		return address;
	}

	@Override
	public DocumentReceiver getBillingAdress(Document document) {
		return getAdressForBillingType(document, BillingType.INVOICE);
	}

	@Override
	public DocumentReceiver getDeliveryAdress(Document document) {
		return getAdressForBillingType(document, BillingType.DELIVERY);
	}

	@Override
	public DocumentReceiver getAdressForBillingType(Document document, BillingType billingType) {
		Optional<DocumentReceiver> documentReceiver = document.getReceiver().stream()
				.filter(rcv -> rcv == null || rcv.getBillingType().compareTo(billingType) == 0).findFirst();
		return documentReceiver.orElse(null);
	}

	@Override
	public Document addReceiverToDocument(Document document, DocumentReceiver documentReceiver) {
		Optional<DocumentReceiver> existingReceiver = document.getReceiver().stream().filter(r -> r.getBillingType().compareTo(documentReceiver.getBillingType()) == 0).findAny();
		existingReceiver.ifPresent(e -> document.getReceiver().remove(e));
		document.getReceiver().add(documentReceiver);
		return document;
	}
}
