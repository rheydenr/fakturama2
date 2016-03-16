/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.addons;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 *
 */
public class ApplicationInitializerWorker implements IRunnableWithProgress {
	protected IProgressMonitor localMonitor;
	protected int   worked = 0;
	private HandlerProcessingAddon handlerAddon;
	
	/**
	 * @param application
	 * @param modelService
	 */
	public ApplicationInitializerWorker(HandlerProcessingAddon handlerAddon) {
		this.handlerAddon = handlerAddon;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException {
        localMonitor = pMonitor;
        worked = 0;
        handlerAddon.initialize(localMonitor);
	}
}
