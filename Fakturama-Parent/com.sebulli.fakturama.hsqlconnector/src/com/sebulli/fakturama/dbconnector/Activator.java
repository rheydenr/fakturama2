package com.sebulli.fakturama.dbconnector;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private BundleContext fContext;
	private HsqlConnectionProvider service;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		fContext = context;
		service = new HsqlConnectionProvider();

		Hashtable props = new Hashtable();
		// register the service
		context.registerService(IDbConnection.class.getName(), service, props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		service = null;
		fContext = null;
	}


}
