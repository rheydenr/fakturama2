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

package com.sebulli.fakturama.preferences;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.OSDependent;
import com.sebulli.fakturama.office.OfficeStarter;

/**
 * A field editor for a file path type preference. A standard file dialog
 * appears when the user presses the change button.
 * 
 * @author Gerd Bartelt
 */
public class AppFieldEditor extends StringButtonFieldEditor {

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private IEclipseContext context;

    private OfficeStarter ooStarter;
  
	/**
	 * Creates a file field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public void prepare(String name, String labelText, Composite parents) {
		init(name, labelText);
		setErrorMessage(JFaceResources.getString("FileFieldEditor.errorMessage"));
		setChangeButtonText(JFaceResources.getString("openBrowse"));
		setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
		createControl(parents);
	}
	
	@PostConstruct
	public void preInit() {
        ooStarter = ContextInjectionFactory.make(OfficeStarter.class, context);
	}

	/**
	 * Method declared on StringButtonFieldEditor. Opens the file chooser dialog
	 * and returns the selected file. Start with the OS dependent program
	 * directory
	 */
	@Override
	protected String changePressed() {
		String startingDir = "";

		// Start with the last URL
		if (!getTextControl().getText().isEmpty())
			startingDir = getTextControl().getText();

		// Remove everything after the last "/"
		if (!startingDir.isEmpty())
			if (startingDir.contains("/"))
				startingDir = startingDir.substring(0, 1 + startingDir.lastIndexOf("/"));

		// use the OS dependent program folder
		if (startingDir.isEmpty())
			startingDir = OSDependent.getProgramFolder();

		// Checks whether the selected folder exists
		File f = new File(startingDir);
		if (!f.exists())
			f = null;
		File d = getFile(f);
		if (d == null)
			return null;

		return d.getAbsolutePath();
	}

	/**
	 * Method declared on StringFieldEditor. Checks whether the text input field
	 * specifies an existing folder to an OpenOffice application or to an
	 * OpenOffice App on a Mac OS
	 */
	@Override
	protected boolean checkState() {

		String message = null;

		String path = getTextControl().getText();

		if (path != null)
			path = path.trim();
		else
			path = "";

		// Check whether it is a valid application
		if (path.length() != 0) {
			if (!ooStarter.isValidPath(path)) {
				if (OSDependent.isOOApp())
					//T: Error message if the selected file is not a valid OpenOffice app
				    message = msg.preferencesOfficeAppfieldNovalidapp;
				else
					//T: Error message if the selected folder is not a valid OpenOffice folder
				    message = msg.preferencesOfficeAppfieldNovalidfolder;
			}
		}

		// Display an error message
		if (message != null) {
			showErrorMessage(message);
			return false;
		}

		// OK!
		clearErrorMessage();
		return true;
	}

	/**
	 * Helper to open the file chooser dialog.
	 * 
	 * @param startingDirectory
	 *            the directory to open the dialog on.
	 * @return File The File the user selected or null if they do not.
	 */
	private File getFile(File startingDirectory) {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		if (startingDirectory != null)
			dialog.setFileName(startingDirectory.getPath());
		String file = dialog.open();
		if (file != null) {
			file = file.trim();
			if (file.length() > 0)
				return new File(file);
		}

		return null;
	}

}
