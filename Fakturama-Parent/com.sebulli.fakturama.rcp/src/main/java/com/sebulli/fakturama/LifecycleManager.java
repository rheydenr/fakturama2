package com.sebulli.fakturama;

import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;

import com.sebulli.fakturama.resources.urihandler.IconURLStreamHandlerService;

public class LifecycleManager {

	@ProcessAdditions
	void postAdditions(IApplicationContext appContext, Display display) {
		// for using of icons from another plugin
		IconURLStreamHandlerService.getInstance().register();
	}
}