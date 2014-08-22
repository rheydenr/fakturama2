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


import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;

import com.sebulli.fakturama.i18n.Messages;


/**
 * This action opens the documents in a table view.
 * 
 * @author Gerd Bartelt
 */
public class WebShopImportHandler {
    
    @Inject
    @Translation
    protected Messages msg;

	//T: Text of the action to connect to the web shop and import new data
	public final static String ACTIONTEXT = "Web Shop"; 
//CommandIds.CMD_WEBSHOP_IMPORT
	
	@CanExecute
	public boolean canExecute() {
		boolean retval = false;
		// cancel, if the data base is not connected.
//		if (!DataBaseConnectionState.INSTANCE.isConnected())
//			return;
//
		// cancel, if the webshop is disabled.
//		if (!Activator.getDefault().getPreferenceStore().getBoolean("WEBSHOP_ENABLED"))
//			return;
		return retval;
	}

	/**
	 * Run the action
	 * 
	 * Open the web shop import manager.
	 */
	@Execute
	public void execute() {
		
		// Start a new web shop import manager in a
		// progress Monitor Dialog
//		WebShopImportManager webShopImportManager = new WebShopImportManager();
//		webShopImportManager.prepareGetProductsAndOrders();
//		IWorkbenchWindow workbenchWindow = ApplicationWorkbenchWindowAdvisor.getActiveWorkbenchWindow();
//		try {
//			new ProgressMonitorDialog(workbenchWindow.getShell()).run(true, true, webShopImportManager);
//
//			// If there is no error - interpret the data.
//			if (!webShopImportManager.getRunResult().isEmpty()) {
//				// If there is an error - display it in a message box
//				MessageBox messageBox = new MessageBox(ApplicationWorkbenchWindowAdvisor.getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR);
//				messageBox.setText(msg("Error importing data from web shop"));
//				String errorMessage = webShopImportManager.getRunResult();
//				if (errorMessage.length() > 400)
//					errorMessage = errorMessage.substring(0, 400) + "...";
//				messageBox.setMessage(errorMessage);
//				messageBox.open();
//			}
//		}
//		catch (InvocationTargetException e) {
//			Logger.logError(e, "Error running web shop import manager.");
//		}
//		catch (InterruptedException e) {
//			Logger.logError(e, "Web shop import manager was interrupted.");
//		}
//
//		// Refresh the views
//		ApplicationWorkbenchAdvisor.refreshView(ViewProductTable.ID);
//		ApplicationWorkbenchAdvisor.refreshView(ViewContactTable.ID);
//		ApplicationWorkbenchAdvisor.refreshView(ViewPaymentTable.ID);
//		ApplicationWorkbenchAdvisor.refreshView(ViewShippingTable.ID);
//		ApplicationWorkbenchAdvisor.refreshView(ViewVatTable.ID);
//
//		// After the web shop import, open the document view
//		// and set the focus to the new imported orders.
//		ViewManager.showView(ViewDocumentTable.ID);
//		IViewPart view = ApplicationWorkbenchWindowAdvisor.getActiveWorkbenchWindow().getActivePage().findView(ViewDocumentTable.ID);
//		ViewDocumentTable viewDocumentTable = (ViewDocumentTable) view;
//		viewDocumentTable.getTopicTreeViewer().selectItemByName(DocumentType.ORDER.getPluralString() + "/" + DataSetDocument.getStringNOTSHIPPED());
	}
}
