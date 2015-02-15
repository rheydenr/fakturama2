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
 * Dialog to select a contact from a table
 * 
 * @author Gerd Bartelt
 */
public class SelectContactDialog {

//	/**
//	 * Constructor
//	 * 
//	 * @param string
//	 *            Dialog title
//	 */
//	public SelectContactDialog(String string) {
//		super(string, false);
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
//		// Mark the columns that are used by the search function.
//		searchColumns = new String[6];
//		searchColumns[0] = "nr";
//		searchColumns[1] = "firstname";
//		searchColumns[2] = "name";
//		searchColumns[3] = "company";
//		searchColumns[4] = "zip";
//		searchColumns[5] = "city";
//
//		// Create the dialog area
//		Control control = super.createDialogArea(parent);
//
//		// Set the content provider
//		tableViewer.setContentProvider(new ViewDataSetTableContentProvider(tableViewer));
//		tableViewer.setSorter(new TableSorter());
//
//		// Get the column width from the preferences
//		int cw_no = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_CONTACTS_NO");
//		int cw_firstname = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_CONTACTS_FIRSTNAME");
//		int cw_lastname = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_CONTACTS_LASTNAME");
//		int cw_company = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_CONTACTS_COMPANY");
//		int cw_zip = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_CONTACTS_ZIP");
//		int cw_city = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_CONTACTS_CITY");
//
//		// Create the table columns
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.RIGHT,_("Customer ID"), cw_no, true, "nr");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("First Name"), cw_firstname, false, "firstname");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("Last Name"), cw_lastname, false, "name");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("Company"), cw_company, false, "company");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("ZIP"), cw_zip, true, "zip");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("City"), cw_city, false, "city");
//
//		// Set the input
//		tableViewer.setInput(Data.INSTANCE.getContacts());
//
//		return control;
//	}
//
}
