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

import static com.sebulli.fakturama.Translate._;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.handlers.ICommandIds;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * This class represents the navigation view of the workbench
 * 
 * @author Gerd Bartelt
 */
public class NavigationView {
	// ID of this view
	public static final String ID = "com.sebulli.fakturama.navigationView"; //$NON-NLS-1$

	@Inject
	private EHandlerService handlerService;
	  
	  private Composite composite;
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@PostConstruct
	public void createPartControl(final Composite parent, 
			final ECommandService commandService) {

		composite = new Composite(parent, SWT.NONE);
	    composite.setLayout(new GridLayout());

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ContextHelpConstants.NAVIGATION_VIEW);

		// Create the first expand bar "Import"
	    PGroup group = createPGroup("command.navigation.import", Icon.ICON_SHOP);
	    addAction(commandService, group, Icon.COMMAND_IMPORT, "command.webshop", ICommandIds.CMD_WEBSHOP_IMPORT);
		
		// Create the 2nd expand bar "Data"
	    PGroup group2 = createPGroup("main.menu.data", Icon.ICON_LETTER);
	    addAction(commandService, group2, Icon.COMMAND_LETTER, "command.documents", ICommandIds.CMD_OPEN_DOCUMENTS);
	    addAction(commandService, group2, Icon.COMMAND_PRODUCT, "command.products", ICommandIds.CMD_OPEN_PRODUCTS);
	    addAction(commandService, group2, Icon.COMMAND_CONTACT, "command.contacts", ICommandIds.CMD_OPEN_CONTACTS);
	    addAction(commandService, group2, Icon.COMMAND_PAYMENT, "command.payments", ICommandIds.CMD_OPEN_PAYMENTS);
	    addAction(commandService, group2, Icon.COMMAND_SHIPPING, "command.shippings", ICommandIds.CMD_OPEN_SHIPPINGS);
	    addAction(commandService, group2, Icon.COMMAND_VAT, "command.vats", ICommandIds.CMD_OPEN_VATS);
	    addAction(commandService, group2, Icon.COMMAND_TEXT, "command.texts", ICommandIds.CMD_OPEN_TEXTS);
	    addAction(commandService, group2, Icon.COMMAND_LIST, "command.lists", ICommandIds.CMD_OPEN_LISTS);
	    addAction(commandService, group2, Icon.COMMAND_EXPENDITURE_VOUCHER, "command.expenditurevouchers", ICommandIds.CMD_OPEN_EXPENDITUREVOUCHERS);
	    addAction(commandService, group2, Icon.COMMAND_RECEIPT_VOUCHER, "command.receiptvouchers", ICommandIds.CMD_NEW_RECEIPTVOUCHER);

		// Create the 3rd expand bar "Create new"
	    PGroup group3 = createPGroup("main.menu.new", Icon.ICON_PRODUCT_NEW);
	    addAction(commandService, group3, Icon.COMMAND_PRODUCT, "command.new.product", ICommandIds.CMD_NEW_PRODUCT);
	    addAction(commandService, group3, Icon.COMMAND_CONTACT, "command.new.contact", ICommandIds.CMD_NEW_CONTACT);

		// Create the 4th expand bar "export"
		/*
	    PGroup group4 = createPGroup("main.menu.export", Icon.ICON_PRODUCT_NEW);
	    addAction(commandService, group4, Icon.COMMAND_EXPORT, "command.export", ICommandIds.CMD_EXPORT);
		final ExpandBar bar4 = new ExpandBar(expandBarManager, top, SWT.NONE, _("Export"),  Icon.COMMAND_EXPORT ,
				_("Export documents, contacts .. to tables and files"));

		bar4.addAction(new ExportSalesAction());
*/
		// Create the 5th expand bar "Miscellaneous"
	    PGroup group5 = createPGroup("command.navigation.misc", Icon.ICON_PRINTOO);
	    addAction(commandService, group5, Icon.COMMAND_PARCEL, "command.parcelservice", ICommandIds.CMD_OPEN_PARCEL_SERVICE);
	    addAction(commandService, group5, Icon.COMMAND_WWW, "command.browser", ICommandIds.CMD_OPEN_BROWSER_EDITOR);
	    addAction(commandService, group5, Icon.COMMAND_CALCULATOR, "command.calculator", ICommandIds.CMD_OPEN_CALCULATOR);
//	    addAction(commandService, group5, Icon.COMMAND_REORGANIZE, "ReorganizeDocumentsAction.116", ICommandIds.CMD_REOGANIZE_DOCUMENTS);
	}

	private void addAction(final ECommandService commandService, PGroup group,
			Icon commandIcon, String commandIconDescriptor,
			final String commandId) {
		CLabel label = new CLabel(group, SWT.NORMAL);
		label.setImage(commandIcon.getImage(IconSize.DefaultIconSize));
		label.setToolTipText(_(commandIconDescriptor + ".tooltip"));
		
		// formerly known as ACTIONTEXT
		label.setText(_(commandIconDescriptor + ".name"));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Command cmd = commandService.getCommand(commandId);
				ParameterizedCommand pCmd = new ParameterizedCommand(cmd, null);
				if (handlerService.canExecute(pCmd)) {
					handlerService.executeHandler(pCmd);
				} else {
					MessageDialog.openInformation(composite.getShell(),
							"Action Info", "Webshop import can't be executed!");
				}
			}
		});
	}

	private PGroup createPGroup(String groupName, Icon groupIcon) {
		PGroup group = new PGroup(composite, SWT.SMOOTH);
		//T: Title of an expand bar in the navigations view
	    group.setText(_(groupName));
	    group.setToolTipText(_(groupName + ".tooltip"));
	    group.setImage(groupIcon.getImageDescriptor(IconSize.ToobarIconSize).createImage());
	    group.setImagePosition(SWT.LEFT | SWT.TOP);
	    
	    GridData gd = new GridData();
	    gd.horizontalAlignment = SWT.FILL;
	    gd.grabExcessHorizontalSpace = true;
	    group.setLayoutData(gd);
	    group.setLayout(new GridLayout());
		return group;
	}

	/**
	 * Set the focus to the top composite.
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
	 */
	@Focus
	public void setFocus() {
		if(composite != null) 
			composite.setFocus();
	}
	
	
}
