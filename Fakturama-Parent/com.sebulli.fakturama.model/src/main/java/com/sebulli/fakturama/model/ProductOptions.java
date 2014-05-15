package com.sebulli.fakturama.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A representation of the model object '<em><b>ProductOptions</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_PRODUCTOPTIONS")
public class ProductOptions implements Serializable {

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
    @Column(name = "T_ATTRIBUTE")
    private String attribute = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "ATTRIBUTEVALUE")
    private String attributeValue = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "SEQUENCENUMBER")
    private Integer sequenceNumber = null;

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
     * Sets the '{@link ProductOptions#getId() <em>id</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newId
     *            the new value of the '{@link ProductOptions#getId() id}'
     *            feature.
     * @generated
     */
    public void setId(Long newId) {
        id = newId;
    }

    /**
     * Returns the value of '<em><b>attribute</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>attribute</b></em>' feature
     * @generated
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Sets the '{@link ProductOptions#getAttribute() <em>attribute</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newAttribute
     *            the new value of the '{@link ProductOptions#getAttribute()
     *            attribute}' feature.
     * @generated
     */
    public void setAttribute(String newAttribute) {
        attribute = newAttribute;
    }

    /**
     * Returns the value of '<em><b>attributeValue</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>attributeValue</b></em>' feature
     * @generated
     */
    public String getAttributeValue() {
        return attributeValue;
    }

    /**
     * Sets the '{@link ProductOptions#getAttributeValue()
     * <em>attributeValue</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newAttributeValue
     *            the new value of the '
     *            {@link ProductOptions#getAttributeValue() attributeValue}'
     *            feature.
     * @generated
     */
    public void setAttributeValue(String newAttributeValue) {
        attributeValue = newAttributeValue;
    }

    /**
     * Returns the value of '<em><b>sequenceNumber</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>sequenceNumber</b></em>' feature
     * @generated
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the '{@link ProductOptions#getSequenceNumber()
     * <em>sequenceNumber</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newSequenceNumber
     *            the new value of the '
     *            {@link ProductOptions#getSequenceNumber() sequenceNumber}'
     *            feature.
     * @generated
     */
    public void setSequenceNumber(Integer newSequenceNumber) {
        sequenceNumber = newSequenceNumber;
    }

    /**
     * A toString method which prints the values of all EAttributes of this
     * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        return "ProductOptions " + " [id: " + getId() + "]" + " [attribute: " + getAttribute() + "]" + " [attributeValue: " + getAttributeValue() + "]"
                + " [sequenceNumber: " + getSequenceNumber() + "]";
    }
}
