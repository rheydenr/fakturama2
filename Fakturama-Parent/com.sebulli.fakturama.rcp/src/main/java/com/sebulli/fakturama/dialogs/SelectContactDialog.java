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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.AbstractSelectionDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;
import com.sebulli.fakturama.views.datatable.contacts.DebitorListTable;

/**
 * @param <T>
 *
 */
public class SelectContactDialog extends AbstractSelectionDialog<Contact> {
    protected String editor = "";
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private ContactsDAO contactDAO;
   
    @Inject
    private IEventBroker evtBroker;
    
    @Inject
    private EModelService modelService;

    protected String title = "";
    
    @Inject
    private IEclipseContext context;
    
    private Control top;

    private DebitorListTable debitorListTable; 

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
        part.getProperties().put("fakturama.datatable.contacts.clickhandler", "com.sebulli.fakturama.command.selectitem");
        context.set(MPart.class, part);
        debitorListTable = ContextInjectionFactory.make(DebitorListTable.class, context);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
//
        return top;
    }
    
    @Override
    protected void okPressed() {
        super.okPressed();
        if(debitorListTable.getSelectedObject() != null) {
            System.out.println("HUHU");
//            if(dlg.open() == 0 && !dlg.getResult().isEmpty()) {
//                addressId = dlg.getResult().iterator().next();
//                document.setBillingContact(addressId);
//                // TODO Unterscheidung zw. Billing / Delivery! siehe Altcode
//            }
        }
    }
    
    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleDialogSelection(@UIEventTopic("DialogSelection/Contact") Event event) {
        if (event != null) {
            Long contactId = (Long) event.getProperty(ContactListTable.SELECTED_CONTACT_ID);
            Contact contact = contactDAO.findById(contactId);
            setResult(contact);
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
