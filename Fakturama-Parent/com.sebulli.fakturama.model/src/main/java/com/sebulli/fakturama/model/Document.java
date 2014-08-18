package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A representation of the model object '<em><b>Document</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
 * 
 * <!-- end-model-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_DOCUMENT")
public class Document extends ModelObject implements IEntity, Serializable {
    /**
     * A common serial ID.
     * 
     * @generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> A
     * manually edited address (which doesn't point to an existing Address
     * entry). <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "ADDRESSMANUAL")
    private String addressManual = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * Reference to the contact which this document is for. <!-- end-model-doc
     * -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumns({ @JoinColumn(name = "DOCUMENT_CONTACT") })
    private Contact contact = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "ADDRESSFIRSTLINE")
    private String addressFirstLine = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "CUSTOMERREF")
    private String customerRef = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "CREATIONDATE")
    @Temporal(TemporalType.DATE)
    private Date creationDate = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(joinColumns = { @JoinColumn(name = "DOCUMENT_ITEMS") }, inverseJoinColumns = { @JoinColumn(name = "ITEMS_DOCUMENTITEM") }, name = "FKT_DOCUMENT_ITEMS")
    private List<DocumentItem> items = new ArrayList<DocumentItem>();

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * 
     * <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "BILLINGTYPE")
    @Enumerated(EnumType.STRING)
    private BillingType billingType = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DUEDAYS")
    private Integer dueDays = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DUNNINGLEVEL")
    private Integer dunningLevel = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * This is the reference to the document from which this document was
     * created <!-- end-model-doc -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumns({ @JoinColumn(name = "DOCUMENT_SOURCEDOCUMENT") })
    private Document sourceDocument = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * this is formerly known as "itemsdiscount" but was changed because of
     * ambiguity to cash discount <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "ITEMSREBATE")
    private Double itemsRebate = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "MESSAGE")
    private String message = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "MESSAGE2")
    private String message2 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "MESSAGE3")
    private String message3 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "ODTPATH")
    private String odtPath = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PDFPATH")
    private String pdfPath = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "ORDERDATE")
    @Temporal(TemporalType.DATE)
    private Date orderDate = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the amount of what is payed (relevant for partial payment) <!--
     * end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PAYEDVALUE")
    private BigDecimal payedValue = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the date when the invoice (or dunning or whatever) is completely payed.
     * <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PAYDATE")
    @Temporal(TemporalType.DATE)
    private Date payDate = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumns({ @JoinColumn(name = "FK_PAYMENT") })
    private Payment payment = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * contains all manually edited data for this document in a well defined XML
     * structure <!-- end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DATACONTAINER")
    private String dataContainer = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PRINTTEMPLATE")
    private String printTemplate = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PROGRESS")
    private Integer progress = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "SERVICEDATE")
    @Temporal(TemporalType.DATE)
    private Date serviceDate = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumns({ @JoinColumn(name = "FK_SHIPPING") })
    private Shipping shipping = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "TOTALVALUE", precision = 12, scale = 3)
    private BigDecimal totalValue = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * This id is for grouping of multiple docuements that belongs to the same
     * transaction. It is a unique ID which determines the transaction. <!--
     * end-model-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "TRANSACTIONID")
    private Long transactionId = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "WEBSHOPDATE")
    @Temporal(TemporalType.DATE)
    private Date webshopDate = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "WEBSHOPID")
    private String webshopId = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "CONSULTANT")
    private String consultant = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> If
     * all the document is completely VAT free then use this entry to point to a
     * zero valued VAT entry. All items in the editor will be displayed with
     * zero tax. <!-- end-model-doc -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumns({ @JoinColumn(name = "FK_VAT") })
    private VAT noVatReference = null;

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
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = 0;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DELETED")
    private Boolean deleted = null;

    /**
     * Returns the value of '<em><b>addressManual</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> A
     * manually edited address (which doesn't point to an existing Address
     * entry). <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>addressManual</b></em>' feature
     * @generated
     */
    public String getAddressManual() {
        return addressManual;
    }

    /**
     * Sets the '{@link Document#getAddressManual() <em>addressManual</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> A
     * manually edited address (which doesn't point to an existing Address
     * entry). <!-- end-model-doc -->
     * 
     * @param newAddressManual
     *            the new value of the '{@link Document#getAddressManual()
     *            addressManual}' feature.
     * @generated
     */
    public void setAddressManual(String newAddressManual) {
        addressManual = newAddressManual;
        firePropertyChange("addressManual", this.addressManual, newAddressManual);
    }

    /**
     * Returns the value of '<em><b>contact</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * Reference to the contact which this document is for. <!-- end-model-doc
     * -->
     * 
     * @return the value of '<em><b>contact</b></em>' feature
     * @generated
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Sets the '{@link Document#getContact() <em>contact</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * Reference to the contact which this document is for. <!-- end-model-doc
     * -->
     * 
     * @param newContact
     *            the new value of the '{@link Document#getContact() contact}'
     *            feature.
     * @generated
     */
    public void setContact(Contact newContact) {
        contact = newContact;
        firePropertyChange("contact", this.contact, newContact);
    }

    /**
     * Returns the value of '<em><b>addressFirstLine</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>addressFirstLine</b></em>' feature
     * @generated
     */
    public String getAddressFirstLine() {
        return addressFirstLine;
    }

    /**
     * Sets the '{@link Document#getAddressFirstLine()
     * <em>addressFirstLine</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newAddressFirstLine
     *            the new value of the '{@link Document#getAddressFirstLine()
     *            addressFirstLine}' feature.
     * @generated
     */
    public void setAddressFirstLine(String newAddressFirstLine) {
        addressFirstLine = newAddressFirstLine;
        firePropertyChange("addressFirstLine", this.addressFirstLine, newAddressFirstLine);
    }

    /**
     * Returns the value of '<em><b>customerRef</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>customerRef</b></em>' feature
     * @generated
     */
    public String getCustomerRef() {
        return customerRef;
    }

    /**
     * Sets the '{@link Document#getCustomerRef() <em>customerRef</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newCustomerRef
     *            the new value of the '{@link Document#getCustomerRef()
     *            customerRef}' feature.
     * @generated
     */
    public void setCustomerRef(String newCustomerRef) {
        customerRef = newCustomerRef;
        firePropertyChange("customerRef", this.customerRef, newCustomerRef);
    }

    /**
     * Returns the value of '<em><b>creationDate</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>creationDate</b></em>' feature
     * @generated
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the '{@link Document#getCreationDate() <em>creationDate</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newCreationDate
     *            the new value of the '{@link Document#getCreationDate()
     *            creationDate}' feature.
     * @generated
     */
    public void setCreationDate(Date newCreationDate) {
        creationDate = newCreationDate;
        firePropertyChange("creationDate", this.creationDate, newCreationDate);
    }

    /**
     * Returns the value of '<em><b>items</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>items</b></em>' feature
     * @generated
     */
    public List<DocumentItem> getItems() {
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
    public boolean addToItems(DocumentItem itemsValue) {
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
    public boolean removeFromItems(DocumentItem itemsValue) {
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
     * Sets the '{@link Document#getItems() <em>items</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newItems
     *            the new value of the '{@link Document#getItems() items}'
     *            feature.
     * @generated
     */
    public void setItems(List<DocumentItem> newItems) {
        items = newItems;
        firePropertyChange("items", this.items, newItems);
    }

    /**
     * Returns the value of '<em><b>billingType</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * 
     * <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>billingType</b></em>' feature
     * @generated
     */
    public BillingType getBillingType() {
        return billingType;
    }

    /**
     * Sets the '{@link Document#getBillingType() <em>billingType</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * 
     * <!-- end-model-doc -->
     * 
     * @param newBillingType
     *            the new value of the '{@link Document#getBillingType()
     *            billingType}' feature.
     * @generated
     */
    public void setBillingType(BillingType newBillingType) {
        billingType = newBillingType;
        firePropertyChange("billingType", this.billingType, newBillingType);
    }

    /**
     * Returns the value of '<em><b>dueDays</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>dueDays</b></em>' feature
     * @generated
     */
    public Integer getDueDays() {
        return dueDays;
    }

    /**
     * Sets the '{@link Document#getDueDays() <em>dueDays</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDueDays
     *            the new value of the '{@link Document#getDueDays() dueDays}'
     *            feature.
     * @generated
     */
    public void setDueDays(Integer newDueDays) {
        dueDays = newDueDays;
        firePropertyChange("dueDays", this.dueDays, newDueDays);
    }

    /**
     * Returns the value of '<em><b>dunningLevel</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>dunningLevel</b></em>' feature
     * @generated
     */
    public Integer getDunningLevel() {
        return dunningLevel;
    }

    /**
     * Sets the '{@link Document#getDunningLevel() <em>dunningLevel</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDunningLevel
     *            the new value of the '{@link Document#getDunningLevel()
     *            dunningLevel}' feature.
     * @generated
     */
    public void setDunningLevel(Integer newDunningLevel) {
        dunningLevel = newDunningLevel;
        firePropertyChange("dunningLevel", this.dunningLevel, newDunningLevel);
    }

    /**
     * Returns the value of '<em><b>sourceDocument</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * This is the reference to the document from which this document was
     * created <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>sourceDocument</b></em>' feature
     * @generated
     */
    public Document getSourceDocument() {
        return sourceDocument;
    }

    /**
     * Sets the '{@link Document#getSourceDocument() <em>sourceDocument</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * This is the reference to the document from which this document was
     * created <!-- end-model-doc -->
     * 
     * @param newSourceDocument
     *            the new value of the '{@link Document#getSourceDocument()
     *            sourceDocument}' feature.
     * @generated
     */
    public void setSourceDocument(Document newSourceDocument) {
        sourceDocument = newSourceDocument;
        firePropertyChange("sourceDocument", this.sourceDocument, newSourceDocument);
    }

    /**
     * Returns the value of '<em><b>itemsRebate</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * this is formerly known as "itemsdiscount" but was changed because of
     * ambiguity to cash discount <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>itemsRebate</b></em>' feature
     * @generated
     */
    public Double getItemsRebate() {
        return itemsRebate;
    }

    /**
     * Sets the '{@link Document#getItemsRebate() <em>itemsRebate</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * this is formerly known as "itemsdiscount" but was changed because of
     * ambiguity to cash discount <!-- end-model-doc -->
     * 
     * @param newItemsRebate
     *            the new value of the '{@link Document#getItemsRebate()
     *            itemsRebate}' feature.
     * @generated
     */
    public void setItemsRebate(Double newItemsRebate) {
        itemsRebate = newItemsRebate;
        firePropertyChange("itemsRebate", this.itemsRebate, newItemsRebate);
    }

    /**
     * Returns the value of '<em><b>message</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>message</b></em>' feature
     * @generated
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the '{@link Document#getMessage() <em>message</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newMessage
     *            the new value of the '{@link Document#getMessage() message}'
     *            feature.
     * @generated
     */
    public void setMessage(String newMessage) {
        message = newMessage;
        firePropertyChange("message", this.message, newMessage);
    }

    /**
     * Returns the value of '<em><b>message2</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>message2</b></em>' feature
     * @generated
     */
    public String getMessage2() {
        return message2;
    }

    /**
     * Sets the '{@link Document#getMessage2() <em>message2</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newMessage2
     *            the new value of the '{@link Document#getMessage2() message2}'
     *            feature.
     * @generated
     */
    public void setMessage2(String newMessage2) {
        message2 = newMessage2;
        firePropertyChange("message2", this.message2, newMessage2);
    }

    /**
     * Returns the value of '<em><b>message3</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>message3</b></em>' feature
     * @generated
     */
    public String getMessage3() {
        return message3;
    }

    /**
     * Sets the '{@link Document#getMessage3() <em>message3</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newMessage3
     *            the new value of the '{@link Document#getMessage3() message3}'
     *            feature.
     * @generated
     */
    public void setMessage3(String newMessage3) {
        message3 = newMessage3;
        firePropertyChange("message3", this.message3, newMessage3);
    }

    /**
     * Returns the value of '<em><b>odtPath</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>odtPath</b></em>' feature
     * @generated
     */
    public String getOdtPath() {
        return odtPath;
    }

    /**
     * Sets the '{@link Document#getOdtPath() <em>odtPath</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newOdtPath
     *            the new value of the '{@link Document#getOdtPath() odtPath}'
     *            feature.
     * @generated
     */
    public void setOdtPath(String newOdtPath) {
        odtPath = newOdtPath;
        firePropertyChange("odtPath", this.odtPath, newOdtPath);
    }

    /**
     * Returns the value of '<em><b>pdfPath</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>pdfPath</b></em>' feature
     * @generated
     */
    public String getPdfPath() {
        return pdfPath;
    }

    /**
     * Sets the '{@link Document#getPdfPath() <em>pdfPath</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPdfPath
     *            the new value of the '{@link Document#getPdfPath() pdfPath}'
     *            feature.
     * @generated
     */
    public void setPdfPath(String newPdfPath) {
        pdfPath = newPdfPath;
        firePropertyChange("pdfPath", this.pdfPath, newPdfPath);
    }

    /**
     * Returns the value of '<em><b>orderDate</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>orderDate</b></em>' feature
     * @generated
     */
    public Date getOrderDate() {
        return orderDate;
    }

    /**
     * Sets the '{@link Document#getOrderDate() <em>orderDate</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newOrderDate
     *            the new value of the '{@link Document#getOrderDate()
     *            orderDate}' feature.
     * @generated
     */
    public void setOrderDate(Date newOrderDate) {
        orderDate = newOrderDate;
        firePropertyChange("orderDate", this.orderDate, newOrderDate);
    }

    /**
     * Returns the value of '<em><b>payedValue</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the amount of what is payed (relevant for partial payment) <!--
     * end-model-doc -->
     * 
     * @return the value of '<em><b>payedValue</b></em>' feature
     * @generated
     */
    public BigDecimal getPayedValue() {
        return payedValue;
    }

    /**
     * Sets the '{@link Document#getPayedValue() <em>payedValue</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the amount of what is payed (relevant for partial payment) <!--
     * end-model-doc -->
     * 
     * @param newPayedValue
     *            the new value of the '{@link Document#getPayedValue()
     *            payedValue}' feature.
     * @generated
     */
    public void setPayedValue(BigDecimal newPayedValue) {
        payedValue = newPayedValue;
        firePropertyChange("payedValue", this.payedValue, newPayedValue);
    }

    /**
     * Returns the value of '<em><b>payDate</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the date when the invoice (or dunning or whatever) is completely payed.
     * <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>payDate</b></em>' feature
     * @generated
     */
    public Date getPayDate() {
        return payDate;
    }

    /**
     * Sets the '{@link Document#getPayDate() <em>payDate</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * the date when the invoice (or dunning or whatever) is completely payed.
     * <!-- end-model-doc -->
     * 
     * @param newPayDate
     *            the new value of the '{@link Document#getPayDate() payDate}'
     *            feature.
     * @generated
     */
    public void setPayDate(Date newPayDate) {
        payDate = newPayDate;
        firePropertyChange("payDate", this.payDate, newPayDate);
    }

    /**
     * Returns the value of '<em><b>payment</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>payment</b></em>' feature
     * @generated
     */
    public Payment getPayment() {
        return payment;
    }

    /**
     * Sets the '{@link Document#getPayment() <em>payment</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPayment
     *            the new value of the '{@link Document#getPayment() payment}'
     *            feature.
     * @generated
     */
    public void setPayment(Payment newPayment) {
        payment = newPayment;
        firePropertyChange("payment", this.payment, newPayment);
    }

    /**
     * Returns the value of '<em><b>dataContainer</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * contains all manually edited data for this document in a well defined XML
     * structure <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>dataContainer</b></em>' feature
     * @generated
     */
    public String getDataContainer() {
        return dataContainer;
    }

    /**
     * Sets the '{@link Document#getDataContainer() <em>dataContainer</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * contains all manually edited data for this document in a well defined XML
     * structure <!-- end-model-doc -->
     * 
     * @param newDataContainer
     *            the new value of the '{@link Document#getDataContainer()
     *            dataContainer}' feature.
     * @generated
     */
    public void setDataContainer(String newDataContainer) {
        dataContainer = newDataContainer;
        firePropertyChange("dataContainer", this.dataContainer, newDataContainer);
    }

    /**
     * Returns the value of '<em><b>printTemplate</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>printTemplate</b></em>' feature
     * @generated
     */
    public String getPrintTemplate() {
        return printTemplate;
    }

    /**
     * Sets the '{@link Document#getPrintTemplate() <em>printTemplate</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPrintTemplate
     *            the new value of the '{@link Document#getPrintTemplate()
     *            printTemplate}' feature.
     * @generated
     */
    public void setPrintTemplate(String newPrintTemplate) {
        printTemplate = newPrintTemplate;
        firePropertyChange("printTemplate", this.printTemplate, newPrintTemplate);
    }

    /**
     * Returns the value of '<em><b>progress</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>progress</b></em>' feature
     * @generated
     */
    public Integer getProgress() {
        return progress;
    }

    /**
     * Sets the '{@link Document#getProgress() <em>progress</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newProgress
     *            the new value of the '{@link Document#getProgress() progress}'
     *            feature.
     * @generated
     */
    public void setProgress(Integer newProgress) {
        progress = newProgress;
        firePropertyChange("progress", this.progress, newProgress);
    }

    /**
     * Returns the value of '<em><b>serviceDate</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>serviceDate</b></em>' feature
     * @generated
     */
    public Date getServiceDate() {
        return serviceDate;
    }

    /**
     * Sets the '{@link Document#getServiceDate() <em>serviceDate</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newServiceDate
     *            the new value of the '{@link Document#getServiceDate()
     *            serviceDate}' feature.
     * @generated
     */
    public void setServiceDate(Date newServiceDate) {
        serviceDate = newServiceDate;
        firePropertyChange("serviceDate", this.serviceDate, newServiceDate);
    }

    /**
     * Returns the value of '<em><b>shipping</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>shipping</b></em>' feature
     * @generated
     */
    public Shipping getShipping() {
        return shipping;
    }

    /**
     * Sets the '{@link Document#getShipping() <em>shipping</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newShipping
     *            the new value of the '{@link Document#getShipping() shipping}'
     *            feature.
     * @generated
     */
    public void setShipping(Shipping newShipping) {
        shipping = newShipping;
        firePropertyChange("shipping", this.shipping, newShipping);
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
     * Sets the '{@link Document#getTotalValue() <em>totalValue</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newTotalValue
     *            the new value of the '{@link Document#getTotalValue()
     *            totalValue}' feature.
     * @generated
     */
    public void setTotalValue(BigDecimal newTotalValue) {
        totalValue = newTotalValue;
        firePropertyChange("totalValue", this.totalValue, newTotalValue);
    }

    /**
     * Returns the value of '<em><b>transactionId</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * This id is for grouping of multiple docuements that belongs to the same
     * transaction. It is a unique ID which determines the transaction. <!--
     * end-model-doc -->
     * 
     * @return the value of '<em><b>transactionId</b></em>' feature
     * @generated
     */
    public Long getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the '{@link Document#getTransactionId() <em>transactionId</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
     * This id is for grouping of multiple docuements that belongs to the same
     * transaction. It is a unique ID which determines the transaction. <!--
     * end-model-doc -->
     * 
     * @param newTransactionId
     *            the new value of the '{@link Document#getTransactionId()
     *            transactionId}' feature.
     * @generated
     */
    public void setTransactionId(Long newTransactionId) {
        transactionId = newTransactionId;
        firePropertyChange("transactionId", this.transactionId, newTransactionId);
    }

    /**
     * Returns the value of '<em><b>webshopDate</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>webshopDate</b></em>' feature
     * @generated
     */
    public Date getWebshopDate() {
        return webshopDate;
    }

    /**
     * Sets the '{@link Document#getWebshopDate() <em>webshopDate</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newWebshopDate
     *            the new value of the '{@link Document#getWebshopDate()
     *            webshopDate}' feature.
     * @generated
     */
    public void setWebshopDate(Date newWebshopDate) {
        webshopDate = newWebshopDate;
        firePropertyChange("webshopDate", this.webshopDate, newWebshopDate);
    }

    /**
     * Returns the value of '<em><b>webshopId</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>webshopId</b></em>' feature
     * @generated
     */
    public String getWebshopId() {
        return webshopId;
    }

    /**
     * Sets the '{@link Document#getWebshopId() <em>webshopId</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newWebshopId
     *            the new value of the '{@link Document#getWebshopId()
     *            webshopId}' feature.
     * @generated
     */
    public void setWebshopId(String newWebshopId) {
        webshopId = newWebshopId;
        firePropertyChange("webshopId", this.webshopId, newWebshopId);
    }

    /**
     * Returns the value of '<em><b>consultant</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>consultant</b></em>' feature
     * @generated
     */
    public String getConsultant() {
        return consultant;
    }

    /**
     * Sets the '{@link Document#getConsultant() <em>consultant</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newConsultant
     *            the new value of the '{@link Document#getConsultant()
     *            consultant}' feature.
     * @generated
     */
    public void setConsultant(String newConsultant) {
        consultant = newConsultant;
        firePropertyChange("consultant", this.consultant, newConsultant);
    }

    /**
     * Returns the value of '<em><b>noVatReference</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> If
     * all the document is completely VAT free then use this entry to point to a
     * zero valued VAT entry. All items in the editor will be displayed with
     * zero tax. <!-- end-model-doc -->
     * 
     * @return the value of '<em><b>noVatReference</b></em>' feature
     * @generated
     */
    public VAT getNoVatReference() {
        return noVatReference;
    }

    /**
     * Sets the '{@link Document#getNoVatReference() <em>noVatReference</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> If
     * all the document is completely VAT free then use this entry to point to a
     * zero valued VAT entry. All items in the editor will be displayed with
     * zero tax. <!-- end-model-doc -->
     * 
     * @param newNoVatReference
     *            the new value of the '{@link Document#getNoVatReference()
     *            noVatReference}' feature.
     * @generated
     */
    public void setNoVatReference(VAT newNoVatReference) {
        noVatReference = newNoVatReference;
        firePropertyChange("noVatReference", this.noVatReference, newNoVatReference);
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
     * Sets the '{@link Document#getName() <em>name</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newName
     *            the new value of the '{@link Document#getName() name}'
     *            feature.
     * @generated
     */
    public void setName(String newName) {
        name = newName;
        firePropertyChange("name", this.name, newName);
    }

    /**
     * Returns the value of '<em><b>id</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>id</b></em>' feature
     * @generated
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the '{@link Document#getId() <em>id</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newId
     *            the new value of the '{@link Document#getId() id}' feature.
     * @generated
     */
    public void setId(long newId) {
        id = newId;
        firePropertyChange("id", this.id, newId);
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
     * Sets the '{@link Document#getDeleted() <em>deleted</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDeleted
     *            the new value of the '{@link Document#getDeleted() deleted}'
     *            feature.
     * @generated
     */
    public void setDeleted(Boolean newDeleted) {
        deleted = newDeleted;
        firePropertyChange("deleted", this.deleted, newDeleted);
    }

    /**
     * A toString method which prints the values of all EAttributes of this
     * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        return "Document " + " [addressManual: " + getAddressManual() + "]" + " [addressFirstLine: " + getAddressFirstLine() + "]" + " [customerRef: "
                + getCustomerRef() + "]" + " [creationDate: " + getCreationDate() + "]" + " [billingType: " + getBillingType() + "]" + " [dueDays: "
                + getDueDays() + "]" + " [dunningLevel: " + getDunningLevel() + "]" + " [itemsRebate: " + getItemsRebate() + "]" + " [message: " + getMessage()
                + "]" + " [message2: " + getMessage2() + "]" + " [message3: " + getMessage3() + "]" + " [odtPath: " + getOdtPath() + "]" + " [pdfPath: "
                + getPdfPath() + "]" + " [orderDate: " + getOrderDate() + "]" + " [payedValue: " + getPayedValue() + "]" + " [payDate: " + getPayDate() + "]"
                + " [dataContainer: " + getDataContainer() + "]" + " [printTemplate: " + getPrintTemplate() + "]" + " [progress: " + getProgress() + "]"
                + " [serviceDate: " + getServiceDate() + "]" + " [totalValue: " + getTotalValue() + "]" + " [transactionId: " + getTransactionId() + "]"
                + " [webshopDate: " + getWebshopDate() + "]" + " [webshopId: " + getWebshopId() + "]" + " [consultant: " + getConsultant() + "]" + " [name: "
                + getName() + "]" + " [id: " + getId() + "]" + " [deleted: " + getDeleted() + "]";
    }
}
