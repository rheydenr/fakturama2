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

package com.sebulli.fakturama.misc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.util.Util;

/**
 * These are the OS-dependent settings.
 * 
 * @author Gerd Bartelt
 */
public class OSDependent {

	/**
	 * Returns the OS dependent program folder
	 * 
	 * @return Program folder as string
	 */
	public static String getProgramFolder() {

		if (Util.isMac() || Util.isPhoton())
			return "/Applications/";

		if (Util.isLinux() || Util.isMotif())
			return "/usr/lib/";

		if (Util.isWindows())
			return "C:\\Program Files\\";

		return "";

	}

	/**
	 * Returns the OS dependent default path of the OpenOffice installation
	 * 
	 * @return Default path as string
	 */
	public static String getOODefaultPath() {

		if (Util.isMac() || Util.isPhoton())
			return getProgramFolder() + "LibreOffice.app";

		if (Util.isLinux() || Util.isMotif())
			return getProgramFolder() + "libreoffice";

		if (Util.isWindows())
			return getProgramFolder() + "LibreOffice 6";

		return "";

	}

	/**
	 * Returns the OpenOffice binary. Checks if the Application exists.
	 * 
	 * @param path
	 *            of the OpenOffice folder (from the preference store)
	 * @return Full Path of the the binary.
	 */
	public static Path getOOBinary(String path) {
	    Path retval = null;

		if (Util.isMac() || Util.isPhoton())
			retval = Paths.get(path, "/Contents/MacOS/soffice");

		if (Util.isLinux() || Util.isMotif())
		    retval = Paths.get(path, "/program/soffice");

		if (Util.isWindows()) {
	        // in case of linked files the file suffix may have changed
			String[] suffixes = new String[] {"exe", "bat", "com", "lnk"};
			for (String suffix : suffixes) {
				retval = Paths.get(path, "program", "soffice." + suffix);
				if(retval != null && Files.exists(retval)) {
					break;
				}
			}
		}
		
		return retval;
	}

	/**
	 * Test, if OpenOffice is in an app archive instead a program folder.
	 * 
	 * @return TRUE, if it an app
	 */
	public static boolean isOOApp() {
		return Util.isMac() || Util.isPhoton();
	}
}
