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

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.i18n.Messages;

public class QuitHandler {
    
    @Inject
    @Translation
    protected Messages msg;

    
	@Execute
	public void execute(IWorkbench workbench,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell){
		if (MessageDialog.openConfirm(shell, "Confirmation",
				msg.mainMenuFileExitQuestion)) {
			workbench.close();
		}
	}
}
