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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Contact_;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.resources.core.Icon;

/**
 * Editor for {@link Debtor}s.
 */
public class DebitorEditor extends ContactEditor<Debitor> {

	public static final String ID = "com.sebulli.fakturama.editors.debtorEditor";
	public static final String EDITOR_ID = "Debtor";

	@Inject
	private DebitorsDAO contactDAO;
	Button sqtButton;

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
		Debitor debitor = modelFactory.createDebitor();
//		debitor.setContactType(ContactType.BILLING);
		return debitor;
	}
	
	@Override
	protected void createAdditionalFields(Composite tabMisc) {
		if (defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX)) {
			/*Label emptyLbl = */new Label(tabMisc, SWT.NONE);
			sqtButton = new Button(tabMisc, SWT.CHECK);
			sqtButton.setText(msg.editorContactFieldSalesequalizationtaxName);
		}
	}
	
	@Override
	protected void bindAdditionalValues(Debitor editorContact) {
		if (defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX)) {
			bindModelValue(editorContact, sqtButton, Contact_.useSalesEqualizationTax.getName());
		}
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
}
