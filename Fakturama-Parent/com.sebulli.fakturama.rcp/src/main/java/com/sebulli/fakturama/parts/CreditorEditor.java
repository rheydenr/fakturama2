/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.parts;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.CreditorsDAO;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.resources.core.Icon;

/**
 *
 */
public class CreditorEditor extends ContactEditor<Creditor> {

	public static final String ID = "com.sebulli.fakturama.editors.creditorEditor";
	public static final String EDITOR_ID = "CreditorEditor";

	@Inject
	private CreditorsDAO contactDAO;

	@Override
	protected Class<Creditor> getModelClass() {
		return Creditor.class;
	}

	@Override
	protected AbstractDAO<Creditor> getContactsDao() {
		return contactDAO;
	}

	@Override
	protected Creditor createNewContact(FakturamaModelFactory modelFactory) {
		return modelFactory.createCreditor();
	}

	@Override
	protected String getEditorID() {
		return CreditorEditor.EDITOR_ID;
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.parts.ContactEditor#getEditorIconURI()
	 */
	@Override
	protected String getEditorIconURI() {
		return Icon.COMMAND_VENDOR.getIconURI();
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.parts.ContactEditor#setPartLabelForNewContact(org.eclipse.e4.ui.model.application.ui.basic.MPart)
	 */
	@Override
	protected void setPartLabelForNewContact(MPart part) {
		part.setLabel(msg.commandNewCreditorName);
	}
	
	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.parts.ContactEditor#getDeliveryContact()
	 */
	@Override
	protected Creditor getDeliveryContact() {
		return (Creditor) editorContact.getAlternateContacts();
	}
}
