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
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * <p>Copies all snippet perspectives to perspective stack called "MainPerspectiveStack".
 * In order to register/reset perspective and not have to sync two copies in e4xmi.</p>
 * 
 * @see <a href="https://stackoverflow.com/questions/19717154/how-do-i-reset-perspective-for-eclipse-e4-rcp-application">StackOverflow</a>
 * 
 */
public class CopyPerspectiveSnippetProcessor {
    private static final String MAIN_PERSPECTIVE_STACK_ID = "com.sebulli.fakturama.perspectivestack";

    @Execute
    public void execute(EModelService modelService, MApplication application) {
        MPerspectiveStack perspectiveStack = (MPerspectiveStack) modelService.find(MAIN_PERSPECTIVE_STACK_ID, application);

        // Only do this when no other children, or the restored workspace state will be overwritten.
        if (perspectiveStack == null || !perspectiveStack.getChildren().isEmpty())
            return;

        // clone each snippet that is a perspective and add the cloned perspective into the main PerspectiveStack
        boolean isFirst = true;
        for (MUIElement snippet : application.getSnippets()) {
            if (snippet instanceof MPerspective) {
                MPerspective perspectiveClone = (MPerspective) modelService.cloneSnippet(application, snippet.getElementId(), null);
                perspectiveStack.getChildren().add(perspectiveClone);
                if (isFirst) {
                    perspectiveStack.setSelectedElement(perspectiveClone);
                    isFirst = false;
                }
            }
        }
    }
}