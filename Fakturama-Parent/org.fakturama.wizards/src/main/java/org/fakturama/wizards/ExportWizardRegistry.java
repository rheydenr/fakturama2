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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.fakturama.wizards.internal.AbstractExtensionWizardRegistry;
import org.fakturama.wizards.internal.registry.IWorkbenchRegistryConstants;
import org.fakturama.wizards.internal.registry.WizardsRegistryReader;

/**
 *
 */
public class ExportWizardRegistry extends AbstractExtensionWizardRegistry {

	private static final String REGISTRY_READER_ADDON_ID = "fakturama.addon.wizardregistry.reader";

	public static final String PLUGINPOINT_EXPORT_WIZARDS = "exportWizards";

	@Inject
	private IExtensionRegistry registry;
	
	private WizardsRegistryReader wizardsRegistryReader;
	
	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_EXPORT;
	}

	@Override
	protected String getPlugin() {
		return Activator.getDefault().getBundle().getSymbolicName();
	}
	
	@PostConstruct
	public void initialize(MApplication application) {
		if(wizardsRegistryReader == null) {
			MAddon addon = application.getAddons().stream().filter(
					a -> a.getElementId().equals(REGISTRY_READER_ADDON_ID)).findFirst().get();
			wizardsRegistryReader = (WizardsRegistryReader) addon.getObject();
			wizardsRegistryReader.setPlugin(getPlugin());
			wizardsRegistryReader.setPluginPoint(PLUGINPOINT_EXPORT_WIZARDS);
		}
	}

	/**
	 * @return the wizardsRegistryReader
	 */
	public final WizardsRegistryReader getWizardsRegistryReader() {
		return wizardsRegistryReader;
	}

	/**
	 * @return the registry
	 */
	public final IExtensionRegistry getRegistry() {
		return registry;
	}
}
