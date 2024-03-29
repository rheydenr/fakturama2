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
 
package org.fakturama.wizard.imp.handler;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.wizard.FakturamaImportExportWizard;

import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * Main entry point for the import wizards. This handler calls
 * the initial import wizard selection dialog from which one
 * can start a single import wizard.
 * <p>
 * This class is similar to org.eclipse.ui.internal.handlers.WizardHandler.
 *
 */
public class ImportHandler {

    private static final int SIZING_WIZARD_WIDTH = 470;
    private static final int SIZING_WIZARD_HEIGHT = 550;

	@Execute
	public void execute(IEclipseContext ctx, Shell shell) {
		// create the wizard selection dialog
		ctx.set(FakturamaImportExportWizard.WIZARD_MODE, FakturamaImportExportWizard.IMPORT);
		FakturamaImportExportWizard wizard = ContextInjectionFactory.make(FakturamaImportExportWizard.class, ctx);
		IDialogSettings settings = ctx.get(IDialogSettings.class);
		WizardDialog.setDefaultImage(Icon.COMMAND_APP.getImage(IconSize.DefaultIconSize));
		wizard.setForcePreviousAndNextButtons(true);
		wizard.setDialogSettings(settings);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH, dialog.getShell()
						.getSize().x), SIZING_WIZARD_HEIGHT);
		dialog.open();
	}
}