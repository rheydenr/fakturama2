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


/**
 * A wrapper class for the Fakturama logger. This Logger delegates all calls
 * to the LogService, which then calls the {@link LogbackAdapter} for
 * the "real" logging (done with SLF4J and Logback). <br>
 * <p>This interface is for using with DI since it is exposed as a service via 
 * a service component descriptor.  
 */
public interface ILogger {
	
    /**
     * Logs a DEBUG message.
     * @param message the message to log
     */
	public void debug(String message);

	/**
     * Logs an INFO message.
     * @param message the message to log
	 */
	public void info(String message);

	/**
     * Logs a WARN message.
     * @param message the message to log
	 */
	public void warn(String message);
	
	/**
     * Logs an ERROR message and the causing throwable.
     * @param message the message to log
	 * @param exception the causing throwable
	 */
	public void error(Throwable exception, String message);

	/**
     * Logs a causing throwable.
     * @param exception the causing throwable
     */
	public void error(Throwable exception);

	void error(String message);

	boolean isDebugEnabled();

}
