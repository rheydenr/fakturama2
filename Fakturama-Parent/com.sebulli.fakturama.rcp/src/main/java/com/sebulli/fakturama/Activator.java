/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.update.configurator.IPlatformConfiguration;
import org.eclipse.update.internal.configurator.FeatureEntry;
import org.eclipse.update.internal.configurator.PlatformConfiguration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.opcoach.e4.preferences.IPreferenceStoreProvider;
import com.sebulli.fakturama.preferences.FakturamaPreferenceStoreProvider;
import com.sebulli.fakturama.webshopimport.IWebshopConnectionService;
import com.sebulli.fakturama.webshopimport.WebshopConnectionService;

// import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Gerd Bartelt
 */
public class Activator implements BundleActivator, IBundleGroupProvider {


	// The bundle ID (Bundle-SymbolicName)
    public static final String PLUGIN_ID = "com.sebulli.fakturama.rcp";

    // The shared instance
    private static BundleContext context;
    ServiceRegistration<?> bundleGroupProviderSR; 
	private PlatformConfiguration configuration;
	
	private ServiceTracker<WebshopConnectionService, IWebshopConnectionService> webshopServiceTracker;
	 
    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static BundleContext getContext() {
        return context;
    }
    
    

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        // background color for focused widgets
//        JFaceResources.getColorRegistry().put(Constants.COLOR_BGYELLOW, new RGB(255, 255, 225));
        
        // perhaps: set RTL mode and configure the Workbench like so:
        if(BooleanUtils.toBoolean(System.getProperty("force.rtl"))) {
                Window.setDefaultOrientation(SWT.RIGHT_TO_LEFT);
        }

        // background for Browser
//		JFaceResources.getColorRegistry().put(Constants.COLOR_WHITE, new RGB(0xff, 0xff, 0xff));
		registerBundleGroupProvider();
		
		// register preference store provider
		bundleContext.registerService(IPreferenceStoreProvider.class, FakturamaPreferenceStoreProvider.getInstance(), null);
	}
	
	private void registerBundleGroupProvider() {
		final String serviceName = IBundleGroupProvider.class.getName();
		try {
			//don't register the service if this bundle has already registered it declaratively
			ServiceReference<?>[] refs = getContext().getServiceReferences(serviceName, null);
			if (refs != null) {
				for (int i = 0; i < refs.length; i++)
					if (PLUGIN_ID.equals(refs[i].getBundle().getSymbolicName()))
						return;
			}
		} catch (InvalidSyntaxException e) {
			//can't happen because we don't pass a filter
		}
		bundleGroupProviderSR = getContext().registerService(serviceName, this, null);
	} 
	
	@Override
	public String getName() {
		return "Bundle Group Provider";
	} 
	
	@Override
	public IBundleGroup[] getBundleGroups() {
		if (configuration == null)
			return new IBundleGroup[0];

		IPlatformConfiguration.IFeatureEntry[] features = configuration.getConfiguredFeatureEntries();
		List<IBundleGroup> bundleGroups = new ArrayList<>(features.length);
		for (int i = 0; i < features.length; i++) {
			if (features[i] instanceof FeatureEntry && ((FeatureEntry) features[i]).hasBranding())
				bundleGroups.add((IBundleGroup) features[i]);
		}
		return bundleGroups.toArray(new IBundleGroup[bundleGroups.size()]);
	}
 
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }
}
