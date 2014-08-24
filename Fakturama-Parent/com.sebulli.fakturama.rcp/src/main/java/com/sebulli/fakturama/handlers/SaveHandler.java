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
import java.util.Collection;

import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.swt.widgets.Shell;
//import org.eclipse.e4.ui.workbench.Persist;

public class SaveHandler implements ISaveHandler {
    
	@CanExecute
	public boolean canExecute(@Active MDirtyable dirtyable) {
		if (dirtyable == null) {
			return false;
		}
		return dirtyable.isDirty();
	}

	@Execute
	public void execute(
			IEclipseContext context,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			/*@Active MDirtyable contribution,*/
			final EPartService partService)
			throws InvocationTargetException, InterruptedException {
//		final IEclipseContext pmContext = context.createChild();
		final MPart activePart = partService.getActivePart();
        if (activePart != null) {
		    partService.savePart(activePart, false);
		}

//		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
//		dialog.open();
//		dialog.run(true, true, new IRunnableWithProgress() {
//			public void run(IProgressMonitor monitor)
//					throws InvocationTargetException, InterruptedException {
//				pmContext.set(IProgressMonitor.class.getName(), monitor);
//                if (activePart != null) {
//				    partService.savePart(activePart, false);
//				}
//			}
//		});
		
//		pmContext.dispose();
	}

    @Override
    public boolean save(MPart dirtyPart, boolean confirm) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean saveParts(Collection<MPart> dirtyParts, boolean confirm) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Save promptToSave(MPart dirtyPart) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Save[] promptToSave(Collection<MPart> dirtyParts) {
        // TODO Auto-generated method stub
        return null;
    }
}
