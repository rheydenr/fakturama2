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
 * A representation of the model object '<em><b>UserProperty</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_USERPROPERTY")
public class UserProperty implements Serializable {
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
	@Column(name = "T_VALUE")
	private String value = null;

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
	 * Sets the '{@link UserProperty#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link UserProperty#getId() id}'
	 *            feature.
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
	 * Sets the '{@link UserProperty#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link UserProperty#getName() name}'
	 *            feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Returns the value of '<em><b>value</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>value</b></em>' feature
	 * @generated
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the '{@link UserProperty#getValue() <em>value</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newValue
	 *            the new value of the '{@link UserProperty#getValue() value}'
	 *            feature.
	 * @generated
	 */
	public void setValue(String newValue) {
		value = newValue;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "UserProperty " + " [id: " + getId() + "]" + " [name: "
				+ getName() + "]" + " [value: " + getValue() + "]";
	}
}
