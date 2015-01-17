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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.views.datatable.shippings.ShippingListTable;

/**
 * This action opens the Shippings in a table view.
 * 
 */
public class OpenShippingsHandler {

    @Inject
    private ILogger log;
    
	/**
	 * Run the action
	 * <br />
	 * Open the Shippings in an table view.
	 */
	@Execute
	public void execute(final EPartService partService) {
		// see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=372211
	    MPart shippingPart = partService.findPart(ShippingListTable.ID);
	    if(shippingPart != null && shippingPart.isVisible()) {
	        log.debug("part is already created!");
    		partService.showPart(shippingPart,
    				PartState.VISIBLE);
	    } else {
	        shippingPart.setVisible(true);
	        // otherwise no content is rendered :-(
	        shippingPart.setToBeRendered(true);
    		partService.showPart(shippingPart,
    				PartState.ACTIVATE);
	    }
	}
	
	@CanExecute
	public boolean canExecute() {
		return true;
	}
}
