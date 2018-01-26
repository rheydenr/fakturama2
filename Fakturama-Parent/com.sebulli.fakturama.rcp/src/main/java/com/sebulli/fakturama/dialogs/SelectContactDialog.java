/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.dialogs;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.internal.events.EventBroker;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.AbstractSelectionDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;
import com.sebulli.fakturama.views.datatable.contacts.CreditorListTable;
import com.sebulli.fakturama.views.datatable.contacts.DebitorListTable;

/**
 * @param <T>
 *
 */
public class SelectContactDialog<T extends Contact> extends AbstractSelectionDialog<T> {
    protected String editor = "";
    
    @Inject
    @Translation
    protected Messages msg;
   
    @Inject
    private IEventBroker evtBroker;
    
    @Inject
    private EModelService modelService;

    protected String title = "";
    
    @Inject
    private IEclipseContext context;
    
    private Control top;

    private ContactListTable<T> contactListTable; 

    @Inject
    public SelectContactDialog(Shell shell, @Translation Messages msg) {
        super(shell);
        this.msg = msg;
        // Set the title
        setTitle(msg.dialogSelectaddressTitle);
    }
    
    @PostConstruct
    public void init(Shell shell) {
        this.top = (Control) context.get(Composite.class); 
    }
    
    /**
     * Create this part of the dialog are that is common in all the different
     * types of SelectDataSetDialogs
     * 
     * @param parent
     *            The parent composite
     */
    @Override
    protected Control createDialogArea(Composite parent) {

        // Create the top composite dialog area
        top = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo((Composite) top);

        context.set(Composite.class, (Composite) top);
        context.set(IEventBroker.class, evtBroker);
        MPart part = modelService.createModelElement(MPart.class);
        part.setContext(context);
        part.getProperties().put(Constants.PROPERTY_CONTACTS_CLICKHANDLER, Constants.COMMAND_SELECTITEM);
        context.set(MPart.class, part);
        // FIXME Workaround (quick & dirty), please use enums
        if(StringUtils.equals((String) context.get("CONTACT_TYPE"), "CREDITOR")) {
        	contactListTable = (ContactListTable<T>) ContextInjectionFactory.make(CreditorListTable.class, context);
        } else {
        	contactListTable = (ContactListTable<T>) ContextInjectionFactory.make(DebitorListTable.class, context);
        }

        GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
    	contactListTable.getSearchControl().getTextControl().setFocus();

        return top;
    }

    /**
     * If an entry is selected it will be put in an Event which will be posted by {@link EventBroker}.
     * After this the dialog closes. The Event is caught by the {@link DocumentEditor} which will use it as
     * billing or delivery address.
     */
    @Override
    protected void okPressed() {
        if (contactListTable.getSelectedObject() != null) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(DocumentEditor.DOCUMENT_ID, context.get(DocumentEditor.DOCUMENT_ID));
            eventParams.put(ContactListTable.SELECTED_CONTACT_ID, Long.valueOf(contactListTable.getSelectedObject().getId()));
            setResult(contactListTable.getSelectedObject());
            // inform the DocumentEditor (or any other Editor) about the selection
            evtBroker.post("DialogSelection/Contact", eventParams);
            // TODO Unterscheidung zw. Billing / Delivery! siehe Altcode
        }
        super.okPressed();
    }

        
    /**
     * Called if a user doubleclicks on an entry. Then the entry will be selected and the dialog closes. Since these are
     * two different actions it couldn't put into one method.
     *  
     * @param event
     */
    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleDialogDoubleClickClose(@UIEventTopic("DialogAction/CloseContact") Event event) {
        if (event != null) {
            if (contactListTable.getSelectedObject() != null) {
                // only for convenience, the result is already set by NatTable on double click and send to the 
                // DocumentEditor.
                setResult(contactListTable.getSelectedObject());
            }
            super.okPressed();
        }
    }

    /**
     * Set the initial size of the dialogs in pixel
     * 
     * @return Size as Point object
     */
    @Override
    protected Point getInitialSize() {
        return new Point(800, 550);
    }
}
