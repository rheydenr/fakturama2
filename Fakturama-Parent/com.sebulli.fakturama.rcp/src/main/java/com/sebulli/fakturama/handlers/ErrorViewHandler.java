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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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
    public void execute(final EPartService partService, @Optional @UIEventTopic("Log/Error") LogEntry errorLogEntry) {
        String declaringClass = "";
        String lineNumber = "";
        String methodName = "";
        String exceptionMessage = "";
        String newErrorString = "";

        // Find the error view
        // in the very beginning this throws an IllegalStateException
        // we've to catch it and silently ignore it...
        try {
            MPart errorPart = partService.findPart(ErrorView.ID);
            if(errorPart != null) {
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
                    exceptionMessage = errorLogEntry.getMessage() + " : " + ((Exception) errorLogEntry.getException()).getLocalizedMessage() + " in: "
                            + declaringClass + "/" + methodName + "(" + lineNumber + ")";

                    // Generate the error string
                    newErrorString += exceptionMessage;
                }
                else {
                    // Generate the error string
                    newErrorString += errorLogEntry.getMessage();
                }
                
                ((ErrorView)errorPart.getObject()).setErrorText(newErrorString);
                if (errorPart.isVisible()) {
                    partService.showPart(errorPart,
                            PartState.VISIBLE);
                }
                else {
                    errorPart.setVisible(true);
                    // otherwise no content is rendered :-(
                    errorPart.setToBeRendered(true);
                    partService.showPart(errorPart,
                            PartState.ACTIVATE);
                }
            }
        }
        catch (IllegalStateException ise) {
            // TODO Auto-generated catch block
//            ise.printStackTrace();
        }
    }
}
