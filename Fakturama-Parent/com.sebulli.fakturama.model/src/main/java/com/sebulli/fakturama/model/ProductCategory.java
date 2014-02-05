package com.sebulli.fakturama.model;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.Table;

/**
 * A representation of the model object '<em><b>ProductCategory</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_PRODUCTCATEGORY")
@PrimaryKeyJoinColumns({ @PrimaryKeyJoinColumn(name = "PRODUCTCATEGORY_PARENT_ID") })
public class ProductCategory extends AbstractCategory {
	/**
	 * @generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "ProductCategory ";
	}
}
