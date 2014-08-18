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
public class OpenParcelServiceHandler /*extends Action */{

//	private DocumentEditor documentEditor;
//
//	private DataSetDocument dataSetDocument = null;
//
	//T: Text of the action
	public final static String ACTIONTEXT = "Parcel Service"; 

	
//	/**
//	 * Constructor
//	 */
//	public OpenParcelServiceHandler() {
//		
//		super(ACTIONTEXT);
//
//		//T: Tool Tip Text
//		setToolTipText(_("Open the web site of the parcel service.") );
//
//		// The id is used to refer to the action in a menu or toolbar
//		setId(ICommandIds.CMD_OPEN_PARCEL_SERVICE);
//
//		// Associate the action with a pre-defined command, to allow key
//		// bindings.
//		setActionDefinitionId(ICommandIds.CMD_OPEN_PARCEL_SERVICE);
//
//		// sets a default 16x16 pixel icon.
//		setImageDescriptor(com.sebulli.fakturama.Activator.getImageDescriptor("/icons/16/parcel_16.png"));
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
//		// cancel, if the data base is not connected.
//		if (!DataBaseConnectionState.INSTANCE.isConnected())
//			return;
//
//		
//		BrowserEditor browserEditor = null;
//		ParcelServiceBrowserEditor parcelServiceBrowserEditor = null;
//		
//		// Get the active workbench window
//		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		final IWorkbenchPage page = workbenchWindow.getActivePage();
//		// Get the active part (view)
//		IWorkbenchPart part = null;
//		if (page != null)
//			part = page.getActivePart();
//
//		
//		// Get the active editor
//		Editor editor = (Editor) page.getActiveEditor();
//		
//		// Do not use the editor, if the ViewDocumentTable is selected
//		if (part instanceof ViewDocumentTable) {
//			editor = null;
//		}
//			
//		if (editor != null) {
//			
//			// A document editor is active
//			if (editor instanceof DocumentEditor) {
//
//				// Search in the folder "Templates" and also in the folder with the localized  name
//				documentEditor = (DocumentEditor) editor;
//
//				// Get the document of the editor
//				dataSetDocument = documentEditor.getDocument();
//			}
//			
//			// A web browser editor is active
//			if (editor instanceof BrowserEditor) {
//				browserEditor = (BrowserEditor)editor;
//				dataSetDocument = null;
//			}
//
//			// A parcel service browser editor is active
//			if (editor instanceof ParcelServiceBrowserEditor) {
//				parcelServiceBrowserEditor = (ParcelServiceBrowserEditor)editor;
//				dataSetDocument = null;
//			}
//			
//			
//			
//		}
//		else if (part != null){
//			ISelection selection;
//
//			// Cast the part to ViewDataSetTable
//			if (part instanceof ViewDataSetTable) {
//
//				ViewDataSetTable view = (ViewDataSetTable) part;
//
//				// does the view exist ?
//				if (view != null) {
//
//					//get the selection
//					selection = view.getSite().getSelectionProvider().getSelection();
//
//					if (selection != null && selection instanceof IStructuredSelection) {
//
//						Object obj = ((IStructuredSelection) selection).getFirstElement();
//
//						// If we had a selection let change the state
//						if (obj != null) {
//							if (obj instanceof DataSetDocument)
//							dataSetDocument = (DataSetDocument) obj;
//						}
//					}
//				}
//			}
//		}
//
//		
//		
//		// Set the editor's input and open a new editor 
//		if (dataSetDocument != null) {
//			if (dataSetDocument instanceof DataSetDocument) {
//				
//				final ParcelServiceManager parcelServiceManager = new ParcelServiceManager();
//
//				// Are there more than one parcel services ?
//				// Display a menu with all.
//				if (parcelServiceManager.size() > 1) {
//
//					// Create a menu
//					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//					Menu menu = new Menu(shell, SWT.POP_UP);
//					
//					// Add an entry for each parcel service
//					for (int i = 0; i < parcelServiceManager.size(); i++) {
//						final MenuItem item = new MenuItem(menu, SWT.PUSH);
//						item.setText(parcelServiceManager.getName(i));
//						item.setData(i);
//						item.addListener(SWT.Selection, new Listener() {
//							public void handleEvent(Event e) {
//								// open the parcel service browser
//								parcelServiceManager.setActive((Integer) item.getData());
//								ParcelServiceBrowserEditorInput input = new ParcelServiceBrowserEditorInput(dataSetDocument, parcelServiceManager );
//								openParcelServiceBrowser(page, input);
//							}
//						});
//					}
//
//					// Set the location of the pup up menu near to the upper left corner,
//					// but with an gap, so it should be under the tool bar icon of this action.
//					int x = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().getCursorLocation().x;
//					int y = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().getCursorLocation().y;
//					
//					menu.setLocation(x + 4, y + 4);
//					menu.setVisible(true);
//				} 
//				// There is only one parcel service. Do not show a menu
//				else if (parcelServiceManager.size() == 1){
//					ParcelServiceBrowserEditorInput input = new ParcelServiceBrowserEditorInput(dataSetDocument, parcelServiceManager);
//					openParcelServiceBrowser(page, input);
//				}
//					
//
//
//			}
//		}
//		else if (browserEditor != null ){
//			// Test the form fields
//			browserEditor.testParcelServiceForm();
//		}
//		else if (parcelServiceBrowserEditor != null ){
//			// Fill the form
//			parcelServiceBrowserEditor.fillForm();
//		}
//		else {
//			// Show an information dialog, if no document is selected
//			MessageBox messageBox = new MessageBox(workbenchWindow.getShell(), SWT.ICON_WARNING | SWT.OK );
//			//T: Title of the dialog 
//			messageBox.setText(_("Information"));
//			//T: Text of the dialog
//			messageBox.setMessage(_("You have to open or select a document."));
//			messageBox.open();
//		}
//	}
//	
//	/**
//	 * Open a new browser editor with the parcel service's web site
//	 * 
//	 * @param page
//	 * 	Workbench page
//	 * @param input
//	 * 	Parcel service browser editor input
//	 */
//	private void openParcelServiceBrowser (IWorkbenchPage page, ParcelServiceBrowserEditorInput input) {
//		// Open the editor
//		try {
//			if (page != null) {
//
//				// If the browser editor is already open, reset the URL
//				ParcelServiceBrowserEditor parcelServiceBrowserEditor = (ParcelServiceBrowserEditor) page.findEditor(input);
//				if (parcelServiceBrowserEditor != null)
//					parcelServiceBrowserEditor.resetUrl();
//
//				page.openEditor(input, ParcelServiceBrowserEditor.ID);
//			}
//		}
//		catch (PartInitException e) {
//			Logger.logError(e, "Error opening Editor: " + ParcelServiceBrowserEditor.ID);
//		}
//		
//		
//	}
}
