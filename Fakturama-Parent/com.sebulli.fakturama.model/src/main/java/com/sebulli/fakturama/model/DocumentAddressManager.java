
package com.sebulli.fakturama.model;

import java.util.Optional;

public class DocumentAddressManager implements IDocumentAddressManager {
    
	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;

	/**
	 * Create a new {@link DocumentReceiver} from a contact address for a given
	 * {@link BillingType}. Nearly all fields are copied from {@link Contact} except
	 * <code>manualAddress</code>.
	 * 
	 * @param contact     the {@link Contact} to copy
	 * @param billingType {@link BillingType} of {@link DocumentReceiver}
	 * @return {@link DocumentReceiver}
	 */
	@Override
	public DocumentReceiver createDocumentReceiverFromAddress(Address address, BillingType billingType) {
		Contact contact = address.getContact();
		return createDocumentReceiver(contact, address, billingType);
	}
	
	private DocumentReceiver createDocumentReceiver(Contact contact, Address address, BillingType billingType) {
		DocumentReceiver documentReceiver = modelFactory.createDocumentReceiver();
		documentReceiver.setBillingType(billingType);

		// copy address data
		documentReceiver.setStreet(address.getStreet());
		documentReceiver.setCity(address.getCity());
		documentReceiver.setZip(address.getZip());
		documentReceiver.setCountryCode(address.getCountryCode());
		documentReceiver.setEmail(address.getEmail());
		documentReceiver.setMobile(address.getMobile());
		documentReceiver.setPhone(address.getPhone());
		documentReceiver.setFax(address.getFax());
		documentReceiver.setConsultant(address.getLocalConsultant());

		// copy fields from contact
		documentReceiver.setOriginContactId(contact.getId());
		documentReceiver.setOriginAddressId(address.getId());
		documentReceiver.setCustomerNumber(contact.getCustomerNumber());
		documentReceiver.setTitle(contact.getTitle());
		documentReceiver.setFirstName(contact.getFirstName());
		documentReceiver.setName(contact.getName());
		documentReceiver.setGender(contact.getGender());
		documentReceiver.setCompany(contact.getCompany());
//		documentReceiver.setBirthday(contact.getBirthday());
		
//		documentReceiver.setDiscount(contact.getDiscount());
//		documentReceiver.setNote(contact.getNote());
//		documentReceiver.setPayment(contact.getPayment());
//		documentReceiver.setReliability(contact.getReliability());
//		documentReceiver.setUseNetGross(contact.getUseNetGross());
		documentReceiver.setVatNumber(contact.getVatNumber());
//		documentReceiver.setWebsite(contact.getWebsite());
//		documentReceiver.setWebshopName(contact.getWebshopName());

		documentReceiver.setSupplierNumber(contact.getSupplierNumber());
		documentReceiver.setMandateReference(contact.getMandateReference());
		documentReceiver.setGln(contact.getGln());
		documentReceiver.setAlias(contact.getAlias());
//		documentReceiver.setBankAccount(contact.getBankAccount());
//		documentReceiver.setUseSalesEqualizationTax(contact.getUseSalesEqualizationTax());

		return documentReceiver;
	}

	@Override
	public DocumentReceiver createDocumentReceiverForBillingType(Contact contact, BillingType billingType) {
		Optional<Address> addressFromContact = getAddressFromContact(contact, billingType.isINVOICE() ? ContactType.BILLING : ContactType.DELIVERY);
		// copy address data
		return createDocumentReceiver(contact, addressFromContact.orElse(null), billingType);
	}

	@Override
	public Optional<Address> getAddressFromContact(Contact contact, ContactType contactType) {
		Optional<Address> address = Optional.empty();
		if (contact != null && contactType != null) {
			address = contact.getAddresses().stream()
					.filter(rcv -> rcv.getContactTypes().isEmpty() || rcv.getContactTypes().contains(contactType))
					.findFirst();
			
			// if there's no fitting address use the first one (fallback)
			if(!address.isPresent() && !contact.getAddresses().isEmpty()) {
				address = Optional.ofNullable(contact.getAddresses().get(0));
			}
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
		// new documents don't have a receiver
		if(document.getReceiver().isEmpty()) {
			return null;
		} else if(document.getReceiver().size() == 1) {
			// if we have only one receiver it is for all BillingTypes
			return document.getReceiver().get(0);
		}
		
		// for the moment, only INVOICE and DELIVERY types are valid
		// all other types will be implemented later on
		// therefore the filter condition contains multiple types
		BillingType filterBillingType;
		switch (billingType) {
        case CREDIT:
        case CONFIRMATION:
        case DUNNING:
        case OFFER:
        case ORDER:
        case PROFORMA:
            filterBillingType = BillingType.INVOICE;
            break;
        default:
            filterBillingType = billingType;
            break;
        }
		Optional<DocumentReceiver> documentReceiver = document.getReceiver().stream()
				.filter(rcv -> rcv.getBillingType().compareTo(filterBillingType) == 0).findFirst();
		
		// FALLBACK: Use billing type of the given document
		if(!documentReceiver.isPresent()) {
			documentReceiver = document.getReceiver().stream()
					.filter(rcv -> rcv.getBillingType().compareTo(document.getBillingType()) == 0).findFirst();
		}
		return documentReceiver.orElse(null);
	}

	@Override
	public Document addOrReplaceReceiverToDocument(Document document, DocumentReceiver documentReceiver) {
		Optional<DocumentReceiver> existingReceiver = document.getReceiver().stream()
				.filter(r -> r.getBillingType().compareTo(documentReceiver.getBillingType()) == 0).findAny();
		existingReceiver.ifPresent(e -> {
			e.setDeleted(Boolean.TRUE);
			document.getReceiver().remove(e);
		});
		document.getReceiver().add(documentReceiver);
		return document;
	}

}
