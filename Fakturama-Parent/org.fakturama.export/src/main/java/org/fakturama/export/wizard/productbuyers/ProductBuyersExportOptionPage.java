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

package org.fakturama.export.wizard.productbuyers;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.fakturama.export.ExportMessages;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.resources.ITemplateResourceManager;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * Create the first (and only) page of the sales export wizard. This page is
 * used to select the start and end date.
 */
public class ProductBuyersExportOptionPage extends WizardPage {
	
	public static final String WIZARD_SORT_BY_QUANTITY = "WIZARD_SORT_BY_QUANTITY";

	@Inject
	@Translation
	protected ExportMessages exportMessages;

    @Inject
    protected ILogger log;

	@Inject
	private ITemplateResourceManager resourceManager;

	//Control elements
	private Button buttonQ;
	private Button buttonV;
	
	/**
	 * Constructor Create the page and set title and message.
	 */
	public ProductBuyersExportOptionPage(String title, String label) {
		super("ExportOptionPage");
		//T: Title of the Sales Export Wizard Page 1
		setTitle(title);
		setMessage(label );
	}
	
	/**
	 * Default constructor. Used only for injection. <br /> 
	 * WARNING: Use <b>only</b> with injection since some
	 * initial values are set in initialize method.
	 */
	public ProductBuyersExportOptionPage() {
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

		// Preview image
		Image previewImage = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.EXPORT_PRODUCT_BUYERS); 
		Label preview = new Label(top, SWT.BORDER);
		preview.setText(exportMessages.wizardCommonPreviewLabel);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(preview);
		try {
			preview.setImage(previewImage);
		}
		catch (Exception e) {
			log.error(e, "Icon not found");
		}

		// Create the label with the help text
		Label labelDescription = new Label(top, SWT.NONE);
		
		//T: Export Sales Wizard Page 1 - Long description.
		labelDescription.setText(exportMessages.wizardExportOptionSorting);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(0, 10).applyTo(labelDescription);

		// Radio buttons for sort order
		buttonQ = new Button (top, SWT.RADIO);
		buttonQ.setText (exportMessages.wizardExportOptionSortingQuantity);
		buttonV = new Button (top, SWT.RADIO);
		buttonV.setText (exportMessages.wizardExportOptionSortingVolume);

		// Default: Sort by volume
		buttonV.setSelection (true);
	}

	/**
	 * Return whether the data should be sorted by quantity or by volume
	 * 
	 * @return 
	 * 		True, if the data should be sorted by quantity
	 */
	public boolean getSortByQuantity() {
		return buttonQ.getSelection();
	}
}
