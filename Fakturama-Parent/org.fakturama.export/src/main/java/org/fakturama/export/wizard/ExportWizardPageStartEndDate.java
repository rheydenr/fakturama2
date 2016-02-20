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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

/**
 * Create the first (and only) page of the sales export wizard. This page is
 * used to select the start and end date.
 * 
 * @author Gerd Bartelt
 */
public class ExportWizardPageStartEndDate /*extends WizardPage*/ {

	// start and end date
	private Label labelStart;
	private Label labelEnd;
	private DateTime dtStartDate;
	private DateTime dtEndDate;

	// Use start and end date or export all
	private Button bDoNotUseTimePeriod;
	private boolean doNotUseTimePeriod;
	
	private String label;
//	
//	/**
//	 * Constructor Create the page and set title and message.
//	 */
//	public ExportWizardPageStartEndDate(String title, String label, boolean doNotUseTimePeriod ) {
//		super("ExportWizandPageStartEndDate");
//		//T: Title of the Sales Export Wizard Page 1
//		setTitle(title);
//		//T: Text of the Sales Export Wizard Page 1
//		setMessage(_("Select a Periode") );
//		this.label = label;
//		this.doNotUseTimePeriod = doNotUseTimePeriod;
//	}
//
//	
//	/**
//	 * Enables or disables the date widget, depending on the
//	 * value of "doNotUseTimePeriod"
//	 */
//	private void enableDisableDateWidget() {
//		dtStartDate.setEnabled(!doNotUseTimePeriod);
//		dtEndDate.setEnabled(!doNotUseTimePeriod);
//		labelStart.setEnabled(!doNotUseTimePeriod);
//		labelEnd.setEnabled(!doNotUseTimePeriod);
//	}
//	
//	/**
//	 * Creates the top level control for this dialog page under the given parent
//	 * composite.
//	 * 
//	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
//	 */
//	@Override
//	public void createControl(Composite parent) {
//
//		// Create the top composite
//		Composite top = new Composite(parent, SWT.NONE);
//		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(top);
//		setControl(top);
//
//		// Create the label with the help text
//		Label labelDescription = new Label(top, SWT.NONE);
//		
//		//T: Export Sales Wizard Page 1 - Long description.
//		labelDescription.setText(label);
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).indent(0, 10).applyTo(labelDescription);
//
//		// Create a spacer
//		Label labelSpacer = new Label(top, SWT.NONE);
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).span(2, 1).indent(0, 10).applyTo(labelSpacer);
//
//		// Label for start date
//		labelStart = new Label(top, SWT.NONE);
//		
//		//T: Export Sales Wizard - Label Start Date of the period
//		labelStart.setText(_("Start Date:"));
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(labelStart);
//
//		// Label for end date
//		labelEnd = new Label(top, SWT.NONE);
//		//T: Export Sales Wizard - Label End Date of the period
//		labelEnd.setText(_("End Date:"));
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(20, 0).applyTo(labelEnd);
//
//		// Start date
//		dtStartDate = new DateTime(top, SWT.DROP_DOWN);
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(dtStartDate);
//
//		dtStartDate.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				setPageComplete(canFlipToNextPage());
//			}
//		});
//		
//		// End date
//		dtEndDate = new DateTime(top, SWT.DROP_DOWN);
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(20, 0).applyTo(dtEndDate);
//
//		dtEndDate.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				setPageComplete(canFlipToNextPage());
//			}
//		});
//
//		// Enable or disable the date widgets
//		enableDisableDateWidget();
//		
//		// Set the start and end date to the 1st and last day of the
//		// last month.
//		GregorianCalendar calendar = new GregorianCalendar(dtEndDate.getYear(), dtEndDate.getMonth(), 1);
//		calendar.add(Calendar.DAY_OF_MONTH, -1);
//		dtEndDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//		calendar = new GregorianCalendar(dtEndDate.getYear(), dtEndDate.getMonth(), 1);
//		dtStartDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//		
//		// Check button: delivery address equals address
//		bDoNotUseTimePeriod = new Button(top, SWT.CHECK);
//		bDoNotUseTimePeriod.setSelection(doNotUseTimePeriod);
//		//T: Label in the export wizard page
//		bDoNotUseTimePeriod.setText(_("Export all and do not use a time periode."));
//		GridDataFactory.swtDefaults().applyTo(bDoNotUseTimePeriod);
//		bDoNotUseTimePeriod.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				
//				doNotUseTimePeriod = bDoNotUseTimePeriod.getSelection();
//				enableDisableDateWidget();
//				setPageComplete(canFlipToNextPage());
//			}
//		});
//		
//
//	}
//
//	/**
//	 * Return the start date as a GregorianCalendar object
//	 * 
//	 * @return Start date as a GregorianCalendar object
//	 */
//	public GregorianCalendar getStartDate() {
//		return new GregorianCalendar(dtStartDate.getYear(), dtStartDate.getMonth(), dtStartDate.getDay());
//	}
//
//	/**
//	 * Return the end date as a GregorianCalendar object
//	 * 
//	 * @return End date as a GregorianCalendar object
//	 */
//	public GregorianCalendar getEndDate() {
//		return new GregorianCalendar(dtEndDate.getYear(), dtEndDate.getMonth(), dtEndDate.getDay());
//	}
//	
//	/**
//	 * 
//	 * Return, if the time period should be used.
//	 * 
//	 * @return
//	 * 		TRUE, if all entries should be exported
//	 */
//	public boolean getDoNotUseTimePeriod() {
//		return doNotUseTimePeriod;
//	}
//
//
//	/**
//	 * Flip to the next page only of the start date is before the end date
//	 */
//	@Override
//	public boolean canFlipToNextPage() {
//		
//		if (doNotUseTimePeriod)
//			return true;
//		
//		return (getEndDate().after(getStartDate()));
//	}
	
}
