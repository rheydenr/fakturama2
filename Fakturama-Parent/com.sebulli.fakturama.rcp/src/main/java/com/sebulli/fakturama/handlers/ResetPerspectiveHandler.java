/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.log.ILogger;

/**
 * Resets a perspective to its defaults.
 * 
 * @see <a href=
 *      "https://stackoverflow.com/questions/19717154/how-do-i-reset-perspective-for-eclipse-e4-rcp-application">StackOverflow</a>
 *      and
 *      <a href="https://www.eclipse.org/forums/index.php/t/210165/">Forum</a>
 *
 */
public class ResetPerspectiveHandler {
    @Inject
    private ILogger log;
    
	private static final String MAIN_WINDOW_ID = "com.sebulli.fakturama.application";

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, EModelService modelService,
			MApplication app, EPartService partService) {

		MWindow window = (MWindow) modelService.find(MAIN_WINDOW_ID, app);

		final MPerspective perspective = modelService.getActivePerspective(window);

		final MUIElement snippet = modelService.cloneSnippet(app, perspective.getElementId(), window);
		snippet.setToBeRendered(true);
		if (snippet != null) {
			MElementContainer<MUIElement> parent = perspective.getParent();
			perspective.setToBeRendered(false);

			List<MWindow> existingDetachedWindows = new ArrayList<MWindow>();
			existingDetachedWindows.addAll(perspective.getWindows());

			MPerspective dummyPerspective = (MPerspective) snippet;
			while (dummyPerspective.getWindows().size() > 0) {
				MWindow detachedWindow = dummyPerspective.getWindows().remove(0);
				perspective.getWindows().add(detachedWindow);
			}

			parent.getChildren().remove(perspective);

			parent.getChildren().add(snippet);
			log.debug(parent.getChildren().get(0).getElementId());
			partService.switchPerspective((MPerspective) snippet);
		}
	}
}