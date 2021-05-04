/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package org.fakturama.connectors.mail;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanPropertyAction;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.nebula.widgets.opal.checkboxgroup.CheckBoxGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.preferences.IInitializablePreference;
import com.sebulli.fakturama.preferences.PreferencesInDatabase;

/**
 * Preferences for the Mail Service
 */
public class MailServicePreferences extends FieldEditorPreferencePage implements IInitializablePreference {
    @Inject
    @Translation
    protected MailServiceMessages messages;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;
    private Composite editorParent;
    
    public MailServicePreferences() {
        super(GRID);
    }

    @Override
    protected void createFieldEditors() {
        final CheckBoxGroup group = new CheckBoxGroup(getFieldEditorParent(), SWT.NONE);
        group.setText(messages.mailservicePreferencesActive);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
   
        BooleanPropertyAction booleanPropertyAction = new BooleanPropertyAction("useMail", getPreferenceStore(), MailServiceConstants.PREFERENCES_MAIL_ACTIVE);
        group.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                booleanPropertyAction.setChecked(((Button)e.getSource()).getSelection());
                booleanPropertyAction.run();
                super.widgetSelected(e);
            }
        });
        editorParent = group.getContent();
        
        addField(new StringFieldEditor(MailServiceConstants.PREFERENCES_MAIL_HOST, messages.mailservicePreferencesServerHost, editorParent));
        addField(new StringFieldEditor(MailServiceConstants.PREFERENCES_MAIL_USER, messages.mailservicePreferencesServerUser, editorParent));
        StringFieldEditor mailServerPassword = new StringFieldEditor(MailServiceConstants.PREFERENCES_MAIL_PASSWORD, messages.mailservicePreferencesServerPassword, editorParent);
        mailServerPassword.getTextControl(editorParent).setEchoChar('*');
        addField(mailServerPassword);
        addField(new StringFieldEditor(MailServiceConstants.PREFERENCES_MAIL_ADDITIONAL_DOCUMENTS_PATH, messages.mailservicePreferencesAdditionaldocpath, editorParent));
    }

    /**
     * Write or read the preference settings to or from the data base
     * 
     * @param write
     *            TRUE: Write to the data base
     */
    public void syncWithPreferencesFromDatabase(boolean write) {
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_HOST, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_USER, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_PASSWORD, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_ADDITIONAL_DOCUMENTS_PATH, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_TEMPLATE_PATH, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_ACTIVE, write);
    }

    @Override
    public void setInitValues(IPreferenceStore node) {
        node.setDefault(MailServiceConstants.PREFERENCES_MAIL_ACTIVE, Boolean.FALSE);
    }

    @Override
    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
        if(preferencesInDatabase != null) {
            Boolean isWrite = (Boolean)context.get(PreferencesInDatabase.LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE);
            syncWithPreferencesFromDatabase(BooleanUtils.toBoolean(isWrite));
        }
    }
}
