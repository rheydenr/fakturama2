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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OrderState;
//import com.sebulli.fakturama.model.CustomDocument;
import com.sebulli.fakturama.model.Document;

/**
 * Web shop import manager. This class provides the functionality to connect to
 * the web shop and import the data, which is transmitted as a XML File. This 
 * file is created by a connector, which is individual for each shop system.
 * Look at Fakturama download page for further information. 
 * The WebshopImporter (which is started by this {@link WebShopImportManager}) creates the 
 * missing products, VATs and documents (orders in this case). 
 * 
 */
public class WebShopImportManager implements IWebshopConnection {
	
    /**
     * Prepare the web shop import to request products and orders or to change
     * the state of an order.
     */
    public static final String PARAM_IS_GET_PRODUCTS = "com.sebulli.fakturama.webshopimport.prepareGetProductsAndOrders";

    @Inject
    @Translation
	private Messages msg;

    /**
     * contains the result from Web shop connector execution
     */
    private Object data;
    
    @Inject 
    private Logger log;
    
    @Inject
	private IPreferenceStore preferences;

    @Inject IEclipseContext context;

    @Inject VatsDAO vatsDAO;
    
    @Inject DocumentsDAO documentsDAO;
    
    @Inject ProductsDAO productsDAO;
    
    @Inject ContactsDAO contactsDAO;
    
    @Inject ShippingCategoriesDAO shippingCategoriesDAO;
    
    @Inject ShippingsDAO shippingsDAO;
    
    @Inject PaymentsDAO paymentsDAO;
    
    @Inject ProductCategoriesDAO productCategoriesDAO;
	        
	// List of all orders which are out of sync with the web shop.
	private Properties orderstosynchronize = null;

	// The result of this import process
	private String runResult = "";

	// Configuration of the web shop request
	private boolean getProducts, getOrders;

	private String generalWorkspace;

	@CanExecute
	public boolean canExecute() {
	    // cancel if the webshop is disabled.
        return getPreferences().getBoolean(Constants.PREFERENCES_WEBSHOP_ENABLED);
	}
	
	@PostConstruct
	public void initialize() {
/* TODO use it!
		XMLParserActivator
*/
		generalWorkspace = getPreferences().getString(Constants.GENERAL_WORKSPACE);
	}

	/**
	 * Prepare the web shop import to request products and orders.
	 */
	private void prepareGetProductsAndOrders() {
		setGetProducts(true);
		setGetOrders(true);
	}

	/**
	 * Prepare the web shop import to change the state of an order.
	 */
	private void prepareChangeState() {
		setGetProducts(false);
		setGetOrders(false);
	}

	
	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.webshopimport.IWebshopConnection#execute(org.eclipse.swt.widgets.Shell, java.lang.String)
	 */
	@Override
	@Execute
	public ExecutionResult execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent,
	        @Optional @Named(PARAM_IS_GET_PRODUCTS) String prepareGetProductsAndOrders) {
	    ExecutionResult executionResult = null;
	    if(BooleanUtils.toBoolean(prepareGetProductsAndOrders)) {
	        prepareGetProductsAndOrders();
	    } else {
	        prepareChangeState();
	    }
        try {
            ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
            IRunnableWithProgress op = new WebShopImportWorker(this);
            progressMonitorDialog.run(true, true, op);
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
        return executionResult;
	}

	/**
	 * Save the list of all orders, which are out of sync with the web shop to
	 * file system
	 * 
	 */
	@Override
	public void saveOrdersToSynchronize() {
		if (getOrderstosynchronize().isEmpty())
			return;

		try (Writer writer = new FileWriter(generalWorkspace + "/orders2sync.txt")) {
			getOrderstosynchronize().store(writer, "OrdersNotInSyncWithWebshop");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update the progress of an order.
	 * 
	 * @param uds
	 *            The UniDataSet with the new progress value
	 */
	public void updateOrderProgress(Document uds, String comment, boolean notify) {

		// Get the progress value of the UniDataSet
		String orderId = uds.getWebshopId();
		int progress = uds.getProgress();
		int webshopState;

		// Get the orders that are out of sync with the shop
		readOrdersToSynchronize();

		// Convert a percent value of 0..100% to a state of 1,2,3
		if (progress >= OrderState.SHIPPED.getState())
			webshopState = 3;
		else if (progress >= OrderState.PROCESSING.getState())
			webshopState = 2;
		else
			webshopState = 1;

		// Set the new progress state 
		// Add an "*" to mark the ID as "notify customer"
		String value = Integer.toString(webshopState);

		//Replace the "," by "&comma;
		comment = java.util.Optional.ofNullable(comment).orElse("").replace("%2C", "%26comma%3B");
		//Replace the "=" by "&equal;
		comment = java.util.Optional.ofNullable(comment).orElse("").replace("%3D", "%26equal%3B");
		
		if (notify)
			value += "*" + comment;

		getOrderstosynchronize().setProperty(orderId, value);
		saveOrdersToSynchronize();
	}

	/**
     * Read the list of all orders, which are out of sync with the web shop
     * from the file system
     * 
     */
    @Override
	public void readOrdersToSynchronize() {
        setOrderstosynchronize(new Properties());
        try (Reader reader = new FileReader(generalWorkspace + "/orders2sync.txt")) {
            getOrderstosynchronize().load(reader);
        } catch (FileNotFoundException fnex) {
            getLog().warn(fnex, "file not found: orders2sync.txt (will be created next time)");
        } catch (IOException e) {
            getLog().error(e);
        }
    }

	/**
	 * Remove the HTML tags from the result
	 * 
	 * @return The formated run result string
	 */
	@Override
	public String getRunResult() {
		return runResult.replaceAll("\\<.*?\\>", "");
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.webshopimport.IWebshopConnection#setRunResult(java.lang.String)
	 */
	@Override
	public void setRunResult(String runResult) {
		this.runResult = runResult;
	}

	/**
	 * @return the preferences
	 */
	@Override
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
	@Override
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
	 * @return the getProducts
	 */
	@Override
	public final boolean isGetProducts() {
		return getProducts;
	}

	/**
	 * @param getProducts the getProducts to set
	 */
	public final void setGetProducts(boolean getProducts) {
		this.getProducts = getProducts;
	}

	/**
	 * @return the getOrders
	 */
	@Override
	public final boolean isGetOrders() {
		return getOrders;
	}

	/**
	 * @param getOrders the getOrders to set
	 */
	public final void setGetOrders(boolean getOrders) {
		this.getOrders = getOrders;
	}

	/**
	 * @return the orderstosynchronize
	 */
	@Override
	public Properties getOrderstosynchronize() {
		return orderstosynchronize;
	}

	/**
	 * @param orderstosynchronize the orderstosynchronize to set
	 */
	@Override
	public void setOrderstosynchronize(Properties orderstosynchronize) {
		this.orderstosynchronize = orderstosynchronize;
	}

	/**
	 * @return the log
	 */
	@Override
	public Logger getLog() {
		return log;
	}

	/**
	 * @return the context
	 */
	@Override
	public final IEclipseContext getContext() {
		return context;
	}

	/**
	 * @return the vatsDAO
	 */
	@Override
	public final VatsDAO getVatsDAO() {
		return vatsDAO;
	}

	/**
	 * @return the documentsDAO
	 */
	@Override
	public final DocumentsDAO getDocumentsDAO() {
		return documentsDAO;
	}

	/**
	 * @return the productsDAO
	 */
	@Override
	public final ProductsDAO getProductsDAO() {
		return productsDAO;
	}

	/**
	 * @return the contactsDAO
	 */
	@Override
	public final ContactsDAO getContactsDAO() {
		return contactsDAO;
	}

	/**
	 * @return the shippingCategoriesDAO
	 */
	@Override
	public final ShippingCategoriesDAO getShippingCategoriesDAO() {
		return shippingCategoriesDAO;
	}

	/**
	 * @return the shippingsDAO
	 */
	@Override
	public final ShippingsDAO getShippingsDAO() {
		return shippingsDAO;
	}

	/**
	 * @return the paymentsDAO
	 */
	@Override
	public final PaymentsDAO getPaymentsDAO() {
		return paymentsDAO;
	}

	/**
	 * @return the productCategoriesDAO
	 */
	@Override
	public final ProductCategoriesDAO getProductCategoriesDAO() {
		return productCategoriesDAO;
	}
	/**
	 * @return the data
	 */
	@Override
	public final Object getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	@Override
	public final void setData(Object data) {
		this.data = data;
	}
}