package com.sebulli.fakturama.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.app.IApplicationContext;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchCommandConstants;

import com.sebulli.fakturama.Constants;
import com.sebulli.fakturama.handlers.ICommandIds;
import com.sebulli.fakturama.i18n.Messages;
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
	private Logger log;
	
	@Inject
	@Translation
	protected Messages msg;


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
	public void createControls(Composite parent, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			IApplicationContext appContext, IWorkbench workbench) {
	    		
		// now lets create our CoolBar
		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		parent.setLayout(layout);
		parent.setSize(SWT.DEFAULT, 70);

		// create cool bar
		CoolBarManager coolbarmgr = new CoolBarManager(SWT.NONE);
		IToolBarManager toolbarmgr = new ToolBarManager(SWT.FLAT | SWT.BOTTOM);
		coolbarmgr.add(toolbarmgr);
		
		CoolBar coolbar1 = coolbarmgr.createControl(parent);
		ToolBar toolBar1 = new ToolBar(coolbar1, SWT.FLAT);
		
		/*
		 * Leider gibt es keine vernünftige Verbindung zw. (altem) Command und der entsprechenden Preference.
		 * deswegen müssen wir ein händisches Mapping erstellen, das so aussieht (Beispiel):
		 * 
		 * "TOOLBAR_SHOW_WEBSHOP" => ICommandIds.CMD_WEBSHOP_IMPORT
		 * 
		 * Das muß man dann beim Erstellen des Icons über den Preference-Store abfragen, da die Einstellung dort
		 * beim Hochfahren der Anwendung bzw. beim Migrieren schon hinterlegt wurde.
		 */
		
		createToolItem(coolbar1, toolBar1, ICommandIds.CMD_WEBSHOP_IMPORT, 
				Icon.ICON_SHOP.getImage(IconSize.ToolbarIconSize), eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_WEBSHOP, true));
		createToolItem(coolbar1, toolBar1, IWorkbenchCommandConstants.FILE_PRINT, 
				Icon.ICON_PRINTOO.getImage(IconSize.ToolbarIconSize), Icon.ICON_PRINTOO_DIS.getImage(IconSize.ToolbarIconSize),
				eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_PRINT, true));
		createToolItem(coolbar1, toolBar1, IWorkbenchCommandConstants.FILE_SAVE, 
				Icon.ICON_SAVE.getImage(IconSize.ToolbarIconSize), Icon.ICON_SAVE_DIS.getImage(IconSize.ToolbarIconSize),
				eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_SAVE, true));
		finishToolbar(coolbar1, toolBar1);
			
		CoolBar coolbar2 = coolbarmgr.createControl(parent);
		ToolBar toolBar2 = new ToolBar(coolbar2, SWT.FLAT);
		String tooltipPrefix = msg.commandNewTooltip + " ";
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_LETTER, 
				tooltipPrefix + msg.mainMenuNewLetter, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_LETTER, true));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_OFFER, 
				tooltipPrefix + msg.mainMenuNewOffer, Icon.ICON_OFFER_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_OFFER, true));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_ORDER, 
				tooltipPrefix + msg.mainMenuNewOrder, Icon.ICON_ORDER_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_ORDER, true));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_CONFIRMATION, 
				tooltipPrefix + msg.mainMenuNewConfirmation, Icon.ICON_CONFIRMATION_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_CONFIRMATION, true));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_INVOICE, 
				tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_INVOICE, true));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_DELIVERY, 
				tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_DELIVERY, true));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_CREDIT, 
				tooltipPrefix + msg.mainMenuNewCredit, Icon.ICON_CREDIT_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_CREDIT, true));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_DUNNING, 
				tooltipPrefix + msg.mainMenuNewDunning, Icon.ICON_DUNNING_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_DUNNING, true));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_PROFORMA, 
				tooltipPrefix + msg.mainMenuNewProforma, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
				, eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_DOCUMENT_NEW_PROFORMA, true));
		finishToolbar(coolbar2, toolBar2);

		CoolBar coolbar3 = coolbarmgr.createControl(parent);
		ToolBar toolBar3 = new ToolBar(coolbar3, SWT.FLAT);
		createToolItem(coolbar3, toolBar3, ICommandIds.CMD_NEW_PRODUCT, Icon.ICON_PRODUCT_NEW.getImage(IconSize.ToolbarIconSize),
		        eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_NEW_PRODUCT, true));	
		createToolItem(coolbar3, toolBar3, ICommandIds.CMD_NEW_CONTACT, Icon.ICON_CONTACT_NEW.getImage(IconSize.ToolbarIconSize),
		        eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_NEW_CONTACT, true));	
		createToolItem(coolbar3, toolBar3, ICommandIds.CMD_NEW_EXPENDITUREVOUCHER, Icon.ICON_EXPENDITURE_NEW.getImage(IconSize.ToolbarIconSize),
		        eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER, true));	
		createToolItem(coolbar3, toolBar3, ICommandIds.CMD_NEW_RECEIPTVOUCHER, Icon.ICON_RECEIPT_VOUCHER_NEW.getImage(IconSize.ToolbarIconSize),
		        eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_NEW_RECEIPTVOUCHER, true));	
		finishToolbar(coolbar3, toolBar3);
		
		CoolBar coolbar4 = coolbarmgr.createControl(parent);
		ToolBar toolBar4 = new ToolBar(coolbar4, SWT.FLAT);
		createToolItem(coolbar4, toolBar4, ICommandIds.CMD_OPEN_PARCEL_SERVICE, Icon.ICON_PARCEL_SERVICE.getImage(IconSize.ToolbarIconSize),
		        eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_OPEN_PARCELSERVICE, true));	
		createToolItem(coolbar4, toolBar4, ICommandIds.CMD_OPEN_BROWSER_EDITOR, Icon.ICON_WWW.getImage(IconSize.ToolbarIconSize),
		        eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_OPEN_BROWSER, true));	
		createToolItem(coolbar4, toolBar4, ICommandIds.CMD_OPEN_CALCULATOR, Icon.ICON_CALCULATOR.getImage(IconSize.ToolbarIconSize), 
		        eclipsePrefs.getBoolean(Constants.TOOLBAR_SHOW_OPEN_CALCULATOR, true));	
		finishToolbar(coolbar4, toolBar4);	
	}


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
	}
	
	private void createToolItem(final CoolBar coolbar, final ToolBar toolBar, final String commandId, 
			final Image iconImage, boolean show) {
		createToolItem(coolbar, toolBar, commandId, null, iconImage, null, show);
	}
	
	private void createToolItem(final CoolBar coolbar, final ToolBar toolBar, final String commandId, 
			final Image iconImage, final Image disabledIcon, boolean show) {
		createToolItem(coolbar, toolBar, commandId, null, iconImage, disabledIcon, show);
	}

	private void createToolItem(final CoolBar coolbar, final ToolBar toolBar, final String commandId, 
			final String tooltip, final Image iconImage, boolean show) {
		createToolItem(coolbar, toolBar, commandId, tooltip, iconImage, null, show);
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
	private void createToolItem(final CoolBar coolbar, final ToolBar toolBar, final String commandId, 
			final String tooltip, final Image iconImage, final Image disabledIcon, boolean show) {
	    
	    if(!show) {
	        return;
	    }
	    
		ToolItem item = new ToolItem(toolBar, SWT.PUSH);
		final Command cmd = cmdService.getCommand(commandId);
		try {
			item.setText(cmd.getName());
			item.setToolTipText((tooltip != null) ? tooltip : cmd.getDescription());
			if(disabledIcon != null) {
				item.setDisabledImage(disabledIcon);
			}
			
			item.setEnabled(cmd.isEnabled());
		}
		catch (NotDefinedException e1) {
			log.error(e1, "Fehler! ");
		}
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ParameterizedCommand pCmd = new ParameterizedCommand(cmd, null);
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
