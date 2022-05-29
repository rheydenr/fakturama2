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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;

import com.sebulli.fakturama.misc.Constants;

public class BackupManager {

    private Logger log;
    private IPreferenceStore preferenceStore;
    
	public BackupManager(IPreferenceStore preferenceStore) {
	    this.preferenceStore = preferenceStore;
    }

    public void createBackup(String workspacePath) {
		ServiceReference<LogService> loggerRefs = FrameworkUtil.getBundle(getClass()).getBundleContext().getServiceReference(LogService.class);
		
		LogService logService = FrameworkUtil.getBundle(getClass()).getBundleContext().getService(loggerRefs);
		log = logService.getLogger(getClass());
		
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
				log.error("can't create backup directory", e1);
				return;
			}
		}
		
		// Filename of the zip file
		String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmSS"));
		dateString = dateString.replace(" ", "_");
		dateString = dateString.replace(":", "");

		Path backupPath = Paths.get(directory.toString(), "/Backup_" + dateString + ".zip");
		log.info("create Database backup in " + backupPath.toString());

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
					log.error("Error during file backup:" + backupedFile, e);
				}
			}
			zip.close();
			
			handleOldBackups();
		}
		catch (IOException ex) {
			log.error("Error during backup", ex);
		}
	}

    /**
     * Remove old backups according to backup strategy set in preferences
     */
    private void handleOldBackups() {
        String strategy = preferenceStore.getString(Constants.PREFERENCES_BACKUP_STRATEGY);
        if(!strategy.isBlank()) {
        
            switch (strategy) {
            case Constants.PREFERENCES_GENERAL_KEEP_NUMBER_BACKUPS:
                int keepBackups = preferenceStore.getInt(Constants.PREFERENCES_GENERAL_KEEP_NUMBER_BACKUPS);
                if(keepBackups < 1)  {
                    return;   // do nothing, keep all backups
                }
                
                deleteBackupsUnto(keepBackups);
                break;
            case Constants.PREFERENCES_GENERAL_DELETEBACKUPS_OLDER_THAN:
                int olderThanDays = preferenceStore.getInt(Constants.PREFERENCES_GENERAL_DELETEBACKUPS_OLDER_THAN);
                if(olderThanDays < 1) {
                    return;   // do nothing, keep all backups
                }
                
                deletebackupsOlderThan(olderThanDays);
                break;
            default:
                break;
            }
        }
    }

    private void deletebackupsOlderThan(int olderThanDays) {
        Path backupPath = Paths.get(preferenceStore.getString(Constants.GENERAL_WORKSPACE), "Backup");
        final LocalDateTime deleteBeforeLocalDate = LocalDateTime.now().minusDays(olderThanDays);
        final Instant localDateInstant = deleteBeforeLocalDate.atZone(ZoneOffset.systemDefault()).toInstant();
        final FileTime fileTimeDeleteBefore = FileTime.from(localDateInstant);
        try {
            Files.walkFileTree(backupPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().startsWith("Backup_") 
                            && file.getFileName().toString().endsWith(".zip")
                            && Files.getLastModifiedTime(file).compareTo(fileTimeDeleteBefore) < 0) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Error deleting old backup file. " + e.getMessage());
        }
    }

    /**
     * Delete all backup files unto only <i>keepBackups</i> files remain.
     * 
     * @param keepBackups number of files to retain
     */
    private void deleteBackupsUnto(int keepBackups) {
        try {
            Path backupPath = Paths.get(preferenceStore.getString(Constants.GENERAL_WORKSPACE), "Backup");
            
            List<Path> allBackupFiles = Files.list(backupPath)
                    .filter(f -> f.getFileName().toString().startsWith("Backup_")
                    && f.getFileName().toString().endsWith(".zip"))
                    .sorted((p, q) -> {
                try {
                    FileTime time1 = Files.getLastModifiedTime(q);
                    FileTime time2 = Files.getLastModifiedTime(p);
                    return time1.compareTo(time2);
                } catch (IOException e) {
                    log.error("Error reading backup directory." + e.getMessage());
                }
                return 0;
                }).collect(Collectors.toList());

            // backup file names are in reverse order
            if(allBackupFiles.size() < keepBackups) {
                return;
            }
            
            allBackupFiles.subList(keepBackups, allBackupFiles.size()).forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException e) {
                    log.error("Error deleting old backup file. " + e.getMessage());
                }
            });
        } catch (IOException e) {
            log.error("Error reading old backup files. " + e.getMessage());
        }
    }
}
