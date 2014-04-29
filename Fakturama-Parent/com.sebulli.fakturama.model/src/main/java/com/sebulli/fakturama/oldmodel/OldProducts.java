package com.sebulli.fakturama.oldmodel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadOnly;

/**
 *  ###  Generated by EclipseLink Project EclipseLink Workbench 2.5.1 - Thu Nov 07 21:56:49 CET 2013.  ###
 */

@Entity
@Table(name = "Products")
@ReadOnly
public class OldProducts {

	private double weight;
	private int webshopid;
	private int vatid;
	private int unit;
	private String qunit;
	private double quantity;
	private double price5;
	private double price4;
	private double price3;
	private double price2;
	private double price1;
	private String picturename;
	private String options;
	private String name;
	private String itemnr;
	private String description;
	private boolean deleted;
	private String dateAdded;
	private String category;
	private int block5;
	private int block4;
	private int block3;
	private int block2;
	private int block1;
	@Id
	private int id;

public OldProducts() {
}

public int getBlock1() {
	return this.block1;
}

public int getBlock2() {
	return this.block2;
}

public int getBlock3() {
	return this.block3;
}

public int getBlock4() {
	return this.block4;
}

public int getBlock5() {
	return this.block5;
}

public String getCategory() {
	return this.category;
}

public String getDateAdded() {
	return this.dateAdded;
}

public String getDescription() {
	return this.description;
}

public int getId() {
	return this.id;
}

public String getItemnr() {
	return this.itemnr;
}

public String getName() {
	return this.name;
}

public String getOptions() {
	return this.options;
}

public String getPicturename() {
	return this.picturename;
}

public double getPrice1() {
	return this.price1;
}

public double getPrice2() {
	return this.price2;
}

public double getPrice3() {
	return this.price3;
}

public double getPrice4() {
	return this.price4;
}

public double getPrice5() {
	return this.price5;
}

public double getQuantity() {
	return this.quantity;
}

public String getQunit() {
	return this.qunit;
}

public int getUnit() {
	return this.unit;
}

public int getVatid() {
	return this.vatid;
}

public int getWebshopid() {
	return this.webshopid;
}

public double getWeight() {
	return this.weight;
}

public boolean isDeleted() {
	return this.deleted;
}

public void setBlock1(int block1) {
	this.block1 = block1;
}

public void setBlock2(int block2) {
	this.block2 = block2;
}

public void setBlock3(int block3) {
	this.block3 = block3;
}

public void setBlock4(int block4) {
	this.block4 = block4;
}

public void setBlock5(int block5) {
	this.block5 = block5;
}

public void setCategory(String category) {
	this.category = category;
}

public void setDateAdded(String dateAdded) {
	this.dateAdded = dateAdded;
}

public void setDeleted(boolean deleted) {
	this.deleted = deleted;
}

public void setDescription(String description) {
	this.description = description;
}

public void setId(int id) {
	this.id = id;
}

public void setItemnr(String itemnr) {
	this.itemnr = itemnr;
}

public void setName(String name) {
	this.name = name;
}

public void setOptions(String options) {
	this.options = options;
}

public void setPicturename(String picturename) {
	this.picturename = picturename;
}

public void setPrice1(double price1) {
	this.price1 = price1;
}

public void setPrice2(double price2) {
	this.price2 = price2;
}

public void setPrice3(double price3) {
	this.price3 = price3;
}

public void setPrice4(double price4) {
	this.price4 = price4;
}

public void setPrice5(double price5) {
	this.price5 = price5;
}

public void setQuantity(double quantity) {
	this.quantity = quantity;
}

public void setQunit(String qunit) {
	this.qunit = qunit;
}

public void setUnit(int unit) {
	this.unit = unit;
}

public void setVatid(int vatid) {
	this.vatid = vatid;
}

public void setWebshopid(int webshopid) {
	this.webshopid = webshopid;
}

public void setWeight(double weight) {
	this.weight = weight;
}

}
