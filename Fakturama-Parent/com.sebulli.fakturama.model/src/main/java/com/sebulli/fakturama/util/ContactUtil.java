/**
 * 
 */
package com.sebulli.fakturama.util;

import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Contact;

/**
 * Utility class for some additional useful methods for the {@link Contact}s.
 *
 */
public class ContactUtil {

    /**
     * the name of the company (if any) and the name of the contact (deliverer or contact)
     * 
     * @param contact the {@link Contact}
     * @param useDelivery if the delivery address should be used
     * @return the concatenated name of the company and the contact
     */
    public static String getNameWithCompany(Contact contact, boolean useDelivery) {
        String line = "";
        // use delivery attribute only if it's not null!
        String deliveryCompany = contact.getDeliveryContact() != null ? contact.getDeliveryContact().getCompany() : null;
        String deliveryFirstName = contact.getDeliveryContact() != null ? contact.getDeliveryContact().getFirstName() : null;
        String deliveryLastName = contact.getDeliveryContact() != null ? contact.getDeliveryContact().getName() : null;
        if (!getSelectedAttribute(contact.getCompany(), deliveryCompany, useDelivery).isEmpty()) {
            line = DataUtils.getSingleLine(getSelectedAttribute(contact.getCompany(), deliveryCompany, useDelivery));
            if ((!getSelectedAttribute(contact.getFirstName(), deliveryFirstName, useDelivery).isEmpty()) || 
                (!getSelectedAttribute(contact.getName(), deliveryLastName, useDelivery).isEmpty()) )
                line +=", ";
        }

        line += getFirstAndLastName(contact, useDelivery);
        return line;
    }
    
    /**
     * If we use a delivery address, return the delivery attribute. Else return the normal attribute.
     * 
     * @param attr1 normal attribute
     * @param attr2 delivery attribute
     * @param useDelivery decide which attribute we should use
     * @return chosen attribute
     */
    private static String getSelectedAttribute(String attr1, String attr2, boolean useDelivery) {
        return useDelivery ? attr2 : attr1;
    }
    
    /**
     * Get the first and the last name
     * 
     * @return First and last name
     */
    public static String getFirstAndLastName(Contact contact, boolean useDelivery) {
        String line = "";
        String deliveryFirstName = contact.getDeliveryContact() != null ? contact.getDeliveryContact().getFirstName() : null;
        String deliveryLastName = contact.getDeliveryContact() != null ? contact.getDeliveryContact().getName() : null;
        if (!getSelectedAttribute(contact.getFirstName(), deliveryFirstName, useDelivery).isEmpty()) {
            line += getSelectedAttribute(contact.getFirstName(), deliveryFirstName, useDelivery);
        }
        
        if (!getSelectedAttribute(contact.getName(), deliveryLastName, useDelivery).isEmpty()) {
            if (!line.isEmpty())
                line += " ";
            line += getSelectedAttribute(contact.getName(), deliveryLastName, useDelivery);
        }

        return line;
    }
}
