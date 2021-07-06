/**
 * 
 */
package com.sebulli.fakturama.dao;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IDocumentAddressManager;

/**
 *
 */
@Creatable
public class DocumentReceiverDAO extends AbstractDAO<DocumentReceiver> {

    @Inject
    IPreferenceStore defaultValuePrefs;

    @Inject
    private ContactsDAO contactsDAO;

    @Inject
    private IDocumentAddressManager addressManager;

    @Override
    protected Class<DocumentReceiver> getEntityClass() {
        return DocumentReceiver.class;
    }

    public boolean isSETEnabled(Document document) {
        if (document != null) {
            // this is only the general availability of SET, the concrete use of it depends on the debtor!
            boolean useSETPreference = this.defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX);

            DocumentReceiver billingAdress = addressManager.getBillingAdress(document);
            Boolean useSalesEqualizationTax = Boolean.FALSE;
            if (billingAdress != null && billingAdress.getOriginContactId() != null && billingAdress.getOriginContactId() > 0) {
                Contact originContact = contactsDAO.findById(billingAdress.getOriginContactId());
                useSalesEqualizationTax = originContact.getUseSalesEqualizationTax();
            }
            return useSETPreference && document != null && addressManager.getBillingAdress(document) != null && BooleanUtils.isTrue(useSalesEqualizationTax);
        } else {
            return false;
        }
    }

}
