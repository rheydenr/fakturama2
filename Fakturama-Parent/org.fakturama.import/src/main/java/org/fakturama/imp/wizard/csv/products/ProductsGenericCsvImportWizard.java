package org.fakturama.imp.wizard.csv.products;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptionPage;
import org.fakturama.imp.wizard.ImportOptions;
import org.fakturama.imp.wizard.ImportProgressDialog;
import org.fakturama.wizards.IFakturamaWizardService;
import org.fakturama.wizards.IImportWizard;

import com.sebulli.fakturama.resources.ITemplateResourceManager;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * A generic CSV import wizard for products.
 *
 */
public class ProductsGenericCsvImportWizard extends Wizard implements IImportWizard {
    
    @Inject
    @Translation
    protected ImportMessages importMessages;
    
    @Inject
    private ITemplateResourceManager resourceManager;
    
    @Inject
    private IEclipseContext ctx;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    // The wizard pages
    private ImportOptionPage optionPage;
    private ImportCSVProductConfigPage csvConfigPage;
    private CSVProductImportFilePage csvProductImportFilePage;

    @PostConstruct
    @Override
    public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
        setWindowTitle(importMessages.wizardImportCsv);
        Image previewImage = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.IMPORT_PRODUCTS2);
        ctx.set(IFakturamaWizardService.WIZARD_TITLE, importMessages.wizardImportCsvProducts);
        ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, importMessages.wizardImportOptionsSet);
        ctx.set(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE, previewImage);
        
        csvProductImportFilePage = ContextInjectionFactory.make(CSVProductImportFilePage.class, ctx);
        addPage(csvProductImportFilePage);
        
        optionPage = ContextInjectionFactory.make(ImportOptionPage.class, ctx);
        addPage(optionPage);
        
        csvConfigPage = ContextInjectionFactory.make(ImportCSVProductConfigPage.class, ctx);
        csvConfigPage.setPageComplete(true);
        addPage(csvConfigPage);
        
        setNeedsProgressMonitor(true);
    }
    
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if(page == csvConfigPage) {
            // options are stored in ImportOptions (located in context)
            csvConfigPage.analyzeCsvFile();
        }
        return super.getNextPage(page);
    }
    
    /**
     * Performs any actions appropriate in response to the user having pressed
     * the Finish button
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        // The selected file to import
        String selectedFile = optionPage.getImportOptions().getCsvFile();
        if (selectedFile != null && !selectedFile.isEmpty()) {
            ImportOptions importOptions = optionPage.getImportOptions();
            optionPage.saveSettings();
            importOptions.setMappings(csvConfigPage.getCompleteMappings());

            GenericProductsCsvImporter csvImporter = ContextInjectionFactory.make(GenericProductsCsvImporter.class, ctx);
            csvImporter.importCSV(importOptions, false);

            ImportProgressDialog dialog = ContextInjectionFactory.make(ImportProgressDialog.class, ctx);
            dialog.setStatusText(csvImporter.getResult());

            // Refresh the table view of all products
            evtBroker.post("ProductEditor", "update");

            // Find the VAT table view
            evtBroker.post("VatEditor", "update");
            
            return (dialog.open() == ImportProgressDialog.OK);
        }

        return false;
    }

}
