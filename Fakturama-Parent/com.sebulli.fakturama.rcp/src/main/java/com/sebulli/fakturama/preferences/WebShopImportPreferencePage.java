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
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.dialogs.WebShopStatusSettingsDialog;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.webshopimport.IWebshopConnectionService;
import com.sebulli.fakturama.webshopimport.Webshop;

/**
 * Preference page for the webshop settings
 * 
 * @author Gerd Bartelt
 */
public class WebShopImportPreferencePage extends FieldEditorPreferencePage implements IInitializablePreference {

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject @Optional
    private PreferencesInDatabase preferencesInDatabase;
    
    @Inject @Optional
    private IWebshopConnectionService webshopConnectionService;
    
    @Inject
    private IEclipseContext context;   

	/**
	 * Constructor
	 */
	public WebShopImportPreferencePage() {
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
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), ContextHelpConstants.WEBSHOP_IMPORT_PREFERENCE_PAGE);

		//T: Preference page "Web Shop Import" - Label checkbox "web shop enabled"
		addField(new BooleanFieldEditor(Constants.PREFERENCES_WEBSHOP_ENABLED, msg.preferencesWebshopEnabled, getFieldEditorParent()));
		addField(new StringFieldEditor(Constants.PREFERENCES_WEBSHOP_URL, msg.preferencesWebshopUrl, getFieldEditorParent()));
		addField(new StringFieldEditor(Constants.PREFERENCES_WEBSHOP_USER, msg.preferencesWebshopUser, getFieldEditorParent()));

		StringFieldEditor passwordEditor = new StringFieldEditor(Constants.PREFERENCES_WEBSHOP_PASSWORD, msg.startFirstSelectDbCredentialsPassword, getFieldEditorParent());
		passwordEditor.getTextControl(getFieldEditorParent()).setEchoChar('*');
		addField(passwordEditor);

		Webshop[] availableWebshops = getWebshopConnectionService().getAvailableWebshops();
		String[][] shoptypes = new String[availableWebshops.length][2];
		for (int i = 0; i < availableWebshops.length; i++) {
            Webshop webshop = availableWebshops[i];
            shoptypes[i][0]=webshop.getLabel();
            shoptypes[i][1]=webshop.name();
        }
		
        addField(new ComboFieldEditor(Constants.PREFERENCES_WEBSHOP_SHOPTYPE, "shop type and version", shoptypes, getFieldEditorParent()));
		
		addField(new StringFieldEditor(Constants.PREFERENCES_WEBSHOP_PRODUCT_CATEGORY, msg.preferencesWebshopLabelProductsincategory, getFieldEditorParent()));
		addField(new StringFieldEditor(Constants.PREFERENCES_WEBSHOP_CONTACT_CATEGORY, msg.preferencesWebshopLabelCustomersincategory, getFieldEditorParent()));
		addField(new StringFieldEditor(Constants.PREFERENCES_WEBSHOP_SHIPPING_CATEGORY, msg.preferencesWebshopLabelShippingsincategory, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.PREFERENCES_WEBSHOP_NOTIFY_PROCESSING, msg.preferencesWebshopNotifycustomerOnprogress, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.PREFERENCES_WEBSHOP_NOTIFY_SHIPPED, msg.preferencesWebshopNotifycustomerOnshipped, getFieldEditorParent()));
		addField(new IntegerFieldEditor(Constants.PREFERENCES_WEBSHOP_MAX_PRODUCTS, msg.preferencesWebshopMaxproducts, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS, msg.preferencesWebshopModifiedproducts, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Constants.PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR, msg.preferencesWebshopEanasitemno, getFieldEditorParent()));

		Button b = new Button(getFieldEditorParent(), SWT.PUSH);
		b.setText(msg.pageWebshopsettings);
		b.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
				context.set(Messages.class, msg);context.get("activeShell");
				WebShopStatusSettingsDialog dialog = ContextInjectionFactory.make(WebShopStatusSettingsDialog.class, context);
				dialog.open();				
		}));
	
	}
	
	

	/**
	 * Write or read the preference settings to or from the data base
	 * 
	 * @param write
	 *            TRUE: Write to the data base
	 */
	public void syncWithPreferencesFromDatabase(boolean write) {
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_ENABLED, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_URL, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_USER, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_PASSWORD, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_SHOPTYPE, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_PRODUCT_CATEGORY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_CONTACT_CATEGORY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_SHIPPING_CATEGORY, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_NOTIFY_PROCESSING, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_NOTIFY_SHIPPED, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_MAX_PRODUCTS, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS, write);
		preferencesInDatabase.syncWithPreferencesFromDatabase(Constants.PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR, write);
		
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
		node.setDefault(Constants.PREFERENCES_WEBSHOP_ENABLED, true);
		
		//T: Preference page "Web Shop Import" - Country specific URL of demo shop
		node.setDefault(Constants.PREFERENCES_WEBSHOP_URL, msg.preferencesWebshopDefaulturl);
		node.setDefault(Constants.PREFERENCES_WEBSHOP_USER, "user");
		node.setDefault(Constants.PREFERENCES_WEBSHOP_PASSWORD, "password");
		node.setDefault(Constants.PREFERENCES_WEBSHOP_SHOPTYPE, "LEGACY_WEBSHOP");
		//T: Preference page "Web Shop Import" - Default value "Product Category"
		node.setDefault(Constants.PREFERENCES_WEBSHOP_PRODUCT_CATEGORY, msg.preferencesWebshop);
		//T: Preference page "Web Shop Import" - Default value "Contact Category"
		node.setDefault(Constants.PREFERENCES_WEBSHOP_CONTACT_CATEGORY, msg.preferencesWebshopCustomer);
		//T: Preference page "Web Shop Import" - Default value "Shipping Category"
		node.setDefault(Constants.PREFERENCES_WEBSHOP_SHIPPING_CATEGORY, msg.preferencesWebshop);
		node.setDefault(Constants.PREFERENCES_WEBSHOP_NOTIFY_PROCESSING, false);
		node.setDefault(Constants.PREFERENCES_WEBSHOP_NOTIFY_SHIPPED, true);
		node.setDefault(Constants.PREFERENCES_WEBSHOP_MAX_PRODUCTS, "1000");
		node.setDefault(Constants.PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS, false);
		node.setDefault(Constants.PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR, false);
		
	}

    private IWebshopConnectionService getWebshopConnectionService() {
        if (webshopConnectionService == null) {
            ServiceReference<IWebshopConnectionService> serviceReference = Activator.getContext().getServiceReference(IWebshopConnectionService.class);
            if (serviceReference != null) {
                webshopConnectionService = Activator.getContext().getService(serviceReference);
            } else {
                MessageDialog.openError(getShell(), msg.dialogMessageboxTitleError, "Webshop connection service cannot be detected");
            }
        }
        return webshopConnectionService;
    }
}
