/*******************************************************************************
 * Copyright (c) 2012 Marco Descher.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Descher - initial API and implementation
 ******************************************************************************/
package com.sebulli.fakturama.resources.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import com.sebulli.fakturama.resources.Activator;

public enum Icon {
	// @formatter:off
	// commands (16x16)
	COMMAND_ACCOUNTS,
	COMMAND_APP,
	COMMAND_BUYERS,
	COMMAND_CALCULATOR,
	COMMAND_CHECKED,
	COMMAND_CHECKED_GREY,
	COMMAND_COLLECTIVE_INVOICE,
	COMMAND_CONFIRMATION,
	COMMAND_CONTACT,
	COMMAND_CONTACT_PLUS,
	COMMAND_COUNTRY,
	COMMAND_CREDIT,
	COMMAND_DATA,
	COMMAND_DELETE,
	COMMAND_DELIVERY,
	COMMAND_DOWN,
	COMMAND_DUNNING,
	COMMAND_ERROR,
	COMMAND_EXPENDITURE,
	COMMAND_EXPENDITURE_VOUCHER,
	COMMAND_EXPORT,
	COMMAND_IMPORT,
	COMMAND_INVOICE,
	COMMAND_LETTER,
	COMMAND_LIST,
	COMMAND_MISC,
	COMMAND_OFFER,
	COMMAND_ORDER,
	COMMAND_ORDER_PENDING,
	COMMAND_ORDER_PROCESSING,
	COMMAND_ORDER_SHIPPED,
	COMMAND_PARCEL,
	COMMAND_PAYMENT,
	COMMAND_PLUS,
	COMMAND_PRINT,
	COMMAND_PRINTER,
	COMMAND_PRINTER_GREY,
	COMMAND_PRODUCT,
	COMMAND_PRODUCT_BUYERS,
	COMMAND_PRODUCT_STATISTICS,
	COMMAND_PROFORMA,
	COMMAND_RECEIPT_VOUCHER,
	COMMAND_REDPOINT,
	COMMAND_REMOVE_INVOICEREF,
	COMMAND_REORGANIZE,
	COMMAND_SALES,
	COMMAND_SAVE,
	COMMAND_SAVEALL,
	COMMAND_SHIPPING,
	COMMAND_SHOP,
	COMMAND_TEXT,
	COMMAND_UP,
	COMMAND_VAT,
	COMMAND_VCARD,
	COMMAND_WWW,

	// browser buttons (20x20)
	BROWSER_BROWSER_BACK,
	BROWSER_BROWSER_FORWARD,
	BROWSER_BROWSER_HOME,
	BROWSER_BROWSER_PARCEL,
	BROWSER_BROWSER_RELOAD,
	BROWSER_BROWSER_STOP,

	// document edit buttons for buttons in document editor (20x20)
	DOCEDIT_CONTACT_LIST,
	DOCEDIT_CONTACT_PLUS,
	DOCEDIT_LIST,
	DOCEDIT_PLUS_LIST,
	DOCEDIT_PRODUCT_LIST,
	DOCEDIT_DELIVERY_NOTE_LIST,

	// treeview icons (10x10)
	TREE_CONTACT,
	TREE_DOCUMENT,
	TREE_DOT,

	// icons (32x32) for toolbar
	ICON_CALCULATOR,
	ICON_CONFIRMATION,
	ICON_CONFIRMATION_NEW,
	ICON_CONTACT_NEW,
	ICON_CREDIT,
	ICON_CREDIT_NEW,
	ICON_DELIVERY,
	ICON_DELIVERY_NEW,
	ICON_DUNNING,
	ICON_DUNNING_NEW,
	ICON_EXPENDITURE_NEW,
	ICON_EXPENDITURE_VOUCHER_NEW,
	ICON_INVOICE,
	ICON_INVOICE_NEW,
	ICON_LETTER,
	ICON_LETTER_NEW,
	ICON_MISC,
	ICON_OFFER,
	ICON_OFFER_NEW,
	ICON_ORDER,
	ICON_ORDER_NEW,
	ICON_PARCEL_SERVICE,
	ICON_PRINTOO,
	ICON_PRINTOO_DIS,
	ICON_PRODUCT_NEW,
	ICON_PROFORMA,
	ICON_PROFORMA_NEW,
	ICON_RECEIPT_VOUCHER_NEW,
	ICON_SAVE,
	ICON_SAVE_DIS,
	ICON_SHOP,
	ICON_WARNING,
	ICON_WWW,

	// big icons (48x48, for use in document editor)
	BIGICON_CONFIRMATION,
	BIGICON_CREDIT,
	BIGICON_DELIVERY,
	BIGICON_DUNNING,
	BIGICON_EXPENDITURE,
	BIGICON_INVOICE,
	BIGICON_LETTER,
	BIGICON_NONE,
	BIGICON_OFFER,
	BIGICON_ORDER,
	
	/* 47x47 calc icons */
	CALC_0,
	CALC_1,
	CALC_2,
	CALC_3,
	CALC_4,
	CALC_5,
	CALC_6,
	CALC_7,
	CALC_8,
	CALC_9,
	CALC_BACK,
	CALC_C,
	CALC_CE,
	CALC_DIV,
	CALC_INV,
	CALC_MC,
	CALC_MINUS,
	CALC_MMINUS,
	CALC_MPLUS,
	CALC_MR,
	CALC_MS,
	CALC_PERCENT,
	CALC_PLUS,
	CALC_PLUSMINUS,
	CALC_POINT,
	CALC_SUM,
	CALC_X,

	// 48x64 overlay
	OVERLAY_CHECKED,
	
	ABOUT_ICON;	
	// @formatter:on

	private Icon() {
	}

	/**
	 * Returns an image. Clients do not need to dispose the image, it will be
	 * disposed automatically.
	 * 
	 * @return an {@link Image}
	 */
	public Image getImage(IconSize is) {
		Image image = JFaceResources.getImageRegistry().get(this.name()+is.name());
		if (image == null) {
			addIconImageDescriptor(this.name(), is);
			image = JFaceResources.getImageRegistry().get(this.name()+is.name());
		}
		return image;
	}

	/**
	 * @return {@link ImageDescriptor} for the current image
	 */
	public ImageDescriptor getImageDescriptor(IconSize is) {
		ImageDescriptor id = null;
		id = JFaceResources.getImageRegistry().getDescriptor(this.name()+is.name());
		if (id == null) {
			addIconImageDescriptor(this.name(), is);
			id = JFaceResources.getImageRegistry().getDescriptor(this.name()+is.name());
		}
		return id;
	}

	/**
	 * @return a string to be embedded as iconURI, see beta plugin process for
	 *         an example
	 */
	public String getIconURI() {
		return "icon://" + name();
	}

	/**
	 * Get the Icon as {@link InputStream}; used by the
	 * {@link IconURLConnection}
	 * 
	 * @param is
	 * @return <code>null</code> if any error in resolving the image
	 * @throws IOException
	 */
	public InputStream getImageAsInputStream(IconSize is) throws IOException {
		InputStream ret = null;

		ResourceBundle iconsetProperties = ResourceBundle.getBundle("iconset");
		String fileName = iconsetProperties.getString(this.name());
		URL url = FileLocator.find(Activator.getDefault().getBundle(),
				new Path("icons/" + is.name + "/" + fileName), null);
		ret = url.openConnection().getInputStream();
		
		return ret;
	}

	/**
	 * Add an image descriptor for a specific key and {@link IconSize} to the
	 * global {@link ImageRegistry}
	 * 
	 * @param name
	 * @param is
	 * @return <code>true</code> if successfully added, else <code>false</code>
	 */
	private static boolean addIconImageDescriptor(String name, IconSize is) {
		try {
			ResourceBundle iconsetProperties = ResourceBundle
					.getBundle("iconset");
			String fileName = iconsetProperties.getString(name);
			URL fileLocation = FileLocator.find(Activator.getDefault()
					.getBundle(),
					new Path("icons/" + is.name + "/" + fileName), null);
			ImageDescriptor id = ImageDescriptor.createFromURL(fileLocation);
			JFaceResources.getImageRegistry().put(name + is.name(), id);
		} catch (MissingResourceException | IllegalArgumentException e) {
			return false;
		}
		return true;
	}
}
