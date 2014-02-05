package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.Table;

/**
 * A representation of the model object '<em><b>ReceiptVoucherItem</b></em>'.
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
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
	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "FK_ITEMACCOUNTTYPE") })
	private ItemAccountType account = null;

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
	@ManyToOne(cascade = { CascadeType.REFRESH })
	@JoinColumns({ @JoinColumn(name = "FK_VAT") })
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
	 * Returns the value of '<em><b>account</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>account</b></em>' feature
	 * @generated
	 */
	public ItemAccountType getAccount() {
		return account;
	}

	/**
	 * Sets the '{@link ReceiptVoucherItem#getAccount() <em>account</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newAccount
	 *            the new value of the '{@link ReceiptVoucherItem#getAccount()
	 *            account}' feature.
	 * @generated
	 */
	public void setAccount(ItemAccountType newAccount) {
		account = newAccount;
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
	 * Returns the value of '<em><b>vat</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>vat</b></em>' feature
	 * @generated
	 */
	public VAT getReceiptVoucherItemVat() {
		return receiptVoucherItemVat;
	}

	/**
	 * Sets the '{@link ReceiptVoucherItem#getReceiptVoucherItemVat()
	 * <em>vat</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newReceiptVoucherItemVat
	 *            the new value of the '
	 *            {@link ReceiptVoucherItem#getReceiptVoucherItemVat() vat}'
	 *            feature.
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
