package com.sebulli.fakturama.addons;

import javax.annotation.PostConstruct;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * This AddOn creates a handler which listens to closing and switching of parts. This triggers a new message
 * which updates the CoolBar items. It is registered as AddOn in the Application model.
 */
public class ClosePartAddon {

	private EventHandler eventHandler;

	@PostConstruct
	public void pc(IEventBroker eventBroker) {
		eventHandler = new EventHandler() {

			@Override
			public void handleEvent(Event event) {
				Object part = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (part instanceof MPart && ((MPart) part).getElementId().startsWith("com.sebulli.fakturama.editors")) {
					eventBroker.post("EditorPart/updateCoolBar", null);			
				}
			}
		};
		
		// register for activation event
		eventBroker.subscribe(UIEvents.UILifeCycle.ACTIVATE, eventHandler);
	}
}