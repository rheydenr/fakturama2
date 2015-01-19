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
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.parts.PaymentEditor;
import com.sebulli.fakturama.parts.ShippingEditor;
import com.sebulli.fakturama.parts.VatEditor;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;
import com.sebulli.fakturama.views.datatable.payments.PaymentListTable;
import com.sebulli.fakturama.views.datatable.shippings.ShippingListTable;
import com.sebulli.fakturama.views.datatable.vats.VATListTable;

/**
 * Universal Handler to open an UniDataSet editor
 * 
 * @author Gerd Bartelt
 */
public class CallEditor {
    
    private static final String BASE_CONTRIBUTION_URI = "bundleclass://com.sebulli.fakturama.rcp/";

    public static final String PARAM_OBJ_ID = "com.sebulli.fakturama.rcp.editor.objId";

    private static final String DOCVIEW_PART_ID = "com.sebulli.fakturama.rcp.docview";
    private static final String DOCVIEW_PARTDESCRIPTOR_ID = "com.sebulli.fakturama.rcp.docdetail";

    @Inject
    @Translation
    protected Messages msg;
	
	@Inject
	private EPartService partService;

	/**
	 * Execute the command
	 */
	@Execute
	public void execute( 
	        @Named("com.sebulli.fakturama.editors.editortype") String editorType,
			@Optional @Named("com.sebulli.fakturama.rcp.cmdparam.objId") String objId,
            final MApplication application,
            final EModelService modelService) throws ExecutionException {
			// If we had a selection lets open the editor
            MPartStack documentPartStack = (MPartStack) modelService.find("com.sebulli.fakturama.rcp.detailpanel", application);
            // TODO close other parts if this is set in preferences!
            IEclipseContext stackContext = null;
            for (MContext contexts : modelService.findElements(documentPartStack, null, MContext.class, null)) {
                if(((MPart)contexts).getElementId().contentEquals(DOCVIEW_PARTDESCRIPTOR_ID)) {
                    stackContext = contexts.getContext();
                }
            }
 //           MPartStack stack = (MPartStack)modelService.find("com.sebulli.fakturama.rcp.detailpanel", application);

			// Define  the editor and try to open it
			partService.showPart(createEditorPart(editorType, objId, stackContext, documentPartStack), PartState.ACTIVATE);
	}
	
	/**
	 * create a new Part from a PartDescriptor if no one exists.
	 * 
	 * @param title
	 * @param objId
	 * @return
	 */
	private MPart createEditorPart(String type, String objId, IEclipseContext stackContext, MPartStack stack) {
		MPart myPart = null;
		Collection<MPart> parts = partService.getParts();
        if (objId != null) {
    		// at first we look for an existing Part
            for (MPart mPart : parts) {
    			if (StringUtils.equalsIgnoreCase(mPart.getElementId(), type) && mPart.getContext() != null) {
    				String object = (String) mPart.getProperties().get(PARAM_OBJ_ID);
    				if (StringUtils.equalsIgnoreCase(object, objId)) {
    					myPart = mPart;
    					break;
    				}
    			}
    		}
        }
        
		// if not found then we create a new one from a part descriptor
		if (myPart == null) {
			myPart = partService.createPart(DOCVIEW_PART_ID);
			myPart.setElementId(type);
			stack.getChildren().add(myPart);
			// we have to distinguish the different editors here
			switch (type) {
			case VatEditor.ID:  // fall through
			case VATListTable.ID:
				myPart.setLabel(msg.commandVatsName);
				myPart.setContributionURI(BASE_CONTRIBUTION_URI + VatEditor.class.getName());
				myPart.setContext(EclipseContextFactory.create());
				myPart.getProperties().put(PARAM_OBJ_ID, objId);
				break;
			case ShippingEditor.ID:
			case ShippingListTable.ID:
                myPart.setLabel(msg.commandShippingsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ShippingEditor.class.getName());
                myPart.setContext(EclipseContextFactory.create());
                myPart.getProperties().put(PARAM_OBJ_ID, objId);
                break;
			case PaymentEditor.ID:
			case PaymentListTable.ID:
                myPart.setLabel(msg.commandPaymentsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + PaymentEditor.class.getName());
                myPart.setContext(EclipseContextFactory.create());
                myPart.getProperties().put(PARAM_OBJ_ID, objId);
                break;
            case ContactEditor.ID:
            case ContactListTable.ID:
                myPart.setLabel(msg.commandContactsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ContactEditor.class.getName());
                myPart.setContext(EclipseContextFactory.create());
                myPart.getProperties().put(PARAM_OBJ_ID, objId);
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
