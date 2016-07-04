/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import javax.inject.Inject;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 *
 */
public class SyncMobileHandler {
	
	@Inject
	private Logger log;

	@CanExecute
	public boolean canExecute() {
		return false;
	}

	@Execute
	public void runHandler(final MApplication application, final EModelService modelService) throws ExecutionException {
		MWindow myPart = (MWindow)modelService.find("window:org.fakturama.syncmobile", application);
		if (myPart != null) {
			myPart.setVisible(true);
			myPart.setToBeRendered(true);
		} else {
			log.error("couldn't launch Window view! (Reason: view not found by ModelService)");
		}
	}
}
