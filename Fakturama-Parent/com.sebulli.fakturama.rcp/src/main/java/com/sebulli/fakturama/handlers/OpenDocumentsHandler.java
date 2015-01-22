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
 
package com.sebulli.fakturama.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;

/**
 *
 */
public class OpenDocumentsHandler {
    @Inject
    private ILogger log;
    
    /**
     * Run the action
     * <br />
     * Open the Documents in an table view.
     */
    @Execute
    public void execute(final EPartService partService) {
        // see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=372211
        MPart documentPart = partService.findPart(DocumentsListTable.ID);
        if(documentPart != null && documentPart.isVisible()) {
            log.debug("part is already created!");
            partService.showPart(documentPart,
                    PartState.VISIBLE);
        } else {
            documentPart.setVisible(true);
            // otherwise no content is rendered :-(
            documentPart.setToBeRendered(true);
            partService.showPart(documentPart,
                    PartState.ACTIVATE);
        }
    }
    
    @CanExecute
    public boolean canExecute() {
        return true;
    }

}
