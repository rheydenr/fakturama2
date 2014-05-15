package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A representation of the model object '<em><b>Contact</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> here comes
 * the documentation <!-- end-model-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_CONTACT")
public class Contact extends ModelObject implements IEntity, Serializable {

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
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the Integer coded value of the contact's gender (1=male / 2=female /
     * 3=unknown) <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "GENDER")
    private Integer gender = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the company name of the contact <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "COMPANY")
    private String company = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The contact's birthday (can be <code>null</code>) <!-- end-model-doc -->
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
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(joinColumns = { @JoinColumn(name = "FK_CATEGORY") }, inverseJoinColumns = { @JoinColumn(name = "CATEGORIES_CONTACTCATEGORY") }, name = "FKT_CONTACT_CATEGORIES")
    private Set<ContactCategory> categories = new HashSet<ContactCategory>();

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinColumns({ @JoinColumn(name = "FK_ADDRESS", nullable = true) })
    private Address address = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumns({ @JoinColumn(name = "FK_DELIVERYCONTACT") })
    private Contact deliveryContact = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the individual discount which is given to the customer / contact <!--
     * end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DISCOUNT", precision = 5, scale = 3)
    private Double discount = null;

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
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The mandate reference for this contact (which in this case is a
     * customer). Used for SEPA direct debit. <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "MANDATEREFERENCE")
    private String mandateReference = null;

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
        firePropertyChange("id", this.id, newId);
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
        firePropertyChange("firstName", this.firstName, newFirstName);
    }

    /**
     * Returns the value of '<em><b>gender</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the Integer coded value of the contact's gender (1=male / 2=female /
     * 3=unknown) <!-- end-model-doc -->
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
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the Integer coded value of the contact's gender (1=male / 2=female /
     * 3=unknown) <!-- end-model-doc -->
     * 
     * @param newGender
     *            the new value of the '{@link Contact#getGender() gender}'
     *            feature.
     * @generated
     */
    public void setGender(Integer newGender) {
        gender = newGender;
        firePropertyChange("gender", this.gender, newGender);
    }

    /**
     * Returns the value of '<em><b>company</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the company name of the contact <!-- end-model-doc -->
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
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the company name of the contact <!-- end-model-doc -->
     * 
     * @param newCompany
     *            the new value of the '{@link Contact#getCompany() company}'
     *            feature.
     * @generated
     */
    public void setCompany(String newCompany) {
        company = newCompany;
        firePropertyChange("company", this.company, newCompany);
    }

    /**
     * Returns the value of '<em><b>birthday</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The contact's birthday (can be <code>null</code>) <!-- end-model-doc -->
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
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The contact's birthday (can be <code>null</code>) <!-- end-model-doc -->
     * 
     * @param newBirthday
     *            the new value of the '{@link Contact#getBirthday() birthday}'
     *            feature.
     * @generated
     */
    public void setBirthday(Date newBirthday) {
        birthday = newBirthday;
        firePropertyChange("birthday", this.birthday, newBirthday);
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
        firePropertyChange("email", this.email, newEmail);
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
        firePropertyChange("mobile", this.mobile, newMobile);
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
        firePropertyChange("account", this.account, newAccount);
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
        firePropertyChange("accountHolder", this.accountHolder, newAccountHolder);
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
        firePropertyChange("bankCode", this.bankCode, newBankCode);
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
        firePropertyChange("bankName", this.bankName, newBankName);
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
        firePropertyChange("bic", this.bic, newBic);
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
        firePropertyChange("iban", this.iban, newIban);
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
    public Set<ContactCategory> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    /**
     * Adds to the <em>categories</em> feature.
     *
     * @param categoriesValue
     *            value to add
     *
     * @generated
     */
    public boolean addToCategories(ContactCategory categoriesValue) {
        if (!categories.contains(categoriesValue)) {
            categories.add(categoriesValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes from the <em>categories</em> feature.
     *
     * @param categoriesValue
     *            value to remove
     *
     * @generated
     */
    public boolean removeFromCategories(ContactCategory categoriesValue) {
        if (categories.contains(categoriesValue)) {
            categories.remove(categoriesValue);
            return true;
        }
        else {
            return false;
        }
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
    public void setCategories(Set<ContactCategory> newCategories) {
        clearCategories();
        for (ContactCategory value : newCategories) {
            addToCategories(value);
        }
        firePropertyChange("categories", this.categories, newCategories);
    }

    /**
     * Returns the value of '<em><b>address</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>address</b></em>' feature
     * @generated
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the '{@link Contact#getAddress() <em>address</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newAddress
     *            the new value of the '{@link Contact#getAddress() address}'
     *            feature.
     * @generated
     */
    public void setAddress(Address newAddress) {
        address = newAddress;
        firePropertyChange("address", this.address, newAddress);
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
        firePropertyChange("deliveryContact", this.deliveryContact, newDeliveryContact);
    }

    /**
     * Returns the value of '<em><b>discount</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the individual discount which is given to the customer / contact <!--
     * end-model-doc -->
     * 
     * @return the value of '<em><b>discount</b></em>' feature
     * @generated
     */
    public Double getDiscount() {
        return discount;
    }

    /**
     * Sets the '{@link Contact#getDiscount() <em>discount</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the individual discount which is given to the customer / contact <!--
     * end-model-doc -->
     * 
     * @param newDiscount
     *            the new value of the '{@link Contact#getDiscount() discount}'
     *            feature.
     * @generated
     */
    public void setDiscount(Double newDiscount) {
        discount = newDiscount;
        firePropertyChange("discount", this.discount, newDiscount);
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
        firePropertyChange("title", this.title, newTitle);
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
        firePropertyChange("name", this.name, newName);
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
        firePropertyChange("dateAdded", this.dateAdded, newDateAdded);
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
        firePropertyChange("deleted", this.deleted, newDeleted);
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
        firePropertyChange("fax", this.fax, newFax);
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
        firePropertyChange("note", this.note, newNote);
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
        firePropertyChange("customerNumber", this.customerNumber, newCustomerNumber);
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
        firePropertyChange("payment", this.payment, newPayment);
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
        firePropertyChange("phone", this.phone, newPhone);
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
        firePropertyChange("reliability", this.reliability, newReliability);
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
        firePropertyChange("useNetGross", this.useNetGross, newUseNetGross);
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
        firePropertyChange("vatNumber", this.vatNumber, newVatNumber);
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
        firePropertyChange("vatNumberValid", this.vatNumberValid, newVatNumberValid);
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
        firePropertyChange("website", this.website, newWebsite);
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
        firePropertyChange("supplierNumber", this.supplierNumber, newSupplierNumber);
    }

    /**
     * Returns the value of '<em><b>mandateReference</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The mandate reference for this contact (which in this case is a
     * customer). Used for SEPA direct debit. <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>mandateReference</b></em>' feature
     * @generated
     */
    public String getMandateReference() {
        return mandateReference;
    }

    /**
     * Sets the '{@link Contact#getMandateReference() <em>mandateReference</em>}
     * ' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The mandate reference for this contact (which in this case is a
     * customer). Used for SEPA direct debit. <!-- end-model-doc -->
     * 
     * @param newMandateReference
     *            the new value of the '{@link Contact#getMandateReference()
     *            mandateReference}' feature.
     * @generated
     */
    public void setMandateReference(String newMandateReference) {
        mandateReference = newMandateReference;
        firePropertyChange("mandateReference", this.mandateReference, newMandateReference);
    }

    /**
     * A toString method which prints the values of all EAttributes of this
     * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        return "Contact " + " [id: " + getId() + "]" + " [firstName: " + getFirstName() + "]" + " [gender: " + getGender() + "]" + " [company: " + getCompany()
                + "]" + " [birthday: " + getBirthday() + "]" + " [email: " + getEmail() + "]" + " [mobile: " + getMobile() + "]" + " [account: " + getAccount()
                + "]" + " [accountHolder: " + getAccountHolder() + "]" + " [bankCode: " + getBankCode() + "]" + " [bankName: " + getBankName() + "]"
                + " [bic: " + getBic() + "]" + " [iban: " + getIban() + "]" + " [discount: " + getDiscount() + "]" + " [title: " + getTitle() + "]"
                + " [name: " + getName() + "]" + " [dateAdded: " + getDateAdded() + "]" + " [deleted: " + getDeleted() + "]" + " [fax: " + getFax() + "]"
                + " [note: " + getNote() + "]" + " [customerNumber: " + getCustomerNumber() + "]" + " [payment: " + getPayment() + "]" + " [phone: "
                + getPhone() + "]" + " [reliability: " + getReliability() + "]" + " [useNetGross: " + getUseNetGross() + "]" + " [vatNumber: " + getVatNumber()
                + "]" + " [vatNumberValid: " + getVatNumberValid() + "]" + " [website: " + getWebsite() + "]" + " [supplierNumber: " + getSupplierNumber()
                + "]" + " [mandateReference: " + getMandateReference() + "]";
    }
}
