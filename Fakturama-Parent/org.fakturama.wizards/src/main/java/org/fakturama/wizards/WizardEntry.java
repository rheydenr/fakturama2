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

import org.eclipse.jface.wizard.IWizardNode;

/**
 *
 */
public class WizardEntry {
	private IE4WizardCategory category;
	private IE4WizardDescriptor descriptor;
	private IWizardNode wizardNode;

	public WizardEntry() {};
	
	public WizardEntry(IE4WizardCategory addressListCategory, IE4WizardDescriptor addressListWizardDescriptor, IWizardNode wizardNode) {
		this.category = addressListCategory;
		this.descriptor = addressListWizardDescriptor;
		this.wizardNode = wizardNode;
	}

	public void setWizardNode(IWizardNode node) {
		this.wizardNode = node;
	}
	
	/**
	 * @return the wizardNode
	 */
	public final IWizardNode getWizardNode() {
		return wizardNode;
	}

	/**
	 * @param category the category to set
	 */
	public final void setCategory(IE4WizardCategory category) {
		this.category = category;
	}

	/**
	 * @return the descriptor
	 */
	public final IE4WizardDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	public final void setDescriptor(IE4WizardDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * @return the category
	 */
	public final IE4WizardCategory getCategory() {
		return category;
	}

}
