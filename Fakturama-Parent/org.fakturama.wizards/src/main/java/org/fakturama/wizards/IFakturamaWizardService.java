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
 
package org.fakturama.wizards;

import java.util.List;

import org.eclipse.jface.wizard.IWizardNode;

/**
 * Service interface for exporter services.
 *
 */
public interface IFakturamaWizardService {

	/**
	 * Retrieve a {@link List} of {@link IWizardNode}s which this service offers.
	 * @return
	 */
	public List<WizardEntry> getExporterList();

	/**
	 * Creates the given wizard. 
	 * 
	 * @param className 
	 * @return
	 */
	public IWorkbenchWizard createWizard(String className);
}
