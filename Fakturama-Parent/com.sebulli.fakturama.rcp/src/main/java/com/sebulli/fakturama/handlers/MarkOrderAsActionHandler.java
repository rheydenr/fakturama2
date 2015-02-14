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

package com.sebulli.fakturama.handlers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.util.ProductUtil;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
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
//    
//    @Inject
//    private Logger log;
    
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
	
	@Execute
	public void markOrderAs(@Active MPart activePart,
	        @Named(PARAM_STATUS) String status,
	        @Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
	    OrderState progress = OrderState.NONE;
	    progress = OrderState.valueOf(status);
	    @SuppressWarnings("rawtypes")
        AbstractViewDataTable currentListtable = (AbstractViewDataTable)activePart.getObject();
	    markOrderAs(parent, (Document)currentListtable.getSelectedObject(), progress, null, false, activePart.getContext());
	}


	/**
	 * Set the progress of the order to a new state. Do it also in the web shop.
	 * Send a comment by email.
	 * 
	 * @param uds
	 *            The order
	 * @param progress
	 *            The new progress value (0-100%)
	 * @param comment
	 *            The comment of the confirmation email.
	 * @param iEclipseContext 
	 */
	public void markOrderAs(Shell parent, Document uds, OrderState progress, String comment, boolean sendNotification, IEclipseContext iEclipseContext) {
			// Do it only, if it is an order.
			if (uds.getBillingType() == BillingType.ORDER) {
						try {
				
			    OrderState progress_old = OrderState.findByProgressValue(uds.getProgress());
				// Stock
			    List<DocumentItem> items = uds.getItems();
				if (progress==OrderState.SHIPPED && progress_old != OrderState.SHIPPED) // mark as shipped - take from stock
				{
					for (DocumentItem item : items) {
						Product id = item.getProduct();
						Double quantity_order = item.getQuantity();
						Product product = productsDAO.findById(id);
						Double quantity_stock =  product.getQuantity();
						product.setQuantity(quantity_stock - quantity_order);
						if (quantity_stock - quantity_order <= 0)
						{
							String name = product.getName();
							String cat = product.getCategories().get(0).getName();
							MessageDialog.openWarning(parent, msg.dialogMessageboxTitleInfo, msg.commandMarkorderWarnStockzero+" " + name + "/" + cat);
						}
                            productsDAO.update(product);
					}
				}
				else if (progress_old == OrderState.SHIPPED && progress != OrderState.SHIPPED)  // mark as processing or lower - add to stock
				{
                    for (DocumentItem item : items) {
                        Product id = item.getProduct();
                        Double quantity_order = item.getQuantity();
						
                        Product product = productsDAO.findById(id);
                        Double quantity_stock =  product.getQuantity();
                        product.setQuantity(quantity_stock + quantity_order);
                        productsDAO.update(product);
					}
				}
				// end patch

				// change the state
				uds.setProgress(progress.getState());

				// also in the database
				documentsDAO.update(uds);

				// Change the state also in the webshop
				if (!uds.getWebshopId().isEmpty() && eclipsePrefs.getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED, Boolean.FALSE)) {

				    // Start a new web shop import manager in a
					// progress Monitor Dialog
				    WebShopImportManager webShopImportManager = new WebShopImportManager(); 
		            ContextInjectionFactory.inject(webShopImportManager, iEclipseContext);
//				    webShopImportManager.initialize();
					webShopImportManager.updateOrderProgress(uds, comment, sendNotification);
					// Send a request to the web shop import manager.
					// It will update the state in the web shop the next time
					// when we synchronize with the shop.
			        Map<String, Object> parameters = new HashMap<>();
			        parameters.put(WebShopImportHandler.PARAM_IS_GET_PRODUCTS, "FALSE");
			        ParameterizedCommand command = cmdService.createCommand(CommandIds.CMD_WEBSHOP_IMPORT_MGR, parameters);
			        /*ExecutionResult executionResult = (ExecutionResult) */handlerService.executeHandler(command);
//					webShopImportManager.prepareChangeState();
//
//					try {
//
//			            ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
//			            progressMonitorDialog.run(true, true, webShopImportManager);
//					}
//					catch (InvocationTargetException e) {
//						log.error(e, "Error running web shop import manager.");
//					}
//					catch (InterruptedException e) {
//						log.error(e, "Web shop import manager was interrupted.");
//					}

				}
                        }
                        catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }}

	}

	/**
	 * Run the action Search all views to get the selected element. If a view
	 * with an selection is found, change the state, if it was an order.
	 */
	public void run() {
//
//		// cancel, if the data base is not connected.
//		if (!DataBaseConnectionState.INSTANCE.isConnected())
//			return;
//
//		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		IWorkbenchPage page = workbenchWindow.getActivePage();
//
//		// Get the active part (view)
//		IWorkbenchPart part = null;
//		if (page != null)
//			part = page.getActivePart();
//
//		ISelection selection;
//
//		// Cast the part to ViewDataSetTable
//		if (part instanceof ViewDataSetTable) {
//
//			ViewDataSetTable view = (ViewDataSetTable) part;
//
//			// does the view exist ?
//			if (view != null) {
//
//				//get the selection
//				selection = view.getSite().getSelectionProvider().getSelection();
//
//				if (selection != null && selection instanceof IStructuredSelection) {
//
//					Object obj = ((IStructuredSelection) selection).getFirstElement();
//
//					// If there is a selection let change the state
//					if (obj != null) {
//
//						// Get the document
//						DataSetDocument uds = (DataSetDocument) obj;
//						// and the type of the document
//						DocumentType documentType = DocumentType.getType(uds.getCategory());
//
//						// Exit, if it was not an order
//						if (documentType != DocumentType.ORDER)
//							return;
//						
//
//						String comment = "";
//						boolean notify = false;
//
//						// Notify the customer only if the web shop is enabled
//						if (Activator.getDefault().getPreferenceStore().getBoolean("WEBSHOP_ENABLED")) {
//							if ((progress == PROCESSING) && Activator.getDefault().getPreferenceStore().getBoolean("WEBSHOP_NOTIFY_PROCESSING")
//									|| ((progress == SHIPPED) && Activator.getDefault().getPreferenceStore().getBoolean("WEBSHOP_NOTIFY_SHIPPED"))) {
//
//								OrderStatusDialog dlg = new OrderStatusDialog(workbenchWindow.getShell());
//
//								if (dlg.open() == Window.OK) {
//
//									// User clicked OK; update the label with the input
//									try {
//										// Encode the comment to send it via HTTP POST request
//										comment = java.net.URLEncoder.encode(dlg.getComment(), "UTF-8");
//									}
//									catch (UnsupportedEncodingException e) {
//										Logger.logError(e, "Error encoding comment.");
//										comment = "";
//									}
//								}
//								else
//									return;
//
//								notify = dlg.getNotify();
//							}
//							
//						}
//
//						// Mark the order as ...
//						markOrderAs(uds, progress, comment, notify);
//
//						// Refresh the table with orders.
//						view.refresh();
//
//					}
//				}
//			}
//		}
	}
}
