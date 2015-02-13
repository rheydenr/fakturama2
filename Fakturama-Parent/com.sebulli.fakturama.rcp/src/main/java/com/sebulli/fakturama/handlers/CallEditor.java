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
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.PaymentEditor;
import com.sebulli.fakturama.parts.ShippingEditor;
import com.sebulli.fakturama.parts.VatEditor;
import com.sebulli.fakturama.util.DocumentTypeUtil;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;
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

    public static final String PARAM_OBJ_ID = "com.sebulli.fakturama.rcp.cmdparam.objId";
    public static final String PARAM_CATEGORY = "com.sebulli.fakturama.editors.category";
    public static final String PARAM_EDITOR_TYPE = "com.sebulli.fakturama.editors.editortype";

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
	        @Named(PARAM_EDITOR_TYPE) String editorType,
			@Optional @Named(PARAM_OBJ_ID) String objId,
			@Optional @Named(PARAM_CATEGORY) String category,
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

			// Define  the editor and try to open it
			partService.showPart(createEditorPart(editorType, objId, stackContext, documentPartStack, category), PartState.ACTIVATE);
	}
	
	/**
	 * create a new Part from a PartDescriptor if no one exists.
	 * 
	 * @param title
	 * @param objId
	 * @param category 
	 * @return
	 */
	private MPart createEditorPart(String type, String objId, IEclipseContext stackContext, MPartStack stack, String category) {
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
			myPart.setContext(EclipseContextFactory.create());
			myPart.getProperties().put(PARAM_OBJ_ID, objId);
			stack.getChildren().add(myPart);
			// we have to distinguish the different editors here
			switch (type) {
			case VatEditor.ID:  // fall through
			case VATListTable.ID:
				myPart.setLabel(msg.commandVatsName);
				myPart.setContributionURI(BASE_CONTRIBUTION_URI + VatEditor.class.getName());
				break;
			case ShippingEditor.ID:
			case ShippingListTable.ID:
                myPart.setLabel(msg.commandShippingsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ShippingEditor.class.getName());
                break;
			case PaymentEditor.ID:
			case PaymentListTable.ID:
                myPart.setLabel(msg.commandPaymentsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + PaymentEditor.class.getName());
                break;
            case ContactEditor.ID:
            case ContactListTable.ID:
                myPart.setLabel(msg.commandContactsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ContactEditor.class.getName());
                break;
            case DocumentsListTable.ID:
            case DocumentEditor.ID:
                BillingType billingType = BillingType.getByName(category);
                DocumentType docType = DocumentTypeUtil.findByBillingType(billingType);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + DocumentEditor.class.getName());
                myPart.setLabel(msg.getMessageFromKey(docType.getNewText()));
                myPart.getProperties().put(PARAM_CATEGORY, category);
                break;
			default:
				myPart.setLabel("unknown");
				myPart.setContributionURI(BASE_CONTRIBUTION_URI + ContactEditor.class.getName());
				break;
			}
		}
		return myPart;
	}

}
