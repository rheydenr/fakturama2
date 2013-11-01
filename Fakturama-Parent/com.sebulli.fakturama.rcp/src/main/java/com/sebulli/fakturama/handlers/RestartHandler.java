/**
 * 
 */
package com.sebulli.fakturama.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;

/**
 * @author rheydenr
 *
 */
public class RestartHandler {
	
	@Execute
	public void restartApplication(IWorkbench workbench) {
	    workbench.restart();
	}
}
