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
import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.webshopimport.ExecutionResult;


/**
 * This action opens the documents in a table view.
 * 
 * @author Gerd Bartelt
 */
public class WebShopImportHandler {
    
    public static final int RC_OK = 0;
    
    @Inject
    @Translation
    private Messages msg;
    
    @Inject
    private Logger log;

    @Inject
    private ECommandService cmdService;
    
    @Inject
    private EHandlerService handlerService;
    
    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;
    
    @Inject
    @Preference(nodePath=Constants.DEFAULT_PREFERENCES_NODE)
    private IEclipsePreferences eclipseDefaultPrefs;

	@CanExecute
	public boolean canExecute() {
	    // cancel, if the webshop is disabled.
        return eclipsePrefs.getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED, 
                eclipseDefaultPrefs.getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED, false));
	}

	/**
	 * Run the action
	 * 
	 * Open the web shop import manager.
	 */
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
		
		// Start a new web shop import manager in a
		// progress Monitor Dialog
	    ParameterizedCommand command = cmdService.createCommand(CommandIds.CMD_WEBSHOP_IMPORT_MGR, null);
        ExecutionResult executionResult = (ExecutionResult) handlerService.executeHandler(command);

        if (executionResult != null && executionResult.getErrorCode() != RC_OK) {
            // If there is an error - display it in a message box
            String errorMessage = executionResult.getErrorMessage();
            if (errorMessage.length() > 400)
                errorMessage = errorMessage.substring(0, 400) + "...";
            MessageDialog.openError(parent, msg.importWebshopActionError, errorMessage);
        }

		// Refresh the views - this is done automatically because we use GlazedLists!
//		ApplicationWorkbenchAdvisor.refreshView(ViewProductTable.ID);
//		ApplicationWorkbenchAdvisor.refreshView(ViewContactTable.ID);
//		ApplicationWorkbenchAdvisor.refreshView(ViewPaymentTable.ID);
//		ApplicationWorkbenchAdvisor.refreshView(ViewShippingTable.ID);
//		ApplicationWorkbenchAdvisor.refreshView(ViewVatTable.ID);

		// After the web shop import, open the document view
		// and set the focus to the new imported orders.
//		ViewManager.showView(ViewDocumentTable.ID);
//		IViewPart view = ApplicationWorkbenchWindowAdvisor.getActiveWorkbenchWindow().getActivePage().findView(ViewDocumentTable.ID);
//		ViewDocumentTable viewDocumentTable = (ViewDocumentTable) view;
//		viewDocumentTable.getTopicTreeViewer().selectItemByName(DocumentType.ORDER.getPluralString() + "/" + DataSetDocument.getStringNOTSHIPPED());
	}
}
