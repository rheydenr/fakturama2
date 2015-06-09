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
import javax.inject.Named;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;

import com.sebulli.fakturama.i18n.Messages;

/**
 * This action creates a new product in an editor.
 * 
 * @author Gerd Bartelt
 */
public class NewProductHandler {
    
    @Inject
    @Translation
    protected Messages msg;

	//T: Text of the action to create a new product
	public final static String ACTION_ID = CommandIds.CMD_NEW_PRODUCT; 

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
    public void execute( 
            @Named(CallEditor.PARAM_EDITOR_TYPE) String editorType) throws ExecutionException {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, editorType);
        ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
        handlerService.executeHandler(parameterizedCommand);        
    }

//	/**
//	 * Constructor
//	 */
//	public NewProductAction() {
//		
//		super(ACTIONTEXT);
//
//		//T: Tool Tip Text
//		setToolTipText(_("Create a new product") );
//
//		// The id is used to refer to the action in a menu or toolbar
//		setId(CommandIds.CMD_NEW_PRODUCT);
//
//		// Associate the action with a pre-defined command, to allow key
//		// bindings.
//		setActionDefinitionId(CommandIds.CMD_NEW_PRODUCT);
//
//		// sets a default 16x16 pixel icon.
//		setImageDescriptor(com.sebulli.fakturama.Activator.getImageDescriptor("/icons/16/product_16.png"));
//	}
}
