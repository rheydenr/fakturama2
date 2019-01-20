/**
 * 
 */
package org.fakturama.export.wizard.vouchers;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.ExportWizardPageStartEndDate;
import org.fakturama.wizards.IExportWizard;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;

/**
 * Wizard for exporting expenditures and receipts in one file.
 * 
 */
public class ReceiptsAndExpendituresWizard extends Wizard implements IExportWizard {

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;
	    
	@Inject
	@Preference(nodePath = "/instance/com.sebulli.fakturama.rcp")
	private IEclipsePreferences eclipsePrefs;

	@Inject
	private IEclipseContext ctx;

	// The first (and only) page of this wizard
	ExportWizardPageStartEndDate page1;
	VoucherExportOptionPage page2;

	/**
	 * Initializes this creation wizard using the passed workbench and object
	 * selection.
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@PostConstruct
	@Override
	public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
		setWindowTitle("Einnahmen und Ausgaben");
		ctx.set(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE, null);

		ctx.set(IFakturamaWizardService.WIZARD_TITLE, exportMessages.wizardExportReceiptvouchersTitle);
		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, exportMessages.wizardExportExpendituresDescription);
		ctx.set(ExportWizardPageStartEndDate.WIZARD_DATESELECT_DONTUSETIMEPERIOD, Boolean.FALSE);
		page1 = ContextInjectionFactory.make(ExportWizardPageStartEndDate.class, ctx);

		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, exportMessages.wizardExportAccountsTableListentriesTitle);
		page2 = ContextInjectionFactory.make(VoucherExportOptionPage.class, ctx);

		addPage(page1);
		addPage(page2);
	}

	@Override
	public boolean performFinish() {
		ctx.set(Constants.PARAM_START_DATE, page1.getStartDate());
		ctx.set(Constants.PARAM_END_DATE, page1.getEndDate());
		ctx.set(ExportWizardPageStartEndDate.WIZARD_DATESELECT_DONTUSETIMEPERIOD, page1.getDoNotUseTimePeriod());
		ctx.set(VoucherExportOptionPage.SHOW_VOUCHER_SUM_COLUMN, page2.getShowVoucherSumColumn());
		ctx.set(VoucherExportOptionPage.SHOW_ZERO_VAT_COLUMN, page2.getShowZeroVatColumn());

		VoucherExporter exporter = ContextInjectionFactory.make(VoucherExporter.class, ctx);
		return exporter.export("Einnahmen und Ausgaben", VoucherExporter.ALL);
	}

}
