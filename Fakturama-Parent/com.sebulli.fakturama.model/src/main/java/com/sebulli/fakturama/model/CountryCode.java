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
 * A representation of the model object '<em><b>CountryCode</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity
@Table(name = "FKT_COUNTRYCODE")
public class CountryCode implements Serializable {
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
	@Column(name = "FULLNAME")
	private String fullName = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * This field contains the German translation of this country name. <!--
	 * end-model-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "FULLNAME_DE")
	private String fullName_de = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "FULLNAME_FR")
	private String fullName_fr = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "CODE3DIGIT")
	private String code3digit = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "CODE2DIGIT")
	private String code2digit = null;

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
	 * Sets the '{@link CountryCode#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link CountryCode#getId() id}' feature.
	 * @generated
	 */
	public void setId(Long newId) {
		id = newId;
	}

	/**
	 * Returns the value of '<em><b>fullName</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>fullName</b></em>' feature
	 * @generated
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Sets the '{@link CountryCode#getFullName() <em>fullName</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newFullName
	 *            the new value of the '{@link CountryCode#getFullName()
	 *            fullName}' feature.
	 * @generated
	 */
	public void setFullName(String newFullName) {
		fullName = newFullName;
	}

	/**
	 * Returns the value of '<em><b>fullName_de</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * This field contains the German translation of this country name. <!--
	 * end-model-doc -->
	 * 
	 * @return the value of '<em><b>fullName_de</b></em>' feature
	 * @generated
	 */
	public String getFullName_de() {
		return fullName_de;
	}

	/**
	 * Sets the '{@link CountryCode#getFullName_de() <em>fullName_de</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * This field contains the German translation of this country name. <!--
	 * end-model-doc -->
	 * 
	 * @param newFullName_de
	 *            the new value of the '{@link CountryCode#getFullName_de()
	 *            fullName_de}' feature.
	 * @generated
	 */
	public void setFullName_de(String newFullName_de) {
		fullName_de = newFullName_de;
	}

	/**
	 * Returns the value of '<em><b>fullName_fr</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>fullName_fr</b></em>' feature
	 * @generated
	 */
	public String getFullName_fr() {
		return fullName_fr;
	}

	/**
	 * Sets the '{@link CountryCode#getFullName_fr() <em>fullName_fr</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newFullName_fr
	 *            the new value of the '{@link CountryCode#getFullName_fr()
	 *            fullName_fr}' feature.
	 * @generated
	 */
	public void setFullName_fr(String newFullName_fr) {
		fullName_fr = newFullName_fr;
	}

	/**
	 * Returns the value of '<em><b>code3digit</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>code3digit</b></em>' feature
	 * @generated
	 */
	public String getCode3digit() {
		return code3digit;
	}

	/**
	 * Sets the '{@link CountryCode#getCode3digit() <em>code3digit</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newCode3digit
	 *            the new value of the '{@link CountryCode#getCode3digit()
	 *            code3digit}' feature.
	 * @generated
	 */
	public void setCode3digit(String newCode3digit) {
		code3digit = newCode3digit;
	}

	/**
	 * Returns the value of '<em><b>code2digit</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>code2digit</b></em>' feature
	 * @generated
	 */
	public String getCode2digit() {
		return code2digit;
	}

	/**
	 * Sets the '{@link CountryCode#getCode2digit() <em>code2digit</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newCode2digit
	 *            the new value of the '{@link CountryCode#getCode2digit()
	 *            code2digit}' feature.
	 * @generated
	 */
	public void setCode2digit(String newCode2digit) {
		code2digit = newCode2digit;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "CountryCode " + " [id: " + getId() + "]" + " [fullName: "
				+ getFullName() + "]" + " [fullName_de: " + getFullName_de()
				+ "]" + " [fullName_fr: " + getFullName_fr() + "]"
				+ " [code3digit: " + getCode3digit() + "]" + " [code2digit: "
				+ getCode2digit() + "]";
	}
}
