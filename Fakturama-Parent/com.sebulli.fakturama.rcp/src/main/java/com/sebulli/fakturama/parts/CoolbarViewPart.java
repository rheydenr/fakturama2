package com.sebulli.fakturama.parts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
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

import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
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

	@Inject
	private ECommandService cmdService;
	
	@Inject
	private EHandlerService handlerService;
	
	@Inject
	@Preference
	private IEclipsePreferences eclipsePrefs;
	
	@Inject
    @Preference(nodePath=Constants.DEFAULT_PREFERENCES_NODE)
	private IEclipsePreferences eclipseDefaultPrefs;
	
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
				Icon.ICON_SHOP.getImage(IconSize.ToolbarIconSize), getPreference(Constants.TOOLBAR_SHOW_WEBSHOP));
		createToolItem(toolBar1, "org.eclipse.ui.file.print"/*IWorkbenchCommandConstants.FILE_PRINT*/, 
				Icon.ICON_PRINTOO.getImage(IconSize.ToolbarIconSize), Icon.ICON_PRINTOO_DIS.getImage(IconSize.ToolbarIconSize),
				getPreference(Constants.TOOLBAR_SHOW_PRINT));
		createToolItem(toolBar1, "org.eclipse.ui.file.save"/*IWorkbenchCommandConstants.FILE_SAVE*/, 
				Icon.ICON_SAVE.getImage(IconSize.ToolbarIconSize), Icon.ICON_SAVE_DIS.getImage(IconSize.ToolbarIconSize),
				getPreference(Constants.TOOLBAR_SHOW_SAVE));
		finishToolbar(coolbar1, toolBar1);
			
		ToolBar toolBar2 = new ToolBar(coolbar1, SWT.FLAT);
		String tooltipPrefix = msg.commandNewTooltip + " ";
		createToolItem(toolBar2, CommandIds.CMD_NEW_LETTER, 
				tooltipPrefix + msg.mainMenuNewLetter, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_LETTER));
		createToolItem(toolBar2, CommandIds.CMD_NEW_OFFER, 
				tooltipPrefix + msg.mainMenuNewOffer, Icon.ICON_OFFER_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_OFFER));
		createToolItem(toolBar2, CommandIds.CMD_NEW_ORDER, 
				tooltipPrefix + msg.mainMenuNewOrder, Icon.ICON_ORDER_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_ORDER));
		createToolItem(toolBar2, CommandIds.CMD_NEW_CONFIRMATION, 
				tooltipPrefix + msg.mainMenuNewConfirmation, Icon.ICON_CONFIRMATION_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_CONFIRMATION));
		createToolItem(toolBar2, CommandIds.CMD_NEW_INVOICE, 
				tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_INVOICE));
		createToolItem(toolBar2, CommandIds.CMD_NEW_DELIVERY, 
				tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_DELIVERY));
		createToolItem(toolBar2, CommandIds.CMD_NEW_CREDIT, 
				tooltipPrefix + msg.mainMenuNewCredit, Icon.ICON_CREDIT_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_CREDIT));
		createToolItem(toolBar2, CommandIds.CMD_NEW_DUNNING, 
				tooltipPrefix + msg.mainMenuNewDunning, Icon.ICON_DUNNING_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_DUNNING));
		createToolItem(toolBar2, CommandIds.CMD_NEW_PROFORMA, 
				tooltipPrefix + msg.mainMenuNewProforma, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
				, getPreference(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_PROFORMA));
		finishToolbar(coolbar1, toolBar2);

		ToolBar toolBar3 = new ToolBar(coolbar1, SWT.FLAT);
		createToolItem(toolBar3, CommandIds.CMD_NEW_PRODUCT, Icon.ICON_PRODUCT_NEW.getImage(IconSize.ToolbarIconSize),
		        getPreference(Constants.TOOLBAR_SHOW_NEW_PRODUCT));	
		
		Map<String, Object> params = new HashMap<>();
		params.put("com.sebulli.fakturama.editors.editortype", ContactEditor.ID);
//		createToolItem(toolBar3, CommandIds.CMD_CALL_EDITOR, Icon.ICON_CONTACT_NEW.getImage(IconSize.ToolbarIconSize),
//		        getPreference(Constants.TOOLBAR_SHOW_NEW_CONTACT), params);
		createToolItem(toolBar3, CommandIds.CMD_NEW_CONTACT, Icon.ICON_CONTACT_NEW.getImage(IconSize.ToolbarIconSize),
		        getPreference(Constants.TOOLBAR_SHOW_NEW_CONTACT));
		
		createToolItem(toolBar3, CommandIds.CMD_NEW_EXPENDITUREVOUCHER, Icon.ICON_EXPENDITURE_NEW.getImage(IconSize.ToolbarIconSize),
		        getPreference(Constants.TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER));	
		createToolItem(toolBar3, CommandIds.CMD_NEW_RECEIPTVOUCHER, Icon.ICON_RECEIPT_VOUCHER_NEW.getImage(IconSize.ToolbarIconSize),
		        getPreference(Constants.TOOLBAR_SHOW_NEW_RECEIPTVOUCHER));	
		finishToolbar(coolbar1, toolBar3);
		
		ToolBar toolBar4 = new ToolBar(coolbar1, SWT.FLAT);
		createToolItem(toolBar4, CommandIds.CMD_OPEN_PARCEL_SERVICE, Icon.ICON_PARCEL_SERVICE.getImage(IconSize.ToolbarIconSize),
		        getPreference(Constants.TOOLBAR_SHOW_OPEN_PARCELSERVICE));	
		createToolItem(toolBar4, CommandIds.CMD_OPEN_BROWSER_EDITOR, Icon.ICON_WWW.getImage(IconSize.ToolbarIconSize),
		        getPreference(Constants.TOOLBAR_SHOW_OPEN_BROWSER));	
		createToolItem(toolBar4, CommandIds.CMD_OPEN_CALCULATOR, Icon.ICON_CALCULATOR.getImage(IconSize.ToolbarIconSize), 
		        getPreference(Constants.TOOLBAR_SHOW_OPEN_CALCULATOR));	
		finishToolbar(coolbar1, toolBar4);	
	}

    /**
     * Helper method for getting a preference. At first, it looks in the regular Eclipse
     * preference store. If there's no preference, then look into default preferences (set while 
     * booting the application in DefaultValuesInitializer). If then not found, return <code>false</code>.
     *   
     * @return preference value, either set via preferences page or the default value set from Initializer
     */
    private boolean getPreference(String pref) {
        return eclipsePrefs.getBoolean(pref, eclipseDefaultPrefs.getBoolean(pref, false));
    }

	@Inject
	@Optional
	public void handleEvent(@UIEventTopic("TOOLBARPREFS") String msg) {
	    // doesn't work :-(
//	    coolbarmgr.update(true);
//	    createControls(top);
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
	
	private void createToolItem(final ToolBar toolBar, final String commandId, 
			final Image iconImage, boolean show) {
		createToolItem(toolBar, commandId, null, iconImage, null, show, null);
	}
	
	private void createToolItem(final ToolBar toolBar, final String commandId, 
			final Image iconImage, boolean show, Map<String, Object> params) {
		createToolItem(toolBar, commandId, null, iconImage, null, show, params);
	}
	
	private void createToolItem(final ToolBar toolBar, final String commandId, 
			final Image iconImage, final Image disabledIcon, boolean show) {
		createToolItem(toolBar, commandId, null, iconImage, disabledIcon, show, null);
	}

	private void createToolItem(final ToolBar toolBar, final String commandId, 
			final String tooltip, final Image iconImage, boolean show) {
		createToolItem(toolBar, commandId, tooltip, iconImage, null, show, null);
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
	private void createToolItem(final ToolBar toolBar, final String commandId, 
			final String tooltip, final Image iconImage, final Image disabledIcon, boolean show,
			Map<String, Object> params) {
	    
	    if(!show) {
	        return;
	    }
	    
		ToolItem item = new ToolItem(toolBar, SWT.PUSH);
        final ParameterizedCommand pCmd = cmdService.createCommand(commandId, params);
		try {
			item.setText(pCmd.getCommand().getName());
			item.setToolTipText((tooltip != null) ? tooltip : pCmd.getCommand().getDescription());
			if(disabledIcon != null) {
				item.setDisabledImage(disabledIcon);
			}
			
			item.setEnabled(pCmd.getCommand().isEnabled());
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
	}
}
