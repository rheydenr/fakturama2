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
package com.sebulli.fakturama.ui.dialogs.about.internal.e3;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.sebulli.fakturama.ui.dialogs.WorkbenchMessages;

/**
 * Displays information about the product plugins.
 * 
 * PRIVATE this class is internal to the ide
 */
public class AboutPluginsDialog extends ProductInfoDialog {
	static final String ABOUT_PRODUCTNAME = "about.productname";
	static final String ABOUT_TITLE = "about.title";
	static final String ABOUT_MESSAGE = "about.message";
	static final String ABOUT_BUNDLES = "about.bundles";
	static final String ABOUT_HELPID = "about.helpid";
	
	@Inject
	private IEclipseContext context;
	
	public AboutPluginsDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@PostConstruct
	public void init( ) {
		AboutPluginsPage page = ContextInjectionFactory.make(AboutPluginsPage.class, context);
		page.setHelpContextId((String) context.get(ABOUT_HELPID));
		page.setBundles((Bundle[]) context.get(ABOUT_BUNDLES));
		page.setMessage((String) context.get(ABOUT_MESSAGE));
		this.title = (String) context.get(ABOUT_TITLE);
		String productName = (String) context.get(ABOUT_PRODUCTNAME);
		if (title == null && page.getProductName() != null)
			title = NLS.bind(WorkbenchMessages.AboutPluginsDialog_shellTitle, productName);
		initializeDialog(page, title, helpContextId);
	}
}
