/**
 * 
 */
package com.sebulli.fakturama.ui.dialogs.about.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author mi32536
 *
 */
public class DialogPlugin implements BundleActivator {

	public static final String ID = "org.eclipse.e4.ui.about.dialog";
	private static BundleContext context;

	@Override
	public void start(BundleContext context) throws Exception {
		DialogPlugin.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		DialogPlugin.context = null;
	}

	public static BundleContext getContext() {
		return context;
	}

}
