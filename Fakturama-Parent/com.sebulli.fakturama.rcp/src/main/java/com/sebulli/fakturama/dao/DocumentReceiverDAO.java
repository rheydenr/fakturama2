/**
 * 
 */
package com.sebulli.fakturama.dao;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

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
	private ContactsDAO contactsDAO;
	
	@Inject
	private IDocumentAddressManager addressManager;

	@Override
	protected Class<DocumentReceiver> getEntityClass() {
		return DocumentReceiver.class;
	}
	
    public boolean isSETEnabled(Document document) {
		DocumentReceiver billingAdress = addressManager.getBillingAdress(document);
		Boolean useSalesEqualizationTax = Boolean.FALSE;
		if(billingAdress != null && billingAdress.getOriginContactId() != null) {
			Contact originContact = contactsDAO.findById(billingAdress.getOriginContactId());
			useSalesEqualizationTax = originContact.getUseSalesEqualizationTax();
		}
		return document != null && addressManager.getBillingAdress(document) != null && BooleanUtils.isTrue(useSalesEqualizationTax);
	}

}
