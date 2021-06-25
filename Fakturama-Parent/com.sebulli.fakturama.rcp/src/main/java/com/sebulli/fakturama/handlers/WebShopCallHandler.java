/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.PaymentEditor;
import com.sebulli.fakturama.parts.ProductEditor;
import com.sebulli.fakturama.parts.ShippingEditor;
import com.sebulli.fakturama.parts.VatEditor;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;
import com.sebulli.fakturama.webshopimport.ExecutionResult;
import com.sebulli.fakturama.webshopimport.WebShopConfig;
import com.sebulli.fakturama.webshopimport.WebShopController;
import com.sebulli.fakturama.webshopimport.Webshop;

/**
 * Handler for calling web shop import actions or getting status values from web shop. This
 * class is the main entry point for all actions concerning the web shop activities.
 *
 */
public class WebShopCallHandler {

    @Inject
    @Translation
    protected Messages msg;

    @Inject @Optional
    private IPreferenceStore preferences;
    
    @Inject
    private EModelService modelService;
    
    @Inject
    private MApplication application;

    @Inject 
    private ILogger log;

    /**
     * The action which has to be called.
     */
    public static final String PARAM_ACTION = "org.fakturama.webshop.connector.action";
    
    public static final String WEBSHOP_CONNECTOR_ACTION_IMPORT = "import-orders";

	/**
	 * Prepare the web shop import to request products and orders or to change
	 * the state of an order.
	 */
	public static final String PARAM_IS_GET_PRODUCTS = "com.sebulli.fakturama.webshopimport.prepareGetProductsAndOrders";
	public static final String PARAM_SELECTEDSHOPSYSTEM = "com.sebulli.fakturama.webshopimport.selectedshopsystem";

    public static final String ACTION_AVAILABLE_STATES = "getavailablestatus";
 
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject 
    private IEclipseContext context;

	@CanExecute
	public boolean canExecute() {
	    // cancel if the webshop is disabled.
        return preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED);
	}
	
	@Execute
	public ExecutionResult execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent,
	        @Optional @Named(WebShopCallHandler.PARAM_IS_GET_PRODUCTS) Boolean prepareGetProductsAndOrders,
	        @Optional @Named(WebShopCallHandler.PARAM_SELECTEDSHOPSYSTEM) String selectedShopSystemId,
	        @Named(WebShopCallHandler.PARAM_ACTION) String action) {

	    // The result of this import process
	    ExecutionResult executionResult = null;
	    
	    // Base URL points to where the API of the Shop starts
		String shopBaseURL = preferences.getString(Constants.PREFERENCES_WEBSHOP_URL);
	    
        WebshopCommand cmd = null;
        boolean refreshUI = false;
        if (BooleanUtils.toBoolean(prepareGetProductsAndOrders)) {
            cmd = WebshopCommand.GET_PRODUCTS_AND_ORDERS_AND_SYNCHRONIZEORDERS;
            refreshUI = true;
        } else {
            if (action != null) {
                if (ACTION_AVAILABLE_STATES.equals(action)) {
                    cmd = WebshopCommand.GET_AVAILABLE_STATES;
                } else if(CommandIds.CMD_MARK_ORDER_AS.equals(action)) {
                    cmd = WebshopCommand.CHANGE_STATE;
                }
            }
        }
        
        if(cmd == null) {
            return new ExecutionResult("no command to execute, aborting", 3);
        }
    	
	    if(selectedShopSystemId == null) {
	        // use default shop from preferences
	        selectedShopSystemId = preferences.getString(Constants.PREFERENCES_WEBSHOP_SHOPTYPE);
	    }
		Webshop selectedShopsystem = Webshop.valueOf(selectedShopSystemId);
		WebShopConfig conn = new WebShopConfig()
        		.withScriptURL(StringUtils.prependIfMissingIgnoreCase(shopBaseURL, "http://", "https://", "file://"))
        		.withUseAuthorization(preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED))
        		.withAuthorizationUser(preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_USER))
        		.withAuthorizationPassword(preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD))
        		.withUser(preferences.getString(Constants.PREFERENCES_WEBSHOP_USER))
        		.withPassword(preferences.getString(Constants.PREFERENCES_WEBSHOP_PASSWORD))
        		.withShopSystem(selectedShopsystem)
        		.withCommand(cmd)
        		;
	    
        try {
            ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
            
            IEclipseContext privateCtx = EclipseContextFactory.create("webshop-conn");
            privateCtx.set(WebShopConfig.class, conn);
            WebShopController importOperation = ContextInjectionFactory.make(WebShopController.class, context, privateCtx);

            progressMonitorDialog.run(true, true, importOperation);
            executionResult = new ExecutionResult(importOperation.getRunResult());
        } catch (InvocationTargetException e) {
            log.error(e, "Error running web shop import manager.");
            executionResult = new ExecutionResult("Error running web shop import manager.", 1);
        } catch (InterruptedException e) {
            log.error(e, "Web shop import manager was interrupted.");
            executionResult = new ExecutionResult("Web shop import manager was interrupted.", 2);
        }
	    
        if (executionResult.getErrorCode() != Constants.RC_OK) {
            // If there is an error - display it in a message box
            String errorMessage = StringUtils.abbreviate(executionResult.getErrorMessage(), 400);
            MessageDialog.openError(parent, msg.importWebshopActionError, errorMessage);
            log.error(errorMessage);
        } else {
            MessageDialog.openInformation(parent, msg.importWebshopActionLabel, msg.importWebshopInfoSuccess);
        }

        if (refreshUI) {
            // Refresh the views -> fire some update events
            // => the messages are handled by list views!
            evtBroker.post(ProductEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(DocumentEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(ContactEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(PaymentEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(ShippingEditor.EDITOR_ID, Editor.UPDATE_EVENT);
            evtBroker.post(VatEditor.EDITOR_ID, Editor.UPDATE_EVENT);

            // After the web shop import, open the document view
            // and set the focus to the new imported orders.
            MUIElement view = modelService.find(DocumentsListTable.ID, application);
            modelService.bringToTop(view);
            // ViewDocumentTable viewDocumentTable = (ViewDocumentTable) view;
            // viewDocumentTable.getTopicTreeViewer().selectItemByName(DocumentType.ORDER.getPluralString()
            // + "/" + DataSetDocument.getStringNOTSHIPPED());
        }
        return executionResult;
	}
}
