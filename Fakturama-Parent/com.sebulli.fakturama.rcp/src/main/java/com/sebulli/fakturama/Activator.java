/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama;

import org.osgi.framework.BundleContext;

//import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Gerd Bartelt
 */
public class Activator  { // extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.sebulli.fakturama.rcp";

	// The shared instance
	private static Activator plugin;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
//	@Override
	public void start(BundleContext context) throws Exception {
//		super.start(context);
		plugin = this;
//		ILog logger = Activator.getDefault().getLog();
//		LogListener logListener = new LogListener();
//		logger.addLogListener(logListener);

	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
//	 * )
//	 */
//	@Override
//	public void stop(BundleContext context) throws Exception {
//		plugin = null;
//	//	super.stop(context);
//	}
//
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
//
//	/**
//	 * Returns an image descriptor for the image file at the given plug-in
//	 * relative path
//	 * 
//	 * @param path
//	 *            the path
//	 * @return the image descriptor
//	 */
//	public static ImageDescriptor getImageDescriptor(String path) {
//		return imageDescriptorFromPlugin(PLUGIN_ID, path);
//	}

}
