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

package com.sebulli.fakturama.webshopimport;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
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

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ProductCategoriesDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.ShippingCategoriesDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dao.WebshopDAO;
import com.sebulli.fakturama.handlers.WebShopCallHandler;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
//import com.sebulli.fakturama.model.CustomDocument;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.PaymentEditor;
import com.sebulli.fakturama.parts.ProductEditor;
import com.sebulli.fakturama.parts.ShippingEditor;
import com.sebulli.fakturama.parts.VatEditor;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;

/**
 * Web shop import manager. This class provides the functionality to connect to
 * the web shop and import the data, which is transmitted as a XML File. This 
 * file is created by a connector, which is individual for each shop system.
 * Look at Fakturama download page for further information. 
 * The WebshopImporter (which is started by this {@link WebShopImportManager}) creates the 
 * missing products, {@link VAT}s and {@link Document}s (orders in this case). 
 * 
 */
public class WebShopImportManager {
	
    @Inject
    @Translation
	private Messages msg;
    
    @Inject
    private EModelService modelService;
    
    @Inject
    private MApplication application;

    /**
     * contains the result from Web shop connector execution
     */
    private Object data;
    
    @Inject 
    private ILogger log;

    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject @Optional
	private IPreferenceStore preferences;

    @Inject 
    private IEclipseContext context;

    @Inject 
    private VatsDAO vatsDAO;
    
    @Inject 
    private DocumentsDAO documentsDAO;
    
    @Inject 
    private ProductsDAO productsDAO;
    
    @Inject 
    private ContactsDAO contactsDAO;
    
    @Inject 
    private ShippingCategoriesDAO shippingCategoriesDAO;
    
    @Inject 
    private ShippingsDAO shippingsDAO;
    
    @Inject 
    private PaymentsDAO paymentsDAO;
    
    @Inject 
    private ProductCategoriesDAO productCategoriesDAO;
    
	@Inject
	private WebshopDAO webshopStateMappingDAO;

	// The result of this import process
	private String runResult = "";

	private WebShopConnector conn;

	@CanExecute
	public boolean canExecute() {
	    // cancel if the webshop is disabled.
        return getPreferences().getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED);
	}
	
/* TODO use it!
		XMLParserActivator
*/

	
	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.webshopimport.IWebshopConnection#execute(org.eclipse.swt.widgets.Shell, java.lang.String)
	 */
	@Execute
	public ExecutionResult execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent,
	        @Optional @Named(WebShopCallHandler.PARAM_IS_GET_PRODUCTS) Boolean prepareGetProductsAndOrders,
	        @Named(WebShopCallHandler.PARAM_ACTION) String action) {
	    ExecutionResult executionResult = null;
	    
		String shopURL = preferences.getString(Constants.PREFERENCES_WEBSHOP_URL);
		
        conn = new WebShopConnector()
        		.withScriptURL(StringUtils.prependIfMissingIgnoreCase(shopURL, "http://", "https://", "file://"))
        		.withUseAuthorization(preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED))
        		.withAuthorizationUser(preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_USER))
        		.withAuthorizationPassword(preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD))
        		.withUser(preferences.getString(Constants.PREFERENCES_WEBSHOP_USER))
        		.withPassword(preferences.getString(Constants.PREFERENCES_WEBSHOP_PASSWORD));
	    
	    if(BooleanUtils.toBoolean(prepareGetProductsAndOrders)) {
	    	conn.prepareGetProductsAndOrders();
	    } else {
	    	conn.prepareChangeState();
	    }
	    
        try {
            ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
            WebShopDataImporter importOperation = ContextInjectionFactory.make(WebShopDataImporter.class, context);
            importOperation.setConnector(conn);
            progressMonitorDialog.run(true, true, importOperation);
            this.runResult = importOperation.getRunResult();
            executionResult = new ExecutionResult(getRunResult(), getRunResult().isEmpty() ? 0 : 1);
        }
          catch (InvocationTargetException e) {
              getLog().error(e, "Error running web shop import manager.");
              executionResult = new ExecutionResult("Error running web shop import manager.", 1);
          }
          catch (InterruptedException e) {
              getLog().error(e, "Web shop import manager was interrupted.");
              executionResult = new ExecutionResult("Web shop import manager was interrupted.", 2);
          }

		// If there is no error - interpret the data.
		if (executionResult.getErrorCode() != Constants.RC_OK) {
			// If there is an error - display it in a message box
			String errorMessage = StringUtils.abbreviate(executionResult.getErrorMessage(), 400);
			MessageDialog.openError(parent, getMsg().importWebshopActionError, errorMessage);
			log.error(errorMessage);
        } else {
        	MessageDialog.openInformation(parent, getMsg().importWebshopActionLabel, getMsg().importWebshopInfoSuccess);
		}

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
//		ViewDocumentTable viewDocumentTable = (ViewDocumentTable) view;
//		viewDocumentTable.getTopicTreeViewer().selectItemByName(DocumentType.ORDER.getPluralString() + "/" + DataSetDocument.getStringNOTSHIPPED());
        return executionResult;
	}

	/**
	 * Remove the HTML tags from the result
	 * 
	 * @return The formated run result string
	 */
	public String getRunResult() {
		return runResult.replaceAll("\\<.*?\\>", "");
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.webshopimport.IWebshopConnection#setRunResult(java.lang.String)
	 */
	public void setRunResult(String runResult) {
		this.runResult = runResult;
	}

	/**
	 * @return the preferences
	 */
	public IPreferenceStore getPreferences() {
		return preferences;
	}

	/**
	 * @param preferences the preferences to set
	 */
	public void setPreferences(IPreferenceStore preferences) {
		this.preferences = preferences;
	}

	/**
	 * @return the msg
	 */
	public Messages getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(Messages msg) {
		this.msg = msg;
	}

	/**
	 * @return the log
	 */
	public ILogger getLog() {
		return log;
	}

	/**
	 * @return the context
	 */
	public final IEclipseContext getContext() {
		return context;
	}

	/**
	 * @return the vatsDAO
	 */
	public final VatsDAO getVatsDAO() {
		return vatsDAO;
	}

	/**
	 * @return the documentsDAO
	 */
	public final DocumentsDAO getDocumentsDAO() {
		return documentsDAO;
	}

	/**
	 * @return the productsDAO
	 */
	public final ProductsDAO getProductsDAO() {
		return productsDAO;
	}

	/**
	 * @return the contactsDAO
	 */
	public final ContactsDAO getContactsDAO() {
		return contactsDAO;
	}

	/**
	 * @return the shippingCategoriesDAO
	 */
	public final ShippingCategoriesDAO getShippingCategoriesDAO() {
		return shippingCategoriesDAO;
	}

	/**
	 * @return the shippingsDAO
	 */
	public final ShippingsDAO getShippingsDAO() {
		return shippingsDAO;
	}

	/**
	 * @return the paymentsDAO
	 */
	public final PaymentsDAO getPaymentsDAO() {
		return paymentsDAO;
	}

	/**
	 * @return the productCategoriesDAO
	 */
	public final ProductCategoriesDAO getProductCategoriesDAO() {
		return productCategoriesDAO;
	}
	/**
	 * @return the webshopStateMappingDAO
	 */
	public final WebshopDAO getWebshopDAO() {
		return webshopStateMappingDAO;
	}

	/**
	 * @return the data
	 */
	public final Object getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public final void setData(Object data) {
		this.data = data;
	}
}