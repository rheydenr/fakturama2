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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.Product;

/**
 * Preference page for the number settings
 * 
 * @author Gerd Bartelt
 */
public class NumberRangeValuesPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {
    
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_PROFORMA_NR = "NUMBERRANGE_PROFORMA_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_DUNNING_NR = "NUMBERRANGE_DUNNING_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_CREDIT_NR = "NUMBERRANGE_CREDIT_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_CONFIRMATION_NR = "NUMBERRANGE_CONFIRMATION_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_ORDER_NR = "NUMBERRANGE_ORDER_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_OFFER_NR = "NUMBERRANGE_OFFER_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_DELIVERY_NR = "NUMBERRANGE_DELIVERY_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_INVOICE_NR = "NUMBERRANGE_INVOICE_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_PRODUCT_NR = "NUMBERRANGE_PRODUCT_NR";
    /**
     * 
     */
    public static final String PREFERENCES_NUMBERRANGE_DEBTOR_NR = "NUMBERRANGE_DEBTOR_NR";
    public static final String PREFERENCES_NUMBERRANGE_CREDITOR_NR = "NUMBERRANGE_CREDITOR_NR";

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;

	/**
	 * Constructor
	 */
	public NumberRangeValuesPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.NUMBERRANGE_PREFERENCE_PAGE);

		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_DEBTOR_NR, msg.preferencesNumberrangeValuesLabelNextdebitorno, getFieldEditorParent()));
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_CREDITOR_NR, msg.preferencesNumberrangeValuesLabelNextcreditorno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_PRODUCT_NR, msg.preferencesNumberrangeValuesLabelNextitemno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_INVOICE_NR, msg.preferencesNumberrangeValuesLabelNextinvoiceno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_DELIVERY_NR, msg.preferencesNumberrangeValuesLabelNextdeliverynoteno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_OFFER_NR, msg.preferencesNumberrangeValuesLabelNextofferno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_ORDER_NR, msg.preferencesNumberrangeValuesLabelNextorderno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_CONFIRMATION_NR, msg.preferencesNumberrangeValuesLabelNextconfirmno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_CREDIT_NR, msg.preferencesNumberrangeValuesLabelNextcreditno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_DUNNING_NR, msg.preferencesNumberrangeValuesLabelNextdunningno, getFieldEditorParent()));
		//T: Preference page "Number Range Values" - Label "next free number"
		addField(new IntegerFieldEditor(PREFERENCES_NUMBERRANGE_PROFORMA_NR, msg.preferencesNumberrangeValuesLabelNextproformano, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
	    return msg.preferencesNumberrangeValuesLabel;
	}
	
	/**
	 * Write or read the preference settings to or from the database
	 * 
	 * @param write
	 *            TRUE: Write to the database
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {

		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_DEBTOR_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_CREDITOR_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_PRODUCT_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_INVOICE_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_DELIVERY_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_OFFER_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_ORDER_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_CREDIT_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_CONFIRMATION_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_DUNNING_NR, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_NUMBERRANGE_PROFORMA_NR, write);
		
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
		node.setDefault(PREFERENCES_NUMBERRANGE_DEBTOR_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_CREDITOR_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_PRODUCT_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_INVOICE_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_DELIVERY_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_OFFER_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_ORDER_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_CREDIT_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_CONFIRMATION_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_DUNNING_NR, Integer.valueOf(1));
		node.setDefault(PREFERENCES_NUMBERRANGE_PROFORMA_NR, Integer.valueOf(1));
		
	    List<String> documentTypes = Arrays.stream(DocumentType.values())
	    		.filter(d -> d != DocumentType.NONE)
	    		.map(d -> d.getTypeAsString()).collect(Collectors.toList());
	    documentTypes.add(Debitor.class.getSimpleName());
	    documentTypes.add(Creditor.class.getSimpleName());
        documentTypes.add(Product.class.getSimpleName());
	    
		for (String editorId : documentTypes) {
            node.setDefault("last_setnextnr_date_" + editorId.toLowerCase(), "2000-01-01");
            node.setDefault("NUMBERRANGE_" + editorId.toUpperCase() + "_NR", Integer.valueOf(1));
        }
	}
}
