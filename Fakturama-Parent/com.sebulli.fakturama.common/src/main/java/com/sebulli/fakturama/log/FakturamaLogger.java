/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.log;

import javax.inject.Inject;

import org.osgi.service.log.LogService;

/**
 * @author rheydenr
 *
 */
public class FakturamaLogger implements ILogger {
	
	@Inject
	private LogService delegate;   

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.log.ILogger#debug(java.lang.String)
	 */
	@Override
	public void debug(String message) {
		delegate.log(LogService.LOG_DEBUG, message);
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.log.ILogger#info(java.lang.String)
	 */
	@Override
	public void info(String message) {
		delegate.log(LogService.LOG_INFO, message);
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.log.ILogger#warn(java.lang.String)
	 */
	@Override
	public void warn(String message) {
		delegate.log(LogService.LOG_WARNING, message);
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.log.ILogger#error(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void error(Throwable exception, String message) {
		delegate.log(LogService.LOG_ERROR, message, exception);
	}

	public void error(Throwable exception) {
		delegate.log(LogService.LOG_ERROR, "Exception occured: ", exception);
	}

	/**
	 * @return the delegate
	 */
	public LogService getDelegate() {
		return delegate;
	}

	/**
	 * @param delegate the delegate to set
	 */
	public void setDelegate(LogService delegate) {
		this.delegate = delegate;
	}
	/**
	 * @param delegate the delegate to set
	 */
	public void unsetDelegate(LogService delegate) {
		this.delegate = null;
	}


}
