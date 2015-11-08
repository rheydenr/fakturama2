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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.util.DocumentTypeUtil;

public class FileOrganizer {

	@Inject
	private IPreferenceStore preferences;

	@Inject
	@Translation
	protected Messages msg;

	@Inject
	protected DocumentsDAO documentsDAO;

	final public static boolean WITH_FILENAME = true;
	final public static boolean NO_FILENAME = false;
	final public static boolean WITH_EXTENSION = true;
	final public static boolean NO_EXTENSION = false;
	final public static boolean PDF = true;
	final public static boolean ODT = false;

	// Counts the documents and show the progress in the status bar
	static private int i;

	/**
	 * Replace all characters, that are not allowed in the path
	 * 
	 * @param s
	 *            The String with special characters
	 * @return The clean string
	 */
	public static String replaceIllegalCharacters(String s) {
		s = s.replaceAll(" ", "_");
		s = s.replaceAll("\\\\", "_");
		s = s.replaceAll("\"", "_");
		s = s.replaceAll("/", "_");
		s = s.replaceAll("\\:", "_");
		s = s.replaceAll("\\*", "_");
		s = s.replaceAll("\\?", "_");
		s = s.replaceAll("\\>", "_");
		s = s.replaceAll("\\<", "_");
		s = s.replaceAll("\\|", "_");
		s = s.replaceAll("\\&", "_");
		s = s.replaceAll("\\n", "_");
		s = s.replaceAll("\\t", "_");
		return s;
	}

	/**
	 * Generates the document file name from the document and the placeholder
	 * string in the preference page
	 * 
	 * @param inclFilename
	 *            <code>true</code> if the filename should also be returned
	 * @param inclExtension
	 *            <code>true</code> if the extension should also be returned
	 * @param isPDF
	 *            <code>true</code> if it is a PDF File
	 * @param document
	 *            The document
	 * @return The filename
	 */
	public String getRelativeDocumentPath(boolean inclFilename, boolean inclExtension, boolean isPDF,
			Document document) {

		String path = "";
		String filename = "";

		String odtpdf = isPDF ? "pdf" : "odt";

		// T: Subdirectory of the OpenOffice documents
		String savePath = msg.pathsDocumentsName + "/";
		String fileNamePlaceholder;
		fileNamePlaceholder = preferences.getString("OPENOFFICE_" + odtpdf.toUpperCase() + "_PATH_FORMAT");

		// Replace all backslashes
		fileNamePlaceholder = fileNamePlaceholder.replace('\\', '/');

		// Remove the extension
		if (fileNamePlaceholder.toLowerCase().endsWith("." + odtpdf)
				|| fileNamePlaceholder.toLowerCase().endsWith(".pdf"))
			fileNamePlaceholder = fileNamePlaceholder.substring(0, fileNamePlaceholder.length() - 4);

		// Replace the placeholders
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{docname\\}", document.getName());
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{doctype\\}", msg.getMessageFromKey(
				DocumentType.getPluralString(DocumentTypeUtil.findByBillingType(document.getBillingType()))));

		String address = document.getAddressFirstLine();
		address = replaceIllegalCharacters(address);
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{address\\}", address);

		String name = document.getBillingContact().getName();
		name = replaceIllegalCharacters(name);
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{name\\}", name);

		// Find the placeholder for a decimal number with n digits
		// with the format "{Xnr}", "X" is the number of digits (which can be
		// empty).
		Pattern p = Pattern.compile("\\{(\\d*)nr\\}");
		Matcher m = p.matcher(fileNamePlaceholder);
		if (m.find()) { // found?
			String replacementString = "";
			String replaceNumberString = "%d"; // default
			if (m.groupCount() > 0) { // has some digits before <nr>?
				String numberString = m.group(1); // get the length for the
													// resulting number
				if (numberString.matches("\\d+")) { // is this really a number?
					// build a format replacement string
					replaceNumberString = "%0" + numberString + "d";
				}
			}
			// find the current docNumber
			Pattern docNumberPattern = Pattern.compile("\\w+(\\d+)");
			Matcher docNumberMatcher = docNumberPattern.matcher(document.getName());
			if (docNumberMatcher.find()) {
				if (docNumberMatcher.groupCount() > 0) {
					String docNumberString = docNumberMatcher.group(1);
					Integer docNumber = Integer.valueOf(docNumberString);
					replacementString = String.format(replaceNumberString, docNumber);
				}
			}
			fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{\\d*nr\\}", replacementString);
		}

		Instant calendar = document.getDocumentDate().toInstant();
		LocalDateTime docDateTime = LocalDateTime.ofInstant(calendar, ZoneId.of("Z"));

		int yyyy = docDateTime.getYear();
		// Replace the date information
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{yyyy\\}", String.format("%04d", yyyy));
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{yy\\}", String.format("%04d", yyyy).substring(2, 4));
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{mm\\}",
				String.format("%02d", docDateTime.getMonth().getValue()));
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{dd\\}",
				String.format("%02d", docDateTime.getDayOfMonth()));

		// Extract path and filename
		int pos = fileNamePlaceholder.lastIndexOf('/');

		if (pos < 0) {
			path = "";
			filename = fileNamePlaceholder;
		} else {
			path = fileNamePlaceholder.substring(0, pos);
			filename = fileNamePlaceholder.substring(pos + 1);
		}

		savePath += path + "/";

		// Use the document name as filename
		if (inclFilename)
			savePath += filename;

		// Use the document name as filename
		if (inclExtension) {
			savePath += "." + odtpdf;
		}

		return savePath;

	}

	/**
	 * Returns the filename (with path) of the Office document including the
	 * workspace path
	 * 
	 * @param inclFilename
	 *            <code>true</code> if also the filename should be used
	 * @param inclExtension
	 *            <code>true</code> if also the extension should be used
	 * @param PDF
	 *            <code>true</code> if it's the PDF filename
	 * @return The filename
	 */
	public Path getDocumentPath(boolean inclFilename, boolean inclExtension, boolean isPDF, Document document) {
		String workspace = preferences.getString(Constants.GENERAL_WORKSPACE);
		return Paths.get(workspace, getRelativeDocumentPath(inclFilename, inclExtension, isPDF, document));
	}

	/**
	 * Move a file and create the directories, if they do not exist
	 * 
	 * @param source
	 *            Source file name
	 * @param destination
	 *            Destination file name
	 * @param copyFile copy files instead of moving them
	 */
	private void fileMove(String source, String destination, boolean copyFile) {

		// Replace backslashed
		destination = destination.replace('\\', '/');

		// Extract the path
		String path;

		int pos = destination.lastIndexOf('/');

		if (pos < 0) {
			path = "";
		} else {
			path = destination.substring(0, pos);
		}

		try {
			// Create the directories
			Path folder = Paths.get(path);
			if (Files.notExists(folder))
				Files.createDirectories(folder);

			// Move it, if possible
			Path temp = Paths.get(destination);
			Path sourceFile = Paths.get(source);

			if (Files.notExists(temp) && Files.exists(sourceFile)) {
				if(copyFile) {
					Files.copy(sourceFile, temp);
				} else {
					Files.move(sourceFile, temp);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Reorganize a document's odt and pdf file
	 * 
	 * @param workspacePath
	 *            The workspace path
	 * @param document
	 *            The document
	 * @param isPDF
	 *            PDF or ODT
	 * @param copyFile copy files instead of moving them
	 * @return True, if it was successful
	 */
	private boolean reorganizeDocument(String workspacePath, Document document, boolean isPDF, boolean copyFile) {
		String oldDocumentPath;

		boolean changed = false;
		// ODT or PDF string
		// Get the old path from the document
		oldDocumentPath = isPDF ? document.getPdfPath() : document.getOdtPath();

		if (oldDocumentPath.isEmpty())
			return false;

		// Update the document entry "odtpath"
		String filename = getRelativeDocumentPath(WITH_FILENAME, WITH_EXTENSION, isPDF, document);
		Path newFile = Paths.get(workspacePath, filename);
		
		// Move it if it exists
		Path oldFile = Paths.get(oldDocumentPath);

		if (Files.exists(oldFile) && !oldFile.toAbsolutePath().equals(newFile.toAbsolutePath())) {
			fileMove(oldDocumentPath, workspacePath + filename, copyFile);
			if (isPDF) {
				document.setPdfPath(newFile.toAbsolutePath().toString());
			} else {
				document.setOdtPath(newFile.toAbsolutePath().toString());
			}
			changed = true;
		}

		return changed;

	}

	/**
	 * Reorganize all documents
	 * 
	 * @param monitor
	 *            ProgressBar to display the success
	 * @param copyFile copy files instead of moving them
	 */
	public void reorganizeDocuments(final IProgressMonitor monitor, boolean copyFile) {

		// Get all documents
		List<Document> documents = documentsDAO.findAll();
		// Get the workspace path
		String workspacePath = preferences.getString(Constants.GENERAL_WORKSPACE);

		i = 0;
		// Get all documents
		for (Document document : documents) {

			boolean changed = false;

			// Rename and move the ODT file.
			if (reorganizeDocument(workspacePath, document, ODT, copyFile)) {
				changed = true;
			}

			// Rename and move the PDF file
			if (reorganizeDocument(workspacePath, document, PDF, copyFile)) {
				changed = true;
			}

			// Update the document in the database
			if (changed) {
				try {
					documentsDAO.update(document);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// Count the documents
			i++;
			
			// Show the progress in the status bar
			if (monitor != null) {
				// T: Message in the status bar
				monitor.setTaskName(String.format("%s... %4d", msg.commandReorganizeDocumentsUpdateMessage, i));
				monitor.worked(1);
			}
		}
	}

}
