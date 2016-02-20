package com.sebulli.fakturama.model;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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

/**
 *
 */
public class ModelActivator implements BundleActivator {

    /** The shared instance */
    private static BundleContext context;
    
    private static ModelActivator instance;
    
	private ModelActivator(BundleContext context2) {
		this.context = context2;
	}



	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("huhu");
		ModelActivator.context = context;
		instance = new ModelActivator(context);
	}
//	
//	public void startDao(IEclipseContext ctx) {
//		ContextInjectionFactory.make(ContactsDAO.class, ctx);
//	}
//
	/**
	 * @return the context
	 */
	public static final BundleContext getContext() {
		return context;
	}



	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		ModelActivator.context = null;
	}



	/**
	 * @return the instance
	 */
	public static final ModelActivator getInstance() {
		return instance;
	}

}
