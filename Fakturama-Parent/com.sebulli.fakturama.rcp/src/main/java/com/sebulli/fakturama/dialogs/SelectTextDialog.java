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

package com.sebulli.fakturama.dialogs;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

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
import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.texts.TextListTable;

/**
 * Dialog to select a text from a table
 * 
 * @author Gerd Bartelt
 */
public class SelectTextDialog extends AbstractSelectionDialog<TextModule> {
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private IEventBroker evtBroker;
    
    @Inject
    private EModelService modelService;
    
    @Inject
    private IEclipseContext context;
    
    private AbstractViewDataTable<TextModule, TextCategory> textListTable;
    
    private Control top;

	/**
	 * Constructor
	 * 
	 * @param string
	 *            Dialog title
	 */
    @Inject
	public SelectTextDialog(Shell shell, @Translation Messages msg) {
        super(shell);
        this.msg = msg;
        // Set the title
        setTitle(msg.dialogSelecttextTitle);
	}


	/**
	 * Create the dialog area
	 * 
	 * @param parent
	 *            Parent composite
	 * @return The new created dialog area
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		// Create the dialog area
		top = super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo((Composite) top);

        context.set(Composite.class, (Composite) top);
        context.set(IEventBroker.class, evtBroker);
        MPart part = modelService.createModelElement(MPart.class);
        part.setContext(context);
        part.getProperties().put(Constants.PROPERTY_TEXTMODULES_CLICKHANDLER, Constants.COMMAND_SELECTITEM);
        context.set(MPart.class, part);
        textListTable = ContextInjectionFactory.make(TextListTable.class, context);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(top);

		return top;
	}
	
    /**
     * If an entry is selected it will be put in an Event which will be posted by {@link EventBroker}.
     * After this the dialog closes. The Event is caught by the {@link DocumentEditor} which will use it as
     * new article.
     */
    @Override
    protected void okPressed() {
    	/*
    	 * TODO check if this dialog could only opened once!
    	 * If you call this dialog more than once, a new instance is created. If the dialog is closed, the shell is disposed, 
    	 * but the event handler live further (as a kind of zombie event... boo!). Now, if one selects an entry by double click,
    	 * *all* event handlers (even if they are the same kind of event!) are called and cause the text to be inserted multiple times.
    	 * Therefore I've introduced a new condition "this.getShell() != null" because this checks if the (former) shell is dead.
    	 * If anyone out there has another idea to suppress these events he is welcome! 
    	 * 
    	 */
        if (this.getShell() != null && textListTable.getSelectedObject() != null) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(DocumentEditor.DOCUMENT_ID, context.get(DocumentEditor.DOCUMENT_ID));
            eventParams.put(TextListTable.SELECTED_TEXT_ID, Long.valueOf(textListTable.getSelectedObject().getId()));
            evtBroker.post("DialogSelection/TextModule", eventParams);
// alternative:            setResult(textListTable.getSelectedObject());
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
    protected void handleDialogDoubleClickClose(@UIEventTopic("DialogAction/CloseTextModule") Event event) {
		if (event != null) {
			okPressed();
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
