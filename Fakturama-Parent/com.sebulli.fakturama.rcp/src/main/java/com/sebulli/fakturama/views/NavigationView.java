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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ExpandAdapter;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
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
	
	@Inject
	private ECommandService commandService;
	
    @Inject
    @Preference
    private IEclipsePreferences preferences;
	
	@Inject
	@Translation
	protected Messages msg;
	
	private List<PGroup> groupList = new ArrayList<>();

    private Composite composite;
    
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@PostConstruct
	public void createPartControl(final Composite parent) {

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ContextHelpConstants.NAVIGATION_VIEW);
	    
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

		// Create the first expand bar "Import"
	    PGroup group = createPGroup("command.navigation.import", Icon.ICON_SHOP);
	    addAction(group, Icon.COMMAND_IMPORT, "command.webshop", CommandIds.CMD_WEBSHOP_IMPORT);
		
		// Create the 2nd expand bar "Data"
	    PGroup group2 = createPGroup("command.data.name", Icon.ICON_LETTER);
	    addAction(group2, Icon.COMMAND_LETTER, "command.documents", CommandIds.CMD_OPEN_DOCUMENTS);
	    addAction(group2, Icon.COMMAND_PRODUCT, "command.products", CommandIds.CMD_OPEN_PRODUCTS);
	    addAction(group2, Icon.COMMAND_CONTACT, "command.contacts", CommandIds.CMD_OPEN_CONTACTS);
	    addAction(group2, Icon.COMMAND_PAYMENT, "command.payments", CommandIds.CMD_OPEN_PAYMENTS);
	    addAction(group2, Icon.COMMAND_SHIPPING, "command.shippings", CommandIds.CMD_OPEN_SHIPPINGS);
	    addAction(group2, Icon.COMMAND_VAT, "command.vats", CommandIds.CMD_OPEN_VATS);
	    addAction(group2, Icon.COMMAND_TEXT, "command.texts", CommandIds.CMD_OPEN_TEXTS);
	    addAction(group2, Icon.COMMAND_LIST, "command.lists", CommandIds.CMD_OPEN_LISTS);
	    addAction(group2, Icon.COMMAND_EXPENDITURE_VOUCHER, "command.expenditurevouchers", CommandIds.CMD_OPEN_EXPENDITUREVOUCHERS);
	    addAction(group2, Icon.COMMAND_RECEIPT_VOUCHER, "command.receiptvouchers", CommandIds.CMD_OPEN_RECEIPTVOUCHERS);

		// Create the 3rd expand bar "Create new"
	    PGroup group3 = createPGroup("command.new.name", Icon.ICON_PRODUCT_NEW);
	    addAction(group3, Icon.COMMAND_PRODUCT, "command.new.product", CommandIds.CMD_NEW_PRODUCT);
	    addAction(group3, Icon.COMMAND_CONTACT, "command.new.contact", CommandIds.CMD_NEW_CONTACT);

		// Create the 4th expand bar "export"
		/*
	    PGroup group4 = createPGroup("main.menu.export", Icon.ICON_PRODUCT_NEW);
	    addAction(group4, Icon.COMMAND_EXPORT, "command.export", CommandIds.CMD_EXPORT);
		final ExpandBar bar4 = new ExpandBar(expandBarManager, top, SWT.NONE, msg("Export"),  Icon.COMMAND_EXPORT ,
				msg("Export documents, contacts .. to tables and files"));

		bar4.addAction(new ExportSalesAction());
*/
		// Create the 5th expand bar "Miscellaneous"
	    PGroup group5 = createPGroup("command.navigation.misc", Icon.ICON_MISC);
	    addAction(group5, Icon.COMMAND_PARCEL, "command.parcelservice", CommandIds.CMD_OPEN_PARCEL_SERVICE);
	    addAction(group5, Icon.COMMAND_WWW, "command.browser", CommandIds.CMD_OPEN_BROWSER_EDITOR);
	    addAction(group5, Icon.COMMAND_CALCULATOR, "command.calculator", CommandIds.CMD_OPEN_CALCULATOR);
//	    addAction(group5, Icon.COMMAND_REORGANIZE, "ReorganizeDocumentsAction.116", CommandIds.CMD_REOGANIZE_DOCUMENTS);
	}

	private void addAction(PGroup group,
			Icon commandIcon, String commandIconDescriptor,
			final String commandId) {
		CLabel label = new CLabel(group, SWT.NORMAL);
		label.setImage(commandIcon.getImage(IconSize.DefaultIconSize));
		label.setToolTipText(msg.getMessageFromKey(commandIconDescriptor + ".tooltip"));
		
		// formerly known as ACTIONTEXT
		label.setText(msg.getMessageFromKey(commandIconDescriptor + ".name"));
		label.addMouseListener(new MouseAdapter() {
            @Override
			public void mouseDown(MouseEvent e) {
				Command cmd = commandService.getCommand(commandId);
				ParameterizedCommand pCmd = new ParameterizedCommand(cmd, null);
				if (handlerService.canExecute(pCmd)) {
					handlerService.executeHandler(pCmd);
				} else {
					MessageDialog.openInformation(composite.getShell(),
							"Action Info", "not yet implemented :-(");
				}
			}
		});
	}

	private PGroup createPGroup(String groupName, Icon groupIcon) {
		PGroup group = new PGroup(composite, SWT.SMOOTH);
		//T: Title of an expand bar in the navigations view
	    group.setText(msg.getMessageFromKey(groupName));
	    group.setToolTipText(msg.getMessageFromKey(groupName + ".tooltip"));
	    group.setImage(groupIcon.getImageDescriptor(IconSize.ToolbarIconSize).createImage());
	    group.setImagePosition(SWT.LEFT | SWT.TOP);
	    
	    GridData gd = new GridData();
	    gd.horizontalAlignment = SWT.FILL;
	    gd.grabExcessHorizontalSpace = true;
	    group.setLayoutData(gd);
	    group.setLayout(new GridLayout());
	    
	    group.addExpandListener(new ExpandAdapter() {
	        @Override
	        public void itemExpanded(ExpandEvent e) {
	            PGroup current = (PGroup)e.getSource();
	            // Collapse expand bar items, or not
	            if (preferences.getBoolean(Constants.PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR, Boolean.FALSE)) {
	                groupList.stream().filter(g -> g != current).forEach(g -> g.setExpanded(false));
	            }
	        }
        });
	    
	    groupList.add(group);
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
