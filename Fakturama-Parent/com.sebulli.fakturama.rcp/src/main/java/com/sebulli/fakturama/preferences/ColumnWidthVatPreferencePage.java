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

package com.sebulli.fakturama.preferences;


import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.opcoach.e4.preferences.ScopedPreferenceStore;
import com.sebulli.fakturama.Activator;
//import com.sebulli.fakturama.ContextHelpConstants;
import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the width of the table columns
 * 
 * @author Gerd Bartelt
 */
public class ColumnWidthVatPreferencePage extends FieldEditorPreferencePage {
    
    @Inject
    @Translation
    protected Messages msg;

	/**
	 * Constructor
	 */
	public ColumnWidthVatPreferencePage() {
		super(GRID);

	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		
		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.COLUMN_WIDTH_PREFERENCE_PAGE);

		
		//T: Preference page "Column width" - Use the same text as in the heading of the corresponding table
		addField(new IntegerFieldEditor("COLUMNWIDTH_VATS_STANDARD", "Standard", getFieldEditorParent()));
		//T: Preference page "Column width" - Use the same text as in the heading of the corresponding table
		addField(new IntegerFieldEditor("COLUMNWIDTH_VATS_NAME", "Name", getFieldEditorParent()));
		//T: Preference page "Column width" - Use the same text as in the heading of the corresponding table
		addField(new IntegerFieldEditor("COLUMNWIDTH_VATS_DESCRIPTION", "Description", getFieldEditorParent()));
		//T: Preference page "Column width" - Use the same text as in the heading of the corresponding table
		addField(new IntegerFieldEditor("COLUMNWIDTH_VATS_VALUE", "Value", getFieldEditorParent()));
	
	}

//	/**
//	 * Initializes this preference page for the given workbench.
//	 * 
//	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
//	 */
//	@Override
//	public void init(IWorkbench workbench) {
//        IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "com.opcoach.e4.preferences.example");   
//		setPreferenceStore(Activator.getContext().getPreferenceStore());
//		//T: Preference page - Title
//		setDescription(_("Column width of the VAT table"));
//	}

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase("COLUMNWIDTH_VATS_STANDARD", write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase("COLUMNWIDTH_VATS_NAME", write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase("COLUMNWIDTH_VATS_DESCRIPTION", write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase("COLUMNWIDTH_VATS_VALUE", write);
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public static void setInitValues(IPreferenceStore node) {
		node.setDefault("COLUMNWIDTH_VATS_STANDARD", "55");
		node.setDefault("COLUMNWIDTH_VATS_NAME", "120");
		node.setDefault("COLUMNWIDTH_VATS_DESCRIPTION", "200");
		node.setDefault("COLUMNWIDTH_VATS_VALUE", "70");
	}

}
