package com.sebulli.fakturama;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.prefs.BackingStoreException;

import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.UnCefactCodeDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dbservice.IDbUpdateService;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.CEFACTCode;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.preferences.PreferencesInDatabase;
import com.sebulli.fakturama.resources.core.TemplateResourceManager;
import com.sebulli.fakturama.startup.ConfigurationManager;
import com.sebulli.fakturama.startup.ISplashService;

/**
 * The LifecycleManager controls the start and the end of an application.
 *
 */
public class LifecycleManager {

    /**
     * The name of the dialog settings file (value 
     * <code>"dialog_settings.xml"</code>).
     */
    private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml"; //$NON-NLS-1$

    private static final String CODELISTS_XLSX = "codelists.xlsx";

	@Inject
    private IEclipseContext context;

    @Inject
    private ILogger log;

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;
    
    @Inject
    private IDbUpdateService dbUpdateService;

    @Inject
    @Translation
    protected Messages msg;

	private IDialogSettings dialogSettings;

    private static final boolean RESTART_APPLICATION = true;

    private Job dbInitJob;

    @PostContextCreate
    public void checksBeforeStartup(final ISplashService splashService, final IEventBroker eventBroker) {
//        IApplicationContext appContext = context.get(IApplicationContext.class);
    	
    	// Splash Service doesn't work at the moment... :-(
    	
//    	splashService.setSplashPluginId(Activator.PLUGIN_ID);
//    	splashService.open();
//    	splashService.setMessage("Starting Application ...");
    	
//    	// The should be a better way to close the Splash
//    	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376821
//    	eventBroker.subscribe(UIEvents.UILifeCycle.ACTIVATE, new EventHandler() {
//    		@Override
//    		public void handleEvent(Event event) {
//    			splashService.close();
//    			eventBroker.unsubscribe(this);
//    		}
//    	});    	
    	
    	
    	
        log.debug("checks before startup");
        // at first we check if we have to migrate an older version
        // check if the db connection is set
        if (eclipsePrefs.get(PersistenceUnitProperties.JDBC_DRIVER, "") != "") {
        	boolean dbupdate = dbUpdateService.updateDatabase(); // TODO what if this fails???
        	if(!dbupdate) {
        		log.error(null, "could'nt create or update database!");
        		System.exit(1);
        	}
            dbInitJob = new Job("initDb") {
    
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                	
                    log.debug("start DAOs - begin");
                    context.set(VatsDAO.class, ContextInjectionFactory.make(VatsDAO.class, context));
                    context.set(ShippingsDAO.class, ContextInjectionFactory.make(ShippingsDAO.class, context));
                    context.set(PaymentsDAO.class, ContextInjectionFactory.make(PaymentsDAO.class, context));
                    context.set(UnCefactCodeDAO.class, ContextInjectionFactory.make(UnCefactCodeDAO.class, context));
                    log.debug("start DAOs - end");
                    return Status.OK_STATUS;
                }
            };
            dbInitJob.schedule(10);  // timeout that the OSGi env can be started before
            
            // register event handler for saving and closing editors before shutdown
//            eventBroker.subscribe(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED,
//                new EventHandler() {
//                        @Override
//                        public void handleEvent(Event event) {
//                        	// formerly known as Workbench.busyClose()
//                        	closeAndSaveEditors(context);
////                        	eventBroker.unsubscribe(eventHandler)
//                        }
//
//                });            
            
        } else {
            // if db connection is not set, it is a certain sign that the application 
            // is started the first time
            eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new AppStartupCompleteEventHandler(context, RESTART_APPLICATION));
        }
    }

    @PreSave
    public final void closeAndSaveEditors(IEclipseContext context2) {
    	EHandlerService handlerService = context.get(EHandlerService.class);
    	ECommandService commandService = context.get(ECommandService.class);
    	ParameterizedCommand command = commandService.createCommand("org.eclipse.ui.file.closeAll", null);
    	handlerService.executeHandler(command);
	}

    /**
     * Some steps to do before workbench is showing.
     * If a new database was created, fill some data with initial values
     * @throws FakturamaStoringException 
     */
    private void fillWithInitialData() throws FakturamaStoringException {
        // wait some seconds until dbInitJob is finished.
        // else you get a NPE!!!
        try {
            dbInitJob.join();
        }
        catch (InterruptedException e) {
            log.info("ready to go ahead and looking for default values in db.");
        }
        FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;
        VatsDAO vatsDAO = context.get(VatsDAO.class);
        ShippingsDAO shippingsDAO = context.get(ShippingsDAO.class);
        PaymentsDAO paymentsDAO = context.get(PaymentsDAO.class);
        UnCefactCodeDAO unCefactCodeDAO = context.get(UnCefactCodeDAO.class);
        // Fill some default data
        // see old sources: com.sebulli.fakturama.data.Data#fillWithInitialData()
        IPreferenceStore defaultValuesNode = EclipseContextFactory.getServiceContext(Activator.getContext()).get(IPreferenceStore.class);
        
        // Set the default values to this entries
        if(eclipsePrefs.getLong(Constants.DEFAULT_VAT, Long.valueOf(0)) == 0L) {
	        VAT defaultVat = modelFactory.createVAT();
	        defaultVat.setName(msg.dataDefaultVat);
	        defaultVat.setDescription(msg.dataDefaultVatDescription);
	        defaultVat.setTaxValue(Double.valueOf(0.0));
            defaultVat = vatsDAO.save(defaultVat);
//	        defaultValuesNode.setValue(Constants.DEFAULT_VAT, defaultVat.getId());
	        eclipsePrefs.putLong(Constants.DEFAULT_VAT, defaultVat.getId());
        }
        
        if (eclipsePrefs.getLong(Constants.DEFAULT_SHIPPING, Long.valueOf(0)) == 0L) {
	        Shipping defaultShipping = modelFactory.createShipping();
	        defaultShipping.setName(msg.dataDefaultShipping);
	        defaultShipping.setDescription(msg.dataDefaultShippingDescription);
	        defaultShipping.setShippingValue(Double.valueOf(0.0));
	        defaultShipping.setAutoVat(ShippingVatType.SHIPPINGVATGROSS);
	        defaultShipping.setShippingVat(vatsDAO.findById(eclipsePrefs.getLong(Constants.DEFAULT_VAT, Long.valueOf(0))));
            defaultShipping = shippingsDAO.save(defaultShipping);
//            defaultValuesNode.setValue(Constants.DEFAULT_SHIPPING, defaultShipping.getId());
            eclipsePrefs.putLong(Constants.DEFAULT_SHIPPING, defaultShipping.getId());
        }

        if(eclipsePrefs.getLong(Constants.DEFAULT_PAYMENT, Long.valueOf(0)) == 0L) {
	        Payment defaultPayment = modelFactory.createPayment();
	        defaultPayment.setName(msg.dataDefaultPayment);
	        defaultPayment.setDescription(msg.dataDefaultPaymentDescription);
	        defaultPayment.setDiscountValue(Double.valueOf(0.0));
	        defaultPayment.setDiscountDays(Integer.valueOf(0));
	        defaultPayment.setDiscountDays(Integer.valueOf(0));
	        defaultPayment.setPaidText(msg.dataDefaultPaymentPaidtext);
	        defaultPayment.setDepositText(msg.dataDefaultPaymentDescription);
	        defaultPayment.setUnpaidText(msg.dataDefaultPaymentUnpaidtext);
            defaultPayment = paymentsDAO.save(defaultPayment);
//            defaultValuesNode.setValue(Constants.DEFAULT_PAYMENT, defaultPayment.getId());
            eclipsePrefs.putLong(Constants.DEFAULT_PAYMENT, defaultPayment.getId());
        }
        
        // init UN/CEFACT codes
        if(unCefactCodeDAO.getCount() == Long.valueOf(0L)) {
        	initializeCodes(unCefactCodeDAO, modelFactory);
        } 
        
        try {
			eclipsePrefs.flush();
		} catch (BackingStoreException e) {
			log.error(e);
		}
//        preferencesInDatabase.loadPreferencesFromDatabase();
        context.set(IPreferenceStore.class, defaultValuesNode);
        context.getParent().set(IPreferenceStore.class, defaultValuesNode);
        // the DefaultPreferences gets initialized through the calling extension point (which is defined in META-INF).
        // here we have to restore the preference values from database
        PreferencesInDatabase preferencesInDatabase = ContextInjectionFactory.make(PreferencesInDatabase.class, context);
        context.set(PreferencesInDatabase.class, preferencesInDatabase);
    }
    
    /**
     * Initializes all the UN/CEFACT codes which are needed for later ZUGFeRD export.
     * The codes are read from resources bundle (file codelists.xlsx).
     * 
     * Format: Rubrik | Code | Name_en | Name_de | abbrev_en | abbrev_de | Hinweis
     * 
     * @param unCefactCodeDAO the dao
     * @param modelFactory 
     */
    private void initializeCodes(UnCefactCodeDAO unCefactCodeDAO, FakturamaModelFactory modelFactory) {
    	try(InputStream wbStream = FrameworkUtil.getBundle(TemplateResourceManager.class).getResource(CODELISTS_XLSX).openStream();){
    		log.info("importing code lists from " + CODELISTS_XLSX);
    		Workbook wb = new XSSFWorkbook(wbStream);
    		Sheet sheet = wb.getSheetAt(0);
			int rows = sheet.getPhysicalNumberOfRows();
			// skip the first n rows
			int skiprows = 1;  // in case we have somedays more than one header line
			for (int r = skiprows; r < rows; r++) {
				Row row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				
				int i = 0; // column index
				CEFACTCode cEFACTCode = modelFactory.createCEFACTCode();
				
				// TEST ONLY (HSQLDB claims about updates of id field :-(
//				cEFACTCode.setId(r*(-1));
				
				cEFACTCode.setTarget(row.getCell(i++).getStringCellValue());
				cEFACTCode.setCode(row.getCell(i++).getStringCellValue());
				cEFACTCode.setName(getNullSafeCellValue(row.getCell(i++)));
				cEFACTCode.setName_de(getNullSafeCellValue(row.getCell(i++)));
				cEFACTCode.setAbbreviation_en(getNullSafeCellValue(row.getCell(i++)));
				cEFACTCode.setAbbreviation_de(getNullSafeCellValue(row.getCell(i++)));
				cEFACTCode.setValidFrom(Date.from(Instant.now()));
//				cEFACTCode.setDateAdded(Date.from(Instant.now()));
				unCefactCodeDAO.save(cEFACTCode);
			}
		} catch (IOException | FakturamaStoringException e) {
            log.error(e);
		}
	}

	private String getNullSafeCellValue(Cell cell) {
		String retval = null;
		if(cell != null) {
			retval = cell.getStringCellValue();
		}
		return retval;
	}

	@PreDestroy
    public void postWindowClose(@Named(E4Workbench.INSTANCE_LOCATION) Location instanceLocation,
    		EPartService partService) {

		// close all open editors
//		partService.getDirtyParts().forEach((MPart part) -> part.save());
		
        //Closes all OpenOffice documents 
 //       OfficeManager.INSTANCE.closeAll();
        if (context.get(PreferencesInDatabase.class) != null) {
        	// TODO at the moment this is EXTREMELY slow, therefore we have to comment out that
//            context.get(PreferencesInDatabase.class).savePreferencesInDatabase();
//            Data.INSTANCE.close();

            // TODO: Create a database backup
//            BackupManager.createBackup();
        }
        
        if(dialogSettings != null) {
        	saveDialogSettings(instanceLocation);
        }
    }
	
    /**
     * Saves this plug-in's dialog settings.
     * Any problems which arise are silently ignored.
     * @param instanceLocation 
     */
    protected void saveDialogSettings(Location instanceLocation) {
        if (dialogSettings == null) {
            return;
        }

        try {
        	URL path = instanceLocation.getDataArea(Activator.PLUGIN_ID);
        	Path storage = null;
        	if(path == null) {
				return;
			} else {
				storage = Paths.get(path.toURI());
				if(Files.notExists(storage)) {
					Files.createDirectories(storage);
				}
			}

        	storage = storage.resolve(FN_DIALOG_SETTINGS);
            if(Files.notExists(storage)) {
            	Files.createFile(storage);
            }
            dialogSettings.save(storage.toString());
        } catch (IOException | IllegalStateException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @ProcessAdditions
    void processAdditions(final IEventBroker eventBroker, MApplication app, EModelService modelService, IApplicationContext appContext,
    		@Named(E4Workbench.INSTANCE_LOCATION) Location instanceLocation) {
        
    	// TODO put the Login Dialog in here

        ConfigurationManager configMgr = ContextInjectionFactory.make(ConfigurationManager.class, context);
        // launch ConfigurationManager.checkFirstStart
        configMgr.checkAndUpdateConfiguration();
        if (eclipsePrefs.get(ConfigurationManager.GENERAL_WORKSPACE_REQUEST, null) != null) {
            eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, 
            		new AppStartupCompleteEventHandler(context, RESTART_APPLICATION));
            IPreferenceStore defaultValuesNode = EclipseContextFactory.getServiceContext(Activator.getContext()).get(IPreferenceStore.class);
            context.set(IPreferenceStore.class, defaultValuesNode);
            context.getParent().set(IPreferenceStore.class, defaultValuesNode);

        } else {
			try {
				fillWithInitialData();
			}
			catch (FakturamaStoringException sqlex) {
				log.error(sqlex, "couldn't fill with inital data! " + sqlex);
			}
        }
    	
        MTrimmedWindow mainMTrimmedWindow = (MTrimmedWindow) modelService.find("com.sebulli.fakturama.application", app);
        mainMTrimmedWindow.setLabel("Fakturama - " + eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null));
        
        initDialogSettings(instanceLocation);

        // close the static splash screen
        // TODO check if we could call it twice (one call is before Migrationmanager)
        appContext.applicationRunning();
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
    
    
	private IDialogSettings initDialogSettings(Location instanceLocation) {
		if(dialogSettings == null) {
			dialogSettings = loadDialogSettings(instanceLocation);
			context.set(IDialogSettings.class, dialogSettings);
		}
		return dialogSettings;
	}
		
    
    @ProcessRemovals
    public void createOneEditor(EModelService modelService, MApplication app) {
        // if no editor is opened we create a Start Browser part
        MPartStack documentPartStack = (MPartStack) modelService.find(Constants.DETAILPANEL_ID, app);
//        if(documentPartStack.getChildren().isEmpty()) {
//        	EHandlerService handlerService = context.get(EHandlerService.class);
//        	ECommandService commandService = context.get(ECommandService.class);
//        	ParameterizedCommand command = commandService.createCommand("com.sebulli.fakturama.command.openBrowserEditor", null);
//        	handlerService.executeHandler(command);
//        }
    	
    }


    /**
     * Loads the dialog settings for this plug-in.
     * The default implementation first looks for a standard named file in the 
     * plug-in's read/write state area; if no such file exists, the plug-in's
     * install directory is checked to see if one was installed with some default
     * settings; if no file is found in either place, a new empty dialog settings
     * is created. If a problem occurs, an empty settings is silently used.
     * <p>
     * This framework method may be overridden, although this is typically
     * unnecessary.
     * </p>
     * 
     * Borrowed from org.eclipse.ui.plugin.AbstractUIPlugin
     * @param instanceLocation 
     */
    private IDialogSettings loadDialogSettings(Location instanceLocation) {
    	IDialogSettings dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
    	
    	//look for bundle specific dialog settings
        URL dsURL = null;
		try {
			dsURL = instanceLocation.getDataArea(Activator.PLUGIN_ID + "/" + FN_DIALOG_SETTINGS);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        if (dsURL == null) {
			return null;
		}
        
        try(InputStream is = dsURL.openStream();) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "utf-8")); //$NON-NLS-1$
            dialogSettings.load(reader);
        } catch (IOException e) {
            // load failed so ensure we have an empty settings
            dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
        }
		return dialogSettings;
    }
}