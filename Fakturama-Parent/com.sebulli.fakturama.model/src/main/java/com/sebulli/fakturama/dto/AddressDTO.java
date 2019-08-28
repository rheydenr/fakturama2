package com.sebulli.fakturama.dto;

public class AddressDTO {
	private String company = null;
	private String firstName = null;
	private String customerNumber = null;
	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	private String title = null;
	private Integer gender = Integer.valueOf(0);
	private String email = null;
	private String supplierNumber = null;
	private Long gln = null;
	private String manualAddress = null;
	private String name = null;

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

}
