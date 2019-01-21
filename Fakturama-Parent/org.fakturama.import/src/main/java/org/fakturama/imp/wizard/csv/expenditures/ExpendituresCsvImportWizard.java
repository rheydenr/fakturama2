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

package org.fakturama.imp.wizard.csv.expenditures;


import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportProgressDialog;
import org.fakturama.wizards.IImportWizard;

/**
 * A wizard to import tables in CSV file format
 */
public class ExpendituresCsvImportWizard extends Wizard implements IImportWizard {
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;
	
	@Inject
	private IEclipseContext ctx;
	
	@Inject
	private Shell shell;

	// The selected file to import
	String selectedFile = "";

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@PostConstruct
	@Override
	public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
		setWindowTitle(importMessages.wizardImportCsv);
		setNeedsProgressMonitor(true);
		performFinish();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		return true;
	}

	/**
	 * Performs any actions appropriate in response to the user having pressed
	 * the Finish button
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		// The selected file to import
		String selectedFile = "";
		FileDialog fileDialog = new FileDialog(shell);
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
		// Import the selected file
		if (selectedFile != null && !selectedFile.isEmpty()) {

			ExpendituresCsvImporter csvImporter = ContextInjectionFactory.make(ExpendituresCsvImporter.class, ctx);
			csvImporter.importCSV(selectedFile, false);

			ImportProgressDialog dialog= ContextInjectionFactory.make(ImportProgressDialog.class, ctx);
			dialog.setStatusText(csvImporter.getResult());

			// Find the expenditure table view
			// Refresh it
		    evtBroker.post("VoucherEditor", "update");

			// Find the VAT table view
			// Refresh it
		    evtBroker.post("VATEditor", "update");
			if (dialog.open() == ImportProgressDialog.OK) {
				performCancel();
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}
