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

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the sales export functionality
 * 
 * @author Gerd Bartelt
 */
public class ExportPreferencePage extends FieldEditorPreferencePage {
    
    /**
     * 
     */
    public static final String PREFERENCES_EXPORTSALES_PAIDDATE = "EXPORTSALES_PAIDDATE";
    
    @Inject
    @Translation
    protected Messages msg;

	/**
	 * Constructor
	 */
	public ExportPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.EXPORT_PREFERENCE_PAGE);

		//T: Preference page "Export Sales" - Label "Use Pay Date instead of Invoice Date"
		addField(new BooleanFieldEditor(PREFERENCES_EXPORTSALES_PAIDDATE, msg.preferencesExportUsepaydate, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.preferencesExport;
	}

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_EXPORTSALES_PAIDDATE, write);
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public static void setInitValues(IPreferenceStore node) {
		node.setDefault(PREFERENCES_EXPORTSALES_PAIDDATE, true);

	}

}
