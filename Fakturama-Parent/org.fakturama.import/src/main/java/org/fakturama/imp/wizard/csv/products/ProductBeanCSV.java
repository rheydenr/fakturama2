package org.fakturama.imp.wizard.csv.products;

import java.util.Date;

import com.sebulli.fakturama.model.Product;

/**
 * Helper class for importing / exporting {@link Product} data.
 * 
 * TODO refactor to record data type (with Java 14+)
 */
public class ProductBeanCSV {
    private Integer block1 = Integer.valueOf(1);
    private Integer block2 = Integer.valueOf(10);
    private Integer block3 = Integer.valueOf(100);
    private Integer block4 = Integer.valueOf(1000);
    private Integer block5 = Integer.valueOf(10000);
    private Date dateAdded = null;
    private String modifiedBy = null;
    private Date modified = null;
    private String pictureName = null;
    private String itemNumber = null;
    private String supplierItemNumber = null;
    private Double price1 = Double.valueOf(0.0);
    private Double price2 = Double.valueOf(0.0);
    private Double price3 = Double.valueOf(0.0);
    private Double price4 = Double.valueOf(0.0);
    private Double price5 = Double.valueOf(0.0);
    private Double quantity = null;
    private String quantityUnit = null;
    private Integer sellingUnit = null;
    private Double vat = null;
    private String vatName = null;
    private String category = null;
    private Long webshopId = null;
    private Double weight = null;
    private Long gtin = null;
    private Double costPrice = null;
    private String allowance = null;
    private String cdf01 = null;
    private String cdf02 = null;
    private String cdf03 = null;
    private String note = null;
    private String description = null;
    private String name = null;
    private long id = 0;

    public Integer getBlock1() {
        return block1;
    }

    public void setBlock1(Integer block1) {
        this.block1 = block1;
    }

    public Integer getBlock2() {
        return block2;
    }

    public void setBlock2(Integer block2) {
        this.block2 = block2;
    }

    public Integer getBlock3() {
        return block3;
    }

    public void setBlock3(Integer block3) {
        this.block3 = block3;
    }

    public Integer getBlock4() {
        return block4;
    }

    public void setBlock4(Integer block4) {
        this.block4 = block4;
    }

    public Integer getBlock5() {
        return block5;
    }

    public void setBlock5(Integer block5) {
        this.block5 = block5;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getSupplierItemNumber() {
        return supplierItemNumber;
    }

    public void setSupplierItemNumber(String supplierItemNumber) {
        this.supplierItemNumber = supplierItemNumber;
    }

    public Double getPrice1() {
        return price1;
    }

    public void setPrice1(Double price1) {
        this.price1 = price1;
    }

    public Double getPrice2() {
        return price2;
    }

    public void setPrice2(Double price2) {
        this.price2 = price2;
    }

    public Double getPrice3() {
        return price3;
    }

    public void setPrice3(Double price3) {
        this.price3 = price3;
    }

    public Double getPrice4() {
        return price4;
    }

    public void setPrice4(Double price4) {
        this.price4 = price4;
    }

    public Double getPrice5() {
        return price5;
    }

    public void setPrice5(Double price5) {
        this.price5 = price5;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public Integer getSellingUnit() {
        return sellingUnit;
    }

    public void setSellingUnit(Integer sellingUnit) {
        this.sellingUnit = sellingUnit;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getWebshopId() {
        return webshopId;
    }

    public void setWebshopId(Long webshopId) {
        this.webshopId = webshopId;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Long getGtin() {
        return gtin;
    }

    public void setGtin(Long gtin) {
        this.gtin = gtin;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public String getAllowance() {
        return allowance;
    }

    public void setAllowance(String allowance) {
        this.allowance = allowance;
    }

    public String getCdf01() {
        return cdf01;
    }

    public void setCdf01(String cdf01) {
        this.cdf01 = cdf01;
    }

    public String getCdf02() {
        return cdf02;
    }

    public void setCdf02(String cdf02) {
        this.cdf02 = cdf02;
    }

    public String getCdf03() {
        return cdf03;
    }

    public void setCdf03(String cdf03) {
        this.cdf03 = cdf03;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getVatName() {
        return vatName;
    }

    public void setVatName(String vatName) {
        this.vatName = vatName;
    }
}
