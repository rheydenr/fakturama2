package com.sebulli.fakturama.dao;

import java.util.List;

import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactType;

public class DebitorAddress {
	private String customerNumber, name, firstName, zipCode, city, company;
	private List<ContactType> contactType;
	private long contactId;
	private Address address;

	public DebitorAddress(Contact contact, Address address) {
		this.customerNumber = contact.getCustomerNumber();
		this.name = contact.getName();
		this.firstName = contact.getFirstName();
		this.zipCode = address.getZip();
		this.city = address.getCity();
		this.company = contact.getCompany();
		this.contactType = address.getContactTypes();
		this.contactId = contact.getId();
		this.address = address;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public List<ContactType> getContactType() {
		return contactType;
	}

	public void setContactType(List<ContactType> contactType) {
		this.contactType = contactType;
	}

	public long getContactId() {
		return contactId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}