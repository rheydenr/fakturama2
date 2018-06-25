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

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.startup.ConfigurationManager;

/**
 * Handler for changing / selecting the workspace and restarting the application.
 *
 */
public class SelectWorkspaceHandler {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private ILogger log;

    @Inject
    @Preference
    private IEclipsePreferences preferences;

	/**
	 * Opens a dialog to select the workspace
	 */
	@Execute
	public void selectWorkspace(Shell parent, IWorkbench workbench) {

		// Open a directory dialog 
		DirectoryDialog directoryDialog = new DirectoryDialog(parent);
		directoryDialog.setFilterPath(System.getProperty("user.home"));

		//T: Title of the dialog to select the working directory
		directoryDialog.setText(msg.startFirstSelectWorkdir);
		//T: Text of the dialog to select the working directory
		directoryDialog.setMessage(msg.startFirstSelectWorkdirVerbose);
		String selectedDirectory = directoryDialog.open();
		
		if (selectedDirectory != null) {

			// test if it is valid
			if (selectedDirectory.equals("/") || selectedDirectory.equals("\\"))
				selectedDirectory = "";
			if (!selectedDirectory.isEmpty()) {

				// If there is a connection to the database,
				// use the new working directory after a restart.
				
				// storing DB credentials
				try {
		    			// for default DB setting we use the workdir as DB store
					
    				if(preferences.get(PersistenceUnitProperties.JDBC_URL, "").startsWith("jdbc:hsqldb:file") 
    						|| preferences.get(PersistenceUnitProperties.JDBC_URL, "").endsWith("fakdbneu")) {
		    			    String jdbcUrl = String.format("jdbc:hsqldb:file:%s/Database/Database;shutdown=true", selectedDirectory);
		    			    preferences.put(PersistenceUnitProperties.JDBC_URL, jdbcUrl);
		    			    preferences.put(PersistenceUnitProperties.JDBC_USER, "sa");
		    			    preferences.put(PersistenceUnitProperties.JDBC_PASSWORD, "");
		    			    preferences.putBoolean("isreinit", true);
		    			}
		    		
						// Store the requested directory in a preference value and clear an old one (if it exists)
						preferences.put(ConfigurationManager.GENERAL_WORKSPACE_REQUEST, selectedDirectory);
						preferences.remove(Constants.GENERAL_WORKSPACE);
						preferences.flush();
						// restarting application
						MessageDialog.openInformation(parent, msg.dialogMessageboxTitleInfo, msg.startFirstRestartmessage);
						
					    workbench.restart();
				} catch (BackingStoreException e) {
					log.error(e);
				}
				
			}

		// Close the workbench if no workspace is set.
		if (selectedDirectory.isEmpty())
			workbench.close();
		}
	}
}
