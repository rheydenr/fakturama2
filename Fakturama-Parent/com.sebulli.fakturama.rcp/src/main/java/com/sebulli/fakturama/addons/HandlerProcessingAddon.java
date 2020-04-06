package com.sebulli.fakturama.addons;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.handlers.paramconverter.BooleanParameterValueConverter;
import com.sebulli.fakturama.handlers.paramconverter.DocumentParameterConverter;
import com.sebulli.fakturama.handlers.paramconverter.NumberParameterValueConverter;

/**
 * Process the additions and removals of handlers on the model
 */
public class HandlerProcessingAddon {
    
    @Inject
    private CommandManager cmdMan;    	

    @Inject
    private IEclipseContext context;
	private MApplication application;
	private EModelService modelService;
	
	/**
	 * Do initial check of handlers and their context upon creation
	 *
	 * @param application
	 * @param modelService
	 */
	@PostConstruct
	public void postConstruct(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent, MApplication application, 
			EModelService modelService, IEventBroker eventBroker) {
		this.application = application;
		this.modelService = modelService;
		
		defineAdditionalParameterTypes();
		
        ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
        IRunnableWithProgress op = new IRunnableWithProgress() {
        	public void run(IProgressMonitor monitor) throws InvocationTargetException ,InterruptedException {
        		initialize(monitor);        		
        	};
        };
        try {
        	// this prevents the ProgressMonitorDialog from opening
        	// (since we have the progress already on splash screen)
        	progressMonitorDialog.setOpenOnRun(false);
			progressMonitorDialog.run(true, false, op);
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * Per default all parameter types are Strings. To change this we have to register additional parameter types
	 * (which corresponds to existing Java classes). 
	 */
	private void defineAdditionalParameterTypes() {
        ParameterType parameterType = cmdMan.getParameterType("com.sebulli.fakturama.model.Order");
        parameterType.define("com.sebulli.fakturama.model.Order", ContextInjectionFactory.make(DocumentParameterConverter.class, context));
		
        ParameterType parameterTypeInteger = cmdMan.getParameterType("java.lang.Integer");
        parameterTypeInteger.define("java.lang.Integer", new NumberParameterValueConverter());
		
        ParameterType parameterTypeBoolean = cmdMan.getParameterType("java.lang.Boolean");
        parameterTypeBoolean.define("java.lang.Boolean", new BooleanParameterValueConverter());
	}

	protected void initialize(IProgressMonitor pMonitor) {
		List<MHandlerContainer> findElements = modelService.findElements(application, null,
				MHandlerContainer.class, null);
		for (MHandlerContainer mHandlerContainer : findElements) {
			if (mHandlerContainer instanceof MContext) {
				MContext mContext = (MContext) mHandlerContainer;
				IEclipseContext context = mContext.getContext();
				if (context != null) {
					for (MHandler mHandler : mHandlerContainer.getHandlers()) {
						processActiveHandler(mHandler, context);
					}
				}
			}
		}
	}
	
	
	/**
	 * Responds to the coming and goings of handlers in the application model by activating and
	 * deactivating them accordingly.
	 *
	 * @param event
	 *            The event thrown in the event bus
	 */
	@Inject
	public void handleHandlerEvent(
			@Optional @EventTopic(UIEvents.HandlerContainer.TOPIC_HANDLERS) Event event) {
		if (event == null)
			return; 
		if ((event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MHandlerContainer)
				&& (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MContext)) {
			MHandlerContainer handlerContainer = (MHandlerContainer) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
				if (event.getProperty(UIEvents.EventTags.NEW_VALUE) instanceof MHandler) {
					MHandler handler = (MHandler) event.getProperty(UIEvents.EventTags.NEW_VALUE);
					MContext mContext = (MContext) handlerContainer;
					IEclipseContext context = mContext.getContext();
					if (context != null) {
						processActiveHandler(handler, context);
					}
				}
			} else if (UIEvents.EventTypes.REMOVE
					.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
				if (event.getProperty(UIEvents.EventTags.OLD_VALUE) instanceof MHandler) {
					MHandler handler = (MHandler) event.getProperty(UIEvents.EventTags.OLD_VALUE);
					MContext mContext = (MContext) handlerContainer;
					IEclipseContext context = mContext.getContext();
					if (context != null) {
						MCommand command = handler.getCommand();
						if (command != null) {
							String commandId = command.getElementId();
							EHandlerService handlerService = (EHandlerService) context
									.get(EHandlerService.class.getName());
							handlerService.deactivateHandler(commandId, handler.getObject());
						}
					}
				}

			}

		}

	}

	/**
	 * Responds to the setting of contexts of handlers in the application model and reacts
	 * accordingly.
	 *
	 * @param event
	 *            The event which signals the setting of the context.
	 */

	@Inject
	public void handleContextEvent(@Optional @EventTopic(UIEvents.Context.TOPIC_CONTEXT) Event event) {
		if (event == null)
			return;
		Object origin = event.getProperty(UIEvents.EventTags.ELEMENT);
		Object context = event.getProperty(UIEvents.EventTags.NEW_VALUE);
		if ((origin instanceof MHandlerContainer)
				&& (UIEvents.EventTypes.SET.equals(event.getProperty(UIEvents.EventTags.TYPE)) && context instanceof IEclipseContext)) {
			MHandlerContainer handlerContainer = (MHandlerContainer) origin;
			IEclipseContext castedContext = (IEclipseContext) context;
			for (MHandler mHandler : handlerContainer.getHandlers()) {
				processActiveHandler(mHandler, castedContext);
			}

		}
	}

	/**
	 * @param handler
	 * @param context
	 */
	private void processActiveHandler(MHandler handler, IEclipseContext context) {
		MCommand command = handler.getCommand();
		if (command != null) {
			String commandId = command.getElementId();
			if (handler.getObject() == null) {
				IContributionFactory contributionFactory = (IContributionFactory) context
						.get(IContributionFactory.class.getName());
				handler.setObject(contributionFactory.create(handler.getContributionURI(), context));
			}
			EHandlerService handlerService = (EHandlerService) context.get(EHandlerService.class
					.getName());
			handlerService.activateHandler(commandId, handler.getObject());
		}
	}

}