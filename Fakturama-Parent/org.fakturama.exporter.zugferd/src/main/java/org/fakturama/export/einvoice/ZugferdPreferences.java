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
 
package org.fakturama.export.einvoice;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import com.sebulli.fakturama.preferences.IInitializablePreference;
import com.sebulli.fakturama.preferences.PreferencesInDatabase;

/**
 *
 */
public class ZugferdPreferences extends FieldEditorPreferencePage implements IInitializablePreference {

	@Inject
    @Translation
    protected ZFMessages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

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
        addField(new BooleanFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_ACTIVE, msg.zugferdPreferencesIsActive, getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_TEST, msg.zugferdPreferencesTestmode, getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_VERSION, msg.zugferdPreferencesVersion, 2, new String[][] { 
			{ "1", "1" },
			{ "2.1 (XRechnung / Factur-X)", "2.1" }},
			getFieldEditorParent()));
		
		ComboFieldEditor conformanceLevelCombo = new ComboFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, msg.zugferdPreferencesProfile, 
				new String[][] { { ConformanceLevel.ZUGFERD_V1_BASIC.toString(), ConformanceLevel.ZUGFERD_V1_BASIC.toString() }, 
			{ ConformanceLevel.ZUGFERD_V1_COMFORT.toString(), ConformanceLevel.ZUGFERD_V1_COMFORT.toString()}, 
			{ ConformanceLevel.ZUGFERD_V1_EXTENDED.toString(), ConformanceLevel.ZUGFERD_V1_EXTENDED.toString() }
		 }, getFieldEditorParent());
		conformanceLevelCombo.setEnabled(false, getFieldEditorParent());
		addField(conformanceLevelCombo);
	}

    /**
     * Write or read the preference settings to or from the data base
     * 
     * @param write
     *            TRUE: Write to the data base
     */
    public void syncWithPreferencesFromDatabase(boolean write) {
        preferencesInDatabase.syncWithPreferencesFromDatabase(ZFConstants.PREFERENCES_ZUGFERD_ACTIVE, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(ZFConstants.PREFERENCES_ZUGFERD_VERSION, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(ZFConstants.PREFERENCES_ZUGFERD_TEST, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, write);
    }

    @Override
    public void setInitValues(IPreferenceStore node) {
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_ACTIVE, Boolean.FALSE);
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_VERSION, "2.1");
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_TEST, Boolean.TRUE);
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, "COMFORT");
    }

    @Override
    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
        if(preferencesInDatabase != null) {
            Boolean isWrite = (Boolean)context.get(PreferencesInDatabase.LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE);
            syncWithPreferencesFromDatabase(BooleanUtils.toBoolean(isWrite));
        }
    }

}
