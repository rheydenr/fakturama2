/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.parts.CreditorEditor;
import com.sebulli.fakturama.parts.DebitorEditor;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * This action creates a new contact in an editor.
 * 
 */
public class NewContactHandler {
    
    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private EHandlerService handlerService;

    @Inject
    private ECommandService commandService;

	/**
	 * Run the action
	 * 
	 * Open a new contact editor.
	 */
	@Execute
    public void execute(Shell shell) throws ExecutionException {
		Menu menu = new Menu(shell, SWT.POP_UP);
		ContactTypeMenuItem[] contactTypes = new ContactTypeMenuItem[]{
				new ContactTypeMenuItem(msg.commandNewCreditorName,CreditorEditor.ID,Icon.COMMAND_VENDOR),
				new ContactTypeMenuItem(msg.commandNewDebtorName,DebitorEditor.ID,Icon.COMMAND_CONTACT)
			};
		for (ContactTypeMenuItem contactType : contactTypes) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(contactType.displayText);
			item.setData(contactType.editorId);
			item.setImage(contactType.icon.getImage(IconSize.DefaultIconSize));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					callHandler((String) e.widget.getData());
				}
			});
		}

		// Set the location of the pop up menu near to the mouse pointer
		int x = shell.getDisplay().getCursorLocation().x;
		int y = shell.getDisplay().getCursorLocation().y;
		menu.setLocation(x, y);
		menu.setVisible(true);
	}
	
	private void callHandler(String editorType) {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, editorType);
//        params.put(CallEditor.PARAM_FORCE_NEW, BooleanUtils.toStringTrueFalse(true));
        ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
        handlerService.executeHandler(parameterizedCommand);	    
	}
	
	private class ContactTypeMenuItem {
		String displayText, editorId;
		Icon icon;
		
		public ContactTypeMenuItem(String displayText, String editorId, Icon icon) {
			this.displayText = displayText;
			this.editorId = editorId;
			this.icon = icon;
		}
		
	}
}
