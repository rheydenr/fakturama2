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

import java.util.List;

import javax.inject.Inject;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.model.AbstractVoucher;
import com.sebulli.fakturama.model.VoucherItem;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.model.ReceiptVoucher;
import com.sebulli.fakturama.model.VoucherCategory;

@Deprecated
public class ReceiptVoucherEditor/* extends VoucherEditor<ReceiptVoucher>*/ {
	
	// Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.receiptvoucherEditor";
    
    public static final String EDITOR_ID = "ReceiptVoucher";


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
//		return ((ReceiptVoucher)voucher).getItems();
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
////
////	/**
////	 * Updates a voucher item
////	 * 
////	 * @param item
////	 * 		The voucher item to update
////	 */
////	public void updateVoucherItem(DataSetVoucherItem item) {
////		Data.INSTANCE.getReceiptVoucherItems().updateDataSet((DataSetReceiptVoucherItem) item);
////	}
////
////	/**
////	 * Updates a voucher
////	 * 
////	 * @param voucher
////	 * 		The voucher to update
////	 */
////	public void updateVoucher(DataSetVoucher voucher) {
////		Data.INSTANCE.getReceiptVouchers().updateDataSet((DataSetReceiptVoucher) voucher);
////	}
//	
//	/**
//	 * Creates a new voucher item 
//     *
//	 * @param name
//	 * 	Data to create the item
//	 * @param category
//	 * 	Data to create the item
//	 * @param price
//	 * 	Data to create the item
//	 * @param vatId
//	 * 	Data to create the item
//	 * @return
//	 * 	The created item
//	 */
//	public ReceiptVoucherItem createNewVoucherItem(String name, String category, Double price, int vatId) {
////	    modelFactory.//
//		return null;// new DataSetReceiptVoucherItem(name, category,price, vatId);
//	}
//	
//	/**
//	 * Creates a new voucher item by a parent item
//	 * 
//	 * @param item
//	 * 	The parent item
//	 * @return
//	 * 	The created item
//	 */
//	public ReceiptVoucherItem createNewVoucherItem (ReceiptVoucherItem item) {
//		return null; //new ReceiptVoucherItem(item); 
//	}
//
//
//	@Override
//	protected AbstractVoucher createNewVoucher() {
//	    return null;//modelFactory.cr;
//	}
//	
////
////	/**
////	 * Creates a new array for voucher items
////	 * 
////	 * @return
////	 * 	Array with all voucher items
////	 */
////	public DataSetArray<?> createNewVoucherItems () {
////		return new DataSetArray<DataSetReceiptVoucherItem>();
////	}
//
////	/**
////	 * Gets the temporary voucher items
////	 * 
////	 * @return
////	 * 	The temporary items
////	 */
////	public DataSetArray<?> getMyVoucherItems() {
////		return voucherItems;
////	}
////
////	/**
////	 * Creates the SWT controls for this workbench part
////	 * 
////	 * @param the
////	 *            parent control
////	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
////	 */
////	@SuppressWarnings("unchecked")
////	public void createPartControl(Composite parent) {
////		super.createPartControl(parent, ContextHelpConstants.VOUCHER_EDITOR);
////		// Fill the table with the items
////		tableViewerItems.setInput((DataSetArray<DataSetReceiptVoucherItem>) getMyVoucherItems());
////	}
//
//    @Override
//    protected AbstractDAO getModelRepository() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    protected Class getModelClass() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    protected IEntity createNewVoucherItem(IEntity item) {
//        // TODO Auto-generated method stub
//        return null;
//    }
	
	
}
