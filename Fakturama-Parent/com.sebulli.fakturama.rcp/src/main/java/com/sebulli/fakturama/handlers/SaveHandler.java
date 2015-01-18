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

import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

public class SaveHandler {
    

    @CanExecute
    public boolean canExecute(EPartService partService) {
        if (partService != null) {
            MDirtyable dirtyable = partService.getActivePart();
		if (dirtyable == null) {
			return false;
		}
		return dirtyable.isDirty();
            //return !partService.getDirtyParts().isEmpty();
        }
        return false;
    }

    
//	@CanExecute
//	public boolean canExecute(
//	        /*final EPartService partService,*/
//	        @Optional /*@Active*/ MDirtyable dirtyable) {
//		if (dirtyable == null) {
//			return false;
//		}
//		return dirtyable.isDirty();
//	}

	@Execute
	public void execute(
			IEclipseContext context,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			final EPartService partService)
			throws InvocationTargetException, InterruptedException {
		final MPart activePart = partService.getActivePart();
        if (activePart != null) {
		    partService.savePart(activePart, false);
		}
	}
}
