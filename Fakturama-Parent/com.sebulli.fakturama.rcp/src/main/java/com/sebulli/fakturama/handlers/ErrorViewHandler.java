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

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.osgi.service.log.LogEntry;

import com.sebulli.fakturama.views.ErrorView;

/**
 * This handler catches the UIEvents with error messages and shows them
 * in the {@link ErrorView}.
 *
 */
public class ErrorViewHandler {

    @Inject
    @Execute
    public void execute(final EPartService partService, 	
    		EModelService modelService,
    		final MApplication application,
    		@Optional @UIEventTopic("Log/Error") LogEntry errorLogEntry) {
        String declaringClass = "";
        String lineNumber = "";
        String methodName = "";
        String exceptionMessage = "";
        String newErrorString = "";
        
        // if no message is given, return
        
        /*
         * FIXME
         * At the moment the error view doesn't work because the finding of the error view is a bit buggy. 
         * Therefore we've taken out this piece.
         */
        if(true || errorLogEntry == null) {
        	return;
        }

        // Find the error view
        try {
            // at first we look for an existing Part
            MPartStack errorViewStack = (MPartStack) modelService.find("com.sebulli.fakturama.errorview.partstack", application);
            if(errorViewStack != null) {
                // prepare the error string
                if (errorLogEntry.getException() != null) {

                    // Get all elements of the stack trace and search for the first
                    // element, that starts with the plugin name.
                    for (StackTraceElement element : errorLogEntry.getException().getStackTrace()) {
                        // this.getClass().getPackage().getName()
                        if (element.getClassName().startsWith("com.sebulli.fakturama")) {
                            declaringClass = element.getClassName();
                            lineNumber = Integer.toString(element.getLineNumber());
                            methodName = element.getMethodName();
                            break;
                        }
                    }

                    // Generate the exception message.
					exceptionMessage = errorLogEntry.getMessage() + " : "
							+ ((Exception) errorLogEntry.getException()).getLocalizedMessage()
							+ " in: " + (declaringClass != null ? declaringClass + "/" : "" )
							+ methodName + "(" + lineNumber + ")";

                    // Generate the error string
                    newErrorString += exceptionMessage;
                }
                else {
                    // Generate the error string
                    newErrorString += errorLogEntry.getMessage();
                }
                
                MPart errorPart = partService.findPart(ErrorView.ID);
                if (!errorViewStack.isVisible()) {
                    // otherwise no content is rendered :-(
                	errorViewStack.setToBeRendered(true);
                	errorViewStack.setVisible(true);
                    partService.showPart(errorPart,
                            PartState.ACTIVATE);
                }
                if(errorPart.getObject() != null) {
                	((ErrorView)errorPart.getObject()).setErrorText(newErrorString);
                }
            }
        }
        catch (IllegalStateException ise) {
            // TODO Auto-generated catch block
            ise.printStackTrace();
        }
    }
}
