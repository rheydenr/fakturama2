/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
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

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.handlers.OpenBrowserEditorHandler;
import com.sebulli.fakturama.handlers.OpenContactsHandler;
import com.sebulli.fakturama.handlers.OpenListViewsHandler;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.views.datatable.contacts.CreditorListTable;
import com.sebulli.fakturama.views.datatable.contacts.DebitorListTable;
import com.sebulli.fakturama.views.datatable.lists.ItemAccountTypeListTable;
import com.sebulli.fakturama.views.datatable.texts.TextListTable;
import com.sebulli.fakturama.views.datatable.vats.VATListTable;

/**
 * This class represents the navigation view of the workbench
 * 
 */
public class NavigationView {
    // ID of this view
    public static final String ID = "com.sebulli.fakturama.navigationView"; //$NON-NLS-1$

    @Inject
    private EHandlerService handlerService;

    @Inject
    private ECommandService commandService;

    @Inject
    @Preference(value=InstanceScope.SCOPE)
    private IPreferenceStore preferences;

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
        Map<String, Object> parameters;

        // Add context help reference 
        //		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ContextHelpConstants.NAVIGATION_VIEW);
        this.preferences = EclipseContextFactory.getServiceContext(Activator.getContext()).get(IPreferenceStore.class);

        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

        // Create the first expand bar "Import"
        PGroup group = createPGroup("command.navigation.import", Icon.ICON_SHOP);
        addAction(group, Icon.COMMAND_IMPORT, "command.webshop", CommandIds.CMD_WEBSHOP_IMPORT);

        // Create the 2nd expand bar "Data"
        PGroup group2 = createPGroup("command.data.name", Icon.ICON_LETTER);
        addAction(group2, Icon.COMMAND_LETTER, "command.documents", CommandIds.CMD_OPEN_DOCUMENTS);
        addAction(group2, Icon.COMMAND_PRODUCT, "command.products", CommandIds.CMD_OPEN_PRODUCTS);
        
        parameters = new HashMap<>();
        parameters.put(OpenContactsHandler.PARAM_LIST_TYPE, CreditorListTable.ID);
        addAction(group2, Icon.COMMAND_VENDOR, "command.creditors", CommandIds.CMD_OPEN_CONTACTS, parameters);
        
        parameters = new HashMap<>();
        parameters.put(OpenContactsHandler.PARAM_LIST_TYPE, DebitorListTable.ID);
        addAction(group2, Icon.COMMAND_CONTACT, "command.debtors", CommandIds.CMD_OPEN_CONTACTS, parameters);
        
        addAction(group2, Icon.COMMAND_PAYMENT, "command.payments", CommandIds.CMD_OPEN_PAYMENTS);
        addAction(group2, Icon.COMMAND_SHIPPING, "command.shippings", CommandIds.CMD_OPEN_SHIPPINGS);
        
        parameters = new HashMap<>();
        parameters.put(OpenListViewsHandler.PARAM_LIST_TYPE, VATListTable.ID);
        addAction(group2, Icon.COMMAND_VAT, "command.vats", CommandIds.CMD_OPEN_VATS, parameters);
        
        parameters = new HashMap<>();
        parameters.put(OpenListViewsHandler.PARAM_LIST_TYPE, TextListTable.ID);
        addAction(group2, Icon.COMMAND_TEXT, "command.texts", CommandIds.CMD_OPEN_TEXTS, parameters);
        
        parameters = new HashMap<>();
        parameters.put(OpenListViewsHandler.PARAM_LIST_TYPE, ItemAccountTypeListTable.ID);
        addAction(group2, Icon.COMMAND_LIST, "command.lists", CommandIds.CMD_OPEN_LISTS, parameters);
        
        addAction(group2, Icon.COMMAND_EXPENDITURE_VOUCHER, "command.expenditurevouchers", CommandIds.CMD_OPEN_EXPENDITUREVOUCHERS);
        addAction(group2, Icon.COMMAND_RECEIPT_VOUCHER, "command.receiptvouchers", CommandIds.CMD_OPEN_RECEIPTVOUCHERS);

        // Create the 3rd expand bar "Create new"
        PGroup group3 = createPGroup("command.new.name", Icon.ICON_PRODUCT_NEW);
//        parameters.put(CallEditor.PARAM_EDITOR_TYPE, ProEd.ID);
        addAction(group3, Icon.COMMAND_PRODUCT, "command.new.product", CommandIds.CMD_NEW_PRODUCT);
        
        parameters = new HashMap<>();
        parameters.put(CallEditor.PARAM_EDITOR_TYPE, ContactEditor.ID);
        addAction(group3, Icon.COMMAND_CONTACT, "command.new.contact", CommandIds.CMD_CALL_EDITOR /*CommandIds.CMD_NEW_CONTACT*/, parameters);

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
        parameters = new HashMap<>();
        parameters.put(OpenBrowserEditorHandler.PARAM_USE_PROJECT_URL, Boolean.TRUE.toString());
        addAction(group5, Icon.COMMAND_WWW, "command.browser", CommandIds.CMD_OPEN_BROWSER_EDITOR, parameters);
        addAction(group5, Icon.COMMAND_CALCULATOR, "command.calculator", CommandIds.CMD_OPEN_CALCULATOR);
        //	    addAction(group5, Icon.COMMAND_REORGANIZE, "ReorganizeDocumentsAction.116", CommandIds.CMD_REOGANIZE_DOCUMENTS);
    }

    private void addAction(PGroup group, Icon commandIcon, String commandIconDescriptor, final String commandId, final Map<String, Object> parameters) {
        final CLabel label = new CLabel(group, SWT.NORMAL);
        label.setImage(commandIcon.getImage(IconSize.DefaultIconSize));
        label.setToolTipText(msg.getMessageFromKey(commandIconDescriptor + ".tooltip"));
        label.setData(parameters);

        // formerly known as ACTIONTEXT
        label.setText(msg.getMessageFromKey(commandIconDescriptor + ".name"));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                ParameterizedCommand pCmd = commandService.createCommand(commandId, parameters);
                if (handlerService.canExecute(pCmd)) {
                    handlerService.executeHandler(pCmd);
                }
                else {
                    MessageDialog.openInformation(composite.getShell(), "Action Info", "not yet implemented :-(");
                }
            }
        });

    }

    private void addAction(PGroup group, Icon commandIcon, String commandIconDescriptor, final String commandId) {
        addAction(group, commandIcon, commandIconDescriptor, commandId, null);
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
                PGroup current = (PGroup) e.getSource();
                // Collapse expand bar items, or not
                if (preferences.getBoolean(Constants.PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR)) {
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
        if (composite != null)
            composite.setFocus();
    }

}
