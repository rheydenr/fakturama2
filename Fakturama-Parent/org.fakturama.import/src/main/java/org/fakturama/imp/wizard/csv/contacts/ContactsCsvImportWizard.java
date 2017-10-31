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

package org.fakturama.imp.wizard.csv.contacts;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptionPage;
import org.fakturama.imp.wizard.ImportOptions;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.resources.ITemplateResourceManager;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * A wizard to import tables in CSV file format
 * 
 */
public abstract class ContactsCsvImportWizard extends Wizard {
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	private ITemplateResourceManager resourceManager;
	
	@Inject
	protected IEclipseContext ctx;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

	// The wizard pages
	private ImportOptionPage optionPage;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
		setWindowTitle(importMessages.wizardImportCsv);
		Image previewImage = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.EXPORT_CONTACTS_CSV);  // CSV example image
		ctx.set(IFakturamaWizardService.WIZARD_TITLE, getWizardTitle());
		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, importMessages.wizardImportOptionsSet);
		ctx.set(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE, previewImage);
		optionPage = ContextInjectionFactory.make(ImportOptionPage.class, ctx);
		optionPage.setPageComplete(true);
		addPage(optionPage);
		setNeedsProgressMonitor(true);
	}

	protected abstract String getWizardTitle();

	/**
	 * Performs any actions appropriate in response to the user having pressed
	 * the Finish button
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// The selected file to import
		String selectedFile = "";
		FileDialog fileDialog = new FileDialog(this.getShell());
		//fileDialog.setFilterPath("/");
		fileDialog.setFilterExtensions(new String[] { "*.csv" });

		// Start at the user's home
		Path path = Paths.get(System.getProperty("user.home"));
		fileDialog.setFilterPath(path.toString());
		
		//T: CSV Import File Dialog Title
		fileDialog.setText(importMessages.wizardImportDialogSelectfile);

		//T: CSV Import File Filter
		fileDialog.setFilterNames(new String[] { importMessages.wizardImportCsvInfo+ " (*.csv)" });
		selectedFile = fileDialog.open();
		if (selectedFile != null) {

			// Import the selected file
			if (!selectedFile.isEmpty()) {
				return doImport(selectedFile, optionPage.getImportOptions());
			}
		}
		return false;
	}

	protected abstract boolean doImport(final String fileName, ImportOptions options);

}
