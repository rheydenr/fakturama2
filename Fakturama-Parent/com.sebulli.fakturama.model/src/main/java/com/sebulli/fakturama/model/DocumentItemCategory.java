package com.sebulli.fakturama.model;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.Table;

/**
 * A representation of the model object '<em><b>DocumentItemCategory</b></em>'.
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_DOCUMENTITEMCATEGORY")
@PrimaryKeyJoinColumns({ @PrimaryKeyJoinColumn(name = "DOCUMENTITEMCATEGORY_PARENT_ID") })
public class DocumentItemCategory extends AbstractCategory {
    /**
     * A common serial ID.
     * 
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
        return "DocumentItemCategory ";
    }
}
