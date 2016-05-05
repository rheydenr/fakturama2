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

import org.eclipse.e4.ui.model.application.ui.MDirtyable;

import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherType;

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
		return msg.expenditurevoucherFieldSupplier;
	}

//
//	/**
//	 * Get all vouchers
//	 * 
//	 * @return
//	 * 	All vouchers
//	 */
//	public DataSetArray<?> getVouchers() {
//		return Data.INSTANCE.getExpenditureVouchers();
//	}
//	
//	/**
//	 * Add a voucher item to the list of all voucher items
//	 * 
//	 * @param item
//	 * 	The new item to add
//	 * @return
//	 *  A Reference to the added item
//	 */
//	public DataSetVoucherItem addVoucherItem(DataSetVoucherItem item) {
//		return Data.INSTANCE.getExpenditureVoucherItems().addNewDataSet((DataSetExpenditureVoucherItem) item);
//	}
	
	/**
	 * @return
	 */
	protected String getEditorTitle() {
		return msg.expenditurevoucherEditorTitle;
	}

	/**
	 * @return
	 */
	protected VoucherType getVoucherType() {
		return VoucherType.EXPENDITURE;
	}
	
	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.parts.VoucherEditor#getEditorId()
	 */
	@Override
	protected String getEditorId() {
		return EDITOR_ID;
	}

//	/**
//	 * Gets the temporary voucher items
//	 * 
//	 * @return
//	 * 	The temporary items
//	 */
//	public DataSetArray<?> getMyVoucherItems() {
//		return voucherItems;
//	}
//
//	/**
//	 * Creates the SWT controls for this workbench part
//	 * 
//	 * @param the
//	 *            parent control
//	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
//	 */
//	@SuppressWarnings("unchecked")
//	public void createPartControl(Composite parent) {
//		super.createPartControl(parent, ContextHelpConstants.VOUCHER_EDITOR);
//		// Fill the table with the items
//		tableViewerItems.setInput((DataSetArray<DataSetExpenditureVoucherItem>) getMyVoucherItems());
//	}
    

    @Override
    protected ExpendituresDAO getModelRepository() {
        return expendituresDAO;
    }
	
    
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }

    @Override
    protected Class<Voucher> getModelClass() {
        return Voucher.class;
    }

}
