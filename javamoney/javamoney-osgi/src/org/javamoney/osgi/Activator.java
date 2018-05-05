package org.javamoney.osgi;

import javax.money.spi.Bootstrap;
import javax.money.spi.ServiceProvider;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext fContext;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		fContext = context;
		
//		Collection<ServiceReference<MonetaryAmountFormatProviderSpi>> serviceReferences = context.getServiceReferences(MonetaryAmountFormatProviderSpi.class, null);
//		serviceReferences.forEach(s -> System.out.println(s.getProperty("component.name")));
		ServiceProvider sp = new OsgiServiceProvider();

		Bootstrap.init(sp);
	}

	/**
	 * @return the fContext
	 */
	public static BundleContext getBundleContext() {
		return fContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		fContext = null;
	}

}
