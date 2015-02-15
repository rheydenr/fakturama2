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
 * Dialog to select a delivery note from a table
 * 
 * @author Gerd Bartelt
 */
public class SelectDeliveryNoteDialog extends SelectDataSetDialog {

//	protected ViewDataSetTableContentProvider contentProvider;
//	protected int contactID = -1;
//	
//	/**
//	 * Constructor
//	 * 
//	 * @param string
//	 *            Dialog title
//	 */
//	public SelectDeliveryNoteDialog(String string, int contactID) {
//		super(string, true);
//		this.contactID = contactID;
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
//		searchColumns[0] = "name";
//		searchColumns[1] = "date";
//		searchColumns[2] = "addressfirstline";
//		searchColumns[3] = "total";
//
//		// Create the dialog area
//		Control control = super.createDialogArea(parent);
//
//		// Set the content provider
//		contentProvider = new ViewDataSetTableContentProvider(tableViewer);
//		contentProvider.setTransactionFilter(-1);
//		contentProvider.setContactFilter(contactID);
//		contentProvider.setCategoryFilter(DocumentType.getPluralString(DocumentType.DELIVERY) + "/" + DataSetDocument.getStringHASNOINVOICE());
//		tableViewer.setContentProvider(contentProvider);
//		tableViewer.setSorter(new TableSorter());
//
//		// Get the column width from the preferences
//		int cw_icon = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DOCUMENTS_ICON");
//		int cw_document = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DOCUMENTS_DOCUMENT");
//		int cw_date = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DOCUMENTS_DATE");
//		int cw_name = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DOCUMENTS_NAME");
//		
//		// Create the table columns
//		// new TableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, "ID", 30, 0, true, "id");
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, "", cw_icon,  true, "$documenttype");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, _("Document"), cw_document, true, "name");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, _("Date"), cw_date, true, "date");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("Name"), cw_name, false, "addressfirstline");
//
//		// Set the input
//		tableViewer.setInput(Data.INSTANCE.getDocuments());
//		
//		return control;
//	}

}
