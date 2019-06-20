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

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.CreditorsDAO;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.resources.core.Icon;

/**
 * Editor for {@link Creditor}s
 */
public class CreditorEditor extends ContactEditor<Creditor> {

	public static final String ID = "com.sebulli.fakturama.editors.creditorEditor";
	public static final String EDITOR_ID = "Creditor";

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
		Creditor createCreditor = modelFactory.createCreditor();
		createCreditor.setContactType(ContactType.BILLING);
		return createCreditor;
	}
	
    @Inject
    @Optional
    @Override
    public void handleForceClose(@UIEventTopic(EDITOR_ID + "/forceClose") Event event) {
    	super.handleForceClose(event);
    }

	@Override
	protected String getEditorID() {
		return EDITOR_ID;
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
}
