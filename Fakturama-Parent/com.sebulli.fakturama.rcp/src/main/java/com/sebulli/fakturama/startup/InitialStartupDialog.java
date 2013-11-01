package com.sebulli.fakturama.startup;

import javax.inject.Named;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

import com.sebulli.fakturama.handlers.RestartHandler;

public class InitialStartupDialog extends TitleAreaDialog {

	private Text txtWorkdir, txtJdbcUrl, txtUser, txtPassword;
	private ComboViewer comboDriver;

	// Workspace path
	private String workspace = "";

	protected IEclipsePreferences workbenchPreferences;
	
	private EHandlerService handlerService;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public InitialStartupDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
		super(parent);
		parent.setText("First start");
		workbenchPreferences = ConfigurationScope.INSTANCE
	            .getNode("workbench");
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
	 	setTitle("Fakturama Initialisierung");
	    // 1st row
		setMessage("Sie starten Fakturama das erste Mal. Bitte nehmen Sie die nachfolgenden Einstellungen vor.", IMessageProvider.INFORMATION);
		
		// 2nd row
		Label lblWorkDir = new Label(container, SWT.NONE);
		lblWorkDir.setText("Arbeitsverzeichnis");
		txtWorkdir = new Text(container, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		layoutData.minimumWidth = 450;
		txtWorkdir.setLayoutData(layoutData);
		final Button btnDirChooser = new Button(container, SWT.NONE);
		btnDirChooser.setText("...");
		btnDirChooser.addSelectionListener(new SelectionAdapter() {
			 
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(btnDirChooser.getShell(),  SWT.OPEN  );
				directoryDialog.setFilterPath(System.getProperty("user.home"));				
				//T: Title of the dialog to select the working directory
				directoryDialog.setText("Select your working directory");
				//T: Text of the dialog to select the working directory
				directoryDialog.setMessage("Please select your working directory, where all the data is stored.");
				String selectedDirectory = directoryDialog.open();
				if (selectedDirectory != null) {

					// test if it is valid
					if (selectedDirectory.equals("/") || selectedDirectory.equals("\\"))
						selectedDirectory = "";
					if (!selectedDirectory.isEmpty()) {

						// If there is a connection to the database,
						// use the new working directory after a restart.
//						if (DataBaseConnectionState.INSTANCE.isConnected()) {

							// Store the requested directory in a preference value
						workbenchPreferences.put("GENERAL_WORKSPACE_REQUEST", selectedDirectory);
							MessageBox messageBox = new MessageBox(btnDirChooser.getShell(), SWT.ICON_INFORMATION);
							
							//T: Title of a message box
							messageBox.setText("Information");
							//T: Text of the dialog that the workspace will be switched and that you should restart Fakturama.
							messageBox.setMessage("To switch the workspace,\nFakturama will be restarted!");
							messageBox.open();

							// Close the workbench
							//					ViewManager.INSTANCE.closeAll();
//							PlatformUI.getWorkbench().restart();
//							handlerService.executeHandler(RestartHandler)
//						}
//						// if there is no connection, use it immediately
//						else {
							setWorkspace(selectedDirectory);
							showWorkingDirInTitleBar();
//						}
					}
				} else {
					return;
				}
				txtWorkdir.setText(selectedDirectory);
			}
		});

		// 3rd row
		Label lblDatabase = new Label(container, SWT.NONE);
		lblDatabase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDatabase.setText("Datenbank");
		
		comboDriver = new ComboViewer(container, SWT.NONE);
		comboDriver.setContentProvider(ArrayContentProvider.getInstance());
		comboDriver.setInput(new String[] { "bitte wählen...", "Standard-Datenbank (HSQL)", "MySQL", "Derby" });
		Combo combo = comboDriver.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		combo.select(0);

		// 4th row
		Label lblJdbcurl = new Label(container, SWT.NONE);
		lblJdbcurl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblJdbcurl.setText("JDBC-URL");
		
		txtJdbcUrl = new Text(container, SWT.BORDER);
		txtJdbcUrl.setText("jdbc:derby:memory:test;create=true");
		txtJdbcUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// 5th row
		Label lblUser = new Label(container, SWT.NONE);
		lblUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUser.setText("User");
		txtUser = new Text(container, SWT.BORDER);
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassword.setText("Password");

		txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

	    return container;

	}

	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) comboDriver.getSelection();
		String driver = selection.isEmpty() ? "org.apache.derby.jdbc.EmbeddedDriver" : selection.getFirstElement().toString();

		try {
			workbenchPreferences.put("jdbc_driver", driver);
			workbenchPreferences.put("jdbc_url", txtJdbcUrl.getText());
			workbenchPreferences.putBoolean("jdbc_reconnect", true);
			workbenchPreferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		super.okPressed();
	}

		/**
		 * Set the workspace
		 * 
		 * @param workspace
		 *            Path to the workspace
		 */
		public void setWorkspace(String workspace) {
			this.workspace = workspace;
			workbenchPreferences.put("GENERAL_WORKSPACE", workspace);
		}

		/**
		 * Displays the current workspace in the title bar
		 */
		public void showWorkingDirInTitleBar() {
			getShell().setText("Fakturama - " + workspace);
		}

}
