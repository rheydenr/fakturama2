/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.dto;

import java.util.Calendar;
import java.util.Date;

import org.osgi.dto.DTO;

import com.sebulli.fakturama.model.AbstractVoucher;
import com.sebulli.fakturama.model.Document;

/**
 * Container for all account entries (from Vouchers or Documents).
 * 
 */
public class AccountEntry extends DTO {
	
	public static final short EXPENDITURE_SIGN = -1;
	public static final short RECEIPTVOUCHER_SIGN = 1;
	
	public Date date;
	public String name; 
	public String text;
	public double value;
	
	/**
	 * Constructor Creates an account entry
	 * 
	 */
	public AccountEntry() {
		this(Calendar.getInstance().getTime(), "", "" , 0.0);
	}

	/**
	 * Constructor Creates an account entry from a voucher
	 * 
	 */
	public AccountEntry(AbstractVoucher voucher, short sign) {
		this(voucher.getVoucherDate(), voucher.getName(),
				voucher.getVoucherNumber() +  "  " + voucher.getDocumentNumber(),
				voucher.getPaidValue() * sign);
	}

	
	/**
	 * Constructor Creates an account entry from a document
	 * 
	 */
	public AccountEntry(Document document) {
		this(document.getPayDate(), document.getAddressFirstLine(),
				document.getName(),
				document.getPaidValue());
	}

	/**
	 * Constructor Creates an account entry
	 * @param date
	 * @param name
	 * 		Customer or supplier
	 * @param text
	 * @param value
	 */
	public AccountEntry(Date date, String name, String text, double value){
		
		this.date = date;
		this.name = name;
		this.text = text;
		this.value = value;
	}
}
