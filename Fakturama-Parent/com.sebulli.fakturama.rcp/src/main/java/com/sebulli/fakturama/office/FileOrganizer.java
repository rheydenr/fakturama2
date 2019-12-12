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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OSDependent;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 * Create file names from format templates and reorganize documents.
 *
 */
public class FileOrganizer {

	@Inject
	private IPreferenceStore preferences;

    @Inject
    protected IEclipseContext context;

	@Inject
	@Translation
	protected Messages msg;
	
	@Inject
	private ILogger log;

	@Inject
	private DocumentsDAO documentsDAO;
	
	enum PathOption {
		WITH_FILENAME,
		WITH_EXTENSION,
	}

	/**
	 * Replace all characters that are not allowed in the path
	 * 
	 * @param s
	 *            The String with special characters
	 * @return The clean string
	 */
	private String replaceIllegalCharacters(String s) {
		if(StringUtils.isNotBlank(s)) {
			s = s.replaceAll(" ", "_")
			     .replaceAll("\\\\", "_")
			     .replaceAll("\"", "_")
			     .replaceAll("/", "_")
			     .replaceAll("\\:", "_")
			     .replaceAll("\\*", "_")
			     .replaceAll("\\?", "_")
			     .replaceAll("\\>", "_")
			     .replaceAll("\\<", "_")
			     .replaceAll("\\|", "_")
			     .replaceAll("\\&", "_")
			     .replaceAll("\\n", "_")
			     .replaceAll("\\t", "_");
		}
		return s;
	}

	/**
	 * Generates the document file name from the document and the placeholder
	 * string in the preference page. Whether the path is absolute or relative depends on
	 * {@link TargetFormat} or preferences format settings.
	 * 
	 * @param pathOptions {@link PathOption}s to use
	 * @param targetFormat the {@link TargetFormat}
	 * @param document
	 *            The document
	 * @return The filename
	 */
	private String getRelativeDocumentPath(Set<PathOption> pathOptions, TargetFormat targetFormat, Document document) {
		String path, filename;
		ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);

		// T: Subdirectory of the OpenOffice documents
		String savePath = msg.pathsDocumentsName + "/";
		String fileNamePlaceholder = preferences.getString("OPENOFFICE_" + targetFormat.getPrefId() + "_PATH_FORMAT");

		// Replace all backslashes
		fileNamePlaceholder = fileNamePlaceholder.replace('\\', '/');

		String address = replaceIllegalCharacters(document.getAddressFirstLine());

		DocumentReceiver documentContact = document.getReceiver().stream().filter(r -> r.getBillingType() == null || r.getBillingType().isINVOICE() || r.getBillingType().isDELIVERY()).findFirst().get();
		String name = replaceIllegalCharacters(StringUtils.defaultString(documentContact.getName()));
		String companyOrName = replaceIllegalCharacters(contactUtil.getCompanyOrLastname(documentContact));
		String alias = replaceIllegalCharacters(StringUtils.defaultString(documentContact.getAlias()));

		// Replace the placeholders
		String customerRef = replaceIllegalCharacters(document.getCustomerRef());
		
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{docname\\}", replaceIllegalCharacters(document.getName()))
				.replaceAll("\\{docref\\}", StringUtils.defaultString(customerRef))
				.replaceAll("\\{doctype\\}", msg.getMessageFromKey(
						DocumentType.getPluralString(DocumentTypeUtil.findByBillingType(document.getBillingType()))))
				.replaceAll("\\{address\\}", StringUtils.defaultString(address))
				.replaceAll("\\{name\\}", name)
				.replaceAll("\\{firstname\\}", replaceIllegalCharacters(documentContact.getFirstName()))
				.replaceAll("\\{companyorname\\}", companyOrName)
				.replaceAll("\\{company\\}", replaceIllegalCharacters(StringUtils.defaultString(documentContact.getCompany())))
				.replaceAll("\\{alias\\}", alias)
				.replaceAll("\\{custno\\}", 
					StringUtils.defaultString(documentContact.getCustomerNumber()));

		// Find the placeholder for a decimal number with n digits
		// with the format "{Xnr}", "X" is the number of digits (which can be
		// empty).
		Pattern p = Pattern.compile("\\{(\\d*)nr\\}");
		Matcher m = p.matcher(fileNamePlaceholder);
		if (m.find()) { // found?
			String replacementString = "";
			String replaceNumberString = "%d"; // default
			if (m.groupCount() > 0) { // has some digits before <nr>?
				String numberString = m.group(1); // get the length for the resulting number
				if (StringUtils.isNumeric(numberString)) { // is this really a number?
					// build a format replacement string
					replaceNumberString = "%0" + numberString + "d";
				}
			}
			// find the current docNumber
			Pattern docNumberPattern = Pattern.compile("\\w+(\\d+)");
			Matcher docNumberMatcher = docNumberPattern.matcher(document.getName());
			if (docNumberMatcher.find() && docNumberMatcher.groupCount() > 0) {
				String docNumberString = docNumberMatcher.group(1);
				Integer docNumber = Integer.valueOf(docNumberString);
				replacementString = String.format(replaceNumberString, docNumber);
			}
			fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{\\d*nr\\}", replacementString);
		}

		LocalDateTime docDateTime = LocalDateTime.ofInstant(document.getDocumentDate().toInstant(), ZoneId.systemDefault());

		int yyyy = docDateTime.getYear();
		// Replace the date information
		fileNamePlaceholder = fileNamePlaceholder.replaceAll("\\{yyyy\\}", String.format("%04d", yyyy))
									.replaceAll("\\{yy\\}", String.format("%04d", yyyy).substring(2, 4))
									.replaceAll("\\{mm\\}",	String.format("%02d", docDateTime.getMonth().getValue()))
									.replaceAll("\\{dd\\}",	String.format("%02d", docDateTime.getDayOfMonth()));

		// Extract path and filename
		int pos = fileNamePlaceholder.lastIndexOf('/');

		if (pos < 0) {
			path = "";
			filename = fileNamePlaceholder;
		} else {
			path = fileNamePlaceholder.substring(0, pos);
			filename = fileNamePlaceholder.substring(pos + 1);
		}

		if(targetFormat.isAbsolutePath() || isAbsolutePath(fileNamePlaceholder)) {
			savePath = path + "/"; 
		} else {
			// if target path is relative we have to put the documents below Fakturama working dir
			savePath += path + "/";
		}

		// Use the document name as filename
		if (pathOptions.contains(PathOption.WITH_FILENAME))
			savePath += filename;

		// Use the document name as filename
		if (pathOptions.contains(PathOption.WITH_EXTENSION) && !fileNamePlaceholder.toLowerCase().endsWith(targetFormat.getExtension())) {
			savePath += targetFormat.getExtension();
		}

		return savePath;
	}

	/**
	 * Checks if the given String is an absolute filename
	 * 
	 * @param fileName
	 * @return
	 */
	private boolean isAbsolutePath(String fileNamePlaceholder) {
		boolean retval = false;
		if(OSDependent.isWin()) {
			retval = fileNamePlaceholder.matches("^\\w:.*");
		} else {
			// detect if the beginning of the given path is an existing one
			Path tmpPath = Paths.get(StringUtils.substringBefore(fileNamePlaceholder, "/"));
			retval = !tmpPath.toString().isEmpty() && Files.exists(tmpPath);
		}
		return retval;
	}

	/**
	 * Returns the filename (with path) of the Office document including the
	 * workspace path
	 * 
	 * @param pathOptions {@link PathOption}s to use
	 * @param targetFormat the {@link TargetFormat}
	 * @return the filename
	 */
	public Path getDocumentPath(Set<PathOption> pathOptions, TargetFormat targetFormat, Document document) {
		String workspace = preferences.getString(Constants.GENERAL_WORKSPACE);
		String documentPath = getRelativeDocumentPath(pathOptions, targetFormat, document);
		if(isAbsolutePath(documentPath)) {
			return Paths.get(documentPath);
		} else {
			return Paths.get(workspace, documentPath);
		}
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
	private boolean reorganizeDocument(String workspacePath, Document document, TargetFormat targetFormat, boolean copyFile) {
		boolean changed = false;
		String oldDocumentPath = targetFormat == TargetFormat.PDF ? document.getPdfPath() : document.getOdtPath();

		// ODT or PDF string
		// Get the old path from the document
		if (oldDocumentPath.isEmpty())
			return false;

		// Update the document entry "odtpath"
		Set<PathOption> pathOptions = Stream.of(PathOption.values()).collect(Collectors.toSet());
		Path newFile = getDocumentPath(pathOptions, targetFormat, document);
		
		// Move it if it exists
		Path oldFile = Paths.get(oldDocumentPath);

		if (Files.exists(oldFile) && !oldFile.toAbsolutePath().equals(newFile.toAbsolutePath())) {
			fileMove(oldDocumentPath, newFile.toString(), copyFile);
			if (targetFormat == TargetFormat.PDF) {
				document.setPdfPath(newFile.toAbsolutePath().toString());
			} else {
				document.setOdtPath(newFile.toAbsolutePath().toString());
			}
			changed = true;
		} else {
			log.warn(String.format("File '%s' couldn't be found or exists in target path. Source document number is '%s'.", oldFile, document.getName()));
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

		// Counts the documents and show the progress in the status bar
		int i = 0;

		// Get all documents
		List<Document> documents = documentsDAO.findAllPrintedDocuments();
		// Get the workspace path
		String workspacePath = preferences.getString(Constants.GENERAL_WORKSPACE);

		// Get all documents
		for (Document document : documents) {

			boolean changed = false;

			// Rename and move the ODT file.
			if (reorganizeDocument(workspacePath, document, TargetFormat.ODT, copyFile)) {
				changed = true;
			}

			// Rename and move the PDF file
			if (reorganizeDocument(workspacePath, document, TargetFormat.PDF, copyFile)) {
				changed = true;
			}

			// Update the document in the database
			if (changed) {
				try {
					documentsDAO.save(document);
                } catch (FakturamaStoringException e) {
                    log.error(e);
                }
			}
			
			// Show the progress in the status bar
			if (monitor != null) {
	
				// Count the documents
				monitor.setTaskName(String.format("%s... %4d", msg.commandReorganizeDocumentsUpdateMessage, i++));
				monitor.worked(1);
			}
		}
	}
}
