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
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.resources.core.Icon;

/**
 *
 */
public class DebitorEditor extends ContactEditor<Debitor> {

	public static final String ID = "com.sebulli.fakturama.editors.debtorEditor";
	public static final String EDITOR_ID = "Debtor";

	@Inject
	private DebitorsDAO contactDAO;

	@Override
	protected Class<Debitor> getModelClass() {
		return Debitor.class;
	}

	@Override
	protected AbstractDAO<Debitor> getContactsDao() {
		return contactDAO;
	}

	@Override
	protected Debitor createNewContact(FakturamaModelFactory modelFactory) {
		return modelFactory.createDebitor();
	}
	
	protected String getEditorIconURI() {
		return Icon.COMMAND_CONTACT.getIconURI();
	}

    
    /**
     * If an entity is deleted via list view we have to close a possibly open
     * editor window. Since this is triggered by a UIEvent we named this method
     * "handle*".
     */
    @Inject
    @Optional
    public void handleForceClose(@UIEventTopic(EDITOR_ID + "/forceClose") Event event) {
    	super.handleForceClose(event);
    }


	@Override
	protected String getEditorID() {
		return EDITOR_ID;
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.parts.ContactEditor#setPartLabelForNewContact(org.eclipse.e4.ui.model.application.ui.basic.MPart)
	 */
	@Override
	protected void setPartLabelForNewContact(MPart part) {
		part.setLabel(msg.commandNewDebtorName);
	}
	
	@Override
	protected Debitor getDeliveryContact() {
		return (Debitor) editorContact.getAlternateContacts();
	}
}
