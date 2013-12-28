package com.sebulli.fakturama.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * A representation of the model object '<em><b>BillingType</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> Can be one
 * of the following values (numbers in parentheses are the ooriginal numbers
 * from Fakturama 1.6.3): - LETTER (1) - OFFER (2) - ORDER (3): - CONFIRMATION
 * (4) - INVOICE (5) - DELIVERY (6) - CREDIT (7) - DUNNING (8) - PROFORMA (9)
 * <!-- end-model-doc -->
 * 
 * @generated
 */
@Entity
public class BillingType extends AbstractCategory {
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
	 * Sets the '{@link BillingType#getType() <em>type</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newType
	 *            the new value of the '{@link BillingType#getType() type}'
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
		return "BillingType " + " [type: " + getType() + "]";
	}
}
