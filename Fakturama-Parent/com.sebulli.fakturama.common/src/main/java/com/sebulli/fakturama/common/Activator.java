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

package com.sebulli.fakturama.common;

import java.util.Iterator;
import java.util.LinkedList;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.sebulli.fakturama.log.LogbackAdapter;

// import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 * 
 */
public class Activator implements BundleActivator {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.sebulli.fakturama.common";
    
	/**
	 * If Logging using slf4j API and Logback as backend you can add Marker.
	 * you can build a Marker- Graph - if Marker IS_BUNDLE is contained, then
	 * the Marker Name is the Bundles Symbolic name.
	 */
	public static final String IS_BUNDLE_MARKER = "OSGI_BUNDLE";	 //$NON-NLS-1$

    // The shared instance
    private static BundleContext context;

    private LogListener m_console = new LogbackAdapter();
    private LinkedList<LogReaderService> m_readers = new LinkedList<LogReaderService>();
	
	// The Bundle Marker: a Marker where the name is the osgi bundle symbolic name
	// and an attached IS_BUNDLE - Marker to guarantee that the Log Framework knows it's a BundleMarker
	public static final Marker bundleMarker = createBundleMarker();
	
	private static final Marker createBundleMarker() {
		Marker bundleMarker = MarkerFactory.getMarker(PLUGIN_ID);
		bundleMarker.add(MarkerFactory.getMarker(IS_BUNDLE_MARKER));
		return bundleMarker;
	}

    //  We use a ServiceListener to dynamically keep track of all the LogReaderService service being
    //  registered or unregistered
    private ServiceListener m_servlistener = new ServiceListener() {
		public void serviceChanged(ServiceEvent event) {
			BundleContext bc = event.getServiceReference().getBundle().getBundleContext();
			LogReaderService lrs = (LogReaderService) bc.getService(event.getServiceReference());
			if (lrs != null) {
				if (event.getType() == ServiceEvent.REGISTERED) {
					m_readers.add(lrs);
					lrs.addLogListener(m_console);
				}
				else if (event.getType() == ServiceEvent.UNREGISTERING) {
					lrs.removeLogListener(m_console);
					m_readers.remove(lrs);
				}
			}
		}
    };

	public void start(BundleContext context) throws Exception {
		Activator.context = context;

		// Get a list of all the registered LogReaderService, and add the console listener
		ServiceTracker<LogService, LogReaderService> logReaderTracker = new ServiceTracker<LogService, LogReaderService>(context,
				org.osgi.service.log.LogReaderService.class.getName(), null);
		logReaderTracker.open();
		Object[] readers = logReaderTracker.getServices();
		if (readers != null) {
			for (int i = 0; i < readers.length; i++) {
				LogReaderService lrs = (LogReaderService) readers[i];
				m_readers.add(lrs);
				lrs.addLogListener(m_console);
			}
		}

		//        logReaderTracker.close();

		// Add the ServiceListener, but with a filter so that we only receive events related to LogReaderService
		String filter = "(objectclass=" + LogReaderService.class.getName() + ")";
		try {
			context.addServiceListener(m_servlistener, filter);
		}
		catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static BundleContext getContext() {
        return context;
    }

	public void stop(BundleContext context) throws Exception {
		for (Iterator<LogReaderService> i = m_readers.iterator(); i.hasNext();) {
			LogReaderService lrs = i.next();
			lrs.removeLogListener(m_console);
			i.remove();
		}
		Activator.context = null;
	}

}
