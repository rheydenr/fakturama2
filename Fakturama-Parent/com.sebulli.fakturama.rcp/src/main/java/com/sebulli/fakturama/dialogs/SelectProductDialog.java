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

package com.sebulli.fakturama.dialogs;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sebulli.fakturama.Activator;

/**
 * Dialog to select a product from a table
 * 
 * @author Gerd Bartelt
 */
public class SelectProductDialog extends SelectDataSetDialog {

//	/**
//	 * Constructor
//	 * 
//	 * @param string
//	 *            Dialog title
//	 */
//	public SelectProductDialog(String string) {
//		super(string, true);
//	}
//
//	/**
//	 * Create the dialog area
//	 * 
//	 * @param parent
//	 *            Parent composite
//	 * @return The new created dialog area
//	 */
//	@Override
//	protected Control createDialogArea(Composite parent) {
//
//		// Mark the columns that are used by the search function.
//		searchColumns = new String[4];
//		searchColumns[0] = "itemnr";
//		searchColumns[1] = "category";
//		searchColumns[2] = "name";
//		searchColumns[3] = "description";
//
//		// Create the dialog area
//		Control control = super.createDialogArea(parent);
//
//		// Set the content provider
//		tableViewer.setContentProvider(new ViewDataSetTableContentProvider(tableViewer));
//		tableViewer.setSorter(new TableSorter());
//
//		// Get the column width from the preferences
//		int cw_itemno = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_PRODUCTS_ITEMNO");
//		int cw_name = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_PRODUCTS_NAME");
//		int cw_description = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_PRODUCTS_DESCRIPTION");
//		int cw_price = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_PRODUCTS_PRICE");
//		int cw_vat = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_PRODUCTS_VAT");
//		
//		// Create the table columns
//		if (Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_ITEMNR"))
//			//T: Used as heading of a table. Keep the word short.
//			new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, _("Item No."), cw_itemno, true, "itemnr");
//
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("Category"), cw_name, false, "category");
//		
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("Name"), cw_name, false, "name");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("Description"), cw_description, false, "description");
//		if (Activator.getDefault().getPreferenceStore().getInt("DOCUMENT_USE_NET_GROSS") == 1)
//			//T: Used as heading of a table. Keep the word short.
//			new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, _("Price"), cw_price, true, "$Price1Gross");
//		else
//			//T: Used as heading of a table. Keep the word short.
//			new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, _("Price"), cw_price, true, "$Price1Net");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, _("VAT"), cw_vat, true, "$vatbyid");
//
//		// Set the input
//		tableViewer.setInput(Data.INSTANCE.getProducts());
//
//		return control;
//	}

}
