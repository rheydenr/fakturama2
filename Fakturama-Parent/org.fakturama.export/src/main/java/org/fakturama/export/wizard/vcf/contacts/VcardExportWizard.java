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

package org.fakturama.export.wizard.vcf.contacts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
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
 * @author Gerd Bartelt
 */
public class VcardExportWizard extends Wizard implements IExportWizard {

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;

	@Inject
	private IEclipseContext ctx;
	
	@Inject
	private ITemplateResourceManager resourceManager;

	@Inject
	private Logger log;

	// The first (and only) page of this wizard
	EmptyWizardPage page1;

	/**
	 * Adds the first (and only) page to the wizard
	 */
	@PostConstruct
	@Override
	public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
		setWindowTitle(msg.pageExport);
		Image previewImage = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.EXPORT_CONTACTS_VCF);
		ctx.set(IFakturamaWizardService.WIZARD_TITLE, exportMessages.wizardExportVcfContactsTitle);
		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, exportMessages.wizardExportVcfContactsDescription);
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
		
		fileDialog.setFilterExtensions(new String[] { "*.vcf" });
		//T: Text in a file name dialog
		fileDialog.setFilterNames(new String[] { exportMessages.wizardExportFilenameTypeVcard +" (*.vcf)" });
		//T: Text in a file name dialog
		fileDialog.setText(exportMessages.wizardExportFilename);
		String selectedFile = fileDialog.open();
		boolean retval = false;
		try {
			if (selectedFile != null) {
				VcardExport exporter = ContextInjectionFactory.make(VcardExport.class, ctx);
				retval = exporter.export(selectedFile);
			}
		} catch (Exception e) {
			// catch an unspecified exception since we don't know which one is thrown.
			MessageDialog.openError(getShell(), msg.dialogMessageboxTitleError, "Export finished with error, see log file!");
			log.error(e, "export VCard didn't finish successfully! Following error occured:");
		}
		return retval;
	}

}
