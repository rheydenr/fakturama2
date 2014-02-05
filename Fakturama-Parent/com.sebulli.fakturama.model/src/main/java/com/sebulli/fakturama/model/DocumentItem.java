package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.Table;

/**
 * A representation of the model object '<em><b>DocumentItem</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_DOCUMENTITEM")
public class DocumentItem implements Serializable {
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
	@Column(name = "NOVAT")
	private Boolean noVat = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "DOCUMENTITEM_CATEGORY") })
	private DocumentItemCategory category = null;

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
	@Column(name = "ITEMREBATE")
	private Double itemRebate = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "ITEMNUMBER")
	private String itemNumber = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumns({ @JoinColumn(name = "DOCUMENTITEM_PRODUCTREF") })
	private Product productref = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	@Column(name = "QUANTITY")
	private Double quantity = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "WEIGHT")
	private Double weight = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "TARA")
	private Double tara = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "DESCRIPTION")
	private String description = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "OPTIONAL")
	private Boolean optional = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "PICTURENAME")
	private String pictureName = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "PRICE")
	private BigDecimal price = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "QUANTITYUNIT")
	private String quantityUnit = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * "Shared" means if the item is shared between different documents (same
	 * item is referenced by different documents). <!-- end-model-doc -->
	 * 
	 * @generated
	 */
	@Basic()
	@Column(name = "SHARED")
	private Boolean shared = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	@JoinColumns({ @JoinColumn(name = "DOCUMENTITEM_ITEMVAT") })
	private VAT itemVat = null;

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
	 * Sets the '{@link DocumentItem#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link DocumentItem#getId() id}'
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
	 * Sets the '{@link DocumentItem#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link DocumentItem#getName() name}'
	 *            feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Returns the value of '<em><b>noVat</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>noVat</b></em>' feature
	 * @generated
	 */
	public Boolean getNoVat() {
		return noVat;
	}

	/**
	 * Sets the '{@link DocumentItem#getNoVat() <em>noVat</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newNoVat
	 *            the new value of the '{@link DocumentItem#getNoVat() noVat}'
	 *            feature.
	 * @generated
	 */
	public void setNoVat(Boolean newNoVat) {
		noVat = newNoVat;
	}

	/**
	 * Returns the value of '<em><b>category</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>category</b></em>' feature
	 * @generated
	 */
	public DocumentItemCategory getCategory() {
		return category;
	}

	/**
	 * Sets the '{@link DocumentItem#getCategory() <em>category</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newCategory
	 *            the new value of the '{@link DocumentItem#getCategory()
	 *            category}' feature.
	 * @generated
	 */
	public void setCategory(DocumentItemCategory newCategory) {
		category = newCategory;
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
	 * Sets the '{@link DocumentItem#getDeleted() <em>deleted</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDeleted
	 *            the new value of the '{@link DocumentItem#getDeleted()
	 *            deleted}' feature.
	 * @generated
	 */
	public void setDeleted(Boolean newDeleted) {
		deleted = newDeleted;
	}

	/**
	 * Returns the value of '<em><b>itemRebate</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>itemRebate</b></em>' feature
	 * @generated
	 */
	public Double getItemRebate() {
		return itemRebate;
	}

	/**
	 * Sets the '{@link DocumentItem#getItemRebate() <em>itemRebate</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newItemRebate
	 *            the new value of the '{@link DocumentItem#getItemRebate()
	 *            itemRebate}' feature.
	 * @generated
	 */
	public void setItemRebate(Double newItemRebate) {
		itemRebate = newItemRebate;
	}

	/**
	 * Returns the value of '<em><b>itemNumber</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>itemNumber</b></em>' feature
	 * @generated
	 */
	public String getItemNumber() {
		return itemNumber;
	}

	/**
	 * Sets the '{@link DocumentItem#getItemNumber() <em>itemNumber</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newItemNumber
	 *            the new value of the '{@link DocumentItem#getItemNumber()
	 *            itemNumber}' feature.
	 * @generated
	 */
	public void setItemNumber(String newItemNumber) {
		itemNumber = newItemNumber;
	}

	/**
	 * Returns the value of '<em><b>productref</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>productref</b></em>' feature
	 * @generated
	 */
	public Product getProductref() {
		return productref;
	}

	/**
	 * Sets the '{@link DocumentItem#getProductref() <em>productref</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newProductref
	 *            the new value of the '{@link DocumentItem#getProductref()
	 *            productref}' feature.
	 * @generated
	 */
	public void setProductref(Product newProductref) {
		productref = newProductref;
	}

	/**
	 * Returns the value of '<em><b>dataContainer</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>dataContainer</b></em>' feature
	 * @generated
	 */
	public String getDataContainer() {
		return dataContainer;
	}

	/**
	 * Sets the '{@link DocumentItem#getDataContainer() <em>dataContainer</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDataContainer
	 *            the new value of the '{@link DocumentItem#getDataContainer()
	 *            dataContainer}' feature.
	 * @generated
	 */
	public void setDataContainer(String newDataContainer) {
		dataContainer = newDataContainer;
	}

	/**
	 * Returns the value of '<em><b>quantity</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>quantity</b></em>' feature
	 * @generated
	 */
	public Double getQuantity() {
		return quantity;
	}

	/**
	 * Sets the '{@link DocumentItem#getQuantity() <em>quantity</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newQuantity
	 *            the new value of the '{@link DocumentItem#getQuantity()
	 *            quantity}' feature.
	 * @generated
	 */
	public void setQuantity(Double newQuantity) {
		quantity = newQuantity;
	}

	/**
	 * Returns the value of '<em><b>weight</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>weight</b></em>' feature
	 * @generated
	 */
	public Double getWeight() {
		return weight;
	}

	/**
	 * Sets the '{@link DocumentItem#getWeight() <em>weight</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newWeight
	 *            the new value of the '{@link DocumentItem#getWeight() weight}'
	 *            feature.
	 * @generated
	 */
	public void setWeight(Double newWeight) {
		weight = newWeight;
	}

	/**
	 * Returns the value of '<em><b>tara</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>tara</b></em>' feature
	 * @generated
	 */
	public Double getTara() {
		return tara;
	}

	/**
	 * Sets the '{@link DocumentItem#getTara() <em>tara</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newTara
	 *            the new value of the '{@link DocumentItem#getTara() tara}'
	 *            feature.
	 * @generated
	 */
	public void setTara(Double newTara) {
		tara = newTara;
	}

	/**
	 * Returns the value of '<em><b>description</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>description</b></em>' feature
	 * @generated
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the '{@link DocumentItem#getDescription() <em>description</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newDescription
	 *            the new value of the '{@link DocumentItem#getDescription()
	 *            description}' feature.
	 * @generated
	 */
	public void setDescription(String newDescription) {
		description = newDescription;
	}

	/**
	 * Returns the value of '<em><b>optional</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>optional</b></em>' feature
	 * @generated
	 */
	public Boolean getOptional() {
		return optional;
	}

	/**
	 * Sets the '{@link DocumentItem#getOptional() <em>optional</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newOptional
	 *            the new value of the '{@link DocumentItem#getOptional()
	 *            optional}' feature.
	 * @generated
	 */
	public void setOptional(Boolean newOptional) {
		optional = newOptional;
	}

	/**
	 * Returns the value of '<em><b>pictureName</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>pictureName</b></em>' feature
	 * @generated
	 */
	public String getPictureName() {
		return pictureName;
	}

	/**
	 * Sets the '{@link DocumentItem#getPictureName() <em>pictureName</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newPictureName
	 *            the new value of the '{@link DocumentItem#getPictureName()
	 *            pictureName}' feature.
	 * @generated
	 */
	public void setPictureName(String newPictureName) {
		pictureName = newPictureName;
	}

	/**
	 * Returns the value of '<em><b>price</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>price</b></em>' feature
	 * @generated
	 */
	public BigDecimal getPrice() {
		return price;
	}

	/**
	 * Sets the '{@link DocumentItem#getPrice() <em>price</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newPrice
	 *            the new value of the '{@link DocumentItem#getPrice() price}'
	 *            feature.
	 * @generated
	 */
	public void setPrice(BigDecimal newPrice) {
		price = newPrice;
	}

	/**
	 * Returns the value of '<em><b>quantityUnit</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>quantityUnit</b></em>' feature
	 * @generated
	 */
	public String getQuantityUnit() {
		return quantityUnit;
	}

	/**
	 * Sets the '{@link DocumentItem#getQuantityUnit() <em>quantityUnit</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newQuantityUnit
	 *            the new value of the '{@link DocumentItem#getQuantityUnit()
	 *            quantityUnit}' feature.
	 * @generated
	 */
	public void setQuantityUnit(String newQuantityUnit) {
		quantityUnit = newQuantityUnit;
	}

	/**
	 * Returns the value of '<em><b>shared</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * "Shared" means if the item is shared between different documents (same
	 * item is referenced by different documents). <!-- end-model-doc -->
	 * 
	 * @return the value of '<em><b>shared</b></em>' feature
	 * @generated
	 */
	public Boolean getShared() {
		return shared;
	}

	/**
	 * Sets the '{@link DocumentItem#getShared() <em>shared</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc -->
	 * "Shared" means if the item is shared between different documents (same
	 * item is referenced by different documents). <!-- end-model-doc -->
	 * 
	 * @param newShared
	 *            the new value of the '{@link DocumentItem#getShared() shared}'
	 *            feature.
	 * @generated
	 */
	public void setShared(Boolean newShared) {
		shared = newShared;
	}

	/**
	 * Returns the value of '<em><b>itemVat</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>itemVat</b></em>' feature
	 * @generated
	 */
	public VAT getItemVat() {
		return itemVat;
	}

	/**
	 * Sets the '{@link DocumentItem#getItemVat() <em>itemVat</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newItemVat
	 *            the new value of the '{@link DocumentItem#getItemVat()
	 *            itemVat}' feature.
	 * @generated
	 */
	public void setItemVat(VAT newItemVat) {
		itemVat = newItemVat;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "DocumentItem " + " [id: " + getId() + "]" + " [name: "
				+ getName() + "]" + " [noVat: " + getNoVat() + "]"
				+ " [deleted: " + getDeleted() + "]" + " [itemRebate: "
				+ getItemRebate() + "]" + " [itemNumber: " + getItemNumber()
				+ "]" + " [dataContainer: " + getDataContainer() + "]"
				+ " [quantity: " + getQuantity() + "]" + " [weight: "
				+ getWeight() + "]" + " [tara: " + getTara() + "]"
				+ " [description: " + getDescription() + "]" + " [optional: "
				+ getOptional() + "]" + " [pictureName: " + getPictureName()
				+ "]" + " [price: " + getPrice() + "]" + " [quantityUnit: "
				+ getQuantityUnit() + "]" + " [shared: " + getShared() + "]";
	}
}
