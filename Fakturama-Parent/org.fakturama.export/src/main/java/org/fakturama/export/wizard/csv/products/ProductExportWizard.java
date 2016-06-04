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

package org.fakturama.export.wizard.csv.products;


import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.EmptyWizardPage;
import org.fakturama.wizards.IExportWizard;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.resources.ITemplateResourceManager;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * Export wizard to export sales
 * 
 */
public class ProductExportWizard extends Wizard implements IExportWizard {

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;
	
	@Inject
	private ITemplateResourceManager resourceManager;
	
	@Inject
	private Shell shell;

	// The first (and only) page of this wizard
	private EmptyWizardPage page1;

	@Inject
	private IEclipseContext ctx;

	/**
	 * Initializes this creation wizard using the passed workbench and object
	 * selection.
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@PostConstruct
	@Override
	public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
		setWindowTitle(msg.pageExport);
		Image previewImage = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.EXPORT_PRODUCTS_CSV);
		ctx.set(IFakturamaWizardService.WIZARD_TITLE, exportMessages.wizardExportProductsAllproductsTitle);
		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, exportMessages.wizardExportCsvProductsTitle);
		ctx.set(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE, previewImage);
		page1 = ContextInjectionFactory.make(EmptyWizardPage.class, ctx);
		addPage(page1);
	}


	/**
	 * Performs any actions appropriate in response to the user having pressed
	 * the Finish button, or refuse if finishing now is not permitted.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		// Create a "SAVE AS" file dialog
		FileDialog fileDialog = new FileDialog(page1.getShell(), SWT.SAVE);
		
		fileDialog.setFilterExtensions(new String[] { "*.csv" });
		//T: Text in a file name dialog
		fileDialog.setFilterNames(new String[] { exportMessages.wizardExportFilenameTypeCsv + " (*.csv)" });
		//T: Text in a file name dialog
		fileDialog.setText(exportMessages.wizardExportFilename);
		String selectedFile = fileDialog.open();
		if (selectedFile != null) {
			ProductExporter exporter = ContextInjectionFactory.make(ProductExporter.class, ctx);
			boolean result = exporter.export(selectedFile);
			if(result) {
				MessageDialog.openInformation(shell, msg.dialogMessageboxTitleInfo, exportMessages.wizardExportCommonSuccess);
			} else {
				MessageDialog.openError(shell, msg.dialogMessageboxTitleError, exportMessages.wizardExportCommonNosuccess);
			}
			return true;   // this closes the wizard dialog
		}
		else 
			return false;
	}
}
