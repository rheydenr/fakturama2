/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 * 		font should be activated and used by other components.
 *******************************************************************************/
package com.sebulli.fakturama.ui.internal.dialog;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.sebulli.fakturama.dialogs.about.AboutPluginsPage;
import com.sebulli.fakturama.dialogs.about.ProductInfoDialog;
import com.sebulli.fakturama.ui.internal.WorkbenchMessages;

/**
 * Displays information about the product plugins.
 * 
 * PRIVATE this class is internal to the ide
 */
public class AboutPluginsDialog extends ProductInfoDialog {
	public AboutPluginsDialog(Shell parentShell, String productName, Bundle[] bundles, String title, String message,
			String helpContextId) {
		super(parentShell);
		AboutPluginsPage page = new AboutPluginsPage();
		page.setHelpContextId(helpContextId);
		page.setBundles(bundles);
		page.setMessage(message);
		if (title == null && page.getProductName() != null)
			title = NLS.bind(WorkbenchMessages.AboutPluginsDialog_shellTitle, productName);
		initializeDialog(page, title, helpContextId);
	}
}
