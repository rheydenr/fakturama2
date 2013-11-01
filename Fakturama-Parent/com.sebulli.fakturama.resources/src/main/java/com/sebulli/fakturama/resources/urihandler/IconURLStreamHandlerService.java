/*******************************************************************************
 * Copyright (c) 2012 Marco Descher.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Descher - initial API and implementation
 ******************************************************************************/
package com.sebulli.fakturama.resources.urihandler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

// TODO logging
public class IconURLStreamHandlerService extends
		AbstractURLStreamHandlerService {

	private static IconURLStreamHandlerService instance;
	private ServiceRegistration<URLStreamHandlerService> iconUrlHandler;

	public static IconURLStreamHandlerService getInstance() {
		if (null == instance) {
			instance = new IconURLStreamHandlerService();
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public void register() {
		Bundle bundle = FrameworkUtil
				.getBundle(IconURLStreamHandlerService.class);
		BundleContext bundleContext = bundle.getBundleContext();
		try {
			@SuppressWarnings("rawtypes")
			Hashtable properties = new Hashtable();
			properties.put(URLConstants.URL_HANDLER_PROTOCOL,
					new String[] { "icon" });
			iconUrlHandler = bundleContext.registerService(
					URLStreamHandlerService.class, this, properties);
		} catch (Exception e) {
			LogHelper.logError("Could not register icon URL handler.", e);
		}
		LogHelper.logInfo("Icon URL handler registered.");
	}

	public void unregister() {
		try {
			if (iconUrlHandler != null) {
				iconUrlHandler.unregister();
				iconUrlHandler = null;
			}
		} catch (Exception e) {
			LogHelper.logError("Could not register icon URL handler.", e);
			e.printStackTrace();
		}
	}

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		return new IconURLConnection(u);
	}

}
