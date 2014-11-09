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

package com.sebulli.fakturama.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.misc.DataUtils;

/**
 * A log listener for all exceptions. The error messages are written into a
 * log file and displayed in an error view in the workbench.
 * 
 * @author Gerd Bartelt
 */
public class LogListener implements org.osgi.service.log.LogListener {

	// Maximum lines of the logfile
	private static final int MAXLINES = 2000;

	// The logile
	private File logFile;

	// The errortext of the errorview
	private String errorString = "";

	// Display or hide the errorview
	private boolean showerrorview = false;

	/**
	 * Constructor
	 */
	public LogListener() {
	}

	/**
	 * Shows the error view and sets the error text
	 */
	public void showErrorView() {

//		// Do it not, if showerrorview flag is not set
//		if (!showerrorview)
//			return;
//
//		// Find the error view
//		IWorkbenchWindow workbenchWindow = ApplicationWorkbenchWindowAdvisor.getActiveWorkbenchWindow(); 
//			
//		
//		if (workbenchWindow != null) {
//			IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
//			
//			if ( workbenchPage !=null ) {
//				try  {
//					workbenchPage.showView(ErrorView.ID);
//				}
//				catch (Exception e) {
//					return;
//				}
//				ErrorView view = (ErrorView) workbenchPage.findView(ErrorView.ID);
//
//				// Set the error text
//				view.setErrorText(errorString);
//			}
//		}

	}

	/**
	 * Return the name of the log file.
	 * 
	 * @return Name of the log file or an empty string, if workspace is not set
	 */
	private String getLogfileName() {
		// Get the directory of the workspace
		String filename = "mpf!";// Activator.getDefault().getPreferenceStore().getString("GENERAL_WORKSPACE");

		// Do not save log files, if there is no workspace set
		if (filename.isEmpty())
			return "";

		// Do not save log files, if workspace is not created
		File directory = new File(filename);
		if (!directory.exists())
			return "";

		// Create a sub folder "Log", if it does not exist yet.
		filename += "/Log/";
		directory = new File(filename);
		if (!directory.exists())
			directory.mkdirs();

		// Name of the log file
		filename += "Error.log";

		return filename;
	}

	/**
	 * Notifies this listener that given status has been logged by a plug-in
	 * 
	 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus,
	 *      java.lang.String)
	 */
	@Override
	public void logged(LogEntry log) {
		try {
			String declaringClass = "";
			String lineNumber = "";
			String methodName = "";
			String lineArray[] = new String[MAXLINES];
			String exceptionMessage = "";
			String newErrorString = "";

			// Add an "I:" or an "E:", depending if it is an information or
			// an error.
			if (log.getLevel() == LogService.LOG_INFO) {

				// Information.
				// Do not open the error view
				newErrorString += "I:";

			}
			else {

				// Error
				// Open the error view
				showerrorview = true;
				newErrorString += "E:";
			}

			if (log.getException() != null) {

				// Get all elements of the stack trace and search for the first
				// element, that starts with the plugin name.
				for (StackTraceElement element : log.getException().getStackTrace()) {
					if (element.getClassName().startsWith(log.getBundle().getSymbolicName())) {
						declaringClass = element.getClassName();
						lineNumber = Integer.toString(element.getLineNumber());
						methodName = element.getMethodName();
						break;
					}
				}

				// Generate the exception message.
				exceptionMessage = log.getMessage() + " : " + ((Exception) log.getException()).getLocalizedMessage() + " in: " + declaringClass + "/"
						+ methodName + "(" + lineNumber + ")" + "\n";

				// Generate the error string
				newErrorString += exceptionMessage;
			}
			else
				// Generate the error string
				newErrorString += log.getMessage() + "\n";

			errorString += newErrorString;
			System.out.print(newErrorString);

			// Show the error view (only if it is not just an information message)
			showErrorView();

			// Get the name of the log file
			String logFileName = getLogfileName();

			// Do not log, if no workspace is set.
			if (logFileName.isEmpty())
				return;

			// Create a File object
			logFile = new File(logFileName);

			int lines = 0;
			int lineIndex = 0;

			// If the log file exists read the content
			if (logFile.exists()) {

				// Open the existing file
				BufferedReader in = new BufferedReader(new FileReader(logFileName));
				String line = "";

				// Read the existing file and store it in a buffer
				// with a fix size. Only the newest lines are kept.
				while ((line = in.readLine()) != null) {
					lineArray[lineIndex] = line;
					lines++;
					lineIndex++;
					lineIndex = lineIndex % MAXLINES;
				}
			}

			// If the existing logile has more than the MAXINES,
			// delete it and create a new one.
			if (lines > MAXLINES) {
				logFile.delete();
				logFile = new File(logFileName);
			}

			// Create a new file
			logFile.createNewFile();
			BufferedWriter bos = new BufferedWriter(new FileWriter(logFile, true));

			// Write the data to the new file.
			if (lines > MAXLINES) {
				for (int i = 0; i < MAXLINES; i++) {
					bos.write(lineArray[lineIndex] + "\n");
					lineIndex++;
					lineIndex = lineIndex % MAXLINES;
				}
			}

			// Create a new string buffer and add the error message
			
			
			StringBuffer str = new StringBuffer();
			str.append(DataUtils.DateAndTimeOfNowAsLocalString());
			str.append(" ");
			str.append(log.getBundle().getSymbolicName());
			str.append(": ");
			str.append(log.getMessage());

			// Add the stack trace
			final Writer stackTrace = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stackTrace);
			if (log.getException() != null)
				log.getException().printStackTrace(printWriter);
			stackTrace.toString();
			str.append(stackTrace.toString());
			str.append("\n");

			// Write the stack trace to the log file
			bos.write(str.toString());
			bos.close();

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
