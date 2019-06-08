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

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OSDependent;
import com.sebulli.fakturama.office.TargetFormat;

/**
 * Preference page for the Office settings
 * 
 * @author Gerd Bartelt
 */
public class OfficePreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {

	@Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

    @Inject @Optional
    private IPreferenceStore preferences;
    
    @Inject
    private IEclipseContext context;

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
	    
	    AppFieldEditor appFieldEditor = ContextInjectionFactory.make(AppFieldEditor.class, context);

		String defaultValue = preferences.getDefaultString(Constants.PREFERENCES_OPENOFFICE_PATH);
		if (!defaultValue.isEmpty())
			//T: Preference page "Office" - Label: Example of the default path. Format: (e.g. PATH).
			//T: Only the "e.g." is translated
			defaultValue = String.format(" (%s %s)",  msg.preferencesOfficeExampleshort, defaultValue);

		if (OSDependent.isOOApp()) {
		    appFieldEditor.prepare(Constants.PREFERENCES_OPENOFFICE_PATH, msg.preferencesOfficeApp + defaultValue, getFieldEditorParent());
			//T: Preference page "Office" - Label: Office App
			addField(appFieldEditor);
		} else {
//		    appFieldEditor.prepare(Constants.PREFERENCES_OPENOFFICE_PATH, msg.preferencesOfficeFolder + defaultValue, getFieldEditorParent());
			//T: Preference page "Office" - Label: Office folder
			addField(new DirectoryFieldEditor(Constants.PREFERENCES_OPENOFFICE_PATH, msg.preferencesOfficeFolder + defaultValue, getFieldEditorParent()));
//			addField(appFieldEditor);
		}
		
		//T: Preference page "Office" - Label: Export documents as ODT or as PDF / only ODT/PDF or both
		addField(new RadioGroupFieldEditor(Constants.PREFERENCES_OPENOFFICE_ODT_PDF, msg.preferencesOfficeExportasLabel, 3, new String[][] { 
				//T: Preference page "Office" - Label: Export documents as ODT or as PDF / only ODT/PDF or both
				{ msg.preferencesOfficeOdtpdfOnlyodt, TargetFormat.ODT.getPrefId() },
				//T: Preference page "Office" - Label: Export documents as ODT or as PDF / only ODT/PDF or both
				{ msg.preferencesOfficeOdtpdfOnlypdf, TargetFormat.PDF.getPrefId() },
				//T: Preference page "Office" - Label: Export documents as ODT or as PDF / only ODT/PDF or both
				{ msg.preferencesOfficeOdtpdfBoth, TargetFormat.ODT.getPrefId() + "+" + TargetFormat.PDF.getPrefId()} },
				getFieldEditorParent()));

		//T: Preference page "Office" 
		addField(new StringFieldEditor(Constants.PREFERENCES_OPENOFFICE_ODT_PATH_FORMAT, msg.preferencesOfficeFormatandpathOdt, getFieldEditorParent()));
		//T: Preference page "Office" 
		addField(new StringFieldEditor(Constants.PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT, msg.preferencesOfficeFormatandpathPdf, getFieldEditorParent()));

		addField(new StringFieldEditor(Constants.PREFERENCES_ADDITIONAL_OPENOFFICE_PDF_PATH_FORMAT, msg.preferencesOfficeFormatandpathAdditionalpdf, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.PREFERENCES_OPENPDF, msg.preferencesOfficeExportasOpenaction, getFieldEditorParent()));
		
		//T: Preference page "Office" - Label checkbox "Start Office in a new thread"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD, msg.preferencesOfficeStartnewthread, getFieldEditorParent()));
	}
	
	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {
		// since path is only locally we don't have to put it into the database
//		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_OPENOFFICE_PATH, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_OPENOFFICE_ODT_PDF, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_OPENOFFICE_ODT_PATH_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_ADDITIONAL_OPENOFFICE_PDF_PATH_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_OPENPDF, write);
	}
	
    @Synchronize
    public void loadUserValuesFromDB() {
        // TRUE ==> Save preferences
        // FALSE ==> Load preferences
        syncWithPreferencesFromDatabase(((Boolean)(context.get(PreferencesInDatabase.LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE))));
    }

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
		node.setDefault(Constants.PREFERENCES_OPENOFFICE_ODT_PDF, TargetFormat.ODT.getPrefId() + "+" + TargetFormat.PDF.getPrefId());
		node.setDefault(Constants.PREFERENCES_OPENOFFICE_ODT_PATH_FORMAT, "ODT/{yyyy}/{doctype}/{docname}_{address}.odt");
		node.setDefault(Constants.PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT, "PDF/{yyyy}/{doctype}/{docname}_{address}.pdf");
		node.setDefault(Constants.PREFERENCES_ADDITIONAL_OPENOFFICE_PDF_PATH_FORMAT, "");
		node.setDefault(Constants.PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD, true);
		node.setDefault(Constants.PREFERENCES_OPENPDF, false);
		
		// Set the default value
		// Search for the Office installation only if there is no path set.
		String oOHome = node.getString(Constants.PREFERENCES_OPENOFFICE_PATH);
		String defaultOOHome = "";

		if (oOHome.isEmpty()) {
//			defaultOOHome = OfficeStarter.getHome();
			if (defaultOOHome.isEmpty()) {
				defaultOOHome = OSDependent.getOODefaultPath();
			}
		} else {
			defaultOOHome = OSDependent.getOODefaultPath();
		}
		node.setDefault(Constants.PREFERENCES_OPENOFFICE_PATH, defaultOOHome);
	}

    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
    }

}
