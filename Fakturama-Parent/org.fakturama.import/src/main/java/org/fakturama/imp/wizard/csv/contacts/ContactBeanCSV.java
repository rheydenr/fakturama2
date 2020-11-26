package org.fakturama.imp.wizard.csv.contacts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EStructuralFeature;

import com.opencsv.bean.CsvIgnore;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Product;

/**
 * Helper class for importing / exporting {@link Contact} data.
 * 
 */
public class ContactBeanCSV {

    @CsvIgnore
    private static Map<String, String> attributeToNameMap;
    private String alias;
    private String category = null;
    private String company = null;
    private String customerNumber = null;
    private String title = null;
    private String firstName = null;
    private String name = null;

    // can be "m" or "f"
    private String gender = null;
    private String birthday = null;

    // billing address
    private String street, cityAddon, zip, city, countryCode, localConsultant, email, mobile, phone, fax, additionalPhone;
    
    // delivery address
    private String deliveryStreet, deliveryCityAddon, deliveryZip, deliveryCity, deliveryCountryCode, deliveryLocalConsultant, deliveryEmail,
            deliveryMobile, deliveryPhone, deliveryFax, deliveryAdditionalPhone;

    private Double discount = null;
    private String paymentType = null;
    private String reliability = null;
    private String vatNumber = null;
    private Boolean vatNumberValid = null;
    private String website = null;
    private String webshopName = null;
    private String supplierNumber = null;

    private Long gln = null;
    private String mandateReference = null;

    // banking info
    private String account_holder = null;
    private String bankName = null;
    private String iban = null;
    private String bic = null;

    private String registerNumber = null;
    private Date dateAdded = null;
    private String note = null;
    private Long id = null;
    //    private String modifiedBy = null;
    //    private Date modified = null;

    public static String getI18NIdentifier(String csvField) {
        return attributeToNameMap.get(csvField);
    }

    /**
     * Creates a map of product attributes which maps to name keys (for I18N'ed
     * list entries). Only classifiers for attributes and 1:1 references are
     * used. The map consists of the <i>name</i> attribute of a
     * {@link EStructuralFeature} and the corresponding label entry. <br/>
     * <p>
     * This method is <code>static</code> because the list of product attributes
     * doesn't change.
     * </p>
     * <p>
     * <b>IMPORTANT</b> This method has to be updated if new attributes are
     * added to {@link Product} entity.
     * </p>
     */
    public static Map<String, String> createContactsAttributeMap(Messages msg) {
        if (attributeToNameMap == null) {
            attributeToNameMap = new HashMap<>();
            attributeToNameMap.put("alias", msg.editorContactFieldAlias);
            attributeToNameMap.put("category", msg.commonFieldCategory);
            attributeToNameMap.put("company", msg.commonFieldCompany);
            attributeToNameMap.put("customerNumber", msg.commonFieldNumber);
            attributeToNameMap.put("title", msg.commonFieldTitle);
            attributeToNameMap.put("firstName", msg.commonFieldFirstname);
            attributeToNameMap.put("name", msg.commonFieldLastname);
            attributeToNameMap.put("gender", msg.commonFieldGender);

            // Billing address
            attributeToNameMap.put("street", msg.commonFieldStreet);
            attributeToNameMap.put("cityAddon", msg.editorContactFieldAddressAddon);
            attributeToNameMap.put("zip", msg.commonFieldZipcode);
            attributeToNameMap.put("city", msg.commonFieldCity);
            attributeToNameMap.put("countryCode", msg.commonFieldCountry);
            attributeToNameMap.put("localConsultant", msg.editorContactFieldLocalconsultant);
            attributeToNameMap.put("email", msg.exporterDataEmail);
            attributeToNameMap.put("mobile", msg.exporterDataMobile);
            attributeToNameMap.put("phone", msg.exporterDataTelephone);
            attributeToNameMap.put("fax", msg.exporterDataTelefax);
            attributeToNameMap.put("additionalPhone", msg.editorContactFieldAdditionalPhone);
            attributeToNameMap.put("birthday", msg.editorContactFieldBirthdayName);

            // Delivery address
            attributeToNameMap.put("deliveryStreet", String.format("%s (%s)", msg.commonFieldStreet, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryCityAddon", String.format("%s (%s)", msg.editorContactFieldAddressAddon, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryZip", String.format("%s (%s)", msg.commonFieldZipcode, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryCity", String.format("%s (%s)", msg.commonFieldCity, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryCountryCode", String.format("%s (%s)", msg.commonFieldCountry, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryLocalConsultant", String.format("%s (%s)", msg.editorContactFieldLocalconsultant, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryEmail", String.format("%s (%s)", msg.exporterDataEmail, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryMobile", String.format("%s (%s)", msg.exporterDataMobile, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryPhone", String.format("%s (%s)", msg.exporterDataTelephone, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryFax", String.format("%s (%s)", msg.exporterDataTelefax, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("deliveryAdditionalPhone", String.format("%s (%s)", msg.editorContactFieldAdditionalPhone, msg.commonFieldDeliveryaddress));

            attributeToNameMap.put("discount", msg.commonFieldDiscount);
            attributeToNameMap.put("paymentType", msg.commandPaymentsName);
            attributeToNameMap.put("reliability", msg.editorContactFieldReliabilityName);
            // omitted
            // attributeToNameMap.put("useNetGross", msg.preferencesDocumentUsenetgross);
            attributeToNameMap.put("vatNumber", msg.exporterDataVatno);
            attributeToNameMap.put("vatNumberValid", msg.exporterDataVatnoValid);
            attributeToNameMap.put("website", msg.exporterDataWebsite);
            attributeToNameMap.put("webshopName", msg.commandWebshopName);
            attributeToNameMap.put("supplierNumber", msg.editorContactFieldSuppliernumberName);
            attributeToNameMap.put("gln", msg.contactFieldGln);
            attributeToNameMap.put("mandateReference", msg.editorContactFieldMandaterefName);

            attributeToNameMap.put("account_holder", msg.commonFieldAccountholder);
            attributeToNameMap.put("bankName", msg.editorContactFieldBankName);
            attributeToNameMap.put("iban", msg.exporterDataIban);
            attributeToNameMap.put("bic", msg.exporterDataBic);

            attributeToNameMap.put("registerNumber", msg.contactFieldRegisterNumber);
            attributeToNameMap.put("dateAdded", "dateAdded");
            //          attributeToNameMap.put("modifiedBy", "modifiedBy"); 
            //          attributeToNameMap.put("modified", "modified"); 
            attributeToNameMap.put("note", msg.editorContactLabelNotice);
            attributeToNameMap.put("id", "id");
        }
        return attributeToNameMap;
    }

    public static Map<String, String> getAttributeToNameMap() {
        return attributeToNameMap;
    }

    public static void setAttributeToNameMap(Map<String, String> attributeToNameMap) {
        ContactBeanCSV.attributeToNameMap = attributeToNameMap;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStreet() {
        return street;
    }

    public String getStreet(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryStreet;
        default:
            return street;
        }
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCityAddon() {
        return cityAddon;
    }

    public String getCityAddon(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryCityAddon;
        default:
            return cityAddon;
        }
    }

    public void setCityAddon(String cityAddon) {
        this.cityAddon = cityAddon;
    }

    public String getZip(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryZip;
        default:
            return zip;
        }
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public String getCity(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryCity;
        default:
            return city;
        }
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryCode(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryCountryCode;
        default:
            return countryCode;
        }
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLocalConsultant() {
        return localConsultant;
    }

    public String getLocalConsultant(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryLocalConsultant;
        default:
            return localConsultant;
        }
    }

    public void setLocalConsultant(String localConsultant) {
        this.localConsultant = localConsultant;
    }

    public String getEmail() {
        return email;
    }

    public String getEmail(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryEmail;
        default:
            return email;
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getMobile(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryMobile;
        default:
            return mobile;
        }
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhone(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryPhone;
        default:
            return phone;
        }
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public String getFax(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryFax;
        default:
            return fax;
        }
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getAdditionalPhone() {
        return additionalPhone;
    }

    public String getAdditionalPhone(ContactType type) {
        switch (type) {
        case DELIVERY:
            return deliveryAdditionalPhone;
        default:
            return additionalPhone;
        }
    }

    public void setAdditionalPhone(String additionalPhone) {
        this.additionalPhone = additionalPhone;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getDeliveryStreet() {
        return deliveryStreet;
    }

    public void setDeliveryStreet(String delivery_street) {
        this.deliveryStreet = delivery_street;
    }

    public String getDeliveryCityAddon() {
        return deliveryCityAddon;
    }

    public void setDeliveryCityAddon(String delivery_cityAddon) {
        this.deliveryCityAddon = delivery_cityAddon;
    }

    public String getDeliveryZip() {
        return deliveryZip;
    }

    public void setDeliveryZip(String delivery_zip) {
        this.deliveryZip = delivery_zip;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String delivery_city) {
        this.deliveryCity = delivery_city;
    }

    public String getDeliveryCountryCode() {
        return deliveryCountryCode;
    }

    public void setDeliveryCountryCode(String delivery_countryCode) {
        this.deliveryCountryCode = delivery_countryCode;
    }

    public String getDeliveryLocalConsultant() {
        return deliveryLocalConsultant;
    }

    public void setDeliveryLocalConsultant(String delivery_localConsultant) {
        this.deliveryLocalConsultant = delivery_localConsultant;
    }

    public String getDeliveryEmail() {
        return deliveryEmail;
    }

    public void setDeliveryEmail(String delivery_email) {
        this.deliveryEmail = delivery_email;
    }

    public String getDeliveryMobile() {
        return deliveryMobile;
    }

    public void setDeliveryMobile(String delivery_mobile) {
        this.deliveryMobile = delivery_mobile;
    }

    public String getDeliveryPhone() {
        return deliveryPhone;
    }

    public void setDeliveryPhone(String delivery_phone) {
        this.deliveryPhone = delivery_phone;
    }

    public String getDeliveryFax() {
        return deliveryFax;
    }

    public void setDeliveryFax(String delivery_fax) {
        this.deliveryFax = delivery_fax;
    }

    public String getDeliveryAdditionalPhone() {
        return deliveryAdditionalPhone;
    }

    public void setDeliveryAdditionalPhone(String delivery_additionalPhone) {
        this.deliveryAdditionalPhone = delivery_additionalPhone;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getReliability() {
        return reliability;
    }

    public void setReliability(String reliability) {
        this.reliability = reliability;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public Boolean getVatNumberValid() {
        return vatNumberValid;
    }

    public void setVatNumberValid(Boolean vatNumberValid) {
        this.vatNumberValid = vatNumberValid;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getWebshopName() {
        return webshopName;
    }

    public void setWebshopName(String webshopName) {
        this.webshopName = webshopName;
    }

    public String getSupplierNumber() {
        return supplierNumber;
    }

    public void setSupplierNumber(String supplierNumber) {
        this.supplierNumber = supplierNumber;
    }

    public Long getGln() {
        return gln;
    }

    public void setGln(Long gln) {
        this.gln = gln;
    }

    public String getMandateReference() {
        return mandateReference;
    }

    public void setMandateReference(String mandateReference) {
        this.mandateReference = mandateReference;
    }

    public String getAccount_holder() {
        return account_holder;
    }

    public void setAccount_holder(String account_holder) {
        this.account_holder = account_holder;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bank_name) {
        this.bankName = bank_name;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean hasDeliveryAddress() {
        return deliveryAdditionalPhone != null || deliveryCity != null || deliveryCityAddon != null
                || deliveryCountryCode != null || deliveryEmail != null || deliveryFax != null || deliveryLocalConsultant != null || deliveryMobile != null
                || deliveryPhone != null || deliveryStreet != null || deliveryZip != null;
    }

}
