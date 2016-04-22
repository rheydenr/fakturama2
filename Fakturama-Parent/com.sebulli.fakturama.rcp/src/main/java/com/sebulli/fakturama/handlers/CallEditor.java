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
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.VoucherType;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.parts.DebitorEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.ExpenditureVoucherEditor;
import com.sebulli.fakturama.parts.ListEditor;
import com.sebulli.fakturama.parts.PaymentEditor;
import com.sebulli.fakturama.parts.ProductEditor;
import com.sebulli.fakturama.parts.ReceiptVoucherEditor;
import com.sebulli.fakturama.parts.ShippingEditor;
import com.sebulli.fakturama.parts.TextEditor;
import com.sebulli.fakturama.parts.VatEditor;
import com.sebulli.fakturama.util.DocumentTypeUtil;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;
import com.sebulli.fakturama.views.datatable.lists.ItemAccountTypeListTable;
import com.sebulli.fakturama.views.datatable.payments.PaymentListTable;
import com.sebulli.fakturama.views.datatable.products.ProductListTable;
import com.sebulli.fakturama.views.datatable.shippings.ShippingListTable;
import com.sebulli.fakturama.views.datatable.texts.TextListTable;
import com.sebulli.fakturama.views.datatable.vats.VATListTable;
import com.sebulli.fakturama.views.datatable.vouchers.ExpenditureVoucherListTable;
import com.sebulli.fakturama.views.datatable.vouchers.ReceiptVoucherListTable;

/**
 * Universal Handler to open an UniDataSet editor
 * 
 */
public class CallEditor {
    
    public static final String DETAIL_PARTSTACK_ID = "com.sebulli.fakturama.rcp.detailpanel";

    public static final String BASE_CONTRIBUTION_URI = "bundleclass://com.sebulli.fakturama.rcp/";

    /**
     * The id of a selected object which has to be shown in an editor.
     */
    public static final String PARAM_OBJ_ID = "com.sebulli.fakturama.rcp.cmdparam.objId";
    
    /**
     * A category (as initial assignment) for the called editor.
     */
    public static final String PARAM_CATEGORY = "com.sebulli.fakturama.editors.category";
    public static final String PARAM_DUPLICATE = "org.fakturama.document.duplicate";
    
    /**
     * The type of the editor which has to be called.
     */
    public static final String PARAM_EDITOR_TYPE = "com.sebulli.fakturama.editors.editortype";
    
    /**
     * The name of the document which is calling an other editor. This is necessary for returning
     * to the calling document (e.g., if you create a new contact while creating a new invoice).
     */
    public static final String PARAM_CALLING_DOC = "org.fakturama.document.caller";

    /**
     * the ID of the docs part descriptor
     */
    public static final String DOCVIEW_PARTDESCRIPTOR_ID = "org.fakturama.rcp.docview";
    
    public static final String DOCVIEW_PART_ID = "org.fakturama.rcp.docdetail";

    /**
     * If a caller wants to force to create a new document.
     */
	public static final String PARAM_FORCE_NEW = "org.fakturama.rcp.forcenew";

	public static final String PARAM_VOUCHERTYPE = "org.fakturama.rcp.vouchertype";

    @Inject
    @Translation
    protected Messages msg;

    @Inject @Optional
    private IPreferenceStore preferences;

	@Inject
	private EPartService partService;
	
    @Inject
    private IEventBroker evtBroker;

	/**
	 * Execute the command
	 * @param duplicate if a document is a duplicate of an other, set this to <code>true</code>
	 */
	@Execute
	public void execute( 
	        @Named(PARAM_EDITOR_TYPE) String editorType,
			@Optional @Named(PARAM_OBJ_ID) String objId,
			@Optional @Named(PARAM_CATEGORY) String category,
            @Optional @Named(PARAM_DUPLICATE) String duplicate,
            @Optional @Named(PARAM_CALLING_DOC) String callingDoc,
            @Optional @Named(PARAM_FORCE_NEW) String pForceNew,
            final MApplication application,
            final EModelService modelService) throws ExecutionException {
			// If we had a selection lets open the editor
            MPartStack documentPartStack = (MPartStack) modelService.find(DETAIL_PARTSTACK_ID, application);
            boolean forceNew = BooleanUtils.toBoolean(pForceNew);
            
            // close other editors if set in preferences
            if(preferences.getBoolean(Constants.PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS)) {
            	partService.getParts().forEach(part -> { if(part.getTags().contains("removeOnHide")) {partService.hidePart(part);}});
            }

            Map<String, String> params = new HashMap<>();
            // forceNew means we want to create a new document unconditionally
            if(!forceNew) {
            	params.put(PARAM_OBJ_ID, objId);
            	params.put(PARAM_CALLING_DOC, callingDoc);
            }
            params.put(PARAM_CATEGORY, category);
            
            // Define  the editor and try to open it
			MPart editorPart = createEditorPart(editorType, documentPartStack, duplicate, params);
			partService.showPart(editorPart, PartState.ACTIVATE);
//			editorPart.a
            evtBroker.post("EditorPart/updateCoolBar", editorType);			
	}
//	
//	@CanExecute
//	public boolean canExecute(@Named(PARAM_EDITOR_TYPE) String editorType) {
//	    return true; // we can *always* create a new editor!
//	}
	
	/**
	 * create a new Part from a PartDescriptor if no one exists.
	 * 
	 * @param title
	 * @param objId
	 * @param category 
	 * @return
	 */
	private MPart createEditorPart(String type, MPartStack stack, String duplicate, Map<String, String> params) {
		MPart myPart = null;
		IEclipseContext stackContext = null;
		// search only if not duplicated!
		if(!Boolean.toString(true).equals(duplicate)) {
			Collection<MPart> parts = partService.getParts();
	        if (params.get(PARAM_OBJ_ID) != null) {
	    		// at first we look for an existing Part
	            for (MPart mPart : parts) {
	            	/*
	            	 * Problem: Open a part and then exit the application. Start the application again and try to open (from list view)
	            	 * the SAME document/payment/shipping/whatever. Since the context is null, a new document window is opened :-(
	            	 */
	    			if (StringUtils.equalsIgnoreCase(mPart.getElementId(), type)/* && mPart.getContext() != null*/) {
	    				String object = (String) mPart.getProperties().get(PARAM_OBJ_ID);
	    				if (StringUtils.equalsIgnoreCase(object, params.get(PARAM_OBJ_ID))) {
	    					myPart = mPart;
	    					break;
	    				}
	    			}
	    		}
	        }
		}
		
		// if not found (or should create a duplicate) then we create a new one from a part descriptor
		if (myPart == null) {
			myPart = partService.createPart(DOCVIEW_PARTDESCRIPTOR_ID);
			myPart.setElementId(type);
			myPart.setVisible(true);

			myPart.getProperties().putAll(params);
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
			case TextEditor.ID:
			case TextListTable.ID:
                myPart.setLabel(msg.commandTextsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + TextEditor.class.getName());
                break;
			case ExpenditureVoucherEditor.ID:
			case ExpenditureVoucherListTable.ID:
                myPart.setLabel(msg.commandExpenditurevouchersName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ExpenditureVoucherEditor.class.getName());
                myPart.getProperties().put(PARAM_VOUCHERTYPE, VoucherType.EXPENDITURE.getName());
                break;
			case ReceiptVoucherEditor.ID:
			case ReceiptVoucherListTable.ID:
                myPart.setLabel(msg.commandReceiptvouchersName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ReceiptVoucherEditor.class.getName());
                myPart.getProperties().put(PARAM_VOUCHERTYPE, VoucherType.RECEIPTVOUCHER.getName());
                break;
			case ListEditor.ID:
			case ItemAccountTypeListTable.ID:
                myPart.setLabel(msg.commandListsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ListEditor.class.getName());
                break;
			case PaymentEditor.ID:
			case PaymentListTable.ID:
                myPart.setLabel(msg.commandPaymentsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + PaymentEditor.class.getName());
                break;
			case ProductEditor.ID:
			case ProductListTable.ID:
                myPart.setLabel(msg.commandPaymentsName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ProductEditor.class.getName());
                break;
            case ContactEditor.ID:
            case ContactListTable.ID:
            case DebitorEditor.ID:
                myPart.setLabel(msg.pageContacts);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + DebitorEditor.class.getName());
                myPart.getProperties().put(PARAM_EDITOR_TYPE, type);
                break;
            case "Creditor":
                myPart.setLabel(msg.pageContacts);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ContactEditor.class.getName());
                myPart.getProperties().put(PARAM_EDITOR_TYPE, type);
                break;
            case DocumentsListTable.ID:
            case DocumentEditor.ID:
                BillingType billingType = BillingType.getByName(params.get(PARAM_CATEGORY));
                DocumentType docType = DocumentTypeUtil.findByBillingType(billingType);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + DocumentEditor.class.getName());
                myPart.setLabel(msg.getMessageFromKey(docType.getNewText()));
                myPart.getProperties().put(PARAM_DUPLICATE, duplicate);
                break;
			default:
				myPart.setLabel("unknown");
				myPart.setContributionURI(BASE_CONTRIBUTION_URI + ContactEditor.class.getName());
				break;
			}
		}
		
		if(stackContext == null) {
			stackContext = EclipseContextFactory.create();
			
			/*
			 * What's this? - The MPart has to be injected into current context. Some Services
			 * (e.g., EMenuService) need an MPart to work. But the MPart is injected from
			 * Context and therefore we have to put an MPart (or, more concrete, *this* MPart)
			 * into context. That's it :-) 
			 */
			stackContext.set(MPart.class, myPart);
		}
		if(myPart.getContext() == null) {
//			myPart.setContext(stackContext);
			ContextInjectionFactory.inject(myPart, stackContext);
		}
		return myPart;
	}

}
