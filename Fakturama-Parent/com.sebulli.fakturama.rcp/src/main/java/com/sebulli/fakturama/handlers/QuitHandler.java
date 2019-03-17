/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sebulli.fakturama.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.sebulli.fakturama.i18n.Messages;

public class QuitHandler {
    
    @Inject
    @Translation
    protected Messages msg;

    
	@Execute
	public void execute(IWorkbench workbench,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		// backup and other cleaning stuff is made by LifeCycleManager
		
//		if (MessageDialog.openConfirm(shell, "Confirmation",
//				msg.mainMenuFileExitQuestion)) {
			workbench.close();
//		}
	}
	
	
	
//
//	private final MWindow window;
//	private final IEventBroker eventBroker;
//
//	@Inject
//	public QuitProcessor(@Named("com.sebulli.fakturama.application") MWindow window, IEventBroker eventBroker) {
//		this.window = window;
//		this.eventBroker = eventBroker;
//	}
//
//	@Execute
//	void installIntoContext() {
//		eventBroker.subscribe(UIEvents.Context.TOPIC_CONTEXT, new EventHandler() {
//
//			@Override
//			public void handleEvent(Event event) {
//				if (UIEvents.isSET(event)) {
//					if (window.equals(event.getProperty("ChangedElement")) && window.getContext() != null) {
//						// use RunAndTrack to get notified after the IWindowCloseHanlder was changed in
//						// the IEclipseContext
//						window.getContext().runAndTrack(new RunAndTrack() {
//
//							private final IWindowCloseHandler quitHandler = ContextInjectionFactory
//									.make(CloseHandler.class, window.getContext());
//
//							@Override
//							public boolean changed(IEclipseContext context) {
//								Object value = context.get(IWindowCloseHandler.class); // access the context value to be
//																						// reevaluated on every future
//																						// change of the value
//
//								if (!quitHandler.equals(value)) { // prevents endless loop
//									context.set(IWindowCloseHandler.class, quitHandler);
//								}
//
//								return true; // ture keeps tracking and the quitHandler as the only opportunity
//							}
//
//						});
//					}
//				}
//			}
//
//		});
//	}
//	
	
}
