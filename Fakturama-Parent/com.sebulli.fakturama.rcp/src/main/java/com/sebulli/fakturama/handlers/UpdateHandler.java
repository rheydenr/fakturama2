
package com.sebulli.fakturama.handlers;

import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.jface.dialogs.MessageDialog;

import com.sebulli.fakturama.i18n.Messages;

public class UpdateHandler {

	@Inject
	protected Logger log;

	@Inject
	@Translation
	protected Messages msg;

	@Execute
	public void execute(IProvisioningAgent agent, UISynchronize sync, IWorkbench workbench) {
		ProvisioningSession session = new ProvisioningSession(agent);
		// update all user-visible installable units
		UpdateOperation operation = new UpdateOperation(session);
		IStatus status = operation.resolveModal(null);
		if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
			MessageDialog.openInformation(null, msg.dialogMessageboxTitleInfo, "Nothing to update");
			return;
		}

		ProvisioningJob provisioningJob = operation.getProvisioningJob(null);
		if (provisioningJob != null) {
			sync.syncExec(new Runnable() {

				@Override
				public void run() {
					boolean performUpdate = MessageDialog.openQuestion(null, "Updates available",
							"There are updates available. Do you want to install them now?");
					if (performUpdate) {
						// ...
						provisioningJob.schedule();
					}
				}
			});
			boolean restart = MessageDialog.openQuestion(null, "Updates installed, restart?",
					"Updates have been installed successfully, do you want to restart?");
			if (restart) {
				workbench.restart();
			}
		} else {
			if (operation.hasResolved()) {
				MessageDialog.openError(null, msg.dialogMessageboxTitleError,
						"Couldn't get provisioning job: " + operation.getResolutionResult());
			} else {
				MessageDialog.openError(null, msg.dialogMessageboxTitleError, "Couldn't resolve provisioning job");
			}
		}
	}
}