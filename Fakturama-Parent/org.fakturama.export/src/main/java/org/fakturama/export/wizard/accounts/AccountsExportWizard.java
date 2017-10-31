/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package org.fakturama.export.wizard.accounts;

import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.MonetaryAmount;

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
import com.sebulli.fakturama.misc.DataUtils;

/**
 * Export wizard to export sales
 * 
 */
public class AccountsExportWizard extends Wizard implements IExportWizard {

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

	// The 3 pages of this wizard
	private ExportWizardPageStartEndDate page1;
	private AccountsExportOptionPage page2;
	private AccountSettingsPage page3;

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
		setWindowTitle(msg.pageExport);
		ctx.set(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE, null);

		ctx.set(IFakturamaWizardService.WIZARD_TITLE, exportMessages.wizardExportAccountsTableTitle);
		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, exportMessages.wizardExportAccountsTableDescription);
		ctx.set(ExportWizardPageStartEndDate.WIZARD_DATESELECT_DONTUSETIMEPERIOD, Boolean.FALSE);
		page1 = ContextInjectionFactory.make(ExportWizardPageStartEndDate.class, ctx);
		
		ctx.set(IFakturamaWizardService.WIZARD_TITLE, exportMessages.wizardExportAccountsTableListentries);
		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, exportMessages.wizardExportAccountsTableListentriesTitle);
		page2 = ContextInjectionFactory.make(AccountsExportOptionPage.class, ctx);

		ctx.set(IFakturamaWizardService.WIZARD_TITLE, exportMessages.wizardExportAccountsTableAccountsettingsTitle);
		ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, exportMessages.wizardExportAccountsTableAccountsettingsDescription);
		page3 = ContextInjectionFactory.make(AccountSettingsPage.class, ctx);

		addPage(page1);
		addPage(page2);
		addPage(page3);
	}

	/**
	 * Performs any actions appropriate in response to the user having pressed
	 * the Finish button, or refuse if finishing now is not permitted.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		GregorianCalendar accountDate = page3.getDate();
				
		String datePropertyKey = "export_account_date_"  + page2.getSelectedAccount().toLowerCase().replaceAll("/", "\\\\/");
		if (!datePropertyKey.isEmpty()) {
			String datePropertyValue = DataUtils.getInstance().getDateAndTimeAsString(accountDate);
			eclipsePrefs.put(datePropertyKey, datePropertyValue);
		}

		String valuePropertyKey = "export_account_value_"  + page2.getSelectedAccount().toLowerCase().replaceAll("/", "\\\\/");
		MonetaryAmount startValue = page3.getValue();
		if (!valuePropertyKey.isEmpty()) {
			String valuePropertyValue = Double.toString(startValue.getNumber().doubleValue());
			eclipsePrefs.put(valuePropertyKey, valuePropertyValue);
		}
		
		ctx.set(ExportWizardPageStartEndDate.WIZARD_DATESELECT_DONTUSETIMEPERIOD, page1.getDoNotUseTimePeriod());
		if(!page1.getDoNotUseTimePeriod()) {
			// only filter by date if it is wanted
			ctx.set(Constants.PARAM_START_DATE, page1.getStartDate());
			ctx.set(Constants.PARAM_END_DATE, page1.getEndDate());
		} else {
			ctx.remove(Constants.PARAM_START_DATE);
			ctx.remove(Constants.PARAM_END_DATE);
		}
		AccountsExporter exporter = ContextInjectionFactory.make(AccountsExporter.class, ctx);
		return exporter.export(page2.getSelectedAccount(), page3.getDate(), 
				startValue);
	}

}
