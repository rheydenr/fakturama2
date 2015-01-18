/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.views.datatable.payments.PaymentListTable;

/**
 *
 */
public class OpenPaymentsHandler {

    @Inject
    private ILogger log;

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private EPartService partService;

    /**
     * Run the action <br />
     * Open the Payments in an table view.
     */
    @Execute
    public void execute() {
        // see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=372211
        MPart listPart = partService.findPart(PaymentListTable.ID);
        if (listPart != null && listPart.isVisible()) {
            log.debug("part is already created!");
            partService.showPart(listPart, PartState.VISIBLE);
        }
        else {
            listPart.setVisible(true);
            // otherwise no content is rendered :-(
            listPart.setToBeRendered(true);
            partService.showPart(listPart, PartState.ACTIVATE);
        }
    }

    @CanExecute
    public boolean canExecute() {
        return true;
    }
}
