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

package com.sebulli.fakturama.views;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;

import com.sebulli.fakturama.handlers.ICommandIds;
import com.sebulli.fakturama.resources.ResourceProvider;

/**
 * This class represents the navigation view of the workbench
 * 
 * @author Gerd Bartelt
 */
@SuppressWarnings("restriction") 
public class NavigationView {
	// ID of this view
	public static final String ID = "com.sebulli.fakturama.navigationView"; //$NON-NLS-1$

	//T: Text of the action to open the contacts
	public final static String ACTIONTEXT = "Contact"; 

    @Inject
    private IResourcePool resourcePool;
	
	@Inject
	private EHandlerService handlerService;
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@PostConstruct
	public void createPartControl(Composite parent, 
			final ECommandService commandService) {

		ExpandBar bar = new ExpandBar (parent, SWT.V_SCROLL);
		
		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ContextHelpConstants.NAVIGATION_VIEW);

		// Create the first expand bar "Import"
		Composite composite = new Composite (bar, SWT.NONE);
		composite.setToolTipText("Import data into Fakturama");
		
	    FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
	    fillLayout.marginHeight = 5;
	    fillLayout.marginWidth = 5;
	    fillLayout.spacing = 1;
		
//		bar1.addAction(new WebShopImportAction());
		
		ExpandItem item0 = new ExpandItem (bar, SWT.NONE, 0);
		//T: Title of an expand bar in the navigations view
		item0.setText("Import");
		item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(composite);
		item0.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_ICN_IMPORT));
		item0.setExpanded(true);

		// Create the 2nd expand bar "Data"
		//T: Title of an expand bar in the navigations view
		Composite composite2 = new Composite (bar, SWT.NONE);
		composite2.setToolTipText("Data like documents, products ... ");

		Button button = new Button(composite2, SWT.FLAT);
		button.setText(ACTIONTEXT);
//		GridDataFactory.fillDefaults().indent(5, 0).applyTo(button);
		//T: Tool Tip Text
		button.setToolTipText("Open a list with all the documents");
	    composite2.setLayout(fillLayout);
		
		ExpandItem item1 = new ExpandItem(bar, SWT.NONE);
		item1.setText("Data");
		item1.setHeight(composite2.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setControl(composite2);
		item1.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_ICN_DATA));
		item1.setExpanded(true);

/*
 * 


		// The id is used to refer to the action in a menu or toolbar
		part.setElementId(ICommandIds.CMD_OPEN_CONTACTS);

		// Associate the action with a predefined command, to allow key
		// bindings.
//	is doch schon verknüpft!	setActionDefinitionId(ICommandIds.CMD_OPEN_CONTACTS);

		// sets a default 16x16 pixel icon.
//		setImageDescriptor(Activator.getImageDescriptor("/icons/16/letter_16.png"));
		
 */
		
		
		
//		GridDataFactory.fillDefaults().indent(5, 0).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				Command command = commandService.getCommand(ICommandIds.CMD_OPEN_CONTACTS);
//				// Activate Handler, assume AboutHandler() class exists already
//				handlerService.activateHandler(ICommandIds.CMD_OPEN_CONTACTS, 
//				    new OpenContactsHandler());
				ParameterizedCommand cmd =
				  commandService.createCommand(ICommandIds.CMD_OPEN_CONTACTS, null);
				// Execute the command
				handlerService.executeHandler(cmd); 		
			}
		});

//		bar2.addAction(new OpenDocumentsAction());
//		bar2.addAction(new OpenProductsAction());
//		bar1.addAction(new OpenContactsAction());
//		bar2.addAction(new OpenPaymentsAction());
//		bar2.addAction(new OpenShippingsAction());
//		bar2.addAction(new OpenVatsAction());
//		bar2.addAction(new OpenTextsAction());
//		bar2.addAction(new OpenListsAction());
//		bar2.addAction(new OpenExpenditureVouchersAction());
//		bar2.addAction(new OpenReceiptVouchersAction());
//
//		// Create the 3rd expand bar "Create new"
//		//T: Title of an expand bar in the navigations view
//		final ExpandBar bar3 = new ExpandBar(expandBarManager, top, SWT.NONE, _("New"), "/icons/16/plus_16.png" ,
//				_("Create new documents, products, contacts .. "));
//
//		bar3.addAction(new NewProductAction());
//		bar3.addAction(new NewContactAction(null));
//
//		/*
//		// Create the 4th expand bar "export"
//		//T: Title of an expand bar in the navigations view
//		final ExpandBar bar4 = new ExpandBar(expandBarManager, top, SWT.NONE, _("Export"), "/icons/16/export_16.png" ,
//				_("Export documents, contacts .. to tables and files"));
//
//		bar4.addAction(new ExportSalesAction());
//*/
//		// Create the 5th expand bar "Miscellaneous"
//		//T: Title of an expand bar in the navigations view
//		final ExpandBar bar5 = new ExpandBar(expandBarManager, top, SWT.NONE, _("Miscellaneous"), "/icons/16/misc_16.png" ,
//				_("Miscellaneous"));
//
//		bar5.addAction(new OpenParcelServiceAction());
//		bar5.addAction(new OpenBrowserEditorAction(true));
//		bar5.addAction(new OpenCalculatorAction());
////		bar5.addAction(new ReorganizeDocumentsAction());
	}

//	/**
//	 * Set the focus to the top composite.
//	 * 
//	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
//	 */
//	public void setFocus() {
//		if(top != null) 
//			top.setFocus();
//	}
	

	/**
	 * Add a new action to the body of the expand bar
	 * 
	 * @param action
	 *            The action to add
	 */
	public void addAction(final ParameterizedCommand action, Composite parent, String tooltip) {

		// Create a new composite for the action
		Composite actionComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).applyTo(actionComposite);
		GridDataFactory.fillDefaults().indent(5, 0).applyTo(actionComposite);

//		// Create the action's icon
		Label actionImage = new Label(actionComposite, SWT.NONE);
//		try {
//			actionImage.setImage(action.getImageDescriptor().createImage());
//			actionImage.setToolTipText(action.getToolTipText());
//		}
//		catch (Exception e) {
//			Logger.logError(e, "Icon not found");
//		}
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(0, 0).applyTo(actionImage);

		// Create the action's text
		Label actionLabel = new Label(actionComposite, SWT.NONE);
		try {
			actionLabel.setText(action.getName());
		} catch (NotDefinedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		actionLabel.setToolTipText(tooltip);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(0, 0).applyTo(actionImage);

		// Run the action, if the user clicks in the composite
		actionComposite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				handlerService.executeHandler(action);
			}
		});

		// Run the action, if the user clicks on the icon
		actionImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				handlerService.executeHandler(action);
			}
		});

		// Run the action, if the user clicks on the text
		actionLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				handlerService.executeHandler(action);
			}
		});

	}
	
}
