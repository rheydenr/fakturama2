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


/**
 * This action marks an entry in the order table as pending, processing, shipped
 * or checked.
 * 
 * @author Gerd Bartelt
 */
public class MarkOrderAsActionHandler {

	public final static int PENDING = 10;
	public final static int PROCESSING = 50;
	public final static int SHIPPED = 90;
	public final static int COMPLETED = 100;
	
	// progress of the order. Value from 0 to 100 (percent)
	int progress;

	/**
	 * Constructor Instead of using a value for the states "pending",
	 * "processing", "shipped" or "checked" a progress value from 0 to 100
	 * (percent) is used.
	 * 
	 * So it's possible to insert states between these.
	 * 
	 * @param text
	 * @param progress
	 */
	public MarkOrderAsActionHandler( int progress) {
		super();
		this.progress = progress;
//
//		// Correlation between progress value and state.
//		// Depending on the state, the icon and the command ID is selected.
//		switch (progress) {
//		case 0:
//		case PENDING:
//			//T: Text of the action
//			this.setText(_("mark as \"pending\""));
//			setSettings(ICommandIds.CMD_MARK_ORDER_AS, "/icons/16/order_pending_16.png");
//			break;
//		case PROCESSING:
//			//T: Text of the action
//			this.setText(_("mark as \"processing\""));
//			setSettings(ICommandIds.CMD_MARK_ORDER_AS, "/icons/16/order_processing_16.png");
//			break;
//		case SHIPPED:
//			//T: Text of the action
//			this.setText(_("mark as \"shipped\""));
//			setSettings(ICommandIds.CMD_MARK_ORDER_AS, "/icons/16/order_shipped_16.png");
//			break;
//		case COMPLETED:
//			//T: Text of the action
//			this.setText(_("mark as \"completed\""));
//			setSettings(ICommandIds.CMD_MARK_ORDER_AS, "/icons/16/checked_16.png");
//			break;
//		}

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
	 */
	public static void markOrderAs(Object uds, int progress, String comment, boolean sendNotification) {

//		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//
//		if (uds instanceof DataSetDocument) {
//
//			// Do it only, if it is an order.
//			if (DocumentType.getType(uds.getIntValueByKey("category")) == DocumentType.ORDER) {
//				
//				// begin Patch from Grucknak, see http://fakturama.sebulli.com/phorum/read.php?4,2503
//				int progress_old = uds.getIntValueByKey("progress");
//				// Stock
//				if (progress==SHIPPED && progress_old != SHIPPED) // mark as shipped - take from stock
//				{
//					ArrayList<DataSetItem> items = uds.getItems().getActiveDatasets();
//					for (DataSetItem item : items) {
//						int id = item.getIntValueByKey("productid");
//						int quantity_order = item.getIntValueByKey("quantity");
//						
//						ArrayList<DataSetProduct> products = Data.INSTANCE.getProducts().getActiveDatasets();
//						for (DataSetProduct product : products) {
//							if (product.getIntValueByKey("id") == id)
//							{
//								int quantity_stock =  product.getIntValueByKey("quantity");
//								product.setIntValueByKey("quantity", quantity_stock - quantity_order);
//								if ((quantity_stock - quantity_order) <= 0)
//								{
//									String name = product.getFormatedStringValueByKey("name");
//									String cat = product.getFormatedStringValueByKey("category");
//									MessageBox mb = new MessageBox(workbenchWindow.getShell(), SWT.ICON_WARNING);
//									//T: Stock is less or equal to zero for product / category
//									mb.setMessage(_("Stock is less or equal to zero for product / category: ") + name + "/" + cat);
//									//T: Title of a message box
//									mb.setText(_("Information"));
//									mb.open();
//								}
//								Data.INSTANCE.getProducts().updateDataSet(product);
//								break;
//							}
//						}
//					}
//				}
//				else if (progress_old == SHIPPED && progress != SHIPPED)  // mark as processing or lower - add to stock
//				{
//					ArrayList<DataSetItem> items = uds.getItems().getActiveDatasets();
//					for (DataSetItem item : items) {
//						int id = item.getIntValueByKey("productid");
//						int quantity_order = item.getIntValueByKey("quantity");
//						
//						ArrayList<DataSetProduct> products = Data.INSTANCE.getProducts().getActiveDatasets();
//						for (DataSetProduct product : products) {
//							if (product.getIntValueByKey("id") == id)
//							{
//								int quantity_stock =  product.getIntValueByKey("quantity");								
//								product.setIntValueByKey("quantity", quantity_stock + quantity_order);
//
//								Data.INSTANCE.getProducts().updateDataSet(product);
//								break;
//							}
//						}
//					}
//				}
//				// end patch
//
//				// change the state
//				uds.setIntValueByKey("progress", progress);
//
//				// also in the database
//				Data.INSTANCE.updateDataSet(uds);
//
//				// Change the state also in the webshop
//				if (!uds.getStringValueByKey("webshopid").isEmpty() && Activator.getDefault().getPreferenceStore().getBoolean("WEBSHOP_ENABLED")) {
//					// Start a new web shop import manager in a
//					// progress Monitor Dialog
//					WebShopImportManager webShopImportManager = new WebShopImportManager();
//					// Send a request to the web shop import manager.
//					// He will update the state in the web shop the next time,
//					// we synchronize with the shop.
//					WebShopImportManager.updateOrderProgress(uds, comment, sendNotification);
//					webShopImportManager.prepareChangeState();
//
//					try {
//						new ProgressMonitorDialog(workbenchWindow.getShell()).run(true, true, webShopImportManager);
//					}
//					catch (InvocationTargetException e) {
//						Logger.logError(e, "Error running web shop import manager.");
//					}
//					catch (InterruptedException e) {
//						Logger.logError(e, "Web shop import manager was interrupted.");
//					}
//
//				}
//
//			}
//		}

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
