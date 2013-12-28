package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
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

/**
 * A representation of the model object '<em><b>ReceiptVoucherItem</b></em>'.
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity
@Table(name = "FKT_RECEIPTVOUCHERITEM")
public class ReceiptVoucherItem implements Serializable {
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
	@Column(name = "NAME")
	private String name = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "_RECEIPTVOUCHERITEM_ACCOUNTTYPES") })
	private List<ReceiptVoucherItemAccountType> accountTypes = new ArrayList<ReceiptVoucherItemAccountType>();

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
	@Column(name = "PRICE")
	private BigDecimal price = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	@JoinColumns({ @JoinColumn(name = "_RECEIPTVOUCHERITEM_RECEIPTVOUCHERITEMVAT") })
	private VAT receiptVoucherItemVat = null;

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
	 * Sets the '{@link ReceiptVoucherItem#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link ReceiptVoucherItem#getId() id}'
	 *            feature.
	 * @generated
	 */
	public void setId(Long newId) {
		id = newId;
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
	 * Sets the '{@link ReceiptVoucherItem#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link ReceiptVoucherItem#getName()
	 *            name}' feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Returns the value of '<em><b>accountTypes</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>accountTypes</b></em>' feature
	 * @generated
	 */
	public List<ReceiptVoucherItemAccountType> getAccountTypes() {
		return accountTypes;
	}

	/**
	 * Adds to the <em>accountTypes</em> feature.
	 * 
	 * @param accountTypesValue
	 *            the value to add
	 * @return true if the value is added to the collection (it was not yet
	 *         present in the collection), false otherwise
	 * @generated
	 */
	public boolean addToAccountTypes(
			ReceiptVoucherItemAccountType accountTypesValue) {
		if (!accountTypes.contains(accountTypesValue)) {
			boolean result = accountTypes.add(accountTypesValue);
			return result;
		}
		return false;
	}

	/**
	 * Removes from the <em>accountTypes</em> feature.
	 * 
	 * @param accountTypesValue
	 *            the value to remove
	 * @return true if the value is removed from the collection (it existed in
	 *         the collection before removing), false otherwise
	 * 
	 * @generated
	 */
	public boolean removeFromAccountTypes(
			ReceiptVoucherItemAccountType accountTypesValue) {
		if (accountTypes.contains(accountTypesValue)) {
			boolean result = accountTypes.remove(accountTypesValue);
			return result;
		}
		return false;
	}

	/**
	 * Clears the <em>accountTypes</em> feature.
	 * 
	 * @generated
	 */
	public void clearAccountTypes() {
		while (!accountTypes.isEmpty()) {
			removeFromAccountTypes(accountTypes.iterator().next());
		}
	}

	/**
	 * Sets the '{@link ReceiptVoucherItem#getAccountTypes()
	 * <em>accountTypes</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newAccountTypes
	 *            the new value of the '
	 *            {@link ReceiptVoucherItem#getAccountTypes() accountTypes}'
	 *            feature.
	 * @generated
	 */
	public void setAccountTypes(
			List<ReceiptVoucherItemAccountType> newAccountTypes) {
		accountTypes = newAccountTypes;
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
	 * Sets the '{@link ReceiptVoucherItem#getDeleted() <em>deleted</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDeleted
	 *            the new value of the '{@link ReceiptVoucherItem#getDeleted()
	 *            deleted}' feature.
	 * @generated
	 */
	public void setDeleted(Boolean newDeleted) {
		deleted = newDeleted;
	}

	/**
	 * Returns the value of '<em><b>price</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>price</b></em>' feature
	 * @generated
	 */
	public BigDecimal getPrice() {
		return price;
	}

	/**
	 * Sets the '{@link ReceiptVoucherItem#getPrice() <em>price</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newPrice
	 *            the new value of the '{@link ReceiptVoucherItem#getPrice()
	 *            price}' feature.
	 * @generated
	 */
	public void setPrice(BigDecimal newPrice) {
		price = newPrice;
	}

	/**
	 * Returns the value of '<em><b>receiptVoucherItemVat</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>receiptVoucherItemVat</b></em>' feature
	 * @generated
	 */
	public VAT getReceiptVoucherItemVat() {
		return receiptVoucherItemVat;
	}

	/**
	 * Sets the '{@link ReceiptVoucherItem#getReceiptVoucherItemVat()
	 * <em>receiptVoucherItemVat</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newReceiptVoucherItemVat
	 *            the new value of the '
	 *            {@link ReceiptVoucherItem#getReceiptVoucherItemVat()
	 *            receiptVoucherItemVat}' feature.
	 * @generated
	 */
	public void setReceiptVoucherItemVat(VAT newReceiptVoucherItemVat) {
		receiptVoucherItemVat = newReceiptVoucherItemVat;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "ReceiptVoucherItem " + " [id: " + getId() + "]" + " [name: "
				+ getName() + "]" + " [deleted: " + getDeleted() + "]"
				+ " [price: " + getPrice() + "]";
	}
}
