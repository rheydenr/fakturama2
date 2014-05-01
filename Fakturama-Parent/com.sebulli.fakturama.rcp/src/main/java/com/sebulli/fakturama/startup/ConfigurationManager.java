/**
 * 
 */
package com.sebulli.fakturama.startup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.internal.services.ResourceBundleHelper;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.window.Window;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Configures the application while startup. It checks for a new Workspace
 * or the database conversion.
 * 
 * @author R. Heydenreich
 * 
 */
public class ConfigurationManager {

	public static final String GENERAL_WORKSPACE_REQUEST = "GENERAL_WORKSPACE_REQUEST";
	public static final String GENERAL_WORKSPACE = "GENERAL_WORKSPACE";
	public static final String MIGRATE_OLD_DATA = "MIGRATE_OLD_DATA";

	@Inject
	private ECommandService cmdService;
	
	@Inject
	private EHandlerService handlerService;

	@Inject
	private IApplicationContext appContext;
	
	@Inject
	@Translation
	protected Messages _;

	@Inject
	protected IEclipseContext context;

	private IWorkbench workbench;

	/**
	 * The eclipse Logger. Level can be set via config.ini
	 * (eclipse.log.level=TRACE|DEBUG|INFO|WARN|ERROR)
	 */
	@Inject
	private Logger log;

	@Inject
	@Preference
	private IEclipsePreferences preferences;

	/**
	 * Checks if the application was started the first time or if the workspace
	 * has changed
	 * 
	 * @param context
	 * 
	 * @param bundleContext
	 * 
	 * @param shell
	 * @param appContext
	 * @param workbench
	 */
	@Execute
	public void checkFirstStart(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, IWorkbench workbench) {
		this.workbench = workbench;
		String requestedWorkspace = preferences.get(GENERAL_WORKSPACE, null);
		// Get the program parameters
		String[] args = (String[]) appContext.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (args.length != 0) {
			// Read the parameter "-workspace"
			String workspaceFromParameters = "";
			int i = 0;
			while (i < args.length) {
				if (args[i].equals("-workspace")) {
					i++;
					workspaceFromParameters = args[i];

					// Checks, whether the workspace from the parameters exists
					File workspacePath = new File(workspaceFromParameters);
					if (workspacePath.exists()) {
						// Use it, if it is an existing folder.
						requestedWorkspace = workspaceFromParameters;
					}
				}
				i++;
			}
		}
		try {
			if (preferences.get(PersistenceUnitProperties.JDBC_DRIVER, null) == null) {
				// if no database is set then we launch the application for the first time
				log.info("Application was started the first time!");
				selectWorkspace(requestedWorkspace, shell);
			} else if (preferences.get(GENERAL_WORKSPACE_REQUEST, null) != null) {
				// Checks, whether the workspace request is set.
				// If yes, the workspace is set to this value and the request value is cleared.
				// This mechanism is used, because the workspace can only be changed by restarting the application.
				requestedWorkspace = preferences.get(GENERAL_WORKSPACE_REQUEST, null);
				if (!requestedWorkspace.isEmpty()) {
					preferences.remove(GENERAL_WORKSPACE_REQUEST);
					preferences.put(GENERAL_WORKSPACE, requestedWorkspace);
					// now check if an old database has to be converted
					if (preferences.get(MIGRATE_OLD_DATA, null) != null) {
						appContext.applicationRunning();
						ParameterizedCommand command = cmdService.createCommand("com.sebulli.fakturama.migman.command", null);
						handlerService.executeHandler(command);  // launch MigrationManager.migrateOldData
					}

				}
				// Checks, whether the workspace is set.
				// If not, the SelectWorkspaceAction is started to select it.
				if (requestedWorkspace.isEmpty()) {
					selectWorkspace(requestedWorkspace, shell);
				}
				else {
					// Checks whether the workspace exists
					// Exit if the workspace path is not valid
					File workspacePath = new File(requestedWorkspace);
					if (!workspacePath.exists()) {
						preferences.put(GENERAL_WORKSPACE, "");
						selectWorkspace(requestedWorkspace, shell);
					}
				}
			}
			else {
				// close the static splash screen
				appContext.applicationRunning();
			}
			preferences.flush();
		}
		catch (BackingStoreException e) {
			log.error(e);
		}
		// now initialize the new workspace
		initWorkspace(requestedWorkspace);
		shell.setText("Fakturama - " + preferences.get(GENERAL_WORKSPACE, "(unknown)"));
	}

	/** 
	 * Brings up a selection dialog for the Workspace path.
	 * 
	 * @param requestedWorkspace
	 * @param shell
	 */
	private void selectWorkspace(String requestedWorkspace, Shell shell) {
		InitialStartupDialog startDialog = new InitialStartupDialog(shell, preferences, workbench, log, _, requestedWorkspace);
		appContext.applicationRunning();
		if (startDialog.open() != Window.OK) {
			// close the application
			log.warn("Dialog was closed without setting any preferences. Exiting.");
			//	ExitHandler!
			System.exit(-1);
			// storing and restarting is initiated in InitialStartupDialog itself
		}
	}

	/**
	 * Initialize the workspace. e.g. creates a new template folder if it not exists.
	 * 
	 * @param workspace
	 * 
	 */
	private void initWorkspace(String workspace) {
		String templateFolderName = _.configWorkspaceTemplatesName;
		//		// Exit, if the workspace path is not set
		//		if (workspace.isEmpty())
		//			return;

		// Exit, if the workspace path is not valid
		File workspacePath = new File(workspace);
		if (!workspacePath.exists())
			return;

		// Create and fill the template folder, if it does not exist.
		File directory = new File(workspace + "/" + templateFolderName);
		if (!directory.exists()) {

			// Copy the templates from the resources to the file system
			//			for (int i = 1; i <= DocumentType.MAXID; i++) {
			//				if (DocumentType.getType(i) == DocumentType.DELIVERY) {
			//					resourceCopy("Templates/Delivery/Document.ott", templateFolderName + "/" + DocumentType.getString(i), "Document.ott");
			//				}
			//				else {
			//					resourceCopy("Templates/Invoice/Document.ott", templateFolderName + "/" + DocumentType.getString(i), "Document.ott");
			//				}
			//			}
		}

		// Create the start page, if it does not exist.
		File startPage = new File(workspace + "/" + templateFolderName + "/Start" + "/" + "start.html");
		if (!startPage.exists()) {
			resourceCopy("Templates/Start/start.html", templateFolderName + "/Start", "start.html", workspace);
			resourceCopy("Templates/Start/logo.png", templateFolderName + "/Start", "logo.png", workspace);
		}

		// Copy the parcel service templates
		//		String parcelServiceTemplatePath = ParcelServiceManager.getRelativeTemplatePath();
		//		File parcelServiceFolder = new File(ParcelServiceManager.getTemplatePath());
		//		if (!parcelServiceFolder.exists()) {
		//			resourceCopy("Templates/ParcelService/DHL_de.txt", parcelServiceTemplatePath , "DHL_de.txt", workspace);
		//			resourceCopy("Templates/ParcelService/eFILIALE_de.txt", parcelServiceTemplatePath , "eFILIALE_de.txt", workspace);
		//			resourceCopy("Templates/ParcelService/myHermes_de.txt", parcelServiceTemplatePath , "myHermes_de.txt", workspace);
		//		}
		//		
		//		isInitialized = true;

	}

	/**
	 * Copies a resource file from the resource to the file system
	 * 
	 * @param resource
	 *            The resource file
	 * @param filePath
	 *            The destination on the file system
	 * @param fileName
	 *            The destination file name
	 * @param workspace
	 */
	private void resourceCopy(String resource, String filePath, String fileName, String workspace) {
		String myFilePath = filePath;
		// Remove the last "/"
		if (myFilePath.endsWith("/"))
			myFilePath = myFilePath.substring(0, myFilePath.length() - 1);

		// Relative path
		myFilePath = workspace + "/" + myFilePath;

		// Create the destination folder
		File directory = new File(myFilePath);
		if (!directory.exists())
			directory.mkdirs();

		// Copy the file
		InputStream in = null;
		try {
			// Create the input stream from the resource file                 "Templates/Invoice/Document.ott"
			//			InputStream in = Activator.getContext().getBundle("com.sebulli.fakturama.resources").getResource(resource).openStream();
			in = ResourceBundleHelper.getBundleForName("com.sebulli.fakturama.resources").getResource(resource).openStream();

			// Create the output stream from the output file name
			File fout = new File(myFilePath + "/" + fileName);
			FileUtils.copyInputStreamToFile(in, fout);

		}
		catch (FileNotFoundException e) {
			log.error(e, "Resource file not found");
		}
		catch (IOException e) {
			log.error(e, "Error copying the resource file to the file system.");
		}
		finally {
			// Close stream
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					log.error(e);
				}
			}
		}

	}

}
