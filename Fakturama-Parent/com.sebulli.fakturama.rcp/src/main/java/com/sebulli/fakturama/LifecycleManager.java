package com.sebulli.fakturama;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.IHandlerService;
import org.osgi.service.prefs.Preferences;

import com.sebulli.fakturama.resources.urihandler.IconURLStreamHandlerService;

public class LifecycleManager {

    private ECommandService cmdService;
    
    private IHandlerService handlerService;

	@ProcessAdditions
	void postAdditions(IApplicationContext appContext, Display display, IEclipseContext context, MApplication application) {

        // for using of icons from another plugin
		IconURLStreamHandlerService.getInstance().register();
//	        cmdService = context.get(ECommandService.class);
//	        handlerService = context.get(EHandlerService.class);
//	        // better than in CoolbarViewPart...
//	        ParameterizedCommand command = cmdService.createCommand("com.sebulli.fakturama.firstStart.command", null);
//	        handlerService.executeHandler(command );  // launch ConfigurationManager.checkFirstStart
	    
	}
	
	   @PostContextCreate
	   public void login(IEclipseContext eContext) {
	       // wanted: IEclipsePreferences preferences
//        IEclipseContext eContext = EclipseContextFactory.getServiceContext(Activator.getContext()).get(IEclipsePreferences.class);
System.out.println("huhu");
        // launch background job for initializing the db connection
        // but only if we are in "normal" mode and not just installing a new workspace...
//        if(eContext.get(PreferencesService.class).getUserPreferences(PLUGIN_ID).get("javax.persistence.jdbc.driver", "") != "") {
//            Job job = new Job("initDb") {
//    
//                @Override
//                protected IStatus run(IProgressMonitor monitor) {
//                    VatsDAO myClassInstance = ContextInjectionFactory.make(VatsDAO.class, eContext);
//                    eContext.set(VatsDAO.class, myClassInstance);
//                    return Status.OK_STATUS;
//                }
//            };
//            job.schedule(10);  // timeout that the OSGi env can be started before
//        }
	   }
}