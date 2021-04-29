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
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.ExportWizardPageStartEndDate;
import org.fakturama.wizards.IFakturamaWizardService;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.parts.widget.formatter.MoneyFormatter;

/**
 * Create the 3rd page of the account export wizard. This page is
 * used to select start value and date of the selected account.
 * 
 */
public class AccountSettingsPage extends WizardPage {
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;
    
	@Inject
	@Preference(nodePath = "/instance/com.sebulli.fakturama.rcp")
	private IEclipsePreferences eclipsePrefs;
    
    @Inject
    private IDateFormatterService dateFormatterService;
    
	@Inject
	protected IEclipseContext context;

	//Control elements
	private CDateTime dtDate;
	private FormattedText txtValue;
	private MonetaryAmount value = Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit());
	private Label warning;
	
	/**
	 * Constructor Create the page and set title and message.
	 */
	public AccountSettingsPage(String title, String label) {
		super("ExportOptionPage");
		//T: Title of the Sales Export Wizard Page 1
		setTitle(title);
		setMessage(label);
	}
	
	/**
	 * Default constructor. Used only for injection. <br /> 
	 * WARNING: Use <b>only</b> with injection since some
	 * initial values are set in initialize method.
	 */
	public AccountSettingsPage() {
		super("ExportOptionPage");
	}
	
	@PostConstruct
	public void initialize(IEclipseContext ctx) {
		setTitle((String) ctx.get(IFakturamaWizardService.WIZARD_TITLE));
		setMessage((String) ctx.get(IFakturamaWizardService.WIZARD_DESCRIPTION));
	}

	/**
	 * Creates the top level control for this dialog page under the given parent
	 * composite.
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// Create the top composite
		Composite top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(top);
		setControl(top);
		
		((WizardDialog)getContainer()).addPageChangedListener(new IPageChangedListener() {
            
            @Override
            public void pageChanged(PageChangedEvent event) {
                if(event.getSelectedPage() instanceof AccountSettingsPage) {
                    // we assume that the current page was selected
                    GregorianCalendar startDate = ((ExportWizardPageStartEndDate)getWizard().getStartingPage()).getStartDate();
                    if(startDate != null) {
                        dtDate.setSelection(startDate.getTime());
                    }
                }
            }
        });
		
		// Create the label with the help text
		Label labelDescription = new Label(top, SWT.NONE);
		
		//T: Account settings page of account exporter
		labelDescription.setText(exportMessages.wizardExportAccountsStartdatevalue+":");
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(0, 10).applyTo(labelDescription);

		Composite dateAndValue = new Composite(top, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(dateAndValue);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(dateAndValue);
		
		// Start date
		dtDate = new CDateTime(dateAndValue, CDT.BORDER | CDT.DROP_DOWN);
		dtDate.setFormat(CDT.DATE_MEDIUM);
		dtDate.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        checkWarning();
		    }
        });
		
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(150, SWT.DEFAULT).applyTo(dtDate);

		txtValue = new FormattedText(dateAndValue, SWT.BORDER | SWT.RIGHT);
		txtValue.setFormatter(ContextInjectionFactory.make(MoneyFormatter.class, context));
		txtValue.setValue(value);
		
		//T: Account settings page of account exporter
		txtValue.getControl().setToolTipText(exportMessages.wizardExportAccountsStartvalue);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtValue.getControl());

		// Create the label with the warning text
		warning = new Label(top, SWT.NONE);
		
		//T: Export Sales Wizard Page
		warning.setText(exportMessages.wizardExportAccountsStartvalueError);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(warning);
		
		JFaceResources.getColorRegistry().put("red", new RGB(255,0,0));
		Color red = JFaceResources.getColorRegistry().get("red");
		warning.setForeground(red);
		warning.setVisible(false);  // initially set to invisible
		
		// Show or hide the warning
		isPageComplete();
		
	}
	
	final private void checkWarning() {
        // Get the first page with the start and end date
        ExportWizardPageStartEndDate startPage = (ExportWizardPageStartEndDate)getWizard().getStartingPage();
        
        // The date must be before the start date
        boolean isAfterStartDate = getDate().after(startPage.getStartDate());
        
        // If not, show a warning text
        if (isAfterStartDate && warning != null)
            warning.setVisible(isAfterStartDate);
        else
            warning.setVisible(false);
	}
	
	/**
	 * Returns the date as a GregorianCalendar object
	 * 
	 * @return date as a GregorianCalendar object
	 */
	public GregorianCalendar getDate() {
		GregorianCalendar retval = new GregorianCalendar();
		if(dtDate.getSelection() != null) {
			retval.setTime(dtDate.getSelection());
		}
		return retval;
	}

	/**
	 * Returns the value as {@link MonetaryAmount}
	 * 
	 * @return
	 * 		The value as {@link MonetaryAmount}
	 */
	public MonetaryAmount getValue() {
		return Money.of((Double)txtValue.getValue(), DataUtils.getInstance().getDefaultCurrencyUnit());
	}
	
	/**
	 * Sets the start date and value. Use the stored values from the preferences
	 */
	public void setAccountStartValues(String account) {
		
		// Create a property key to store the date and add the name of the account
		String datePropertyKey = "export_account_date_" + account.toLowerCase().replaceAll("/", "\\\\/");
		String date = eclipsePrefs.get(datePropertyKey, "2000-01-01");
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar = dateFormatterService.getCalendarFromDateString(date);

		// Set the date widget with the property from the database
		if (dtDate != null) {
			dtDate.setSelection(calendar.getTime());
		}

		// Create a property key to store the value and add the name of the account
		String valuePropertyKey = "export_account_value_" + account.toLowerCase().replaceAll("/", "\\\\/");
		String valueString = eclipsePrefs.get(valuePropertyKey, "0.0");

		// Set the widget with the value
		if (value != null) {
			value = Money.of(Double.parseDouble(valueString), DataUtils.getInstance().getDefaultCurrencyUnit());
			txtValue.setValue(value);
		}
	}
	
	/**
     * @return the dtDate
     */
    public CDateTime getDtDate() {
        return dtDate;
    }

    /**
	 * Test, whether the page is complete
	 */
	@Override
	public boolean isPageComplete() {
		// Get the first page with the start and end date
		ExportWizardPageStartEndDate startPage = (ExportWizardPageStartEndDate)getWizard().getStartingPage();
		
		if (startPage.getDoNotUseTimePeriod())
			return true;
		
		if (dtDate == null)
			return false;
		
		if(startPage.getStartDate() != null && dtDate.getData() == null) {
		    dtDate.setSelection(startPage.getStartDate().getTime());
		    dtDate.setData("isSet");
		}

		// The date must be before the start date
		boolean isAfterStartDate = getDate().after(startPage.getStartDate());
		
        // If not, show a warning text
        if (isAfterStartDate && warning != null)
            warning.setVisible(isAfterStartDate);
        else
            warning.setVisible(false);
		
		return !isAfterStartDate;
	}
}
