package com.sebulli.fakturama.startup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.prefs.BackingStoreException;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

public class InitialStartupDialog extends TitleAreaDialog {

//	private static final String DEFAULT_JDBC_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DEFAULT_JDBC_CLASS = "org.hsqldb.jdbc.JDBCDriver";
    private Text txtWorkdir, txtOldWorkdir, txtJdbcUrl, txtUser, txtPassword;
	private ComboViewer comboDriver;

	/** 
	 * Workspace path
	 */
	private String workspace = "";
	
	/*
	 * These fields can't be injected since this class is NOT constructed ApplicationModel.
	 * Therefore there's no EclipseContext from which these fields could be determined. 
	 */
	private ILogger log;
	private Messages msg;

	/** 
	 * The plugin's preference store
	 */
	private IEclipsePreferences preferences;
	
	private static final Map<String, String> jdbcUrlMap = new HashMap<String, String>();
	public static final int EMPTY_WORKSPACE = 100;

	private List<ServiceReference<DataSourceFactory>> connectionProviders = new ArrayList<>();
    private int jdbcClassComboIndex = 0;
    private final DirectoryChecker dirChecker;
    private Composite dbSettings;
    private Button btnUseDefaultDb;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param preferences
	 * @param log
	 * @param messages 
	 * @param requestedWorkspace 
	 */
	public InitialStartupDialog(Shell parent,
	        IEclipsePreferences preferences, ILogger log, Messages messages, String requestedWorkspace) {
		super(parent);
		this.dirChecker = new DirectoryChecker(parent);
		this.log = log;
		this.preferences = preferences;
		this.workspace = requestedWorkspace;
		this.msg = messages;
		parent.setText(msg.startFirstSelectWorkdir);
		BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		Collection<ServiceReference<DataSourceFactory>> serviceReferences;
        String oldJdbcDriverClass = preferences.get(PersistenceUnitProperties.JDBC_DRIVER, "org.hsqldb.jdbc.JDBCDriver");
		try {
			// get all available Datasources (which are registered in OSGi context)
			// and store them in a hash for using in ComboBox
			serviceReferences = bundleContext.getServiceReferences(DataSourceFactory.class, null);
//			serviceReferences.stream()
			int i = 0;
			for (ServiceReference<DataSourceFactory> serviceReference : serviceReferences) {
				connectionProviders.add(serviceReference);
				String driverClass = (String) serviceReference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
				if(StringUtils.equalsIgnoreCase((String)driverClass, oldJdbcDriverClass)) {
				    jdbcClassComboIndex = i;  // remember HSQL service
				}
				
				// initialize some JDBC URLs
				switch (driverClass) {
				case "org.hsqldb.jdbc.JDBCDriver":
		            // HSQL (File) => this is the original setting from Fakturama 1.x
		            // "jdbc:hsqldb:file:/path/to/database;shutdown=true
		            jdbcUrlMap.put(driverClass, "jdbc:hsqldb:file:/path/to/database;shutdown=true");
					break;
				case "org.apache.derby.jdbc.ClientDriver":
				case "org.apache.derby.jdbc.EmbeddedDriver":
		            // Derby
		            // jdbc:derby://localhost:1527/<databasename>;user=<username>;password=<password>
		            // jdbc:derby://localhost:1527/c:/my-db-dir/my-db-name;user=<username>;password=<password>
		            jdbcUrlMap.put(driverClass, "jdbc:derby://localhost:1527/<databasename>");
					break;
				case "com.mysql.jdbc.Driver":
		            // MySQL
		            // jdbc:mysql://[<host>][:<port>]/<database>[?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...
		            jdbcUrlMap.put(driverClass, "jdbc:mysql://<host>[:<port>]/<database>");
		            break;
				default:
					log.warn(String.format("unknown database driver found in service registry; class name=[%s]",driverClass));
					break;
				}
				log.info(String.format("adding [%s (%s), %s] as DB Connection Provider", 
						serviceReference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME),
						serviceReference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION),
						StringUtils.substringAfterLast((String) driverClass, ".")));
				i++;
			}
		}
		catch (InvalidSyntaxException e) {
			log.error(e);
		}
	}
	
	/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(msg.startFirstTitle);
			newShell.setImage(Icon.COMMAND_APP.getImage(IconSize.DefaultIconSize));
		}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
	    container.setLayout(new GridLayout(3, false));
	    setTitleImage(Icon.APP_ABOUT_ICON.getImage(IconSize.AppIconSize));
	 	setTitle(msg.startFirstTitle);
	 	
	    // 1st row
		setMessage(msg.startFirstSelectWorkdirVerbose, IMessageProvider.INFORMATION);
		
		// 2nd row
		Label lblWorkDir = new Label(container, SWT.NONE);
		lblWorkDir.setText(msg.startFirstSelectWorkdirShort);
		txtWorkdir = new Text(container, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		layoutData.minimumWidth = 450;
		txtWorkdir.setText(StringUtils.defaultIfEmpty(workspace, ""));
		txtWorkdir.setLayoutData(layoutData);
//		txtWorkdir.addFocusListener(new FocusAdapter() {
//			@Override
//			public void focusLost(FocusEvent e) {
//				getButton(OK).setEnabled(StringUtils.isNotBlank(txtWorkdir.getText()));
//			}
//		});

		final Button btnDirChooser = new Button(container, SWT.NONE);
		btnDirChooser.setText("...");
		btnDirChooser.setToolTipText(msg.startFirstSelectWorkdirVerbose);
		btnDirChooser.addSelectionListener(new DirectoryChooser(txtWorkdir, false));
		
		// 2.1st row
		Label lblOldWorkDir = new Label(container, SWT.NONE);
		lblOldWorkDir.setText(msg.startFirstSelectOldworkdirShort);
		txtOldWorkdir = new Text(container, SWT.BORDER);
		txtOldWorkdir.setLayoutData(layoutData);
		txtOldWorkdir.setText(getOldWorkDir());
		txtOldWorkdir.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if(e != null) {
                    String directory = ((Text)e.getSource()).getText();
                    dirChecker.checkPreviousVersion(directory);
                }
            }
            
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.FocusAdapter#focusGained(org.eclipse.swt.events.FocusEvent)
             */
            @Override
            public void focusGained(FocusEvent e) {
            	txtOldWorkdir.setSelection(0, txtOldWorkdir.getText().length());
            }
        });
		
		final Button btnOldDirChooser = new Button(container, SWT.NONE);
		btnOldDirChooser.setText("...");
		btnOldDirChooser.setToolTipText(msg.startFirstSelectOldworkdirVerbose);
		btnOldDirChooser.addSelectionListener(new DirectoryChooser(txtOldWorkdir, true));
		
        btnUseDefaultDb = new Button(container, SWT.CHECK);
        btnUseDefaultDb.setText(msg.startFirstSelectDbUsedefault);
        btnUseDefaultDb.setSelection(true);
        btnUseDefaultDb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        btnUseDefaultDb.setToolTipText(msg.startFirstSelectDbUsedefaultTooltip);
        btnUseDefaultDb.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
                dbSettings.setVisible(!((Button)e.getSource()).getSelection());
        }));
		
		dbSettings = new Composite(container, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(3).applyTo(dbSettings);
        dbSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        dbSettings.setVisible(false);  // hide initially
		// 3rd row
		Label lblDatabase = new Label(dbSettings, SWT.NONE);
		lblDatabase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDatabase.setText(msg.startFirstSelectDbCredentialsName);
		
		comboDriver = new ComboViewer(dbSettings, SWT.NONE | SWT.READ_ONLY);
		comboDriver.setContentProvider(ArrayContentProvider.getInstance());
		comboDriver.setInput(connectionProviders);
		comboDriver.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				String driverName = (String) ((ServiceReference<DataSourceFactory>)element).getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
				String jdbcVersion = (String) ((ServiceReference<DataSourceFactory>)element).getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION);
				String scope = StringUtils.substringAfterLast((String) ((ServiceReference<DataSourceFactory>)element).getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS), ".");
				if(jdbcVersion != null) {
					driverName = String.format("%s (%s), %s", driverName, jdbcVersion, scope);
				}
				return driverName;
			}
		});
		comboDriver.addSelectionChangedListener(new ISelectionChangedListener() {
			
            @SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				String driverClass = (String) ((ServiceReference<DataSourceFactory>)((IStructuredSelection) event
			      .getSelection()).getFirstElement()).getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
				txtJdbcUrl.setText(StringUtils.defaultString(jdbcUrlMap.get(driverClass), ""));
			}
		});
		Combo combo = comboDriver.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		combo.select(jdbcClassComboIndex);

		// 4th row
		Label lblJdbcurl = new Label(dbSettings, SWT.NONE);
		lblJdbcurl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblJdbcurl.setText(msg.startFirstSelectDbCredentialsJdbc);
		
		txtJdbcUrl = new Text(dbSettings, SWT.BORDER);
		// if an old value is set, we use it, else use the first entry from combo box
		@SuppressWarnings("unchecked")
		String firstEntry = (String) ((ServiceReference<DataSourceFactory>)comboDriver.getElementAt(jdbcClassComboIndex)).getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
		txtJdbcUrl.setText(preferences.get(PersistenceUnitProperties.JDBC_URL, firstEntry));
		txtJdbcUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// 5th row
		Label lblUser = new Label(dbSettings, SWT.NONE);
		lblUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUser.setText(msg.startFirstSelectDbCredentialsUser);
		txtUser = new Text(dbSettings, SWT.BORDER);
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		txtUser.setText(preferences.get(PersistenceUnitProperties.JDBC_USER, ""));

		Label lblPassword = new Label(dbSettings, SWT.NONE);
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassword.setText(msg.startFirstSelectDbCredentialsPassword);

		txtPassword = new Text(dbSettings, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		txtPassword.setText(preferences.get(PersistenceUnitProperties.JDBC_PASSWORD, ""));

	    return container;
	}
//	
//	@Override
//	protected Control createContents(Composite parent) {
//		Control retval = super.createContents(parent);
//		getButton(OK).setEnabled(false); // initially the ok button is disabled
//											// (because we haven't set a new
//											// workdir)
//		return retval;
//	}

	/**
	 * Try to find the old workdir path.
	 * 
     * @return
     */
    private String getOldWorkDir() {
        // TODO
        // old: ${user}/.fakturama
        // new: ${user}/.fakturama2
        // or eclipsePrefs.get(Constants.GENERAL_WORKSPACE, "")
        Path userDir = Paths.get(System.getProperty("user.home"), ".fakturama",
                ".metadata", ".plugins", "org.eclipse.core.runtime", ".settings", "com.sebulli.fakturama.prefs");
        String retval = "";
        if(Files.exists(userDir)) {
            Properties oldProps = new Properties();
            try {
                oldProps.load(Files.newInputStream(userDir));
                retval = oldProps.getProperty(Constants.GENERAL_WORKSPACE);
            }
            catch (IOException e) {
                // ok, something went wrong, therefore we leave it blank
                log.warn("couldn't get the old Fakturama properties from " + userDir.getFileName());
            }
        }
        return Optional.ofNullable(retval).orElse("");
    }

    @SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) comboDriver.getSelection();
		ServiceReference<DataSourceFactory> firstElement = (ServiceReference<DataSourceFactory>) selection.getFirstElement();
		String driver = selection.isEmpty() ? DEFAULT_JDBC_CLASS : (String) firstElement.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);

//        String oldJdbcDriverClass = preferences.get(PersistenceUnitProperties.JDBC_DRIVER, "org.hsqldb.jdbc.JDBCDriver");
		
		// storing DB credentials
		try {
			workspace = txtWorkdir.getText();
			
			// handle workdir and JDBC connection
			if (workspace.isEmpty()) {
				MessageDialog.openError(getParentShell(), msg.dialogMessageboxTitleError, msg.startFirstSelectWorkdirNoselection);
				txtWorkdir.setFocus();
			} else {
				if(Files.notExists(Paths.get(workspace))) {
					Files.createDirectories(Paths.get(workspace));
				}
				
    			preferences.put(PersistenceUnitProperties.JDBC_DRIVER, driver);
    			
    			// for default DB setting we use the workdir as DB store
    			if(btnUseDefaultDb.getSelection()) {//;hsqldb.lob_compressed=true
    			    String jdbcUrl = String.format("jdbc:hsqldb:file:%s/Database/Database;shutdown=true", workspace);
    			    preferences.put(PersistenceUnitProperties.JDBC_URL, jdbcUrl);
    			    preferences.put(PersistenceUnitProperties.JDBC_USER, "sa");
    			    preferences.put(PersistenceUnitProperties.JDBC_PASSWORD, "");
    			} else {
    			    preferences.put(PersistenceUnitProperties.JDBC_URL, txtJdbcUrl.getText());
    			    preferences.put(PersistenceUnitProperties.JDBC_USER, txtUser.getText());
    			    preferences.put(PersistenceUnitProperties.JDBC_PASSWORD, txtPassword.getText());   // TODO encrypt!!!
    			}
    			preferences.putBoolean("jdbc_reconnect", true);
    		
    			dirChecker.checkPreviousVersion(txtOldWorkdir.getText());
				// Store the requested directory in a preference value and clear an old one (if it exists)
				preferences.put(ConfigurationManager.GENERAL_WORKSPACE_REQUEST, workspace);
				preferences.remove(Constants.GENERAL_WORKSPACE);
				preferences.flush();
				// restarting application
				MessageDialog.openInformation(getParentShell(), msg.dialogMessageboxTitleInfo, msg.startFirstRestartmessage);
				super.okPressed();
			}
		} catch (BackingStoreException | IOException e) {
			log.error(e);
		}
	}
	
	/**
	 * Checks if a directory contains an older version of Fakturama.
	 * If so, the migration flag is set for further processing.
	 *
	 */
	private final class DirectoryChecker {
	    private Shell shell;
	    
	    // don't ask twice for these directories
	    private final Set<String> alreadyCheckedDirs = new HashSet<String>();

        /**
         * @param shell
         */
        protected DirectoryChecker(Shell shell) {
            this.shell = shell;
        }

        /**
         * Checks if a previous version is installed at this position
         * @param selectedDirectory 
         */
        private void checkPreviousVersion(String selectedDirectory) {
            if(StringUtils.isNotBlank(selectedDirectory)) {
                // The data base is in the /Database/ directory
                Path directory = Paths.get(selectedDirectory, "/Database/Database.script");
                if(!alreadyCheckedDirs.contains(selectedDirectory) && Files.exists(directory)) {
                    boolean answer = MessageDialog.openQuestion(shell, "Datenübernahme", 
                               msg.startMigrationWarning);
                    if(answer) {
                        preferences.put(ConfigurationManager.MIGRATE_OLD_DATA, selectedDirectory);
//                        txtOldWorkdir.setText(selectedDirectory);
                    }
                    alreadyCheckedDirs.add(selectedDirectory);
                }
            }
        }
	}
	
	/**
	 * Selection Adapter for choosing the working directory.
	 * 
	 *
	 */
	private final class DirectoryChooser extends SelectionAdapter {
		private final Text selectionField;
		private boolean shouldCheckPreviousVersion;
		private boolean forOldVersion;
		private DirectoryChecker dirChecker;

		private DirectoryChooser(Text selectionField, boolean shouldCheckPreviousVersion,
				boolean forOldVersion) {
			this.selectionField = selectionField;
			this.shouldCheckPreviousVersion = shouldCheckPreviousVersion;
			this.forOldVersion = forOldVersion;
			this.dirChecker = new DirectoryChecker(selectionField.getShell());
		}

		private DirectoryChooser(Text selectionField, boolean forOldVersion) {
			this(selectionField, true, forOldVersion);
		}
		
		private DirectoryChooser(Text selectionField) {
		    this(selectionField, true, false);
		}
		
		private void detectWorkspace(String selectedDirectory) {
			if (selectedDirectory != null) {
				// test if it is valid
				if (selectedDirectory.equals("/") || selectedDirectory.equals("\\")) {
					selectedDirectory = "";
				}
				selectionField.setText(selectedDirectory);
				if(shouldCheckPreviousVersion && forOldVersion) {
				    dirChecker.checkPreviousVersion(selectedDirectory);
				}
			}
		}

		public void widgetSelected(SelectionEvent event) {
			DirectoryDialog directoryDialog = new DirectoryDialog(selectionField.getShell(), SWT.OPEN);
			directoryDialog.setFilterPath(StringUtils.defaultIfBlank(selectionField.getText(), System.getProperty("user.home")));				
			//T: Title of the dialog to select the working directory
			directoryDialog.setText(forOldVersion ? msg.commandSelectoldworkspaceName : msg.commandSelectworkspaceName);
			//T: Text of the dialog to select the working directory
			directoryDialog.setMessage(forOldVersion ? msg.startFirstSelectOldworkdirVerbose : msg.startFirstSelectWorkdirVerbose);
			String selectedDirectory = directoryDialog.open();
			detectWorkspace(selectedDirectory);
		}
	}

}
