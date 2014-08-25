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
package com.sebulli.fakturama.office;


/**
 * Get the home application in an extra thread.
 * So it can be cancelled if the NOA plugin couldn't find it
 * in 10 seconds.
 * 
 * @author Gerd Bartelt
 */
public class OfficeHomeApplication implements Runnable {

	private String home = null;
	
	/**
	 * Get the result of the search
	 * 
	 * @return the path of the OpenOffice installation
	 */
	public String getHome() {
		return home;
	}
	
	/**
	 * Search for the OpenOfficeOrg home folder
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
//		try {
//			
//			// Get the home folder
//			IApplicationAssistant applicationAssistant;
//			applicationAssistant = new ApplicationAssistant();
//			ILazyApplicationInfo appInfo = null;
//			appInfo = applicationAssistant.getLatestLocalApplication();
//			
//			// An OpenOffice installation was found
//			if (appInfo!=null) { 
//				home = appInfo.getHome();
//			}
//		}
//		catch (OfficeApplicationException e) {
//		}

	}
	
}

