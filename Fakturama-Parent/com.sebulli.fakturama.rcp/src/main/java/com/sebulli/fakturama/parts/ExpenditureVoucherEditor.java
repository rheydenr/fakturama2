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
import javax.xml.crypto.Data;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.AbstractVoucher;
import com.sebulli.fakturama.model.Expenditure;
import com.sebulli.fakturama.model.IEntity;

public class ExpenditureVoucherEditor extends VoucherEditor<Expenditure> {
	
	// Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.expenditureVoucherEditor";
	
	public static final String EDITOR_ID = "ExpenditureVoucher";

    @Inject
    protected IPreferenceStore defaultValuePrefs;
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    protected Logger log;

//	public ExpenditureVoucherEditor () {
//		super();
//		tableViewID = ViewExpenditureVoucherTable.ID;
//		editorID = "voucher";
//		
//		//T: Title of the voucher editor
//		titleText = _("Expense Voucher");
//
//		// Text of the name property
//		customerSupplier = 	DataSetExpenditureVoucher.CUSTOMERSUPPLIER;
//	}
	
	@Override
	protected Class getModelClass() {
	    return Expenditure.class;
	}
//	
//	/**
//	 * Get all items from the voucher
//	 * 
//	 * @return
//	 * 		All voucher items
//	 */
//	public DataSetArray<?> getVoucherItems() {
//		return Data.INSTANCE.getExpenditureVoucherItems();
//	}
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
//	
//	/**
//	 * Add a voucher to the list of all vouchers
//	 * 
//	 * @param voucher
//	 * 	The new voucher to add
//	 * @return
//	 *  A Reference to the added voucher
//	 */
//	public DataSetVoucher addVoucher(DataSetVoucher voucher) {
//		return Data.INSTANCE.getExpenditureVouchers().addNewDataSet((DataSetExpenditureVoucher) voucher);
//	}
//
//	/**
//	 * Updates a voucher item
//	 * 
//	 * @param item
//	 * 		The voucher item to update
//	 */
//	public void updateVoucherItem(DataSetVoucherItem item) {
//		Data.INSTANCE.getExpenditureVoucherItems().updateDataSet((DataSetExpenditureVoucherItem) item);
//	}
//
//	/**
//	 * Updates a voucher
//	 * 
//	 * @param voucher
//	 * 		The voucher to update
//	 */
//	public void updateVoucher(DataSetVoucher voucher) {
//		Data.INSTANCE.getExpenditureVouchers().updateDataSet((DataSetExpenditureVoucher) voucher);
//	}
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
//	public DataSetVoucherItem createNewVoucherItem(String name, String category, Double price, int vatId) {
//		return new DataSetExpenditureVoucherItem(name, category,price, vatId);
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
//	public DataSetVoucherItem createNewVoucherItem (DataSetVoucherItem item) {
//		return new DataSetExpenditureVoucherItem(item); 
//	}
//
//	/**
//	 * Creates a new voucher
//	 * 
//	 * @param input
//	 * 	The editors input
//	 * @return
//	 * 	The created voucher
//	 */
//	public DataSetVoucher createNewVoucher (IEditorInput input) {
//		return new DataSetExpenditureVoucher(((UniDataSetEditorInput) input).getCategory());
//	}
//
//	/**
//	 * Creates a new array for voucher items
//	 * 
//	 * @return
//	 * 	Array with all voucher items
//	 */
//	public DataSetArray<?> createNewVoucherItems () {
//		return new DataSetArray<DataSetExpenditureVoucherItem>();
//	}
//
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
    protected AbstractDAO getModelRepository() {
        // TODO Auto-generated method stub
        return null;
    }
	
	
}
