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

import com.sebulli.fakturama.dao.ReceiptVouchersDAO;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherType;

@Deprecated
public class ReceiptVoucherEditor extends VoucherEditor {
	
	// Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.receiptvoucherEditor";
    
    public static final String EDITOR_ID = "Voucher";
    
    @Inject
    protected ReceiptVouchersDAO receiptVouchersDAO;

//	
//	public ReceiptVoucherEditor () {
//		super();
//		tableViewID = ViewReceiptVoucherTable.ID;
//		editorID = "voucher";
//
//		//T: Title of the voucher editor
//		titleText = _("Receipt Voucher");
//		
//		// Text of the name property
//		customerSupplier = 	DataSetReceiptVoucher.CUSTOMERSUPPLIER;
//
//	}
//	
//	/**
//	 * Get all items from the voucher
//	 * 
//	 * @return
//	 * 		All voucher items
//	 */
//	protected List<ReceiptVoucherItem> getVoucherItems() {
//		return ((Voucher)voucher).getItems();
//	}
//
//	protected VoucherCategory getLastUsedCategory() {
//	    return voucherCategoriesDAO.getLastUsedCategoryForReceiptvoucher();
//	}
//	
////	/**
////	 * Get all vouchers
////	 * 
////	 * @return
////	 * 	All vouchers
////	 */
////	public DataSetArray<?> getVouchers() {
////		return Data.INSTANCE.getReceiptVouchers();
////	}
////	
////	/**
////	 * Add a voucher item to the list of all voucher items
////	 * 
////	 * @param item
////	 * 	The new item to add
////	 * @return
////	 *  A Reference to the added item
////	 */
////	public DataSetVoucherItem addVoucherItem(DataSetVoucherItem item) {
////		return Data.INSTANCE.getReceiptVoucherItems().addNewDataSet(
////				new DataSetReceiptVoucherItem((DataSetReceiptVoucherItem) item));
////	}
////	
////	/**
////	 * Add a voucher to the list of all vouchers
////	 * 
////	 * @param voucher
////	 * 	The new voucher to add
////	 * @return
////	 *  A Reference to the added voucher
////	 */
////	public DataSetVoucher addVoucher(DataSetVoucher voucher) {
////		return Data.INSTANCE.getReceiptVouchers().addNewDataSet((DataSetReceiptVoucher) voucher);
////	}
    
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
    protected MDirtyable getMDirtyablePart() {
        return part;
    }

    @Override
    protected Class<Voucher> getModelClass() {
        return Voucher.class;
    }

//    @Override
//    protected IEntity createNewVoucherItem(IEntity item) {
//        // TODO Auto-generated method stub
//        return null;
//    }
	
	
}
