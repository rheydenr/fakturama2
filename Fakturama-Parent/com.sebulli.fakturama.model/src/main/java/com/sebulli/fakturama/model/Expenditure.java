package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
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
 * A representation of the model object '<em><b>Expenditure</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_EXPENDITURE")
public class Expenditure implements Serializable {
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
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The account type of this Expenditure. This is formerly known as Category.
	 * <!-- end-model-doc -->
	 * 
	 * @generated
	 */
	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "FK_ACCOUNT") })
	private Account account = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "EXPENDITUREDATE")
	@Temporal(TemporalType.DATE)
	private Date expenditureDate = null;

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
	@Column(name = "DISCOUNTED")
	private Boolean discounted = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "DONOTBOOK")
	private Boolean doNotBook = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The document number of this expenditure. This is *NOT* a reference to the
	 * Document type! <!-- end-model-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "DOCUMENTNUMBER")
	private String documentNumber = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "FK_EXPENDITURE") })
	private List<ExpenditureItem> items = new ArrayList<ExpenditureItem>();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The voucher number <!-- end-model-doc -->
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
	@Column(name = "EXPENDITURENUMBER")
	private String expenditureNumber = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "PAIDVALUE")
	private BigDecimal paidValue = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "TOTALVALUE")
	private BigDecimal totalValue = null;

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
	 * Sets the '{@link Expenditure#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link Expenditure#getId() id}' feature.
	 * @generated
	 */
	public void setId(Long newId) {
		id = newId;
	}

	/**
	 * Returns the value of '<em><b>account</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The account type of this Expenditure. This is formerly known as Category.
	 * <!-- end-model-doc -->
	 * 
	 * @return the value of '<em><b>account</b></em>' feature
	 * @generated
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * Sets the '{@link Expenditure#getAccount() <em>account</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The account type of this Expenditure. This is formerly known as Category.
	 * <!-- end-model-doc -->
	 * 
	 * @param newAccount
	 *            the new value of the '{@link Expenditure#getAccount() account}
	 *            ' feature.
	 * @generated
	 */
	public void setAccount(Account newAccount) {
		account = newAccount;
	}

	/**
	 * Returns the value of '<em><b>expenditureDate</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>expenditureDate</b></em>' feature
	 * @generated
	 */
	public Date getExpenditureDate() {
		return expenditureDate;
	}

	/**
	 * Sets the '{@link Expenditure#getExpenditureDate()
	 * <em>expenditureDate</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newExpenditureDate
	 *            the new value of the '{@link Expenditure#getExpenditureDate()
	 *            expenditureDate}' feature.
	 * @generated
	 */
	public void setExpenditureDate(Date newExpenditureDate) {
		expenditureDate = newExpenditureDate;
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
	 * Sets the '{@link Expenditure#getDeleted() <em>deleted</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDeleted
	 *            the new value of the '{@link Expenditure#getDeleted() deleted}
	 *            ' feature.
	 * @generated
	 */
	public void setDeleted(Boolean newDeleted) {
		deleted = newDeleted;
	}

	/**
	 * Returns the value of '<em><b>discounted</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>discounted</b></em>' feature
	 * @generated
	 */
	public Boolean getDiscounted() {
		return discounted;
	}

	/**
	 * Sets the '{@link Expenditure#getDiscounted() <em>discounted</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDiscounted
	 *            the new value of the '{@link Expenditure#getDiscounted()
	 *            discounted}' feature.
	 * @generated
	 */
	public void setDiscounted(Boolean newDiscounted) {
		discounted = newDiscounted;
	}

	/**
	 * Returns the value of '<em><b>doNotBook</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>doNotBook</b></em>' feature
	 * @generated
	 */
	public Boolean getDoNotBook() {
		return doNotBook;
	}

	/**
	 * Sets the '{@link Expenditure#getDoNotBook() <em>doNotBook</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDoNotBook
	 *            the new value of the '{@link Expenditure#getDoNotBook()
	 *            doNotBook}' feature.
	 * @generated
	 */
	public void setDoNotBook(Boolean newDoNotBook) {
		doNotBook = newDoNotBook;
	}

	/**
	 * Returns the value of '<em><b>documentNumber</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The document number of this expenditure. This is *NOT* a reference to the
	 * Document type! <!-- end-model-doc -->
	 * 
	 * @return the value of '<em><b>documentNumber</b></em>' feature
	 * @generated
	 */
	public String getDocumentNumber() {
		return documentNumber;
	}

	/**
	 * Sets the '{@link Expenditure#getDocumentNumber() <em>documentNumber</em>}
	 * ' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The document number of this expenditure. This is *NOT* a reference to the
	 * Document type! <!-- end-model-doc -->
	 * 
	 * @param newDocumentNumber
	 *            the new value of the '{@link Expenditure#getDocumentNumber()
	 *            documentNumber}' feature.
	 * @generated
	 */
	public void setDocumentNumber(String newDocumentNumber) {
		documentNumber = newDocumentNumber;
	}

	/**
	 * Returns the value of '<em><b>items</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>items</b></em>' feature
	 * @generated
	 */
	public List<ExpenditureItem> getItems() {
		return items;
	}

	/**
	 * Adds to the <em>items</em> feature.
	 * 
	 * @param itemsValue
	 *            the value to add
	 * @return true if the value is added to the collection (it was not yet
	 *         present in the collection), false otherwise
	 * @generated
	 */
	public boolean addToItems(ExpenditureItem itemsValue) {
		if (!items.contains(itemsValue)) {
			boolean result = items.add(itemsValue);
			return result;
		}
		return false;
	}

	/**
	 * Removes from the <em>items</em> feature.
	 * 
	 * @param itemsValue
	 *            the value to remove
	 * @return true if the value is removed from the collection (it existed in
	 *         the collection before removing), false otherwise
	 * 
	 * @generated
	 */
	public boolean removeFromItems(ExpenditureItem itemsValue) {
		if (items.contains(itemsValue)) {
			boolean result = items.remove(itemsValue);
			return result;
		}
		return false;
	}

	/**
	 * Clears the <em>items</em> feature.
	 * 
	 * @generated
	 */
	public void clearItems() {
		while (!items.isEmpty()) {
			removeFromItems(items.iterator().next());
		}
	}

	/**
	 * Sets the '{@link Expenditure#getItems() <em>items</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newItems
	 *            the new value of the '{@link Expenditure#getItems() items}'
	 *            feature.
	 * @generated
	 */
	public void setItems(List<ExpenditureItem> newItems) {
		items = newItems;
	}

	/**
	 * Returns the value of '<em><b>name</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The voucher number <!-- end-model-doc -->
	 * 
	 * @return the value of '<em><b>name</b></em>' feature
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the '{@link Expenditure#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * The voucher number <!-- end-model-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link Expenditure#getName() name}'
	 *            feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Returns the value of '<em><b>expenditureNumber</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>expenditureNumber</b></em>' feature
	 * @generated
	 */
	public String getExpenditureNumber() {
		return expenditureNumber;
	}

	/**
	 * Sets the '{@link Expenditure#getExpenditureNumber()
	 * <em>expenditureNumber</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newExpenditureNumber
	 *            the new value of the '
	 *            {@link Expenditure#getExpenditureNumber() expenditureNumber}'
	 *            feature.
	 * @generated
	 */
	public void setExpenditureNumber(String newExpenditureNumber) {
		expenditureNumber = newExpenditureNumber;
	}

	/**
	 * Returns the value of '<em><b>paidValue</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>paidValue</b></em>' feature
	 * @generated
	 */
	public BigDecimal getPaidValue() {
		return paidValue;
	}

	/**
	 * Sets the '{@link Expenditure#getPaidValue() <em>paidValue</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newPaidValue
	 *            the new value of the '{@link Expenditure#getPaidValue()
	 *            paidValue}' feature.
	 * @generated
	 */
	public void setPaidValue(BigDecimal newPaidValue) {
		paidValue = newPaidValue;
	}

	/**
	 * Returns the value of '<em><b>totalValue</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>totalValue</b></em>' feature
	 * @generated
	 */
	public BigDecimal getTotalValue() {
		return totalValue;
	}

	/**
	 * Sets the '{@link Expenditure#getTotalValue() <em>totalValue</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newTotalValue
	 *            the new value of the '{@link Expenditure#getTotalValue()
	 *            totalValue}' feature.
	 * @generated
	 */
	public void setTotalValue(BigDecimal newTotalValue) {
		totalValue = newTotalValue;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "Expenditure " + " [id: " + getId() + "]"
				+ " [expenditureDate: " + getExpenditureDate() + "]"
				+ " [deleted: " + getDeleted() + "]" + " [discounted: "
				+ getDiscounted() + "]" + " [doNotBook: " + getDoNotBook()
				+ "]" + " [documentNumber: " + getDocumentNumber() + "]"
				+ " [name: " + getName() + "]" + " [expenditureNumber: "
				+ getExpenditureNumber() + "]" + " [paidValue: "
				+ getPaidValue() + "]" + " [totalValue: " + getTotalValue()
				+ "]";
	}
}
