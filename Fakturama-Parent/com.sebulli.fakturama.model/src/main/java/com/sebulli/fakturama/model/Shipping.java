package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.util.Collections;
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

/**
 * A representation of the model object '<em><b>Shipping</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_SHIPPING")
public class Shipping extends ModelObject implements IEntity, Serializable {

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
    @Column(name = "AUTOVAT")
    private Boolean autoVat = null;

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
    @Column(name = "DESCRIPTION")
    private String description = null;

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
    @Column(name = "SHIPPINGVALUE")
    private Double shippingValue = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.REFRESH }, optional = false)
    @JoinColumns({ @JoinColumn(name = "FK_VAT") })
    private VAT shippingVat = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(joinColumns = { @JoinColumn(name = "SHIPPING_CATEGORIES") }, inverseJoinColumns = { @JoinColumn(name = "CATEGORIES_SHIPPINGCATEGORY") }, name = "FKT_SHIPPING_CATEGORIES")
    private Set<ShippingCategory> categories = new HashSet<ShippingCategory>();

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
     * Sets the '{@link Shipping#getId() <em>id</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newId
     *            the new value of the '{@link Shipping#getId() id}' feature.
     * @generated
     */
    public void setId(Long newId) {
        id = newId;
        firePropertyChange("id", this.id, newId);
    }

    /**
     * Returns the value of '<em><b>autoVat</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>autoVat</b></em>' feature
     * @generated
     */
    public Boolean getAutoVat() {
        return autoVat;
    }

    /**
     * Sets the '{@link Shipping#getAutoVat() <em>autoVat</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newAutoVat
     *            the new value of the '{@link Shipping#getAutoVat() autoVat}'
     *            feature.
     * @generated
     */
    public void setAutoVat(Boolean newAutoVat) {
        autoVat = newAutoVat;
        firePropertyChange("autoVat", this.autoVat, newAutoVat);
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
     * Sets the '{@link Shipping#getDeleted() <em>deleted</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDeleted
     *            the new value of the '{@link Shipping#getDeleted() deleted}'
     *            feature.
     * @generated
     */
    public void setDeleted(Boolean newDeleted) {
        deleted = newDeleted;
        firePropertyChange("deleted", this.deleted, newDeleted);
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
     * Sets the '{@link Shipping#getDescription() <em>description</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDescription
     *            the new value of the '{@link Shipping#getDescription()
     *            description}' feature.
     * @generated
     */
    public void setDescription(String newDescription) {
        description = newDescription;
        firePropertyChange("description", this.description, newDescription);
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
     * Sets the '{@link Shipping#getName() <em>name</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newName
     *            the new value of the '{@link Shipping#getName() name}'
     *            feature.
     * @generated
     */
    public void setName(String newName) {
        name = newName;
        firePropertyChange("name", this.name, newName);
    }

    /**
     * Returns the value of '<em><b>shippingValue</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>shippingValue</b></em>' feature
     * @generated
     */
    public Double getShippingValue() {
        return shippingValue;
    }

    /**
     * Sets the '{@link Shipping#getShippingValue() <em>shippingValue</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newShippingValue
     *            the new value of the '{@link Shipping#getShippingValue()
     *            shippingValue}' feature.
     * @generated
     */
    public void setShippingValue(Double newShippingValue) {
        shippingValue = newShippingValue;
        firePropertyChange("shippingValue", this.shippingValue, newShippingValue);
    }

    /**
     * Returns the value of '<em><b>shippingVat</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>shippingVat</b></em>' feature
     * @generated
     */
    public VAT getShippingVat() {
        return shippingVat;
    }

    /**
     * Sets the '{@link Shipping#getShippingVat() <em>shippingVat</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newShippingVat
     *            the new value of the '{@link Shipping#getShippingVat()
     *            shippingVat}' feature.
     * @generated
     */
    public void setShippingVat(VAT newShippingVat) {
        shippingVat = newShippingVat;
        firePropertyChange("shippingVat", this.shippingVat, newShippingVat);
    }

    /**
     * Returns the value of '<em><b>categories</b></em>' feature. Note: the
     * returned collection is Unmodifiable use the
     * {#addToCategories(com.sebulli.fakturama.model.ShippingCategory value)}
     * and {@link #removeFromCategories(ShippingCategory value)} methods to
     * modify this feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>categories</b></em>' feature
     * @generated
     */
    public Set<ShippingCategory> getCategories() {
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
    public boolean addToCategories(ShippingCategory categoriesValue) {
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
    public boolean removeFromCategories(ShippingCategory categoriesValue) {
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
     * Sets the '{@link Shipping#getCategories() <em>categories</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newCategories
     *            the new value of the '{@link Shipping#getCategories()
     *            categories}' feature.
     * @generated
     */
    public void setCategories(Set<ShippingCategory> newCategories) {
        clearCategories();
        for (ShippingCategory value : newCategories) {
            addToCategories(value);
        }
        firePropertyChange("categories", this.categories, newCategories);
    }

    /**
     * A toString method which prints the values of all EAttributes of this
     * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        return "Shipping " + " [id: " + getId() + "]" + " [autoVat: " + getAutoVat() + "]" + " [deleted: " + getDeleted() + "]" + " [description: "
                + getDescription() + "]" + " [name: " + getName() + "]" + " [shippingValue: " + getShippingValue() + "]";
    }
}
