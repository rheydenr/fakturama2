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

package com.sebulli.fakturama.dbservice.impl;


/**
 * Does some update jobs
 * 
 * @author Gerd Bartelt
 *
 */
@SuppressWarnings("restriction")
public class Updater {

	
	/**
	 * Default constructor
	 */
	public Updater() {
	}
	
	/**
	 * Check, if something should be updated
	 */
	public void checkVersion() {
//		// Get the actual version from the bundle
//		final Version plugInVersion = new Version(Platform.getBundle("com.sebulli.fakturama").getHeaders().get("Bundle-Version").toString());
//
//		// Get the last version from the data base
//		// if there is no version string, it must be version 1.4.1 or earlier
//		String dataBaseVersionString = Data.INSTANCE.getProperty("bundleversion","1.4.1");
//		
//
//		final Version dataBaseVersion = new Version(dataBaseVersionString);
//		
//		// The plugin version is newer
//		if (plugInVersion.compareTo(dataBaseVersion) >= 1) 
//		{
//			// Update the entry in the data base
//			Data.INSTANCE.setProperty("bundleversion", plugInVersion.toString());
//
//			IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//			final StatusLineManager slm = ((WorkbenchWindow)workbenchWindow).getStatusLineManager();
//
//			
//			new Thread(new Runnable() {
//				public void run() {
//					
//					// Load the country codes
//					CountryCodes.update(plugInVersion, dataBaseVersion, slm);
//
//					// Update the documents
//					FileOrganizer.update(plugInVersion ,dataBaseVersion, slm);
//					
//					Display.getDefault().syncExec(new Runnable() {
//					    public void run() {
//					    	//T: Clear the status bar
//					    	slm.setMessage(_("done !"));
//					    }
//					});
//
//				}
//			}).start();
//			
//		}
	}
	
}
