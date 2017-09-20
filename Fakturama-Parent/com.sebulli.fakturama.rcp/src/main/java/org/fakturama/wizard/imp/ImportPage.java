package org.fakturama.wizard.imp;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.fakturama.wizards.IE4WizardCategory;
import org.fakturama.wizards.IFakturamaWizardService;
import org.fakturama.wizards.IWizardRegistry;
import org.fakturama.wizards.ImportExportPage;
import org.fakturama.wizards.ImportWizardRegistry;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.i18n.Messages;

/**
 * Wizard page class from which an import wizard is selected.
 * 
 * @since 3.2
 *
 */
public class ImportPage extends ImportExportPage {
	private static final String STORE_SELECTED_IMPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME
		+ "STORE_SELECTED_IMPORT_WIZARD_ID"; //$NON-NLS-1$
	
	private static final String STORE_EXPANDED_IMPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
		+ "STORE_EXPANDED_IMPORT_CATEGORIES";	//$NON-NLS-1$

	CategorizedWizardSelectionTree importTree;

	/**
	 * The import service is injected by OSGi container. It resides in a bundle
	 * which contains several importers.
	 */
	private IFakturamaWizardService importService;
	
	@Inject
	private Messages msg;
	
	@Inject
	private MApplication application;

	private ImportWizardRegistry importWizardRegistry;

	@Inject
	public ImportPage(/* E4Workbench workbench*/) {
		super(null, null);
//		this.importService = importService;
	}
	
//	@PostConstruct
	public void createControl(Composite parent) {
		super.createControl(parent);
		setTitle(msg.wizardExportCommonHeadline);
		setDescription(msg.wizardCommonTitleSelectsource);
		
		// setImageDescriptor(image);
		// IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ
	}

	
	protected void initialize() {
//		workbench.getHelpSystem().setHelp(getControl(),
//                IWorkbenchHelpContextIds.IMPORT_WIZARD_SELECTION_WIZARD_PAGE);

		// because more than one service exists for this interface
    	String filter = "(component.name=myImporter)";
		ServiceReference<IFakturamaWizardService>[] serviceReferences;
		try {
			serviceReferences = (ServiceReference<IFakturamaWizardService>[]) 
					Activator.getContext().getServiceReferences(IFakturamaWizardService.class.getName(), filter);
			if(serviceReferences.length > 0) {
				IFakturamaWizardService service = Activator.getContext().getService(serviceReferences[0]);
				this.importService = service;
			}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected Composite createTreeViewer(Composite parent) {
		IE4WizardCategory root = getImportWizardRegistry().getRootCategory();
		
		importTree = new CategorizedWizardSelectionTree(
				root, msg.wizardImportCommonFilterlabel);
		Composite importComp = importTree.createControl(parent);
		importTree.getViewer().addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				listSelectionChanged(event.getSelection());    	       			
			}
		});
		importTree.getViewer().addDoubleClickListener(new IDoubleClickListener(){
	    	public void doubleClick(DoubleClickEvent event) {
	    		treeDoubleClicked(event);
	    	}
	    });
		setTreeViewer(importTree.getViewer());
	    return importComp;
	}
	
	private IWizardRegistry getImportWizardRegistry() {
		if(importWizardRegistry == null) {
			MAddon addon = application.getAddons().stream().filter(
					a -> a.getElementId().equals(ImportWizardRegistry.REGISTRY_ADDON_ID)).findFirst().get();
			importWizardRegistry = (ImportWizardRegistry) addon.getObject();
		}
		return importWizardRegistry;
	}

	/**
	 * @return the importService
	 */
	public IFakturamaWizardService getImportExportService() {
		return importService;
	}

	public void saveWidgetValues(){
    	storeExpandedCategories(STORE_EXPANDED_IMPORT_CATEGORIES, importTree.getViewer());
        storeSelectedCategoryAndWizard(STORE_SELECTED_IMPORT_WIZARD_ID, importTree.getViewer()); 	
        super.saveWidgetValues();
	}
	
	protected void restoreWidgetValues(){
        IE4WizardCategory importRoot = getImportWizardRegistry().getRootCategory();
        expandPreviouslyExpandedCategories(STORE_EXPANDED_IMPORT_CATEGORIES, importRoot, importTree.getViewer());
        selectPreviouslySelected(STORE_SELECTED_IMPORT_WIZARD_ID, importRoot, importTree.getViewer());       
        super.restoreWidgetValues();
	}
	
//	protected ITriggerPoint getTriggerPoint(){
//		return getWorkbench().getActivitySupport()
//    		.getTriggerPointManager().getTriggerPoint(WorkbenchTriggerPoints.IMPORT_WIZARDS);
//	}
	
	protected void updateMessage(){
//		setMessage(WorkbenchMessages.ImportExportPage_chooseExportDestination); 
		super.updateMessage();
	}
}