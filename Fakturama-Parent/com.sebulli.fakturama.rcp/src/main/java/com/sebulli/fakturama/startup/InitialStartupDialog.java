package com.sebulli.fakturama.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

public class InitialStartupDialog extends TitleAreaDialog {

	private Text txtWorkdir, txtOldWorkdir, txtJdbcUrl, txtUser, txtPassword;
	private ComboViewer comboDriver;

	// Workspace path
	private String workspace = "";
	
	/*
	 * These fields can't be injected since this class is NOT constructed ApplicationModel.
	 * Therefore there's no EclipseContext from which these fields could be determined. 
	 */
	private Logger log;
	protected Messages msg;
	private IWorkbench workbench;
	
	@Inject
	protected Shell parent;

	// The plugin's preference store
	IEclipsePreferences preferences;
	
    private static final Map<String, String> jdbcUrlMap = new HashMap<String, String>();;


	private List<ServiceReference<DataSourceFactory>> connectionProviders = new ArrayList<>();

	/**
	 * Create the dialog.
	 * @param parent
	 * @param preferences2 
	 * @param log
	 * @param messages 
	 * @param requestedWorkspace 
	 * @param style
	 */
	public InitialStartupDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent,
			IEclipsePreferences preferences, IWorkbench workbench, Logger log, Messages messages, String requestedWorkspace) {
		super(parent);
		this.workbench = workbench;
		this.log = log;
		this.preferences = preferences;
		this.workspace = requestedWorkspace;
		this.msg = messages;
		parent.setText(msg.startFirstSelectWorkdir);
		BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		Collection<ServiceReference<DataSourceFactory>> serviceReferences;
		try {
			// get all available Datasources (which are registered in OSGi context
			// and store them in a hash for using in ComboBox
			serviceReferences = bundleContext.getServiceReferences(DataSourceFactory.class, null);
			for (ServiceReference<DataSourceFactory> serviceReference : serviceReferences) {
//				DataSourceFactory s = (DataSourceFactory) bundleContext.getService(serviceReference);
				connectionProviders.add(serviceReference);
				log.info(String.format("adding [%s (%s)] as DB Connection Provider", 
						serviceReference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME),
						serviceReference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION)));
			}

			// initialize some JDBC URLs
            // Derby
            // jdbc:derby://localhost:1527/<databasename>;user=<username>;password=<password>
            // jdbc:derby://localhost:1527/c:/my-db-dir/my-db-name;user=<username>;password=<password>
            jdbcUrlMap.put("org.apache.derby.jdbc.ClientDriver", "jdbc:derby://localhost:1527/<databasename>");
            
            // MySQL
            // jdbc:mysql://[<host>][:<port>]/<database>[?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...
            jdbcUrlMap.put("com.mysql.jdbc.Driver", "jdbc:mysql://[<host>][:<port>]/<database>");

		}
		catch (InvalidSyntaxException e) {
			log.error(e);
		}
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
	    setTitleImage(Icon.ABOUT_ICON.getImage(IconSize.AppIconSize));
	 	setTitle("Fakturama Initialisierung");
	    // 1st row
		setMessage(msg.startFirstSelectWorkdirVerbose, IMessageProvider.INFORMATION);
		
		// 2nd row
		Label lblWorkDir = new Label(container, SWT.NONE);
		lblWorkDir.setText(msg.startFirstSelectWorkdirShort);
		txtWorkdir = new Text(container, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		layoutData.minimumWidth = 450;
		txtWorkdir.setLayoutData(layoutData);
		txtWorkdir.setText(workspace);
        final DirectoryChecker dirChecker = new DirectoryChecker(txtWorkdir.getShell());
		txtWorkdir.addFocusListener(new FocusAdapter() {
		    @Override
		    public void focusLost(FocusEvent e) {
                String directory = ((Text)e.getSource()).getText();
                dirChecker.checkPreviousVersion(directory);
		    }
        });
		
		final Button btnDirChooser = new Button(container, SWT.NONE);
		btnDirChooser.setText("...");
		btnDirChooser.setToolTipText(msg.startFirstSelectWorkdirVerbose);
		btnDirChooser.addSelectionListener(new DirectoryChooser(txtWorkdir, false));
		
		// 2.1st row
		Label lblOldWorkDir = new Label(container, SWT.NONE);
		lblOldWorkDir.setText(msg.startFirstSelectOldworkdirShort);
		txtOldWorkdir = new Text(container, SWT.BORDER);
		txtOldWorkdir.setLayoutData(layoutData);
		txtOldWorkdir.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if(e != null) {
                    String directory = ((Text)e.getSource()).getText();
                    dirChecker.checkPreviousVersion(directory);
                }
            }
        });
		
		final Button btnOldDirChooser = new Button(container, SWT.NONE);
		btnOldDirChooser.setText("...");
		btnOldDirChooser.setToolTipText(msg.startFirstSelectOldworkdirVerbose);
		btnOldDirChooser.addSelectionListener(new DirectoryChooser(txtOldWorkdir, true));
		
		// 3rd row
		Label lblDatabase = new Label(container, SWT.NONE);
		lblDatabase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDatabase.setText(msg.startFirstSelectDbCredentialsName);
		
		comboDriver = new ComboViewer(container, SWT.NONE);
		comboDriver.setContentProvider(ArrayContentProvider.getInstance());
		comboDriver.setInput(connectionProviders);
		comboDriver.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				String driverName = (String) ((ServiceReference<DataSourceFactory>)element).getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
				String jdbcVersion = (String) ((ServiceReference<DataSourceFactory>)element).getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION);
				if(jdbcVersion != null) {
					driverName = String.format("%s (%s)", driverName, jdbcVersion);
				}
				return driverName;
			}
		});
		comboDriver.addSelectionChangedListener(new ISelectionChangedListener() {
			
            @SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				String driverClass = (String) ((ServiceReference<DataSourceFactory>)((IStructuredSelection) event
			      .getSelection()).getFirstElement()).getProperty("osgi.jdbc.driver.class");
				
//				txtJdbcUrl.setText(driverClass);
				txtJdbcUrl.setText(jdbcUrlMap.get(driverClass));
			}
		});
		Combo combo = comboDriver.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		combo.select(0);

		// 4th row
		Label lblJdbcurl = new Label(container, SWT.NONE);
		lblJdbcurl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblJdbcurl.setText(msg.startFirstSelectDbCredentialsJdbc);
		
		txtJdbcUrl = new Text(container, SWT.BORDER);
		txtJdbcUrl.setText("jdbc:derby:memory:test;create=true");
		txtJdbcUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// 5th row
		Label lblUser = new Label(container, SWT.NONE);
		lblUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUser.setText(msg.startFirstSelectDbCredentialsUser);
		txtUser = new Text(container, SWT.BORDER);
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassword.setText(msg.startFirstSelectDbCredentialsPassword);

		txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

	    return container;

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) comboDriver.getSelection();
		ServiceReference<DataSourceFactory> firstElement = (ServiceReference<DataSourceFactory>) selection.getFirstElement();
		String driver = selection.isEmpty() ? "org.apache.derby.jdbc.EmbeddedDriver" :  (String) firstElement.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);

		// storing DB credentials
		try {
			preferences.put(PersistenceUnitProperties.JDBC_DRIVER, driver);
			preferences.put(PersistenceUnitProperties.JDBC_URL, txtJdbcUrl.getText());
			preferences.put(PersistenceUnitProperties.JDBC_USER, txtUser.getText());
			preferences.put(PersistenceUnitProperties.JDBC_PASSWORD, txtPassword.getText());   // TODO encrypt!!!
			preferences.putBoolean("jdbc_reconnect", true);
			preferences.flush();
		
			// handle workdir
			workspace = txtWorkdir.getText();
			if (!workspace.isEmpty()) {
				// Store the requested directory in a preference value
				preferences.put(ConfigurationManager.GENERAL_WORKSPACE_REQUEST, workspace);
				preferences.flush();
				// Close the workbench
				// ViewManager.INSTANCE.closeAll();
				// restarting application
				MessageDialog.openInformation(parent, msg.dialogMessageboxTitleInfo, msg.startFirstRestartmessage);
				workbench.restart();
			}
		} catch (BackingStoreException e) {
			log.error(e);
		}
		
//		super.okPressed();
	}
	
	private final class DirectoryChecker {
	    private Shell shell;
	    
	    // don't ask twice for this directory
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
            // The data base is in the /Database/ directory
            String path = selectedDirectory + "/Database/Database.script";
            File directory = new File(path);
            if(!alreadyCheckedDirs.contains(selectedDirectory) && directory.exists()) {
                boolean answer = MessageDialog.openQuestion(shell, "Datenübernahme", 
                           "In dem angegebenen Arbeitsverzeichnis befindet sich eine frühere\n"
                         + "Fakturama-Version. Möchten Sie diese Daten (Rechnungen,\n"
                         + "Produkte, Kontakte usw.) übernehmen?");
                if(answer) {
                    preferences.put(ConfigurationManager.MIGRATE_OLD_DATA, selectedDirectory);
                    txtOldWorkdir.setText(selectedDirectory);
                }
                alreadyCheckedDirs.add(selectedDirectory);
            }
        }

	}
	
	/**
	 * Selection Adapter for choosing the working directory
	 * 
	 * @author R. Heydenreich
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
				if(shouldCheckPreviousVersion) {
				    dirChecker.checkPreviousVersion(selectedDirectory);
				}
			}
		}

		public void widgetSelected(SelectionEvent event) {
			DirectoryDialog directoryDialog = new DirectoryDialog(selectionField.getShell(), SWT.OPEN);
			directoryDialog.setFilterPath(System.getProperty("user.home"));				
			//T: Title of the dialog to select the working directory
			directoryDialog.setText(forOldVersion ? msg.commandSelectoldworkspaceName : msg.commandSelectworkspaceName);
			//T: Text of the dialog to select the working directory
			directoryDialog.setMessage(forOldVersion ? msg.startFirstSelectOldworkdirVerbose : msg.startFirstSelectWorkdirVerbose);
			String selectedDirectory = directoryDialog.open();
			detectWorkspace(selectedDirectory);
		}
	}

}
