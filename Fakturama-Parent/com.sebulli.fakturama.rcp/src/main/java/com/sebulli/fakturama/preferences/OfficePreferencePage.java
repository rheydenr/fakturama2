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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import com.opcoach.e4.preferences.ScopedPreferenceStore;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.OSDependent;
import com.sebulli.fakturama.office.OfficeStarter;

/**
 * Preference page for the Office settings
 * 
 * @author Gerd Bartelt
 */
public class OfficePreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {

	/**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD = "OPENOFFICE_START_IN_NEW_THREAD";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT = "OPENOFFICE_PDF_PATH_FORMAT";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_ODT_PATH_FORMAT = "OPENOFFICE_ODT_PATH_FORMAT";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_ODT_PDF = "OPENOFFICE_ODT_PDF";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_PATH = "OPENOFFICE_PATH";
    
    @Inject
    @Translation
    protected Messages msg;

    @Inject
    @Preference(value=InstanceScope.SCOPE)
    private IPreferenceStore preferences;

    /**
	 * Constructor
	 */
	public OfficePreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.OPENOFFICE_PREFERENCE_PAGE);

		String defaultValue = preferences.getString(PREFERENCES_OPENOFFICE_PATH);
		if (!defaultValue.isEmpty())
			//T: Preference page "Office" - Label: Example of the default path. Format: (e.g. PATH).
			//T: Only the "e.g." is translated
			defaultValue = String.format(" (%s %s)",  msg.preferencesOfficeExampleshort, defaultValue);

		if (OSDependent.isOOApp())
			//T: Preference page "Office" - Label: Office App
			addField(new AppFieldEditor(PREFERENCES_OPENOFFICE_PATH, msg.preferencesOfficeApp, getFieldEditorParent(), msg));
		else
			//T: Preference page "Office" - Label: Office folder
			addField(new DirectoryFieldEditor(PREFERENCES_OPENOFFICE_PATH, msg.preferencesOfficeFolder + defaultValue, getFieldEditorParent()));

		//T: Preference page "Office" - Label: Export documents as ODT or as PDF / only ODT/PDF or both
		addField(new RadioGroupFieldEditor(PREFERENCES_OPENOFFICE_ODT_PDF, msg.preferencesOfficeExportasLabel, 3, new String[][] { 
				//T: Preference page "Office" - Label: Export documents as ODT or as PDF / only ODT/PDF or both
				{ msg.preferencesOfficeOnlyodtLabel, "ODT" },
				//T: Preference page "Office" - Label: Export documents as ODT or as PDF / only ODT/PDF or both
				{ msg.preferencesOfficeOnlypdfLabel, "PDF" },
				//T: Preference page "Office" - Label: Export documents as ODT or as PDF / only ODT/PDF or both
				{ msg.preferencesOfficeOdtpdfLabel, "ODT+PDF" } },
				getFieldEditorParent()));

		//T: Preference page "Office" 
		addField(new StringFieldEditor(PREFERENCES_OPENOFFICE_ODT_PATH_FORMAT, msg.preferencesOfficeFormatandpathodt, getFieldEditorParent()));
		//T: Preference page "Office" 
		addField(new StringFieldEditor(PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT, msg.preferencesOfficeFormatandpathpdf, getFieldEditorParent()));

		
		//T: Preference page "Office" - Label checkbox "Start Office in a new thread"
		addField(new BooleanFieldEditor(PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD, msg.preferencesOfficeStartnewthread, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.preferencesOffice;
	}
	
	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
        IPreferenceStore node = new ScopedPreferenceStore(InstanceScope.INSTANCE, "com.sebulli.fakturama.preferences");   
		//PreferencesInDatabase.syncWithPreferencesFromDatabase("OPENOFFICE_PATH", write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_OPENOFFICE_ODT_PDF, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_OPENOFFICE_ODT_PATH_FORMAT, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT, write);
		
		// Set the default value
		// Search for the Office installation only if there is no path set.
		String oOHome = node.getString(PREFERENCES_OPENOFFICE_PATH);
		String defaultOOHome;

		if (!write) {
			if (oOHome.isEmpty()){
				defaultOOHome = OfficeStarter.getHome();
				if (defaultOOHome.isEmpty())
					defaultOOHome = OSDependent.getOODefaultPath();
			}
			else {
				defaultOOHome = OSDependent.getOODefaultPath();
			}
			node.setValue(PREFERENCES_OPENOFFICE_PATH, defaultOOHome);
		}
		
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
		node.setDefault(PREFERENCES_OPENOFFICE_ODT_PDF, "ODT+PDF");
		node.setDefault(PREFERENCES_OPENOFFICE_ODT_PATH_FORMAT, "ODT/{yyyy}/{doctype}/{docname}_{address}.odt");
		node.setDefault(PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT, "PDF/{yyyy}/{doctype}/{docname}_{address}.pdf");
		node.setDefault(PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD, true);
	}

}
