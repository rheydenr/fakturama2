package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A representation of the model object '<em><b>ReceiptVoucher</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_RECEIPTVOUCHER")
public class ReceiptVoucher implements Serializable {
    /**
     * A common serial ID.
     * 
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
    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinColumns({ @JoinColumn(name = "RECEIPTVOUCHER_ACCOUNT") })
    private Account account = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "RECEIPTVOUCHERDATE")
    @Temporal(TemporalType.DATE)
    private Date receiptVoucherDate = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DELETED")
    private Boolean deleted = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DISCOUNTED")
    private Boolean discounted = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The document number of this receiptVoucher. This is *NOT* a reference to
     * the Document type! <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DOCUMENTNUMBER")
    private String documentNumber = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DONOTBOOK")
    private Boolean doNotBook = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumns({ @JoinColumn(name = "RECEIPTVOUCHER_ITEMS") })
    private Set<ReceiptVoucherItem> items = new HashSet<ReceiptVoucherItem>();

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
    @Column(name = "RECEIPTVOUCHERNUMBER")
    private String receiptVoucherNumber = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PAIDVALUE")
    private BigDecimal paidValue = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "TOTALVALUE")
    private BigDecimal totalValue = null;

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
     * Sets the '{@link ReceiptVoucher#getId() <em>id</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newId
     *            the new value of the '{@link ReceiptVoucher#getId() id}'
     *            feature.
     * @generated
     */
    public void setId(Long newId) {
        id = newId;
    }

    /**
     * Returns the value of '<em><b>account</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>account</b></em>' feature
     * @generated
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getAccount() <em>account</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newAccount
     *            the new value of the '{@link ReceiptVoucher#getAccount()
     *            account}' feature.
     * @generated
     */
    public void setAccount(Account newAccount) {
        account = newAccount;
    }

    /**
     * Returns the value of '<em><b>receiptVoucherDate</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>receiptVoucherDate</b></em>' feature
     * @generated
     */
    public Date getReceiptVoucherDate() {
        return receiptVoucherDate;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getReceiptVoucherDate()
     * <em>receiptVoucherDate</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newReceiptVoucherDate
     *            the new value of the '
     *            {@link ReceiptVoucher#getReceiptVoucherDate()
     *            receiptVoucherDate}' feature.
     * @generated
     */
    public void setReceiptVoucherDate(Date newReceiptVoucherDate) {
        receiptVoucherDate = newReceiptVoucherDate;
    }

    /**
     * Returns the value of '<em><b>deleted</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>deleted</b></em>' feature
     * @generated
     */
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getDeleted() <em>deleted</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDeleted
     *            the new value of the '{@link ReceiptVoucher#getDeleted()
     *            deleted}' feature.
     * @generated
     */
    public void setDeleted(Boolean newDeleted) {
        deleted = newDeleted;
    }

    /**
     * Returns the value of '<em><b>discounted</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>discounted</b></em>' feature
     * @generated
     */
    public Boolean getDiscounted() {
        return discounted;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getDiscounted() <em>discounted</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDiscounted
     *            the new value of the '{@link ReceiptVoucher#getDiscounted()
     *            discounted}' feature.
     * @generated
     */
    public void setDiscounted(Boolean newDiscounted) {
        discounted = newDiscounted;
    }

    /**
     * Returns the value of '<em><b>documentNumber</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The document number of this receiptVoucher. This is *NOT* a reference to
     * the Document type! <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>documentNumber</b></em>' feature
     * @generated
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getDocumentNumber()
     * <em>documentNumber</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * The document number of this receiptVoucher. This is *NOT* a reference to
     * the Document type! <!-- end-model-doc -->
     * 
     * @param newDocumentNumber
     *            the new value of the '
     *            {@link ReceiptVoucher#getDocumentNumber() documentNumber}'
     *            feature.
     * @generated
     */
    public void setDocumentNumber(String newDocumentNumber) {
        documentNumber = newDocumentNumber;
    }

    /**
     * Returns the value of '<em><b>doNotBook</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>doNotBook</b></em>' feature
     * @generated
     */
    public Boolean getDoNotBook() {
        return doNotBook;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getDoNotBook() <em>doNotBook</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDoNotBook
     *            the new value of the '{@link ReceiptVoucher#getDoNotBook()
     *            doNotBook}' feature.
     * @generated
     */
    public void setDoNotBook(Boolean newDoNotBook) {
        doNotBook = newDoNotBook;
    }

    /**
     * Returns the value of '<em><b>items</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>items</b></em>' feature
     * @generated
     */
    public Set<ReceiptVoucherItem> getItems() {
        return items;
    }

    /**
     * Adds to the <em>items</em> feature.
     *
     * @param itemsValue
     *            value to add
     *
     * @generated
     */
    public boolean addToItems(ReceiptVoucherItem itemsValue) {
        if (!items.contains(itemsValue)) {
            items.add(itemsValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes from the <em>items</em> feature.
     *
     * @param itemsValue
     *            value to remove
     *
     * @generated
     */
    public boolean removeFromItems(ReceiptVoucherItem itemsValue) {
        if (items.contains(itemsValue)) {
            items.remove(itemsValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Clears the <em>items</em> feature.
     * 
     * @generated
     */
    public void clearItems() {
        while (!items.isEmpty()) {
            removeFromItems(items.iterator().next());
        }
    }

    /**
     * Sets the '{@link ReceiptVoucher#getItems() <em>items</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newItems
     *            the new value of the '{@link ReceiptVoucher#getItems() items}'
     *            feature.
     * @generated
     */
    public void setItems(Set<ReceiptVoucherItem> newItems) {
        items = newItems;
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
     * Sets the '{@link ReceiptVoucher#getName() <em>name</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newName
     *            the new value of the '{@link ReceiptVoucher#getName() name}'
     *            feature.
     * @generated
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Returns the value of '<em><b>receiptVoucherNumber</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>receiptVoucherNumber</b></em>' feature
     * @generated
     */
    public String getReceiptVoucherNumber() {
        return receiptVoucherNumber;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getReceiptVoucherNumber()
     * <em>receiptVoucherNumber</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newReceiptVoucherNumber
     *            the new value of the '
     *            {@link ReceiptVoucher#getReceiptVoucherNumber()
     *            receiptVoucherNumber}' feature.
     * @generated
     */
    public void setReceiptVoucherNumber(String newReceiptVoucherNumber) {
        receiptVoucherNumber = newReceiptVoucherNumber;
    }

    /**
     * Returns the value of '<em><b>paidValue</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>paidValue</b></em>' feature
     * @generated
     */
    public BigDecimal getPaidValue() {
        return paidValue;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getPaidValue() <em>paidValue</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPaidValue
     *            the new value of the '{@link ReceiptVoucher#getPaidValue()
     *            paidValue}' feature.
     * @generated
     */
    public void setPaidValue(BigDecimal newPaidValue) {
        paidValue = newPaidValue;
    }

    /**
     * Returns the value of '<em><b>totalValue</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>totalValue</b></em>' feature
     * @generated
     */
    public BigDecimal getTotalValue() {
        return totalValue;
    }

    /**
     * Sets the '{@link ReceiptVoucher#getTotalValue() <em>totalValue</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newTotalValue
     *            the new value of the '{@link ReceiptVoucher#getTotalValue()
     *            totalValue}' feature.
     * @generated
     */
    public void setTotalValue(BigDecimal newTotalValue) {
        totalValue = newTotalValue;
    }

    /**
     * A toString method which prints the values of all EAttributes of this
     * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        return "ReceiptVoucher " + " [id: " + getId() + "]" + " [receiptVoucherDate: " + getReceiptVoucherDate() + "]" + " [deleted: " + getDeleted() + "]"
                + " [discounted: " + getDiscounted() + "]" + " [documentNumber: " + getDocumentNumber() + "]" + " [doNotBook: " + getDoNotBook() + "]"
                + " [name: " + getName() + "]" + " [receiptVoucherNumber: " + getReceiptVoucherNumber() + "]" + " [paidValue: " + getPaidValue() + "]"
                + " [totalValue: " + getTotalValue() + "]";
    }
}
