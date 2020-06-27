package com.sebulli.fakturama.parts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.handlers.OpenBrowserEditorHandler;
import com.sebulli.fakturama.handlers.WebShopCallHandler;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * Composite for the Coolbar. Since this couldn't configured via Application Model we have to implement it ourself.
 * The Application Model Toolbar doesn't support text below button (or you write a new renderer, what I didn't want).
 * 
 */
public class CoolbarViewPart {
    
    public static final String TOOLITEM_COMMAND = "toolitem_command";

	@Inject
	private ECommandService cmdService;

	@Inject
	private EPartService partService;

	@Inject
	private EHandlerService handlerService;
	
    @Inject
    private IPreferenceStore preferences;
	
	@Inject
	private ILogger log;
	
	@Inject
	@Translation
	protected Messages msg;

    private Composite top;
	
	@Inject
    private IEclipseContext ctx;
    
    private Set<ToolBar> coolBarsByKey = new HashSet<>();
//
//    private CoolBarManager coolbarmgr;
//
//    private ToolBarManager toolbarmgr;
//    
	/**
	 * Fill the cool bar with 4 Toolbars.
	 * 
	 * 1st with general tool items like save and print. 2nd with tool items to
	 * create a new document 3rd with some extra items like calculator
	 * 
	 * The icons of the actions are replaced by 32x32 pixel icons. If the action
	 * is in the tool bar and in the menu, 2 actions have to be defined: one
	 * with a 16x16 pixel icon for the menu and one with 32x32 pixel in the tool
	 * bar.
	 *
	 * @param parent the parent
	 */
	@PostConstruct
	public void createControls(Composite parent) {
	    this.top = parent;
	    top.setLayout(new GridLayout(1, false));
	    
	    // now lets create our CoolBar
//		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
//		top.setLayout(layout);
//		top.setSize(SWT.DEFAULT, 70);
		
		if(preferences == null) {
			// this is only the case if we start it for the very first time
			// and nothing is initialized
			return;
		}

//		coolbarmgr = new CoolBarManager(SWT.FLAT);
		
		CoolBar coolBar = new CoolBar(top, SWT.NONE); // coolbarmgr.createControl(top);
//		toolbarmgr = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		ToolBar toolBar1 = new ToolBar(coolBar, SWT.NONE); //toolbarmgr.createControl(coolBar);
		/*
		 * Leider gibt es keine vernünftige Verbindung zw. (altem) Command und der entsprechenden Preference.
		 * deswegen müssen wir ein händisches Mapping erstellen, das so aussieht (Beispiel):
		 * 
		 * "TOOLBAR_SHOW_WEBSHOP" => CommandIds.CMD_WEBSHOP_IMPORT
		 * 
		 * Das muß man dann beim Erstellen des Icons über den Preference-Store abfragen, da die Einstellung dort
		 * beim Hochfahren der Anwendung bzw. beim Migrieren schon hinterlegt wurde.
		 */
//		coolbarmgr.add(toolbarmgr);

//		ToolBarContributionItem toolBarContributionItem = (ToolBarContributionItem) coolbarmgr.getItems()[0];
//        toolBarContributionItem.setMinimumItemsToShow(1);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(WebShopCallHandler.PARAM_IS_GET_PRODUCTS, Boolean.TRUE);
        parameters.put(WebShopCallHandler.PARAM_ACTION, WebShopCallHandler.WEBSHOP_CONNECTOR_ACTION_IMPORT);
		createToolItem(toolBar1, CommandIds.CMD_WEBSHOP_IMPORT, msg.commandWebshopName, msg.commandWebshopTooltip,
				Icon.ICON_SHOP.getImage(IconSize.ToolbarIconSize), null, preferences.getBoolean(Constants.TOOLBAR_SHOW_WEBSHOP), parameters);
		
		createToolItem(toolBar1, "org.fakturama.print.oofile"/*IWorkbenchCommandConstants.FILE_PRINT*/, 
				Icon.ICON_PRINTOO.getImage(IconSize.ToolbarIconSize), Icon.ICON_PRINTOO_DIS.getImage(IconSize.ToolbarIconSize),
				preferences.getBoolean(Constants.TOOLBAR_SHOW_PRINT));

		createToolItem(toolBar1, "org.eclipse.ui.file.save"/*IWorkbenchCommandConstants.FILE_SAVE*/, 
				Icon.ICON_SAVE.getImage(IconSize.ToolbarIconSize), Icon.ICON_SAVE_DIS.getImage(IconSize.ToolbarIconSize),
				preferences.getBoolean(Constants.TOOLBAR_SHOW_SAVE));
//        toolbarmgr.add(toolBarContributionItem);
		finishToolbar(coolBar, toolBar1);
	    coolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBar toolBar2 = new ToolBar(coolBar, SWT.FLAT);
        IToolBarManager toolbarmgr2 = new ToolBarManager(toolBar2);
		String tooltipPrefix = msg.commandNewTooltip + " ";
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewLetterName,
				tooltipPrefix + msg.mainMenuNewLetter, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_LETTER), createCommandParams(DocumentType.LETTER));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewOfferName,
				tooltipPrefix + msg.mainMenuNewOffer, Icon.ICON_OFFER_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_OFFER), createCommandParams(DocumentType.OFFER));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewOrderName,
				tooltipPrefix + msg.mainMenuNewOrder, Icon.ICON_ORDER_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_ORDER), createCommandParams(DocumentType.ORDER));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewConfirmationName,
				tooltipPrefix + msg.mainMenuNewConfirmation, Icon.ICON_CONFIRMATION_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_CONFIRMATION), createCommandParams(DocumentType.CONFIRMATION));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
				tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_INVOICE), createCommandParams(DocumentType.INVOICE));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDeliveryName,
				tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_DELIVERY), createCommandParams(DocumentType.DELIVERY));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewCreditName, 
				tooltipPrefix + msg.mainMenuNewCredit, Icon.ICON_CREDIT_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_CREDIT), createCommandParams(DocumentType.CREDIT));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDocumentDunningName, 
				tooltipPrefix + msg.mainMenuNewDunning, Icon.ICON_DUNNING_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_DUNNING), createCommandParams(DocumentType.DUNNING));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.documentTypeProforma, 
				tooltipPrefix + msg.documentTypeProforma, Icon.ICON_PROFORMA_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_PROFORMA), createCommandParams(DocumentType.PROFORMA));
		finishToolbar(coolBar, toolBar2);
//        coolbarmgr.add(toolbarmgr2);

		ToolBar toolBar3 = new ToolBar(coolBar, SWT.FLAT);
        IToolBarManager toolbarmgr3 = new ToolBarManager(toolBar3);
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ProductEditor.ID);
        createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewProductName, msg.commandNewProductTooltip, 
        		Icon.ICON_PRODUCT_NEW.getImage(IconSize.ToolbarIconSize), null,
                preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_PRODUCT), params);    

        params = new HashMap<>();
		ToolItem contactToolItem = createToolItem(toolBar3, CommandIds.CMD_NEW_CONTACT, msg.toolbarNewContactName, msg.commandNewContactTooltip, 
		        Icon.ICON_CONTACT_NEW.getImage(IconSize.ToolbarIconSize), null,
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_CONTACT), params, true);
		DropdownSelectionListener contactTypeListener =  ContextInjectionFactory.make(DropdownSelectionListener.class, ctx);
		contactTypeListener.setDefaultCommandId(DebitorEditor.ID);
        contactTypeListener.add(new ContactTypeMenuItem(msg.commandNewCreditorName, CreditorEditor.ID, Icon.COMMAND_VENDOR));
        contactTypeListener.add(new ContactTypeMenuItem(msg.commandNewDebtorName, DebitorEditor.ID, Icon.COMMAND_CONTACT));
        contactToolItem.addSelectionListener(contactTypeListener);

        params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ExpenditureVoucherEditor.ID);
		createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewExpenditurevoucherName,
		        tooltipPrefix + msg.mainMenuNewExpenditurevoucher, Icon.ICON_EXPENDITURE_VOUCHER_NEW.getImage(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER), params);	
        params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ReceiptVoucherEditor.ID);
		createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewReceiptvoucherName,
		        tooltipPrefix + msg.mainMenuNewReceiptvoucher, Icon.ICON_RECEIPT_VOUCHER_NEW.getImage(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_RECEIPTVOUCHER), params);	
		finishToolbar(coolBar, toolBar3);
//        coolbarmgr.add(toolbarmgr3);

		ToolBar toolBar4 = new ToolBar(coolBar, SWT.FLAT);
        IToolBarManager toolbarmgr4 = new ToolBarManager(toolBar4);
		createToolItem(toolBar4, CommandIds.CMD_OPEN_PARCEL_SERVICE, Icon.ICON_PARCEL_SERVICE.getImage(IconSize.ToolbarIconSize),
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_PARCELSERVICE));
		
		params = new HashMap<>();
		params.put(OpenBrowserEditorHandler.PARAM_USE_PROJECT_URL, Boolean.toString(true));
		createToolItem(toolBar4, CommandIds.CMD_OPEN_BROWSER_EDITOR, msg.commandOpenWwwName, msg.commandBrowserTooltip, Icon.ICON_WWW.getImage(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_BROWSER), params);	
		
		createToolItem(toolBar4, CommandIds.CMD_OPEN_CALCULATOR, msg.commandCalculatorName, msg.commandCalculatorTooltip,
				Icon.ICON_CALCULATOR.getImage(IconSize.ToolbarIconSize), null,
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_CALCULATOR), null);	
		
		createToolItem(toolBar4, CommandIds.CMD_QRK_EXPORT, msg.commandExportQrkName, msg.commandExportQrkTooltip,
				Icon.ICON_QRK_EXPORT.getImage(IconSize.ToolbarIconSize), null,
				preferences.getBoolean(Constants.TOOLBAR_SHOW_QRK_EXPORT), null);	
		finishToolbar(coolBar, toolBar4);	
//        coolbarmgr.add(toolbarmgr4);
	}

    /**
     * Creates the command params.
     *
     * @param docType the doc type
     * @return the map< string, object>
     */
    private Map<String, Object> createCommandParams(DocumentType docType) {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, DocumentEditor.ID);
        params.put(CallEditor.PARAM_CATEGORY, docType.name());
        return params;
    }

    /**
     * Handle dialog selection.
     *
     * @param event the event
     */
    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleDialogSelection(@UIEventTopic("EditorPart/updateCoolBar") Event event) {
        if (event != null) {
            updateCoolbar();
        }
	}

    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void recreateCoolBar(@UIEventTopic("EditorPart/recreateCoolBar") Event event) {
        if (event != null) {
        	// doesn't work :-( => the coolbar is completely empty after re-creating...
//            coolbarmgr.removeAll();
//            createControls(top);
        }
	}

	/**
	 * Update coolbar.
	 */
	private void updateCoolbar() {
		for (ToolBar toolBar : coolBarsByKey) {
		    for (ToolItem toolItem : toolBar.getItems()) {
		        ParameterizedCommand pCmd = (ParameterizedCommand) toolItem.getData(TOOLITEM_COMMAND);
		        if(pCmd != null) {
		            toolItem.setEnabled(pCmd.getCommand().isEnabled());
		        }
		    }
		}
	}
	
    @Inject
    @Optional
    void dirtyChanged(@UIEventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) Event eventData) {
        updateCoolbar();
    }
	
//
//    @Inject
//    @Optional
//    public void reactOnPrefColorChange(@Preference(value = Constants.TOOLBAR_SHOW_SAVE) Boolean colorKey) {
//        log.debug("React on a change in preferences with colorkey = " + colorKey);
//        if ((top != null) && !top.isDisposed()) {
//            toolItemsByKey.get("org.eclipse.ui.file.save"/*Constants.TOOLBAR_SHOW_SAVE*/).dispose();
//        }
//    }

	/**
	 * Packs the toolbar and adds it to the Coolbar.
	 * 
	 * @param coolbar
	 * @param toolBar
	 */
	private void finishToolbar(CoolBar coolbar, ToolBar toolBar) {
		toolBar.pack();
		CoolItem coolItem = new CoolItem(coolbar, SWT.DROP_DOWN);
		coolItem.setControl(toolBar);
	    calcSize(coolItem);
	    
	    // Add a listener to handle clicks on the chevron button
	    coolItem.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	        calcSize(coolItem);
	      }
	    });
	    
	    coolBarsByKey.add(toolBar);
	}
	 
	  /**
	   * Helper method to calculate the size of the cool item
	   * 
	   * @param item the cool item
	   */
	  private void calcSize(CoolItem item) {
	    Control control = item.getControl();
	    Point pt = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    pt = item.computeSize(pt.x, pt.y);
	    item.setSize(pt);
	  }

	private ToolItem createToolItem(final ToolBar toolBar, final String commandId, 
			final Image iconImage, boolean show) {
		return createToolItem(toolBar, commandId, null, null, iconImage, null, show, null);
	}
	
	private ToolItem createToolItem(final ToolBar toolBar, final String commandId, 
			final Image iconImage, final Image disabledIcon, boolean show) {
		return createToolItem(toolBar, commandId, null, null, iconImage, disabledIcon, show, null);
	}
	
	private ToolItem createToolItem(final ToolBar toolBar, final String commandId, 
			final String commandName, final String tooltip, final Image iconImage, final Image disabledIcon, boolean show,
			Map<String, Object> params) {
	    return createToolItem(toolBar, commandId, commandName, tooltip, iconImage, disabledIcon, show, params, false);
	}
	    
	/**
	 * Creates an icon in the given tool bar.
	 * 
	 * @param coolbar which coolbar to use
	 * @param toolBar on which toolbar the icon should be placed
	 * @param commandId which command id should be used
	 * @param tooltip nice tooltip
	 * @param iconImage image for the icon 
	 * @param disabledIcon if it is disabled, which icon should be displayed?
	 * @param show if <code>false</code>, the icon is hidden (configurable via preferences)
	 * @param withChevron <code>true</code> if a chevron button should be created
	 */
	private ToolItem createToolItem(final ToolBar toolBar, final String commandId, 
			final String commandName, final String tooltip, final Image iconImage, final Image disabledIcon, boolean show,
			Map<String, Object> params, boolean withChevron) {
	    
	    if(!show) {
	        return null;
	    }
	    
		ToolItem item;
		if(withChevron) {
    		item = new ToolItem(toolBar, SWT.BORDER | SWT.DROP_DOWN);
		} else {
		    item = new ToolItem(toolBar, SWT.PUSH | SWT.BORDER);
		}
        
//        // TODO das ist für die Verwendung von CoolBarManager / ToolBarManagaer,
//        // damit man einzelne Icons anzeigen/verstecken kann. Siehe Snippet Snippet140BisWithDynamicChanges.
//        IAction ac = new Action("id", Icon.ICON_PARCEL_SERVICE.getImageDescriptor(IconSize.ToolbarIconSize)) {
//            
//            @Override
//            public void run() {
//                System.out.println("mpf");
//            }
//        };
//        ActionContributionItem i = new ActionContributionItem(ac);
//        toolbarmgr.add(i);
		
        final ParameterizedCommand pCmd = cmdService.createCommand(commandId, params);
		try {
			if(pCmd != null) {
				item.setText(commandName != null ? commandName : pCmd.getCommand().getName());
				item.setToolTipText((tooltip != null) ? tooltip : pCmd.getCommand().getDescription());
				if(disabledIcon != null) {
					item.setDisabledImage(disabledIcon);
				}
				
				item.setEnabled(pCmd.getCommand().isEnabled());
				item.setData(TOOLITEM_COMMAND, pCmd);
			} else {
				// this *MUST* be a great error!
				log.error("No command found for " + commandId + " (" + commandName + ")" + ". Please check your Applicationmodel!");
			}
			
		}
		catch (NotDefinedException e1) {
			log.error(e1, "Fehler!");
		}
		item.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
			if (handlerService.canExecute(pCmd)) {
				final IEclipseContext staticContext = EclipseContextFactory.create("fakturama-static-context");
				
				// if CTRL key is pressed then we try to duplicate the current editor into a new one
				if ((e.stateMask & SWT.MOD1) == SWT.MOD1) {
					// does only work under certain circumstances
					ParameterizedCommand duplicateCmd = cmdService.createCommand(CommandIds.CMD_OBJECT_DUPLICATE);
					if(handlerService.canExecute(duplicateCmd)) {
						MPart activePart = partService.getActivePart();
						// item has to correspond to the active editor!
						if (activePart != null && activePart.getObject() instanceof Editor) {
							String editorType = (String) pCmd.getParameterMap().get(CallEditor.PARAM_EDITOR_TYPE);
							if (editorType != null && activePart.getElementId().equalsIgnoreCase(editorType)) {
								staticContext.set(CallEditor.PARAM_COPY, Boolean.TRUE);
								staticContext.set(CallEditor.PARAM_FORCE_NEW, Boolean.FALSE);
								staticContext.set(CallEditor.PARAM_OBJ_ID,
										activePart.getTransientData().get(CallEditor.PARAM_OBJ_ID));
							}
						}
					}
				} else {
			        // if called from CoolBar it is *always* a new one...
                    staticContext.set(CallEditor.PARAM_FORCE_NEW, Boolean.TRUE);
                    // NOTE: You can't set it if it was set before. Therefore we set it here.
				}
				/*
				 * Dirty hack. The HandlerService first determines the active leaf in the
				 * current context before it is executing the command. The current active leaf
				 * in the context is the document editor. But in its context there's a setting
				 * for the "category" parameter. But this parameter is not reasonable e.g. for
				 * new product editors. Therefore we have to remove it from context. The
				 * alternative would be to set another active leaf, but I didn't get it.
				 */
					if(ctx != null && ctx.getActiveLeaf() != null) {
						ctx.getActiveLeaf().remove(CallEditor.PARAM_CATEGORY);
					}
					
					// clear SelectionService so that following calls don't get confused (esp. CallEditor)
					// Important: Use the correct SelectionService from WorkbenchContext!
					ctx.getParent().get(ESelectionService.class).setSelection(null);
					ctx.get(ESelectionService.class).setSelection(null);
					handlerService.executeHandler(pCmd, staticContext);
			} else {
				MessageDialog.openInformation(toolBar.getShell(),
						"Action Info", "current action can't be executed!");
			}
		}));
        item.setImage(iconImage);
        return item;
	}
}
