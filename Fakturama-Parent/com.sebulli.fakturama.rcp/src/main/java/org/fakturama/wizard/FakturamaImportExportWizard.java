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
package org.fakturama.wizard;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.fakturama.wizard.export.ExportPage;
import org.fakturama.wizard.imp.ImportPage;
import org.fakturama.wizards.IFakturamaWizardService;
import org.fakturama.wizards.ImportExportPage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.Activator;
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
	
	private IEclipseContext staticContext;
	
    @Inject
    @Translation
    protected Messages msg;

	private ImportExportPage importExportPage;
    
    @PostConstruct
    public void init(IExtensionRegistry registry) {
    	String filter = "";
    	page = (String) ctx.get(WIZARD_MODE);
    	staticContext = EclipseContextFactory.create();
    	
    	registerMessages();
    	
        ImageDescriptor wizardBannerImage = null;
        if (IMPORT.equals(page)){
        	filter = "(component.name=myImporter)";
        	wizardBannerImage = Icon.IMPORT_WIZ.getImageDescriptor(IconSize.WizardHeaderIconSize);
        	setWindowTitle(msg.wizardImportCommonTitle);
        } else if (EXPORT.equals(page)){
        	filter = "(component.name=myExporter)";
        	wizardBannerImage = Icon.EXPORT_WIZ.getImageDescriptor(IconSize.WizardHeaderIconSize);
        	setWindowTitle(msg.wizardExportCommonTitle);
        }
        
        /*
         * Since we have more than one IFakturamaWizardService (import and export at least) we have to distinguish
         * between them by name. Otherwise the ContextInjectionFactory (later on) takes the first fitting service reference,
         * which must not be correct. Therefore, we're looking for the correct service depending on page type and put it into 
         * a static context. This context can be used later on to inject the correct service into the wizard. 
         */
    	try {
    		ServiceReference<IFakturamaWizardService>[] serviceReferences = (ServiceReference<IFakturamaWizardService>[]) Activator.getContext().getServiceReferences(IFakturamaWizardService.class.getName(), filter);
    		if(serviceReferences.length > 0) {
    			IFakturamaWizardService service = Activator.getContext().getService(serviceReferences[0]);
    			staticContext.set(IFakturamaWizardService.class, service);
    		}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    	IEclipseContext staticCtx = EclipseContextFactory.create();
    	if (page.equals(IMPORT)) {
	    	importExportPage = ContextInjectionFactory.make(ImportPage.class, ctx, staticCtx);
		} else if (page.equals(EXPORT)) {
	    	importExportPage = ContextInjectionFactory.make(ExportPage.class, ctx, staticCtx);
		}
        if (importExportPage != null) {
			addPage(importExportPage);
		}
    }
    
//    @Override
//    public boolean canFinish() {
//    	if(importExportPage != null && ((WizardSelectionPage)importExportPage).getSelectedNode() != null) {
//    		return ((WizardSelectionPage)importExportPage).getSelectedNode().getWizard().canFinish();
//    	} else {
//    		return super.canFinish();
//    	}
//    }
    
    
    @Override
    public boolean performFinish() {
//    	if(importExportPage != null && ((WizardSelectionPage)importExportPage).getSelectedNode() != null) {
    		importExportPage.saveWidgetValues();
//    		return ((WizardSelectionPage)importExportPage).getSelectedNode().getWizard().performFinish();
//    	} else {
    		return true;
//    	}
    }
    
}