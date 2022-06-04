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
import java.text.MessageFormat;
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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.ULocale;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
//import com.sebulli.fakturama.ContextHelpConstants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.money.CurrencySettingEnum;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.ExpenditureVoucherEditor;
import com.sebulli.fakturama.parts.ProductEditor;
import com.sebulli.fakturama.parts.ReceiptVoucherEditor;
import com.sebulli.fakturama.parts.ShippingEditor;

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
    
    @Inject
    private IEventBroker evtBroker;
    
	@Inject
	private ILocaleService localeUtil;
	
	@Inject
	private INumberFormatterService numberFormatterService;

    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

    private ComboFieldEditor currencyLocaleCombo;
    private Text example;
    private BooleanFieldEditor cashCheckbox;
    private BooleanFieldEditor thousandsSeparatorCheckbox;
    private RadioGroupFieldEditor useCurrencySymbolCheckbox;
    private IntegerFieldEditor decimalCurrencyPlaces, generalDecimalPlaces;

    private Group currencySettings;

    private IntegerFieldEditor backupSettingKeepNumberBackups;
    private IntegerFieldEditor backupSettingDeleteBackupsOlderThan;
    private Button radioButtonKeepNumberBackups;
    private Button radioButtonDeleteBackupsOlderThan;

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

		currencySettings = new Group(getFieldEditorParent(), SWT.SHADOW_IN | SWT.BORDER_SOLID);
        GridLayoutFactory.swtDefaults().margins(10, 20).numColumns(2).applyTo(currencySettings);
		currencySettings.setText(msg.preferencesGeneralCurrencyGroup);
        Locale[] locales = NumberFormat.getAvailableLocales();
        final Collator collator = Collator.getInstance(ULocale.getDefault());
        collator.setStrength(Collator.SECONDARY);
        List<Locale> currencyLocaleList = Arrays.stream(locales)
                .filter(l -> l.getCountry().length() == 2
                    && StringUtils.length(l.getLanguage()) < 3)
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
        
        currencyLocaleCombo = new ComboFieldEditor(Constants.PREFERENCE_CURRENCY_LOCALE, msg.preferencesGeneralCurrencyLocale, currencyLocales, currencySettings);
        addField(currencyLocaleCombo);
        
        WidgetFactory.label(SWT.NONE)
            .text(msg.preferencesGeneralCurrencyExample)
            .create(currencySettings);
        example = WidgetFactory.text(SWT.BORDER)
            .text(calculateExampleCurrencyFormatString(
        		super.getPreferenceStore().getString(Constants.PREFERENCE_CURRENCY_LOCALE), 
        		super.getPreferenceStore().getBoolean(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR), 
        		super.getPreferenceStore().getBoolean(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING), 
        		CurrencySettingEnum.valueOf(super.getPreferenceStore().getString(Constants.PREFERENCES_CURRENCY_USE_SYMBOL))))
            .layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
            .create(currencySettings);
        example.setEditable(false);
                
        cashCheckbox = new BooleanFieldEditor(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, msg.preferencesGeneralCurrencyCashrounding, currencySettings);
        cashCheckbox.getDescriptionControl(currencySettings).setToolTipText(msg.preferencesGeneralCurrencyCashroundingTooltip);
        String localeString = getPreferenceStore().getString(Constants.PREFERENCE_CURRENCY_LOCALE);
        if(!localeString.endsWith("CH")) {
            cashCheckbox.setEnabled(false, currencySettings);
        }
        addField(cashCheckbox);

        useCurrencySymbolCheckbox = new RadioGroupFieldEditor(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, "", 3, new String[][] { 
			{ msg.preferencesGeneralCurrencyUsesymbol, CurrencySettingEnum.SYMBOL.name() },
			{ msg.preferencesGeneralCurrencyUseisocode, CurrencySettingEnum.CODE.name() },
			{ msg.preferencesGeneralCurrencyUsenothing, CurrencySettingEnum.NONE.name() } },
                currencySettings);
        addField(useCurrencySymbolCheckbox);
        
        thousandsSeparatorCheckbox = new BooleanFieldEditor(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, msg.preferencesGeneralThousandseparator, currencySettings);
        addField(thousandsSeparatorCheckbox);
        
        decimalCurrencyPlaces = new IntegerFieldEditor(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, msg.preferencesGeneralCurrencyDecimalplaces, currencySettings);
        decimalCurrencyPlaces.setValidRange(0, 5);
        addField(decimalCurrencyPlaces);
        GridDataFactory.fillDefaults().indent(SWT.DEFAULT, 10).span(2, 1).applyTo(currencySettings);

        generalDecimalPlaces = new IntegerFieldEditor(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES, msg.preferencesGeneralQuantityDecimalplaces, getFieldEditorParent());
        generalDecimalPlaces.setValidRange(0, 5);     
        addField(generalDecimalPlaces);

        addField(useCurrencySymbolCheckbox);
        
        // Backup handling
        Group backupSettings = WidgetFactory.group(SWT.NONE)
            .layout(GridLayoutFactory.fillDefaults()
                    .numColumns(3).margins(2, 7).create())
            .layoutData(GridDataFactory.fillDefaults().span(2, 1).create())
            .create(getFieldEditorParent());
        backupSettings.setText(msg.preferencesGeneralBackupStrategy);
        
        Composite radioButtonContainer = WidgetFactory.composite(SWT.NONE)
            .layout(GridLayoutFactory.fillDefaults()
                    .numColumns(1).create())
            .layoutData(GridDataFactory.swtDefaults().span(1, 2).align(SWT.BEGINNING, SWT.BEGINNING).create())
            .create(backupSettings);
        Composite valueFieldContainer = WidgetFactory.composite(SWT.NONE)
            .layout(GridLayoutFactory.fillDefaults()
                    .numColumns(3).create())
            .layoutData(GridDataFactory.fillDefaults().span(1, 2).grab(true, false).create())
            .create(backupSettings);
        
        // get text blocks
        String[] messageForOlderThan = msg.preferencesGeneralBackupStrategyOlderthan.split("#");
        radioButtonDeleteBackupsOlderThan = WidgetFactory.button(SWT.RADIO)
            .text(messageForOlderThan[0])
            .create(radioButtonContainer);
        backupSettingDeleteBackupsOlderThan = new IntegerFieldEditor(Constants.PREFERENCES_GENERAL_DELETEBACKUPS_OLDER_THAN, "", valueFieldContainer) {

            @Override
            protected void valueChanged() {
                radioButtonKeepNumberBackups.setSelection(false);
                radioButtonDeleteBackupsOlderThan.setSelection(true);
                super.valueChanged();
            }
        };
        
        backupSettingDeleteBackupsOlderThan.getTextControl(valueFieldContainer).setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).create());
        backupSettingDeleteBackupsOlderThan.fillIntoGrid(valueFieldContainer, 2);
        backupSettingDeleteBackupsOlderThan.setValidRange(0, 360);
        backupSettingDeleteBackupsOlderThan.setErrorMessage(MessageFormat.format(msg.preferencesGeneralBackupStrategyInvalid, 0, 360));
        addField(backupSettingDeleteBackupsOlderThan);
        WidgetFactory.label(SWT.NONE).text(messageForOlderThan[1])
            .layoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).create())
            .create(backupSettings);

        String[] messageForKeepBackups = msg.preferencesGeneralBackupStrategyKeep.split("#");
        radioButtonKeepNumberBackups = WidgetFactory.button(SWT.RADIO)
            .text(messageForKeepBackups[0])
            .create(radioButtonContainer);
        backupSettingKeepNumberBackups = new IntegerFieldEditor(Constants.PREFERENCES_GENERAL_KEEP_NUMBER_BACKUPS, "", valueFieldContainer) {
            
            @Override
            protected void valueChanged() {
                radioButtonKeepNumberBackups.setSelection(true);
                radioButtonDeleteBackupsOlderThan.setSelection(false);
                super.refreshValidState();
            }
        };
        backupSettingKeepNumberBackups.setValidRange(0, 50);
        backupSettingKeepNumberBackups.setErrorMessage(MessageFormat.format(msg.preferencesGeneralBackupStrategyInvalid, 0, 50));
        
        backupSettingKeepNumberBackups.getLabelControl(valueFieldContainer).setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).create());
        backupSettingKeepNumberBackups.fillIntoGrid(valueFieldContainer, 2);
        WidgetFactory.label(SWT.NONE).text(messageForKeepBackups[1]).create(backupSettings);
        addField(backupSettingKeepNumberBackups);
        
        // Info: DB connection string
        WidgetFactory.label(SWT.NONE)
            .text(msg.preferencesGeneralDatabase)
            .create(getFieldEditorParent());
        Text dbConnectionInfo = WidgetFactory.text(SWT.BORDER)
            .layoutData(GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT).create())
            .text(getPreferenceStore().getString(PersistenceUnitProperties.JDBC_URL))
            .create(getFieldEditorParent());
        dbConnectionInfo.setEditable(false);
        
        WidgetFactory.button(SWT.PUSH)
            .text(msg.preferencesGeneralResetdialogsettings)
            .onSelect(e -> {
                    getPreferenceStore().setToDefault(Constants.DISPLAY_SUCCESSFUL_PRINTING);
                    MessageDialog.openInformation(getShell(), msg.dialogMessageboxTitleInfo, "Einstellung wurde zurückgesetzt.");
            })
            .layoutData(GridDataFactory.swtDefaults().indent(SWT.DEFAULT, 5).span(2, SWT.DEFAULT).create())
            .create(getFieldEditorParent());
	}
	
    @Override
    protected void initialize() {
        super.initialize();

        boolean isOlderThanSelected = getPreferenceStore().getString(Constants.PREFERENCES_BACKUP_STRATEGY).equals(Constants.PREFERENCES_GENERAL_DELETEBACKUPS_OLDER_THAN);
        radioButtonDeleteBackupsOlderThan.setSelection(isOlderThanSelected);
        radioButtonKeepNumberBackups.setSelection(!isOlderThanSelected);
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
            
            String preferenceName = "";
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
                        privateStringMethod.invoke(currencyLocaleCombo, currencySettings);
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
    private String calculateExampleCurrencyFormatString(String localeString, boolean useThousandsSeparator, boolean useCashRounding,
            CurrencySettingEnum currencySetting) {
        double myNumber = -1234.56864;
        String retval = "";
        Pattern pattern = Pattern.compile("(\\w{2})/(\\w{2})");
        Matcher matcher = pattern.matcher(localeString);
        if (matcher.matches() && matcher.groupCount() > 1) {
            String s = matcher.group(1);
            String s2 = matcher.group(2);
            ULocale locale = new ULocale(s, s2);

            // NumberFormat form;
            // if(currencySetting == CurrencySettingEnum.NONE) {
            // form = NumberFormat.getNumberInstance();
            // } else {
            // form = NumberFormat.getCurrencyInstance(locale);
            // }
            // form.setGroupingUsed(useThousandsSeparator);
            // form.setMinimumFractionDigits(decimalPlaces != null ?
            // decimalPlaces.getIntValue() : 2);
            // retval = form.format(myNumber);

            /* 
             * Can't work directly with JavaMoney classes (ServiceProviders) since
             * they already loaded by DataUtils and therefore the classloader gets
             * confused.
             */
            if (cashCheckbox != null) {
                cashCheckbox.setEnabled(locale.getCountry().equals("CH"), currencySettings);
            }
            retval = numberFormatterService.formatCurrency(myNumber, locale, currencySetting, cashCheckbox != null ? cashCheckbox.getBooleanValue() : true,
                    useThousandsSeparator);
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
		
		// at the moment we have to reset the DataUtils manually
		// TODO put it in a service!
		DataUtils.getInstance().refresh();

        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_DELETEBACKUPS_OLDER_THAN, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_GENERAL_KEEP_NUMBER_BACKUPS, write);
        preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_BACKUP_STRATEGY, write);
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
		node.setDefault(Constants.PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR, IPreferenceStore.FALSE);
		node.setDefault(Constants.PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS, IPreferenceStore.FALSE);
        node.setDefault(Constants.PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR, IPreferenceStore.TRUE);
        node.setDefault(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES, Integer.valueOf(2));
        node.setDefault(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES, Integer.valueOf(2));
        node.setDefault(Constants.PREFERENCES_CURRENCY_USE_CASHROUNDING, IPreferenceStore.FALSE);
        node.setDefault(Constants.PREFERENCES_CURRENCY_USE_SYMBOL, CurrencySettingEnum.SYMBOL.name());

		//Set the default currency locale from current locale
		ULocale defaultLocale = localeUtil.getCurrencyLocale();
		String currencyLocaleString = defaultLocale.getLanguage() + "/" + defaultLocale.getCountry();
		
		// Quick hack: numberFormatterService has to be re-initialized since the scale is 0 
		numberFormatterService.update();
		
		node.setDefault(Constants.PREFERENCE_CURRENCY_LOCALE, currencyLocaleString);
		CurrencySettingEnum currencySetting = CurrencySettingEnum.valueOf(node.getString(Constants.PREFERENCES_CURRENCY_USE_SYMBOL));
        String exampleFormat = calculateExampleCurrencyFormatString(currencyLocaleString, 
                true, false, currencySetting);
        node.setDefault(Constants.PREFERENCE_CURRENCY_FORMAT_EXAMPLE, exampleFormat);
        node.setDefault(Constants.PREFERENCES_GENERAL_DELETEBACKUPS_OLDER_THAN, Integer.valueOf(0));
        node.setDefault(Constants.PREFERENCES_GENERAL_KEEP_NUMBER_BACKUPS, Integer.valueOf(10));

        // use constant as strategy identifier
        node.setDefault(Constants.PREFERENCES_BACKUP_STRATEGY, Constants.PREFERENCES_GENERAL_DELETEBACKUPS_OLDER_THAN);
	}

	/**
	 * Update the currency Symbol for the whole application
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
    @Override
    public boolean performOk() {
        // modify backup options
        // if Opt1 was selected then delete value for Opt2 and vice versa
        if(radioButtonDeleteBackupsOlderThan.getSelection()) {
            getPreferenceStore().setValue(Constants.PREFERENCES_BACKUP_STRATEGY, Constants.PREFERENCES_GENERAL_DELETEBACKUPS_OLDER_THAN);
        } else if(radioButtonKeepNumberBackups.getSelection()) {
            getPreferenceStore().setValue(Constants.PREFERENCES_BACKUP_STRATEGY, Constants.PREFERENCES_GENERAL_KEEP_NUMBER_BACKUPS);
        }
        
        boolean preferencesSuccessfulStored = super.performOk();
        if (preferencesSuccessfulStored) {
            localeUtil.refresh();
            DataUtils.getInstance().refresh();
            numberFormatterService.update();

            // Refresh the table view of all documents
            evtBroker.post(DocumentEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(ProductEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(ReceiptVoucherEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(ExpenditureVoucherEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(ShippingEditor.EDITOR_ID, Editor.UPDATE_EVENT);
        }
        return preferencesSuccessfulStored;
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
