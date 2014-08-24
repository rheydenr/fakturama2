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
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.parts.VatEditor;
import com.sebulli.fakturama.views.datatable.vats.VATListTable;

/**
 * Universal Handler to open an UniDataSet editor
 * 
 * @author Gerd Bartelt
 */
public class CallEditor {
    
    private static final String BASE_CONTRIBUTION_URI = "bundleclass://com.sebulli.fakturama.rcp/";

    private static final String PARAM_OBJ_ID = "com.sebulli.fakturama.rcp.editor.objId";

    private static final String DOCVIEW_PART_ID = "com.sebulli.fakturama.rcp.docview";

    @Inject
    @Translation
    protected Messages msg;
	
	@Inject
	private EPartService partService;

	/**
	 * Execute the command
	 */
	@Execute
	public void execute( @Named("com.sebulli.fakturama.editors.editortype") String param,
			@Optional @Named("com.sebulli.fakturama.rcp.cmdparam.objId") String objId) throws ExecutionException {
			// If we had a selection lets open the editor
//			if (objId != null) {
				// Define  the editor and try to open it
				partService.showPart(createEditorPart(param, objId), PartState.ACTIVATE);
//			}
	}
	
	/**
	 * create a new Part from a PartDescriptor if no one exists.
	 * 
	 * @param title
	 * @param objId
	 * @return
	 */
	private MPart createEditorPart(String type, String objId) {
		MPart myPart = null;
		Collection<MPart> parts = partService.getParts();
		// at first we look for an existing Part
		for (MPart mPart : parts) {
			if (StringUtils.equalsIgnoreCase(mPart.getElementId(), DOCVIEW_PART_ID) && mPart.getContext() != null) {
				Object object = mPart.getContext().get(PARAM_OBJ_ID);
				if (object != null && object.equals(objId)) {
					myPart = mPart;
					break;
				}
			}
		}
		
		// if not found then we create a new one from a part descriptor
		if (myPart == null) {			
			myPart = partService.createPart(DOCVIEW_PART_ID);
			
			// we have to distinguish the different editors here
			switch (type) {
			case VatEditor.ID:  // fall through
			case VATListTable.ID:
				myPart.setLabel("VAT ");
				myPart.setContributionURI(BASE_CONTRIBUTION_URI + VatEditor.class.getName());
				myPart.setContext(partService.getActivePart().getContext());
				myPart.getContext().set(PARAM_OBJ_ID, objId);
				break;

			default:
				myPart.setLabel("unknown");
				myPart.setContext(EclipseContextFactory.create());
				myPart.setContributionURI(BASE_CONTRIBUTION_URI + ContactEditor.class.getName());
				break;
			}
		}
		return myPart;
	}

}
