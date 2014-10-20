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

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Preference page for the document settings
 * 
 * @author Gerd Bartelt
 */
public class DocumentPreferencePage extends FieldEditorPreferencePage {

	
	/* TRANSLATORS: The placeholder indicates the bug-reporting address
    for this package.  Please add _another line_ saying
    "Report translation bugs to <...>\n" with the address for translation
    bugs (typically your translation team's web or email address).  */

	
	
	/**
     * 
     */
    public static final String PREFERENCES_FINALPAYMENT_TEXT = "FINALPAYMENT_TEXT";
    /**
     * 
     */
    public static final String PREFERENCES_DEPOSIT_TEXT = "DEPOSIT_TEXT";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_MESSAGES = "DOCUMENT_MESSAGES";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD = "DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_DIALOG = "DOCUMENT_CUSTOMER_STATISTICS_DIALOG";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE = "DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_DELIVERY_NOTE_ITEMS_WITH_PRICE = "DOCUMENT_DELIVERY_NOTE_ITEMS_WITH_PRICE";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS = "DOCUMENT_USE_DISCOUNT_ALL_ITEMS";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_DISCOUNT_EACH_ITEM = "DOCUMENT_USE_DISCOUNT_EACH_ITEM";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_ITEM_POS = "DOCUMENT_USE_ITEM_POS";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_PREVIEW_PICTURE = "DOCUMENT_USE_PREVIEW_PICTURE";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG = "DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_COPY_MESSAGE_FROM_PARENT = "DOCUMENT_COPY_MESSAGE_FROM_PARENT";
    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_NET_GROSS = "DOCUMENT_USE_NET_GROSS";
    
    @Inject
    @Translation
    protected Messages msg;

    /**
	 * Constructor
	 */
	public DocumentPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.DOCUMENT_PREFERENCE_PAGE);

		//T: Preference page "Document" - Label "Format (net or gross) of the price in the item list"
		addField(new RadioGroupFieldEditor(PREFERENCES_DOCUMENT_USE_NET_GROSS, msg.preferencesDocumentUsenetgross, 2, new String[][] { 
					{ msg.productDataNet, "0" },
					{ msg.productDataGross, "1" } },
				getFieldEditorParent()));
		
		//T: Preference page "Document" - Label "Copy the content of the message field when creating a duplicate of the document."
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_COPY_MESSAGE_FROM_PARENT, msg.preferencesDocumentCopymsgfield, getFieldEditorParent()));
		//T: Preference page "Document" - Label "Copy the description in product selection dialog."
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG, msg.preferencesDocumentCopydescfield, getFieldEditorParent()));
		//T: Preference page "Document" 
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_USE_PREVIEW_PICTURE, msg.preferencesDocumentDisplaypreview, getFieldEditorParent()));
		//T: Preference page "Document" 
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_USE_ITEM_POS, msg.preferencesDocumentUsepos, getFieldEditorParent()));
		//T: Preference page "Document" 
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_USE_DISCOUNT_EACH_ITEM, msg.preferencesDocumentUsediscountsingle, getFieldEditorParent()));
		//T: Preference page "Document" 
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS, msg.preferencesDocumentUsediscountall, getFieldEditorParent()));
		//T: Preference page "Document" 
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_DELIVERY_NOTE_ITEMS_WITH_PRICE, msg.preferencesDocumentShowitemsprices, getFieldEditorParent()));
		//T: Preference page "Document" 
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE, msg.preferencesDocumentAdddelnotenumber, getFieldEditorParent()));
		//T: Preference page "Document" 
		addField(new BooleanFieldEditor(PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_DIALOG, msg.preferencesDocumentShowcustomerstat, getFieldEditorParent()));
		//T: Preference page "Document" - How to compare the address to generate the customer statistics
		addField(new RadioGroupFieldEditor(PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD, msg.preferencesDocumentLabelCompare, 2, new String[][] { 
					//T: Preference page "Document" - How to compare the address to generate the customer statistics
					{ msg.preferencesDocumentOnlycontactid, "0" },
					//T: Preference page "Document" - How to compare the address to generate the customer statistics
					{ msg.preferencesDocumentAlsoaddress, "1" } },
				getFieldEditorParent()));
		//T: Preference page "Document" 
		addField(new ComboFieldEditor(PREFERENCES_DOCUMENT_MESSAGES, msg.preferencesDocumentNumberofremarkfields, new String[][] { { "1", "1" }, { "2", "2" }, { "3", "3" }
			 }, getFieldEditorParent()));
		//T: Preference page "Document"
		addField(new StringFieldEditor(PREFERENCES_DEPOSIT_TEXT, msg.preferencesDocumentLabelDepositrow, getFieldEditorParent()));
		//T: Preference page "Document"
		addField(new StringFieldEditor(PREFERENCES_FINALPAYMENT_TEXT, msg.preferencesDocumentLabelFinalrow, getFieldEditorParent()));
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#getDescription()
     */
    @Override
    public String getDescription() {
        return msg.preferencesDocument;
    }

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public static void syncWithPreferencesFromDatabase(boolean write) {
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_USE_ITEM_POS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_USE_NET_GROSS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_COPY_MESSAGE_FROM_PARENT, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_USE_PREVIEW_PICTURE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_USE_DISCOUNT_EACH_ITEM, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_DELIVERY_NOTE_ITEMS_WITH_PRICE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_DIALOG, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DOCUMENT_MESSAGES, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_DEPOSIT_TEXT, write);
		PreferencesInDatabase.syncWithPreferencesFromDatabase(PREFERENCES_FINALPAYMENT_TEXT, write);
		}

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node, Messages msg) {
		node.setDefault(PREFERENCES_DOCUMENT_USE_NET_GROSS, "1");
		node.setDefault(PREFERENCES_DOCUMENT_COPY_MESSAGE_FROM_PARENT, false);
		node.setDefault(PREFERENCES_DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG, false);
		node.setDefault(PREFERENCES_DOCUMENT_USE_ITEM_POS, false);
		node.setDefault(PREFERENCES_DOCUMENT_USE_PREVIEW_PICTURE, true);
		node.setDefault(PREFERENCES_DOCUMENT_USE_DISCOUNT_EACH_ITEM, true);
		node.setDefault(PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS, true);
		node.setDefault(PREFERENCES_DOCUMENT_DELIVERY_NOTE_ITEMS_WITH_PRICE, true);
		node.setDefault(PREFERENCES_DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE, true);
		node.setDefault(PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_DIALOG, true);
		node.setDefault(PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD, "1");
		node.setDefault(PREFERENCES_DOCUMENT_MESSAGES, "1");
		node.setDefault(PREFERENCES_DEPOSIT_TEXT, msg.preferencesDocumentLabelDepositrow);
		node.setDefault(PREFERENCES_FINALPAYMENT_TEXT, msg.preferencesDocumentLabelFinalrow);
	}
}
