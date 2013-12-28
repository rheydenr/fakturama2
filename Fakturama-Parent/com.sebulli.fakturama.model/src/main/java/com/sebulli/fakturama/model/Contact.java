package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A representation of the model object '<em><b>Contact</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity
@Table(name = "FKT_CONTACT")
public class Contact implements Serializable {
	/**
	 * @generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "ID")
	private Long id = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "FIRSTNAME")
	private String firstName = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "GENDER")
	private Integer gender = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "COMPANY")
	private String company = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "BIRTHDAY")
	@Temporal(TemporalType.DATE)
	private Date birthday = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "EMAIL")
	private String email = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "MOBILE")
	private String mobile = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "ACCOUNT")
	private String account = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "ACCOUNTHOLDER")
	private String accountHolder = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "BANKCODE")
	private Integer bankCode = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "BANKNAME")
	private String bankName = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "BIC")
	private String bic = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "IBAN")
	private String iban = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "FK_CATEGORY") })
	private List<ContactCategory> categories = new ArrayList<ContactCategory>();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "_CONTACT_ADDRESSES") })
	private List<Address> addresses = new ArrayList<Address>();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	@JoinColumns({ @JoinColumn(name = "FK_CONTACT") })
	private Contact deliveryContact = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "DISCOUNT")
	private BigDecimal discount = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "TITLE")
	private String title = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "NAME")
	private String name = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "DATEADDED")
	@Temporal(TemporalType.DATE)
	private Date dateAdded = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "DELETED")
	private Boolean deleted = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "FAX")
	private String fax = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "NOTE")
	private String note = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "CUSTOMERNUMBER")
	private String customerNumber = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "PAYMENT")
	private Integer payment = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "PHONE")
	private String phone = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "RELIABILITY")
	private Integer reliability = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "USENETGROSS")
	private Boolean useNetGross = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "VATNUMBER")
	private String vatNumber = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "VATNUMBERVALID")
	private Boolean vatNumberValid = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "WEBSITE")
	private String website = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "SUPPLIERNUMBER")
	private String supplierNumber = null;

	/**
	 * Returns the value of '<em><b>id</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>id</b></em>' feature
	 * @generated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the '{@link Contact#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link Contact#getId() id}' feature.
	 * @generated
	 */
	public void setId(Long newId) {
		id = newId;
	}

	/**
	 * Returns the value of '<em><b>firstName</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>firstName</b></em>' feature
	 * @generated
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the '{@link Contact#getFirstName() <em>firstName</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newFirstName
	 *            the new value of the '{@link Contact#getFirstName() firstName}
	 *            ' feature.
	 * @generated
	 */
	public void setFirstName(String newFirstName) {
		firstName = newFirstName;
	}

	/**
	 * Returns the value of '<em><b>gender</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>gender</b></em>' feature
	 * @generated
	 */
	public Integer getGender() {
		return gender;
	}

	/**
	 * Sets the '{@link Contact#getGender() <em>gender</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newGender
	 *            the new value of the '{@link Contact#getGender() gender}'
	 *            feature.
	 * @generated
	 */
	public void setGender(Integer newGender) {
		gender = newGender;
	}

	/**
	 * Returns the value of '<em><b>company</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>company</b></em>' feature
	 * @generated
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * Sets the '{@link Contact#getCompany() <em>company</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newCompany
	 *            the new value of the '{@link Contact#getCompany() company}'
	 *            feature.
	 * @generated
	 */
	public void setCompany(String newCompany) {
		company = newCompany;
	}

	/**
	 * Returns the value of '<em><b>birthday</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>birthday</b></em>' feature
	 * @generated
	 */
	public Date getBirthday() {
		return birthday;
	}

	/**
	 * Sets the '{@link Contact#getBirthday() <em>birthday</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newBirthday
	 *            the new value of the '{@link Contact#getBirthday() birthday}'
	 *            feature.
	 * @generated
	 */
	public void setBirthday(Date newBirthday) {
		birthday = newBirthday;
	}

	/**
	 * Returns the value of '<em><b>email</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>email</b></em>' feature
	 * @generated
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the '{@link Contact#getEmail() <em>email</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newEmail
	 *            the new value of the '{@link Contact#getEmail() email}'
	 *            feature.
	 * @generated
	 */
	public void setEmail(String newEmail) {
		email = newEmail;
	}

	/**
	 * Returns the value of '<em><b>mobile</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>mobile</b></em>' feature
	 * @generated
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * Sets the '{@link Contact#getMobile() <em>mobile</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newMobile
	 *            the new value of the '{@link Contact#getMobile() mobile}'
	 *            feature.
	 * @generated
	 */
	public void setMobile(String newMobile) {
		mobile = newMobile;
	}

	/**
	 * Returns the value of '<em><b>account</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>account</b></em>' feature
	 * @generated
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * Sets the '{@link Contact#getAccount() <em>account</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newAccount
	 *            the new value of the '{@link Contact#getAccount() account}'
	 *            feature.
	 * @generated
	 */
	public void setAccount(String newAccount) {
		account = newAccount;
	}

	/**
	 * Returns the value of '<em><b>accountHolder</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>accountHolder</b></em>' feature
	 * @generated
	 */
	public String getAccountHolder() {
		return accountHolder;
	}

	/**
	 * Sets the '{@link Contact#getAccountHolder() <em>accountHolder</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newAccountHolder
	 *            the new value of the '{@link Contact#getAccountHolder()
	 *            accountHolder}' feature.
	 * @generated
	 */
	public void setAccountHolder(String newAccountHolder) {
		accountHolder = newAccountHolder;
	}

	/**
	 * Returns the value of '<em><b>bankCode</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>bankCode</b></em>' feature
	 * @generated
	 */
	public Integer getBankCode() {
		return bankCode;
	}

	/**
	 * Sets the '{@link Contact#getBankCode() <em>bankCode</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newBankCode
	 *            the new value of the '{@link Contact#getBankCode() bankCode}'
	 *            feature.
	 * @generated
	 */
	public void setBankCode(Integer newBankCode) {
		bankCode = newBankCode;
	}

	/**
	 * Returns the value of '<em><b>bankName</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>bankName</b></em>' feature
	 * @generated
	 */
	public String getBankName() {
		return bankName;
	}

	/**
	 * Sets the '{@link Contact#getBankName() <em>bankName</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newBankName
	 *            the new value of the '{@link Contact#getBankName() bankName}'
	 *            feature.
	 * @generated
	 */
	public void setBankName(String newBankName) {
		bankName = newBankName;
	}

	/**
	 * Returns the value of '<em><b>bic</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>bic</b></em>' feature
	 * @generated
	 */
	public String getBic() {
		return bic;
	}

	/**
	 * Sets the '{@link Contact#getBic() <em>bic</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newBic
	 *            the new value of the '{@link Contact#getBic() bic}' feature.
	 * @generated
	 */
	public void setBic(String newBic) {
		bic = newBic;
	}

	/**
	 * Returns the value of '<em><b>iban</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>iban</b></em>' feature
	 * @generated
	 */
	public String getIban() {
		return iban;
	}

	/**
	 * Sets the '{@link Contact#getIban() <em>iban</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newIban
	 *            the new value of the '{@link Contact#getIban() iban}' feature.
	 * @generated
	 */
	public void setIban(String newIban) {
		iban = newIban;
	}

	/**
	 * Returns the value of '<em><b>categories</b></em>' feature. Note: the
	 * returned collection is Unmodifiable use the
	 * {#addToCategories(com.sebulli.fakturama.model.ContactCategory value)} and
	 * {@link #removeFromCategories(ContactCategory value)} methods to modify
	 * this feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>categories</b></em>' feature
	 * @generated
	 */
	public List<ContactCategory> getCategories() {
		return Collections.unmodifiableList(categories);
	}

	/**
	 * Adds to the <em>categories</em> feature.
	 * 
	 * @param categoriesValue
	 *            the value to add
	 * @return true if the value is added to the collection (it was not yet
	 *         present in the collection), false otherwise
	 * @generated
	 */
	public boolean addToCategories(ContactCategory categoriesValue) {
		if (!categories.contains(categoriesValue)) {
			boolean result = categories.add(categoriesValue);
			return result;
		}
		return false;
	}

	/**
	 * Removes from the <em>categories</em> feature.
	 * 
	 * @param categoriesValue
	 *            the value to remove
	 * @return true if the value is removed from the collection (it existed in
	 *         the collection before removing), false otherwise
	 * 
	 * @generated
	 */
	public boolean removeFromCategories(ContactCategory categoriesValue) {
		if (categories.contains(categoriesValue)) {
			boolean result = categories.remove(categoriesValue);
			return result;
		}
		return false;
	}

	/**
	 * Clears the <em>categories</em> feature.
	 * 
	 * @generated
	 */
	public void clearCategories() {
		while (!categories.isEmpty()) {
			removeFromCategories(categories.iterator().next());
		}
	}

	/**
	 * Sets the '{@link Contact#getCategories() <em>categories</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newCategories
	 *            the new value of the '{@link Contact#getCategories()
	 *            categories}' feature.
	 * @generated
	 */
	public void setCategories(List<ContactCategory> newCategories) {
		clearCategories();
		for (ContactCategory value : newCategories) {
			addToCategories(value);
		}
	}

	/**
	 * Returns the value of '<em><b>addresses</b></em>' feature. Note: the
	 * returned collection is Unmodifiable use the
	 * {#addToAddresses(com.sebulli.fakturama.model.Address value)} and
	 * {@link #removeFromAddresses(Address value)} methods to modify this
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>addresses</b></em>' feature
	 * @generated
	 */
	public List<Address> getAddresses() {
		return Collections.unmodifiableList(addresses);
	}

	/**
	 * Adds to the <em>addresses</em> feature.
	 * 
	 * @param addressesValue
	 *            the value to add
	 * @return true if the value is added to the collection (it was not yet
	 *         present in the collection), false otherwise
	 * @generated
	 */
	public boolean addToAddresses(Address addressesValue) {
		if (!addresses.contains(addressesValue)) {
			boolean result = addresses.add(addressesValue);
			return result;
		}
		return false;
	}

	/**
	 * Removes from the <em>addresses</em> feature.
	 * 
	 * @param addressesValue
	 *            the value to remove
	 * @return true if the value is removed from the collection (it existed in
	 *         the collection before removing), false otherwise
	 * 
	 * @generated
	 */
	public boolean removeFromAddresses(Address addressesValue) {
		if (addresses.contains(addressesValue)) {
			boolean result = addresses.remove(addressesValue);
			return result;
		}
		return false;
	}

	/**
	 * Clears the <em>addresses</em> feature.
	 * 
	 * @generated
	 */
	public void clearAddresses() {
		while (!addresses.isEmpty()) {
			removeFromAddresses(addresses.iterator().next());
		}
	}

	/**
	 * Sets the '{@link Contact#getAddresses() <em>addresses</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newAddresses
	 *            the new value of the '{@link Contact#getAddresses() addresses}
	 *            ' feature.
	 * @generated
	 */
	public void setAddresses(List<Address> newAddresses) {
		clearAddresses();
		for (Address value : newAddresses) {
			addToAddresses(value);
		}
	}

	/**
	 * Returns the value of '<em><b>deliveryContact</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>deliveryContact</b></em>' feature
	 * @generated
	 */
	public Contact getDeliveryContact() {
		return deliveryContact;
	}

	/**
	 * Sets the '{@link Contact#getDeliveryContact() <em>deliveryContact</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDeliveryContact
	 *            the new value of the '{@link Contact#getDeliveryContact()
	 *            deliveryContact}' feature.
	 * @generated
	 */
	public void setDeliveryContact(Contact newDeliveryContact) {
		deliveryContact = newDeliveryContact;
	}

	/**
	 * Returns the value of '<em><b>discount</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>discount</b></em>' feature
	 * @generated
	 */
	public BigDecimal getDiscount() {
		return discount;
	}

	/**
	 * Sets the '{@link Contact#getDiscount() <em>discount</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDiscount
	 *            the new value of the '{@link Contact#getDiscount() discount}'
	 *            feature.
	 * @generated
	 */
	public void setDiscount(BigDecimal newDiscount) {
		discount = newDiscount;
	}

	/**
	 * Returns the value of '<em><b>title</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>title</b></em>' feature
	 * @generated
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the '{@link Contact#getTitle() <em>title</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newTitle
	 *            the new value of the '{@link Contact#getTitle() title}'
	 *            feature.
	 * @generated
	 */
	public void setTitle(String newTitle) {
		title = newTitle;
	}

	/**
	 * Returns the value of '<em><b>name</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>name</b></em>' feature
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the '{@link Contact#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link Contact#getName() name}' feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Returns the value of '<em><b>dateAdded</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>dateAdded</b></em>' feature
	 * @generated
	 */
	public Date getDateAdded() {
		return dateAdded;
	}

	/**
	 * Sets the '{@link Contact#getDateAdded() <em>dateAdded</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDateAdded
	 *            the new value of the '{@link Contact#getDateAdded() dateAdded}
	 *            ' feature.
	 * @generated
	 */
	public void setDateAdded(Date newDateAdded) {
		dateAdded = newDateAdded;
	}

	/**
	 * Returns the value of '<em><b>deleted</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>deleted</b></em>' feature
	 * @generated
	 */
	public Boolean getDeleted() {
		return deleted;
	}

	/**
	 * Sets the '{@link Contact#getDeleted() <em>deleted</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDeleted
	 *            the new value of the '{@link Contact#getDeleted() deleted}'
	 *            feature.
	 * @generated
	 */
	public void setDeleted(Boolean newDeleted) {
		deleted = newDeleted;
	}

	/**
	 * Returns the value of '<em><b>fax</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>fax</b></em>' feature
	 * @generated
	 */
	public String getFax() {
		return fax;
	}

	/**
	 * Sets the '{@link Contact#getFax() <em>fax</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newFax
	 *            the new value of the '{@link Contact#getFax() fax}' feature.
	 * @generated
	 */
	public void setFax(String newFax) {
		fax = newFax;
	}

	/**
	 * Returns the value of '<em><b>note</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>note</b></em>' feature
	 * @generated
	 */
	public String getNote() {
		return note;
	}

	/**
	 * Sets the '{@link Contact#getNote() <em>note</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newNote
	 *            the new value of the '{@link Contact#getNote() note}' feature.
	 * @generated
	 */
	public void setNote(String newNote) {
		note = newNote;
	}

	/**
	 * Returns the value of '<em><b>customerNumber</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>customerNumber</b></em>' feature
	 * @generated
	 */
	public String getCustomerNumber() {
		return customerNumber;
	}

	/**
	 * Sets the '{@link Contact#getCustomerNumber() <em>customerNumber</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newCustomerNumber
	 *            the new value of the '{@link Contact#getCustomerNumber()
	 *            customerNumber}' feature.
	 * @generated
	 */
	public void setCustomerNumber(String newCustomerNumber) {
		customerNumber = newCustomerNumber;
	}

	/**
	 * Returns the value of '<em><b>payment</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>payment</b></em>' feature
	 * @generated
	 */
	public Integer getPayment() {
		return payment;
	}

	/**
	 * Sets the '{@link Contact#getPayment() <em>payment</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newPayment
	 *            the new value of the '{@link Contact#getPayment() payment}'
	 *            feature.
	 * @generated
	 */
	public void setPayment(Integer newPayment) {
		payment = newPayment;
	}

	/**
	 * Returns the value of '<em><b>phone</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>phone</b></em>' feature
	 * @generated
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Sets the '{@link Contact#getPhone() <em>phone</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newPhone
	 *            the new value of the '{@link Contact#getPhone() phone}'
	 *            feature.
	 * @generated
	 */
	public void setPhone(String newPhone) {
		phone = newPhone;
	}

	/**
	 * Returns the value of '<em><b>reliability</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>reliability</b></em>' feature
	 * @generated
	 */
	public Integer getReliability() {
		return reliability;
	}

	/**
	 * Sets the '{@link Contact#getReliability() <em>reliability</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newReliability
	 *            the new value of the '{@link Contact#getReliability()
	 *            reliability}' feature.
	 * @generated
	 */
	public void setReliability(Integer newReliability) {
		reliability = newReliability;
	}

	/**
	 * Returns the value of '<em><b>useNetGross</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>useNetGross</b></em>' feature
	 * @generated
	 */
	public Boolean getUseNetGross() {
		return useNetGross;
	}

	/**
	 * Sets the '{@link Contact#getUseNetGross() <em>useNetGross</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newUseNetGross
	 *            the new value of the '{@link Contact#getUseNetGross()
	 *            useNetGross}' feature.
	 * @generated
	 */
	public void setUseNetGross(Boolean newUseNetGross) {
		useNetGross = newUseNetGross;
	}

	/**
	 * Returns the value of '<em><b>vatNumber</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>vatNumber</b></em>' feature
	 * @generated
	 */
	public String getVatNumber() {
		return vatNumber;
	}

	/**
	 * Sets the '{@link Contact#getVatNumber() <em>vatNumber</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newVatNumber
	 *            the new value of the '{@link Contact#getVatNumber() vatNumber}
	 *            ' feature.
	 * @generated
	 */
	public void setVatNumber(String newVatNumber) {
		vatNumber = newVatNumber;
	}

	/**
	 * Returns the value of '<em><b>vatNumberValid</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>vatNumberValid</b></em>' feature
	 * @generated
	 */
	public Boolean getVatNumberValid() {
		return vatNumberValid;
	}

	/**
	 * Sets the '{@link Contact#getVatNumberValid() <em>vatNumberValid</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newVatNumberValid
	 *            the new value of the '{@link Contact#getVatNumberValid()
	 *            vatNumberValid}' feature.
	 * @generated
	 */
	public void setVatNumberValid(Boolean newVatNumberValid) {
		vatNumberValid = newVatNumberValid;
	}

	/**
	 * Returns the value of '<em><b>website</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>website</b></em>' feature
	 * @generated
	 */
	public String getWebsite() {
		return website;
	}

	/**
	 * Sets the '{@link Contact#getWebsite() <em>website</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newWebsite
	 *            the new value of the '{@link Contact#getWebsite() website}'
	 *            feature.
	 * @generated
	 */
	public void setWebsite(String newWebsite) {
		website = newWebsite;
	}

	/**
	 * Returns the value of '<em><b>supplierNumber</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>supplierNumber</b></em>' feature
	 * @generated
	 */
	public String getSupplierNumber() {
		return supplierNumber;
	}

	/**
	 * Sets the '{@link Contact#getSupplierNumber() <em>supplierNumber</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newSupplierNumber
	 *            the new value of the '{@link Contact#getSupplierNumber()
	 *            supplierNumber}' feature.
	 * @generated
	 */
	public void setSupplierNumber(String newSupplierNumber) {
		supplierNumber = newSupplierNumber;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "Contact " + " [id: " + getId() + "]" + " [firstName: "
				+ getFirstName() + "]" + " [gender: " + getGender() + "]"
				+ " [company: " + getCompany() + "]" + " [birthday: "
				+ getBirthday() + "]" + " [email: " + getEmail() + "]"
				+ " [mobile: " + getMobile() + "]" + " [account: "
				+ getAccount() + "]" + " [accountHolder: " + getAccountHolder()
				+ "]" + " [bankCode: " + getBankCode() + "]" + " [bankName: "
				+ getBankName() + "]" + " [bic: " + getBic() + "]" + " [iban: "
				+ getIban() + "]" + " [discount: " + getDiscount() + "]"
				+ " [title: " + getTitle() + "]" + " [name: " + getName() + "]"
				+ " [dateAdded: " + getDateAdded() + "]" + " [deleted: "
				+ getDeleted() + "]" + " [fax: " + getFax() + "]" + " [note: "
				+ getNote() + "]" + " [customerNumber: " + getCustomerNumber()
				+ "]" + " [payment: " + getPayment() + "]" + " [phone: "
				+ getPhone() + "]" + " [reliability: " + getReliability() + "]"
				+ " [useNetGross: " + getUseNetGross() + "]" + " [vatNumber: "
				+ getVatNumber() + "]" + " [vatNumberValid: "
				+ getVatNumberValid() + "]" + " [website: " + getWebsite()
				+ "]" + " [supplierNumber: " + getSupplierNumber() + "]";
	}
}
