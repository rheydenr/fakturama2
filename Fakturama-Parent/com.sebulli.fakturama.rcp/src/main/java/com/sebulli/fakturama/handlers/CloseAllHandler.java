/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.parts.BrowserEditor;
import com.sebulli.fakturama.views.ErrorView;

/**
 *
 */
public class CloseAllHandler {
    
    /**
     * Close the part safely if it isn't dirty. If so, you have to ask for saving before closing.
     * 
     * @param dirtyable
     * @return
     */
    @CanExecute
    public boolean canExecute(
            MApplication application,
            final EModelService modelService) {
        MPartStack documentPartStack = (MPartStack) modelService.find(Constants.DETAILPANEL_ID, application);
        return !documentPartStack.getChildren().isEmpty(); // you can close a window if there are open detail views
    }

    @Execute
    public void execute(MApplication application, final EModelService modelService, final EPartService partService) throws InvocationTargetException,
            InterruptedException {
        
        // close error view
        try {
			MPart errorPart = (MPart) modelService.find(ErrorView.ID, application);
			if(errorPart != null) {
				partService.hidePart(errorPart, true);
			}
		} catch (IllegalStateException e) {
			// hide
		}        
      // at first find the PartStack "detailpanel"
        MPartStack documentPartStack = (MPartStack) modelService.find(Constants.DETAILPANEL_ID, application);
        List<MStackElement> stackElements = documentPartStack.getChildren().stream().filter(
        		elem -> !elem.getElementId().equals(BrowserEditor.ID)
        		      && elem.getTags().contains("documentWindow")).collect(Collectors.toList());
        // alternative: EPartService.REMOVE_ON_HIDE_TAG
        
        for (MStackElement stackElement : stackElements) {
        	MPart activePart = (MPart)stackElement;
            // ask before closing
            if (activePart.getContext() != null) {
                // the parts are removed when they are hidden
                if (activePart.isDirty() && partService.savePart(activePart, true)) {
                    partService.hidePart(activePart, true);
                } else {
                    partService.hidePart(activePart, true);
                }
            }
        }
        
//        // the error stack can also be closed
        // TODO doesn't work at the moment
//        MPartStack errorViewStack = (MPartStack) modelService.find("com.sebulli.fakturama.errorview.partstack", application);
//        if(errorViewStack != null) {
//        	errorViewStack.setVisible(false);
//        }
        
    }
}
