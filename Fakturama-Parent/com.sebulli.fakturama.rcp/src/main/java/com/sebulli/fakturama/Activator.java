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

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.osgi.logservice.impl.LogServiceFactory;
import org.slf4j.osgi.logservice.impl.LogServiceImpl;

import com.sebulli.fakturama.resources.urihandler.IconURLStreamHandlerService;

// import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Gerd Bartelt
 */
public class Activator implements BundleActivator {

	// The bundle ID (Bundle-SymbolicName)
    public static final String PLUGIN_ID = "com.sebulli.fakturama.rcp";
	private static final String[] LOGSERVICE_CLASSES = {LogService.class.getName()};
    private ServiceFactory<?> logServiceFactory = new LogServiceFactory();
	private ServiceRegistration<?> logServiceRegistration;
	
	/**
	 * If Logging using slf4j API and Logback as backend you can add Marker.
	 * you can build a Marker- Graph - if Marker IS_BUNDLE is contained, then
	 * the Marker Name is the Bundles Symbolic name.
	 */
	public static final String IS_BUNDLE_MARKER = "OSGI_BUNDLE";	 //$NON-NLS-1$

    // The shared instance
    private static BundleContext context;
	
	// logger name = class name
	private static final Logger logger = LoggerFactory.getLogger(Activator.class);
	
	// The Bundle Marker: a Marker where the name is the osgi bundle symbolic name
	// and an attached IS_BUNDLE - Marker to guarantee that the Log Framework knows its a BundleMarker
	public static final Marker bundleMarker = createBundleMarker();
	
	private static final Marker createBundleMarker() {
		Marker bundleMarker = MarkerFactory.getMarker(PLUGIN_ID);
		bundleMarker.add(MarkerFactory.getMarker(IS_BUNDLE_MARKER));
		return bundleMarker;
	}

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
        JFaceResources.getColorRegistry().put("bgyellow", new RGB(255, 255, 225));
        // for using of icons from another plugin
        IconURLStreamHandlerService.getInstance().register();
//        ContextInjectionFactory.inject(logServiceRegistration,bundleContext.);

        logServiceRegistration = bundleContext.registerService(LOGSERVICE_CLASSES, logServiceFactory, null);

        logger.info("Starting org.eclipsecon.logging.log4j Excample");
		logger.debug("Here is a Log4J Bundle at the EclipseCon 2010");
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
    	logServiceRegistration.unregister();
        Activator.context = null;
    }
}
