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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.IParcelService;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.parcelservice.ParcelServiceManager;
import com.sebulli.fakturama.parts.BrowserEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.ParcelServiceBrowserEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;

/**
 * This action opens the project website in an editor.
 * 
 */
public class OpenParcelServiceHandler {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private EPartService partService;
    
    @Inject
    private IEclipseContext ctx;

	private DocumentEditor documentEditor;
	private	ParcelServiceBrowserEditor parcelServiceBrowserEditor = null;
	
	//T: Text of the action
	public final static String ACTIONTEXT = "Parcel Service";

	private Document dataSetDocument = null; 
	
	@CanExecute
    public boolean canExecute(final MApplication application, final EModelService modelService) {
		dataSetDocument = null;
		documentEditor = null;
		BrowserEditor activeBrowserEditor = findActiveDocumentOrGetBrowser(application, modelService);
		
        return (activeBrowserEditor != null 
        		|| documentEditor != null
        		|| parcelServiceBrowserEditor != null
        		|| dataSetDocument != null
        		);
    }

	/**
	 * Run the action
	 * 
	 * Set the URL and open the editor.
	 */
	@Execute
	public void run(Shell shell, final MApplication application, final EModelService modelService) {
        MPartStack documentPartStack = (MPartStack) modelService.find(CallEditor.DETAIL_PARTSTACK_ID, application);

		BrowserEditor browserEditor = findActiveDocumentOrGetBrowser(application, modelService);
		
		final IParcelService parcelServiceManager = ContextInjectionFactory.make(ParcelServiceManager.class, ctx);
		// put exactly _this_ IParcelService into context
		ctx.set(IParcelService.class, parcelServiceManager);
		
		// Set the editor's input and open a new editor 
		if (dataSetDocument != null) {
			if (dataSetDocument instanceof Document) {
				
				// Are there more than one parcel services?
				// Display a menu with all.
				if (parcelServiceManager.size() > 1) {

					// Create a menu
					Menu menu = new Menu(shell, SWT.POP_UP);
					
					// Add an entry for each parcel service
					for (int i = 0; i < parcelServiceManager.size(); i++) {
						final MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText(parcelServiceManager.getName(i));
						item.setData(i);
						item.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								// open the parcel service browser
								parcelServiceManager.setActive((Integer) item.getData());
								openParcelServiceBrowser(documentPartStack);
							}
						});
					}

					// Set the location of the pup up menu near to the upper left corner,
					// but with an gap, so it should be under the tool bar icon of this action.
					int x = shell.getDisplay().getCursorLocation().x;
					int y = shell.getDisplay().getCursorLocation().y;
					
					menu.setLocation(x + 4, y + 4);
					menu.setVisible(true);
				} 
				// There is only one parcel service. Do not show a menu
				else if (parcelServiceManager.size() == 1) {
					openParcelServiceBrowser(documentPartStack);
				}
			}
		}
		else if (browserEditor != null ){
			// Test the form fields
			browserEditor.testParcelServiceForm();
		}
		else if (parcelServiceBrowserEditor != null ){
			// Fill the form
			parcelServiceBrowserEditor.fillForm();
		}
		else {
			// Show a warning dialog, if no document is selected
			MessageDialog.openWarning(shell, msg.dialogMessageboxTitleInfo,
					msg.commandParcelserviceWarning);
		}
	}
	
	/**
	 * @param modelService 
	 * @param activePart 
	 * @param application 
	 * @return 
	 * 
	 */
	private BrowserEditor findActiveDocumentOrGetBrowser(MApplication application, EModelService modelService) {
		BrowserEditor browserEditor = null;
		MPart activePart = null;
		
        MPartStack documentPartStack = (MPartStack) modelService.find(CallEditor.DETAIL_PARTSTACK_ID, application);
        
        // first try is to look for an open Document editor
        // this step can't executed together with looking for an opened Browser Editor
        // since we can't determine if one of that windows is on top (isOnTop() is false even if
        // the Editor is opened on top...).
        // Therefore, if we find a Document editor, all went ok.
        for (MStackElement stackElement : documentPartStack.getChildren()) {
			if(stackElement.isVisible() 
				&& (stackElement.getElementId().contentEquals(DocumentEditor.ID))) {
				activePart = (MPart) modelService.find(stackElement.getElementId(), stackElement);
				break;
			}
		}
        
        // nothing found? Then try to find an open Browser Editor
        if(activePart == null) {
	        for (MStackElement stackElement : documentPartStack.getChildren()) {
				if(stackElement.isVisible() 
					&&(stackElement.getElementId().contentEquals(ParcelServiceBrowserEditor.ID)
						|| stackElement.getElementId().contentEquals(BrowserEditor.ID))) {
					activePart = (MPart) modelService.find(stackElement.getElementId(), stackElement);
					if(activePart.getContext() == null) {
						// dead part, reset and go on
						activePart = null;
					} else {
						// found!
						break;
					}
				}
			}
        }
        
        // try to get a reasonable document from documents list view
        if(activePart == null) {
			List<MUIElement> dataPanelElements = modelService.findElements(application, "fakturama.views.documentTable", MUIElement.class, null, EModelService.IN_ACTIVE_PERSPECTIVE);
			if(!dataPanelElements.isEmpty() && dataPanelElements.get(0).getElementId().contentEquals(DocumentsListTable.ID)) {
				activePart= (MPart) dataPanelElements.get(0);
			}
        }
		if (documentEditor == null && activePart != null) {
			
			// A document editor is active
			if (activePart.getElementId().contentEquals(DocumentEditor.ID)) {
				documentEditor = (DocumentEditor) activePart.getObject();

				// Get the document of the editor
				dataSetDocument = documentEditor.getDocument();
			}
			
			// A web browser editor is active
			if (activePart.getElementId().contentEquals(BrowserEditor.ID)) {
				browserEditor = (BrowserEditor)activePart.getObject();
				dataSetDocument = null;
			}

			// A parcel service browser editor is active
			if (activePart.getElementId().contentEquals(ParcelServiceBrowserEditor.ID)) {
				parcelServiceBrowserEditor = (ParcelServiceBrowserEditor)activePart.getObject();
				dataSetDocument = null;
			}

			if(activePart.getElementId().contentEquals(DocumentsListTable.ID)) {
				// Cast the part to ViewDataSetTable
		        @SuppressWarnings("rawtypes")
		        AbstractViewDataTable currentListtable = (AbstractViewDataTable) activePart.getObject();
		        Document[] selectedObjects = (Document[]) currentListtable.getSelectedObjects();
		        if (selectedObjects != null) {
					// If we had a selection let change the state
					dataSetDocument = selectedObjects[0];
				}
			}
		}
		
		return browserEditor;
	}

	/**
	 * Open a new browser editor with the parcel service's web site
	 * @param dataSetDocument 
	 * 
	 * @param page
	 * 	Workbench page
	 * @param input
	 * 	Parcel service browser editor input
	 */
	private void openParcelServiceBrowser(MPartStack stack) {
        // at first we look for an existing Part
        MPart myPart = partService.findPart(ParcelServiceBrowserEditor.ID);

        // if not found then we create a new one from a part descriptor
        if (myPart == null || myPart.getObject() == null) {
            myPart = partService.createPart(CallEditor.DOCVIEW_PARTDESCRIPTOR_ID);
            myPart.setElementId(ParcelServiceBrowserEditor.ID);
//            myPart.setContext(ctx);
            myPart.setContributionURI(CallEditor.BASE_CONTRIBUTION_URI + ParcelServiceBrowserEditor.class.getName());
//            myPart.setLabel(msg.commandBrowserOpenStartpage);
            myPart.getTransientData().put("DOCUMENT", dataSetDocument);

            if(stack == null) {
                stack = (MPartStack) partService.createPart(CallEditor.DOCVIEW_PART_ID);
            }
            stack.getChildren().add(myPart);
        }
        else if(myPart.getObject() != null) {
            // If the browser editor is already open, reset the URL
            ((ParcelServiceBrowserEditor) myPart.getObject()).resetUrl();
        }
        
        partService.activate(myPart);
    }
	
}
