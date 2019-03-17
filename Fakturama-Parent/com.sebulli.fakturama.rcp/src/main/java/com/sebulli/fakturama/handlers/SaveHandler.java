/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sebulli.fakturama.handlers;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.dialogs.SaveablePartPromptDialog;
import com.sebulli.fakturama.log.ILogger;

public class SaveHandler extends PartServiceSaveHandler {

    @CanExecute
    public boolean canExecute(EPartService partService) {
        if (partService != null) {
            MDirtyable dirtyable = partService.getActivePart();
            if (dirtyable == null) { return false; }
            return dirtyable.isDirty();
        }
        return false;
    }

    @Execute
    public void execute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, final EPartService partService)
            throws InvocationTargetException, InterruptedException {
        final MPart activePart = partService.getActivePart();
        if (activePart != null) {
            partService.savePart(activePart, false);
        }
    }
    
    
	
	@Inject
    private IEclipseContext context;
	
	@Inject
	private ILogger log;
	
	@Override
	public boolean save(MPart dirtyPart, boolean confirm) {
		if (confirm) {
			switch (promptToSave(dirtyPart)) {
			case NO:
				return true;
			case CANCEL:
				return false;
			case YES:
				break;
			}
		}

		Object client = dirtyPart.getObject();
		Boolean retVal = Boolean.TRUE;
		try {
			retVal = (Boolean) ContextInjectionFactory.invoke(client, Persist.class, dirtyPart.getContext());
			log.debug(client.getClass().getSimpleName().toString() + " save result: " + retVal);
		} catch (InjectionException e) {
			log.error(e, MessageFormat.format("Failed to persist contents of part ({0})", dirtyPart.getElementId()));
			return false;
		} catch (RuntimeException e) {
			log.error(e, MessageFormat.format("Failed to persist contents of part ({0}) via DI", dirtyPart.getElementId()));
			return false;
		}
		return retVal;
	}
	
	@Override
	public Save promptToSave(MPart dirtyPart) {
		Shell shell = (Shell) context.get(IServiceConstants.ACTIVE_SHELL);
		Object[] elements = promptForSave(shell, Collections.singleton(dirtyPart));
		if (elements == null) {
			return Save.CANCEL;
		}
		return elements.length == 0 ? Save.NO : Save.YES;
	}

	@Override
	public Save[] promptToSave(Collection<MPart> dirtyParts) {
		List<MPart> parts = new ArrayList<>(dirtyParts);
		Shell shell = (Shell) context
				.get(IServiceConstants.ACTIVE_SHELL);
		Save[] response = new Save[dirtyParts.size()];
		Object[] elements = promptForSave(shell, parts);
		if (elements == null) {
			Arrays.fill(response, Save.CANCEL);
		} else {
			Arrays.fill(response, Save.NO);
			for (int i = 0; i < elements.length; i++) {
				response[parts.indexOf(elements[i])] = Save.YES;
			}
		}
		return response;
	}

	private Object[] promptForSave(Shell parentShell,
			Collection<MPart> saveableParts) {
		SaveablePartPromptDialog dialog = new SaveablePartPromptDialog(
				parentShell, saveableParts, context);
		if (dialog.open() == Window.CANCEL) {
			return null;
		}

		return dialog.getCheckedElements();
	}
}
