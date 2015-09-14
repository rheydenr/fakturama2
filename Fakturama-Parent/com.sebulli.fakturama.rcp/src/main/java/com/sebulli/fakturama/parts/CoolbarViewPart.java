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
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.handlers.OpenBrowserEditorHandler;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * Composite for the Coolbar. Since this couldn't configured via Application Model we have to implement it ourself.
 * The Application Model Toolbar doesn't support text below button (or you write a new renderer, what I didn't want).
 * 
 * @author R. Heydenreich
 *
 */
public class CoolbarViewPart {
    
    private static final String TOOLITEM_COMMAND = "toolitem_command";

	@Inject
	private ECommandService cmdService;
	
	@Inject
	private EHandlerService handlerService;

    @Inject
    private IPreferenceStore preferences;
	
	@Inject
	private Logger log;
	
	@Inject
	@Translation
	protected Messages msg;

    private Composite top;
    
    private Set<ToolBar> coolBarsByKey = new HashSet<>();

    private CoolBarManager coolbarmgr;
    
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
	 */
	@PostConstruct
	public void createControls(Composite parent) {
	    this.top = parent;
	    // now lets create our CoolBar
		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		top.setLayout(layout);
		top.setSize(SWT.DEFAULT, 70);

		coolbarmgr = new CoolBarManager(SWT.NONE);
		IToolBarManager toolbarmgr = new ToolBarManager(SWT.FLAT | SWT.BOTTOM);
		coolbarmgr.add(toolbarmgr);
		
		CoolBar coolbar1 = coolbarmgr.createControl(top);
		ToolBar toolBar1 = new ToolBar(coolbar1, SWT.FLAT);
		
		/*
		 * Leider gibt es keine vernünftige Verbindung zw. (altem) Command und der entsprechenden Preference.
		 * deswegen müssen wir ein händisches Mapping erstellen, das so aussieht (Beispiel):
		 * 
		 * "TOOLBAR_SHOW_WEBSHOP" => CommandIds.CMD_WEBSHOP_IMPORT
		 * 
		 * Das muß man dann beim Erstellen des Icons über den Preference-Store abfragen, da die Einstellung dort
		 * beim Hochfahren der Anwendung bzw. beim Migrieren schon hinterlegt wurde.
		 */
		createToolItem(toolBar1, CommandIds.CMD_WEBSHOP_IMPORT, 
				Icon.ICON_SHOP.getImage(IconSize.ToolbarIconSize), preferences.getBoolean(Constants.TOOLBAR_SHOW_WEBSHOP));
		/*ToolItem ooPrintButton = */createToolItem(toolBar1, "org.eclipse.ui.file.print"/*IWorkbenchCommandConstants.FILE_PRINT*/, 
				Icon.ICON_PRINTOO.getImage(IconSize.ToolbarIconSize), Icon.ICON_PRINTOO_DIS.getImage(IconSize.ToolbarIconSize),
				preferences.getBoolean(Constants.TOOLBAR_SHOW_PRINT));
//		ooPrintButton.addSelectionListener(new SelectionAdapter() {
//		    @Override
//		    public void widgetSelected(SelectionEvent e) {
//		        
//		        super.widgetSelected(e);
//		    }
//        });
		createToolItem(toolBar1, "org.eclipse.ui.file.save"/*IWorkbenchCommandConstants.FILE_SAVE*/, 
				Icon.ICON_SAVE.getImage(IconSize.ToolbarIconSize), Icon.ICON_SAVE_DIS.getImage(IconSize.ToolbarIconSize),
				preferences.getBoolean(Constants.TOOLBAR_SHOW_SAVE));
		finishToolbar(coolbar1, toolBar1);
			
		ToolBar toolBar2 = new ToolBar(coolbar1, SWT.FLAT);
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
		createToolItem(toolBar2, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDocumentProformaName, 
				tooltipPrefix + msg.mainMenuNewProforma, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
				, null, preferences.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_PROFORMA), createCommandParams(DocumentType.PROFORMA));
		finishToolbar(coolbar1, toolBar2);

		ToolBar toolBar3 = new ToolBar(coolbar1, SWT.FLAT);
        createToolItem(toolBar3, CommandIds.CMD_NEW_PRODUCT, Icon.ICON_PRODUCT_NEW.getImage(IconSize.ToolbarIconSize),
                preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_PRODUCT));    
        
        Map<String, Object> params = new HashMap<>();
		createToolItem(toolBar3, CommandIds.CMD_NEW_CONTACT, msg.toolbarNewContactName, msg.commandNewContactTooltip, 
		        Icon.ICON_CONTACT_NEW.getImage(IconSize.ToolbarIconSize), null,
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_CONTACT), params);

        params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ExpenditureVoucherEditor.ID);
		createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewExpenditurevoucherName,
		        tooltipPrefix + msg.mainMenuNewExpenditurevoucher, Icon.ICON_EXPENDITURE_NEW.getImage(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER), params);	
        params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, ReceiptVoucherEditor.ID);
		createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewReceiptvoucherName,
		        tooltipPrefix + msg.mainMenuNewReceiptvoucher, Icon.ICON_RECEIPT_VOUCHER_NEW.getImage(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_NEW_RECEIPTVOUCHER), params);	
		finishToolbar(coolbar1, toolBar3);
		
		ToolBar toolBar4 = new ToolBar(coolbar1, SWT.FLAT);
		createToolItem(toolBar4, CommandIds.CMD_OPEN_PARCEL_SERVICE, Icon.ICON_PARCEL_SERVICE.getImage(IconSize.ToolbarIconSize),
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_PARCELSERVICE));
		
		params = new HashMap<>();
		params.put(OpenBrowserEditorHandler.PARAM_USE_PROJECT_URL, Boolean.toString(true));
		createToolItem(toolBar4, CommandIds.CMD_OPEN_BROWSER_EDITOR, msg.commandOpenWwwName, msg.commandBrowserTooltip, Icon.ICON_WWW.getImage(IconSize.ToolbarIconSize),
		        null, preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_BROWSER), params);	
		
		createToolItem(toolBar4, CommandIds.CMD_OPEN_CALCULATOR, Icon.ICON_CALCULATOR.getImage(IconSize.ToolbarIconSize), 
		        preferences.getBoolean(Constants.TOOLBAR_SHOW_OPEN_CALCULATOR));	
		finishToolbar(coolbar1, toolBar4);	
	}

    private Map<String, Object> createCommandParams(DocumentType docType) {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, DocumentEditor.ID);
        params.put(CallEditor.PARAM_CATEGORY, docType.name());
        return params;
    }

	@Inject
	@Optional
	public void handleEvent(@UIEventTopic("TOOLBARPREFS") String msg) {
	    // doesn't work :-(
//	    coolbarmgr.update(true);
//	    createControls(top);
	}
	
    @Inject
    @Optional
    void dirtyChanged(@UIEventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) Event eventData) {
        for (ToolBar toolBar : coolBarsByKey) {
            for (ToolItem toolItem : toolBar.getItems()) {
                ParameterizedCommand pCmd = (ParameterizedCommand) toolItem.getData(TOOLITEM_COMMAND);
                if(pCmd != null) {
                    toolItem.setEnabled(pCmd.getCommand().isEnabled());
                }
            }
        }
    }
	
//
//    @Inject
//    @Optional
//    public void reactOnPrefColorChange(@Preference(value = Constants.TOOLBAR_SHOW_SAVE) Boolean colorKey) {
//        System.out.println("React on a change in preferences with colorkey = " + colorKey);
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
	    Point size = toolBar.getSize();
		CoolItem coolItem = new CoolItem(coolbar, SWT.NONE);
		coolItem.setControl(toolBar);
	    Point preferred = coolItem.computeSize(size.x, size.y);
	    coolItem.setPreferredSize(preferred);
	    
        coolBarsByKey.add(toolBar);
	}
	
	private ToolItem createToolItem(final ToolBar toolBar, final String commandId, 
			final Image iconImage, boolean show) {
		return createToolItem(toolBar, commandId, null, null, iconImage, null, show, null);
	}
	
	private ToolItem createToolItem(final ToolBar toolBar, final String commandId, 
			final Image iconImage, final Image disabledIcon, boolean show) {
		return createToolItem(toolBar, commandId, null, null, iconImage, disabledIcon, show, null);
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
	 */
	private ToolItem createToolItem(final ToolBar toolBar, final String commandId, 
			final String commandName, final String tooltip, final Image iconImage, final Image disabledIcon, boolean show,
			Map<String, Object> params) {
	    
	    if(!show) {
	        return null;
	    }
	    
		ToolItem item = new ToolItem(toolBar, SWT.PUSH);
        final ParameterizedCommand pCmd = cmdService.createCommand(commandId, params);
		try {
			item.setText(commandName != null ? commandName : pCmd.getCommand().getName());
			item.setToolTipText((tooltip != null) ? tooltip : pCmd.getCommand().getDescription());
			if(disabledIcon != null) {
				item.setDisabledImage(disabledIcon);
			}
			
			item.setEnabled(pCmd.getCommand().isEnabled());
			item.setData(TOOLITEM_COMMAND, pCmd);
		}
		catch (NotDefinedException e1) {
			log.error(e1, "Fehler! ");
		}
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (handlerService.canExecute(pCmd)) {
					handlerService.executeHandler(pCmd);
				} else {
					MessageDialog.openInformation(toolBar.getShell(),
							"Action Info", "current action can't be executed!");
				}
			}
		});
        item.setImage(iconImage);
        return item;
	}
}
