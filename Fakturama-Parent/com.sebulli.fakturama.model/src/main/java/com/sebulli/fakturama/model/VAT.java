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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A representation of the model object '<em><b>VAT</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_VAT")
public class VAT implements Serializable {
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
	@Basic()
	@Column(name = "DELETED")
	private Boolean deleted = Boolean.FALSE;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "DESCRIPTION")
	private String description = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "TAXVALUE")
	private Double taxValue = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "MODIFIED")
	@Temporal(TemporalType.DATE)
	private Date modified = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	@JoinTable(joinColumns = { @JoinColumn(name = "VAT_CATEGORIES") }, inverseJoinColumns = { @JoinColumn(name = "CATEGORIES_VATCATEGORY") }, name = "FKT_VAT_CATEGORIES")
	private Set<VATCategory> categories = new HashSet<VATCategory>();

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
	 * Sets the '{@link VAT#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link VAT#getId() id}' feature.
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
	 * Sets the '{@link VAT#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link VAT#getName() name}' feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
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
	 * Sets the '{@link VAT#getDeleted() <em>deleted</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDeleted
	 *            the new value of the '{@link VAT#getDeleted() deleted}'
	 *            feature.
	 * @generated
	 */
	public void setDeleted(Boolean newDeleted) {
		deleted = newDeleted;
	}

	/**
	 * Returns the value of '<em><b>description</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>description</b></em>' feature
	 * @generated
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the '{@link VAT#getDescription() <em>description</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDescription
	 *            the new value of the '{@link VAT#getDescription() description}
	 *            ' feature.
	 * @generated
	 */
	public void setDescription(String newDescription) {
		description = newDescription;
	}

	/**
	 * Returns the value of '<em><b>taxValue</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>taxValue</b></em>' feature
	 * @generated
	 */
	public Double getTaxValue() {
		return taxValue;
	}

	/**
	 * Sets the '{@link VAT#getTaxValue() <em>taxValue</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newTaxValue
	 *            the new value of the '{@link VAT#getTaxValue() taxValue}'
	 *            feature.
	 * @generated
	 */
	public void setTaxValue(Double newTaxValue) {
		taxValue = newTaxValue;
	}

	/**
	 * Returns the value of '<em><b>modified</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>modified</b></em>' feature
	 * @generated
	 */
	public Date getModified() {
		return modified;
	}

	/**
	 * Sets the '{@link VAT#getModified() <em>modified</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newModified
	 *            the new value of the '{@link VAT#getModified() modified}'
	 *            feature.
	 * @generated
	 */
	public void setModified(Date newModified) {
		modified = newModified;
	}

	/**
	 * Returns the value of '<em><b>categories</b></em>' feature. Note: the
	 * returned collection is Unmodifiable use the
	 * {#addToCategories(com.sebulli.fakturama.model.VATCategory value)} and
	 * {@link #removeFromCategories(VATCategory value)} methods to modify this
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>categories</b></em>' feature
	 * @generated
	 */
	public Set<VATCategory> getCategories() {
		return Collections.unmodifiableSet(categories);
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
	public boolean addToCategories(VATCategory categoriesValue) {
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
	public boolean removeFromCategories(VATCategory categoriesValue) {
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
	 * Sets the '{@link VAT#getCategories() <em>categories</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newCategories
	 *            the new value of the '{@link VAT#getCategories() categories}'
	 *            feature.
	 * @generated
	 */
	public void setCategories(Set<VATCategory> newCategories) {
		clearCategories();
		for (VATCategory value : newCategories) {
			addToCategories(value);
		}
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "VAT " + " [id: " + getId() + "]" + " [name: " + getName() + "]"
				+ " [deleted: " + getDeleted() + "]" + " [description: "
				+ getDescription() + "]" + " [taxValue: " + getTaxValue() + "]"
				+ " [modified: " + getModified() + "]";
	}
}
