/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2013 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ralf Heydenreich - initial API and implementation
 */ 
package com.sebulli.fakturama.dbconnector;

/**
 * Information about the old entities (tables).
 * 
 * @author R. Heydenreich
 *
 */
public enum OldTableinfo {
	Contacts("command.contacts.name"), 
	Documents("command.documents.name"), 
	Expenditureitems("data.expenditure.items"), 
	Expenditures("command.expenditurevouchers.name"), 
	Items("editor.document.items"), 
	Lists("command.lists.name"), 
	Payments("command.payments.name"), 
	Products("command.products.name"), 
	Properties("main.menu.file.openpreferences"), 
	Receiptvoucheritems("data.receiptvoucher.items"), 
	Receiptvouchers("command.receiptvouchers.name"), 
	Shippings("command.shippings.name"), 
	Texts("command.texts.name"), 
	Vats("command.vats.name");
	
	private String messageKey;
	
	/**
	 * @param messageKey
	 */
	private OldTableinfo(String messageKey) {
		this.messageKey = messageKey;
	}



	/**
	 * @return the messageKey
	 */
	public String getMessageKey() {
		return messageKey;
	}
	
	
}
