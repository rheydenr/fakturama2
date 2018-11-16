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

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.office.FileOrganizer;

/**
 * This handler reorganizes all documents (i.e., moves them to a new location
 * and perhaps rename them).
 */
public class ReorganizeDocuments {
	
	public static final String RUN_REORGANIZE_SILENTLY = "com.sebulli.fakturama.command.documents.reorganize.silently";

	@Inject
	@Translation
	protected Messages msg;

	@Inject
	private ILogger log;

    @Inject
    protected IEclipseContext context;
    
    @Inject
	private DocumentsDAO documentDAO;

	/**
	 * Run the action
	 * 
	 * Reorganize all documents
	 */
	@Execute
	public void run(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent,
			@Optional @Named(RUN_REORGANIZE_SILENTLY) String runSilently
			) {
		
		final boolean isSilentRun = BooleanUtils.toBoolean(runSilently);

		if (!isSilentRun 
				&& !MessageDialog.openQuestion(parent, msg.dialogMessageboxTitleWarning, msg.dialogReorganizeQuestion)) {
			return;
		}
		
		final Long countOfDocuments = documentDAO.getCount();

		// Run the reorganization in an extra thread and show the progress in
		// the status bar
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
		try {
			IRunnableWithProgress op = new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(msg.mainMenuExtraReorganize, countOfDocuments.intValue());
					FileOrganizer fo = ContextInjectionFactory.make(FileOrganizer.class, context);
					fo.reorganizeDocuments(monitor, isSilentRun);
					monitor.done();  // msg.dialogReorganizeDonemessage
				}
			};
			progressMonitorDialog.run(true, true, op);
		}
		catch (InvocationTargetException e) {
			log.error(e, "Fehler: ");
		}
		catch (InterruptedException e) {
			// handle cancellation
			throw new OperationCanceledException();
		}
		finally {
			log.info(msg.startMigrationEnd);
		}
	}

}
