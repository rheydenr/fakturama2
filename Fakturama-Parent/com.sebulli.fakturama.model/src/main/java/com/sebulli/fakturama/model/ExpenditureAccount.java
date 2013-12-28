package com.sebulli.fakturama.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * A representation of the model object '<em><b>ExpenditureAccount</b></em>'.
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity
public class ExpenditureAccount extends AbstractCategory {
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
	 * Sets the '{@link ExpenditureAccount#getType() <em>type</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newType
	 *            the new value of the '{@link ExpenditureAccount#getType()
	 *            type}' feature.
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
		return "ExpenditureAccount " + " [type: " + getType() + "]";
	}
}
