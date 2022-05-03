/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rightsreserved_ This program and the accompanying materials
 * are made available under the terms of the Eclipse Public Licensev1_0
 * which accompanies this distribution,
 and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.office;

import java.util.Arrays;

/**
 * Placeholders for document templates
 */
public enum Placeholder {
    // placeholders for own company
    YOURCOMPANY_COMPANY("YOURCOMPANY.COMPANY"),
    YOURCOMPANY_OWNER("YOURCOMPANY.OWNER"),
    YOURCOMPANY_OWNER_FIRSTNAME("YOURCOMPANY.OWNER.FIRSTNAME"),
    YOURCOMPANY_OWNER_LASTNAME("YOURCOMPANY.OWNER.LASTNAME"),
    YOURCOMPANY_STREET("YOURCOMPANY.STREET"),
    YOURCOMPANY_STREETNAME("YOURCOMPANY.STREETNAME"),
    YOURCOMPANY_STREETNO("YOURCOMPANY.STREETNO"),
    YOURCOMPANY_ZIP("YOURCOMPANY.ZIP"),
    YOURCOMPANY_CITY("YOURCOMPANY.CITY"),
    YOURCOMPANY_COUNTRY("YOURCOMPANY.COUNTRY"),
    YOURCOMPANY_EMAIL("YOURCOMPANY.EMAIL"),
    YOURCOMPANY_MOBILE("YOURCOMPANY.MOBILE"),
    YOURCOMPANY_PHONE("YOURCOMPANY.PHONE"),
    YOURCOMPANY_PHONE_PRE("YOURCOMPANY.PHONE.PRE"),
    YOURCOMPANY_PHONE_POST("YOURCOMPANY.PHONE.POST"),
    YOURCOMPANY_FAX("YOURCOMPANY.FAX"),
    YOURCOMPANY_FAX_PRE("YOURCOMPANY.FAX.PRE"),
    YOURCOMPANY_FAX_POST("YOURCOMPANY.FAX.POST"),
    YOURCOMPANY_WEBSITE("YOURCOMPANY.WEBSITE"),
    YOURCOMPANY_VATNR("YOURCOMPANY.VATNR"),
    YOURCOMPANY_TAXNR("YOURCOMPANY.TAXNR"),
    YOURCOMPANY_TAXOFFICE("YOURCOMPANY.TAXOFFICE"),
    YOURCOMPANY_BANKACCOUNTNR("YOURCOMPANY.BANKACCOUNTNR"),
    YOURCOMPANY_BANK("YOURCOMPANY.BANK"),
    YOURCOMPANY_BANKCODE("YOURCOMPANY.BANKCODE"),
    YOURCOMPANY_IBAN("YOURCOMPANY.IBAN"),
    YOURCOMPANY_BIC("YOURCOMPANY.BIC"),
    YOURCOMPANY_CREDITORID("YOURCOMPANY.CREDITORID"),
    
    // common document placeholders
    DOCUMENT_DATE("DOCUMENT.DATE"),
    DOCUMENT_ADDRESSES_EQUAL("DOCUMENT.ADDRESSES.EQUAL"),
    DOCUMENT_ADDRESS("DOCUMENT.ADDRESS"),
    DOCUMENT_DELIVERYADDRESS("DOCUMENT.DELIVERYADDRESS"),
    DOCUMENT_DIFFERENT_ADDRESS("DOCUMENT.DIFFERENT.ADDRESS"),
    DOCUMENT_DIFFERENT_DELIVERYADDRESS("DOCUMENT.DIFFERENT.DELIVERYADDRESS"),
    DOCUMENT_TYPE("DOCUMENT.TYPE"),
    DOCUMENT_NAME("DOCUMENT.NAME"),
    DOCUMENT_CUSTOMERREF("DOCUMENT.CUSTOMERREF"),
    DOCUMENT_CONSULTANT("DOCUMENT.CONSULTANT"),
    DOCUMENT_SERVICEDATE("DOCUMENT.SERVICEDATE"),
    DOCUMENT_MESSAGE("DOCUMENT.MESSAGE"),
    DOCUMENT_MESSAGE1("DOCUMENT.MESSAGE1"),
    DOCUMENT_MESSAGE2("DOCUMENT.MESSAGE2"),
    DOCUMENT_MESSAGE3("DOCUMENT.MESSAGE3"),
    DOCUMENT_TRANSACTION("DOCUMENT.TRANSACTION"),
    DOCUMENT_INVOICE("DOCUMENT.INVOICE"),
    DOCUMENT_WEBSHOP_ID("DOCUMENT.WEBSHOP.ID"),
    DOCUMENT_WEBSHOP_DATE("DOCUMENT.WEBSHOP.DATE"),
    DOCUMENT_ORDER_DATE("DOCUMENT.ORDER.DATE"),
    DOCUMENT_VESTINGPERIOD_START("DOCUMENT.VESTINGPERIOD.START"),
    DOCUMENT_VESTINGPERIOD_END("DOCUMENT.VESTINGPERIOD.END"),
    DOCUMENT_DUNNING_LEVEL("DOCUMENT.DUNNING.LEVEL"),
    DOCUMENT_ITEMS_GROSS("DOCUMENT.ITEMS.GROSS"),
    DOCUMENT_ITEMS_NET("DOCUMENT.ITEMS.NET"),
    DOCUMENT_ITEMS_NET_DISCOUNTED("DOCUMENT.ITEMS.NET.DISCOUNTED"),
    DOCUMENT_ITEMS_COUNT("DOCUMENT.ITEMS.COUNT"),
    DOCUMENT_TOTAL_QUANTITY("DOCUMENT.TOTAL.QUANTITY"),
    DOCUMENT_TOTAL_NET("DOCUMENT.TOTAL.NET"),
    DOCUMENT_TOTAL_VAT("DOCUMENT.TOTAL.VAT"),
    DOCUMENT_TOTAL_GROSS("DOCUMENT.TOTAL.GROSS"),
    DOCUMENT_WEIGHT_TARA("DOCUMENT.WEIGHT.TARA"),
    DOCUMENT_WEIGHT_NET("DOCUMENT.WEIGHT.NET"),
    DOCUMENT_WEIGHT_TOTAL("DOCUMENT.WEIGHT.TOTAL"),
    DOCUMENT_DEPOSIT_DEPOSIT("DOCUMENT.DEPOSIT.DEPOSIT"),
    DOCUMENT_DEPOSIT_FINALPAYMENT("DOCUMENT.DEPOSIT.FINALPAYMENT"),
    DOCUMENT_DEPOSIT_DEP_TEXT("DOCUMENT.DEPOSIT.DEP_TEXT"),
    DOCUMENT_DEPOSIT_FINALPMT_TEXT("DOCUMENT.DEPOSIT.FINALPMT_TEXT"),
    DOCUMENT_REFERENCE_OFFER("DOCUMENT.REFERENCE.OFFER"),
    DOCUMENT_REFERENCE_ORDER("DOCUMENT.REFERENCE.ORDER"),
    DOCUMENT_REFERENCE_CONFIRMATION("DOCUMENT.REFERENCE.CONFIRMATION"),
    DOCUMENT_REFERENCE_INVOICE("DOCUMENT.REFERENCE.INVOICE"),
    DOCUMENT_REFERENCE_INVOICE_DATE("DOCUMENT.REFERENCE.INVOICE.DATE"),
    DOCUMENT_REFERENCE_DELIVERY("DOCUMENT.REFERENCE.DELIVERY"),
    DOCUMENT_REFERENCE_CREDIT("DOCUMENT.REFERENCE.CREDIT"),
    DOCUMENT_REFERENCE_DUNNING("DOCUMENT.REFERENCE.DUNNING"),
    DOCUMENT_REFERENCE_PROFORMA("DOCUMENT.REFERENCE.PROFORMA"),
    INVOICE_SWISSCODE("INVOICE.SWISSCODE"),
    
    /* Hint: The "discounted" flag for Vouchers isn't persisted,
     so we can't create a placeholder for it. */
    ITEMS_DISCOUNT_PERCENT("ITEMS.DISCOUNT.PERCENT"),
    ITEMS_DISCOUNT_NET("ITEMS.DISCOUNT.NET"),
    ITEMS_DISCOUNT_GROSS("ITEMS.DISCOUNT.GROSS"),
    ITEMS_DISCOUNT_VALUE("ITEMS.DISCOUNT.VALUE"),
    ITEMS_DISCOUNT_NETVALUE("ITEMS.DISCOUNT.NETVALUE"),
    ITEMS_DISCOUNT_TARAVALUE("ITEMS.DISCOUNT.TARAVALUE"),
    ITEMS_DISCOUNT_DISCOUNTPERCENT("ITEMS.DISCOUNT.DISCOUNTPERCENT"),
    ITEMS_DISCOUNT_DAYS("ITEMS.DISCOUNT.DAYS"),
    ITEMS_DISCOUNT_DUEDATE("ITEMS.DISCOUNT.DUEDATE"),
    SHIPPING_NET("SHIPPING.NET"),
    SHIPPING_VAT("SHIPPING.VAT"),
    SHIPPING_GROSS("SHIPPING.GROSS"),
    SHIPPING_DESCRIPTION("SHIPPING.DESCRIPTION"),
    SHIPPING_VAT_DESCRIPTION("SHIPPING.VAT.DESCRIPTION"),
    PAYMENT_TEXT("PAYMENT.TEXT"),
    PAYMENT_DESCRIPTION("PAYMENT.DESCRIPTION"),
    PAYMENT_PAID_VALUE("PAYMENT.PAID.VALUE"),
    PAYMENT_PAID_DATE("PAYMENT.PAID.DATE"),
    PAYMENT_DUE_DAYS("PAYMENT.DUE.DAYS"),
    PAYMENT_DUE_DATE("PAYMENT.DUE.DATE"),
    PAYMENT_PAID("PAYMENT.PAID"),
    
    // address placeholders
    ADDRESS_FIRSTLINE("ADDRESS.FIRSTLINE"),
    ADDRESS("ADDRESS"),
    ADDRESS_LOCALCONSULTANT("ADDRESS.LOCALCONSULTANT"),
    ADDRESS_ADDRESSADDON("ADDRESS.ADDRESSADDON"),
    ADDRESS_GENDER("ADDRESS.GENDER"),
    ADDRESS_GREETING("ADDRESS.GREETING"),
    ADDRESS_TITLE("ADDRESS.TITLE"),
    ADDRESS_NAME("ADDRESS.NAME"),
    ADDRESS_NAMESUFFIX("ADDRESS.NAMESUFFIX"),
    ADDRESS_NAMEADDON("ADDRESS.NAMEADDON"),
    ADDRESS_NAMEWITHCOMPANY("ADDRESS.NAMEWITHCOMPANY"),
    ADDRESS_FIRSTANDLASTNAME("ADDRESS.FIRSTANDLASTNAME"),
    ADDRESS_FIRSTNAME("ADDRESS.FIRSTNAME"),
    ADDRESS_LASTNAME("ADDRESS.LASTNAME"),
    ADDRESS_COMPANY("ADDRESS.COMPANY"),
    ADDRESS_STREET("ADDRESS.STREET"),
    ADDRESS_STREETNAME("ADDRESS.STREETNAME"),
    ADDRESS_STREETNO("ADDRESS.STREETNO"),
    ADDRESS_ZIP("ADDRESS.ZIP"),
    ADDRESS_CITY("ADDRESS.CITY"),
    ADDRESS_CITYADDON("ADDRESS.CITYADDON"),
    ADDRESS_COUNTRY("ADDRESS.COUNTRY"),
    ADDRESS_COUNTRY_CODE2("ADDRESS.COUNTRY.CODE2"),
    ADDRESS_COUNTRY_CODE3("ADDRESS.COUNTRY.CODE3"),
    ADDRESS_BIRTHDAY("ADDRESS.BIRTHDAY"),
    ADDRESS_BANK_ACCOUNT_HOLDER("ADDRESS.BANK.ACCOUNT.HOLDER"),
    ADDRESS_BANK_ACCOUNT("ADDRESS.BANK.ACCOUNT"),
    ADDRESS_BANK_CODE("ADDRESS.BANK.CODE"),
    ADDRESS_BANK_NAME("ADDRESS.BANK.NAME"),
    ADDRESS_BANK_IBAN("ADDRESS.BANK.IBAN"),
    ADDRESS_BANK_BIC("ADDRESS.BANK.BIC"),
    ADDRESS_DISCOUNT("ADDRESS.DISCOUNT"),
    ADDRESS_EMAIL("ADDRESS.EMAIL"),
    ADDRESS_FAX("ADDRESS.FAX"),
    ADDRESS_FAX_PRE("ADDRESS.FAX.PRE"),
    ADDRESS_FAX_POST("ADDRESS.FAX.POST"),
    ADDRESS_GLN("ADDRESS.GLN"),
    ADDRESS_MANDATEREFERENCE("ADDRESS.MANDATEREFERENCE"),
    ADDRESS_MOBILE("ADDRESS.MOBILE"),
    ADDRESS_MOBILE_PRE("ADDRESS.MOBILE.PRE"),
    ADDRESS_MOBILE_POST("ADDRESS.MOBILE.POST"),
    ADDRESS_NR("ADDRESS.NR"),
    ADDRESS_NOTE("ADDRESS.NOTE"),
    ADDRESS_PHONE("ADDRESS.PHONE"),
    ADDRESS_PHONE_PRE("ADDRESS.PHONE.PRE"),
    ADDRESS_PHONE_POST("ADDRESS.PHONE.POST"),
    ADDRESS_PHONE2("ADDRESS.PHONE2"),
    ADDRESS_SUPPLIER_NUMBER("ADDRESS.SUPPLIER.NUMBER"),
    ADDRESS_VATNR("ADDRESS.VATNR"),
    ADDRESS_WEBSITE("ADDRESS.WEBSITE"),
    ADDRESS_WEBSHOPUSER("ADDRESS.WEBSHOPUSER"),
    ADDRESS_PAYMENT("ADDRESS.PAYMENT"),
    ADDRESS_ALIAS("ADDRESS.ALIAS"),
    ADDRESS_HASSALESEQTAX("ADDRESS.HASSALESEQTAX"),
    ADDRESS_REGISTERNUMBER("ADDRESS.REGISTERNUMBER"),
    ADDRESS_RELIABLILITY("ADDRESS.RELIABLILITY"),
    
    DELIVERY_ADDRESS_FIRSTLINE("DELIVERY.ADDRESS.FIRSTLINE"),
    DELIVERY_ADDRESS("DELIVERY.ADDRESS"),
    DELIVERY_ADDRESS_GENDER("DELIVERY.ADDRESS.GENDER"),
    DELIVERY_ADDRESS_GREETING("DELIVERY.ADDRESS.GREETING"),
    DELIVERY_ADDRESS_TITLE("DELIVERY.ADDRESS.TITLE"),
    DELIVERY_ADDRESS_NAME("DELIVERY.ADDRESS.NAME"),
    DELIVERY_ADDRESS_BIRTHDAY("DELIVERY.ADDRESS.BIRTHDAY"),
    DELIVERY_ADDRESS_NAMEWITHCOMPANY("DELIVERY.ADDRESS.NAMEWITHCOMPANY"),
    DELIVERY_ADDRESS_FIRSTNAME("DELIVERY.ADDRESS.FIRSTNAME"),
    DELIVERY_ADDRESS_LASTNAME("DELIVERY.ADDRESS.LASTNAME"),
    DELIVERY_ADDRESS_COMPANY("DELIVERY.ADDRESS.COMPANY"),
    DELIVERY_ADDRESS_STREET("DELIVERY.ADDRESS.STREET"),
    DELIVERY_ADDRESS_STREETNAME("DELIVERY.ADDRESS.STREETNAME"),
    DELIVERY_ADDRESS_STREETNO("DELIVERY.ADDRESS.STREETNO"),
    DELIVERY_ADDRESS_ZIP("DELIVERY.ADDRESS.ZIP"),
    DELIVERY_ADDRESS_CITY("DELIVERY.ADDRESS.CITY"),
    DELIVERY_ADDRESS_COUNTRY("DELIVERY.ADDRESS.COUNTRY"),
    DELIVERY_ADDRESS_COUNTRY_CODE2("DELIVERY.ADDRESS.COUNTRY.CODE2"),
    DELIVERY_ADDRESS_COUNTRY_CODE3("DELIVERY.ADDRESS.COUNTRY.CODE3"),
    DELIVERY_ADDRESS_LOCALCONSULTANT("DELIVERY.ADDRESS.LOCALCONSULTANT"),
    DELIVERY_ADDRESS_ADDRESSADDON("DELIVERY.ADDRESS.ADDRESSADDON"),
    DELIVERY_ADDRESS_NAMESUFFIX("DELIVERY.ADDRESS.NAMESUFFIX"),
    DELIVERY_ADDRESS_NAMEADDON("DELIVERY.ADDRESS.NAMEADDON"),
    DELIVERY_ADDRESS_FIRSTANDLASTNAME("DELIVERY.ADDRESS.FIRSTANDLASTNAME"),
    DELIVERY_ADDRESS_CITYADDON("DELIVERY.ADDRESS.CITYADDON"),
    DELIVERY_ADDRESS_EMAIL("DELIVERY.ADDRESS.EMAIL"),
    DELIVERY_ADDRESS_FAX("DELIVERY.ADDRESS.FAX"),
    DELIVERY_ADDRESS_FAX_PRE("DELIVERY.ADDRESS.FAX.PRE"),
    DELIVERY_ADDRESS_FAX_POST("DELIVERY.ADDRESS.FAX.POST"),
    DELIVERY_ADDRESS_MOBILE("DELIVERY.ADDRESS.MOBILE"),
    DELIVERY_ADDRESS_MOBILE_PRE("DELIVERY.ADDRESS.MOBILE.PRE"),
    DELIVERY_ADDRESS_MOBILE_POST("DELIVERY.ADDRESS.MOBILE.POST"),
    DELIVERY_ADDRESS_PHONE("DELIVERY.ADDRESS.PHONE"),
    DELIVERY_ADDRESS_PHONE_PRE("DELIVERY.ADDRESS.PHONE.PRE"),
    DELIVERY_ADDRESS_PHONE_POST("DELIVERY.ADDRESS.PHONE.POST"),
    DELIVERY_ADDRESS_PHONE2("DELIVERY.ADDRESS.PHONE2"),

    DEBITOR_MANDATREF("DEBITOR.MANDATREF");

    /* only for completeness (not used / unnecessary placeholders)
     * -VOUCHER_DONTBOOK
     * -ADDRESS_RELIABILITY
     */
            
    private String key;

    private Placeholder(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
    public Placeholder findByKey(String key) {
        return Arrays.stream(values()).filter(p -> p.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
    }
}
