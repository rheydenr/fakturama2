package com.sebulli.fakturama.log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.services.events.EventBrokerFactory;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.misc.Constants;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * The LogbackAdaptor converts the LogEntry objects it receives into calls to
 * the slf4j loggers (the native interface of logback).
 * <p> The error messages are written into a
 * log file and displayed in an error view in the workbench.
 * 
 * <p>see <a href="https://code.google.com/p/osgi-logging/w/list">OSGi logging-related tools and documentation</a></p>
 * 
 */
public class LogbackAdapter implements LogListener {    

    private IEventBroker eventBroker;
    
    /**
     * The token for replacing the file name in a <tt>logback.xml</tt> file.
     */
	public static final String LOG_FILE_NAME_TOKEN = "logFileName";
	
	/**
	 * A {@link Map} of all valid {@link Logger}s.
	 */
    Map<Long, Logger> loggers = new HashMap<Long, Logger>();

    /**
     * Default constructor where initializing takes place.
     * 
     */
	public LogbackAdapter() {
		/*
		 * We have to put the log file into {workspace}/Log directory. However,
		 * we DON'T know at this point if the user has switched the workspace,
		 * if the application is started the first time or 
		 */
		String productName = System.getProperty(InternalPlatform.PROP_PRODUCT).replaceAll("\\.product", "");
		String workspaceLoc = InstanceScope.INSTANCE.getNode(productName).get(Constants.GENERAL_WORKSPACE, null);
		String logFile = getLogfileName(workspaceLoc);
		if (StringUtils.isNotBlank(logFile)) {
			// determine the configuration file location
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			// if a workspace is set we can adapt the log configuration file location
			String defaultLogConfigFileName;
			// at first check if the configuration is set via a switch
			if (System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY) != null) {
				defaultLogConfigFileName = System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
			} else {
				defaultLogConfigFileName = workspaceLoc;
			}
			defaultLogConfigFileName += "/" + ContextInitializer.AUTOCONFIG_FILE;

			try {
			    if(StringUtils.isNotBlank(defaultLogConfigFileName)) {
			        Path defaultLogConfigFile = Paths.get(defaultLogConfigFileName);
					if (!Files.exists(defaultLogConfigFile)) {
    					// oh, there's no configuration file... 
    					// then we create it from our own template
    				    // 
    				    // this doesn't work
//    				    Activator.getContext().getBundle().getEntry("/logback.template.xml").openStream();
    					InputStream templateStream = LogbackAdapter.class.getClassLoader().getResourceAsStream("/logback.template.xml");
    					Files.copy(templateStream, defaultLogConfigFile);
    				}
    				JoranConfigurator jc = new JoranConfigurator();
    				jc.setContext(loggerContext);
    				loggerContext.putProperty(LOG_FILE_NAME_TOKEN, logFile);
    				// now try to set this log file 
    				jc.doConfigure(defaultLogConfigFileName);    		        
			    }
			}
			catch (JoranException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This methods is called by the LogReaderService and dispatches all calls
	 * to the appropriate SLF4J loggers. 
	 */
	public void logged(LogEntry log) {
		if (log.getBundle() == null || log.getBundle().getSymbolicName() == null) {
			// if there is no name, it's probably the framework emitting a log
			// This should not happen and we don't want to log something anonymous
			return;
		}

		// Retrieve a Logger object, or create it if none exists.
		Logger logger = loggers.get(log.getBundle().getBundleId());
		if (logger == null) {
			logger = LoggerFactory.getLogger(log.getBundle().getSymbolicName());
			loggers.put(log.getBundle().getBundleId(), logger);
		}		

		// Show the error view (only if it is not just an information message)
		if (log.getLevel() == LogService.LOG_ERROR) {
			if(!showErrorView(log)) {
			    logger.error(Activator.BUNDLE_MARKER, "Can't show the error message in Error View because no EventBroker is available!");
			}
		}

		// If there is an exception available, use it, otherwise just log 
		// the message
		if (log.getException() != null) {
			switch (log.getLevel()) {
			case LogService.LOG_DEBUG:
				logger.debug(log.getMessage(), log.getException());
				break;
			case LogService.LOG_INFO:
				logger.info(log.getMessage(), log.getException());
				break;
			case LogService.LOG_WARNING:
				logger.warn(log.getMessage(), log.getException());
				break;
			case LogService.LOG_ERROR:
				logger.error(log.getMessage(), log.getException());
				break;
			}
		} else {
			String message = filter(log.getMessage());
			if(message != null) {
				switch (log.getLevel()) {
				case LogService.LOG_DEBUG:
					logger.debug(Activator.BUNDLE_MARKER, message);
					break;
				case LogService.LOG_INFO:
					logger.info(Activator.BUNDLE_MARKER, message);
					break;
				case LogService.LOG_WARNING:
					logger.warn(Activator.BUNDLE_MARKER, message);
					break;
				case LogService.LOG_ERROR:
					logger.error(Activator.BUNDLE_MARKER, message);
					break;
				}
			}
		}
	}

	/**
	 * Filter unwanted messages.
	 *
	 * @param message the message
	 */
	private String filter(String message) {
	    String filteredMessage = message;
	    if(message.startsWith("BundleEvent")) filteredMessage = null;
	    if(message.startsWith("ServiceEvent")) filteredMessage = null;
		return filteredMessage;
	}

	/**
	 * Shows the error view and sets the error text.
	 * We post a message to all listeners. If one of them is responsible for showing
	 * a {@link LogEntry}, it grabs this {@link LogEntry} and displays it.
	 * 
	 * At the moment this is the ErrorView from com.sebulli.fakturama.rcp bundle.
	 */
    private boolean showErrorView(LogEntry log) {
        boolean retval = true;
        if (getEventBroker() != null) {
            getEventBroker().post("Log/Error", log);
        } else {
            retval = false;
        }
        return retval;
    }

	/**
	 * Return the name of the log file.
	 * @param workspaceLoc 
	 * 
	 * @return Name of the log file or an empty string, if workspace is not set
	 */
	private String getLogfileName(final String workspaceLoc) {
		// Do not save log files, if there is no workspace set
		if (StringUtils.isBlank(workspaceLoc)) { return ""; }

		// Do not save log files if workspace is not created
		Path directory = Paths.get(workspaceLoc);
		if (Files.notExists(directory)) { return ""; }

		// Create a sub folder "Log" if it does not exist yet.
		try {
			directory = Files.createDirectories(directory.resolve("Log/"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// Name of the log file
		directory = directory.resolve("Error");  // .log is substituted in the configuration file, as there's a date before it

		return directory.toString();
	}

    /**
     * @return the eventBroker
     */
    public IEventBroker getEventBroker() {
        if(eventBroker == null) {
            
            /* FIXME
             * Ok - I know, you shouldn't do this (I know it, really!). But in this case I've no other chance to get
             * the EventBroker service. If I use the "official" way like 
             * 
             * IEclipseContext eclipseContext = EclipseContextFactory.getServiceContext(Activator.getContext());
             * IEventBroker eventBroker = (IEventBroker) eclipseContext.get(IEventBroker.class);
             * 
             * or 
             * 
             * EventBrokerFactory eventBrokerFactory = new EventBrokerFactory();
             * eventBrokerFactory.compute(wb.getContext(), null);
             * 
             * I get an InjectionException because a Logger could not be found
             * in the given context. It's only in the Workbench context. But the
             * IWorkbench interface doesn't have a getContext() method. Therefore 
             * I use the (internal) E4Workbench class. It works for the moment, but
             * if anybody out there has an idea for getting the EventBroker the 
             * right way please let me know. You are welcome!
             */
            
            ServiceReference<IWorkbench> serviceReference = Activator.getContext().getServiceReference(IWorkbench.class);
            if(serviceReference != null) {
                E4Workbench wb = (E4Workbench) Activator.getContext().getService(serviceReference);
                EventBrokerFactory eventBrokerFactory = new EventBrokerFactory();
                setEventBroker((IEventBroker) eventBrokerFactory.compute(wb.getContext(), null));
            }
        }
        return eventBroker;
    }

    /**
     * @param eventBroker the eventBroker to set
     */
    public void setEventBroker(IEventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }
}