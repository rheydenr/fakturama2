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
 
package org.fakturama.export.handler;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.export.wizard.FakturamaExporterWizardsSelector;

/**
 * Main entry point for the export wizards.
 *
 */
public class ExportHandler {
	
	@Execute
	public void execute(IEclipseContext ctx, Shell shell) {
		FakturamaExporterWizardsSelector fakturamaExporterWizardsSelector = ContextInjectionFactory.make(FakturamaExporterWizardsSelector.class, ctx);
		WizardDialog dlg = new WizardDialog(shell, fakturamaExporterWizardsSelector);
		dlg.open();
	}
		
}