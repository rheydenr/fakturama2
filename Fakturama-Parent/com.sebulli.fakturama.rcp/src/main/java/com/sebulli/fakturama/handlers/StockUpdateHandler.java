/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2018 The Fakturama Team
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Fakturama Team - initial API and implementation
 */
package com.sebulli.fakturama.handlers;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.ProductEditor;

/**
 * Handler for updating stock quantities. Stock quantities are updated if 
 * <ul>
 * <li>an order is created,
 * <li>a delivery document is created,
 * <li>an invoice is created or
 * <li>if an order is created during web shop import.
 * </ul>
 * Only one of these triggers leads to a stock update. This is configurable in the preference page (Products).
 * This Handler has to be called each time if a Document is saved or a web shop import was started.
 *
 */
public class StockUpdateHandler {

    @Inject
    @Translation
    private Messages msg;

//    @Inject
//    private Logger log;

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;

    @Inject
    private ProductsDAO productsDAO;

    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

	@Execute
	public void updateStockQuantity(Shell parent,
			@org.eclipse.e4.core.di.annotations.Optional @Named(Constants.PARAM_PROGRESS) Integer oldProgress,
			@Named(Constants.PARAM_ORDERID) Document document) {
		boolean needUpdate = false; // if an update of views is needed
		if (eclipsePrefs.getBoolean(Constants.PREFERENCES_PRODUCT_USE_QUANTITY, Boolean.TRUE)) {

			switch (eclipsePrefs.get(Constants.PREFERENCES_PRODUCT_CHANGE_QTY, Constants.PREFERENCES_PRODUCT_CHANGE_QTY_INVOICE)) {
			case Constants.PREFERENCES_PRODUCT_CHANGE_QTY_ORDER:
				if(document.getProgress() != null) {
					OrderState progress = OrderState.findByProgressValue(Optional.of(document.getProgress()));
					OrderState progressOld = OrderState.findByProgressValue(Optional.of(oldProgress));
					// adapt stock value
					List<DocumentItem> items = document.getItems();
					// mark as shipped - take from stock
					if (progress == OrderState.SHIPPED && progressOld != OrderState.SHIPPED) {
						BiFunction<Double, Double, Double> func = (x, y) -> {
							return x - y;
						};
						needUpdate = updateStockFromItems(items, parent, func);
					}
					// mark as processing or lower - add to stock
					else if (progressOld == OrderState.SHIPPED && progress != OrderState.SHIPPED) {
						// TODO DO THIS IN DAO!!!
						BiFunction<Double, Double, Double> func = (x, y) -> {
							return x + y;
						};
						needUpdate = updateStockFromItems(items, parent, func);
					}
				}
				
				break;
			case Constants.PREFERENCES_PRODUCT_CHANGE_QTY_DELIVERY:
				if(document != null && document.getBillingType().isDELIVERY()) {
					List<DocumentItem> items = document.getItems();
					BiFunction<Double, Double, Double> stockUpdateFunction = (x, y) -> {
						return x - y;
					};
					needUpdate = updateStockFromItems(items, parent, stockUpdateFunction);
				}
				
				break;
			case Constants.PREFERENCES_PRODUCT_CHANGE_QTY_INVOICE:
				if(document != null) {
					switch (document.getBillingType()) {
					case INVOICE:
						List<DocumentItem> items = document.getItems();
						BiFunction<Double, Double, Double> func = (x, y) -> {
							return x - y;
						};
						needUpdate = updateStockFromItems(items, parent, func);

						break;
					case CREDIT:
						items = document.getItems();
						func = (x, y) -> {
							return x + y;
						};
						needUpdate = updateStockFromItems(items, parent, func);
						break;
					default:
						// do nothing
						break;
					}
				}
				break;
			default:
				// do nothing
				break;
			}

			if (needUpdate) {
				// Refresh the table view of all documents
				evtBroker.post(ProductEditor.EDITOR_ID, Editor.UPDATE_EVENT);
			}
		}
	}

	/**
	 * Updates the stock quantity of a product based on a given
	 * {@link DocumentItem}.
	 * 
	 * @param items
	 *            {@link DocumentItem}s from which the quantity is given
	 * @param parent
	 *            the parent shell
	 * @param func
	 *            which function should be applied for changing the stock value
	 *            (depending if stock quantity has to be increased or decreased)
	 * @return if a refresh of the list views is needed
	 */
	private boolean updateStockFromItems(List<DocumentItem> items, Shell parent,
			BiFunction<Double, Double, Double> func) {
		boolean needUpdate = false;
		for (DocumentItem item : items) {
			Product product = item.getProduct();
			// only process if item is based on a real product and if quantity is given
			if (product != null && product.getQuantity() != null) {
				
				/*
				 * TODO Das muß nochmal korrigiert werden. Wenn man nämlich die Menge eines bereits bestehenden Dokumentes ändert, wird immer die
				 * absolute Menge vom Bestand abgezogen. Es muß aber relativ zur vorherigen Menge berechnet werden.
				 */
				// quantity is the difference between the old (formerly stored) and the current quantity of this DocumentItem.
				// The origin quantity is stored in a separate (transient) field. If the DocumentItem is a new one, 
				// the origin quantity can be empty. In this case we use the current quantity.
				// If the Document item was removed from Document then the quantity is null and the originQuantity contains the old value.
				// The "real" quantity results to a negative number, so that the item is "put back" into the stock (but only if the document was printed before).
				Double realQuantity = Optional.ofNullable(item.getQuantity()).orElse(Double.valueOf(0.0)) - Optional.ofNullable(item.getOriginQuantity()).orElse(Double.valueOf(0.0));
				product.setQuantity(func.apply(product.getQuantity(), realQuantity));
				try {
					productsDAO.update(product);
					needUpdate = true;
				} catch (FakturamaStoringException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (product.getQuantity() <= 0) {
					MessageDialog.openWarning(parent, msg.dialogMessageboxTitleInfo,
							MessageFormat.format(msg.commandMarkorderWarnStockzero, product.getName(),
									CommonConverter.getCategoryName(product.getCategories(), "/")));
				}
			}
		}
		return needUpdate;
	}

}
