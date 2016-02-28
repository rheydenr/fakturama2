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

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.FakturamaModelFactory;

/**
 *
 */
public class DebitorEditor extends ContactEditor<Debitor> {
	
	public static final String ID = "com.sebulli.fakturama.editors.debitorEditor";
	
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

@Override
protected String getEditorID() {
    return "Debitor";
}


}
