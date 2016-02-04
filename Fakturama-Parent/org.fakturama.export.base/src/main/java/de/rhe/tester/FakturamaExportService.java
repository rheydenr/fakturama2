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
import java.util.List;

import org.fakturama.export.AbstractWizardNode;
import org.fakturama.export.IFakturamaExportService;
import org.fakturama.export.wizard.contacts.AddressListExportWizardNode;

/**
 * Implementation of the {@link IFakturamaExportService}.
 *
 */
public class FakturamaExportService implements IFakturamaExportService {

	private AbstractWizardNode[] wizardNodes;

	//@Inject IEclipseContext ctx;
	public void startUp() {
		wizardNodes = new AbstractWizardNode[]{new AddressListExportWizardNode("Java Project"),
//		wizardNodes = new AbstractWizardNode[]{
//                ContextInjectionFactory.make(AddressListExportWizardNode.class, ctx), //("Java Project"),
//                ContextInjectionFactory.make(AddressListExportWizardNode.class, ctx), //("Scala Project"),
//                ContextInjectionFactory.make(AddressListExportWizardNode.class, ctx)  //("JavaScript Project")
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
