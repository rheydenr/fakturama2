/**
 * 
 */
package org.fakturama.imp.wizard.csv.orders;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.fakturama.imp.ImportMessages;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.handlers.CreateOODocumentHandler;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.parts.widget.contentprovider.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.EntityLabelProvider;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 *
 */
public class ImportOrdersOptionPage extends WizardPage {

	public static final String WIZARD_TITLE = "title";
	public static final String WIZARD_DESCRIPTION = "description";
	public static final String WIZARD_PREVIEW_IMAGE = "previewimage";
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	@Translation
	protected ImportOrdersMessages importOrdersMessages;
	
	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	private ILogger log;

    @Inject
    protected ShippingsDAO shippingsDao;

    @Inject @Optional
    private IPreferenceStore preferences;
    
    @Inject
    private IDialogSettings dialogSettings;

    @Inject
    protected IEclipseContext context;

	//Control elements
	private Image previewImage = null;
	private ComboViewer comboViewerShipping;
	private ComboViewer comboViewerOrderTemplates;
	private ComboViewer comboViewerDeliveryTemplates;

	/**
	 * Constructor Create the page and set title and message.
	 */
	public ImportOrdersOptionPage(String title, String label, @Optional ProgramImages image) {
		super("ImportOptionPage");
		//T: Title of the Import Wizard Page 1
		setTitle(title);
		setMessage(label);
	}
	
	/**
	 * Default constructor. Used only for injection. <br /> 
	 * WARNING: Use <b>only</b> with injection since some
	 * initial values are set in initialize method.
	 */
	public ImportOrdersOptionPage() {
		super("ImportOrdersOptionPage");
	}

	@PostConstruct
	public void initialize(IEclipseContext ctx) {
		setTitle((String) ctx.get(WIZARD_TITLE));
		setMessage((String) ctx.get(WIZARD_DESCRIPTION));
		this.previewImage = (Image) ctx.get(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		// Create the top composite
		Composite top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(top);
		setControl(top);

		// Preview image
		if (previewImage != null) {
			Label preview = new Label(top, SWT.BORDER);
			preview.setText(importMessages.wizardCommonPreviewLabel);
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(preview);
			try {
				preview.setImage(previewImage);
			}
			catch (Exception e) {
				log.error(e, "Icon not found");
			}
		}
        
        // Shipping composite contains label and combo.
        Composite shippingComposite = new Composite(top, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(shippingComposite);
        GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).applyTo(shippingComposite);

        // Shipping label
        Label shippingLabel = new Label(shippingComposite, SWT.NONE);
        //T: Document Editor - Label shipping 
        shippingLabel.setText(importOrdersMessages.wizardImportCsvOrdersOptionsShippingValue);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(shippingLabel);

        // Shipping combo
        comboViewerShipping = new ComboViewer(shippingComposite, SWT.BORDER | SWT.READ_ONLY);
        comboViewerShipping.getCombo().setToolTipText(importOrdersMessages.wizardImportCsvOrdersOptionsShippingTooltip);
        GridDataFactory.swtDefaults().hint(250, SWT.DEFAULT).grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(comboViewerShipping.getCombo());

        comboViewerShipping.setContentProvider(new EntityComboProvider());
        comboViewerShipping.setLabelProvider(new EntityLabelProvider());

        // Fill the shipping combo with the shipping values.
        List<Shipping> allShippings = shippingsDao.findAll();
        comboViewerShipping.setInput(allShippings);
        long shippingId = dialogSettings.get(OrderImportWizard.ORDERS_CSV_IMPORT_SHIPPING) != null 
        		? dialogSettings.getLong(OrderImportWizard.ORDERS_CSV_IMPORT_SHIPPING) 
        		: preferences.getLong(Constants.DEFAULT_SHIPPING);
        Shipping shippingPreselection = shippingsDao.findById(shippingId);
		ISelection shippingSelection = null;
		if(shippingPreselection != null) {
			shippingSelection = new StructuredSelection(shippingPreselection);
		} else if(!allShippings.isEmpty()) {
			shippingSelection = new StructuredSelection(allShippings.get(0));
		}
		log.debug(String.format("shippingPreselection=%s", shippingPreselection));
		if(shippingSelection != null) {
			comboViewerShipping.setSelection(shippingSelection);
		}
        
        // Order Template combo
        Label templateLabel = new Label(shippingComposite, SWT.NONE);
        templateLabel.setText(importOrdersMessages.wizardImportCsvOrdersOptionsOrdertemplatesValue);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(templateLabel);
        
        comboViewerOrderTemplates = new ComboViewer(shippingComposite, SWT.BORDER | SWT.READ_ONLY);
        comboViewerOrderTemplates.getCombo().setToolTipText(importOrdersMessages.wizardImportCsvOrdersOptionsOrdertemplatesTooltip);
        GridDataFactory.swtDefaults().hint(250, SWT.DEFAULT).grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(comboViewerOrderTemplates.getCombo());
        comboViewerOrderTemplates.setContentProvider(new PathComboProvider());
        comboViewerOrderTemplates.setLabelProvider(new PathLabelProvider());

        // Fill the templates combo with the template values.
		CreateOODocumentHandler ooContextHandler = ContextInjectionFactory.make(CreateOODocumentHandler.class, context);
		List<Path> orderTemplates = ooContextHandler.collectTemplates(DocumentType.ORDER);
        comboViewerOrderTemplates.setInput(orderTemplates);        
        Path preselectedOrderTemplate = dialogSettings.get(OrderImportWizard.ORDERS_CSV_IMPORT_ORDERTEMPLATE) != null 
        		? Paths.get(dialogSettings.get(OrderImportWizard.ORDERS_CSV_IMPORT_ORDERTEMPLATE)) 
        		: orderTemplates.get(0);
        final ISelection orderTemplateSelection = new StructuredSelection(preselectedOrderTemplate);
        comboViewerOrderTemplates.setSelection(orderTemplateSelection);
        
        // Delivery Template combo
        Label deliveryTemplateLabel = new Label(shippingComposite, SWT.NONE);
        deliveryTemplateLabel.setText(importOrdersMessages.wizardImportCsvOrdersOptionsDeliverytemplatesValue);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(deliveryTemplateLabel);
        
        comboViewerDeliveryTemplates = new ComboViewer(shippingComposite, SWT.BORDER | SWT.READ_ONLY);
        comboViewerDeliveryTemplates.getCombo().setToolTipText(importOrdersMessages.wizardImportCsvOrdersOptionsDeliverytemplatesTooltip);
        GridDataFactory.swtDefaults().hint(250, SWT.DEFAULT).grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(comboViewerDeliveryTemplates.getCombo());
        comboViewerDeliveryTemplates.setContentProvider(new PathComboProvider());
        comboViewerDeliveryTemplates.setLabelProvider(new PathLabelProvider());
        
        // Fill the templates combo with the template values.
        List<Path> deliveryTemplates = ooContextHandler.collectTemplates(DocumentType.DELIVERY);
        comboViewerDeliveryTemplates.setInput(deliveryTemplates);        
        Path preselectedDeliveryTemplate = dialogSettings.get(OrderImportWizard.ORDERS_CSV_IMPORT_DELIVERYTEMPLATE) != null 
        		? Paths.get(dialogSettings.get(OrderImportWizard.ORDERS_CSV_IMPORT_DELIVERYTEMPLATE)) 
        		: deliveryTemplates.get(0);
        final ISelection deliveryTemplateSelection = new StructuredSelection(preselectedDeliveryTemplate);
        comboViewerDeliveryTemplates.setSelection(deliveryTemplateSelection);
	}
	
	public java.util.Optional<Shipping> getSelectedShipping() {
		java.util.Optional<Shipping> retval = java.util.Optional.empty();
		if(comboViewerShipping != null && comboViewerShipping.getStructuredSelection() != null) {
			retval = java.util.Optional.ofNullable((Shipping)comboViewerShipping.getStructuredSelection().getFirstElement());
		}
		return retval;
	}
	
	public java.util.Optional<Path> getSelectedOrderTemplate() {
		java.util.Optional<Path> retval = java.util.Optional.empty();
		if(comboViewerOrderTemplates != null && comboViewerOrderTemplates.getStructuredSelection() != null) {
			retval = java.util.Optional.ofNullable((Path)comboViewerOrderTemplates.getStructuredSelection().getFirstElement());
		}
		return retval;
	}
	
	public java.util.Optional<Path> getSelectedDeliveryTemplate() {
		java.util.Optional<Path> retval = java.util.Optional.empty();
		if(comboViewerDeliveryTemplates != null && comboViewerDeliveryTemplates.getStructuredSelection() != null) {
			retval = java.util.Optional.ofNullable((Path)comboViewerDeliveryTemplates.getStructuredSelection().getFirstElement());
		}
		return retval;
	}
}
