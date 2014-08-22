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

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;

import com.sebulli.fakturama.i18n.Messages;

/**
 * This action creates a new contact in an editor.
 * 
 * @author Gerd Bartelt
 */
public class NewContactHandler/* extends NewEditorAction */{
    
    @Inject
    @Translation
    protected Messages msg;

	//T: Text of the action to create a new contact
	public final static String ACTION_ID = CommandIds.CMD_NEW_CONTACT; 

//	/**
//	 * Constructor
//	 * 
//	 * @param category
//	 *            Category of the new contact
//	 */
//	public NewContactHandler(String category) {
//
//		super(ACTIONTEXT, category);
//
//		//T: Tool Tip Text
//		setToolTipText(_("Create a new contact") );
//
//		// The id is used to refer to the action in a menu or toolbar
//		setId(CommandIds.CMD_NEW_CONTACT);
//
//		// Associate the action with a predefined command, to allow key
//		// bindings.
//		setActionDefinitionId(CommandIds.CMD_NEW_CONTACT);
//
//		// sets a default 16x16 pixel icon.
//		setImageDescriptor(com.sebulli.fakturama.Activator.getImageDescriptor("/icons/16/contact_16.png"));
//
//	}
//
//	/**
//	 * Run the action
//	 * 
//	 * Open a new contact editor.
//	 */
//	@Override
//	public void run() {
//
//		// cancel, if the data base is not connected.
//		if (!DataBaseConnectionState.INSTANCE.isConnected())
//			return;
//
//		// Sets the editors input
//		UniDataSetEditorInput input = new UniDataSetEditorInput(category);
//
//		// Open a new Contact Editor 
//		try {
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, ContactEditor.ID);
//		}
//		catch (PartInitException e) {
//			Logger.logError(e, "Error opening Editor: " + ContactEditor.ID);
//		}
//	}
}
