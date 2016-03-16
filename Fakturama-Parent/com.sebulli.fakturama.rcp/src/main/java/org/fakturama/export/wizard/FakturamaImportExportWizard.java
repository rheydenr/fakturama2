/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
package org.fakturama.export.wizard;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * This is the main entry point for all export wizards. This class
 * creates a "single page" (dummy) wizard which contains a selection
 * tree of all available wizards (see {@link FakturamaExportPage}). 
 * <p>
 * This class is similar to org.eclipse.ui.internal.dialogs.ImportExportWizard.
 *
 */
public class FakturamaImportExportWizard extends Wizard {
	/**
	 * Constant used to to specify to the import/export wizard
	 * which page should initially be shown. 
	 */
	public static final String IMPORT = "import";	//$NON-NLS-1$
	/**
	 * Constant used to to specify to the import/export wizard
	 * which page should initially be shown. 
	 */
	public static final String EXPORT = "export";	//$NON-NLS-1$
	public static final String WIZARD_MODE = "org.fakturama.wizards.importexport";	//$NON-NLS-1$
	
    private String page = null;

	@Inject
	protected IEclipseContext ctx;

    @Inject
    @Translation
    protected Messages msg;

	private ExportPage importExportPage;
    
    @PostConstruct
    public void init(IExtensionRegistry registry) {
    	page = (String) ctx.get(WIZARD_MODE);
    	registerMessages();
        
        ImageDescriptor wizardBannerImage = null;
        if (IMPORT.equals(page)){
        	wizardBannerImage = Icon.IMPORT_WIZ.getImageDescriptor(IconSize.WizardHeaderIconSize);
        	setWindowTitle(msg.wizardExportCommonTitle);
        }
        else if (EXPORT.equals(page)){
        	wizardBannerImage = Icon.EXPORT_WIZ.getImageDescriptor(IconSize.WizardHeaderIconSize);
        	setWindowTitle(msg.wizardExportCommonTitle);
        }
        if (wizardBannerImage != null) {
			setDefaultPageImageDescriptor(wizardBannerImage);
		}
        setNeedsProgressMonitor(true);
    }

	/**
	 * 
	 */
	private void registerMessages() {
		/*
    	 * The following lines are for registering the EclipseContext in the OSGi bundle context
    	 * since else it is not available (including Messages). 
    	 */
        Bundle bundle = FrameworkUtil.getBundle(Messages.class);
        BundleContext bundleContext = bundle.getBundleContext();
        ctx.set(Messages.class, msg);
        bundleContext.registerService(IEclipseContext.class, ctx, null);
	}

    @Override
    public void addPages() {
    	if (page.equals(IMPORT)) {
//	    	importExportPage = ContextInjectionFactory.make(ImportPage.class, ctx);
		} else if (page.equals(EXPORT)) {
	    	importExportPage = ContextInjectionFactory.make(ExportPage.class, ctx);
		}
        if (importExportPage != null) {
			addPage(importExportPage);
		}
    }
    
    @Override
    public boolean performFinish() {
    	importExportPage.saveWidgetValues();
        return true;
    }
    
}