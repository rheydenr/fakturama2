/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package org.fakturama.export.zugferd;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import com.sebulli.fakturama.preferences.IInitializablePreference;

/**
 *
 */
public class ZugferdPreferences extends FieldEditorPreferencePage implements IInitializablePreference {

	@Inject
    @Translation
    protected ZFMessages msg;

    @Inject @Optional
    private IPreferenceStore preferences;
    
    /**
     * The Constructor.
     */
	public ZugferdPreferences() {
		super(GRID);
	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_PATH, msg.zugferdPreferencesFilelocation, getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_VERSION, msg.zugferdPreferencesVersion, 2, new String[][] { 
			{ "1", "1" },
			{ "2", "2" }},
			getFieldEditorParent()));
		addField(new ComboFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, msg.zugferdPreferencesProfile, new String[][] { { "BASIC", "BASIC" }, { "COMFORT", "COMFORT" }, { "EXTENDED", "EXTENDED" }
		 }, getFieldEditorParent()));
	}

    @Override
    public void setInitValues(IPreferenceStore node) {
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_VERSION, "1");
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, "COMFORT");
    }

    @Override
    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
        // TODO implement!
    }

}
