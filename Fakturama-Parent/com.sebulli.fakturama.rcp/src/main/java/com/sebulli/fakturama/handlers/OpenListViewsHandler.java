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

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.log.ILogger;

/**
 * This action opens the (account) Lists in a table view.
 * 
 */
public class OpenListViewsHandler {

    public static final String PARAM_LIST_TYPE = "com.sebulli.fakturama.lists.viewtype";

    @Inject
    private ILogger log;
    
	/**
	 * Run the action
	 * <br />
	 * Open the VATs in an table view.
	 */
	@Execute
	public void execute(
	        @Named(PARAM_LIST_TYPE) String editorType,
	        final EPartService partService) {
		// see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=372211
	    MPart listViewPart = partService.findPart(editorType);
	    if(listViewPart != null && listViewPart.isVisible()) {
	        log.debug("part is already created!");
    		partService.showPart(listViewPart,
    				PartState.VISIBLE);
	    } else {
	        listViewPart.setVisible(true);
	        // otherwise no content is rendered :-(
	        listViewPart.setToBeRendered(true);
    		partService.showPart(listViewPart,
    				PartState.ACTIVATE);
	    }
	}
	
	@CanExecute
	public boolean canExecute() {
		return true;
	}
}
