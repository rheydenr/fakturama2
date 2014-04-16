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

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.views.datatable.nattabletest.RHENatTable;

/**
 * This action opens the VATs in a table view.
 * 
 * @author Ralf Heydenreich
 */
public class OpenVatsHandler {

	/**
	 * Run the action
	 * <br />
	 * Open the VATs in an table view.
	 */
	@Execute
	public void execute(final EPartService partService) {
		// see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=372211
		partService.showPart(RHENatTable.ID,
				PartState.ACTIVATE);
	}
	
	@CanExecute
	public boolean canExecute() {
		return true;
	}

}
