package com.sebulli.fakturama.dto;

import java.util.List;

import javax.money.MonetaryAmount;

import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.VAT;

public class DocumentSummaryParam {
	private List<DocumentItem> documentItems;
	private Double shippingValue;
	private ShippingVatType autoVat;
	private Double itemsDiscount;
	private Double scaleFactor;
	private VAT noVatRef;
	private VAT shippingVat;
	private int netGross;
	private MonetaryAmount deposit;

	public DocumentSummaryParam withItems(List<DocumentItem> documentItems) {
		this.documentItems = documentItems;
		return this;
	}
	
	public DocumentSummaryParam withShippingValue(Double shippingValue) {
		this.shippingValue = shippingValue;
		return this;
	}
	
	public DocumentSummaryParam withAutoVat(ShippingVatType autoVat) {
		this.autoVat = autoVat;
		return this;
	}
	
	public DocumentSummaryParam withNoVatRef(VAT noVatRef) {
		this.noVatRef = noVatRef;
		return this;
	}

	public List<DocumentItem> getDocumentItems() {
		return documentItems;
	}

	public Double getShippingValue() {
		return shippingValue;
	}

	public ShippingVatType getAutoVat() {
		return autoVat;
	}

	public VAT getNoVatRef() {
		return noVatRef;
	}

	public VAT getShippingVat() {
		return shippingVat;
	}

	public DocumentSummaryParam withShippingVat(VAT shippingVat) {
		this.shippingVat = shippingVat;
		return this;
	}

	public Double getItemsDiscount() {
		return itemsDiscount;
	}

	public DocumentSummaryParam withItemsDiscount(Double itemsDiscount) {
		this.itemsDiscount = itemsDiscount;
		return this;
	}

	public Double getScaleFactor() {
		return scaleFactor;
	}

	public DocumentSummaryParam withScaleFactor(Double scaleFactor) {
		this.scaleFactor = scaleFactor;
		return this;
	}

	public int getNetGross() {
		return netGross;
	}

	public DocumentSummaryParam withNetGross(int netGross) {
		this.netGross = netGross;
		return this;
	}

	public MonetaryAmount getDeposit() {
		return deposit;
	}

	public DocumentSummaryParam withDeposit(MonetaryAmount deposit) {
		this.deposit = deposit;
		return this;
	}
}