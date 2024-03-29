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
//import javax.inject.Provider;

import org.apache.commons.lang3.ClassUtils;
import org.eclipse.equinox.log.ExtendedLogService;
import org.osgi.service.log.LogLevel;
//import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.osgi.service.log.LogService;

import ch.qos.logback.classic.spi.CallerData;

/**
 * A wrapper class for the Fakturama logger. This Logger delegates all calls
 * to the {@link LogService}, which then calls the {@link LogbackAdapter} for
 * the "real" logging (done with SLF4J and LogBack).
 */
public class FakturamaLogger implements ILogger {

    @Inject
	private ExtendedLogService delegate;  
    
//    // TODO prove to use this
//    @Inject
//    private Provider<StatusReporter> statusReporter;

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.log.ILogger#debug(java.lang.String)
	 */
	@Override
	public void debug(String message) {
		log(LogLevel.DEBUG, message);
	}
	
	private void log(LogLevel level, String message) {
		if(delegate != null) {
			message = extractMessageWithCaller(message);
			switch (level) {
            case DEBUG:
                this.delegate.debug(message);
                break;
            case INFO:
                this.delegate.info(message);
                break;
            case WARN:
                this.delegate.warn(message);
                break;
            case ERROR:
                this.delegate.error(message);
                break;

            default:
    			// fallback
    			System.out.println(message);
                break;
            }
		} else {
			// fallback
			System.out.println(message);
		}
	}

	private String extractMessageWithCaller(String message) {
        StackTraceElement[] caller = CallerData.extract(new Throwable(), this.getClass().getName(), 1, null);
		if (caller != null && caller.length > 0) {
			message = String.format("%s.%s:%d|%s", ClassUtils.getAbbreviatedName(caller[0].getClassName(), 15),
					caller[0].getMethodName(), caller[0].getLineNumber(), message);
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.log.ILogger#info(java.lang.String)
	 */
	@Override
	public void info(String message) {
		log(LogLevel.INFO, message);
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.log.ILogger#warn(java.lang.String)
	 */
	@Override
	public void warn(String message) {
		log(LogLevel.WARN, message);
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.log.ILogger#error(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void error(Throwable exception, String message) {
		this.delegate.error(message, exception);
	}

	@Override
	public void error(Throwable exception) {
		delegate.error("Exception occured: ", exception);
	}

	@Override
	public void error(String message) {
		log(LogLevel.ERROR, message);
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
	public void setDelegate(ExtendedLogService delegate) {
		this.delegate = delegate;
	}
	/**
	 * @param delegate the delegate to set
	 */
	public void unsetDelegate(LogService delegate) {
		this.delegate = null;
	}

	@Override
	public boolean isDebugEnabled() {
		return this.delegate.isDebugEnabled();
	}
}
