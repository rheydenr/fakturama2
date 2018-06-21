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

package com.sebulli.fakturama.addons;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.IDateFormatterService;

public class BackupManager {

    @Inject
    private ILogger log;

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;
    
    @Inject
    private IDateFormatterService dateFormatterService;

	public void createBackup() {

		// Get the path to the workspace
		String workspacePath = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
		if (workspacePath == null || workspacePath.length() == 0)
			return;
		
		// no HSQL database available
		if(Files.notExists(Paths.get(workspacePath, "Database", "Database.script"))) {
			return;
		}

		Path directory = Paths.get(workspacePath, "Backup");

		// Create the backup folder, if it dosn't exist.
		if (Files.notExists(directory)) {
			try {
				Files.createDirectories(directory);
			} catch (IOException e1) {
				log.error(e1, "can't create backup directory");
				return;
			}
		}

		// Filename of the zip file
		String dateString = dateFormatterService.DateAndTimeOfNowAsLocalString();
		dateString = dateString.replace(" ", "_");
		dateString = dateString.replace(":", "");

		Path backupPath = Paths.get(directory.toString(), "/Backup_" + dateString + ".zip");

		// The file to add to the ZIP archive
		ArrayList<String> backupedFiles = new ArrayList<String>();
		backupedFiles.add("Database/Database.properties");
		backupedFiles.add("Database/Database.script");
		backupedFiles.add("Database/Database.lobs");
		backupedFiles.add("Database/Database.log");  // contains last activities

		FileInputStream in;
		byte[] data = new byte[1024];
		int read = 0;

		// Connect ZIP archive with stream
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(backupPath))) {

			// Set mode
			zip.setMethod(ZipOutputStream.DEFLATED);

			// Zip all files
			for (String backupedFile : backupedFiles) {

				try {
					Path inFile = Paths.get(workspacePath, backupedFile);
					if (Files.exists(inFile)) {
						in = new FileInputStream(inFile.toFile());

						if (in != null) {

							// Create a new entry
							ZipEntry entry = new ZipEntry(backupedFile);

							// Add a new entry to the archive
							zip.putNextEntry(entry);

							// Add the data
							while ((read = in.read(data, 0, 1024)) != -1)
								zip.write(data, 0, read);

							zip.closeEntry(); // Close the entry
							in.close();
						}
					}
				}
				catch (Exception e) {
					log.error(e, "Error during file backup:" + backupedFile);
				}
			}
			zip.close();
		}
		catch (IOException ex) {
			log.error(ex, "Error during backup");
		}
	}
}
