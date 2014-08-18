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

package com.sebulli.fakturama.handlers;


/**
 * This action opens the project website in an editor.
 * 
 * @author Gerd Bartelt
 */
public class OpenBrowserEditorHandler/* extends Action*/ {

	//T: Text of the action to open the webbrowser
	public final static String ACTIONTEXT = "Web Browser"; 
//
//	// URL of the Fakturama project site
//	public final static String FAKTURAMA_PROJECT_URL = "http://www.fakturama.org/";
//	
//	// Open the Fakturama forum
//	private boolean useFakturamaProjectURL;
//	
//	/**
//	 * Constructor
//	 */
//	public OpenBrowserEditorHandler(boolean useFakturamaProjectURL) {
//		super("www.fakturama.org");
//
//		this.useFakturamaProjectURL = useFakturamaProjectURL;
//		
//		//T: Tool Tip Text
//		setToolTipText(_("Open the project web site www.fakturama.org") );
//
//		// The id is used to refer to the action in a menu or toolbar
//		setId(ICommandIds.CMD_OPEN_BROWSER_EDITOR);
//
//		// Associate the action with a pre-defined command, to allow key
//		// bindings.
//		setActionDefinitionId(ICommandIds.CMD_OPEN_BROWSER_EDITOR);
//
//		// sets a default 16x16 pixel icon.
//		setImageDescriptor(com.sebulli.fakturama.Activator.getImageDescriptor("/icons/16/www_16.png"));
//	}
//
//	/**
//	 * Run the action
//	 * 
//	 * Set the URL and open the editor.
//	 */
//	@Override
//	public void run() {
//
//		// Get the active workbench window
//		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//
//		// Sets the URL
//		String url;
//		if (this.useFakturamaProjectURL)
//			url = FAKTURAMA_PROJECT_URL;
//		else {
//			url = Activator.getDefault().getPreferenceStore().getString("GENERAL_WEBBROWSER_URL");
//
//			// In case of an empty URL: use the start page
//			if (url.isEmpty() || url.equals("http://www.fakturama.org/"))
//				url = "file://" +
//					Workspace.INSTANCE.getWorkspace() + "/" +
//					Workspace.INSTANCE.getTemplateFolderName() +  
//					"/Start/start.html";
//			
//		}
//
//		// In case of an URL with only "-" do not show an editor
//		if (url.equals("-"))
//			return;
//
//		
//		// Add the "http://" or "file://"
//		if ((!url.toLowerCase().startsWith("http://")) && 
//			(!url.toLowerCase().startsWith("file://")) )
//			url = "http://" + url;
//		
//		// Check, if the URL is the Fakturama project
//		boolean isFakturamaProjectUrl = url.equalsIgnoreCase(FAKTURAMA_PROJECT_URL);
//			
//		// Add version and language a a GET parameter
//		// The language is uses, if the project website can generate
//		// localized content.
//		if (isFakturamaProjectUrl) {
//			url += "?version=" + Activator.getDefault().getBundle().getVersion();
//			url += "&lang=" + Locale.getDefault().getCountry();
//		}
//
//		// Sets the URL as input for the editor.
//		BrowserEditorInput input;
//		if (isFakturamaProjectUrl)
//			//T: Short description of start page 
//			input = new BrowserEditorInput(url, _("Fakturama Project"), true);
//		else
//			//T: Short description of start page 
//			input = new BrowserEditorInput(url, _("Start"), false);
//
//		// Open the editor
//		try {
//			if (workbenchWindow != null) {
//				IWorkbenchPage page = workbenchWindow.getActivePage();
//				if (page != null) {
//
//					// If the browser editor is already open, reset the URL
//					BrowserEditor browserEditor = (BrowserEditor) page.findEditor(input);
//					if (browserEditor != null)
//						browserEditor.resetUrl();
//
//					page.openEditor(input, BrowserEditor.ID);
//				}
//			}
//		}
//		catch (PartInitException e) {
//			Logger.logError(e, "Error opening Editor: " + BrowserEditor.ID);
//		}
//	}
}
