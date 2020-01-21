/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *      font should be activated and used by other components.
 *******************************************************************************/
package com.sebulli.fakturama.ui.dialogs.about;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.ui.dialogs.about.internal.AboutBundleGroupData;
import com.sebulli.fakturama.ui.dialogs.about.internal.AboutFeaturesPage;
import com.sebulli.fakturama.ui.dialogs.about.internal.IWorkbenchHelpContextIds;
import com.sebulli.fakturama.ui.dialogs.about.internal.ProductInfoDialog;
import com.sebulli.fakturama.ui.dialogs.about.internal.WorkbenchMessages;

/**
 * Displays information about the product plugins.
 * 
 * PRIVATE This class is internal to the workbench and must not be called
 * outside the workbench.
 */
public class AboutFeaturesDialog extends ProductInfoDialog {
	/**
	 * Constructor for AboutFeaturesDialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param productName
	 *            the product name
	 * @param bundleGroupInfos
	 *            the bundle info
	 */
	public AboutFeaturesDialog(Shell parentShell, String productName, AboutBundleGroupData[] bundleGroupInfos,
			AboutBundleGroupData initialSelection) {
		super(parentShell);
		AboutFeaturesPage page = new AboutFeaturesPage();
		page.setProductName(productName);
		page.setBundleGroupInfos(bundleGroupInfos);
		page.setInitialSelection(initialSelection);
		String title;
		if (productName != null)
			title = NLS.bind(WorkbenchMessages.AboutFeaturesDialog_shellTitle, productName);
		else
			title = WorkbenchMessages.AboutFeaturesDialog_SimpleTitle;
		initializeDialog(page, title, IWorkbenchHelpContextIds.ABOUT_FEATURES_DIALOG);
	}
}
