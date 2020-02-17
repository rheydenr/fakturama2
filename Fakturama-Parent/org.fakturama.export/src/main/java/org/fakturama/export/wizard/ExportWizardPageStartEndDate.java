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

package org.fakturama.export.wizard;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.fakturama.export.ExportMessages;
import org.fakturama.wizards.IFakturamaWizardService;

/**
 * Create the first (and only) page of the sales export wizard. This page is
 * used to select the start and end date.
 * 
 */
public class ExportWizardPageStartEndDate extends WizardPage {
	
	public static final String WIZARD_DATESELECT_DONTUSETIMEPERIOD = "WIZARD_DATESELECT_DONTUSETIMEPERIOD";

	public static final String WIZARD_SINGLEPAGE = "singlepage";

	@Inject
	@Translation
	protected ExportMessages exportMessages;
	
	// start and end date
	private Label labelStart;
	private Label labelEnd;
	private CDateTime dtStartDate;
	private CDateTime dtEndDate;
	private boolean singlePage = false;

	// Use start and end date or export all
	private Button bDoNotUseTimePeriod;
	private boolean doNotUseTimePeriod;
	
	private String label;
	
	/**
	 * Constructor Create the page and set title and message.
	 */
	public ExportWizardPageStartEndDate(String title, String label, boolean doNotUseTimePeriod ) {
		super("ExportWizandPageStartEndDate");
		//T: Title of the Accounts Export Wizard Page 1
		setTitle(title);
		//T: Text of the Accounts Export Wizard Page 1
//		setMessage(exportMessages.wizardExportDateselectTitle);
		this.label = label;
		this.doNotUseTimePeriod = doNotUseTimePeriod;
	}
	
	/**
	 * Default constructor. Used only for injection. <br /> 
	 * WARNING: Use <b>only</b> with injection since some
	 * initial values are set in initialize method.
	 */
	public ExportWizardPageStartEndDate() {
		super("ExportWizandPageStartEndDate");
	}
	
	/**
	 * Create the page and set title, message and preview image
	 */
	@PostConstruct
	public void initialize(IEclipseContext ctx) {
		setTitle((String) ctx.get(IFakturamaWizardService.WIZARD_TITLE));
		this.label = (String) ctx.get(IFakturamaWizardService.WIZARD_DESCRIPTION);
		this.doNotUseTimePeriod = (Boolean) ctx.get(WIZARD_DATESELECT_DONTUSETIMEPERIOD);
		this.singlePage = Optional.ofNullable((Boolean)ctx.get(WIZARD_SINGLEPAGE)).orElse(Boolean.FALSE);
	}
	
	/**
	 * Enables or disables the date widget, depending on the
	 * value of "doNotUseTimePeriod"
	 */
	private void enableDisableDateWidget() {
		dtStartDate.setEnabled(!doNotUseTimePeriod);
		dtEndDate.setEnabled(!doNotUseTimePeriod);
		labelStart.setEnabled(!doNotUseTimePeriod);
		labelEnd.setEnabled(!doNotUseTimePeriod);
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
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(top);
		setControl(top);

		// Create the label with the help text
		Label labelDescription = new Label(top, SWT.NONE);
		
		//T: Export Sales Wizard Page 1 - Long description.
		labelDescription.setText(label);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).indent(0, 10).applyTo(labelDescription);

		// Create a spacer
		Label labelSpacer = new Label(top, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).span(2, 1).indent(0, 10).applyTo(labelSpacer);

		// Label for start date
		labelStart = new Label(top, SWT.NONE);
		
		//T: Export Sales Wizard - Label Start Date of the period
		labelStart.setText(exportMessages.wizardExportDateselectStartdate);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(labelStart);

		// Label for end date
		labelEnd = new Label(top, SWT.NONE);
		//T: Export Sales Wizard - Label End Date of the period
		labelEnd.setText(exportMessages.wizardExportDateselectEnddate);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(20, 0).applyTo(labelEnd);

		// Start date
		dtStartDate = new CDateTime(top, CDT.BORDER | CDT.DROP_DOWN);
		dtStartDate.setFormat(CDT.DATE_MEDIUM);
		
		Date currentDate = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		dtStartDate.setSelection(currentDate);
		
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(150, SWT.DEFAULT).applyTo(dtStartDate);

		dtStartDate.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
				setPageComplete(canFlipToNextPage());
		}));
		
		// End date
		dtEndDate = new CDateTime(top, CDT.BORDER | CDT.DROP_DOWN);
		dtEndDate.setFormat(CDT.DATE_MEDIUM);
		dtEndDate.setSelection(currentDate);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(150, SWT.DEFAULT).indent(20, 0).applyTo(dtEndDate);

		dtEndDate.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
				setPageComplete(canFlipToNextPage());
		}));

		// Enable or disable the date widgets
		enableDisableDateWidget();
		
		// Set the start and end date to the 1st and last day of the
		// last month.
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(dtEndDate.getSelection());
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		dtEndDate.setSelection(calendar.getTime());
		
		calendar.setTime(dtEndDate.getSelection());
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		dtStartDate.setSelection(calendar.getTime());
		
		// Check button: delivery address equals address
		bDoNotUseTimePeriod = new Button(top, SWT.CHECK);
		bDoNotUseTimePeriod.setSelection(doNotUseTimePeriod);
		//T: Label in the export wizard page
		bDoNotUseTimePeriod.setText(exportMessages.wizardExportDateselectExportall);
		GridDataFactory.swtDefaults().applyTo(bDoNotUseTimePeriod);
		bDoNotUseTimePeriod.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
				
				doNotUseTimePeriod = bDoNotUseTimePeriod.getSelection();
				enableDisableDateWidget();
				setPageComplete(canFlipToNextPage());
		}));
	}

	/**
	 * Return the start date as a GregorianCalendar object
	 * 
	 * @return Start date as a GregorianCalendar object
	 */
	public GregorianCalendar getStartDate() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(dtStartDate.getSelection());
		return calendar;
	}

	/**
	 * Return the end date as a GregorianCalendar object
	 * 
	 * @return End date as a GregorianCalendar object
	 */
	public GregorianCalendar getEndDate() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(dtEndDate.getSelection());
		return calendar;
	}
	
	/**
	 * 
	 * Return, if the time period should be used.
	 * 
	 * @return
	 * 		TRUE, if all entries should be exported
	 */
	public boolean getDoNotUseTimePeriod() {
		return doNotUseTimePeriod;
	}


	/**
	 * Flip to the next page only of the start date is before the end date
	 */
	@Override
	public boolean canFlipToNextPage() {
		
		if (doNotUseTimePeriod)
			return true;
		
		return !singlePage && getEndDate().after(getStartDate());
	}
	
	@Override
	public boolean isPageComplete() {
		return doNotUseTimePeriod || getEndDate().after(getStartDate()) ;
	}
	
}
