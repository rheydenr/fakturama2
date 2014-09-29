package com.sebulli.fakturama;

import java.sql.SQLException;

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
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.prefs.Preferences;

import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.resources.ITemplateResourceManager;
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
    
    @Inject
    protected ITemplateResourceManager resourceManager;
 
    private static final boolean RESTART_APPLICATION = true;

    private Job dbInitJob;

    @PostContextCreate
    public void checksBeforeStartup(final IEventBroker eventBroker) {
        log.debug("checks before startup");
        // at first we check if we have to migrate an older version

        // check if the db connection is set
        if (eclipsePrefs.get(PersistenceUnitProperties.JDBC_DRIVER, "") != "") {
            dbInitJob = new Job("initDb") {
    
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    context.set(VatsDAO.class, ContextInjectionFactory.make(VatsDAO.class, context));
                    context.set(ShippingsDAO.class, ContextInjectionFactory.make(ShippingsDAO.class, context));
                    context.set(PaymentsDAO.class, ContextInjectionFactory.make(PaymentsDAO.class, context));
                    return Status.OK_STATUS;
                }
            };
            dbInitJob.schedule(10);  // timeout that the OSGi env can be started before
        } else {
            // if db connection is not set, it is a certain sign that the application 
            // is started the first time
            eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new AppStartupCompleteEventHandler(context, RESTART_APPLICATION));
        }
        // at first create a (temporary) shell
        final Shell shell = new Shell(SWT.TOOL | SWT.NO_TRIM);
        
        // since the ConfigurationManager is started via constructor, there's no injection possible :-(
        // TODO Change it to a service or at least a handler...
        ConfigurationManager configMgr = new ConfigurationManager(shell, context, eclipsePrefs, log, msg, resourceManager);
        // launch ConfigurationManager.checkFirstStart
        configMgr.checkAndUpdateConfiguration();
        if (eclipsePrefs.get(ConfigurationManager.GENERAL_WORKSPACE_REQUEST, null) != null) {
            eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new AppStartupCompleteEventHandler(context, RESTART_APPLICATION));
        } else {
            try {
                fillWithInitialData();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        IApplicationContext appContext = context.get(IApplicationContext.class);
        // close the static splash screen
        // TODO check if we could call it twice (one call is before Migrationmanager)
        appContext.applicationRunning();
    }

    /**
     * Some steps to do before workbench is showing.
     * If a new data base was created, fill some data with initial values
     * @throws SQLException 
     */
    public void fillWithInitialData() throws SQLException {
        // wait some seconds until dbInitJob is finished.
        // else you get a NPE!!!
        try {
            dbInitJob.join();
        }
        catch (InterruptedException e) {
            log.info("ready to go ahead and look for default values in db.");
        }
        
        VatsDAO vatsDAO = context.get(VatsDAO.class);
        ShippingsDAO shippingsDAO = context.get(ShippingsDAO.class);
        PaymentsDAO paymentsDAO = context.get(PaymentsDAO.class);
        // Fill some default data

        // Set the default values to this entries
        Preferences defaultNode = eclipsePrefs.node("/configuration/defaultValues");
        VAT defaultVat = null;
        if(vatsDAO.getCount() == 0L) {
            defaultVat = new VAT();
            defaultVat.setName(msg.dataDefaultVat);
            defaultVat.setDescription(msg.dataDefaultVatDescription);
            defaultVat.setTaxValue(0.0);
            defaultVat.setDeleted(Boolean.FALSE);
            defaultVat = vatsDAO.save(defaultVat);
        }
        if(defaultVat != null && defaultNode.getLong(Constants.DEFAULT_VAT, 0L) == 0L) {
            defaultNode.putLong(Constants.DEFAULT_VAT, defaultVat.getId());
        }
        
        Shipping defaultShipping;
        if(shippingsDAO.getCount() == 0L) {
            defaultShipping = new Shipping();
            defaultShipping.setName(msg.dataDefaultShipping);
            defaultShipping.setDescription(msg.dataDefaultShippingDescription);
            defaultShipping.setShippingValue(0.0);
            defaultShipping.setAutoVat(Boolean.TRUE);
            defaultShipping.setShippingVat(vatsDAO.findById(defaultNode.getLong(Constants.DEFAULT_VAT, 1)));
            defaultShipping.setDeleted(Boolean.FALSE);
            defaultShipping = shippingsDAO.update(defaultShipping);
            defaultNode.putLong(Constants.DEFAULT_SHIPPING, defaultShipping.getId());
        }

        Payment defaultPayment;
        if(paymentsDAO.getCount() == 0L) {
            defaultPayment = new Payment();
            defaultPayment.setName(msg.dataDefaultPayment);
            defaultPayment.setDescription(msg.dataDefaultPaymentDescription);
            defaultPayment.setDiscountValue(0.0);
            defaultPayment.setDiscountDays(0);
            defaultPayment.setDiscountDays(0);
            defaultPayment.setPaidText(msg.dataDefaultPaymentPaidtext);
            defaultPayment.setDepositText(msg.dataDefaultPaymentDescription);
            defaultPayment.setUnpaidText(msg.dataDefaultPaymentUnpaidtext);
            defaultPayment.setDefaultPaid(Boolean.FALSE);
            defaultPayment.setDeleted(Boolean.FALSE);
            defaultPayment = paymentsDAO.save(defaultPayment);
            defaultNode.putLong(Constants.DEFAULT_PAYMENT, defaultPayment.getId());
        }
        
// now, initialize some other preferences ==> das macht schon der Extension Point!
//        DefaultValuesInitializer defaultValuesInitializer = new DefaultValuesInitializer(log);
//        defaultValuesInitializer.initializeDefaultPreferences();
    }

    @ProcessAdditions
    void processAdditions(MApplication app, EModelService modelService) {
        MTrimmedWindow mainMTrimmedWindow = (MTrimmedWindow) modelService.find("com.sebulli.fakturama.application", app);
        mainMTrimmedWindow.setLabel("Fakturama - " + eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null));
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