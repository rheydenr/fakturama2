package com.sebulli.fakturama;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;

import com.sebulli.fakturama.resources.urihandler.IconURLStreamHandlerService;

public class LifecycleManager {

    private ECommandService cmdService;
    private EHandlerService handlerService;
    

	@ProcessAdditions
	void postAdditions(IApplicationContext appContext, Display display) {
		// for using of icons from another plugin
		IconURLStreamHandlerService.getInstance().register();
	}
	
//	   @PostContextCreate
//	   public void login(IEclipseContext context) {
//	        cmdService = context.get(ECommandService.class);
//	        handlerService = context.get(EHandlerService.class);
//	        
//	        // FIXME: How could we do this in a Startup class??? This solution looks realy ugly!
//	        ParameterizedCommand command = cmdService.createCommand("com.sebulli.fakturama.firstStart.command", null);
//	        handlerService.executeHandler(command );  // launch ConfigurationManager.checkFirstStart
//	   }
}