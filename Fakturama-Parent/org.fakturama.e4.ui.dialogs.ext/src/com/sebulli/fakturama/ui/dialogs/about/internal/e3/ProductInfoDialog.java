/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sebulli.fakturama.ui.dialogs.about.internal.e3;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Abstract superclass of the individual about dialogs that appear outside of
 * the InstallationDialog. These dialogs contain a single installation page, and
 * scope the page to something more specific than it would be in the standard
 * installation dialog.
 * 
 * It is important that the visibility and enablement expressions of
 * contributions to this dialog, and the source variables that drive them, do
 * not conflict with those used inside the normal InstallationDialog. Otherwise,
 * the button manager of the InstallationDialog will be affected by changes in
 * the launched dialog. Where commands have enablement expressions in this
 * dialog, we use a unique command id so that there are no handler conflicts
 * with the regular dialog.
 */

public abstract class ProductInfoDialog extends InstallationDialog {

	ProductInfoPage page;
	String title;
	String helpContextId;

	protected ProductInfoDialog(Shell shell) {
		super(shell);
	}

	public void initializeDialog(ProductInfoPage page, String title, String helpContextId) {
		this.page = page;
		this.title = title;
		this.helpContextId = helpContextId;
	}

	@Override
	protected void createFolderItems(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(title);
		Composite control = new Composite(folder, SWT.BORDER);
		control.setLayout(new GridLayout());
		item.setControl(control);
		page.createControl(control);
		item.setData(page);
		item.setData(ID, page.getId());
		page.setPageContainer(this);
		item.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				page.dispose();
			}
		});
		control.layout(true, true);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButtons(page);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);

		// FIXME HelpSystem
		// if (helpContextId != null) {
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
		// helpContextId);
		// }
	}

	@Override
	protected String pageToId(InstallationPage page) {
		Assert.isLegal(page == this.page);
		return this.page.getId();
	}
}
