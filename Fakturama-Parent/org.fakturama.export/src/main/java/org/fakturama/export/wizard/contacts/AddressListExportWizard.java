package org.fakturama.export.wizard.contacts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.fakturama.export.wizard.EmptyWizardPage;
import org.fakturama.wizards.IExportWizard;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.resources.ITemplateResourceManager;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * Export wizard to export an address list
 * 
 */
public class AddressListExportWizard extends Wizard implements IExportWizard {

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	private ITemplateResourceManager resourceManager;
    
	// The first (and only) page of this wizard
	private EmptyWizardPage page1;

	@Inject
	private IEclipseContext ctx;

	/**
	 * Performs any actions appropriate in response to the user having pressed
	 * the Finish button, or refuse if finishing now is not permitted.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		AddressListExport exporter = ContextInjectionFactory.make(AddressListExport.class, ctx);
		return exporter.export();
	}

	/**
	 * Adds the first (and only) page to the wizard
	 */
	@PostConstruct
	@Override
	public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
		setWindowTitle(msg.pageExport);
		Image previewImage = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.EXPORT_CONTACTS);
		ctx.set(EmptyWizardPage.WIZARD_TITLE, msg.wizardExportContactsAllcontactsTitle);
		ctx.set(EmptyWizardPage.WIZARD_DESCRIPTION, msg.wizardExportContactsAllcontactsDescription);
		ctx.set(EmptyWizardPage.WIZARD_PREVIEW_IMAGE, previewImage);
		page1 = ContextInjectionFactory.make(EmptyWizardPage.class, ctx);
		addPage(page1);
	}


}
