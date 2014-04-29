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
 * A representation of the model object '<em><b>ExpenditureItem</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_EXPENDITUREITEM")
public class ExpenditureItem implements Serializable {
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
	@Column(name = "NAME")
	private String name = null;

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
	private VAT vat = null;

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
	 * Sets the '{@link ExpenditureItem#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link ExpenditureItem#getId() id}'
	 *            feature.
	 * @generated
	 */
	public void setId(Long newId) {
		id = newId;
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
	 * Sets the '{@link ExpenditureItem#getAccount() <em>account</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newAccount
	 *            the new value of the '{@link ExpenditureItem#getAccount()
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
	 * Sets the '{@link ExpenditureItem#getDeleted() <em>deleted</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDeleted
	 *            the new value of the '{@link ExpenditureItem#getDeleted()
	 *            deleted}' feature.
	 * @generated
	 */
	public void setDeleted(Boolean newDeleted) {
		deleted = newDeleted;
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
	 * Sets the '{@link ExpenditureItem#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link ExpenditureItem#getName() name}'
	 *            feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
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
	 * Sets the '{@link ExpenditureItem#getPrice() <em>price</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newPrice
	 *            the new value of the '{@link ExpenditureItem#getPrice() price}
	 *            ' feature.
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
	public VAT getVat() {
		return vat;
	}

	/**
	 * Sets the '{@link ExpenditureItem#getVat() <em>vat</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newVat
	 *            the new value of the '{@link ExpenditureItem#getVat() vat}'
	 *            feature.
	 * @generated
	 */
	public void setVat(VAT newVat) {
		vat = newVat;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "ExpenditureItem " + " [id: " + getId() + "]" + " [deleted: "
				+ getDeleted() + "]" + " [name: " + getName() + "]"
				+ " [price: " + getPrice() + "]";
	}
}
