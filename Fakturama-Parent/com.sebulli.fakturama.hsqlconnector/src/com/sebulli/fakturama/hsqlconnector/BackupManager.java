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

package com.sebulli.fakturama.hsqlconnector;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class BackupManager {

//    @Inject
    private LogService log;
    
//    @Inject
//    private IDateFormatterService dateFormatterService;

	public void createBackup(String workspacePath) {
		ServiceReference<LogService> loggerRefs = FrameworkUtil.getBundle(getClass()).getBundleContext().getServiceReference(LogService.class);
		
		log = FrameworkUtil.getBundle(getClass()).getBundleContext().getService(loggerRefs);
		log.log(LogService.LOG_WARNING, "TEST");
//		dateFormatterService = new DateFormatter();
		
		// Get the path to the workspace
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
				log.log(LogService.LOG_ERROR, "can't create backup directory", e1);
				return;
			}
		}
		
		// Filename of the zip file
		String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmSS"));
		dateString = dateString.replace(" ", "_");
		dateString = dateString.replace(":", "");

		Path backupPath = Paths.get(directory.toString(), "/Backup_" + dateString + ".zip");
		log.log(LogService.LOG_INFO, "create Database backup in " + backupPath.toString());

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
					log.log(LogService.LOG_ERROR, "Error during file backup:" + backupedFile, e);
				}
			}
			zip.close();
		}
		catch (IOException ex) {
			log.log(LogService.LOG_ERROR, "Error during backup", ex);
		}
	}
}
