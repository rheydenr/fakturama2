/**
 * 
 */
package com.sebulli.fakturama.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

/**
 * @author rheydenr
 *
 */
public class SaveAllHandler {

	  @Execute
	  void execute(EPartService partService) {
	    partService.saveAll(false);
	  }

}
