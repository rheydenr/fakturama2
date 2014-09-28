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

package com.sebulli.fakturama.handlers;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

/**
 * This action opens the calculator in a view.
 * 
 * @author Gerd Bartelt
 */
public class OpenCalculatorHandler {
    @Inject
    private EPartService partService;

	/**
	 * Run the action
	 * 
	 * Open the calculators view.
	 */
    @Execute
    public void handleDelete(final MApplication application,
        final EModelService modelService) throws ExecutionException {
        MPartStack calcStack = (MPartStack) modelService.find("com.sebulli.fakturama.rcp.calculatorpanel", application);
        // try to open it
        MPart myPart = null;
        
        Collection<MPart> parts = partService.getParts();
        // at first we look for an existing Part
        for (MPart mPart : parts) {
            if (StringUtils.equalsIgnoreCase(mPart.getElementId(), "part:com.sebulli.fakturama.views.calculator") && mPart.getContext() != null) {
                myPart = mPart;
                break;
            }
        }
        calcStack.setToBeRendered(true);
        calcStack.setVisible(true);
        if(myPart != null) {
            partService.showPart(myPart, PartState.ACTIVATE);
        } else {
            myPart = partService.createPart("part:com.sebulli.fakturama.views.calculator");
            myPart.setToBeRendered(true);
            myPart.setContext(application.getContext());
        }
	}
}
