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

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;

/**
 * Preference page for the contact settings
 * 
 * @author Gerd Bartelt
 */
public class ToolbarPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;
//    
//    @Inject
//    protected IEventBroker eventBroker;

	/**
	 * Constructor
	 */
	public ToolbarPreferencePage() {
		super(GRID);
	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {

		//T: Preference page "toolbar" 
		String showIcon = msg.preferencesToolbarShowicon + " ";

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.TOOLBAR_PREFERENCE_PAGE);

		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_WEBSHOP, showIcon + msg.commandWebshopName, getFieldEditorParent()));

		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_PRINT, showIcon + msg.mainMenuFilePrintTooltipDeprecated, getFieldEditorParent()));

		//T: Preference page "toolbar" 
		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_SAVE, showIcon + msg.mainMenuFileSave, getFieldEditorParent()));

		// Get all documents
		for (int i=1; i< DocumentType.MAXID; i++) {
			addField(new BooleanFieldEditor("TOOLBAR_SHOW_DOCUMENT_NEW_" + DocumentType.getTypeAsString(i).toUpperCase(), showIcon + msg.getMessageFromKey(DocumentType.getNewTextKey(DocumentType.findByKey(i))), getFieldEditorParent()));
		}
		
		//T: Preference page "toolbar" 
		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_NEW_PRODUCT, showIcon + msg.getMessageFromKey(CommandIds.TOOLBAR_PRODUCT), getFieldEditorParent()));
		
		//T: Preference page "toolbar" 
		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_NEW_CONTACT, showIcon + msg.getMessageFromKey(CommandIds.TOOLBAR_CONTACT), getFieldEditorParent()));
		
		//T: Preference page "toolbar" 
		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER, showIcon + msg.getMessageFromKey(CommandIds.TOOLBAR_EXPENDITUREVOUCHER), getFieldEditorParent()));
		
		//T: Preference page "toolbar" 
		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_NEW_RECEIPTVOUCHER, showIcon + msg.getMessageFromKey(CommandIds.TOOLBAR_RECEIPTVOUCHER), getFieldEditorParent()));
		
		//T: Preference page "toolbar" 
		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_OPEN_PARCELSERVICE, showIcon + msg.commandParcelserviceName, getFieldEditorParent()));
		
		//T: Preference page "toolbar" 
		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_OPEN_BROWSER, showIcon + msg.commandBrowserCommand, getFieldEditorParent()));
		
		//T: Preference page "toolbar" 
		addField(new BooleanFieldEditor(Constants.TOOLBAR_SHOW_OPEN_CALCULATOR, showIcon + msg.commandCalculatorName, getFieldEditorParent()));
	}
	
//	/* (non-Javadoc)
//	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
//	 */
//	@Override
//	public boolean performOk() {
//	    eventBroker.post("TOOLBARPREFS", "jaa");
//	    return super.performOk();
//	}
//	
//	@Override
//	public String getDescription() {
//	    return msg.preferencesToolbarIcons;
//	}
//
	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_WEBSHOP, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_PRINT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_SAVE, write);
		// Get all documents
		for (int i=1; i<= DocumentType.MAXID; i++) {
			preferencesInDatabase.syncWithPreferencesFromDatabase("TOOLBAR_SHOW_DOCUMENT_NEW_" + DocumentType.getTypeAsString(i).toUpperCase(), write);
		}
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_NEW_PRODUCT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_NEW_CONTACT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_NEW_RECEIPTVOUCHER, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_OPEN_PARCELSERVICE, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_OPEN_BROWSER, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.TOOLBAR_SHOW_OPEN_CALCULATOR, write);
	}
	
	@Synchronize
	public void loadUserValuesFromDB() {
	    syncWithPreferencesFromDatabase(false);
	}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
		node.setDefault(Constants.TOOLBAR_SHOW_WEBSHOP, true);
		node.setDefault(Constants.TOOLBAR_SHOW_PRINT, true);
		node.setDefault(Constants.TOOLBAR_SHOW_SAVE, true);
		// Get all documents
		// TODO change to to a loop over native enumerations!!!
		for (int i=1; i<DocumentType.MAXID; i++) {
			node.setDefault("TOOLBAR_SHOW_DOCUMENT_NEW_" + DocumentType.getTypeAsString(i).toUpperCase(), 
					i==3 || i ==5 || i == 6);
		}
		node.setDefault(Constants.TOOLBAR_SHOW_NEW_PRODUCT, true);
		node.setDefault(Constants.TOOLBAR_SHOW_NEW_CONTACT, true);
		node.setDefault(Constants.TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER, true);
		node.setDefault(Constants.TOOLBAR_SHOW_NEW_RECEIPTVOUCHER, true);
		node.setDefault(Constants.TOOLBAR_SHOW_OPEN_PARCELSERVICE, true);
		node.setDefault(Constants.TOOLBAR_SHOW_OPEN_BROWSER, true);
		node.setDefault(Constants.TOOLBAR_SHOW_OPEN_CALCULATOR, true);
	}

}
