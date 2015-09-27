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
package com.sebulli.fakturama.resources;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * Resource manager for handling the different Fakturama templates. This is
 * used by the initial startup of the application, while the template directory
 * is checked and the templates are copied from template resource fragment.
 *
 */
public interface ITemplateResourceManager {

    /**
     * Creates the workspace directory structure and fills it up with all the needed templates.
     * 
     * @param workspace the workspace directory to be filled
     * @param templateFolderName the parent folder name for the templates (this
     * is a localized string)
     * 
     * @return <code>true</code> if all went ok<br />
     * <code>false</code> if an error occurred
     */
    public boolean createWorkspaceTemplates(String workspace, IEclipseContext ctx);

    public Image getProgramImage(Display display, ProgramImages imageName);
}