package com.sebulli.fakturama.parts;
// TODO GS/ e-mail/url: Links with appropriate functionality

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.sebulli.fakturama.misc.EmbeddedProperties;

//TODO GS/i18n - check all -
public class EInvoiceNoticeDialog extends Dialog {
	private String title;
	private String message;
	private EmbeddedProperties embeddedProperties;
	private Text txtNotice;
	private Text txtMailTo;
	private Text txtUrl;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public EInvoiceNoticeDialog(Shell parentShell, String title, String message, EmbeddedProperties ep) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.title = title;
		this.message = message;
		this.embeddedProperties = ep;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		Label lblMessage = new Label(container, SWT.NONE);
		GridData gd_lblMessage = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_lblMessage.verticalIndent = 15;
		lblMessage.setLayoutData(gd_lblMessage);
		lblMessage.setText(message);
		
		Label lblAdditionalInfo = new Label(container, SWT.NONE);
		lblAdditionalInfo.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		GridData gd_lblAdditionalInfo = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_lblAdditionalInfo.verticalIndent = 10;
		lblAdditionalInfo.setLayoutData(gd_lblAdditionalInfo);
		lblAdditionalInfo.setAlignment(SWT.LEFT);
		lblAdditionalInfo.setText("Informationen zur erstellten E-Rechnung");
		
		Label lblFormat = new Label(container, SWT.NONE);
		lblFormat.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFormat.setText("Format");
		
		Label lblFormatValue = new Label(container, SWT.NONE);
		lblFormatValue.setFont(SWTResourceManager.getFont("Sans", 11, SWT.NORMAL));
		lblFormatValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblFormatValue.setText(embeddedProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE, "", ""));
		
		Label lblNotice = new Label(container, SWT.NONE);
		lblNotice.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNotice.setText("Hinweis");
		
		txtNotice = new Text(container, SWT.BORDER);
		txtNotice.setEditable(false);
		txtNotice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtNotice.setText(embeddedProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_NOTICE, "", ""));
		
		Label lblMailTo = new Label(container, SWT.NONE);
		lblMailTo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMailTo.setText("per E-Mail an");
		
		txtMailTo = new Text(container, SWT.BORDER);
		txtMailTo.setEditable(false);
		txtMailTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtMailTo.setText(embeddedProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_MAILTO, "", ""));
		
		Label lblUrl = new Label(container, SWT.NONE);
		lblUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUrl.setText("Portal/URL");
		
		txtUrl = new Text(container, SWT.BORDER);
		txtUrl.setEditable(false);
		txtUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtUrl.setText(embeddedProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_URL, "", ""));

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button btnOk = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
//		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(561, 304);
	}

}
