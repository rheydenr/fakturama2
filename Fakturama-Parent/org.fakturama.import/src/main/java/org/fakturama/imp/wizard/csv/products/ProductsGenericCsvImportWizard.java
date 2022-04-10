package org.fakturama.imp.wizard.csv.products;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptionPage;
import org.fakturama.imp.wizard.ImportOptions;
import org.fakturama.imp.wizard.ImportProgressDialog;
import org.fakturama.imp.wizard.csv.common.BeanCsvFieldComboProvider;
import org.fakturama.imp.wizard.csv.common.CSVImportFilePage;
import org.fakturama.imp.wizard.csv.common.ImportCSVConfigTablePage;
import org.fakturama.wizards.IFakturamaWizardService;
import org.fakturama.wizards.IImportWizard;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.FakturamaModelPackage;
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
    @Translation
    protected Messages msg;

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
    private ImportCSVConfigTablePage csvConfigPage;
    private CSVImportFilePage csvProductImportFilePage;

    @PostConstruct
    @Override
    public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
        setWindowTitle(importMessages.wizardImportCsv);
        Image previewImage = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.IMPORT_PRODUCTS2);
        ctx.set(IFakturamaWizardService.WIZARD_TITLE, importMessages.wizardImportCsvProducts);
        ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, importMessages.wizardImportOptionsSet);
        ctx.set(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE, previewImage);
        setDialogSettings(ctx.get(IDialogSettings.class));
        
        ctx.set(ImportOptions.class, new ImportOptions(getDialogSettings()));

        csvProductImportFilePage = ContextInjectionFactory.make(CSVImportFilePage.class, ctx);
        csvProductImportFilePage.setWizard(this);
        addPage(csvProductImportFilePage);
        
        optionPage = ContextInjectionFactory.make(ImportOptionPage.class, ctx);
        optionPage.setWizard(this);
        addPage(optionPage);
        
        // this was the first try with TreeMapper widget, but it's too obfuscating and 
        // you can't get the real mapping with one view
//        csvConfigPage = ContextInjectionFactory.make(ImportCSVProductConfigPage.class, ctx);
        
        EClass productModel = (EClass) FakturamaModelPackage.INSTANCE.getEPackage().getEClassifiers().get(FakturamaModelPackage.PRODUCT_CLASSIFIER_ID);
        String[] reqHdr = new String[] { 
                productModel.getEStructuralFeature(FakturamaModelPackage.PRODUCT_ITEMNUMBER_FEATURE_ID).getName(),
                productModel.getEStructuralFeature(FakturamaModelPackage.PRODUCT_NAME_FEATURE_ID).getName(),
                productModel.getEStructuralFeature(FakturamaModelPackage.PRODUCT_PRICE1_FEATURE_ID).getName() };
        ctx.set(ImportCSVConfigTablePage.PARAM_REQUIRED_HEADERS, reqHdr);
        ctx.set(ImportCSVConfigTablePage.PARAM_MAPPING_MESSAGE, importMessages.wizardImportCsvGenericCreatemapping);
        ctx.set(ImportCSVConfigTablePage.PARAM_SPEC_QUALIFIER, ImportCSVConfigTablePage.PRODUCT_SPEC_QUALIFIER);
        ctx.set(ImportCSVConfigTablePage.PARAM_SPEC_NAME, msg.exporterDataProduct);
        
        csvConfigPage = ContextInjectionFactory.make(ImportCSVConfigTablePage.class, ctx);
        csvConfigPage.setDataProvider(new BeanCsvFieldComboProvider(ProductBeanCSV.createProductsAttributeMap(msg)));
        csvConfigPage.setI18nMappingFunction(s -> ProductBeanCSV.getI18NIdentifier(s));
        csvConfigPage.setPageComplete(true);
        csvConfigPage.setWizard(this);
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
            importOptions.setMappings(csvConfigPage.getCompleteMappings());
            BusyIndicator.showWhile(Display.getCurrent(), () -> {
                GenericProductsCsvImporter csvImporter = ContextInjectionFactory.make(GenericProductsCsvImporter.class, ctx);
                csvImporter.importCSV(importOptions, false, new NullProgressMonitor());
                ctx.set("LAST_EXECUTION_RESULT", csvImporter.getResult());
                // return Status.OK_STATUS;
            });

            ImportProgressDialog dialog = ContextInjectionFactory.make(ImportProgressDialog.class, ctx);
            dialog.setStatusText((String) ctx.get("LAST_EXECUTION_RESULT"));

            // Refresh the table view of all products
            evtBroker.post("ProductEditor", "update");

            // Find the VAT table view
            evtBroker.post("VatEditor", "update");

            optionPage.saveSettings();
            return (dialog.open() == ImportProgressDialog.OK);
        }

        return false;
    }

}
