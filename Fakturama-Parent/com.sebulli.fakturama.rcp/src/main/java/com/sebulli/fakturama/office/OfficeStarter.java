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

package com.sebulli.fakturama.office;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OSDependent;

/**
 * Starts the OpenOffice Application from the application's path
 * 
 * @author Gerd Bartelt
 */
public class OfficeStarter {

    @Inject
    private IPreferenceStore preferences;

    @Inject
    @Translation
    protected Messages msg;

    private Shell shell;

    @PostConstruct
    public void init(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
        this.shell = shell;
    }

    /**
     * Returns, if the Application exists
     * 
     * @param preferencePath
     *            The path from the preference store
     * @return TRUE, if it exists.
     */
    public boolean isValidPath(String preferencePath) {
        Path file = OSDependent.getOOBinary(preferencePath);
        return file != null && Files.exists(file);
    }

    /**
     * Checks the OpenOffice (LibreOffice) path if it's valid. Displays an error message if it's not the case.
     *  
     * @return
     */
    public Path getCheckedOOPath() {
    	return getCheckedOOPath(false);
    }

    /**
     * Checks the OpenOffice (LibreOffice) path if it's valid. Displays an error message if it's not the case (except it's in silent mode).
     * @param silentMode if this parameter is set to <code>true</code> no message is displayed
     * @return 
     */
	public Path getCheckedOOPath(boolean silentMode) {
        Path ooPath = null;
        // Get the path to the application set in the preference store
        String preferencePath = preferences.getString(Constants.PREFERENCES_OPENOFFICE_PATH);

        // Show a message (and exit), if there is no OpenOffice found
        if (!isValidPath(preferencePath)) {
        	if(!silentMode) {
	            MessageDialog.openWarning(shell, msg.viewErrorlogName,
	            //T: Format: OpenOffice path ... is invalid.
	                    MessageFormat.format(msg.officePathInvalid, preferencePath));
        	} else {
        		System.err.println(MessageFormat.format(msg.officePathInvalid, preferencePath));
        	}
        } else {
        	ooPath = OSDependent.getOOBinary(preferencePath);
        }
        return ooPath;
	}
}
