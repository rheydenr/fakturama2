/**
 * 
 */
package com.sebulli.fakturama.startup;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.window.Window;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.migration.MigrationManager;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.resources.ITemplateResourceManager;

/**
 * Configures the application while startup. It checks for a new Workspace
 * or the need of database conversion. Furthermore, it checks if all the templates are
 * available and if the directory structure is ok. If needed, the templates are copied 
 * from resource bundle into the template directory. 
 * 
 * @author R. Heydenreich
 * 
 */
public class ConfigurationManager {

	public static final String GENERAL_WORKSPACE_REQUEST = "GENERAL_WORKSPACE_REQUEST";
	public static final String MIGRATE_OLD_DATA = "MIGRATE_OLD_DATA";
	
	private IApplicationContext appContext;
	protected Messages msg;

	@Inject
	protected IEclipseContext context;
	
	@Inject
	@Preference
	private IEclipsePreferences eclipsePrefs;
	
	private ITemplateResourceManager resourceManager;
	
	private Shell shell;

	private ILogger log;
	
	public ConfigurationManager() {
	    // constructor for injection framework
	}

	public ConfigurationManager(Shell shell, IEclipseContext eContext, IEclipsePreferences eclipsePrefs, ILogger log, Messages msg,
	        ITemplateResourceManager resourceManager) {
	    this.shell = shell;
	    this.appContext = eContext.get(IApplicationContext.class);
	    this.context = eContext;
	    this.eclipsePrefs = eclipsePrefs;
	    this.log = log;
	    this.msg = msg;
	    this.resourceManager = resourceManager;
    }

    /**
	 * Checks if the application was started the first time or if the workspace
	 * has changed.
	 * 
	 * @param cmdService {@link ECommandService} for creating commands
	 * @param handlerService {@link EHandlerService} for executing commands
	 * 
	 */
	public void checkAndUpdateConfiguration() {
		String requestedWorkspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
		boolean isRestart = false;
		// Get the program parameters

		String[] args = (String[]) appContext.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		OptionBuilder.withArgName("workspace");
		OptionBuilder.hasArg();
		OptionBuilder.withLongOpt("workspace");
		OptionBuilder.withDescription(msg.commandSelectworkspaceTooltip);
        Option selectWorkspaceOpt = OptionBuilder.create("w");

        // create Options object
        Options options = new Options();
        options.addOption(selectWorkspaceOpt);
        
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if(cmd.hasOption("w")) {
    			// Read the parameter "--workspace"
                //  --workspace d:\MeineDaten\Fakt1.6.1-EN
    			String workspaceFromParameters = cmd.getOptionValue('w');
    
    			// Checks, whether the workspace from the parameters exists
    			Path workspacePath = Paths.get(workspaceFromParameters);
    			if (Files.exists(workspacePath, LinkOption.NOFOLLOW_LINKS)) {
    				// Use it, if it is an existing folder.
    			    requestedWorkspace = workspaceFromParameters;
    				eclipsePrefs.put(GENERAL_WORKSPACE_REQUEST, requestedWorkspace);
                } else {
        			// if it not exists, ignore it quietly...
        			// ... or, better, at least we inform the user
        			log.warn("The requested workspace folder is invalid. Please select "
        			        + "a new one from the Fakturama menu (File - select workspace...)."
        			        + "The option is for now ignored.");
                }
            }
        } catch (ParseException e1) {
            log.error(e1, "Fehler beim Aufruf des Programms.");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Fakturama", options);
//            appContext.setResult(null, IApplication.EXIT_OK);
            //  ExitHandler!
            System.exit(-1);  // TODO error handling?!
        }
		
		try {
			if (eclipsePrefs.get(PersistenceUnitProperties.JDBC_DRIVER, null) == null) {
				// if no database is set then we launch the application for the first time
				log.info("Application was started the first time or no workspace was set!");
				selectWorkspace(requestedWorkspace, shell);
                isRestart = true;
			} else if (eclipsePrefs.get(GENERAL_WORKSPACE_REQUEST, null) != null || requestedWorkspace == null) {
				// Checks, whether the workspace request is set.
				// If yes, the workspace is set to this value and the request value is cleared.
				// This mechanism is used because the workspace can only be changed by restarting the application.
			    requestedWorkspace = eclipsePrefs.get(GENERAL_WORKSPACE_REQUEST, null);
				if (StringUtils.isNoneBlank(requestedWorkspace)) {
				    // switch the preference from a temporary one to the right one
					eclipsePrefs.remove(GENERAL_WORKSPACE_REQUEST);
					eclipsePrefs.put(Constants.GENERAL_WORKSPACE, requestedWorkspace);
					// now check if an old database has to be converted
					if (eclipsePrefs.get(MIGRATE_OLD_DATA, null) != null) {
						MigrationManager migMan = new MigrationManager(context, msg, eclipsePrefs);
//						appContext.applicationRunning();
						migMan.migrateOldData(shell);
	                    eclipsePrefs.remove(MIGRATE_OLD_DATA);
					}
				}
				// Checks, whether the workspace is set.
				// If not, the SelectWorkspaceAction is started to select it.
				if (StringUtils.isBlank(requestedWorkspace)) {
					selectWorkspace(requestedWorkspace, shell);
					isRestart = true;
				} else {
					// Checks whether the workspace exists
					// Exit if the workspace path is not valid
				    Path workspacePath = Paths.get(requestedWorkspace);
					if (!Files.exists(workspacePath, LinkOption.NOFOLLOW_LINKS)) {
						eclipsePrefs.put(Constants.GENERAL_WORKSPACE, "");
						selectWorkspace(requestedWorkspace, shell);
					}
				}
			}
			eclipsePrefs.flush();
		} catch (BackingStoreException e) {
			log.error(e);
		}
		if(!isRestart) {
    		// now initialize the new workspace
    		initWorkspace(eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null));
		}
	}

	/** 
	 * Brings up a selection dialog for the Workspace path.
	 * 
	 * @param requestedWorkspace
	 * @param shell
	 */
	private void selectWorkspace(String requestedWorkspace, Shell shell) {
		InitialStartupDialog startDialog = new InitialStartupDialog(shell, eclipsePrefs, log, msg, requestedWorkspace);
		if (startDialog.open() != Window.OK) {
			// close the application
			log.warn("Dialog was closed without setting any preferences. Exiting.");
			//	ExitHandler!
			System.exit(-1);
			// storing and restarting is initiated in InitialStartupDialog itself
		}
		appContext.applicationRunning();
	}

	/**
	 * Initialize the workspace. E.g., creates a new template folder if it not exists
	 * and fills it with all the templates.
	 * 
	 * @param workspace the workspace path to use
	 * 
	 */
	private void initWorkspace(String requestedWorkspace) {
		resourceManager.createWorkspaceTemplates(requestedWorkspace, context);
	}

}
