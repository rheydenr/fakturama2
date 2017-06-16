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

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the number settings
 * 
 * @author Gerd Bartelt
 */
public class NumberRangeFormatPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {

    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_PROFORMA_FORMAT = "NUMBERRANGE_PROFORMA_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_DUNNING_FORMAT = "NUMBERRANGE_DUNNING_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_CREDIT_FORMAT = "NUMBERRANGE_CREDIT_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_CONFIRMATION_FORMAT = "NUMBERRANGE_CONFIRMATION_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_ORDER_FORMAT = "NUMBERRANGE_ORDER_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_OFFER_FORMAT = "NUMBERRANGE_OFFER_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_DELIVERY_FORMAT = "NUMBERRANGE_DELIVERY_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_INVOICE_FORMAT = "NUMBERRANGE_INVOICE_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_PRODUCT_FORMAT = "NUMBERRANGE_PRODUCT_FORMAT";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_DEBITOR_FORMAT = "NUMBERRANGE_DEBITOR_FORMAT";
    public static final String PREFERENCES_NUMBERRANGE_CREDITOR_FORMAT = "NUMBERRANGE_CREDITOR_FORMAT";
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

	/**
	 * Constructor
	 */
	public NumberRangeFormatPreferencePage() {
		super(GRID);

	}

	/**
	 * Creates the page's field editors.
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.NUMBERRANGE_FORMAT_PREFERENCE_PAGE);

		//T: Preference page "Number Range Format" - Label "Format of the Customer ID"
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_DEBITOR_FORMAT, msg.preferencesNumberrangeFormatDebitornoLabel, getFieldEditorParent()));
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_CREDITOR_FORMAT, msg.preferencesNumberrangeFormatCreditornoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the item No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_PRODUCT_FORMAT, msg.preferencesNumberrangeFormatItemnoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the invoice No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_INVOICE_FORMAT, msg.preferencesNumberrangeFormatInvoicenoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the delivery note No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_DELIVERY_FORMAT, msg.preferencesNumberrangeFormatDeliverynotenoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the offer No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_OFFER_FORMAT, msg.preferencesNumberrangeFormatOffernoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the order No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_ORDER_FORMAT, msg.preferencesNumberrangeFormatOrdernoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the confirmation No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_CONFIRMATION_FORMAT, msg.preferencesNumberrangeFormatConfirmationoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the credit No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_CREDIT_FORMAT, msg.preferencesNumberrangeFormatCreditnoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the dunning No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_DUNNING_FORMAT, msg.preferencesNumberrangeFormatDunningnoLabel, getFieldEditorParent()));
		//T: Preference page "Number Range Format" - Label "Format of the proforma No."
		addField(new StringFieldEditor(PREFERENCES_NUMBERRANGE_PROFORMA_FORMAT, msg.preferencesNumberrangeFormatProformanoLabel, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {

		//T: Preference page "Number Range Format" - Title with an example of the format
	    return msg.preferencesNumberrangeFormatDescription;
	}
	
	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {

		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_DEBITOR_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_CREDITOR_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_PRODUCT_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_INVOICE_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_DELIVERY_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_OFFER_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_ORDER_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_CREDIT_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_CONFIRMATION_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_DUNNING_FORMAT, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_PROFORMA_FORMAT, write);
		
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
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Customer" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_DEBITOR_FORMAT, msg.preferencesNumberrangeFormatDebitornoValue);
		node.setDefault(PREFERENCES_NUMBERRANGE_CREDITOR_FORMAT, msg.preferencesNumberrangeFormatCreditornoValue);
		//Preference page "Number Range Format" - Default value: Abbreviation for "Product/Item" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_PRODUCT_FORMAT, "");
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Invoice" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_INVOICE_FORMAT, msg.preferencesNumberrangeFormatInvoicenoValue);
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Delivery Note" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_DELIVERY_FORMAT, msg.preferencesNumberrangeFormatDeliverynotenoValue);
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Offer" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_OFFER_FORMAT, msg.preferencesNumberrangeFormatOffernoValue);
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Order" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_ORDER_FORMAT, msg.preferencesNumberrangeFormatOrdernoValue);
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Credit Note" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_CREDIT_FORMAT, msg.preferencesNumberrangeFormatCreditnoValue);
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Confirmation" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_CONFIRMATION_FORMAT, msg.preferencesNumberrangeFormatConfirmationoValue);
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Dunning" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_DUNNING_FORMAT, msg.preferencesNumberrangeFormatDunningnoValue);
		//T: Preference page "Number Range Format" - Default value: Abbreviation for "Proforma" with {6nr} for a 6 digits number
		node.setDefault(PREFERENCES_NUMBERRANGE_PROFORMA_FORMAT, msg.preferencesNumberrangeFormatProformanoValue);
	}
}
