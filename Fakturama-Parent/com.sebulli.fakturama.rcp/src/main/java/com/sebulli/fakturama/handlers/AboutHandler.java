/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sebulli.fakturama.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.dialogs.about.E4AboutDialog;

public class AboutHandler {
	
	@Inject
	private EPartService partService;

	@Execute
	public void execute(Shell shell, IEclipseContext context) {
		// doesn't work :-(
//		partService.showPart("com.sebulli.fakturama.rcp.part.0", PartState.ACTIVATE);
		E4AboutDialog dlg = ContextInjectionFactory.make(E4AboutDialog.class, context);
		dlg.open();
	}
}
