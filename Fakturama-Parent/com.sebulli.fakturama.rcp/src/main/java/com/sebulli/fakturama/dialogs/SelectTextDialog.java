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
 * Dialog to select a text from a table
 * 
 * @author Gerd Bartelt
 */
public class SelectTextDialog extends SelectDataSetDialog {

//	/**
//	 * Constructor
//	 * 
//	 * @param string
//	 *            Dialog title
//	 */
//	public SelectTextDialog(String string) {
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
//
//		// Mark the columns that are used by the search function.
//		searchColumns = new String[2];
//		searchColumns[0] = "name";
//		searchColumns[1] = "text";
//
//		// Create the dialog area
//		Control control = super.createDialogArea(parent);
//
//		// Set the content provider
//		tableViewer.setContentProvider(new ViewDataSetTableContentProvider(tableViewer));
//
//		// Get the column width from the preferences
//		int cw_name = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_TEXTS_NAME");
//		int cw_text = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_DIALOG_TEXTS_TEXT");
//
//		// Create the table columns
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("Name"), cw_name, true, "name");
//		//T: Used as heading of a table. Keep the word short.
//		new UniDataSetTableColumn(tableColumnLayout, tableViewer, SWT.LEFT, _("Text"), cw_text, false, "text");
//
//		// Set the input
//		tableViewer.setInput(Data.INSTANCE.getTexts());
//
//		return control;
//	}
//
}
