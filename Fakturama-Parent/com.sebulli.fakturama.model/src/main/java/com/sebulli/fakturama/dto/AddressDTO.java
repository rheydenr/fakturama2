package com.sebulli.fakturama.dto;

import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.DocumentReceiver;

public class AddressDTO {
	private String company = null;
	private String firstName = null;
	private String customerNumber = null;

	private Long addressId = null;
	private String title = null;
	private String countryCode;
	private String street, zip, city, cityAddon;
	private Integer gender = Integer.valueOf(0);
	private String email = null;
	private String supplierNumber = null;
	private Long gln = null;
	private String manualAddress = null;
	private String name = null;
    
    public static AddressDTO from(DocumentReceiver documentReceiver) {
    	AddressDTO addressDTO = new AddressDTO()
			.withAddressId(documentReceiver.getOriginAddressId())
			.withCompany(documentReceiver.getCompany())
			.withName(documentReceiver.getName())
			.withFirstName(documentReceiver.getFirstName())
			.withManualAddress(documentReceiver.getManualAddress())
			.withCustomerNumber(documentReceiver.getCustomerNumber())
			.withCountryCode(documentReceiver.getCountryCode())
			.withGender(documentReceiver.getGender())
			.withStreet(documentReceiver.getStreet())
			.withCity(documentReceiver.getCity())
			.withZip(documentReceiver.getZip())
			;
    	return addressDTO;
    }
    
	public static AddressDTO from(Contact contact, Address specificAddress) {
		AddressDTO addressDTO = new AddressDTO().withCompany(contact.getCompany()) //
				.withName(contact.getName()) //
				.withFirstName(contact.getFirstName()) //
				.withCustomerNumber(contact.getCustomerNumber()) //
				.withGender(contact.getGender()); //
		if (specificAddress != null) {
			addressDTO = addressDTO.withAddressId(specificAddress.getId())
					.withCountryCode(specificAddress.getCountryCode()) //
					.withStreet(specificAddress.getStreet()) //
					.withCity(specificAddress.getCity()) //
					.withZip(specificAddress.getZip());
		}
		return addressDTO;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setManualAddress(String manualAddress) {
		this.manualAddress = manualAddress;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AddressDTO withCompany(String company) {
		this.company = company;
		return this;
	}

	public String getName() {
		return name;
	}

	public AddressDTO withName(String name) {
		this.name = name;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public AddressDTO withFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getManualAddress() {
		return manualAddress;
	}

	public AddressDTO withManualAddress(String manualAddress) {
		this.manualAddress = manualAddress;
		return this;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public AddressDTO withCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
		return this;
	}

	public Long getAddressId() {
		return addressId;
	}

	public AddressDTO withAddressId(Long addressId) {
		this.addressId = addressId;
		return this;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public AddressDTO withCountryCode(String countryCode) {
		this.countryCode = countryCode;
		return this;
	}

	public Integer getGender() {
		return gender;
	}

	public AddressDTO withGender(Integer gender) {
		this.gender = gender;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public AddressDTO withTitle(String title) {
		this.title = title;
		return this;
	}

	public String getStreet() {
		return street;
	}

	public AddressDTO withStreet(String street) {
		this.street = street;
		return this;
	}

	public String getZip() {
		return zip;
	}

	public AddressDTO withZip(String zip) {
		this.zip = zip;
		return this;
	}

	public String getCity() {
		return city;
	}

	public AddressDTO withCity(String city) {
		this.city = city;
		return this;
	}

}
