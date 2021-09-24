/**
 * 
 */
package com.sebulli.fakturama.startup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.window.Window;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
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
 */
public class ConfigurationManager {

	public static final String GENERAL_WORKSPACE_REQUEST = "GENERAL_WORKSPACE_REQUEST";
	public static final String MIGRATE_OLD_DATA = "MIGRATE_OLD_DATA";
	
	/**
	 * Status for the initialization
	 */
	private static final int STATUS_OK = 0;
    private static final int STATUS_RESTART = 1;
    private static final int STATUS_INIT_WORKSPACE = 2;
	
	@Inject
	private IApplicationContext appContext;

	@Inject
	protected IEclipseContext context;
	
    @Inject
    @Preference   //(value=InstanceScope.SCOPE)
    private IEclipsePreferences eclipsePrefs;
    
    @Inject
    @Translation
    protected Messages msg;
	
	@Inject
	private ITemplateResourceManager resourceManager;
	
	private Shell shell;

	@Inject
	private ILogger log;
	
	@PostConstruct
	public void init() {
        // at first create a (temporary) shell
        shell = new Shell(SWT.TOOL | SWT.NO_TRIM);
	}

    /**
	 * Checks if the application was started the first time or if the workspace
	 * has changed.
	 * 
	 */
	@SuppressWarnings("static-access")
	public void checkAndUpdateConfiguration() {
		String requestedWorkspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
		int restart = STATUS_OK;
		// Get the program parameters

		String[] args = (String[]) appContext.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		Option selectWorkspaceOpt = OptionBuilder.withArgName("workspace").hasArg().withLongOpt("workspace").withDescription(msg.commandSelectworkspaceTooltip).create("w");
		Option persistState = OptionBuilder.withArgName(IWorkbench.PERSIST_STATE).hasArg().withLongOpt(IWorkbench.PERSIST_STATE).create("r");
		Option showlocation = OptionBuilder.withArgName("showlocation").withLongOpt("showlocation").create("s");

        // create Options object
        Options options = new Options();
        options.addOption(selectWorkspaceOpt).addOption(persistState).addOption(showlocation);
        
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args, true);
            if(cmd.hasOption('w')) {
    			// Read the parameter "--workspace"
                // e.g. --workspace d:\MeineDaten\Fakt1.6.1-EN
    			String workspaceFromParameters = cmd.getOptionValue('w');
    
    			// Checks, whether the workspace from the parameters exists
    			Path workspacePath = Paths.get(workspaceFromParameters);
    			if (Files.exists(workspacePath, LinkOption.NOFOLLOW_LINKS)) {
    				// Use it, if it is an existing folder.
    			    requestedWorkspace = workspaceFromParameters;
    			    String oldWorkspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, "");
    				eclipsePrefs.put(Constants.GENERAL_WORKSPACE, requestedWorkspace);
    				eclipsePrefs.remove(GENERAL_WORKSPACE_REQUEST);
    				// clear any previously set database (if any)
    				// change the JDBC connection, if the database is an HSQL one and it's only a file
    				if(eclipsePrefs.get(PersistenceUnitProperties.JDBC_URL, "").startsWith("jdbc:hsqldb:file") 
    						|| eclipsePrefs.get(PersistenceUnitProperties.JDBC_URL, "").endsWith("fakdbneu")) {
    					String jdbcUrl = String.format("jdbc:hsqldb:file:%s/Database/Database;shutdown=true", requestedWorkspace);
    					eclipsePrefs.put(PersistenceUnitProperties.JDBC_URL, jdbcUrl);
    					eclipsePrefs.put(PersistenceUnitProperties.JDBC_USER, "sa");
    					eclipsePrefs.put(PersistenceUnitProperties.JDBC_PASSWORD, "");
    					// stop a running instance of HSQLDB, if any
    					eclipsePrefs.putBoolean("isreinit", !StringUtils.equalsIgnoreCase(oldWorkspace, requestedWorkspace));
    				}
                } else {
        			// if it not exists, ignore it quietly...
        			// ... or, better, at least we inform the user
        			log.warn("The requested workspace folder is invalid. Please select "
        			        + "a new one from the Fakturama menu (File - select workspace...). "
        			        + "The option is ignored for now.");
                }
            }
        } catch (ParseException e1) {
            log.error(e1, "Error launching program.");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(msg.applicationName, options);
            //  ExitHandler!
            System.exit(-1);  // TODO error handling?!
        }
		
		try {
			if (eclipsePrefs.get(PersistenceUnitProperties.JDBC_DRIVER, null) == null) {
				// if no database is set then we launch the application for the first time
				log.info("Application was started the first time or no workspace was set!");
				selectWorkspace(requestedWorkspace);
                restart = STATUS_RESTART;
			} else if (eclipsePrefs.get(GENERAL_WORKSPACE_REQUEST, null) != null || requestedWorkspace == null) {
				// Checks whether the workspace request is set.
				// If yes, the workspace is set to this value and the request value is cleared.
				// This mechanism is used because the workspace can only be changed by restarting the application.
			    requestedWorkspace = eclipsePrefs.get(GENERAL_WORKSPACE_REQUEST, null);
				if (StringUtils.isNotBlank(requestedWorkspace)) {
					// at first we have to copy the logfile template
					adaptLogfile(eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null), requestedWorkspace);
				    // switch the preference from a temporary one to the right one
					eclipsePrefs.remove(GENERAL_WORKSPACE_REQUEST);
					eclipsePrefs.put(Constants.GENERAL_WORKSPACE, requestedWorkspace);
					// now check if an old database has to be converted
					context.set(IEclipsePreferences.class, eclipsePrefs);
					if (eclipsePrefs.get(MIGRATE_OLD_DATA, null) != null) {
						
					    MigrationManager migMan = ContextInjectionFactory.make(MigrationManager.class, context);
						migMan.migrateOldData(shell);
	                    eclipsePrefs.remove(MIGRATE_OLD_DATA);
					}
				}
				// Checks, whether the workspace is set.
				// If not, the SelectWorkspaceAction is started to select it.
				if (StringUtils.isBlank(requestedWorkspace)) {
					selectWorkspace(requestedWorkspace);
					restart = STATUS_RESTART;
				} else {
					// Checks whether the workspace exists
					// If not, create one
				    Path workspacePath = Paths.get(requestedWorkspace);
				    Files.createDirectories(workspacePath);
//					if (!Files.exists(workspacePath, LinkOption.NOFOLLOW_LINKS)) {
//						eclipsePrefs.put(Constants.GENERAL_WORKSPACE, "");
//						selectWorkspace(requestedWorkspace, shell);
//					}
					restart = STATUS_INIT_WORKSPACE;
				}
			}
			eclipsePrefs.flush();
		} catch (BackingStoreException | IOException e) {
			log.error(e);
		}
//		if(restart != STATUS_OK) {
    		// now initialize the new workspace
    		initWorkspace(eclipsePrefs.get(Constants.GENERAL_WORKSPACE, eclipsePrefs.get(GENERAL_WORKSPACE_REQUEST, null)));
//		}
	}

	/**
	 * Changes the log configuration file to the new location.
	 * 
	 * @param oldWorkspace
	 * @param requestedWorkspace
	 * 
	 * ==> at the moment this method does intentionally nothing!
	 */
	private void adaptLogfile(String oldWorkspace, String requestedWorkspace) {
//		String defaultLogConfigFile = StringUtils.defaultIfBlank(System.getProperty(EquinoxLocations.PROP_USER_AREA), 
//				System.getProperty(EquinoxLocations.PROP_USER_AREA_DEFAULT)) + defaultLogConfigFile;
		// at first check if the configuration is set via a switch
		// -Dlogback.configurationFile=${resource_loc:/Fakturama-Parent/com.sebulli.fakturama.common/src/main/resources/logback.xml}
//		if(System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY) != null) {
//			System.err.println("Es wurde eine eigene Konfigurationsdatei für das Logging gesetzt. "
//					+ "Diese muß manuell auf das neue Arbeitsverzeichnis eingestellt werden.");
//		} else {
//			
//		}
		
	}

	/** 
	 * Brings up a selection dialog for the Workspace path.
	 * 
	 * @param requestedWorkspace
	 * @param shell
	 */
	private void selectWorkspace(String requestedWorkspace) {
	    // you can't use the ModelService because it isn't available at this moment :-(
		Shell dialogShell = new Shell(Display.getCurrent());
		InitialStartupDialog startDialog = new InitialStartupDialog(dialogShell, eclipsePrefs, log, msg, requestedWorkspace) {
            @Override
            protected Shell getParentShell() {
                // Bug 429308: Make workspace selection dialog visible
                // in the task manager of the OS
                return null;
            }
		};
		int result = startDialog.open();
		
		dialogShell.dispose();
		
		if (result != Window.OK || eclipsePrefs.get(ConfigurationManager.GENERAL_WORKSPACE_REQUEST, null) == null) {
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
