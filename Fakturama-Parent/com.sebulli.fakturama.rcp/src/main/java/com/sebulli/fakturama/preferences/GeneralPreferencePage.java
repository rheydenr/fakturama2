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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
//import com.sebulli.fakturama.ContextHelpConstants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.money.CurrencySettingEnum;

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

    private ComboFieldEditor currencyLocaleCombo;
    private Text example, dbConnectionInfo;
    private BooleanFieldEditor cashCheckbox;
    private BooleanFieldEditor thousandsSeparatorCheckbox;
    private RadioGroupFieldEditor useCurrencySymbolCheckbox;
    private IntegerFieldEditor decimalCurrencyPlaces, generalDecimalPlaces;

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

        Locale[] locales = NumberFormat.getAvailableLocales();
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY);
        List<Locale> currencyLocaleList = Arrays.stream(locales)
                .filter(l -> l.getCountry().length() != 0)
                .sorted((o1, o2) -> collator.compare(o1.getDisplayCountry(),o2.getDisplayCountry()))
                // distinguish different Locales by country AND language! 
                .filter(distinctByKey(l -> l.getDisplayCountry() + l.getDisplayLanguage()))
                .collect(Collectors.toList());
        String[][] currencyLocales = new String[currencyLocaleList.size()][2];
        for (Locale locale : currencyLocaleList) {
            currencyLocales[index][0] = String.format("%s (%s)", locale.getDisplayCountry(), locale.getDisplayLanguage());
            currencyLocales[index][1] = String.format("%s/%s", locale.getLanguage(), locale.getCountry());
            index++;
        }
        
        currencyLocaleCombo = new ComboFieldEditor(Constants.PREFERENCE_CURRENCY_LOCALE, msg.preferencesGeneralCurrencyLocale, currencyLocales, getFieldEditorParent());
        addField(currencyLocaleCombo);
        
        Label exampleLabel = new Label(getFieldEditorParent(), SWT.NONE);
        exampleLabel.setText(msg.preferencesGeneralCurrencyExample);
        example = new Text(getFieldEditorParent(), SWT.BORDER);
        example.setEditable(false);
        example.setText(calculateExampleCurrencyFormatString(
        		super.getPreferenceStore().getString(Constants.PREFERENCE_CURRENCY_LOCALE), 
        		super.getPreferenceStore().getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR), 
        		super.getPreferenceStore().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING), 
        		CurrencySettingEnum.valueOf(super.getPreferenceStore().getString(Constants.PREFERENCES_CURRENCY_USE_SYMBOL))));
        GridDataFactory.fillDefaults().grab(true, false).applyTo(example);
                
//        example.setSize(400, SWT.DEFAULT);

        cashCheckbox = new BooleanFieldEditor(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, msg.preferencesGeneralCurrencyCashrounding, getFieldEditorParent());
        cashCheckbox.getDescriptionControl(getFieldEditorParent()).setToolTipText(msg.preferencesGeneralCurrencyCashroundingTooltip);
        String localeString = getPreferenceStore().getString(Constants.PREFERENCE_CURRENCY_LOCALE);
        if(!localeString.endsWith("CH")) {
            cashCheckbox.setEnabled(false, getFieldEditorParent());
        }
        addField(cashCheckbox);

        useCurrencySymbolCheckbox = new RadioGroupFieldEditor(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, msg.preferencesGeneralCurrencyUsesymbol, 3, new String[][] { 
			{ msg.preferencesGeneralCurrencyUsesymbol, CurrencySettingEnum.SYMBOL.name() },
			{ msg.preferencesGeneralCurrencyUseisocode, CurrencySettingEnum.CODE.name() },
			{ msg.preferencesGeneralCurrencyUsenothing, CurrencySettingEnum.NONE.name() } },
			getFieldEditorParent());
        addField(useCurrencySymbolCheckbox);
        
        thousandsSeparatorCheckbox = new BooleanFieldEditor(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, msg.preferencesGeneralThousandseparator, getFieldEditorParent());
        addField(thousandsSeparatorCheckbox);
        
        decimalCurrencyPlaces = new IntegerFieldEditor(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, msg.preferencesGeneralCurrencyDecimalplaces, getFieldEditorParent());
        decimalCurrencyPlaces.setValidRange(0, 5);
        addField(decimalCurrencyPlaces);
        
        generalDecimalPlaces = new IntegerFieldEditor(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES, msg.preferencesGeneralQuantityDecimalplaces, getFieldEditorParent());
        generalDecimalPlaces.setValidRange(0, 5);     
        addField(generalDecimalPlaces);

        // Info: DB connection string
        Label dbConnectionLabel = new Label(getFieldEditorParent(), SWT.NONE);
        dbConnectionLabel.setText(msg.preferencesGeneralDatabase);
        dbConnectionInfo = new Text(getFieldEditorParent(), SWT.BORDER);
        dbConnectionInfo.setEditable(false);
        dbConnectionInfo.setText(getPreferenceStore().getString(PersistenceUnitProperties.JDBC_URL));
        GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT).applyTo(dbConnectionInfo);
	}
	
	public static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
	    Map<Object,Boolean> seen = new ConcurrentHashMap<>();
	    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	/**
	 * Some values depends from each other. This method listens to changes for some values and adapt them if necessary.
	 */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        Method privateStringMethod, privateValueMethod;
        Field privateValueField;
        super.propertyChange(event);
        
        /*
         * The current value of the radiogroup can't only read from attribute "value", but this attribute is private and 
         * has no getter. This forced me to use another ugly hack... Please, excuse me...
         */
        String value = CurrencySettingEnum.SYMBOL.name(); // only as a precaution
        try {
        	privateValueField = RadioGroupFieldEditor.class.getDeclaredField("value");
        	privateValueField.setAccessible(true);
			value = (String) privateValueField.get(useCurrencySymbolCheckbox);
        }
        catch (SecurityException | IllegalAccessException 
                | IllegalArgumentException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        
        CurrencySettingEnum currencySetting = CurrencySettingEnum.valueOf(value);
        if (event.getSource() instanceof ComboFieldEditor) {
            String newValue = (String) event.getNewValue();
            cashCheckbox.loadDefault();
            String exampleFormat = calculateExampleCurrencyFormatString(newValue, thousandsSeparatorCheckbox.getBooleanValue(), cashCheckbox.getBooleanValue(), currencySetting);
            example.setText(exampleFormat);
        } else if(event.getSource() instanceof BooleanFieldEditor
                && (((BooleanFieldEditor)event.getSource()).getPreferenceName()
                    .equals(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING)
                    ||((BooleanFieldEditor)event.getSource()).getPreferenceName()
                    .equals(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR))
                || event.getSource() instanceof RadioGroupFieldEditor
                    &&((RadioGroupFieldEditor)event.getSource()).getPreferenceName()
                    .equals(Constants.PREFERENCES_CURRENCY_USE_SYMBOL)
                ) {
            boolean useThousandsSeparator = thousandsSeparatorCheckbox.getBooleanValue();
            boolean useCashRounding = cashCheckbox.getBooleanValue();
            
            String preferenceName;
            if(event.getSource() instanceof BooleanFieldEditor) {
				preferenceName = ((BooleanFieldEditor)event.getSource()).getPreferenceName();
            } else {
            	preferenceName = ((RadioGroupFieldEditor)event.getSource()).getPreferenceName();
            	currencySetting = CurrencySettingEnum.valueOf((String) event.getNewValue());
            }
            if (preferenceName.equals(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING)) {
                useCashRounding = (Boolean) event.getNewValue();
            }
            if (preferenceName.equals(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR)) {
                useThousandsSeparator = (Boolean) event.getNewValue();
            }
            
            // WHO HAS MADE THE getComboBoxControl() METHOD PRIVATE??? WHY???
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
                value = (String)privateValueMethod.invoke(currencyLocaleCombo, localeString);
                
                String exampleFormat = calculateExampleCurrencyFormatString(value, useThousandsSeparator, useCashRounding, currencySetting);
                example.setText(exampleFormat);
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
     * @param currencySetting 
     * 
     * @param locale
     *            locale string
     * @return formatted example value
     */
    private String calculateExampleCurrencyFormatString(String localeString, 
            boolean useThousandsSeparator, boolean useCashRounding, CurrencySettingEnum currencySetting) {
        double myNumber = -1234.56864;
        String retval = "";
        Pattern pattern = Pattern.compile("(\\w{2})/(\\w{2})");
        Matcher matcher = pattern.matcher(localeString);
        if (matcher.matches() && matcher.groupCount() > 1) {
            String s = matcher.group(1);
            String s2 = matcher.group(2);
            Locale locale = new Locale(s, s2);

//            NumberFormat form;
//            if(currencySetting == CurrencySettingEnum.NONE) {
//            	form = NumberFormat.getNumberInstance();
//            } else {
//            	form = NumberFormat.getCurrencyInstance(locale);
//            }
//            form.setGroupingUsed(useThousandsSeparator);
//            form.setMinimumFractionDigits(decimalPlaces != null ? decimalPlaces.getIntValue() : 2);
//            retval = form.format(myNumber);
            
            if (locale.getCountry().equals("CH")) {
                if(cashCheckbox != null) {
                    cashCheckbox.setEnabled(true, getFieldEditorParent());
                }
             //   if(useCashRounding) {
                    /* 
                     * Can't work directly with JavaMoney classes (ServiceProviders) since
                     * they already loaded by DataUtils and therefore the classloader gets
                     * confused.
                     */
                    retval = DataUtils.getInstance().formatCurrency(myNumber, locale, 
                    				currencySetting,
                                    cashCheckbox != null ? cashCheckbox.getBooleanValue() : true,
                                            useThousandsSeparator);
              //  }
            } else {
                retval = DataUtils.getInstance().formatCurrency(myNumber, locale, 
                				currencySetting,
                                cashCheckbox != null ? cashCheckbox.getBooleanValue() : true, useThousandsSeparator);
                if(cashCheckbox != null) {
                    cashCheckbox.setEnabled(false, getFieldEditorParent());
                }
            }
        }
        return retval;
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
//        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCE_CURRENCY_FORMAT_EXAMPLE, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES, write);
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
		node.setDefault(Constants.PREFERENCE_CURRENCY_LOCALE, currencyLocaleString);
        node.setDefault(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, true);
        node.setDefault(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, Integer.valueOf(2));
        node.setDefault(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES, Integer.valueOf(2));
        node.setDefault(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, false);
        node.setDefault(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, CurrencySettingEnum.SYMBOL.name());
		CurrencySettingEnum currencySetting = CurrencySettingEnum.valueOf(node.getString(Constants.PREFERENCES_CURRENCY_USE_SYMBOL));
        String exampleFormat = calculateExampleCurrencyFormatString(currencyLocaleString, 
                true, false, currencySetting);
        node.setDefault(Constants.PREFERENCE_CURRENCY_FORMAT_EXAMPLE, exampleFormat);
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
