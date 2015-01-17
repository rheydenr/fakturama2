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

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.sebulli.fakturama.log.LogbackAdapter;
import com.sebulli.fakturama.misc.Constants;

/**
 * The activator class controls the plug-in life cycle
 * 
 */
public class Activator implements BundleActivator {
	
	/** The plug-in ID */
    public static final String PLUGIN_ID = "com.sebulli.fakturama.common";
    
	/**
	 * If Logging using slf4j API and Logback as backend you can add Marker.
	 * you can build a Marker- Graph - if Marker IS_BUNDLE is contained, then
	 * the Marker Name is the Bundles Symbolic name.
	 */
	public static final String IS_BUNDLE_MARKER = "OSGI_BUNDLE";	 //$NON-NLS-1$

    /** The shared instance */
    private static BundleContext context;
    
    private static Preferences preferences;

    private LogListener logAdapter;
    private LinkedList<LogReaderService> logReaders = new LinkedList<LogReaderService>();
	
	/** The Bundle Marker: a Marker where the name is the osgi bundle symbolic name
	 * and an attached IS_BUNDLE - Marker to guarantee that the Log Framework knows it's a BundleMarker
	 */
	public static final Marker BUNDLE_MARKER = createBundleMarker();
	
	private static final Marker createBundleMarker() {
		Marker bundleMarker = MarkerFactory.getMarker(PLUGIN_ID);
		bundleMarker.add(MarkerFactory.getMarker(IS_BUNDLE_MARKER));
		return bundleMarker;
	}

	/**
	 * We use a ServiceListener to dynamically keep track of all the
	 * LogReaderService service being registered or unregistered
	 */
	private ServiceListener logServlistener = new ServiceListener() {
		public void serviceChanged(ServiceEvent event) {
			BundleContext bc = event.getServiceReference().getBundle().getBundleContext();
			LogReaderService lrs = (LogReaderService) bc.getService(event.getServiceReference());
			if (lrs != null) {
				if (event.getType() == ServiceEvent.REGISTERED) {
					logReaders.add(lrs);
					lrs.addLogListener(logAdapter);
				}
				else if (event.getType() == ServiceEvent.UNREGISTERING) {
					lrs.removeLogListener(logAdapter);
					logReaders.remove(lrs);
				}
			}
		}
	};

	public void start(BundleContext context) throws Exception {
		Activator.context = context;
        logAdapter = new LogbackAdapter();

		// Get a list of all the registered LogReaderService, and add the listener
		ServiceTracker<LogService, LogReaderService> logReaderTracker = new ServiceTracker<LogService, LogReaderService>(context,
				LogReaderService.class.getName(), null);
		logReaderTracker.open();
		Object[] readers = logReaderTracker.getServices();
		if (readers != null) {
			for (int i = 0; i < readers.length; i++) {
				LogReaderService lrs = (LogReaderService) readers[i];
				logReaders.add(lrs);
				lrs.addLogListener(logAdapter);
			}
		}

		// Add the ServiceListener, but with a filter so that we only receive events related to LogReaderService
		String filter = "(objectclass=" + LogReaderService.class.getName() + ")";
		try {
			context.addServiceListener(logServlistener, filter);
		}
		catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		
		// get Preferences
		ServiceReference<IPreferencesService> servRef = context.getServiceReference(IPreferencesService.class);
		preferences = context.getService(servRef).getRootNode().node("/instance/com.sebulli.fakturama.rcp");
		// don't close the tracker, else the logger won't work!
		//		logReaderTracker.close();
	}
	
    /**
     * @return the preferences
     */
    public static Preferences getPreferences() {
        return preferences;
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
		for (Iterator<LogReaderService> i = logReaders.iterator(); i.hasNext();) {
			LogReaderService lrs = i.next();
			lrs.removeLogListener(logAdapter);
			i.remove();
		}
		Activator.context = null;
	}

}
