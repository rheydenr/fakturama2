package com.sebulli.fakturama.model;

/**
 * A representation of the model object '<em><b>IEntity</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public interface IEntity {
    /**
     * Returns the value of '<em><b>name</em></b>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>name</b></em>' feature
     * @generated
     */
    public String getName();

    /**
     * Sets the '{@link IEntity#getName() <em>name</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newName
     *            the new value of the '{@link IEntity#getName() <em>name</em>}'
     *            feature.
     * @generated
     */
    public void setName(String newName);

    /**
     * Returns the value of '<em><b>id</em></b>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>id</b></em>' feature
     * @generated
     */
    public long getId();

    /**
     * Sets the '{@link IEntity#getId() <em>id</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newId
     *            the new value of the '{@link IEntity#getId() <em>id</em>}'
     *            feature.
     * @generated
     */
    public void setId(long newId);

    /**
     * Returns the value of '<em><b>deleted</em></b>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>deleted</b></em>' feature
     * @generated
     */
    public Boolean getDeleted();

    /**
     * Sets the '{@link IEntity#getDeleted() <em>deleted</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDeleted
     *            the new value of the '{@link IEntity#getDeleted()
     *            <em>deleted</em>}' feature.
     * @generated
     */
    public void setDeleted(Boolean newDeleted);

}
