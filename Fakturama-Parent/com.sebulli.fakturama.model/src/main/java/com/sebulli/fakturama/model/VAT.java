package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.util.Date;

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
public class VAT extends ModelObject implements IEntity, Serializable {
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
    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinColumns({ @JoinColumn(name = "FK_VAT") })
    private VATCategory category = null;

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
        firePropertyChange("name", this.name, newName);
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
	    firePropertyChange("deleted", this.deleted, newDeleted);
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
        firePropertyChange("description", this.description, newDescription);
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
	    firePropertyChange("taxValue", this.taxValue, newTaxValue); 
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
	    firePropertyChange("modified", this.modified, newModified);
		modified = newModified;
	}

	/**
     * Returns the value of '<em><b>category</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>category</b></em>' feature
     * @generated
     */
    public VATCategory getCategory() {
        return category;
    }

    /**
     * Sets the '{@link VAT#getCategory() <em>category</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newCategory
     *            the new value of the '{@link VAT#getCategory() category}'
     *            feature.
     * @generated
     */
    public void setCategory(VATCategory newCategory) {
        category = newCategory;
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

	@Override
	public int getTransactionKey() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAddressKey() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getFormatedStringValueByKey(String key) {
		// TODO Auto-generated method stub
		return null;
	}
}
