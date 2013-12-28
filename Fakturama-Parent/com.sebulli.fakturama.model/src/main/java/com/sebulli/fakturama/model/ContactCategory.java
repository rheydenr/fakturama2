package com.sebulli.fakturama.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A representation of the model object '<em><b>ContactCategory</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@DiscriminatorValue("CONTACT")
@Entity
public class ContactCategory extends AbstractCategory {
	/**
	 * @generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "T_TYPE", nullable = true)
	private String type = null;

	/**
	 * Returns the value of '<em><b>type</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>type</b></em>' feature
	 * @generated
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the '{@link ContactCategory#getType() <em>type</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newType
	 *            the new value of the '{@link ContactCategory#getType() type}'
	 *            feature.
	 * @generated
	 */
	public void setType(String newType) {
		type = newType;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "ContactCategory " + " [type: " + getType() + "]";
	}
}
