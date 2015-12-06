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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
//import com.sebulli.fakturama.ContextHelpConstants;
import com.sebulli.fakturama.misc.DataUtils;

/**
 * Preference page for the document settings. The preferences are written to the Fakturama common preferences store
 * (usually in {program}\.metadata\.plugins\org.eclipse.core.runtime\.settings\com.sebulli.fakturama.rcp.prefs
 * 
 * @author Gerd Bartelt
 */
public class GeneralPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;
    
//  @Inject
//  private IPreferenceStore preferences;

    private ComboFieldEditor currencyLocaleCombo;
    private StringFieldEditor example;
    private BooleanFieldEditor cashCheckbox;
    private BooleanFieldEditor thousandsSeparatorCheckbox;
    private BooleanFieldEditor useCurrencySymbolCheckbox;

	/**
	 * Constructor
	 */
	public GeneralPreferencePage() {
		super(GRID);

	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
        int index = 0;
		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.GENERAL_PREFERENCE_PAGE);
		addField(new BooleanFieldEditor(Constants.PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR, msg.preferencesGeneralCollapsenavbar, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS, msg.preferencesGeneralCloseeditors, getFieldEditorParent()));

//		addField(new StringFieldEditor(Constants.PREFERENCE_GENERAL_CURRENCY, msg.preferencesGeneralCurrency, getFieldEditorParent()));
        
        Locale[] locales = NumberFormat.getAvailableLocales();
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY);
        List<Locale> currencyLocaleList = Arrays.stream(locales).sorted((o1, o2) -> collator.compare(o1.getDisplayCountry(),o2.getDisplayCountry()))
                .filter(l -> l.getCountry().length() != 0).collect(Collectors.toList());
        String[][] currencyLocales = new String[currencyLocaleList.size()][2];
        for (Locale locale : currencyLocaleList) {
            currencyLocales[index][0] = locale.getDisplayCountry() + " (" + locale.getDisplayLanguage() + ")";
            currencyLocales[index][1] = locale.getLanguage() + "/"+locale.getCountry();
            index++;
        }
        
        currencyLocaleCombo = new ComboFieldEditor(Constants.PREFERENCE_CURRENCY_LOCALE, msg.preferencesGeneralCurrencyLocale, currencyLocales, getFieldEditorParent());
        addField(currencyLocaleCombo);
        
        example = new StringFieldEditor(Constants.PREFERENCE_CURRENCY_FORMAT_EXAMPLE, msg.preferencesGeneralCurrencyExample, getFieldEditorParent());
        example.setEnabled(false, getFieldEditorParent());
        addField(example);

        cashCheckbox = new BooleanFieldEditor(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, msg.preferencesGeneralCurrencyCashrounding, getFieldEditorParent());
        cashCheckbox.getDescriptionControl(getFieldEditorParent()).setToolTipText(msg.preferencesGeneralCurrencyCashroundingTooltip);
        String localeString = getPreferenceStore().getString(Constants.PREFERENCE_CURRENCY_LOCALE);
        if(!localeString.endsWith("CH")) {
            cashCheckbox.setEnabled(false, getFieldEditorParent());
        }
        addField(cashCheckbox);

        useCurrencySymbolCheckbox = new BooleanFieldEditor(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, msg.preferencesGeneralCurrencyUsesymbol, getFieldEditorParent());
        addField(useCurrencySymbolCheckbox);
        
        thousandsSeparatorCheckbox = new BooleanFieldEditor(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, msg.preferencesGeneralThousandseparator, getFieldEditorParent());
        addField(thousandsSeparatorCheckbox);
	}
	
	/**
	 * Some values depends from each other. This method listens to changes for some values and adapt them if necessary.
	 */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getSource() instanceof ComboFieldEditor) {
            String newValue = (String) event.getNewValue();
            String exampleFormat = calculateExampleCurrencyFormatString(newValue, thousandsSeparatorCheckbox.getBooleanValue(), cashCheckbox.getBooleanValue());
            example.setStringValue(exampleFormat);
        } else if(event.getSource() instanceof BooleanFieldEditor
                && (((BooleanFieldEditor)event.getSource()).getPreferenceName()
                    .equals(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING)
                    ||((BooleanFieldEditor)event.getSource()).getPreferenceName()
                    .equals(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR)
                    ||((BooleanFieldEditor)event.getSource()).getPreferenceName()
                    .equals(Constants.PREFERENCES_CURRENCY_USE_SYMBOL))
                ) {
            boolean useThousandsSeparator = thousandsSeparatorCheckbox.getBooleanValue();
            boolean useCashRounding = cashCheckbox.getBooleanValue();
            String preferenceName = ((BooleanFieldEditor)event.getSource()).getPreferenceName();
            if (preferenceName.equals(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING)) {
                useCashRounding = (Boolean) event.getNewValue();
            }
            if (preferenceName.equals(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR)) {
                useThousandsSeparator = (Boolean) event.getNewValue();
            }
            // WHO HAS MADE THE getComboBoxControl() METHOD PRIVATE??? WHY???
            Method privateStringMethod, privateValueMethod;
            try {
                privateStringMethod = ComboFieldEditor.class.
                        getDeclaredMethod("getComboBoxControl", Composite.class);
                privateStringMethod.setAccessible(true);
                Combo returnValue = (Combo)
                        privateStringMethod.invoke(currencyLocaleCombo, getFieldEditorParent());
                privateValueMethod = ComboFieldEditor.class.
                        getDeclaredMethod("getValueForName", String.class);
                privateValueMethod.setAccessible(true);
                String localeString = returnValue.getText();
                String value = (String)privateValueMethod.invoke(currencyLocaleCombo, localeString);
                
                String exampleFormat = calculateExampleCurrencyFormatString(value, useThousandsSeparator, useCashRounding);
                example.setStringValue(exampleFormat);
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException 
                    | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates an example string which contains a formatted currency amount
     * based on given locale string ("language/COUNTRY").
     * 
     * @param locale
     *            locale string
     * @return formatted example value
     */
    private String calculateExampleCurrencyFormatString(String localeString, 
            boolean useThousandsSeparator, boolean currencyCheckboxEnabled) {
        double myNumber = -1234.56864;
        String retval = "";
        Pattern pattern = Pattern.compile("(\\w{2})/(\\w{2})");
        Matcher matcher = pattern.matcher(localeString);
        if (matcher.matches() && matcher.groupCount() > 1) {
            String s = matcher.group(1);
            String s2 = matcher.group(2);
            Locale locale = new Locale(s, s2);

            NumberFormat form = NumberFormat.getCurrencyInstance(locale);
            form.setGroupingUsed(useThousandsSeparator);
            retval = form.format(myNumber);
            
            if (locale.getCountry().equals("CH")) {
                if(cashCheckbox != null) {
                    cashCheckbox.setEnabled(true, getFieldEditorParent());
                }
             //   if(currencyCheckboxEnabled) {
                    /* 
                     * Can't work directly with JavaMoney classes (ServiceProviders) since
                     * they already loaded by DataUtils and therefore the classloader gets
                     * confused.
                     */
                    retval = DataUtils.getInstance().formatCurrency(myNumber, locale, 
                            useCurrencySymbolCheckbox != null ? useCurrencySymbolCheckbox.getBooleanValue() : true,
                                    cashCheckbox != null ?  cashCheckbox.getBooleanValue() : true,
                                            useThousandsSeparator);
              //  }
            } else {
                retval = DataUtils.getInstance().formatCurrency(myNumber, locale, 
                        useCurrencySymbolCheckbox != null ? useCurrencySymbolCheckbox.getBooleanValue() : true,
                                cashCheckbox != null ?  cashCheckbox.getBooleanValue() : true, useThousandsSeparator);
                if(cashCheckbox != null) {
                    cashCheckbox.setEnabled(false, getFieldEditorParent());
                }
            }
        }
        return retval;
    }

	@Override
	public String getDescription() {
	    return msg.preferencesGeneral;
	}
	
	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCE_CURRENCY_LOCALE, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCE_CURRENCY_FORMAT_EXAMPLE, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, write);
	}
    
    @Override
    @Synchronize
    public void loadOrSaveUserValuesFromDB(IEclipseContext context) {
        if(preferencesInDatabase != null) {
            Boolean isWrite = (Boolean)context.get(PreferencesInDatabase.LOAD_OR_SAVE_PREFERENCES_FROM_OR_IN_DATABASE);
            syncWithPreferencesFromDatabase(BooleanUtils.toBoolean(isWrite));
        }
    }

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node) {
		node.setDefault(Constants.PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR, false);
		node.setDefault(Constants.PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS, false);

		//Set the default currency locale from current locale
		Locale defaultLocale = LocaleUtil.getInstance().getCurrencyLocale();
		String currencyLocaleString = defaultLocale.getLanguage() + "/" + defaultLocale.getCountry();
        String exampleFormat = calculateExampleCurrencyFormatString(currencyLocaleString, 
                true, false);
		node.setDefault(Constants.PREFERENCE_CURRENCY_LOCALE, currencyLocaleString);
        node.setDefault(Constants.PREFERENCE_CURRENCY_FORMAT_EXAMPLE, exampleFormat);
        node.setDefault(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, true);
        node.setDefault(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false);
        node.setDefault(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, true);
	}

	/**
	 * Update the currency Symbol for the whole application
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		DataUtils.getInstance().refresh();
		return super.performOk();
	}

	/**
	 * Update the currency Symbol for the whole application
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
        DataUtils.getInstance().refresh();
		super.performApply();
	}

	/**
	 * Update the currency Symbol for the whole application
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
        DataUtils.getInstance().refresh();
		super.performDefaults();
	}
}
