package com.sebulli.fakturama.startup;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InitialStartupDialog extends Dialog {

	protected int result;
	private Text text;
	private Text txtJdbcUrl;

	// Workspace path
	private String workspace = "";

	@Inject
	@Preference(nodePath = "/configuration/workbench")
	protected IEclipsePreferences workbenchPreferences;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public InitialStartupDialog(Shell parent) {
		super(parent);
	}

	/**
	 * Create contents of the dialog.
	 */
	  @Override
	  protected Control createDialogArea(Composite parent) {
	    Composite container = (Composite) super.createDialogArea(parent);
		getShell().setText("First start");
	    container.setLayout(new GridLayout(4, false));
		
		Label lblSieStartenFakturama = new Label(container, SWT.NONE);
		lblSieStartenFakturama.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		lblSieStartenFakturama.setText("Sie starten Fakturama das erste Mal. Bitte nehmen Sie die nachfolgenden Einstellungen vor.");
		Label filler = new Label(container, SWT.NONE);
		GridDataFactory.generate(filler, 4, 1);
		
		Label lblArbeitsverzeichnis = new Label(container, SWT.NONE);
		lblArbeitsverzeichnis.setText("Arbeitsverzeichnis:");
		
		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final Button button = new Button(container, SWT.NONE);
		button.setText("...");
		button.addSelectionListener(new SelectionListener() {
			 
			public void widgetDefaultSelected(SelectionEvent e) {
			}
 
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(button.getShell(),  SWT.OPEN  );
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
//							Activator.getDefault().getPreferenceStore().setValue("GENERAL_WORKSPACE_REQUEST", selectedDirectory);
						workbenchPreferences.put("GENERAL_WORKSPACE_REQUEST", selectedDirectory);
							MessageBox messageBox = new MessageBox(button.getShell(), SWT.ICON_INFORMATION);
							
							//T: Title of a message box
							messageBox.setText("Information");
							//T: Text of the dialog that the workspace will be switched and that you should restart Fakturama.
							messageBox.setMessage("To switch the workspace,\nFakturama will be restarted!");
							messageBox.open();

							// Close the workbench
							//					ViewManager.INSTANCE.closeAll();
//							PlatformUI.getWorkbench().restart();
//						}
//						// if there is no connection, use it immediately
//						else {
							setWorkspace(selectedDirectory);
//							showWorkingDirInTitleBar();
//						}
					}
				} else {
					return;
				}
				
				
				
				
				
				text.setText(selectedDirectory);
			}
		});
		
		new Label(container, SWT.NONE);
		
		Label lblDatenbank = new Label(container, SWT.NONE);
		lblDatenbank.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDatenbank.setText("Datenbank:");
		
		CCombo combo = new CCombo(container, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo.setItems(new String[] {"bitte wählen...", "Standard-Datenbank (HSQL)", "MySQL", "Derby"});
		combo.select(0);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblJdbcurl = new Label(container, SWT.NONE);
		lblJdbcurl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblJdbcurl.setText("JDBC-URL:");
		
		txtJdbcUrl = new Text(container, SWT.BORDER);
		txtJdbcUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridDataFactory.generate(filler, 3, 1);
	    return container;

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
			try {
				getShell().setText("Fakturama - " + workspace);
			}
			catch (Exception e) {
			}
			;

		}

}
