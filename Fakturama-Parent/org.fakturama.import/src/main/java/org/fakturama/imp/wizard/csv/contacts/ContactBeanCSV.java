package org.fakturama.imp.wizard.csv.contacts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EStructuralFeature;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Product;

/**
 * Helper class for importing / exporting {@link Contact} data.
 * 
 */
public class ContactBeanCSV extends Contact {
    
    /**
     * 
     */
    private static final long serialVersionUID = -2909963861955363393L;
    private static Map<String, String> attributeToNameMap;
    private String category = null;
    private String paymentType = null;
   
    public static String getI18NIdentifier(String csvField) {
        return attributeToNameMap.get(csvField);
    }
    
    /**
     * Creates a map of product attributes which maps to name keys (for I18N'ed list entries).
     * Only classifiers for attributes and 1:1 references are used. The map consists of the
     * <i>name</i> attribute of a {@link EStructuralFeature} and the corresponding label entry.
     * <br/>
     * <p>This method is <code>static</code> because the list of product attributes doesn't change.</p>
     * <p><b>IMPORTANT</b> This method has to be updated if new attributes are added to {@link Product} entity.</p>
     */
    public static Map<String, String> createContactsAttributeMap(Messages msg) {
        if(attributeToNameMap == null) {
            attributeToNameMap = new HashMap<>();
            attributeToNameMap.put("alias", msg.editorContactFieldAlias); 
            attributeToNameMap.put("category", msg.commonFieldCategory); 
            attributeToNameMap.put("company", msg.commonFieldCompany); 
            attributeToNameMap.put("customerNumber", msg.commonFieldNumber); 
            attributeToNameMap.put("title", msg.commonFieldTitle); 
            attributeToNameMap.put("firstName", msg.commonFieldFirstname); 
            attributeToNameMap.put("name", msg.commonFieldLastname); 
            attributeToNameMap.put("gender", msg.commonFieldGender); 
            
            attributeToNameMap.put("addresses.billing.name", msg.commonFieldLastname); 
            attributeToNameMap.put("addresses.billing.street", msg.commonFieldStreet); 
            attributeToNameMap.put("addresses.billing.cityAddon", msg.editorContactFieldAddressAddon); 
            attributeToNameMap.put("addresses.billing.zip", msg.commonFieldZipcode); 
            attributeToNameMap.put("addresses.billing.city", msg.commonFieldCity); 
            attributeToNameMap.put("addresses.billing.countryCode", msg.commonFieldCountry); 
            attributeToNameMap.put("addresses.billing.localConsultant", msg.editorContactFieldLocalconsultant); 
            attributeToNameMap.put("addresses.billing.email", msg.exporterDataEmail); 
            attributeToNameMap.put("addresses.billing.mobile", msg.exporterDataMobile); 
            attributeToNameMap.put("addresses.billing.phone", msg.exporterDataTelephone); 
            attributeToNameMap.put("addresses.billing.fax", msg.exporterDataTelefax); 
            attributeToNameMap.put("addresses.billing.additionalPhone", msg.editorContactFieldAdditionalPhone); 
            attributeToNameMap.put("addresses.billing.birthday", msg.editorContactFieldBirthdayName); 
            
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
            
            attributeToNameMap.put("account_holder", msg.commonFieldAccountholder); 
            attributeToNameMap.put("account_holder", msg.commonFieldAccountholder); 
            
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

}
