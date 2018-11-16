/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dialogs.OrderStatusDialog;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.ProductEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;
import com.sebulli.fakturama.webshopimport.OrderSyncManager;

/**
 * This action marks an entry in the order table as pending, processing, shipped
 * or checked.
 * 
 */
public class MarkOrderAsActionHandler {

    @Inject
    @Translation
    private Messages msg;

    @Inject
    private ILogger log;

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;

    @Inject
    private DocumentsDAO documentsDAO;

    @Inject
    private ECommandService cmdService;

    @Inject
    private EHandlerService handlerService;
    
    @Inject
    private ESelectionService selectionService;

    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @CanExecute
    public boolean canExecute(@Active MPart activePart, EPartService partService) {
        boolean retval = false;
        Document[] selectedObjects = null;
        
        if (activePart.getElementId().contentEquals(DocumentsListTable.ID)) {
            @SuppressWarnings("rawtypes")
            AbstractViewDataTable currentListtable = (AbstractViewDataTable) activePart.getObject();
			selectedObjects = (Document[]) currentListtable.getSelectedObjects();
        } else if(activePart.getElementId().contentEquals(DocumentEditor.ID)) {
        	DocumentEditor editor = (DocumentEditor)activePart.getObject();
        	selectedObjects = new Document[]{editor.getDocument()};
        }
        retval = selectedObjects != null && Arrays.stream(selectedObjects).allMatch(doc -> doc.getBillingType().isORDER());
        return retval;
    }

    public void markOrderAs(/*@Active */MPart activePart,
    /*@Named(PARAM_STATUS) */String status,
    /*@Named(IServiceConstants.ACTIVE_SHELL)*/Shell parent) {
        OrderState progress = OrderState.NONE;
        progress = OrderState.valueOf(status);
        @SuppressWarnings("rawtypes")
        AbstractViewDataTable currentListtable = (AbstractViewDataTable) activePart.getObject();
        markOrderAs(parent, (Document[]) currentListtable.getSelectedObjects(), progress, null, false, activePart.getContext());
    }

    /**
     * Set the progress of the order to a new state. Do it also in the web shop.
     * Send a comment by email.
     * 
     * @param documents
     *            The order
     * @param progress
     *            The new progress value (0-100%)
     * @param comment
     *            The comment of the confirmation email.
     * @param iEclipseContext
     */
    public void markOrderAs(Shell parent, Document[] documents, OrderState progress, String comment, boolean sendNotification, IEclipseContext iEclipseContext) {

        for (int i = 0; i < documents.length; i++) {
        	Document document = (Document) documents[i];
            markOrderAs(parent, document, progress, comment, sendNotification, iEclipseContext);
        }
    }

    public void markOrderAs(Shell parent, Document document, OrderState progress, String comment, boolean sendNotification, IEclipseContext iEclipseContext) {
    	boolean needUpdate = false ;  // if an update of views is needed
        // Do it only, if it is an order.
        if (document.getBillingType().isORDER()) {
            try {

            	// the object has to be refreshed, else no action is taken by saving 
            	// (even if some values are changed - the uow isn't updated :-( )
            	document = documentsDAO.update(document);
            	
            	// save the previous progress for further processing
            	Integer oldProgress = document.getProgress();

                // change the state
                document.setProgress(progress.getState());

                // also in the database 
                document = documentsDAO.save(document);
                
                // update stock quantity
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.PARAM_PROGRESS, oldProgress);
                params.put(Constants.PARAM_ORDERID, document);
                ParameterizedCommand stockUpdate = cmdService.createCommand(CommandIds.CMD_PRODUCTS_STOCKUPDATE, params);
                handlerService.executeHandler(stockUpdate);

                // Change the state also in the webshop
                if (StringUtils.isNotEmpty(document.getWebshopId()) && eclipsePrefs.getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED, Boolean.FALSE)) {

                	// sync orders with web shop
                	OrderSyncManager orderSyncManager = ContextInjectionFactory.make(OrderSyncManager.class, iEclipseContext);
                	orderSyncManager.updateOrderProgress(document, comment, sendNotification);
                	
                    // Send a request to the web shop import manager.
                    // It will update the state in the web shop the next time
                    // when we synchronize with the shop.
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put(WebShopCallHandler.PARAM_IS_GET_PRODUCTS, Boolean.FALSE.toString());
                    ParameterizedCommand command = cmdService.createCommand(CommandIds.CMD_WEBSHOP_IMPORT, parameters);
                    /*ExecutionResult executionResult = (ExecutionResult) */handlerService.executeHandler(command);
                    //                  webShopImportManager.prepareChangeState();
                    //
                    //                  try {
                    //
                    //                      ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
                    //                      progressMonitorDialog.run(true, true, webShopImportManager);
                    //                  }
                    //                  catch (InvocationTargetException e) {
                    //                      log.error(e, "Error running web shop import manager.");
                    //                  }
                    //                  catch (InterruptedException e) {
                    //                      log.error(e, "Web shop import manager was interrupted.");
                    //                  }

                }
                
                if (needUpdate) {
                    // Refresh the table view of all documents
                    evtBroker.post(ProductEditor.EDITOR_ID, Editor.UPDATE_EVENT);
                }
            }
            catch (FakturamaStoringException e) {
                log.error(e);
            }
        }
    }

    /**
     * Run the action Search all views to get the selected element. If a view
     * with an selection is found, change the state, if it was an order.
     */
    @SuppressWarnings("unchecked")
	@Execute
    public void run(@Active MPart activePart, @Named(Constants.PARAM_STATUS) String status, @Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {

        OrderState progress = OrderState.NONE;
        progress = OrderState.valueOf(status);
        List<Document> uds;
        if(activePart.getObject() instanceof DocumentsListTable) {
	        uds = (List<Document>)selectionService.getSelection();
        } else {
        	Document doc = (Document) ((DocumentEditor)activePart.getObject()).getDocument();
			uds = Arrays.asList(doc);
        }
        for (Document document : uds) {
            // Get the document and the type of the document
            // Exit, if it was not an order or if progress state hasn't changed
            if (!document.getBillingType().isORDER() || document.getProgress() == progress.getState())
                continue;

            String comment = "";
            boolean notify = false;

            // Notify the customer only if the web shop is enabled
            if (eclipsePrefs.getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED, Boolean.FALSE)) {
                if (progress == OrderState.PROCESSING && eclipsePrefs.getBoolean(Constants.PREFERENCES_WEBSHOP_NOTIFY_PROCESSING, Boolean.FALSE)
                        || (progress == OrderState.SHIPPED && eclipsePrefs.getBoolean(Constants.PREFERENCES_WEBSHOP_NOTIFY_SHIPPED, Boolean.FALSE))) {

                    //						        MDialog dlg = (MDialog)modelService.find("com.sebulli.fakturama.dialog.orderstatus", application);
                    //						        dlg.setVisible(true);
                    //						        modelService.bringToTop(dlg);

                    OrderStatusDialog dlg = new OrderStatusDialog(parent, msg);

                    if (dlg.open() == Window.OK) {

                        // User clicked OK; update the label with the input
                        try {
                            // Encode the comment to send it via HTTP POST request
                            comment = java.net.URLEncoder.encode(dlg.getComment(), "UTF-8");
                        }
                        catch (UnsupportedEncodingException e) {
                            log.error(e, "Error encoding comment.");
                            comment = "";
                        }
                    }
                    else {
                        return;
                    }
                    notify = dlg.getNotify();
                }
            }

            // Mark the order as ...
            markOrderAs(parent, document, progress, comment, notify, activePart.getContext());
        }
    }
}
