package com.sebulli.fakturama;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.sebulli.fakturama.dao.PropertiesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.startup.ConfigurationManager;

/**
 * The LifecycleManager controls the start and the end of an application.
 * @author rheydenr
 *
 */
public class LifecycleManager {

    @Inject
    private IEclipseContext context;

    @Inject
    private Logger log;

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;
    
    @Inject
    @Translation
    protected Messages msg;
 
    private static final boolean RESTART_APPLICATION = true;

    @ProcessAdditions
    public void checksBeforeStartup(final IEventBroker eventBroker) {
        log.debug("checks before startup");
        // at first we check if we have to migrate an older version

        // check if the db connection is set
        // if not, it is a certain sign that the application is started the first time
        if (eclipsePrefs.get(PersistenceUnitProperties.JDBC_DRIVER, "") != "") {
            // launch background job for initializing the db connection
            // but only if we are in "normal" mode and not just installing a new workspace...
            Job job = new Job("initDb") {
    
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    VatsDAO myClassInstance = ContextInjectionFactory.make(VatsDAO.class, context);
                    context.set(VatsDAO.class, myClassInstance);
                    return Status.OK_STATUS;
                }
            };
            job.schedule(10);  // timeout that the OSGi env can be started before
        } else {
            // for later restarting
            eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new AppStartupCompleteEventHandler(context, RESTART_APPLICATION));
        }
        // at first create a (temporary) shell
        final Shell shell = new Shell(SWT.TOOL | SWT.NO_TRIM);
//        handlerService = ContextInjectionFactory.make(EHandlerService.class, context);
//        cmdService = ContextInjectionFactory.make(ECommandService.class, context);
        ConfigurationManager configMgr = new ConfigurationManager(shell, context, eclipsePrefs, log, msg);
        // launch ConfigurationManager.checkFirstStart
        configMgr.checkFirstStart(null, null);
        
        postInitialize();
    }
    
    /**
     * Some steps to do before workbench is showing.
     */
    private void postInitialize() {
        PropertiesDAO myClassInstance = ContextInjectionFactory.make(PropertiesDAO.class, context);
        context.set(PropertiesDAO.class, myClassInstance);
        context.get(PropertiesDAO.class).findAll();
    }
    
    /**
     * Because we don't have a complete workbench at this stage, the
     * {@link EventHandler} is registered so that we can restart the application
     * if the working directory has changed or the application is launched the
     * first time. 
     *
     */
    private static final class AppStartupCompleteEventHandler implements EventHandler {
        private final IEclipseContext _context;
        private final boolean restartApplication;

        AppStartupCompleteEventHandler(final IEclipseContext context, boolean restartApplication) {
            _context = context;
            this.restartApplication = restartApplication;
        }

        @Override
        public void handleEvent(final Event event) {
            IWorkbench workbench = _context.get(IWorkbench.class);
            if (restartApplication) {
                workbench.restart();
            }
        }
    }

}