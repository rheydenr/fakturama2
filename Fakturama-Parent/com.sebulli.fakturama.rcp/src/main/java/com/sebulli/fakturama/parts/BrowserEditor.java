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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.handlers.OpenBrowserEditorHandler;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.parcelservice.ParcelServiceFormFiller;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * Web Browser Editor
 * 
 */
public class BrowserEditor {
	public static final String ID = "com.sebulli.fakturama.editors.browserEditor";

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
//    @Preference(value=Constants.DEFAULT_PREFERENCES_NODE) //==> doesn't work :-(
    private IPreferenceStore preferences;

    @Inject
    private ILogger log;
    
    @Inject
    private IEclipseContext ctx;
    
    private MPart part;

    private String url;

	// SWT components of the editor
	private Composite top;
	private Browser browser;

	// Button, to go home to the fakturama website
	private Button homeButton;

	// URL of the last site on fakturama.sebulli.com
	private String lastFakturamaURL = "";

	// the URL is the Fakturama project
	private boolean isFakturamaProjectUrl;
	
	// Show or hide the URL bar
	private boolean showURLbar;
	
	// Button, to go home to fakturama.com
	private Composite homeButtonComposite;

	// The URL textbox 
	private Text urlText = null; 

	/**
	 * Creates the content of the editor
	 * 
	 * @param parent
	 *            Parent control element
	 */
	@PostConstruct
	public void createPartControl(final Composite parent) {
// Initialize the editor. Set the URL as part name
        this.part = (MPart) parent.getData("modelElement");
        part.setIconURI(Icon.COMMAND_WWW.getIconURI());

		url =  (String) part.getTransientData().get(OpenBrowserEditorHandler.PARAM_URL);
//		setPartName(input.getName());
		isFakturamaProjectUrl = BooleanUtils.toBoolean((Boolean) part.getTransientData().get(OpenBrowserEditorHandler.PARAM_USE_PROJECT_URL));
        // Sets the URL
        if (this.isFakturamaProjectUrl) {
            url = OpenBrowserEditorHandler.FAKTURAMA_PROJECT_URL;
        } else {
            url = getPreferences().getString(Constants.PREFERENCES_GENERAL_WEBBROWSER_URL);

            // In case of an empty URL: use the start page
            if (StringUtils.isBlank(getPreferences().getString(Constants.GENERAL_WORKSPACE))) {
				url = "-";
			} else {
				if (url.isEmpty() || url.equals(OpenBrowserEditorHandler.FAKTURAMA_PROJECT_URL)) {
				    url = "file://" +
				        StringUtils.appendIfMissing(getPreferences().getString(Constants.GENERAL_WORKSPACE).replaceAll("\\\\", "/"), "/") +
				        StringUtils.appendIfMissing(msg.configWorkspaceTemplatesName, "/") +  
				        "Start/start.html";
				}
			}
        }

        // In case of an URL with only "-" do not show an editor
        if (url.equals("-")) {
            // if we do this, the initial view gets damaged!
//            part.setVisible(false);
            return;
        }
		
		showURLbar = getPreferences().getBoolean(Constants.PREFERENCES_BROWSER_SHOW_URL_BAR);

		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);

		// Format the top composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(top);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.BROWSER_EDITOR);

		//Show or hide the url bar
		if (showURLbar) {
			// Create a composite that will contain the home button
			Composite urlComposite = new Composite(top, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(urlComposite);
			GridLayoutFactory.fillDefaults().numColumns(6).applyTo(urlComposite);
			
			// The add back button
			Label backButton = new Label(urlComposite, SWT.NONE);
			//T: Browser Editor
			//T: Tool Tip Text
			backButton.setToolTipText(msg.editorBrowserButtonBack);
			backButton.setImage(Icon.BROWSER_BROWSER_BACK.getImage(IconSize.BrowserIconSize));

			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(backButton);
			backButton.addMouseListener(new MouseAdapter() {

				// Click on this icon
				public void mouseDown(MouseEvent e) {
					if (browser != null)
					browser.back();
				}
			});

			// The add forward button
			Label forwardButton = new Label(urlComposite, SWT.NONE);
			//T: Browser Editor
			//T: Tool Tip Text
			forwardButton.setToolTipText(msg.editorBrowserButtonForward);
			forwardButton.setImage(Icon.BROWSER_BROWSER_FORWARD.getImage(IconSize.BrowserIconSize));

			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(forwardButton);
			forwardButton.addMouseListener(new MouseAdapter() {

				// Click on this icon
				public void mouseDown(MouseEvent e) {
					if (browser != null) {
					    browser.forward();
					}
				}
			});


			// The add reload button
			Label reloadButton = new Label(urlComposite, SWT.NONE);
			//T: Browser Editor
			//T: Tool Tip Text
			reloadButton.setToolTipText(msg.editorBrowserButtonReload);
			reloadButton.setImage(Icon.BROWSER_BROWSER_RELOAD.getImage(IconSize.BrowserIconSize));

			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(reloadButton);
			reloadButton.addMouseListener(new MouseAdapter() {

				// Click on this icon
				public void mouseDown(MouseEvent e) {
					if (browser != null) {
					    browser.refresh();
					}
				}
			});

			// The add stop button
			Label stopButton = new Label(urlComposite, SWT.NONE);
			//T: Browser Editor
			//T: Tool Tip Text
			stopButton.setToolTipText(msg.editorBrowserButtonStop);
			stopButton.setImage(Icon.BROWSER_BROWSER_STOP.getImage(IconSize.BrowserIconSize));

			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(stopButton);
			stopButton.addMouseListener(new MouseAdapter() {

				// Click on this icon
				public void mouseDown(MouseEvent e) {
					if (browser != null) {
					    browser.stop();
					}
				}
			});


			// The add home button
			Label hButton = new Label(urlComposite, SWT.NONE);
			//T: Browser Editor
			//T: Tool Tip Text
			hButton.setToolTipText(msg.editorBrowserButtonHome);
			hButton.setImage(Icon.BROWSER_BROWSER_HOME.getImage(IconSize.BrowserIconSize));

			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(hButton);
			hButton.addMouseListener(new MouseAdapter() {

				// Click on this icon
				public void mouseDown(MouseEvent e) {
					if (browser != null) {
					    browser.setUrl(url);
					}
				}
			});
			

			// URL field
			urlText = new Text(urlComposite, SWT.BORDER);
			urlText.setText("http://");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(urlText);
			urlText.addKeyListener(new KeyAdapter() {

				/**
				 * Enter
				 */
				@Override
				public void keyPressed(KeyEvent e) {
					if ( (e.keyCode == 13) && (browser != null) ) {
						browser.setUrl(urlText.getText());
					}
				}
			});
			
		}
		else {
			// Create a composite that will contain the home button
			homeButtonComposite = new Composite(top, SWT.NONE);
			Color color = new Color(null, 0xc8, 0xda, 0xe4);
			homeButtonComposite.setBackground(color);
			GridDataFactory.fillDefaults().grab(true, false).hint(0, 0).applyTo(homeButtonComposite);
			GridLayoutFactory.fillDefaults().applyTo(homeButtonComposite);
			color.dispose();
		}
		
		// Create a new web browser control
		try {
			int browserStyle = SWT.NONE;
			
			// Use the browser style from the preferences
			int browserType = getPreferences().getInt(Constants.PREFERENCES_BROWSER_TYPE);
			
			if (browserType == 1)
				browserStyle = SWT.WEBKIT;

			if (browserType == 2)
				browserStyle = SWT.MOZILLA;
			
			browser = new Browser(top, browserStyle);
			Color browserColor = new Color(null, 0xff, 0xff, 0xff);
			browser.setBackground(browserColor);
			browserColor.dispose();

			browser.addProgressListener(new ProgressListener() {
				@Override
				public void completed(ProgressEvent event) {
					String browserURL = browser.getUrl();
					boolean isValidURL = browserURL.startsWith("http://") || browserURL.startsWith("https://") || browserURL.startsWith("file://");
					if (showURLbar){
						if (urlText != null) {
						    
//							String startUrl = "file://" +
//				                    StringUtils.appendIfMissing(getPreferences().getString(Constants.GENERAL_WORKSPACE).replaceAll("\\\\", "/"), "/") +
//				                    StringUtils.appendIfMissing(msg.configWorkspaceTemplatesName, "/") +  
//				                    "Start/start.html";
//
//							if (! browserURL.equals(startUrl)  ){
								if (isValidURL)
									urlText.setText(browserURL);
//							}
//							else {
//								urlText.setText("http://");
//							}
						}
					}
				}

				// If the website has changes, add a "go back" button
				@Override
				public void changed(ProgressEvent event) {
					
					String browserURL = browser.getUrl();
					boolean isValidURL = browserURL.startsWith("http://") || browserURL.startsWith("https://") || browserURL.startsWith("file://");
					if (showURLbar)
						return;
					
					// We are back at home - remove the button (if it exists)
					if ((browserURL.startsWith(OpenBrowserEditorHandler.FAKTURAMA_PROJECT_URL) && !browserURL.startsWith("http://www.fakturama.org/mantis")) || !isValidURL || !isFakturamaProjectUrl) {

						// Store this URL as last URL
						lastFakturamaURL = browserURL;

						if (homeButton != null) {
							homeButton.dispose();
							homeButton = null;
							GridDataFactory.fillDefaults().grab(true, false).hint(0, 0).applyTo(homeButtonComposite);
							top.layout(true);
						}
					}
					// We are on an other web site - add the back to home button
					else if (isValidURL) {
						if (homeButton == null) {
							homeButton = new Button(homeButtonComposite, SWT.NONE);
							//T: Button to go back to the Fakturama home page
							homeButton.setText(msg.editorBrowserButtonProject);
							homeButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 

									// Restore the last URL
									browser.setUrl(lastFakturamaURL);
							}));
							GridDataFactory.swtDefaults().applyTo(homeButton);
							GridDataFactory.fillDefaults().grab(true, false).applyTo(homeButtonComposite);
							top.layout(true);
						}
					}
				}

			});
			browser.addTitleListener(new TitleListener() {
				
				@Override
				public void changed(TitleEvent event) {
					part.setLabel(StringUtils.abbreviate(event.title, 20));
				}
			});
			GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);

			// Open the website: url
			resetUrl(url);
			
		}
		catch (Exception e) {
			log.error(e, "Error opening browser");
			return;
		}
	}
	
	/**
	 * Go to the start page (OpenBrowserEditorHandler.FAKTURAMA_PROJECT_URL)
	 * @param url 
	 */
	public void resetUrl(String url) {

		// set the URL
	    this.url = url;
		if (browser != null) {
			browser.setUrl(url);
		}
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
	 * Fills the form of the parcel service with the address data
	 */
	public void fillForm(Browser browser) {
		this.browser = browser;
	}
	
	/**
	 * Test the parcel service form.
	 * Fills all form elements with its names and creates a template file.
	 */
	public void testParcelServiceForm() {
		ParcelServiceFormFiller parcelServiceFormFiller = ContextInjectionFactory.make(ParcelServiceFormFiller.class, ctx);
		parcelServiceFormFiller.testParcelServiceForm(browser);  
	}

	/**
	 * @return the preferences
	 */
	private IPreferenceStore getPreferences() {
		if(preferences == null) {
			preferences = EclipseContextFactory.getServiceContext(Activator.getContext()).get(IPreferenceStore.class);
		}
		return preferences;
	}
}
