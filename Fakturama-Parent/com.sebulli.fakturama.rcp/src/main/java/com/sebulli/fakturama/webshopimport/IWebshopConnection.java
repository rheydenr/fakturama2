package com.sebulli.fakturama.webshopimport;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
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
import com.sebulli.fakturama.i18n.Messages;

public interface IWebshopConnection {

	/**
	 * This is the central execution entry point for the Webshop import process.
	 * 
	 * @param parent
	 * @return
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	ExecutionResult execute(Shell parent, String prepareGetProductsAndOrders);

	/**
	 * @param runResult the runResult to set
	 */
	void setRunResult(String runResult);

	String getRunResult();

	Messages getMsg();

	IPreferenceStore getPreferences();

	void readOrdersToSynchronize();

	boolean isGetOrders();

	boolean isGetProducts();

	Properties getOrderstosynchronize();

	Logger getLog();

	void setOrderstosynchronize(Properties orderstosynchronize);

	void saveOrdersToSynchronize();

	ProductCategoriesDAO getProductCategoriesDAO();

	PaymentsDAO getPaymentsDAO();

	ShippingsDAO getShippingsDAO();

	ShippingCategoriesDAO getShippingCategoriesDAO();

	ContactsDAO getContactsDAO();

	ProductsDAO getProductsDAO();

	DocumentsDAO getDocumentsDAO();

	VatsDAO getVatsDAO();

	IEclipseContext getContext();

	void setData(Object data);

	Object getData();

	WebshopDAO getWebshopDAO();

}