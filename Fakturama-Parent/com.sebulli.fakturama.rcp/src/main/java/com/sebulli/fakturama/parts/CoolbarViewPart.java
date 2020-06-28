package com.sebulli.fakturama.parts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
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

    private CoolBarManager coolbarmgr;

    private ToolBarManager toolbarmgr;
    
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

		coolbarmgr = new CoolBarManager(SWT.FLAT);
		
		CoolBar coolBar = coolbarmgr.createControl(top);
		toolbarmgr = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		ToolBar toolBar1 = toolbarmgr.createControl(coolBar);
		/*
		 * Leider gibt es keine vernünftige Verbindung zw. (altem) Command und der entsprechenden Preference.
		 * deswegen müssen wir ein händisches Mapping erstellen, das so aussieht (Beispiel):
		 * 
		 * "TOOLBAR_SHOW_WEBSHOP" => CommandIds.CMD_WEBSHOP_IMPORT
		 * 
		 * Das muß man dann beim Erstellen des Icons über den Preference-Store abfragen, da die Einstellung dort
		 * beim Hochfahren der Anwendung bzw. beim Migrieren schon hinterlegt wurde.
		 */
		coolbarmgr.add(toolbarmgr);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(WebShopCallHandler.PARAM_IS_GET_PRODUCTS, Boolean.TRUE);
        parameters.put(WebShopCallHandler.PARAM_ACTION, WebShopCallHandler.WEBSHOP_CONNECTOR_ACTION_IMPORT);
        
        createToolItem(toolBar1, CommandIds.CMD_WEBSHOP_IMPORT, msg.commandWebshopName, msg.commandWebshopTooltip,
				Icon.ICON_SHOP.getImageDescriptor(IconSize.ToolbarIconSize), null, preferences.getBoolean(Constants.TOOLBAR_SHOW_WEBSHOP), 
				parameters).ifPresent(e -> toolbarmgr.add(e));
		
        createToolItem(toolBar1, "org.fakturama.print.oofile"/*IWorkbenchCommandConstants.FILE_PRINT*/, 
				Icon.ICON_PRINTOO.getImageDescriptor(IconSize.ToolbarIconSize), Icon.ICON_PRINTOO_DIS.getImageDescriptor(IconSize.ToolbarIconSize),
				preferences.getBoolean(Constants.TOOLBAR_SHOW_PRINT)).ifPresent(e -> toolbarmgr.add(e));

        createToolItem(toolBar1, "org.eclipse.ui.file.save"/*IWorkbenchCommandConstants.FILE_SAVE*/, 
				Icon.ICON_SAVE.getImageDescriptor(IconSize.ToolbarIconSize), Icon.ICON_SAVE_DIS.getImageDescriptor(IconSize.ToolbarIconSize),
				preferences.getBoolean(Constants.TOOLBAR_SHOW_SAVE)).ifPresent(e -> toolbarmgr.add(e));
		finishToolbar(coolBar, toolBar1);
//        coolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        ToolBar toolBar2 = new ToolBar(coolBar, SWT.FLAT);
        ToolBarManager toolbarmgr2 = new ToolBarManager(toolBar2);
		String tooltipPrefix = msg.commandNewTooltip + " ";
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewLetterName,
				tooltipPrefix + msg.mainMenuNewLetter, Icon.ICON_LETTER_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_LETTER), createCommandParams(DocumentType.LETTER)).ifPresent(e -> toolbarmgr.add(e));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewOfferName,
				tooltipPrefix + msg.mainMenuNewOffer, Icon.ICON_OFFER_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_OFFER), createCommandParams(DocumentType.OFFER)).ifPresent(e -> toolbarmgr.add(e));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewOrderName,
				tooltipPrefix + msg.mainMenuNewOrder, Icon.ICON_ORDER_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_ORDER), createCommandParams(DocumentType.ORDER)).ifPresent(e -> toolbarmgr.add(e));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewConfirmationName,
				tooltipPrefix + msg.mainMenuNewConfirmation, Icon.ICON_CONFIRMATION_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_CONFIRMATION), createCommandParams(DocumentType.CONFIRMATION)).ifPresent(e -> toolbarmgr.add(e));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
				tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_INVOICE), createCommandParams(DocumentType.INVOICE)).ifPresent(e -> toolbarmgr.add(e));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDeliveryName,
				tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_DELIVERY), createCommandParams(DocumentType.DELIVERY)).ifPresent(e -> toolbarmgr.add(e));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewCreditName, 
				tooltipPrefix + msg.mainMenuNewCredit, Icon.ICON_CREDIT_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_CREDIT), createCommandParams(DocumentType.CREDIT)).ifPresent(e -> toolbarmgr.add(e));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDocumentDunningName, 
				tooltipPrefix + msg.mainMenuNewDunning, Icon.ICON_DUNNING_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_DUNNING), createCommandParams(DocumentType.DUNNING)).ifPresent(e -> toolbarmgr.add(e));
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.documentTypeProforma, 
				tooltipPrefix + msg.documentTypeProforma, Icon.ICON_PROFORMA_NEW.getImageDescriptor(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_PROFORMA), createCommandParams(DocumentType.PROFORMA)).ifPresent(e -> toolbarmgr.add(e));
		finishToolbar(coolBar, toolBar2);
        coolbarmgr.add(toolbarmgr2);
	    coolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBar toolBar3 = new ToolBar(coolBar, SWT.FLAT);
        IToolBarManager toolbarmgr3 = new ToolBarManager(toolBar3);
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ProductEditor.ID);
        createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewProductName, msg.commandNewProductTooltip, 
        		Icon.ICON_PRODUCT_NEW.getImageDescriptor(IconSize.ToolbarIconSize), null,
                preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_PRODUCT), params).ifPresent(e -> toolbarmgr.add(e));    

        params = new HashMap<>();
		java.util.Optional<ActionContributionItem> contactToolItem = createToolItem(toolBar3, CommandIds.CMD_NEW_CONTACT, msg.toolbarNewContactName, msg.commandNewContactTooltip, 
		        Icon.ICON_CONTACT_NEW.getImageDescriptor(IconSize.ToolbarIconSize), null,
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_CONTACT), params, true);
		DropdownSelectionListener contactTypeListener =  ContextInjectionFactory.make(DropdownSelectionListener.class, ctx);
		contactTypeListener.setDefaultCommandId(DebitorEditor.ID);
        contactTypeListener.add(new DropdownMenuItem(msg.commandNewCreditorName, CreditorEditor.ID, Icon.COMMAND_VENDOR));
        contactTypeListener.add(new DropdownMenuItem(msg.commandNewDebtorName, DebitorEditor.ID, Icon.COMMAND_CONTACT));
		contactToolItem.get().getAction().setMenuCreator(contactTypeListener);
		((FakturamaCoolbarAction)contactToolItem.get().getAction()).setDefaultAction(DebitorEditor.ID);
        contactToolItem.ifPresent(e -> toolbarmgr.add(e));

        params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ExpenditureVoucherEditor.ID);
		createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewExpenditurevoucherName,
		        tooltipPrefix + msg.mainMenuNewExpenditurevoucher, Icon.ICON_EXPENDITURE_VOUCHER_NEW.getImageDescriptor(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER), params).ifPresent(e -> toolbarmgr.add(e));	
        params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ReceiptVoucherEditor.ID);
		createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewReceiptvoucherName,
		        tooltipPrefix + msg.mainMenuNewReceiptvoucher, Icon.ICON_RECEIPT_VOUCHER_NEW.getImageDescriptor(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_RECEIPTVOUCHER), params).ifPresent(e -> toolbarmgr.add(e));
		finishToolbar(coolBar, toolBar3);
        coolbarmgr.add(toolbarmgr3);

		ToolBar toolBar4 = new ToolBar(coolBar, SWT.FLAT);
        IToolBarManager toolbarmgr4 = new ToolBarManager(toolBar4);
		createToolItem(toolBar4, CommandIds.CMD_OPEN_PARCEL_SERVICE, Icon.ICON_PARCEL_SERVICE.getImageDescriptor(IconSize.ToolbarIconSize),
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_PARCELSERVICE)).ifPresent(e -> toolbarmgr.add(e));
		
		params = new HashMap<>();
		params.put(OpenBrowserEditorHandler.PARAM_USE_PROJECT_URL, Boolean.toString(true));
		createToolItem(toolBar4, CommandIds.CMD_OPEN_BROWSER_EDITOR, msg.commandOpenWwwName, msg.commandBrowserTooltip, Icon.ICON_WWW.getImageDescriptor(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_BROWSER), params).ifPresent(e -> toolbarmgr.add(e));
		
		createToolItem(toolBar4, CommandIds.CMD_OPEN_CALCULATOR, msg.commandCalculatorName, msg.commandCalculatorTooltip,
				Icon.ICON_CALCULATOR.getImageDescriptor(IconSize.ToolbarIconSize), null,
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_CALCULATOR), null).ifPresent(e -> toolbarmgr.add(e));	
		
		createToolItem(toolBar4, CommandIds.CMD_QRK_EXPORT, msg.commandExportQrkName, msg.commandExportQrkTooltip,
				Icon.ICON_QRK_EXPORT.getImageDescriptor(IconSize.ToolbarIconSize), null,
				preferences.getBoolean(Constants.TOOLBAR_SHOW_QRK_EXPORT), null).ifPresent(e -> toolbarmgr.add(e));
		finishToolbar(coolBar, toolBar4);	
        coolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        coolbarmgr.add(toolbarmgr4);

        // IMPORTANT: This line is for showing the chevron if application frame is resizing 
        // and the coolbar doesn't fit in its whole width.
        Arrays.stream(coolbarmgr.getItems()).forEach(item -> ((ToolBarContributionItem) item).setMinimumItemsToShow(1));
        
        coolbarmgr.update(true);
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
	    coolbarmgr.update(true);
//		for (ToolBar toolBar : coolBarsByKey) {
//		    for (ToolItem toolItem : toolBar.getItems()) {
//		        ParameterizedCommand pCmd = (ParameterizedCommand) toolItem.getData(TOOLITEM_COMMAND);
//		        if(pCmd != null) {
//		            toolItem.setEnabled(pCmd.getCommand().isEnabled());
//		        }
//		    }
//		}
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
//		toolBar.pack();
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

	private java.util.Optional<ActionContributionItem> createToolItem(final ToolBar toolBar, final String commandId, 
			final ImageDescriptor iconImage, boolean show) {
		return createToolItem(toolBar, commandId, null, null, iconImage, null, show, null);
	}
	
	private java.util.Optional<ActionContributionItem> createToolItem(final ToolBar toolBar, final String commandId, 
			final ImageDescriptor iconImage, final ImageDescriptor disabledIcon, boolean show) {
		return createToolItem(toolBar, commandId, null, null, iconImage, disabledIcon, show, null);
	}
	
	private java.util.Optional<ActionContributionItem> createToolItem(final ToolBar toolBar, final String commandId, 
			final String commandName, final String tooltip, final ImageDescriptor iconImage, final ImageDescriptor disabledIcon, boolean show,
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
    private java.util.Optional<ActionContributionItem> createToolItem(final ToolBar toolBar, final String commandId, final String commandName, final String tooltip,
            final ImageDescriptor iconImage, final ImageDescriptor disabledIcon, boolean show, Map<String, Object> params, boolean withChevron) {

        if (!show) {
            return java.util.Optional.empty();
        }

        IAction item = null;
        ActionContributionItem actionContributionItem = null;

        final ParameterizedCommand pCmd = cmdService.createCommand(commandId, params);
        try {
            if (pCmd != null) {

                item = withChevron ? new FakturamaCoolbarAction(pCmd, toolBar, IAction.AS_DROP_DOWN_MENU) : new FakturamaCoolbarAction(pCmd, toolBar, IAction.AS_PUSH_BUTTON);
                
                item.setText(commandName != null ? commandName : pCmd.getCommand().getName());
                item.setToolTipText((tooltip != null) ? tooltip : pCmd.getCommand().getDescription());
                if(disabledIcon != null) {
                    item.setDisabledImageDescriptor(disabledIcon);
                }

                ContextInjectionFactory.inject(item, ctx);
                if (disabledIcon != null) {
                    item.setDisabledImageDescriptor(disabledIcon);
                }

//                item.setData(TOOLITEM_COMMAND, pCmd);
                actionContributionItem = new ActionContributionItem(item);
                actionContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
            } else {
                // this *MUST* be a great error!
                log.error("No command found for " + commandId + " (" + commandName + ")" + ". Please check your Applicationmodel!");
            }
            item.setImageDescriptor(iconImage);

        } catch (NotDefinedException e1) {
            log.error(e1, "Fehler!");
        }

        return java.util.Optional.ofNullable(actionContributionItem);
    }	

}
