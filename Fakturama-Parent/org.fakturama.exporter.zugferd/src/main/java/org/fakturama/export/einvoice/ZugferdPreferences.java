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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanPropertyAction;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.widgets.opal.checkboxgroup.CheckBoxGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.preferences.IInitializablePreference;
import com.sebulli.fakturama.preferences.PreferencesInDatabase;

/**
 * Preferences for the ZUGFeRD export settings (including Factur-X and XRechnung)
 */
public class ZugferdPreferences extends FieldEditorPreferencePage implements IInitializablePreference {

	@Inject
    @Translation
    protected ZFMessages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

    private ComboFieldEditor conformanceLevelCombo;

    enum ZugferdVersion {
        V1("1", "1"), //
        V2_1("2.1 (XRechnung / Factur-X)", "2.1");

        final String description, version;

        ZugferdVersion(String description, String version) {
            this.description = description;
            this.version = version;
        }

        public String getDescription() {
            return description;
        }

        public String getVersion() {
            return version;
        }
    }

    private Map<ZugferdVersion, String[][]> featureMap;

    private StringFieldEditor xrechnungPathField;

    private Composite editorParent;
    
    /**
     * The Constructor.
     */
	public ZugferdPreferences() {
		super(GRID);
        
        featureMap = new EnumMap<>(ZugferdVersion.class);
        featureMap.put(ZugferdVersion.V1, new String[][] { 
//          { ConformanceLevel.ZUGFERD_V1_BASIC.toString(), ConformanceLevel.ZUGFERD_V1_BASIC.toString() }, 
            { ConformanceLevel.ZUGFERD_V1_COMFORT.toString(), ConformanceLevel.ZUGFERD_V1_COMFORT.toString()}});
		
        featureMap.put(ZugferdVersion.V2_1, new String[][] { 
            { ConformanceLevel.ZUGFERD_V2_COMFORT.toString(), ConformanceLevel.ZUGFERD_V2_COMFORT.toString()}, 
//            { ConformanceLevel.ZUGFERD_V2_EN16931.toString(), ConformanceLevel.ZUGFERD_V2_EN16931.toString()}, 
            { ConformanceLevel.XRECHNUNG.toString(), ConformanceLevel.XRECHNUNG.toString()}, 
            { ConformanceLevel.FACTURX_EN16931.toString(), ConformanceLevel.FACTURX_EN16931.toString()}}
        );
	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
        final CheckBoxGroup group = new CheckBoxGroup(getFieldEditorParent(), SWT.NONE);
        group.setText(msg.zugferdPreferencesIsActive);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        
        BooleanPropertyAction booleanPropertyAction = new BooleanPropertyAction("useZF", getPreferenceStore(), ZFConstants.PREFERENCES_ZUGFERD_ACTIVE);
        group.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                booleanPropertyAction.setChecked(((Button)e.getSource()).getSelection());
                booleanPropertyAction.run();
                enableXRechnungPathField(group.getSelection(), null);
            }
        });
        editorParent = group.getContent();

//		addField(new BooleanFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_TEST, msg.zugferdPreferencesTestmode, getFieldEditorParent()));
 
		RadioGroupFieldEditor zugferdVersionRadioGroup = new RadioGroupFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_VERSION, msg.zugferdPreferencesVersion, 2, new String[][] { 
			{ ZugferdVersion.V1.getDescription(), ZugferdVersion.V1.getVersion() },
			{ ZugferdVersion.V2_1.getDescription(), ZugferdVersion.V2_1.getVersion() }},
		        editorParent);
        addField(zugferdVersionRadioGroup);
        
	    // fill combo box according to selected version!
        String zfVersionStr = StringUtils.defaultIfBlank(getPreferenceStore().getString(ZFConstants.PREFERENCES_ZUGFERD_VERSION), 
                getPreferenceStore().getDefaultString(ZFConstants.PREFERENCES_ZUGFERD_VERSION));
        
        java.util.Optional<ZugferdVersion> zfVersion = Arrays.stream(ZugferdVersion.values()).filter(v -> v.getVersion().equalsIgnoreCase(zfVersionStr)).findAny();
        if(!featureMap.containsKey(zfVersion.get())) {
            // emergency exit
            zfVersion = java.util.Optional.of(ZugferdVersion.V2_1);
        }
		conformanceLevelCombo = new ComboFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, msg.zugferdPreferencesProfile, 
				featureMap.get(zfVersion.get()), editorParent);
		addField(conformanceLevelCombo);
		
        xrechnungPathField = new StringFieldEditor(ZFConstants.PREFERENCES_ZUGFERD_PATH, msg.zugferdPreferencesFilelocation, editorParent) {
            public void setEmptyStringAllowed(boolean b) {
                super.setEmptyStringAllowed(b);
                // else the error flag isn't reset
                refreshValidState();
            };
        };
        xrechnungPathField.setEmptyStringAllowed(false);
        addField(xrechnungPathField);
        boolean isZFActive = getPreferenceStore().getBoolean(ZFConstants.PREFERENCES_ZUGFERD_ACTIVE);
        group.setSelection(isZFActive);
        enableXRechnungPathField(isZFActive, getPreferenceStore().getString(ZFConstants.PREFERENCES_ZUGFERD_PROFILE));
	}

    private void enableXRechnungPathField(boolean isZFActive, String currentConformanceLevelString) {
        ConformanceLevel currentConformanceLevel;
        
        if(currentConformanceLevelString == null) {
            Combo comboBox = getCombo(conformanceLevelCombo);
            currentConformanceLevelString = comboBox.getItem(comboBox.getSelectionIndex());
        }
        
        try {
            currentConformanceLevel = ConformanceLevel.valueOf(currentConformanceLevelString);
        } catch (Exception e) {
            // only if conformance level can't be determined
            currentConformanceLevel = ConformanceLevel.FACTURX_EN16931;
        }
        boolean enabled = isZFActive && ConformanceLevel.XRECHNUNG == currentConformanceLevel;
        xrechnungPathField.setEnabled(enabled, editorParent);
        xrechnungPathField.setEmptyStringAllowed(!enabled);
        checkState();
    }
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
	    super.propertyChange(event);
        boolean isZFActive = getPreferenceStore().getBoolean(ZFConstants.PREFERENCES_ZUGFERD_ACTIVE);
	    if(event.getSource() instanceof RadioGroupFieldEditor
	            && event.getOldValue() != event.getNewValue()) {
		    String selectionValueStr = ((RadioGroupFieldEditor)event.getSource()).getSelectionValue();
	        java.util.Optional<ZugferdVersion> selectionValue = Arrays.stream(ZugferdVersion.values()).filter(v -> v.getVersion().equalsIgnoreCase(selectionValueStr)).findAny();
		    Combo cfCombo = getCombo(conformanceLevelCombo);
		    cfCombo.removeAll();
            Arrays.stream(featureMap.get(selectionValue.get())).forEach(v -> cfCombo.add(v[0]));
            cfCombo.select(0);

            enableXRechnungPathField(isZFActive, cfCombo.getText());
	    } else if(event.getSource() instanceof ComboFieldEditor) {
	        enableXRechnungPathField(isZFActive, (String) event.getNewValue());
	    }
	}

    private Combo getCombo(ComboFieldEditor comboFieldEditor) {
        Method privateStringMethod;

        try {
            privateStringMethod = ComboFieldEditor.class.getDeclaredMethod("getComboBoxControl", Composite.class);
            privateStringMethod.setAccessible(true);
            return (Combo) privateStringMethod.invoke(comboFieldEditor, editorParent);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
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
        preferencesInDatabase.syncWithPreferencesFromDatabase(ZFConstants.PREFERENCES_ZUGFERD_PATH, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, write);
    }

    @Override
    public void setInitValues(IPreferenceStore node) {
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_ACTIVE, Boolean.FALSE);
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_VERSION, ZugferdVersion.V2_1.getVersion());
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_TEST, Boolean.FALSE);
        node.setDefault(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, ConformanceLevel.XRECHNUNG.name());
    }

    @Override
    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
        if(preferencesInDatabase != null) {
            Boolean isWrite = (Boolean)context.get(PreferencesInDatabase.LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE);
            syncWithPreferencesFromDatabase(BooleanUtils.toBoolean(isWrite));
        }
    }

}
