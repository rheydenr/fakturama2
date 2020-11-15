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
            attributeToNameMap.put("dateAdded", "dateAdded"); 
//          attributeToNameMap.put("modifiedBy", "modifiedBy"); 
//          attributeToNameMap.put("modified", "modified"); 
            attributeToNameMap.put("note", msg.editorContactLabelNotice); 
            attributeToNameMap.put("name", msg.commonFieldName); 
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
