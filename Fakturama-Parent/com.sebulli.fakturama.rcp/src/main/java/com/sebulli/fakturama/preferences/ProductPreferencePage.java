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
import java.lang.reflect.Method;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.emf.common.util.Reflect;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;

/**
 * Preference page for the product settings
 * 
 * @author Gerd Bartelt
 */
public class ProductPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

	private BooleanFieldEditor useQuantityCheckbox;

	private RadioGroupFieldEditor radioGroupQtyChange;

    /**
	 * Constructor
	 */
	public ProductPreferencePage() {
		super(GRID);

	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		
//		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.PRODUCT_PREFERENCE_PAGE);

		//T: Preference page "Product" - Label "Use item No."
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_ITEMNR, msg.preferencesProductUseitemno, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use Quantity Unit"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_QUNIT, msg.preferencesProductUsequantityunit, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use description"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_DESCRIPTION, msg.preferencesProductUsedescription, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use gross or net or both"
		addField(new RadioGroupFieldEditor(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS, msg.preferencesProductNetorgrossprices, 3, new String[][] { 
				{ msg.productDataNet, "1" },
				{ msg.productDataGross, "2" },
				//T: Preference page "Product" - Label "Use both: net and gross"
				{ msg.preferencesProductNetandgross, "0" } }, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use scaled prices"
		addField(new ComboFieldEditor(Constants.PREFERENCES_PRODUCT_SCALED_PRICES, msg.preferencesProductScaledprices, new String[][] { { "--", "1" }, { "2", "2" }, { "3", "3" }, { "4", "4" },
				{ "5", "5" } }, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Possibility to select the VAT"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_VAT, msg.preferencesProductSelectvat, getFieldEditorParent()));

		//T: Preference page "Product" - Label "Use weight"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_WEIGHT, msg.preferencesProductUseweight, getFieldEditorParent()));
		
		useQuantityCheckbox = new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_QUANTITY, msg.preferencesProductUsequantity, getFieldEditorParent());
		addField(useQuantityCheckbox);
		
		radioGroupQtyChange = new RadioGroupFieldEditor(Constants.PREFERENCES_PRODUCT_CHANGE_QTY, msg.preferencesProductQtyHeader, 3, new String[][] { 
			{ msg.preferencesProductQtyChangeOrder, Constants.PREFERENCES_PRODUCT_CHANGE_QTY_ORDER },
			{ msg.preferencesProductQtyChangeDelivery, Constants.PREFERENCES_PRODUCT_CHANGE_QTY_DELIVERY },
			{ msg.preferencesProductQtyChangeInvoice, Constants.PREFERENCES_PRODUCT_CHANGE_QTY_INVOICE } }, getFieldEditorParent());
		addField(radioGroupQtyChange);
		
		//T: Preference page "Product" - Label "Use product picture"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_PRODUCT_USE_PICTURE, msg.preferencesProductUsepicture, getFieldEditorParent()));
		
		radioGroupQtyChange.setEnabled(useQuantityCheckbox.getBooleanValue(), getFieldEditorParent());
	}
	
	/**
	 * Some values depends from each other. This method listens to changes for some values and adapt them if necessary.
	 */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String preferenceName = null;
        super.propertyChange(event);
        
        Object eventSource = event.getSource();
		if (eventSource instanceof BooleanFieldEditor) {
			preferenceName = ((BooleanFieldEditor)eventSource).getPreferenceName();
        }
        if (StringUtils.equalsIgnoreCase(preferenceName, Constants.PREFERENCES_PRODUCT_USE_QUANTITY)) {
        	radioGroupQtyChange.setEnabled(((BooleanFieldEditor)eventSource).getBooleanValue(), getFieldEditorParent());
        }
    }
	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_ITEMNR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_QUNIT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_DESCRIPTION, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_SCALED_PRICES, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_VAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_WEIGHT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_QUANTITY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_CHANGE_QTY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_PRODUCT_USE_PICTURE, write);
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
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_ITEMNR, true);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_QUNIT, false);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_DESCRIPTION, true);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS, Integer.valueOf(2));
		node.setDefault(Constants.PREFERENCES_PRODUCT_SCALED_PRICES, Integer.valueOf(1));
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_VAT, true);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_WEIGHT, false);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_QUANTITY, true);
		node.setDefault(Constants.PREFERENCES_PRODUCT_CHANGE_QTY, Constants.PREFERENCES_PRODUCT_CHANGE_QTY_INVOICE);
		node.setDefault(Constants.PREFERENCES_PRODUCT_USE_PICTURE, true);
	}
}
