package org.fakturama.export.wizard;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.fakturama.export.IFakturamaExportService;
import org.fakturama.wizards.ExportWizardRegistry;
import org.fakturama.wizards.IE4WizardCategory;
import org.fakturama.wizards.IWizardRegistry;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Wizard page class from which an export wizard is selected.
 * 
 * @since 3.2
 *
 */
public class ExportPage extends ImportExportPage {
	private static final String STORE_SELECTED_EXPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME
		+ "STORE_SELECTED_EXPORT_WIZARD_ID"; //$NON-NLS-1$
	
	private static final String STORE_EXPANDED_EXPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
		+ "STORE_EXPANDED_EXPORT_CATEGORIES";	//$NON-NLS-1$

	CategorizedWizardSelectionTree exportTree;

	/**
	 * The export service is injected by OSGi container. It resides in a bundle
	 * which contains several exporters.
	 */
	@Inject
	private IFakturamaExportService exportService;
	
	@Inject
	private IEclipseContext ctx;

	@Inject
	private Messages msg;
	
	@Inject
	private MApplication application;

	private ExportWizardRegistry exportWizardRegistry;

	@Inject
	public ExportPage(/* E4Workbench workbench*/) {
		super(null, null);
//		this.exportService = exportService;
	}
	
//	@PostConstruct
	public void createControl(Composite parent) {
		super.createControl(parent);
		setTitle(msg.wizardExportCommonHeadline);
		setDescription(msg.wizardExportCommonDescription);
		// setImageDescriptor(image);
		// IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ
	}

	
	protected void initialize() {
//		workbench.getHelpSystem().setHelp(getControl(),
//                IWorkbenchHelpContextIds.EXPORT_WIZARD_SELECTION_WIZARD_PAGE);
	}

	protected Composite createTreeViewer(Composite parent) {
		IE4WizardCategory root = getExportWizardRegistry().getRootCategory();
		
		exportTree = new CategorizedWizardSelectionTree(
				root, msg.wizardExportCommonFilterlabel);
		Composite exportComp = exportTree.createControl(parent);
		exportTree.getViewer().addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				listSelectionChanged(event.getSelection());    	       			
			}
		});
		exportTree.getViewer().addDoubleClickListener(new IDoubleClickListener(){
	    	public void doubleClick(DoubleClickEvent event) {
	    		treeDoubleClicked(event);
	    	}
	    });
		setTreeViewer(exportTree.getViewer());
	    return exportComp;
	}
	
	private IWizardRegistry getExportWizardRegistry() {
		if(exportWizardRegistry == null) {
			MAddon addon = application.getAddons().stream().filter(
					a -> a.getElementId().equals("fakturama.addon.wizardregistry")).findFirst().get();
			exportWizardRegistry = (ExportWizardRegistry) addon.getObject();
		}
		return exportWizardRegistry;
	}

	/**
	 * @return the exportService
	 */
	public IFakturamaExportService getExportService() {
		return exportService;
	}

	public void saveWidgetValues(){
    	storeExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES, exportTree.getViewer());
        storeSelectedCategoryAndWizard(STORE_SELECTED_EXPORT_WIZARD_ID, exportTree.getViewer()); 	
        super.saveWidgetValues();
	}
	
	protected void restoreWidgetValues(){
//        IE4WizardCategory exportRoot = WorkbenchPlugin.getDefault().getExportWizardRegistry().getRootCategory();
//        expandPreviouslyExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES, exportRoot, exportTree.getViewer());
//        selectPreviouslySelected(STORE_SELECTED_EXPORT_WIZARD_ID, exportRoot, exportTree.getViewer());       
        super.restoreWidgetValues();
	}
//	
//	protected ITriggerPoint getTriggerPoint(){
//		return getWorkbench().getActivitySupport()
//    		.getTriggerPointManager().getTriggerPoint(WorkbenchTriggerPoints.EXPORT_WIZARDS);
//	}
	
	protected void updateMessage(){
//		setMessage(WorkbenchMessages.ImportExportPage_chooseExportDestination); 
		super.updateMessage();
	}
}