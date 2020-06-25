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

import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherType;
import com.sebulli.fakturama.resources.core.Icon;

public class ExpenditureVoucherEditor extends VoucherEditor {

    // Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.expenditureVoucherEditor";
	
	public static final String EDITOR_ID = "ExpenditureVoucherEditor";
    
    @Inject
    protected ExpendituresDAO expendituresDAO;
    
//    @Inject
//    private EHelpService helpService;

    /**
	 * @return
	 */
	protected String getCustomerSupplierString() {
		return msg.editorVoucherExpenditureFieldSupplier;
	}
	
	/**
	 * @return
	 */
	protected String getEditorTitle() {
		return msg.editorVoucherExpenditureTitle;
	}

	/**
	 * @return
	 */
	public VoucherType getVoucherType() {
		return VoucherType.EXPENDITURE;
	}
	
	protected String getEditorIconURI() {
		return Icon.COMMAND_EXPENDITURE_VOUCHER.getIconURI();
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.parts.VoucherEditor#getEditorId()
	 */
	@Override
	protected String getEditorId() {
		return EDITOR_ID;
	}
    
    @Override
    protected String[] getNameProposals(int maxLength) {
    	return expendituresDAO.getVoucherNames(maxLength);
    }

    @Override
    protected ExpendituresDAO getModelRepository() {
        return expendituresDAO;
    }

    @Override
    protected Class<Voucher> getModelClass() {
        return Voucher.class;
    }
}
