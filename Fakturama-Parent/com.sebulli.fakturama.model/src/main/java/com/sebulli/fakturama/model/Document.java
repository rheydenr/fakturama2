package com.sebulli.fakturama.model;

import java.io.Serializable;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * A representation of the model object '<em><b>Document</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity
@Table(name = "FKT_DOCUMENT")
public class Document implements Serializable {
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
	@JoinColumns({ @JoinColumn(name = "_DOCUMENT_ITEMS") })
	private List<Item> items = new ArrayList<Item>();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "_DOCUMENT_CATEGORIES") })
	private List<BillingType> categories = new ArrayList<BillingType>();

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
	 * Sets the '{@link Document#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link Document#getId() id}' feature.
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
	 * Sets the '{@link Document#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link Document#getName() name}'
	 *            feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Returns the value of '<em><b>items</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>items</b></em>' feature
	 * @generated
	 */
	public List<Item> getItems() {
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
	public boolean addToItems(Item itemsValue) {
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
	public boolean removeFromItems(Item itemsValue) {
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
	 * Sets the '{@link Document#getItems() <em>items</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newItems
	 *            the new value of the '{@link Document#getItems() items}'
	 *            feature.
	 * @generated
	 */
	public void setItems(List<Item> newItems) {
		items = newItems;
	}

	/**
	 * Returns the value of '<em><b>categories</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>categories</b></em>' feature
	 * @generated
	 */
	public List<BillingType> getCategories() {
		return categories;
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
	public boolean addToCategories(BillingType categoriesValue) {
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
	public boolean removeFromCategories(BillingType categoriesValue) {
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
	 * Sets the '{@link Document#getCategories() <em>categories</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newCategories
	 *            the new value of the '{@link Document#getCategories()
	 *            categories}' feature.
	 * @generated
	 */
	public void setCategories(List<BillingType> newCategories) {
		categories = newCategories;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "Document " + " [id: " + getId() + "]" + " [name: " + getName()
				+ "]";
	}
}
