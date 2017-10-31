/**
 * 
 */
package org.fakturama.imp.wizard.csv.contacts;

import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.fakturama.imp.wizard.ImportOptions;
import org.fakturama.imp.wizard.ImportProgressDialog;
import org.fakturama.wizards.IImportWizard;

import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.util.ContactUtil;

/**
 *
 */
public class CreditorsCsvImportWizard extends ContactsCsvImportWizard implements IImportWizard {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@PostConstruct
	@Override
	public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
		super.init(workbench, selection);
	}
	
	protected String getWizardTitle() {
		return importMessages.wizardImportCsvCreditors;		
	}
	
	@Override
	protected boolean doImport(final String fileName, ImportOptions options) {
		ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, ctx);
		ctx.set(ContactUtil.class, contactUtil);
		ContactsCsvImporter csvImporter = ContextInjectionFactory.make(ContactsCsvImporter.class, ctx);
		csvImporter.importCSV(fileName, FakturamaModelPackage.CREDITOR_CLASSIFIER_ID, options.getUpdateExisting(), options.getUpdateWithEmptyValues());

		ImportProgressDialog dialog = ContextInjectionFactory.make(ImportProgressDialog.class, ctx);
		dialog.setStatusText(csvImporter.getResult());

		// Refresh the table view of all contacts
        evtBroker.post("ContactEditor", "update");

		return (dialog.open() == ImportProgressDialog.OK);
	}
}
