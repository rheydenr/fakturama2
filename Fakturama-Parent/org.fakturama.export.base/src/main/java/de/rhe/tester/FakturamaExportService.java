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
 
package de.rhe.tester;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.fakturama.export.AbstractWizardNode;
import org.fakturama.export.IFakturamaExportService;
import org.fakturama.export.wizard.contacts.AddressListExportWizardNode;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;

/**
 * Implementation of the {@link IFakturamaExportService} *
 */
public class FakturamaExportService implements IFakturamaExportService {

	private AbstractWizardNode[] wizardNodes;
    
@Inject 
	IEclipseContext ctx;

//@Inject
//private LogS log;

//	
//	/**
//	 * @param ctx
//	 */
//	@Inject
//	public FakturamaExportService(IEclipseContext ctx) {
//		this.ctx = ctx;
//	}

	public void startUp() {
		
        Bundle bundle = FrameworkUtil.getBundle(Messages.class);
        BundleContext bundleContext = bundle.getBundleContext();
        try {
			Collection<ServiceReference<IEclipseContext>> serviceReferences = bundleContext.getServiceReferences(IEclipseContext.class, null);
			ServiceReference<IEclipseContext> next = serviceReferences.iterator().next();
			ctx = bundleContext.getService(next);
		} catch (InvalidSyntaxException e) {
//			log.error(e);
		}
		
//		wizardNodes = new AbstractWizardNode[]{new AddressListExportWizardNode("Java Project"),
		wizardNodes = new AbstractWizardNode[]{
                ContextInjectionFactory.make(AddressListExportWizardNode.class, ctx), //("Java Project"),
                ContextInjectionFactory.make(AddressListExportWizardNode.class, ctx), //("Scala Project"),
                ContextInjectionFactory.make(AddressListExportWizardNode.class, ctx)  //("JavaScript Project")
        };
	}
	
	public void shutDown() {
		wizardNodes = null;
	}

	/* (non-Javadoc)
	 * @see org.fakturama.export.IFakturamaExportService#getExporterList()
	 */
	@Override
	public List<AbstractWizardNode> getExporterList() {
        return Arrays.asList(wizardNodes);
	}

}
