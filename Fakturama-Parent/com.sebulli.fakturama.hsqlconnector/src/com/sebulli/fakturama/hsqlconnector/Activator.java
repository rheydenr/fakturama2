package com.sebulli.fakturama.hsqlconnector;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sebulli.fakturama.dbconnector.IDbConnection;

public class Activator implements BundleActivator {

	private HsqlConnectionProvider service;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		service = new HsqlConnectionProvider();

		Hashtable<String, String> props = new Hashtable<>();
		// register the service
		context.registerService(IDbConnection.class.getName(), service, props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		service = null;
	}


}
