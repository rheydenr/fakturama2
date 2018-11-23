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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.LogEntry;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.views.ErrorView;

/**
 * This handler catches the UIEvents with error messages and shows them
 * in the {@link ErrorView}.
 *
 */
public class ErrorViewHandler {
    
    @Inject
    @Translation
    protected Messages msg;

    @Inject
    @Execute
    public void execute(EModelService modelService,
    		final MApplication application,
    		@Optional @UIEventTopic("Log/Error") LogEntry errorLogEntry) {
        String declaringClass = "";
        int lineNumber = -1;
        String methodName = "";
        String exceptionMessage = "";
        String newErrorString = "";
        
        // if no message is given, return
        if(errorLogEntry == null) {
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
                            lineNumber = element.getLineNumber();
                            methodName = element.getMethodName();
                            break;
                        }
                    }

                    // Generate the exception message.
					exceptionMessage = String.format("%s in: %s%s (%d)", errorLogEntry.getMessage(),
							(declaringClass != null ? declaringClass + "#" : ""), methodName, lineNumber);

                    // Generate the error string
                    newErrorString += exceptionMessage;
                }
                else {
                    // Generate the error string
                    newErrorString += errorLogEntry.getMessage();
                }
                
				errorViewStack.setToBeRendered(true);
				errorViewStack.setVisible(true);
                MPart errorPart = (MPart) modelService.find(ErrorView.ID, errorViewStack);
                if(errorPart == null) {
                	errorPart = modelService.createModelElement(MPart.class);
                	// otherwise no content is rendered :-(
                	errorPart.setElementId(ErrorView.ID);
                	errorPart.setLabel(msg.viewErrorlogName);
                	errorPart.setContributionURI(String.format("bundleclass://%s/%s", FrameworkUtil.getBundle(ErrorView.class).getSymbolicName(),
                			ErrorView.class.getName()));
                	errorViewStack.getChildren().add(errorPart);
                	/* The following line looks a bit crazy, but should work so far :-)
                	 * If we use the partService (from injected parameter above), we get an exception if the window
                	 * is not the main window (e.g., if you are in preferences dialog). The partService from that dialog
                	 * can't show the errorView in the main window. Therefore we have to use the partService from main application 
                	 * window, which is done in the following line. Ugly, isn't it ;-) ?
                	 */
                	application.getChildren().get(0).getContext().get(EPartService.class).showPart(ErrorView.ID, PartState.ACTIVATE);
                }
                if(errorPart.getObject() != null) {
                	((ErrorView)errorPart.getObject()).setErrorText(StringUtils.abbreviate(newErrorString.replaceAll("\\n", " "), 150));
                }
            }
        }
        catch (IllegalStateException ise) {
            // TODO Auto-generated catch block
            ise.printStackTrace();
        }
    }
    
	@Inject
	public void closeErrorView(EModelService modelService, final MApplication application,
			@Optional @UIEventTopic("Log/ErrorClose") String info) {
		if (info == null || info.contentEquals("nodata")) {
            // at first we look for an existing Part
            MPartStack errorViewStack = (MPartStack) modelService.find("com.sebulli.fakturama.errorview.partstack", application);
            if(errorViewStack != null) {
            	// since setting to invisible doesn't really hide the view we remove them from its parent stack
            	errorViewStack.setVisible(false);
            	errorViewStack.getChildren().clear();
            }
		}
    }
}
