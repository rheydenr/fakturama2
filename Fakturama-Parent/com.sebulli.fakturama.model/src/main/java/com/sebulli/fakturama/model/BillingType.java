package com.sebulli.fakturama.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A representation of the literals of the enumeration '
 * <em><b>BillingType</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
 * <!-- begin-model-doc --> Can be one of the following values (numbers in
 * parentheses are the original numbers from Fakturama 1.6.3):
 * <ul>
 * <li>LETTER (1)
 * <li>OFFER (2)
 * <li>ORDER (3):
 * <li>CONFIRMATION (4)
 * <li>INVOICE (5)
 * <li>DELIVERY (6)
 * <li>CREDIT (7)
 * <li>DUNNING (8)
 * <li>PROFORMA (9)
 * </ul>
 * <!-- end-model-doc -->
 * 
 * @generated
 */
public enum BillingType {

	/**
	 * The enum: INVOICE <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	INVOICE(5, "INVOICE", "INVOICE") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isINVOICE() {
			return true;
		}
	},
	/**
	 * The enum: LETTER <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	LETTER(1, "LETTER", "LETTER") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isLETTER() {
			return true;
		}
	},
	/**
	 * The enum: OFFER <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	OFFER(2, "OFFER", "OFFER") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isOFFER() {
			return true;
		}
	},
	/**
	 * The enum: ORDER <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	ORDER(3, "ORDER", "ORDER") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isORDER() {
			return true;
		}
	},
	/**
	 * The enum: CONFIRMATION <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	CONFIRMATION(4, "CONFIRMATION", "CONFIRMATION") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isCONFIRMATION() {
			return true;
		}
	},
	/**
	 * The enum: DELIVERY <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	DELIVERY(6, "DELIVERY", "DELIVERY") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isDELIVERY() {
			return true;
		}
	},
	/**
	 * The enum: CREDIT <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	CREDIT(7, "CREDIT", "CREDIT") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isCREDIT() {
			return true;
		}
	},
	/**
	 * The enum: DUNNING <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	DUNNING(8, "DUNNING", "DUNNING") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isDUNNING() {
			return true;
		}
	},
	/**
	 * The enum: PROFORMA <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	PROFORMA(9, "PROFORMA", "PROFORMA") {

		/**
		 * @return always true for this instance
		 * @generated
		 */
		@Override
		public boolean isPROFORMA() {
			return true;
		}
	};

	/**
	 * An array of all the '<em><b>BillingType</b></em>' enumerators. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static final BillingType[] VALUES_ARRAY = new BillingType[] {
			INVOICE, LETTER, OFFER, ORDER, CONFIRMATION, DELIVERY, CREDIT,
			DUNNING, PROFORMA };

	/**
	 * A public read-only list of all the '<em><b>BillingType</b></em>'
	 * enumerators. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static final List<BillingType> VALUES = Collections
			.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>BillingType</b></em>' literal with the specified
	 * literal value. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param literal
	 *            the literal to use to get the enum instance
	 * @return the BillingType, the literal enum class
	 * @generated
	 */
	public static BillingType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			BillingType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>BillingType</b></em>' literal with the specified
	 * name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param name
	 *            the name to use to get the enum instance
	 * @return the BillingType, the literal enum class
	 * @generated
	 */
	public static BillingType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			BillingType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>BillingType</b></em>' literal with the specified
	 * integer value. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the value to use to get the enum instance
	 * @return the BillingType, the literal enum
	 * @generated
	 */
	public static BillingType get(int value) {
		for (BillingType enumInstance : VALUES_ARRAY) {
			if (enumInstance.getValue() == value) {
				return enumInstance;
			}
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	private BillingType(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isINVOICE() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isLETTER() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isOFFER() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isORDER() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isCONFIRMATION() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isDELIVERY() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isCREDIT() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isDUNNING() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return false, is overridden by actual enum types.
	 * @generated
	 */
	public boolean isPROFORMA() {
		return false;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value
	 * @generated
	 */
	public int getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the name
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the literal of this enum instance
	 * @generated
	 */
	public String getLiteral() {
		return literal;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the literal value of the enumerator, which is its string
	 *         representation.
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
}
