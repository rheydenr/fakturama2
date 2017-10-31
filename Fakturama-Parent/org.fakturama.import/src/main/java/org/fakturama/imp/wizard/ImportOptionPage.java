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

package org.fakturama.imp.wizard;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.fakturama.imp.ImportMessages;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * Create a page of the import wizard. This page is
 * used to select some options.
 * 
 */
public class ImportOptionPage extends WizardPage {

	public static final String WIZARD_TITLE = "title";
	public static final String WIZARD_DESCRIPTION = "description";
	public static final String WIZARD_PREVIEW_IMAGE = "previewimage";
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	private ILogger log;

	//Control elements
	private Button buttonUpdateExisting;
	private Button buttonUpdateWithEmptyValues;
	private Image previewImage = null;
	private Text quoteChar, separator;
	
	/**
	 * Constructor Create the page and set title and message.
	 */
	public ImportOptionPage(String title, String label, ProgramImages image) {
		super("ImportOptionPage");
		//T: Title of the Import Wizard Page 1
		setTitle(title);
		setMessage(label );
	}
	
	/**
	 * Default constructor. Used only for injection. <br /> 
	 * WARNING: Use <b>only</b> with injection since some
	 * initial values are set in initialize method.
	 */
	public ImportOptionPage() {
		super("ImportOptionPage");
	}
	
	@PostConstruct
	public void initialize(IEclipseContext ctx) {
		setTitle((String) ctx.get(WIZARD_TITLE));
//		setMessage((String) ctx.get(WIZARD_DESCRIPTION));
		this.previewImage = (Image) ctx.get(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE);
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

		// Preview image
		if (previewImage != null) {
			Label preview = new Label(top, SWT.BORDER);
			preview.setText(importMessages.wizardCommonPreviewLabel);
			GridDataFactory.swtDefaults().span(2, 1).align(SWT.BEGINNING, SWT.CENTER).applyTo(preview);
			try {
				preview.setImage(previewImage);
			}
			catch (Exception e) {
				log.error(e, "Icon not found");
			}
		}
		
		// Create the label with the help text
		Label labelDescription = new Label(top, SWT.NONE);
		
		//T: Import Wizard Page 1 - Long description.
		labelDescription.setText(importMessages.wizardImportOptionsSet);
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.BEGINNING, SWT.CENTER).indent(0, 10).applyTo(labelDescription);

		buttonUpdateExisting = new Button (top, SWT.CHECK);
		buttonUpdateExisting.setText(importMessages.wizardImportOptionsUpdate);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(buttonUpdateExisting);

		buttonUpdateWithEmptyValues = new Button (top, SWT.CHECK);
		buttonUpdateWithEmptyValues.setText(importMessages.wizardImportOptionsEmptyupdate);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(buttonUpdateWithEmptyValues);
		
		Label quoteCharLbl = new Label(top, SWT.NONE);
		quoteCharLbl.setText(importMessages.wizardImportOptionsQuotechar);
		GridDataFactory.swtDefaults().hint(190, SWT.DEFAULT).grab(false, false).applyTo(quoteCharLbl);
		
		quoteChar = new Text(top, SWT.BORDER);
		quoteChar.setText("\"");
		GridDataFactory.swtDefaults().hint(10, SWT.DEFAULT).grab(false, false).applyTo(quoteChar);
		Label separatorLbl = new Label(top, SWT.NONE);
		separatorLbl.setText(importMessages.wizardImportOptionsSeparator);
		separator = new Text(top, SWT.BORDER);
		separator.setText(";");
		GridDataFactory.swtDefaults().hint(10, SWT.DEFAULT).grab(false, false).applyTo(separator);
	}

	/**
	 * Return whether existing entries should be overwritten
	 * 
	 * @return 
	 * 		True, if they should be overwritten
	 */
	public boolean getUpdateExisting() {
		return buttonUpdateExisting.getSelection();
	}
	
	/**
	 * Return whether empty cells should be imported
	 * 
	 * @return 
	 * 		True, if they should be imported
	 */
	public boolean getUpdateWithEmptyValues() {
		return buttonUpdateWithEmptyValues.getSelection();
	}

	/**
	 * @return the quoteChar
	 */
	public String getQuoteChar() {
		return quoteChar.getText();
	}

	/**
	 * @param quoteChar the quoteChar to set
	 */
	public void setQuoteChar(String quoteChar) {
		this.quoteChar.setText(quoteChar);
	}

	/**
	 * @return the separator
	 */
	public String getSeparator() {
		return separator.getText();
	}

	/**
	 * @param separator the separator to set
	 */
	public void setSeparator(String separator) {
		this.separator.setText(separator);
	}
	
	public ImportOptions getImportOptions() {
		return ImportOptions.importOptions()
			.withQuoteChar(getQuoteChar())
			.withSeparator(getSeparator())
			.withUpdateExisting(getUpdateExisting())
			.withUpdateWithEmptyValues(getUpdateWithEmptyValues())
			.build();
	}
}
