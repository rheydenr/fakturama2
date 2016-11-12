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

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.parts.ContactEditor;

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
    public void execute() throws ExecutionException {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ContactEditor.ID);
//        params.put(CallEditor.PARAM_FORCE_NEW, BooleanUtils.toStringTrueFalse(true));
        ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
        handlerService.executeHandler(parameterizedCommand);	    
	}
}
