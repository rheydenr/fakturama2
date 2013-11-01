package com.sebulli.fakturama;
import javax.inject.Inject;

import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.internal.workbench.ExitHandler;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.dao.ContactDAO;
import com.sebulli.fakturama.resources.urihandler.IconURLStreamHandlerService;
import com.sebulli.fakturama.startup.InitialStartupDialog;

// for a extended example see
// https://bugs.eclipse.org/382224
public class LifecycleManager {
	
	@Inject
	private ContactDAO contactDAO;

	@ProcessAdditions
	public void processAdditions() {
		IconURLStreamHandlerService.getInstance().register();
	}

  @PostContextCreate
  void postContextCreate(IApplicationContext appContext, Display display) {
    final Shell shell = new Shell(SWT.TOOL | SWT.NO_TRIM);
//    InitialStartupDialog startDialog = new InitialStartupDialog(shell);
//
//    // close the static splash screen
//    appContext.applicationRunning();
//
//    if (startDialog.open() != Window.OK) {
//      // close the application
//    //	ExitHandler!
//      System.exit(-1);
//    }
  }
} 