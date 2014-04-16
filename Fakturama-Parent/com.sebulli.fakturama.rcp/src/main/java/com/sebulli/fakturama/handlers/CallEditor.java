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

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.parts.ContactEditor;

/**
 * Universal Handler to open an UniDataSet editor
 * 
 * @author Gerd Bartelt
 */
public class CallEditor {
	
	@Inject
	private EPartService partService;

	/**
	 * Execute the command
	 */
	@Execute
	public void execute( @Named("com.sebulli.fakturama.editors.editortype") String param,
			@Optional @Named("com.sebulli.fakturama.rcp.cmdparam.objId") String objId) throws ExecutionException {
			// If we had a selection lets open the editor
			if (objId != null) {
				// Define  the editor
//				// And try to open it
				partService.showPart(createEditorPart("info", objId), PartState.ACTIVATE);
			}
	}
	
	private MPart createEditorPart(String title, String objId) {
		MPart myPart = null;
		Collection<MPart> parts = partService.getParts();
		for (MPart mPart : parts) {
			if(StringUtils.equalsIgnoreCase(mPart.getElementId(), "com.sebulli.fakturama.rcp.docview")
					&& mPart.getContext() != null){
			Object object = mPart.getContext().get("com.sebulli.fakturama.rcp.editor.objId");
			if(object.equals(objId)) {
				myPart = mPart;
				break;
			}
			}
		}
		if(myPart == null) {
		
		myPart = partService.createPart("com.sebulli.fakturama.rcp.docview");
		myPart.setLabel(title);
		myPart.setContributionURI("bundleclass://com.sebulli.fakturama.rcp/"+ContactEditor.class.getName());
		myPart.setContext(EclipseContextFactory.create());
		myPart.getContext().set("com.sebulli.fakturama.rcp.editor.objId", objId);
		}
		return myPart;
	}

}
