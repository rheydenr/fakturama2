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

package com.sebulli.fakturama.handlers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;

/**
 * This action marks an entry in the invoice table as unpaid or paid.
 * 
 * @author Gerd Bartelt
 */
public class MarkDocumentAsPaidHandler {

    @Inject
    @Translation
    private Messages msg;

    @Inject
    private ILogger log;

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;
    
    @Inject
    private ESelectionService selectionService;

    @Inject
    private DocumentsDAO documentsDAO;

    public static final String PARAM_STATUS = "com.sebulli.fakturama.command.document.markas.state";
    public static final String PARAM_INVOICEID = "com.sebulli.fakturama.command.document.markas.invoiceid";

    @CanExecute
    public boolean canExecute(@Active MPart activePart) {
        boolean retval = false;
        if (activePart.getElementId().contentEquals(DocumentsListTable.ID)) {
            @SuppressWarnings("rawtypes")
            AbstractViewDataTable currentListtable = (AbstractViewDataTable) activePart.getObject();
            Document[] selectedObjects = (Document[]) currentListtable.getSelectedObjects();
            retval = selectedObjects != null && Arrays.stream(selectedObjects).allMatch(doc -> doc.getBillingType().isINVOICE());
        }
        return retval;
    }

    /**
     * Instead of using a value for the states "unpaid" or "paid" a progress
     * value from 0 to 100 (percent) is used.
     * 
     * So it's possible to insert states between these.
     * 
     * @param text
     * @param progress
     */
    @Execute
    public void handleMarkDocument(@Optional @Named(PARAM_INVOICEID) String objId, 
            @Named(PARAM_STATUS) String status, 
            IEventBroker evtBroker) {

    	@SuppressWarnings("unchecked")
		List<Document> selectedObjects = (List<Document>)selectionService.getSelection();
        if (selectedObjects != null) {
            // TODO DO THIS IN DAO!!!

            for (Document document : selectedObjects) {
                // If we had a selection let's change the state
                DocumentType docType = DocumentType.findByKey(document.getBillingType().getValue());
                // Do it only if it is allowed to mark this kind of document as paid.
                if (docType.canBePaid()) {
                    // change the state
                    try {
                        document = documentsDAO.findById(document.getId(), true);  // otherwise the document isn't updated :-(
                        document.setPaid(StringUtils.equals(status, "PAID"));
                        document.setPayDate(Calendar.getInstance().getTime());
						document.setPaidValue(document.getTotalValue());
						
						// if this document is an invoice we have to update the dunnings, too.
                        if(document.getBillingType().isINVOICE()) {
                        	documentsDAO.updateDunnings(document);
                        }
                        // also in the database
                        documentsDAO.update(document);
                        
                        // Refresh the corresponding table view
                    } catch (FakturamaStoringException e) {
                        log.error(e);
                    }
                }
            }
            evtBroker.post(DocumentEditor.EDITOR_ID, "update");
        }
    }
}
