package com.sebulli.fakturama.startup;

import static com.sebulli.fakturama.Translate._;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

public class InitialStartupDialog extends TitleAreaDialog {

	private Text txtWorkdir, txtOldWorkdir, txtJdbcUrl, txtUser, txtPassword;
	private ComboViewer comboDriver;

	// Workspace path
	private String workspace = "";
	
	private Logger log;
	
	@Inject
	protected Shell parent;
	
	private IWorkbench workbench;


	// The plugin's preference store
	IEclipsePreferences preferences;
	
	private List<ServiceReference<DataSourceFactory>> connectionProviders = new ArrayList<>();

	/**
	 * Create the dialog.
	 * @param parent
	 * @param preferences2 
	 * @param log
	 * @param requestedWorkspace 
	 * @param style
	 */
	public InitialStartupDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent,
			IEclipsePreferences preferences, IWorkbench workbench, Logger log, String requestedWorkspace) {
		super(parent);
		this.workbench = workbench;
		this.preferences = preferences;
		this.workspace = requestedWorkspace;
		parent.setText(_("start.first.select.workdir"));
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
		setMessage(_("start.first.select.workdir.verbose"), IMessageProvider.INFORMATION);
		
		// 2nd row
		Label lblWorkDir = new Label(container, SWT.NONE);
		lblWorkDir.setText(_("start.first.select.workdir.short"));
		txtWorkdir = new Text(container, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		layoutData.minimumWidth = 450;
		txtWorkdir.setLayoutData(layoutData);
		txtWorkdir.setText(workspace);
		
		final Button btnDirChooser = new Button(container, SWT.NONE);
		btnDirChooser.setText("...");
		btnDirChooser.setToolTipText(_("start.first.select.workdir.verbose"));
		btnDirChooser.addSelectionListener(new DirectoryChooser(btnDirChooser, true, false));
		
		
		// 2.1st row
		Label lblOldWorkDir = new Label(container, SWT.NONE);
		lblOldWorkDir.setText(_("start.first.select.oldworkdir.short"));
		txtOldWorkdir = new Text(container, SWT.BORDER);
		txtOldWorkdir.setLayoutData(layoutData);
		
		final Button btnOldDirChooser = new Button(container, SWT.NONE);
		btnOldDirChooser.setText("...");
		btnOldDirChooser.setToolTipText(_("start.first.select.oldworkdir.verbose"));
		btnOldDirChooser.addSelectionListener(new DirectoryChooser(btnOldDirChooser, false, true));
		
		// 3rd row
		Label lblDatabase = new Label(container, SWT.NONE);
		lblDatabase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDatabase.setText(_("start.first.select.db.credentials.name"));
		
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
			      .getSelection()).getFirstElement()).getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
				txtJdbcUrl.setText(driverClass);
			}
		});
		Combo combo = comboDriver.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		combo.select(0);

		// 4th row
		Label lblJdbcurl = new Label(container, SWT.NONE);
		lblJdbcurl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblJdbcurl.setText(_("start.first.select.db.credentials.jdbc"));
		
		txtJdbcUrl = new Text(container, SWT.BORDER);
		txtJdbcUrl.setText("jdbc:derby:memory:test;create=true");
		txtJdbcUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// 5th row
		Label lblUser = new Label(container, SWT.NONE);
		lblUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUser.setText(_("start.first.select.db.credentials.user"));
		txtUser = new Text(container, SWT.BORDER);
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassword.setText(_("start.first.select.db.credentials.password"));

		txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

	    return container;

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) comboDriver.getSelection();
		ServiceReference<DataSourceFactory> firstElement = (ServiceReference<DataSourceFactory>) selection.getFirstElement();
		String driver =selection.isEmpty() ? "org.apache.derby.jdbc.EmbeddedDriver" :  (String) firstElement.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);

		// storing DB credentials
		try {
			preferences.put(PersistenceUnitProperties.JDBC_DRIVER, driver);
			preferences.put(PersistenceUnitProperties.JDBC_URL, txtJdbcUrl.getText());
			preferences.put(PersistenceUnitProperties.JDBC_USER, txtUser.getText());
			preferences.put(PersistenceUnitProperties.JDBC_PASSWORD, txtPassword.getText());
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
				MessageDialog.openInformation(parent, _("dialog.messagebox.title.info"), _("start.first.restartmessage"));
				workbench.restart();
			}
		} catch (BackingStoreException e) {
			log.error(e);
		}
		
		super.okPressed();
	}
	
	/*
	 * JDBC-URLs:
	 * Derby: jdbc:derby:[subsubprotocol:][databaseName][;attribute=value]*
	 * 
	 */
	
	/**
	 * Selection Adapter for choosing the working directory
	 * 
	 * @author R. Heydenreich
	 *
	 */
	private final class DirectoryChooser extends SelectionAdapter {
		private final Button btnDirChooser;
		private boolean shouldCheckPreviousVersion;
		private boolean forOldVersion;

		private DirectoryChooser(Button btnDirChooser, boolean shouldCheckPreviousVersion,
				boolean forOldVersion) {
			this.btnDirChooser = btnDirChooser;
			this.shouldCheckPreviousVersion = shouldCheckPreviousVersion;
			this.forOldVersion = forOldVersion;
		}

		public void widgetSelected(SelectionEvent e) {
			String oldString = forOldVersion ? "old" : "";
			DirectoryDialog directoryDialog = new DirectoryDialog(btnDirChooser.getShell(), SWT.OPEN);
			directoryDialog.setFilterPath(System.getProperty("user.home"));				
			//T: Title of the dialog to select the working directory
			directoryDialog.setText(_("command.select"+oldString+"workspace.name"));
			//T: Text of the dialog to select the working directory
			directoryDialog.setMessage(_("start.first.select."+oldString+"workdir.verbose"));
			String selectedDirectory = directoryDialog.open();
			if (selectedDirectory != null) {

				// test if it is valid
				if (selectedDirectory.equals("/") || selectedDirectory.equals("\\")) {
					selectedDirectory = "";
				}
				txtWorkdir.setText(selectedDirectory);
				if(shouldCheckPreviousVersion) {
					checkPreviousVersion(selectedDirectory);
				}
			}
		}

		/**
		 * Checks if a previous version is installed at this position
		 * @param selectedDirectory 
		 */
		private void checkPreviousVersion(String selectedDirectory) {
			// The data base is in the /Database/ directory
			String path = selectedDirectory + "/Database/Database.script";
			File directory = new File(path);
			if(directory.exists()) {
				boolean answer = MessageDialog.openQuestion(btnDirChooser.getShell(), "Datenübernahme", 
						   "In dem angegebenen Arbeitsverzeichnis befindet sich eine frühere\n"
						 + "Fakturama-Version. Möchten Sie diese Daten (Rechnungen,\n"
						 + "Produkte, Kontakte usw.) übernehmen?");
				if(answer) {
					preferences.put(ConfigurationManager.MIGRATE_OLD_DATA, selectedDirectory);
					txtOldWorkdir.setText(selectedDirectory);
				}
			}
		}
	}

}
