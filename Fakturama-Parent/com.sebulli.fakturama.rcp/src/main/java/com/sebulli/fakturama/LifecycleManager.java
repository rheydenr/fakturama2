package com.sebulli.fakturama;

import java.sql.SQLException;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.opcoach.e4.preferences.ScopedPreferenceStore;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.resources.ITemplateResourceManager;
import com.sebulli.fakturama.startup.ConfigurationManager;

/**
 * The LifecycleManager controls the start and the end of an application.
 *
 */
public class LifecycleManager {

    @Inject
    private IEclipseContext context;
    
    @Inject
    private ILogger log;

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
        IApplicationContext appContext = context.get(IApplicationContext.class);  
        log.debug("checks before startup");
        // at first we check if we have to migrate an older version
        // check if the db connection is set
        if (eclipsePrefs.get(PersistenceUnitProperties.JDBC_DRIVER, "") != "") {
            dbInitJob = new Job("initDb") {
    
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    log.debug("start DAOs - begin");
                    context.set(VatsDAO.class, ContextInjectionFactory.make(VatsDAO.class, context));
                    context.set(ShippingsDAO.class, ContextInjectionFactory.make(ShippingsDAO.class, context));
                    context.set(PaymentsDAO.class, ContextInjectionFactory.make(PaymentsDAO.class, context));
                    log.debug("start DAOs - end");
                    return Status.OK_STATUS;
                }
            };
            dbInitJob.schedule(10);  // timeout that the OSGi env can be started before
        } else {
            // if db connection is not set, it is a certain sign that the application 
            // is started the first time
            eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new AppStartupCompleteEventHandler(context, RESTART_APPLICATION));
        }
        
        // TODO Change it to a service or at least a handler...
        ConfigurationManager configMgr = ContextInjectionFactory.make(ConfigurationManager.class, context);
        // launch ConfigurationManager.checkFirstStart
        configMgr.checkAndUpdateConfiguration();
        if (eclipsePrefs.get(ConfigurationManager.GENERAL_WORKSPACE_REQUEST, null) != null) {
            eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, 
            		new AppStartupCompleteEventHandler(context, RESTART_APPLICATION));
        } else {
			try {
				fillWithInitialData();
			}
			catch (SQLException sqlex) {
				log.error(sqlex, "couldn't fill with inital data! " + sqlex);
			}
        }

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
        FakturamaModelFactory modelFactory = new FakturamaModelFactory();
        VatsDAO vatsDAO = context.get(VatsDAO.class);
        ShippingsDAO shippingsDAO = context.get(ShippingsDAO.class);
        PaymentsDAO paymentsDAO = context.get(PaymentsDAO.class);
        // Fill some default data
        // see old sources: com.sebulli.fakturama.data.Data#fillWithInitialData()
        
        IPreferenceStore defaultValuesNode = new ScopedPreferenceStore(InstanceScope.INSTANCE, String.format("/%s/%s", InstanceScope.SCOPE, Activator
                .getContext().getBundle().getSymbolicName()),
                Constants.DEFAULT_PREFERENCES_NODE);
        context.set(IPreferenceStore.class, defaultValuesNode);
        context.getParent().set(IPreferenceStore.class, defaultValuesNode);
        // Set the default values to this entries
        VAT defaultVat = modelFactory.createVAT(); //defaultValuesNode.getString(Constants.DEFAULT_VAT);
        defaultVat.setName(msg.dataDefaultVat);
        defaultVat.setDescription(msg.dataDefaultVatDescription);
        defaultVat.setTaxValue(Double.valueOf(0.0));
        if(vatsDAO.getCount() == Long.valueOf(0L)) {
            defaultVat = vatsDAO.save(defaultVat);
        } else if(defaultValuesNode.getLong(Constants.DEFAULT_VAT) == Long.valueOf(0L)) {
            defaultVat = vatsDAO.findOrCreate(defaultVat);
        }
        if(defaultVat != null && defaultValuesNode.getLong(Constants.DEFAULT_VAT) == Long.valueOf(0L)) {
            defaultValuesNode.setValue(Constants.DEFAULT_VAT, defaultVat.getId());
        }
        
        Shipping defaultShipping = modelFactory.createShipping();
        defaultShipping.setName(msg.dataDefaultShipping);
        defaultShipping.setDescription(msg.dataDefaultShippingDescription);
        defaultShipping.setShippingValue(Double.valueOf(0.0));
        defaultShipping.setAutoVat(ShippingVatType.SHIPPINGVATGROSS);
        defaultShipping.setShippingVat(vatsDAO.findById(defaultValuesNode.getLong(Constants.DEFAULT_VAT)));
        if (shippingsDAO.getCount() == Long.valueOf(0L)) {
            defaultShipping = shippingsDAO.save(defaultShipping);
        } else if(defaultValuesNode.getLong(Constants.DEFAULT_SHIPPING) == Long.valueOf(0L)) {
            defaultShipping = shippingsDAO.findOrCreate(defaultShipping);
        }
        if(defaultShipping != null && defaultValuesNode.getLong(Constants.DEFAULT_VAT) == Long.valueOf(0L)) {
            defaultValuesNode.setValue(Constants.DEFAULT_SHIPPING, defaultShipping.getId());
        }

        Payment defaultPayment;
        if(paymentsDAO.getCount() == Long.valueOf(0L)) {
            defaultPayment = new Payment();
            defaultPayment.setName(msg.dataDefaultPayment);
            defaultPayment.setDescription(msg.dataDefaultPaymentDescription);
            defaultPayment.setDiscountValue(Double.valueOf(0.0));
            defaultPayment.setDiscountDays(Integer.valueOf(0));
            defaultPayment.setDiscountDays(Integer.valueOf(0));
            defaultPayment.setPaidText(msg.dataDefaultPaymentPaidtext);
            defaultPayment.setDepositText(msg.dataDefaultPaymentDescription);
            defaultPayment.setUnpaidText(msg.dataDefaultPaymentUnpaidtext);
            defaultPayment = paymentsDAO.save(defaultPayment);
            defaultValuesNode.setValue(Constants.DEFAULT_PAYMENT, defaultPayment.getId());
        }
        // the DefaultPreferences gets initialized through the calling extension point (which is defined in META-INF).
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