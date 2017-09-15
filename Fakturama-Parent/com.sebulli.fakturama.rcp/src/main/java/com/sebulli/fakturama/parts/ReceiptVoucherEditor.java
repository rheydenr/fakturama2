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

package com.sebulli.fakturama.parts;

import javax.inject.Inject;

import com.sebulli.fakturama.dao.ReceiptVouchersDAO;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherType;

//@Deprecated
public class ReceiptVoucherEditor extends VoucherEditor {
	
	// Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.receiptVoucherEditor";
    
    public static final String EDITOR_ID = "ReceiptVoucherEditor";
    
    @Inject
    protected ReceiptVouchersDAO receiptVouchersDAO;
    
    /* (non-Javadoc)
     * @see com.sebulli.fakturama.parts.VoucherEditor#getEditorTitle()
     */
    @Override
    protected String getEditorTitle() {
    	return msg.receiptvoucherEditorTitle;
    }

	/**
	 * @return
	 */
	protected String getCustomerSupplierString() {
		return msg.receiptvoucherFieldCustomer;
	}

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.parts.VoucherEditor#getVoucherType()
     */
    @Override
    protected VoucherType getVoucherType() {
    	return VoucherType.RECEIPTVOUCHER;
    }


    @Override
    protected ReceiptVouchersDAO getModelRepository() {
        return receiptVouchersDAO;
    }

    @Override
    protected Class<Voucher> getModelClass() {
        return Voucher.class;
    }
    
	@Override
	protected String getEditorId() {
		return EDITOR_ID;
	}

}
