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

package com.sebulli.fakturama.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.IParcelService;
import com.sebulli.fakturama.parcelservice.ParcelServiceFormFiller;
import com.sebulli.fakturama.resources.core.Icon;

/**
 * Parcel Service Web Browser Editor
 * 
 */
public class ParcelServiceBrowserEditor {
	public static final String ID = "com.sebulli.fakturama.editors.parcelServiceBrowserEditor";

    @Inject
    private Logger log;
    
    @Inject
    private IEclipseContext ctx;

	// SWT components of the editor
	private Composite top;
	private Browser browser;
	
	@Inject
	private IParcelService manager;

    @Inject
    private EPartService partService;
	
	private MPart part;
	
	// The form filler
	private ParcelServiceFormFiller parcelServiceFormFiller;

	public Browser getBrowser() {
		return browser;
	}

	/**
	 * Creates the content of the editor
	 * 
	 * @param parent
	 *            Parent control element
	 */
	@PostConstruct
	public void createPartControl(final Composite parent, @Active MPart page) {
// Initialize the editor. Set the URL as part name
        this.part = (MPart) parent.getData("modelElement");
        part.getTags().add("documentWindow");  // mark this part as a documentWindow. This tag is evaluated by CloseAllHandler.
		parcelServiceFormFiller = ContextInjectionFactory.make(ParcelServiceFormFiller.class, ctx);
		// Set the name
		part.setLabel(manager.getName());
		part.setIconURI(Icon.COMMAND_PARCEL.getIconURI());
		
		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);

		// Format the top composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(top);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.PARCELSERVICE_EDITOR);

		// Create a new web browser control
		try {
			browser = new Browser(top, SWT.NONE);
			browser.setBackground(JFaceResources.getColorRegistry().get(Constants.COLOR_WHITE));
			browser.addProgressListener(new ProgressListener() {
				@Override
				public void completed(ProgressEvent event) {
					parcelServiceFormFiller.fillForm(browser, part, false);
				}

				@Override
				public void changed(ProgressEvent event) {
				}

			});
			GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);

			// Open the web site: URL
			browser.setUrl(manager.getUrl());
			
			browser.addOpenWindowListener(new OpenWindowListener() {
				public void open(WindowEvent event) {
					Browser newBrowser = null;
					if (!event.required) return;	/* only do it if necessary */
					
					// Sets the document with the address data as input for the editor.
					// Open the editor
					try {
						if (page != null) {

							// If the browser editor is already open, reset the URL
					        MPart myPart = partService.findPart(ParcelServiceBrowserEditor.ID);
							if (myPart != null) {
								ParcelServiceBrowserEditor parcelServiceBrowserEditor = (ParcelServiceBrowserEditor)myPart;
								parcelServiceBrowserEditor.resetUrl();
							}

//							page.openEditor(input, ParcelServiceBrowserEditor.ID);
//							newBrowser = ((ParcelServiceBrowserEditor)page.getActiveEditor()).getBrowser();
						}
					}
					catch (Exception e) {
						log.error(e, "Error opening Editor: " + ParcelServiceBrowserEditor.ID);
					}

					// Return the new browser
					event.browser = newBrowser;
				}
			});

		}
		catch (Exception e) {
			log.error(e, "Error opening parcel service browser");
			return;
		}
	}

	/**
	 * Go to the start page (fakturama.sebulli.com)
	 */
	public void resetUrl() {

		// set the URL
		if (browser != null)
			browser.setUrl(manager.getUrl());
	}

	
	/**
	 * Set the focus to the top composite.
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
	 */
	@Focus
	public void setFocus() {
		if(top != null) 
			top.setFocus();
	}


	/**
	 * Fill the form with the document data
	 */
	public void fillForm() {
		parcelServiceFormFiller.fillForm(browser, part, true);
	}
	
}
