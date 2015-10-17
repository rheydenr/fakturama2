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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

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
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dialogs.OrderStatusDialog;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;
import com.sebulli.fakturama.webshopimport.WebShopImportManager;

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
    private Logger log;

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;

    @Inject
    private ProductsDAO productsDAO;

    @Inject
    private DocumentsDAO documentsDAO;

    @Inject
    private ECommandService cmdService;

    @Inject
    private EHandlerService handlerService;

    public static final String PARAM_STATUS = "com.sebulli.fakturama.command.order.markas.progress";
    public static final String PARAM_ORDERID = "com.sebulli.fakturama.command.order.markas.orderid";

    @CanExecute
    public boolean canExecute(@Active MPart activePart) {
        boolean retval = false;
        if (activePart.getElementId().contentEquals(DocumentsListTable.ID)) {
            @SuppressWarnings("rawtypes")
            AbstractViewDataTable currentListtable = (AbstractViewDataTable) activePart.getObject();
            Document[] selectedObjects = (Document[]) currentListtable.getSelectedObjects();
            retval = selectedObjects != null && Arrays.stream(selectedObjects).allMatch(doc -> doc.getBillingType() == BillingType.ORDER);
        }
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
            Document document = documents[i];
            markOrderAs(parent, document, progress, comment, sendNotification, iEclipseContext);
        }
    }

    public void markOrderAs(Shell parent, Document document, OrderState progress, String comment, boolean sendNotification, IEclipseContext iEclipseContext) {
        // Do it only, if it is an order.
        if (document.getBillingType() == BillingType.ORDER) {
            try {

                OrderState progress_old = OrderState.findByProgressValue(Optional.of(document.getProgress()));
                // Stock
                List<DocumentItem> items = document.getItems();
                if (progress == OrderState.SHIPPED && progress_old != OrderState.SHIPPED) // mark as shipped - take from stock
                {
                    for (DocumentItem item : items) {
                        Product id = item.getProduct();
                        Double quantity_order = item.getQuantity();
                        Product product = productsDAO.findById(id);
                        Double quantity_stock = product.getQuantity();
                        product.setQuantity(quantity_stock - quantity_order);
                        if (quantity_stock - quantity_order <= 0) {
                            String name = product.getName();
                            String cat = product.getCategories()/*.get(0)*/.getName();
                            MessageDialog.openWarning(parent, msg.dialogMessageboxTitleInfo, msg.commandMarkorderWarnStockzero + " " + name + "/" + cat);
                        }
                        productsDAO.update(product);
                    }
                }
                else if (progress_old == OrderState.SHIPPED && progress != OrderState.SHIPPED) // mark as processing or lower - add to stock
                {
                    // TODO DO THIS IN DAO!!!
                    for (DocumentItem item : items) {
                        Product id = item.getProduct();
                        Double quantity_order = item.getQuantity();

                        Product product = productsDAO.findById(id);
                        Double quantity_stock = product.getQuantity();
                        product.setQuantity(quantity_stock + quantity_order);
                        productsDAO.update(product);
                    }
                }
                // end patch

                // change the state
                document.setProgress(progress.getState());

                // also in the database
                documentsDAO.update(document);

                // Change the state also in the webshop
                if (!document.getWebshopId().isEmpty() && eclipsePrefs.getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED, Boolean.FALSE)) {

                    // Start a new web shop import manager in a
                    // progress Monitor Dialog
                    WebShopImportManager webShopImportManager = new WebShopImportManager();
                    ContextInjectionFactory.inject(webShopImportManager, iEclipseContext);
                    //                  webShopImportManager.initialize();
                    webShopImportManager.updateOrderProgress(document, comment, sendNotification);
                    // Send a request to the web shop import manager.
                    // It will update the state in the web shop the next time
                    // when we synchronize with the shop.
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put(WebShopImportHandler.PARAM_IS_GET_PRODUCTS, "FALSE");
                    ParameterizedCommand command = cmdService.createCommand(CommandIds.CMD_WEBSHOP_IMPORT_MGR, parameters);
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
            }
            catch (SQLException e) {
                // TODO Change it to an application exception!
                e.printStackTrace();
            }
        }
    }

    /**
     * Run the action Search all views to get the selected element. If a view
     * with an selection is found, change the state, if it was an order.
     */
    @Execute
    public void run(@Active MPart activePart, @Named(PARAM_STATUS) String status, @Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {

        OrderState progress = OrderState.NONE;
        progress = OrderState.valueOf(status);
        @SuppressWarnings("rawtypes")
        AbstractViewDataTable currentListtable = (AbstractViewDataTable) activePart.getObject();
        Document[] uds = (Document[]) currentListtable.getSelectedObjects();
        for (int i = 0; i < uds.length; i++) {
            Document document = uds[i];
            // Get the document
            // and the type of the document
            DocumentType documentType = DocumentType.findByKey(document.getBillingType().getValue());

            // Exit, if it was not an order
            if (documentType != DocumentType.ORDER)
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
