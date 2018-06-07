/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Dialog to enter a comment send to the user via the web shop
 * 
 * @author Gerd Bartelt
 */
public class OrderStatusDialog extends Dialog {

	// Controls of the dialog
	private Label labelComment;
	private Text txtComment;
	private Button bNotification;

	// The comment
	String comment = "";

	//True, if the customer should be notified
	boolean notify = false;

	protected Messages msg;

	/**
	 * Constructor
	 * 
	 * @param parentShell
	 *            Reference to the parents shell
	 * @param dialogTitle
	 *            The dialog title
	 */
	public OrderStatusDialog(Shell parentShell, Messages msg) {
		super(parentShell);
		this.msg = msg;
	}

	/**
	 * Creates and returns the contents of the upper part of this dialog (above
	 * the button bar).
	 * 
	 * @param the
	 *            parents composite.
	 */
	protected Control createDialogArea(Composite parent) {

		// The top composite
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		GridDataFactory.swtDefaults().applyTo(composite);

		// The label
		labelComment = new Label(composite, SWT.NONE);
		//T: Change the state of an order and send a notification to the customer.
		labelComment.setText(msg.dialogOrderstatusTitle + ":");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).indent(0, 10).applyTo(labelComment);

		// The text field for the  comment
		txtComment = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().hint(450, 120).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(txtComment);

		// The notification check box
		bNotification = new Button(composite, SWT.CHECK | SWT.LEFT);
		bNotification.setSelection(true);
		//T: Change the state of an order and send a notification to the customer.
		bNotification.setText(msg.dialogOrderstatusFieldNotify);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(bNotification);

		// Hide the text field, when "No notification" is selected
		bNotification.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { txtComment.setVisible(bNotification.getSelection()); }));

		return composite;
	}

	/**
	 * Configures the given shell in preparation for opening this window in it.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//T: Change the state of an order and send a notification to the customer.
		newShell.setText(msg.dialogOrderstatusTitle);
	}

	/**
	 * Returns the comment that was entered
	 * 
	 * @return The comment, or an empty string
	 */
	public String getComment() {
		return notify ? comment : "";
	}

	/**
	 * Returns, if the customer should be notified
	 * 
	 * @return TRUE, if the customer should be notified
	 */
	public boolean getNotify() {
		return notify;
	}

	/**
	 * Close the dialog and copy the content of the SWT controls to local
	 * variables.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		comment = StringUtils.chomp(txtComment.getText());
		notify = bNotification.getSelection();
		return super.close();

	}

}
