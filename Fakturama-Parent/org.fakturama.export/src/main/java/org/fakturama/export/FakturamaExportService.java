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

package org.fakturama.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.fakturama.wizards.IExportWizard;
import org.fakturama.wizards.IFakturamaWizardService;
import org.fakturama.wizards.IWorkbenchWizard;
import org.fakturama.wizards.WizardEntry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;

/**
 * Implementation of the {@link IFakturamaWizardService}
 */
public class FakturamaExportService implements IFakturamaWizardService {

	private List<WizardEntry> wizardNodes = new ArrayList<>();

//	@Inject
	private IEclipseContext ctx;

	@Inject
	private ILogger log;

	public void startUp() {

		Bundle bundle = FrameworkUtil.getBundle(Messages.class);
		BundleContext bundleContext = bundle.getBundleContext();
		try {
			Collection<ServiceReference<IEclipseContext>> serviceReferences = bundleContext
					.getServiceReferences(IEclipseContext.class, null);
			ServiceReference<IEclipseContext> next = serviceReferences.iterator().next();
			ctx = bundleContext.getService(next);
		} catch (InvalidSyntaxException e) {
			// log.error(e);
		}
	}

	public void shutDown() {
		wizardNodes = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fakturama.export.IFakturamaExportService#getExporterList()
	 */
	@Override
	public List<WizardEntry> getExporterList() {
		return wizardNodes;
	}

	@Override
	public IWorkbenchWizard createWizard(String className) {
		try {
			Class wizardClass = Class.forName(className);
			boolean matches = Arrays.stream(wizardClass.getInterfaces()).anyMatch(c -> c.getName().equals(IExportWizard.class.getName()));
			if(matches) {
				return (IWorkbenchWizard) ContextInjectionFactory.make(wizardClass, ctx);
			}
		} catch (ClassNotFoundException e) {
			log.error(e, "error while creating a wizard ("+className+")");
		}
		
		return null;
	}

	@Override
	public String getExtensionPointPlugin() {
		return FrameworkUtil.getBundle(getClass()).getSymbolicName();
	}
}
