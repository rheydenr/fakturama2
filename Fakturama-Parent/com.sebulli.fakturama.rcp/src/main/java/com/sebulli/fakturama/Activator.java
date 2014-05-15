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

package com.sebulli.fakturama;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sebulli.fakturama.dao.VatsDAO;

// import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Gerd Bartelt
 */
public class Activator implements BundleActivator {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.sebulli.fakturama.rcp";

    // The shared instance
    private static BundleContext context;

    private IEclipseContext eContext;

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        eContext = EclipseContextFactory.getServiceContext(bundleContext);

        // launch background job for initializing the db connection
        Job job = new Job("initDb") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                VatsDAO myClassInstance = ContextInjectionFactory.make(VatsDAO.class, eContext);
                eContext.set(VatsDAO.class, myClassInstance);
                return Status.OK_STATUS;
            }
        };
        job.schedule(10);  // timeout that the OSGi env can be started before

        // background color for focused widgets
        JFaceResources.getColorRegistry().put("bgyellow", new RGB(255, 255, 225));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }
}
