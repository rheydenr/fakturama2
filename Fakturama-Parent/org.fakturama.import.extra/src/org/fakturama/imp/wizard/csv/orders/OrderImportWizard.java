/**
 * 
 */
package org.fakturama.imp.wizard.csv.orders;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.FileDialog;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportProgressDialog;
import org.fakturama.wizards.IFakturamaWizardService;
import org.fakturama.wizards.IImportWizard;

import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.util.ContactUtil;

/**
 *
 */
public class OrderImportWizard extends Wizard implements IImportWizard {
	
	public static final String ORDERS_CSV_IMPORT_SHIPPING = "ORDERS_CSV_IMPORT_SHIPPING";
	public static final String ORDERS_CSV_IMPORT_ORDERTEMPLATE = "ORDERS_CSV_IMPORT_ORDERTEMPLATE";
	public static final String ORDERS_CSV_IMPORT_DELIVERYTEMPLATE = "ORDERS_CSV_IMPORT_DELIVERYTEMPLATE";

	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	@Translation
	protected ImportOrdersMessages importOrdersMessages;

	@Inject
	private ECommandService cmdService;
	
	@Inject
	private EHandlerService handlerService;

	@Inject
	private IEclipseContext ctx;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

	// The wizard pages
	private ImportOrdersOptionPage optionPage;

	/* (non-Javadoc)
	 * @see org.fakturama.wizards.IWorkbenchWizard#init(org.eclipse.e4.ui.workbench.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@PostConstruct
	@Override
	public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
		setWindowTitle(importMessages.wizardImportCsv);
//		Image previewImage = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.IMPORT_CONTACTS2);
		ctx.set(IFakturamaWizardService.WIZARD_TITLE, importOrdersMessages.wizardImportCsvOrders);
		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, importMessages.wizardImportOptionsSet);
		ctx.set(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE, null);
		optionPage = ContextInjectionFactory.make(ImportOrdersOptionPage.class, ctx);
		optionPage.setPageComplete(true);
		addPage(optionPage);
		setNeedsProgressMonitor(true);
	}

	/* (non-Javadoc)
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
		fileDialog.setFilterNames(new String[] { importMessages.wizardImportCsvInfo + " (*.csv)" });
		selectedFile = fileDialog.open();
			// Import the selected file
		if (selectedFile != null && !selectedFile.isEmpty()) {

			ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, ctx);
			ctx.set(ContactUtil.class, contactUtil);
			OrdersCsvImporter csvImporter = ContextInjectionFactory.make(OrdersCsvImporter.class, ctx);
//			
//			MessageDialog.openInformation(this.getShell(), "Info", "Hier käme dann der Bestellungsimport.");
//			
			List<Document> importedDocuments = csvImporter.importCSV(selectedFile, false, optionPage.getSelectedShipping().get(), optionPage.getSelectedOrderTemplate(), optionPage.getSelectedDeliveryTemplate());

			ImportProgressDialog dialog = ContextInjectionFactory.make(ImportProgressDialog.class, ctx);
			dialog.setStatusText(csvImporter.getResult());
			dialog.open();

			// Refresh the table view of all contacts
		    evtBroker.post("DocumentEditor", "update");

		    // save settings
			IDialogSettings dialogSettings = ctx.get(IDialogSettings.class);
			if(dialogSettings != null) {
				java.util.Optional<Shipping> selectedShipping = optionPage.getSelectedShipping();
				dialogSettings.put(ORDERS_CSV_IMPORT_SHIPPING, selectedShipping.isPresent() ? selectedShipping.get().getId() : Long.valueOf(-1));
				java.util.Optional<Path> selectedOrderTemplate = optionPage.getSelectedOrderTemplate();
				dialogSettings.put(ORDERS_CSV_IMPORT_ORDERTEMPLATE, selectedOrderTemplate.isPresent() ? selectedOrderTemplate.get().toString() : null);
				java.util.Optional<Path> selectedDeliveryTemplate = optionPage.getSelectedDeliveryTemplate();
				dialogSettings.put(ORDERS_CSV_IMPORT_DELIVERYTEMPLATE, selectedDeliveryTemplate.isPresent() ? selectedDeliveryTemplate.get().toString() : null);
			}
			return true; // (dialog.open() == ImportProgressDialog.OK);
		}
		return false;
	}

}
