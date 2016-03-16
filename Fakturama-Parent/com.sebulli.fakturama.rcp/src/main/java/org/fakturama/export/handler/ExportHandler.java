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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.export.wizard.FakturamaImportExportWizard;
import org.osgi.framework.FrameworkUtil;

import com.sebulli.fakturama.Activator;

/**
 * Main entry point for the export wizards. This handler calls
 * the initial export wizard selection dialog from which one
 * can start a single export wizard.
 * <p>
 * This class is similar to org.eclipse.ui.internal.handlers.WizardHandler.
 *
 */
public class ExportHandler {

    /**
     * The name of the dialog settings file (value 
     * <code>"dialog_settings.xml"</code>).
     */
    private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml"; //$NON-NLS-1$

    private static final int SIZING_WIZARD_WIDTH = 470;
    private static final int SIZING_WIZARD_HEIGHT = 550;

	@Execute
	public void execute(IEclipseContext ctx, Shell shell) {
		// create the wizard selection dialog
		ctx.set(FakturamaImportExportWizard.WIZARD_MODE, FakturamaImportExportWizard.EXPORT);
		FakturamaImportExportWizard wizard = ContextInjectionFactory.make(FakturamaImportExportWizard.class, ctx);
		IDialogSettings settings = ctx.get(IDialogSettings.class);
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