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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * A representation of the model object '<em><b>Payment</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_PAYMENT")
public class Payment extends ModelObject implements IEntity, Serializable {

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
    @Column(name = "DEFAULTPAID")
    private Boolean defaultPaid = null;

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
    @Column(name = "DISCOUNTDAYS")
    private Integer discountDays = null;

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
    @Column(name = "DISCOUNTVALUE")
    private Double discountValue = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "NETDAYS")
    private Integer netDays = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(joinColumns = { @JoinColumn(name = "PAYMENT_CATEGORIES") }, inverseJoinColumns = { @JoinColumn(name = "CATEGORIES_PAYMENTCATEGORY") }, name = "FKT_PAYMENT_CATEGORIES")
    private List<PaymentCategory> categories = new ArrayList<PaymentCategory>();

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(length = 1024, name = "PAIDTEXT")
    private String paidText = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(length = 1024, name = "UNPAIDTEXT")
    private String unpaidText = null;

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
     * Sets the '{@link Payment#getId() <em>id</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newId
     *            the new value of the '{@link Payment#getId() id}' feature.
     * @generated
     */
    public void setId(Long newId) {
        id = newId;
        firePropertyChange("id", this.id, newId);
    }

    /**
     * Returns the value of '<em><b>defaultPaid</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>defaultPaid</b></em>' feature
     * @generated
     */
    public Boolean getDefaultPaid() {
        return defaultPaid;
    }

    /**
     * Sets the '{@link Payment#getDefaultPaid() <em>defaultPaid</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDefaultPaid
     *            the new value of the '{@link Payment#getDefaultPaid()
     *            defaultPaid}' feature.
     * @generated
     */
    public void setDefaultPaid(Boolean newDefaultPaid) {
        defaultPaid = newDefaultPaid;
        firePropertyChange("defaultPaid", this.defaultPaid, newDefaultPaid);
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
     * Sets the '{@link Payment#getDeleted() <em>deleted</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDeleted
     *            the new value of the '{@link Payment#getDeleted() deleted}'
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
     * Sets the '{@link Payment#getDescription() <em>description</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDescription
     *            the new value of the '{@link Payment#getDescription()
     *            description}' feature.
     * @generated
     */
    public void setDescription(String newDescription) {
        description = newDescription;
        firePropertyChange("description", this.description, newDescription);
    }

    /**
     * Returns the value of '<em><b>discountDays</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>discountDays</b></em>' feature
     * @generated
     */
    public Integer getDiscountDays() {
        return discountDays;
    }

    /**
     * Sets the '{@link Payment#getDiscountDays() <em>discountDays</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDiscountDays
     *            the new value of the '{@link Payment#getDiscountDays()
     *            discountDays}' feature.
     * @generated
     */
    public void setDiscountDays(Integer newDiscountDays) {
        discountDays = newDiscountDays;
        firePropertyChange("discountDays", this.discountDays, newDiscountDays);
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
     * Sets the '{@link Payment#getName() <em>name</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newName
     *            the new value of the '{@link Payment#getName() name}' feature.
     * @generated
     */
    public void setName(String newName) {
        name = newName;
        firePropertyChange("name", this.name, newName);
    }

    /**
     * Returns the value of '<em><b>discountValue</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>discountValue</b></em>' feature
     * @generated
     */
    public Double getDiscountValue() {
        return discountValue;
    }

    /**
     * Sets the '{@link Payment#getDiscountValue() <em>discountValue</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDiscountValue
     *            the new value of the '{@link Payment#getDiscountValue()
     *            discountValue}' feature.
     * @generated
     */
    public void setDiscountValue(Double newDiscountValue) {
        discountValue = newDiscountValue;
        firePropertyChange("discountValue", this.discountValue, newDiscountValue);
    }

    /**
     * Returns the value of '<em><b>netDays</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>netDays</b></em>' feature
     * @generated
     */
    public Integer getNetDays() {
        return netDays;
    }

    /**
     * Sets the '{@link Payment#getNetDays() <em>netDays</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newNetDays
     *            the new value of the '{@link Payment#getNetDays() netDays}'
     *            feature.
     * @generated
     */
    public void setNetDays(Integer newNetDays) {
        netDays = newNetDays;
        firePropertyChange("netDays", this.netDays, newNetDays);
    }

    /**
     * Returns the value of '<em><b>categories</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>categories</b></em>' feature
     * @generated
     */
    public List<PaymentCategory> getCategories() {
        return categories;
    }

    /**
     * Adds to the <em>categories</em> feature.
     *
     * @param categoriesValue
     *            value to add
     *
     * @generated
     */
    public boolean addToCategories(PaymentCategory categoriesValue) {
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
    public boolean removeFromCategories(PaymentCategory categoriesValue) {
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
     * Sets the '{@link Payment#getCategories() <em>categories</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newCategories
     *            the new value of the '{@link Payment#getCategories()
     *            categories}' feature.
     * @generated
     */
    public void setCategories(List<PaymentCategory> newCategories) {
        categories = newCategories;
        firePropertyChange("categories", this.categories, newCategories);
    }

    /**
     * Returns the value of '<em><b>paidText</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>paidText</b></em>' feature
     * @generated
     */
    public String getPaidText() {
        return paidText;
    }

    /**
     * Sets the '{@link Payment#getPaidText() <em>paidText</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPaidText
     *            the new value of the '{@link Payment#getPaidText() paidText}'
     *            feature.
     * @generated
     */
    public void setPaidText(String newPaidText) {
        paidText = newPaidText;
        firePropertyChange("paidText", this.paidText, newPaidText);
    }

    /**
     * Returns the value of '<em><b>unpaidText</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>unpaidText</b></em>' feature
     * @generated
     */
    public String getUnpaidText() {
        return unpaidText;
    }

    /**
     * Sets the '{@link Payment#getUnpaidText() <em>unpaidText</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newUnpaidText
     *            the new value of the '{@link Payment#getUnpaidText()
     *            unpaidText}' feature.
     * @generated
     */
    public void setUnpaidText(String newUnpaidText) {
        unpaidText = newUnpaidText;
        firePropertyChange("unpaidText", this.unpaidText, newUnpaidText);
    }

    /**
     * A toString method which prints the values of all EAttributes of this
     * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        return "Payment " + " [id: " + getId() + "]" + " [defaultPaid: " + getDefaultPaid() + "]" + " [deleted: " + getDeleted() + "]" + " [description: "
                + getDescription() + "]" + " [discountDays: " + getDiscountDays() + "]" + " [name: " + getName() + "]" + " [discountValue: "
                + getDiscountValue() + "]" + " [netDays: " + getNetDays() + "]" + " [paidText: " + getPaidText() + "]" + " [unpaidText: " + getUnpaidText()
                + "]";
    }
}
