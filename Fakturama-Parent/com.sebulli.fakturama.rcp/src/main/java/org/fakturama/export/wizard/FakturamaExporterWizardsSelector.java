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

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.wizard.Wizard;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.sebulli.fakturama.i18n.Messages;

/**
 * This is the main entry point for all export wizards. This class
 * creates a "single page" (dummy) wizard which contains a selection
 * tree of all available wizards (see {@link FakturamaExporterWizards}). 
 *
 */
public class FakturamaExporterWizardsSelector extends Wizard {
	@Inject
	IEclipseContext ctx;

    @Inject
    @Translation
    protected Messages msg;

    @Override
    public void addPages() {
    	
    	/*
    	 * The following lines are for registering the EclipseContext in the OSGi bundle context
    	 * since else it is not available (including Messages). 
    	 */
        Bundle bundle = FrameworkUtil.getBundle(Messages.class);
        BundleContext bundleContext = bundle.getBundleContext();
        ctx.set(Messages.class, msg);
        bundleContext.registerService(IEclipseContext.class, ctx, null);
    	
    	FakturamaExporterWizards fakturamaExporterWizards = ContextInjectionFactory.make(FakturamaExporterWizards.class, ctx);
        addPage(fakturamaExporterWizards);
        setForcePreviousAndNextButtons(true);
    }
    
    @Override
    public boolean performFinish() {
        return false;
    }
    
}