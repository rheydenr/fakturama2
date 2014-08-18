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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A representation of the model object '<em><b>Product</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_PRODUCT")
public class Product extends ModelObject implements IEntity, Serializable {
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
    @Basic()
    @Column(name = "BLOCK1")
    private Integer block1 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "BLOCK2")
    private Integer block2 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "BLOCK3")
    private Integer block3 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "BLOCK4")
    private Integer block4 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "BLOCK5")
    private Integer block5 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(joinColumns = { @JoinColumn(name = "PRODUCT_CATEGORIES") }, inverseJoinColumns = { @JoinColumn(name = "CATEGORIES_PRODUCTCATEGORY") }, name = "FKT_PRODUCT_CATEGORIES")
    private List<ProductCategory> categories = new ArrayList<ProductCategory>();

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DATEADDED")
    @Temporal(TemporalType.DATE)
    private Date dateAdded = null;

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
    @Column(name = "PRODUCTCODE")
    private String productCode = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumns({ @JoinColumn(name = "PRODUCT_ATTRIBUTES") })
    private List<ProductOptions> attributes = new ArrayList<ProductOptions>();

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
    @Column(name = "ITEMNUMBER")
    private String itemNumber = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PRICE1", precision = 8, scale = 3)
    private BigDecimal price1 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PRICE2", precision = 8, scale = 3)
    private BigDecimal price2 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PRICE3", precision = 8, scale = 3)
    private BigDecimal price3 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PRICE4", precision = 8, scale = 3)
    private BigDecimal price4 = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "PRICE5", precision = 8, scale = 3)
    private BigDecimal price5 = null;

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
    @Column(name = "QUANTITYUNIT")
    private String quantityUnit = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "SELLINGUNIT")
    private Integer sellingUnit = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumns({ @JoinColumn(name = "FK_VAT") })
    private VAT vat = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "WEBSHOPID")
    private Long webshopId = null;

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
     * Returns the value of '<em><b>block1</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>block1</b></em>' feature
     * @generated
     */
    public Integer getBlock1() {
        return block1;
    }

    /**
     * Sets the '{@link Product#getBlock1() <em>block1</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newBlock1
     *            the new value of the '{@link Product#getBlock1() block1}'
     *            feature.
     * @generated
     */
    public void setBlock1(Integer newBlock1) {
        block1 = newBlock1;
        firePropertyChange("block1", this.block1, newBlock1);
    }

    /**
     * Returns the value of '<em><b>block2</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>block2</b></em>' feature
     * @generated
     */
    public Integer getBlock2() {
        return block2;
    }

    /**
     * Sets the '{@link Product#getBlock2() <em>block2</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newBlock2
     *            the new value of the '{@link Product#getBlock2() block2}'
     *            feature.
     * @generated
     */
    public void setBlock2(Integer newBlock2) {
        block2 = newBlock2;
        firePropertyChange("block2", this.block2, newBlock2);
    }

    /**
     * Returns the value of '<em><b>block3</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>block3</b></em>' feature
     * @generated
     */
    public Integer getBlock3() {
        return block3;
    }

    /**
     * Sets the '{@link Product#getBlock3() <em>block3</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newBlock3
     *            the new value of the '{@link Product#getBlock3() block3}'
     *            feature.
     * @generated
     */
    public void setBlock3(Integer newBlock3) {
        block3 = newBlock3;
        firePropertyChange("block3", this.block3, newBlock3);
    }

    /**
     * Returns the value of '<em><b>block4</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>block4</b></em>' feature
     * @generated
     */
    public Integer getBlock4() {
        return block4;
    }

    /**
     * Sets the '{@link Product#getBlock4() <em>block4</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newBlock4
     *            the new value of the '{@link Product#getBlock4() block4}'
     *            feature.
     * @generated
     */
    public void setBlock4(Integer newBlock4) {
        block4 = newBlock4;
        firePropertyChange("block4", this.block4, newBlock4);
    }

    /**
     * Returns the value of '<em><b>block5</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>block5</b></em>' feature
     * @generated
     */
    public Integer getBlock5() {
        return block5;
    }

    /**
     * Sets the '{@link Product#getBlock5() <em>block5</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newBlock5
     *            the new value of the '{@link Product#getBlock5() block5}'
     *            feature.
     * @generated
     */
    public void setBlock5(Integer newBlock5) {
        block5 = newBlock5;
        firePropertyChange("block5", this.block5, newBlock5);
    }

    /**
     * Returns the value of '<em><b>categories</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>categories</b></em>' feature
     * @generated
     */
    public List<ProductCategory> getCategories() {
        return categories;
    }

    /**
     * Adds to the <em>categories</em> feature.
     *
     * @param categoriesValue
     *            value to add
     *
     * @generated
     */
    public boolean addToCategories(ProductCategory categoriesValue) {
        if (!categories.contains(categoriesValue)) {
            categories.add(categoriesValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes from the <em>categories</em> feature.
     *
     * @param categoriesValue
     *            value to remove
     *
     * @generated
     */
    public boolean removeFromCategories(ProductCategory categoriesValue) {
        if (categories.contains(categoriesValue)) {
            categories.remove(categoriesValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Clears the <em>categories</em> feature.
     * 
     * @generated
     */
    public void clearCategories() {
        while (!categories.isEmpty()) {
            removeFromCategories(categories.iterator().next());
        }
    }

    /**
     * Sets the '{@link Product#getCategories() <em>categories</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newCategories
     *            the new value of the '{@link Product#getCategories()
     *            categories}' feature.
     * @generated
     */
    public void setCategories(List<ProductCategory> newCategories) {
        categories = newCategories;
        firePropertyChange("categories", this.categories, newCategories);
    }

    /**
     * Returns the value of '<em><b>dateAdded</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>dateAdded</b></em>' feature
     * @generated
     */
    public Date getDateAdded() {
        return dateAdded;
    }

    /**
     * Sets the '{@link Product#getDateAdded() <em>dateAdded</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDateAdded
     *            the new value of the '{@link Product#getDateAdded() dateAdded}
     *            ' feature.
     * @generated
     */
    public void setDateAdded(Date newDateAdded) {
        dateAdded = newDateAdded;
        firePropertyChange("dateAdded", this.dateAdded, newDateAdded);
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
     * Sets the '{@link Product#getDescription() <em>description</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDescription
     *            the new value of the '{@link Product#getDescription()
     *            description}' feature.
     * @generated
     */
    public void setDescription(String newDescription) {
        description = newDescription;
        firePropertyChange("description", this.description, newDescription);
    }

    /**
     * Returns the value of '<em><b>productCode</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>productCode</b></em>' feature
     * @generated
     */
    public String getProductCode() {
        return productCode;
    }

    /**
     * Sets the '{@link Product#getProductCode() <em>productCode</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newProductCode
     *            the new value of the '{@link Product#getProductCode()
     *            productCode}' feature.
     * @generated
     */
    public void setProductCode(String newProductCode) {
        productCode = newProductCode;
        firePropertyChange("productCode", this.productCode, newProductCode);
    }

    /**
     * Returns the value of '<em><b>attributes</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>attributes</b></em>' feature
     * @generated
     */
    public List<ProductOptions> getAttributes() {
        return attributes;
    }

    /**
     * Adds to the <em>attributes</em> feature.
     *
     * @param attributesValue
     *            value to add
     *
     * @generated
     */
    public boolean addToAttributes(ProductOptions attributesValue) {
        if (!attributes.contains(attributesValue)) {
            attributes.add(attributesValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes from the <em>attributes</em> feature.
     *
     * @param attributesValue
     *            value to remove
     *
     * @generated
     */
    public boolean removeFromAttributes(ProductOptions attributesValue) {
        if (attributes.contains(attributesValue)) {
            attributes.remove(attributesValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Clears the <em>attributes</em> feature.
     * 
     * @generated
     */
    public void clearAttributes() {
        while (!attributes.isEmpty()) {
            removeFromAttributes(attributes.iterator().next());
        }
    }

    /**
     * Sets the '{@link Product#getAttributes() <em>attributes</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newAttributes
     *            the new value of the '{@link Product#getAttributes()
     *            attributes}' feature.
     * @generated
     */
    public void setAttributes(List<ProductOptions> newAttributes) {
        attributes = newAttributes;
        firePropertyChange("attributes", this.attributes, newAttributes);
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
     * Sets the '{@link Product#getPictureName() <em>pictureName</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPictureName
     *            the new value of the '{@link Product#getPictureName()
     *            pictureName}' feature.
     * @generated
     */
    public void setPictureName(String newPictureName) {
        pictureName = newPictureName;
        firePropertyChange("pictureName", this.pictureName, newPictureName);
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
     * Sets the '{@link Product#getItemNumber() <em>itemNumber</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newItemNumber
     *            the new value of the '{@link Product#getItemNumber()
     *            itemNumber}' feature.
     * @generated
     */
    public void setItemNumber(String newItemNumber) {
        itemNumber = newItemNumber;
        firePropertyChange("itemNumber", this.itemNumber, newItemNumber);
    }

    /**
     * Returns the value of '<em><b>price1</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>price1</b></em>' feature
     * @generated
     */
    public BigDecimal getPrice1() {
        return price1;
    }

    /**
     * Sets the '{@link Product#getPrice1() <em>price1</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPrice1
     *            the new value of the '{@link Product#getPrice1() price1}'
     *            feature.
     * @generated
     */
    public void setPrice1(BigDecimal newPrice1) {
        price1 = newPrice1;
        firePropertyChange("price1", this.price1, newPrice1);
    }

    /**
     * Returns the value of '<em><b>price2</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>price2</b></em>' feature
     * @generated
     */
    public BigDecimal getPrice2() {
        return price2;
    }

    /**
     * Sets the '{@link Product#getPrice2() <em>price2</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPrice2
     *            the new value of the '{@link Product#getPrice2() price2}'
     *            feature.
     * @generated
     */
    public void setPrice2(BigDecimal newPrice2) {
        price2 = newPrice2;
        firePropertyChange("price2", this.price2, newPrice2);
    }

    /**
     * Returns the value of '<em><b>price3</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>price3</b></em>' feature
     * @generated
     */
    public BigDecimal getPrice3() {
        return price3;
    }

    /**
     * Sets the '{@link Product#getPrice3() <em>price3</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPrice3
     *            the new value of the '{@link Product#getPrice3() price3}'
     *            feature.
     * @generated
     */
    public void setPrice3(BigDecimal newPrice3) {
        price3 = newPrice3;
        firePropertyChange("price3", this.price3, newPrice3);
    }

    /**
     * Returns the value of '<em><b>price4</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>price4</b></em>' feature
     * @generated
     */
    public BigDecimal getPrice4() {
        return price4;
    }

    /**
     * Sets the '{@link Product#getPrice4() <em>price4</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPrice4
     *            the new value of the '{@link Product#getPrice4() price4}'
     *            feature.
     * @generated
     */
    public void setPrice4(BigDecimal newPrice4) {
        price4 = newPrice4;
        firePropertyChange("price4", this.price4, newPrice4);
    }

    /**
     * Returns the value of '<em><b>price5</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>price5</b></em>' feature
     * @generated
     */
    public BigDecimal getPrice5() {
        return price5;
    }

    /**
     * Sets the '{@link Product#getPrice5() <em>price5</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newPrice5
     *            the new value of the '{@link Product#getPrice5() price5}'
     *            feature.
     * @generated
     */
    public void setPrice5(BigDecimal newPrice5) {
        price5 = newPrice5;
        firePropertyChange("price5", this.price5, newPrice5);
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
     * Sets the '{@link Product#getQuantity() <em>quantity</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newQuantity
     *            the new value of the '{@link Product#getQuantity() quantity}'
     *            feature.
     * @generated
     */
    public void setQuantity(Double newQuantity) {
        quantity = newQuantity;
        firePropertyChange("quantity", this.quantity, newQuantity);
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
     * Sets the '{@link Product#getQuantityUnit() <em>quantityUnit</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newQuantityUnit
     *            the new value of the '{@link Product#getQuantityUnit()
     *            quantityUnit}' feature.
     * @generated
     */
    public void setQuantityUnit(String newQuantityUnit) {
        quantityUnit = newQuantityUnit;
        firePropertyChange("quantityUnit", this.quantityUnit, newQuantityUnit);
    }

    /**
     * Returns the value of '<em><b>sellingUnit</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>sellingUnit</b></em>' feature
     * @generated
     */
    public Integer getSellingUnit() {
        return sellingUnit;
    }

    /**
     * Sets the '{@link Product#getSellingUnit() <em>sellingUnit</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newSellingUnit
     *            the new value of the '{@link Product#getSellingUnit()
     *            sellingUnit}' feature.
     * @generated
     */
    public void setSellingUnit(Integer newSellingUnit) {
        sellingUnit = newSellingUnit;
        firePropertyChange("sellingUnit", this.sellingUnit, newSellingUnit);
    }

    /**
     * Returns the value of '<em><b>vat</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>vat</b></em>' feature
     * @generated
     */
    public VAT getVat() {
        return vat;
    }

    /**
     * Sets the '{@link Product#getVat() <em>vat</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newVat
     *            the new value of the '{@link Product#getVat() vat}' feature.
     * @generated
     */
    public void setVat(VAT newVat) {
        vat = newVat;
        firePropertyChange("vat", this.vat, newVat);
    }

    /**
     * Returns the value of '<em><b>webshopId</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>webshopId</b></em>' feature
     * @generated
     */
    public Long getWebshopId() {
        return webshopId;
    }

    /**
     * Sets the '{@link Product#getWebshopId() <em>webshopId</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newWebshopId
     *            the new value of the '{@link Product#getWebshopId() webshopId}
     *            ' feature.
     * @generated
     */
    public void setWebshopId(Long newWebshopId) {
        webshopId = newWebshopId;
        firePropertyChange("webshopId", this.webshopId, newWebshopId);
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
     * Sets the '{@link Product#getWeight() <em>weight</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newWeight
     *            the new value of the '{@link Product#getWeight() weight}'
     *            feature.
     * @generated
     */
    public void setWeight(Double newWeight) {
        weight = newWeight;
        firePropertyChange("weight", this.weight, newWeight);
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
     * Sets the '{@link Product#getName() <em>name</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newName
     *            the new value of the '{@link Product#getName() name}' feature.
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
     * Sets the '{@link Product#getId() <em>id</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newId
     *            the new value of the '{@link Product#getId() id}' feature.
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
     * Sets the '{@link Product#getDeleted() <em>deleted</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDeleted
     *            the new value of the '{@link Product#getDeleted() deleted}'
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
        return "Product " + " [block1: " + getBlock1() + "]" + " [block2: " + getBlock2() + "]" + " [block3: " + getBlock3() + "]" + " [block4: " + getBlock4()
                + "]" + " [block5: " + getBlock5() + "]" + " [dateAdded: " + getDateAdded() + "]" + " [description: " + getDescription() + "]"
                + " [productCode: " + getProductCode() + "]" + " [pictureName: " + getPictureName() + "]" + " [itemNumber: " + getItemNumber() + "]"
                + " [price1: " + getPrice1() + "]" + " [price2: " + getPrice2() + "]" + " [price3: " + getPrice3() + "]" + " [price4: " + getPrice4() + "]"
                + " [price5: " + getPrice5() + "]" + " [quantity: " + getQuantity() + "]" + " [quantityUnit: " + getQuantityUnit() + "]" + " [sellingUnit: "
                + getSellingUnit() + "]" + " [webshopId: " + getWebshopId() + "]" + " [weight: " + getWeight() + "]" + " [name: " + getName() + "]" + " [id: "
                + getId() + "]" + " [deleted: " + getDeleted() + "]";
    }
}
