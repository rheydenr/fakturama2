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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanPropertyAction;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.nebula.widgets.opal.checkboxgroup.CheckBoxGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.preferences.IInitializablePreference;
import com.sebulli.fakturama.preferences.PreferencesInDatabase;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 * Preferences for the Mail Service
 */
public class MailServicePreferences extends FieldEditorPreferencePage implements IInitializablePreference {
    @Inject
    @Translation
    protected MailServiceMessages messages;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;
    
    @Inject
    @Translation
    protected Messages msg;

    private ExtendedStringFieldEditor mailServerPassword;
    private ExtendedStringFieldEditor mailUser;
    private ExtendedStringFieldEditor mailHost;

    private BooleanPropertyAction booleanPropertyAction;

    private List<ExtendedStringFieldEditor> settingFields;

    private Group subjectGroup;
    
    public MailServicePreferences() {
        super(GRID);
    }

    @Override
    protected void createFieldEditors() {
        final CheckBoxGroup group = new CheckBoxGroup(getFieldEditorParent(), SWT.NONE);
        group.setText(messages.mailservicePreferencesActive);

        booleanPropertyAction = new BooleanPropertyAction("useMail", getPreferenceStore(), MailServiceConstants.PREFERENCES_MAIL_ACTIVE) {

            @Override
            public void runWithEvent(Event event) {
                super.runWithEvent(event);
                boolean isActive = ((Button)event.widget).getSelection();
                setEmptyStringAllowed(!isActive);
                
                if(!isActive) {
                 // if inactive any errors in fields are unimportant
                   setErrorMessage(null);
                   setValid(true);
                }
            }
        };
        
        group.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                booleanPropertyAction.setChecked(((Button) e.getSource()).getSelection());
                
                Event event = new Event();
                event.widget = (Button) e.getSource();
                booleanPropertyAction.runWithEvent(event);
                super.widgetSelected(e);
            }
        });
        
        mailHost = new ExtendedStringFieldEditor(MailServiceConstants.PREFERENCES_MAIL_HOST, messages.mailservicePreferencesServerHost, group.getContent());
        addField(mailHost);
        mailUser = new ExtendedStringFieldEditor(MailServiceConstants.PREFERENCES_MAIL_USER, messages.mailservicePreferencesServerUser, group.getContent());
        addField(mailUser);

        mailServerPassword = new ExtendedStringFieldEditor(MailServiceConstants.PREFERENCES_MAIL_PASSWORD, messages.mailservicePreferencesServerPassword, group.getContent());
        mailServerPassword.getTextControl(group.getContent()).setEchoChar('*');
        addField(mailServerPassword);
        
        addField(new DirectoryFieldEditor(MailServiceConstants.PREFERENCES_MAIL_ADDITIONAL_DOCUMENTS_PATH, messages.mailservicePreferencesAdditionaldocpath, group.getContent()));

        boolean isMailActive = getPreferenceStore().getBoolean(MailServiceConstants.PREFERENCES_MAIL_ACTIVE);
        group.setSelection(isMailActive);
        group.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(3, 1).create());
        
        settingFields = Arrays.asList(mailServerPassword, mailHost, mailUser);
        setEmptyStringAllowed(!group.getSelection());

        subjectGroup = new Group(getFieldEditorParent(), SWT.NONE);
        subjectGroup.setText(messages.mailservicePreferencesSubjectLabel);
        
        // subject fields
        createSubjectField(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_INVOICE, BillingType.INVOICE);
        createSubjectField(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_DELIVERY, BillingType.DELIVERY);
        createSubjectField(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_OFFER, BillingType.OFFER);
        createSubjectField(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_ORDER, BillingType.ORDER);
        createSubjectField(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_DUNNING, BillingType.DUNNING);
        createSubjectField(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_CONFIRMATION, BillingType.CONFIRMATION);
        createSubjectField(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_CREDIT, BillingType.CREDIT);
        createSubjectField(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_PROFORMA, BillingType.PROFORMA);
        subjectGroup.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        subjectGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
    }
    
    private void createSubjectField(String pref, BillingType billingType) {
        StringFieldEditor subjectEntry = new StringFieldEditor(pref, 
                msg.getMessageFromKey(DocumentTypeUtil.findByBillingType(billingType).getSingularKey()), 
                subjectGroup) {
            
            @Override
            protected void adjustForNumColumns(int numColumns) {
                // ignore adjusting of columns
            }
            
        };
        
        addField(subjectEntry);
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
        
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_INVOICE, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_DELIVERY, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_OFFER, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_ORDER, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_DUNNING, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_CONFIRMATION, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_CREDIT, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(MailServiceConstants.PREFERENCES_MAIL_SUBJECT_PROFORMA, write);
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

    private void setEmptyStringAllowed(boolean isAllowed) {
        
        settingFields.forEach(f -> f.setEmptyStringAllowed(isAllowed));
        if (!isAllowed) {
            settingFields.forEach(ExtendedStringFieldEditor::refreshState);
    
            boolean isValid = settingFields.stream().allMatch(ExtendedStringFieldEditor::isValid);
            setValid(isValid);
        }
    }
    
    /**
     * Private class for extended {@link StringFieldEditor}s
     *
     */
    class ExtendedStringFieldEditor extends StringFieldEditor {
        public ExtendedStringFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        public void refreshState() {
            refreshValidState();
        }
    }
}
