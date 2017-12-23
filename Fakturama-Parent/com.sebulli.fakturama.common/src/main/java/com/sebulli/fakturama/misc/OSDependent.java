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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;

/**
 * These are the OS-dependent settings.
 * 
 * @author Gerd Bartelt
 */
public class OSDependent {

    // valid OS strings:  "win32", "motif", "gtk", "photon", "carbon", "cocoa", "wpf"
	private static final String platform = SWT.getPlatform();
 
    
	/**
	 * Test, if it is a Mac OSX
	 * 
	 * @return TRUE, if it one
	 */
	public static boolean isMacOSX() {
        return platform.equalsIgnoreCase("photon")
		       || platform.equalsIgnoreCase("carbon")
		       || platform.equalsIgnoreCase("cocoa");
	}

	/**
	 * Test, if it is a Linux system
	 * 
	 * @return TRUE, if it one
	 */
	public static boolean isLinux() {
		return platform.equalsIgnoreCase("motif")
		        || platform.equalsIgnoreCase("gtk");
	}

	/**
	 * Test, if it is a Windows System
	 * 
	 * @return TRUE, if it one
	 */
	public static boolean isWin() {
		return platform.startsWith("win");
	}

	/**
	 * Returns the OS dependent program folder
	 * 
	 * @return Program folder as string
	 */
	public static String getProgramFolder() {

		if (isMacOSX())
			return "/Applications/";

		if (isLinux())
			return "/usr/lib/";

		if (isWin())
			return "C:\\Program Files\\";

		return "";

	}

	/**
	 * Returns the OS dependent default path of the OpenOffice installation
	 * 
	 * @return Default path as string
	 */
	public static String getOODefaultPath() {

		if (isMacOSX())
			return getProgramFolder() + "LibreOffice.app";

		if (isLinux())
			return getProgramFolder() + "libreoffice";

		if (isWin())
			return getProgramFolder() + "LibreOffice 5";

		return "";

	}

	/**
	 * Returns the OpenOffice binary
	 * 
	 * @param path
	 *            of the OpenOffice folder
	 * @return Full Path of the the binary.
	 */
	public static Path getOOBinary(String path) {
	    Path retval = null;

		if (isMacOSX())
			retval = Paths.get(path, "/Contents/MacOS/soffice");

		if (isLinux())
		    retval = Paths.get(path, "/program/soffice");

		if (isWin())
		    retval = Paths.get(path, "program", "soffice.exe");

		return retval;
	}
//
//	/**
//	 * Test, if it is allowed to add an about menu to the menu bar. In some OS
//	 * the about menu is set to the menu bar by the OS. So, it is not necessary
//	 * to add it twice.
//	 * 
//	 * @return TRUE, if it is necessary
//	 */
//	public static boolean canAddAboutMenuItem() {
//		return !isMacOSX();
//	}

	/**
	 * Test, if it is allowed to add an preference menu to the menu bar. In some
	 * OS the about menu is set to the menu bar by the OS. So, it is not
	 * necessary to add it twice.
	 * 
	 * @return TRUE, if it is necessary
	 */
	public static boolean canAddPreferenceAboutMenu() {
		return !isMacOSX();
	}

	/**
	 * Test, if OpenOffice is in an app archive instead a program folder.
	 * 
	 * @return TRUE, if it an app
	 */
	public static boolean isOOApp() {
		return isMacOSX();
	}

	// getNewline is now System.lineSeparator()
}
