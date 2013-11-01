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

import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;

import com.sebulli.fakturama.resources.Activator;

public class LogHelper {
	static final private String plugin_name = "com.sebulli.fakturama.resources"; //$NON-NLS-1$

	static public void logError(String msg, Throwable e) {
		log(msg, FrameworkLogEntry.ERROR, e);
	}

	static public void logWarning(String msg, Throwable e) {
		log(msg, FrameworkLogEntry.WARNING, e);
	}

	static public void logInfo(String msg) {
		log(msg, FrameworkLogEntry.INFO, null);
	}

	static public void log(String msg, int severity, Throwable e) {
		FrameworkLog log = Activator.getDefault().getFrameworkLog();
		FrameworkLogEntry logEntry = new FrameworkLogEntry(plugin_name,
				severity, 0, msg, 0, e, null);
		log.log(logEntry);
	}
}
