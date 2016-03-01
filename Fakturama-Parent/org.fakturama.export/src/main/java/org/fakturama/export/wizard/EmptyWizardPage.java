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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.fakturama.export.ExportMessages;

import com.sebulli.fakturama.log.ILogger;


/**
 * Common wizard page. This page is used if only one wizard page is needed.
 * 
 */
public class EmptyWizardPage extends WizardPage {

	private static final String WIZARD_PAGE_NAME = "Wizard Page";
	
	@Inject
	@Translation
	protected ExportMessages exportMessages;
	
	@Inject
	private ILogger log;
	
	private Image previewImage = null;

	public static final String WIZARD_TITLE = "title";
	public static final String WIZARD_DESCRIPTION = "description";
	public static final String WIZARD_PREVIEW_IMAGE = "previewimage";
	
	public EmptyWizardPage() {
		super(WIZARD_PAGE_NAME);
	}
	
	/**
	 * Constructor Create the page and set title and message.
	 */
	public EmptyWizardPage(String title, String message) {
		super(WIZARD_PAGE_NAME);
		//T: Title of the Sales Export Wizard Page 1
		setTitle(title);
		//T: Text of the Sales Export Wizard Page 1
		setMessage( message );
	}
	
	/**
	 * Create the page and set title, message and preview image
	 */
	@PostConstruct
	public void initialize(IEclipseContext ctx) {
		//T: Title of the Sales Export Wizard Page 1
		setTitle((String) ctx.get(WIZARD_TITLE));
		//T: Text of the Sales Export Wizard Page 1
		setMessage((String) ctx.get(WIZARD_DESCRIPTION));
		this.previewImage = (Image) ctx.get(WIZARD_PREVIEW_IMAGE);
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
		
		// Display a preview image, if it is not empty
		if (previewImage != null) {
			// Preview image
			Label preview = new Label(top, SWT.NONE);
			preview.setText(exportMessages.wizardCommonPreviewLabel);
			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(preview);
			try {
				preview.setImage(previewImage);
			}
			catch (IllegalArgumentException | SWTException e) {
				log.error(e, "Icon not found");
			}
		}

		setControl(top);
	}

}
