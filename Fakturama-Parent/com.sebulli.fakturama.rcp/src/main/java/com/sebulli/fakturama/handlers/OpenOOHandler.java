/**
 * 
 */
package com.sebulli.fakturama.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;

/**
 * @author rheydenr
 *
 */
public class OpenOOHandler {

	/**
	 * Run the action
	 * <br />
	 * Open the OO as an application.
	 */
	@Execute
	public void execute(final EPartService partService) {
		// see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=372211
		
		// TODO implement!
		partService.showPart(ContactListTable.ID,
				PartState.ACTIVATE);
	}


}
