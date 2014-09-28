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

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;

/**
 * This action marks an entry in the invoice table as unpaid or paid.
 * 
 * @author Gerd Bartelt
 */
public class MarkDocumentAsPaidHandler {
    @Inject
    @Translation
    protected Messages msg;

	// progress of the order. Value from 0 to 100 (percent)
	boolean paid;

	/* *
	 * Instead of using a value for the states "unpaid" or "paid"
	 * a progress value from 0 to 100 (percent) is used.
	 * 
	 * So it's possible to insert states between these.
	 * 
	 * @param text
	 * @param progress
	 */
	/**
	 * Run the action Search all views to get the selected element. If a view
	 * with a selection is found, change the state if it was an order.
	 */
    @Execute
    public void handleMarkDocument(@Optional @Named("com.sebulli.fakturama.cmdmarkparam.objId") String objId,
            @Active MPart activePart) {
        activePart.getObject();
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
////					Object obj = ((IStructuredSelection) selection).getFirstElement();
//					Iterator iterator = ((IStructuredSelection) selection).iterator();
//					while(iterator.hasNext()) {
//						Object obj = iterator.next();
//						// If we had a selection let change the state
////						if (obj != null) {
//							DataSetDocument uds = (DataSetDocument) obj;
//							if (uds instanceof DataSetDocument) {
//	
//								// Do it only, if it is allowed to mark this kind of document as paid.
//								if (DocumentType.getType(uds.getIntValueByKey("category")).hasPaid()) {
//	
//									// change the state
//									uds.setPaid(paid);
//	
//									// also in the database
//									Data.INSTANCE.updateDataSet(uds);
//								}
//							}
////						}
//					}
//	
//					// Refresh the table with orders.
//					view.refresh();
//				}
//			}
//		}
	}
}
