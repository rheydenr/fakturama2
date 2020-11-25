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

    private String street, cityAddon, zip, city, countryCode, localConsultant, email, mobile, phone, fax, additionalPhone, birthday;
    private String delivery_street, delivery_cityAddon, delivery_zip, delivery_city, delivery_countryCode, delivery_localConsultant, delivery_email,
            delivery_mobile, delivery_phone, delivery_fax, delivery_additionalPhone, delivery_birthday;

    private Double discount = null;
    private String paymentType = null;
    private String reliability = null;
    private String useNetGross = null;
    private String vatNumber = null;
    private Boolean vatNumberValid = null;
    private String website = null;
    private String webshopName = null;
    private String supplierNumber = null;

    private Long gln = null;
    private String mandateReference = null;

    private String account_holder = null;
    private String bank_name = null;
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
            attributeToNameMap.put("delivery_street", String.format("%s (%s)", msg.commonFieldStreet, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_cityAddon", String.format("%s (%s)", msg.editorContactFieldAddressAddon, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_zip", String.format("%s (%s)", msg.commonFieldZipcode, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_city", String.format("%s (%s)", msg.commonFieldCity, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_countryCode", String.format("%s (%s)", msg.commonFieldCountry, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_localConsultant", String.format("%s (%s)", msg.editorContactFieldLocalconsultant, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_email", String.format("%s (%s)", msg.exporterDataEmail, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_mobile", String.format("%s (%s)", msg.exporterDataMobile, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_phone", String.format("%s (%s)", msg.exporterDataTelephone, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_fax", String.format("%s (%s)", msg.exporterDataTelefax, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_additionalPhone", String.format("%s (%s)", msg.editorContactFieldAdditionalPhone, msg.commonFieldDeliveryaddress));
            attributeToNameMap.put("delivery_birthday", String.format("%s (%s)", msg.editorContactFieldBirthdayName, msg.commonFieldDeliveryaddress));

            attributeToNameMap.put("discount", msg.commonFieldDiscount);
            attributeToNameMap.put("paymentType", msg.commandPaymentsName);
            attributeToNameMap.put("reliability", msg.editorContactFieldReliabilityName);
            attributeToNameMap.put("useNetGross", msg.preferencesDocumentUsenetgross);
            attributeToNameMap.put("vatNumber", msg.exporterDataVatno);
            attributeToNameMap.put("vatNumberValid", msg.exporterDataVatnoValid);
            attributeToNameMap.put("website", msg.exporterDataWebsite);
            attributeToNameMap.put("webshopName", msg.commandWebshopName);
            attributeToNameMap.put("supplierNumber", msg.editorContactFieldSuppliernumberName);
            attributeToNameMap.put("gln", msg.contactFieldGln);
            attributeToNameMap.put("mandateReference", msg.editorContactFieldMandaterefName);

            attributeToNameMap.put("account_holder", msg.commonFieldAccountholder);
            attributeToNameMap.put("bank_name", msg.editorContactFieldBankName);
            attributeToNameMap.put("iban", msg.exporterDataIban);
            attributeToNameMap.put("bic", msg.exporterDataBic);

            //            attributeToNameMap.put("account_holder", msg.commonFieldAccountholder); 
            //            attributeToNameMap.put("account_holder", msg.commonFieldAccountholder); 

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
            return delivery_street;
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
            return delivery_cityAddon;
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
            return delivery_zip;
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
            return delivery_city;
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
            return delivery_countryCode;
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
            return delivery_localConsultant;
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
            return delivery_email;
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
            return delivery_mobile;
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
            return delivery_phone;
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
            return delivery_fax;
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
            return delivery_additionalPhone;
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

    public String getBirthday(ContactType type) {
        switch (type) {
        case DELIVERY:
            return delivery_birthday;
        default:
            return birthday;
        }
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getDelivery_street() {
        return delivery_street;
    }

    public void setDelivery_street(String delivery_street) {
        this.delivery_street = delivery_street;
    }

    public String getDelivery_cityAddon() {
        return delivery_cityAddon;
    }

    public void setDelivery_cityAddon(String delivery_cityAddon) {
        this.delivery_cityAddon = delivery_cityAddon;
    }

    public String getDelivery_zip() {
        return delivery_zip;
    }

    public void setDelivery_zip(String delivery_zip) {
        this.delivery_zip = delivery_zip;
    }

    public String getDelivery_city() {
        return delivery_city;
    }

    public void setDelivery_city(String delivery_city) {
        this.delivery_city = delivery_city;
    }

    public String getDelivery_countryCode() {
        return delivery_countryCode;
    }

    public void setDelivery_countryCode(String delivery_countryCode) {
        this.delivery_countryCode = delivery_countryCode;
    }

    public String getDelivery_localConsultant() {
        return delivery_localConsultant;
    }

    public void setDelivery_localConsultant(String delivery_localConsultant) {
        this.delivery_localConsultant = delivery_localConsultant;
    }

    public String getDelivery_email() {
        return delivery_email;
    }

    public void setDelivery_email(String delivery_email) {
        this.delivery_email = delivery_email;
    }

    public String getDelivery_mobile() {
        return delivery_mobile;
    }

    public void setDelivery_mobile(String delivery_mobile) {
        this.delivery_mobile = delivery_mobile;
    }

    public String getDelivery_phone() {
        return delivery_phone;
    }

    public void setDelivery_phone(String delivery_phone) {
        this.delivery_phone = delivery_phone;
    }

    public String getDelivery_fax() {
        return delivery_fax;
    }

    public void setDelivery_fax(String delivery_fax) {
        this.delivery_fax = delivery_fax;
    }

    public String getDelivery_additionalPhone() {
        return delivery_additionalPhone;
    }

    public void setDelivery_additionalPhone(String delivery_additionalPhone) {
        this.delivery_additionalPhone = delivery_additionalPhone;
    }

    public String getDelivery_birthday() {
        return delivery_birthday;
    }

    public void setDelivery_birthday(String delivery_birthday) {
        this.delivery_birthday = delivery_birthday;
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

    public String getUseNetGross() {
        return useNetGross;
    }

    public void setUseNetGross(String useNetGross) {
        this.useNetGross = useNetGross;
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

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
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
        return delivery_additionalPhone != null || delivery_birthday != null || delivery_city != null || delivery_cityAddon != null
                || delivery_countryCode != null || delivery_email != null || delivery_fax != null || delivery_localConsultant != null || delivery_mobile != null
                || delivery_phone != null || delivery_street != null || delivery_zip != null;
    }

}
