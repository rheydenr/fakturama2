package com.sebulli.fakturama.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;

/**
 * The LogbackAdaptor converts the LogEntry objects it receives into calls to
 * the slf4j loggers (the native interface of logback).
 * <p> The error messages are written into a
 * log file and displayed in an error view in the workbench.
 * 
 * <p>see <a href="https://code.google.com/p/osgi-logging/w/list">OSGi logging-related tools and documentation</a></p>
 * 
 * @author Rodrigo Reyes
 *
 */
public class LogbackAdapter implements LogListener {
	Map<Long, Logger> loggers = new HashMap<Long, Logger>();
    
	// Maximum lines of the logfile
	private static final int MAXLINES = 2000;
	
	private String workspacePath = "";

	// The logile
	private Path logFile;

	// The errortext of the errorview
	private String errorString = "";

	// Display or hide the errorview
	private boolean showerrorview = false;
//	
//	public void configure() {
//		// das suchen wir:
//		// eclipsePrefs;
//		EnvironmentInfo envInfo = Activator.getContext().getService(Activator.getContext().getServiceReference(EnvironmentInfo.class));
//		
//String[] commandLineArgs = envInfo.getCommandLineArgs();
//String s2 = "";
//for (int i = 0; i < commandLineArgs.length; i++) {
//	String s = commandLineArgs[i];
//	if(s.contentEquals("-data")) {
//		s2 = commandLineArgs[i+1];
//		break;
//	}
//}
//		
//		MDC.put(s2, Constants.GENERAL_WORKSPACE);
//	}	

	/**
	 * This methods is called by the LogReaderService, and dispatch them to a
	 * set of Loggers, created with
	 */
	public void logged(LogEntry log) {
		if (log.getBundle() == null || log.getBundle().getSymbolicName() == null) {
			// if there is no name, it's probably the framework emitting a log
			// This should not happen and we don't want to log something anonymous
			return;
		}
//		configure();
		
		// FIXME Workaround until we can get the correct workspace location for the Log file.
		logged2(log);

//		// Retrieve a Logger object, or create it if none exists.
//		Logger logger = loggers.get(log.getBundle().getBundleId());
//		if (logger == null) {
//			logger = LoggerFactory.getLogger(log.getBundle().getSymbolicName());
//			loggers.put(log.getBundle().getBundleId(), logger);
//		}		
//
//		// If there is an exception available, use it, otherwise just log 
//		// the message
//		if (log.getException() != null) {
//			switch (log.getLevel()) {
//			case LogService.LOG_DEBUG:
//				logger.debug(log.getMessage(), log.getException());
//				break;
//			case LogService.LOG_INFO:
//				logger.info(log.getMessage(), log.getException());
//				break;
//			case LogService.LOG_WARNING:
//				logger.warn(log.getMessage(), log.getException());
//				break;
//			case LogService.LOG_ERROR:
//				logger.error(log.getMessage(), log.getException());
//				break;
//			}
//		} else {
//			switch (log.getLevel()) {
//			case LogService.LOG_DEBUG:
//				logger.debug(Activator.bundleMarker, log.getMessage());
//				break;
//			case LogService.LOG_INFO:
//				logger.info(Activator.bundleMarker, log.getMessage());
//				break;
//			case LogService.LOG_WARNING:
//				logger.warn(Activator.bundleMarker, log.getMessage());
//				break;
//			case LogService.LOG_ERROR:
//				logger.error(Activator.bundleMarker, log.getMessage());
//				break;
//			}
//		}
	}

	/**
	 * Shows the error view and sets the error text
	 */
	private void showErrorView() {

		// Do it not, if showerrorview flag is not set
		if (!showerrorview)
			return;

		// Find the error view
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
		if (filename.isEmpty()) { return ""; }

		// Do not save log files, if workspace is not created
		Path directory = Paths.get(filename);
		if (Files.notExists(directory)) { return ""; }

		// Create a sub folder "Log", if it does not exist yet.
		try {
			Files.createDirectory(directory.resolve("/Log/"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// Name of the log file
		filename += "Error.log";

		return filename;
	}

	/**
	 * Notifies this listener that given status has been logged by a plug-in
	 * 
	 * @deprecated until we can get the real Workspace directory (then we use SLF4J)
	 */
	private void logged2(LogEntry log) {
		try {
			String declaringClass = "";
			String lineNumber = "";
			String methodName = "";
			String lineArray[] = new String[MAXLINES];
			StringBuilder exceptionMessage = new StringBuilder();
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
				exceptionMessage = new StringBuilder();
				exceptionMessage.append(log.getMessage());
				exceptionMessage.append(" : ");
				exceptionMessage.append(((Exception) log.getException()).getLocalizedMessage());
				exceptionMessage.append(" in: ");
				exceptionMessage.append(declaringClass);
				exceptionMessage.append("/");
				exceptionMessage.append(methodName);
				exceptionMessage.append("(");
				exceptionMessage.append(lineNumber);
				exceptionMessage.append(")");
				exceptionMessage.append("\n").toString();

				// Generate the error string
				newErrorString += exceptionMessage;
			}
			else
				// Generate the error string
				newErrorString += log.getMessage() + "\n";

			errorString += newErrorString;
			System.err.print(newErrorString);

			// Show the error view (only if it is not just an information message)
			showErrorView();

			// Get the name of the log file
			String logFileName = getLogfileName();

			// Do not log, if no workspace is set.
			if (logFileName.isEmpty())
				return;

			// Create a File object
			logFile = Paths.get(logFileName);

			int lines = 0;
			int lineIndex = 0;

			// If the log file exists read the content
			if (Files.exists(logFile)) {

				// Open the existing file
				BufferedReader in = Files.newBufferedReader(logFile);
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

			// If the existing logfile has more than the MAXINES,
			// delete it and create a new one.
			if (lines > MAXLINES) {
				Files.delete(logFile);
				logFile = Paths.get(logFileName);
			}

			// Create a new file
			BufferedWriter bos = Files.newBufferedWriter(logFile, StandardOpenOption.CREATE);
			
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
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the workspacePath
	 */
	public String getWorkspacePath() {
		return workspacePath;
	}

	/**
	 * @param workspacePath the workspacePath to set
	 */
	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}
}