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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.model.VoucherType;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.parts.CreditorEditor;
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
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
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
    public static final String PARAM_FOLLOW_UP = "org.fakturama.document.followup";
    public static final String PARAM_COPY = "org.fakturama.document.copy";
    
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
	private EModelService modelService;
    
    @Inject
    private EHandlerService handlerService;
    
    @Inject
    private ECommandService commandService;
    
    @Inject
    private ESelectionService selectionService;
    
    @Inject
    private ILogger log;
    
    @CanExecute
    public boolean canExecute(@Active MPart activePart, EPartService partService, @Optional MMenuItem menuItem) {
        boolean retval = true;
        // only check for a certain popup menu action; the action is nearly always executable
        if (menuItem != null && menuItem.getElementId().equals("com.sebulli.fakturama.document.popup.opendoc")) {
            @SuppressWarnings("unchecked")
            List<Document> selectedObjects = (List<Document>) selectionService.getSelection(activePart.getElementId());
            retval = selectedObjects != null && selectedObjects.size() == 1;
        }
        return retval;
    }

	/**
	 * Execute the command
	 * 
	 * @param isFollowUp if a document is a duplicate of an other, set this to <code>true</code>
	 * @param objId the object id of the current document
	 * @param category for Document editors only (this is the BillingType)
	 */
	@Execute
	public void execute( 
	        @Named(PARAM_EDITOR_TYPE) String editorType,
			@Optional @Named(PARAM_OBJ_ID) String objId,
			@Optional @Named(PARAM_CATEGORY) String category,
            @Optional @Named(PARAM_FOLLOW_UP) Boolean isFollowUp,
            @Optional @Named(PARAM_COPY) Boolean isCopy,
            @Optional @Named(PARAM_CALLING_DOC) String callingDoc,
            @Optional @Named(PARAM_FORCE_NEW) Boolean isForceNew,
            final MApplication application
            ) throws ExecutionException {
		
			// If we had a selection lets open the editor
            MPartStack documentPartStack = (MPartStack) modelService.find(DETAIL_PARTSTACK_ID, application);
            // close other editors if set in preferences
            if(preferences.getBoolean(Constants.PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS)) {
                ParameterizedCommand closeCommand = commandService.createCommand("org.eclipse.ui.file.closeAll", null);
                handlerService.executeHandler(closeCommand);
            }
            
            Map<String, String> params = new HashMap<>();
            log.debug("==> " + objId+ " / SEL-LISTNR (Call): " + selectionService);
            log.debug("isForceNew: " + isForceNew);
            // forceNew means we want to create a new document unconditionally
            if(!BooleanUtils.toBoolean(isForceNew)) {
                
                Object selObj = selectionService.getSelection();
                Long id = null;
                if (selObj instanceof List) {
                    @SuppressWarnings({ "unchecked" })
                    List<IEntity> selection = (List<IEntity>) selectionService.getSelection();
                    if (!selection.isEmpty()) {
                        id = (Long) selection.get(0).getId();
                    }
                } else {
                    id = (Long)selObj;
                }

                if(id != null) {
                    params.put(PARAM_OBJ_ID, Long.toString(id));
                } else {
                    params.put(PARAM_OBJ_ID, objId);
                }
            	params.put(PARAM_CALLING_DOC, callingDoc);
            	params.put(PARAM_COPY, BooleanUtils.toStringTrueFalse(isCopy));
            }
            params.put(PARAM_CATEGORY, category);
            log.debug("PARAM_OBJ_ID: " + params.get(PARAM_OBJ_ID));
            // Define  the editor and try to open it
			MPart editorPart = createEditorPart(editorType, documentPartStack, isFollowUp, isCopy, params);
			log.debug("PART: " + editorPart.getObject());
			partService.showPart(editorPart, PartState.ACTIVATE);
			
			// clear the objId parameter because of unwanted side effects for subsequent creation of an editor
			editorPart.getContext().remove(PARAM_OBJ_ID);
			editorPart.getContext().remove(PARAM_FOLLOW_UP);
//            evtBroker.post("EditorPart/updateCoolBar", editorType);			
	}
	
	/**
	 * create a new Part from a PartDescriptor if no one exists.
	 * 
	 * @param type the type of the new editor
	 * @param stack the current {@link MPartStack}
	 * @param isFollowUp if a follow-up document should be created
	 * @param isCopy if a copy of the active editor should be created
	 * @param params a {@link Map} of params which should be attached to the current command
	 * @return new {@link MPart} or existing one (if it was previously created)
	 */
	private MPart createEditorPart(String type, MPartStack stack, Boolean isFollowUp, Boolean isCopy, Map<String, String> params) {
		MPart myPart = null;
		IEclipseContext stackContext = null;
		// search only if not duplicated! Skip if a copy should be created.
//		log.debug("OBJ_ID: " + params.get(PARAM_OBJ_ID));
		if(!BooleanUtils.toBoolean(isFollowUp) && !BooleanUtils.toBoolean(isCopy)) {
			Collection<MPart> parts = partService.getParts();
//			log.debug("PARTS: " + parts.size()) ;
	        if (params.get(PARAM_OBJ_ID) != null) {
	    		// at first we look for an existing Part
	            for (MPart mPart : parts) {
	            	/*
	            	 * Problem: Open a part and then exit the application. Start the application again and try to open (from list view)
	            	 * the SAME document/payment/shipping/whatever. Since the context is null, a new document window is opened :-(
	            	 */
	            	
	    			if (StringUtils.equalsIgnoreCase(mPart.getElementId(), type)/* && mPart.getContext() != null*/) {
	    				String object = (String) mPart.getTransientData().get(PARAM_OBJ_ID);
	    				if (StringUtils.equalsIgnoreCase(object, params.get(PARAM_OBJ_ID))) {
//        log.debug("MYPART: " + (mPart != null? mPart.getObject() : "null") + "; obj: " + object);
	    					myPart = mPart;
	    					break;
	    				}
	    			}
	    		}
	        }
		}
        

		// if not found (or should create a duplicate / copy) then we create a new one from a part descriptor
		if (myPart == null) {
			MPartDescriptor partDescriptor = modelService.getPartDescriptor(DOCVIEW_PARTDESCRIPTOR_ID);
			myPart = partService.createPart(DOCVIEW_PARTDESCRIPTOR_ID);
			myPart.setElementId(type);
			myPart.setVisible(true);
			myPart.getTags().add(partDescriptor.getCategory());

			myPart.getTransientData().putAll(params);
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
                myPart.getTransientData().put(PARAM_VOUCHERTYPE, VoucherType.EXPENDITURE.getName());
                break;
			case ReceiptVoucherEditor.ID:
			case ReceiptVoucherListTable.ID:
                myPart.setLabel(msg.commandReceiptvouchersName);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + ReceiptVoucherEditor.class.getName());
                myPart.getTransientData().put(PARAM_VOUCHERTYPE, VoucherType.RECEIPTVOUCHER.getName());
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
                myPart.getTransientData().put(PARAM_EDITOR_TYPE, type);
                break;
            case CreditorEditor.ID:
                myPart.setLabel(msg.pageContacts);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + CreditorEditor.class.getName());
                myPart.getTransientData().put(PARAM_EDITOR_TYPE, type);
                break;
            case DocumentsListTable.ID:
            case DocumentEditor.ID:
                BillingType billingType = BillingType.getByName(params.get(PARAM_CATEGORY));
                DocumentType docType = DocumentTypeUtil.findByBillingType(billingType);
                myPart.setContributionURI(BASE_CONTRIBUTION_URI + DocumentEditor.class.getName());
                myPart.setLabel(msg.getMessageFromKey(docType.getNewText()));
                myPart.getTransientData().put(PARAM_FOLLOW_UP, isFollowUp);
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
