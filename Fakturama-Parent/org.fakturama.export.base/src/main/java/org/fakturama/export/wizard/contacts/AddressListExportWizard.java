package org.fakturama.export.wizard.contacts;

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.wizard.Wizard;
import org.fakturama.export.wizard.EmptyWizardPage;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * Export wizard to export sales
 * 
 * @author Gerd Bartelt
 */
public class AddressListExportWizard extends Wizard {

	@Inject
	@Translation
	protected Messages msg;
    
	// The first (and only) page of this wizard
	private EmptyWizardPage page1;

	/**
	 * Constructor Adds the first page to the wizard
	 */
	public AddressListExportWizard() {
		//T: Title of the export wizard
		setWindowTitle(msg.pageExport);
		//T: Title of the export wizard
		page1 = new EmptyWizardPage(msg.wizardExportContactsAllcontactsTitle,
				//T: Text of the export wizard
				msg.wizardExportContactsAllcontactsDescription,
				Icon.EXPORT_CONTACTS.getImageDescriptor(IconSize.PreviewIconSize)
		);
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
		AddressListExport exporter = new AddressListExport();
		return exporter.export();
	}


}
