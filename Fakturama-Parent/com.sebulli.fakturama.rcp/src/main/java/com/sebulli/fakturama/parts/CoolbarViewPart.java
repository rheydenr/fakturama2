package com.sebulli.fakturama.parts;

import static com.sebulli.fakturama.Translate._;

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

import com.sebulli.fakturama.handlers.ICommandIds;
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
	private IEclipsePreferences preferences;
	
	@Inject
	private Logger log;


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
		ParameterizedCommand command = cmdService.createCommand("com.sebulli.fakturama.firstStart.command", null);
		handlerService.executeHandler(command );  // launch ConfigurationManager.checkFirstStart
		
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
		createToolItem(coolbar1, toolBar1, ICommandIds.CMD_WEBSHOP_IMPORT, 
				Icon.ICON_SHOP.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar1, toolBar1, IWorkbenchCommandConstants.FILE_PRINT, 
				Icon.ICON_PRINTOO.getImage(IconSize.ToobarIconSize), Icon.ICON_PRINTOO_DIS.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar1, toolBar1, IWorkbenchCommandConstants.FILE_SAVE, 
				Icon.ICON_SAVE.getImage(IconSize.ToobarIconSize), Icon.ICON_SAVE_DIS.getImage(IconSize.ToobarIconSize));
		finishToolbar(coolbar1, toolBar1);
			
		CoolBar coolbar2 = coolbarmgr.createControl(parent);
		ToolBar toolBar2 = new ToolBar(coolbar2, SWT.FLAT);
		String tooltipPrefix = _("command.new.tooltip") + " ";
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_LETTER, 
				tooltipPrefix + _("main.menu.new.letter"), Icon.ICON_LETTER_NEW.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_OFFER, 
				tooltipPrefix + _("main.menu.new.offer"), Icon.ICON_OFFER_NEW.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_ORDER, 
				tooltipPrefix + _("main.menu.new.order"), Icon.ICON_ORDER_NEW.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_CONFIRMATION, 
				tooltipPrefix + _("main.menu.new.confirmation"), Icon.ICON_CONFIRMATION_NEW.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_INVOICE, 
				tooltipPrefix + _("main.menu.new.invoice"), Icon.ICON_INVOICE_NEW.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_DELIVERY, 
				tooltipPrefix + _("main.menu.new.deliverynote"), Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_CREDIT, 
				tooltipPrefix + _("main.menu.new.credit"), Icon.ICON_CREDIT_NEW.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_DUNNING, 
				tooltipPrefix + _("main.menu.new.dunning"), Icon.ICON_DUNNING_NEW.getImage(IconSize.ToobarIconSize));
		createToolItem(coolbar2, toolBar2, ICommandIds.CMD_NEW_PROFORMA, 
				tooltipPrefix + _("main.menu.new.proforma"), Icon.ICON_LETTER_NEW.getImage(IconSize.ToobarIconSize));
		finishToolbar(coolbar2, toolBar2);
		
		CoolBar coolbar3 = coolbarmgr.createControl(parent);
		ToolBar toolBar3 = new ToolBar(coolbar3, SWT.FLAT);
		createToolItem(coolbar3, toolBar3, ICommandIds.CMD_NEW_PRODUCT, Icon.ICON_PRODUCT_NEW.getImage(IconSize.ToobarIconSize));	
		createToolItem(coolbar3, toolBar3, ICommandIds.CMD_NEW_CONTACT, Icon.ICON_CONTACT_NEW.getImage(IconSize.ToobarIconSize));	
		createToolItem(coolbar3, toolBar3, ICommandIds.CMD_NEW_EXPENDITUREVOUCHER, Icon.ICON_EXPENDITURE_NEW.getImage(IconSize.ToobarIconSize));	
		createToolItem(coolbar3, toolBar3, ICommandIds.CMD_NEW_RECEIPTVOUCHER, Icon.ICON_RECEIPT_VOUCHER_NEW.getImage(IconSize.ToobarIconSize));	
		finishToolbar(coolbar3, toolBar3);
		
		CoolBar coolbar4 = coolbarmgr.createControl(parent);
		ToolBar toolBar4 = new ToolBar(coolbar4, SWT.FLAT);
		createToolItem(coolbar4, toolBar4, ICommandIds.CMD_OPEN_PARCEL_SERVICE, Icon.ICON_PARCEL_SERVICE.getImage(IconSize.ToobarIconSize));	
		createToolItem(coolbar4, toolBar4, ICommandIds.CMD_OPEN_BROWSER_EDITOR, Icon.ICON_WWW.getImage(IconSize.ToobarIconSize));	
		createToolItem(coolbar4, toolBar4, ICommandIds.CMD_OPEN_CALCULATOR, Icon.ICON_CALCULATOR.getImage(IconSize.ToobarIconSize));	
		finishToolbar(coolbar4, toolBar4);
		
/*
 * TODO
 *     <trimBars xmi:id="_RmPTtetOEeKo5Ms1QUgPTw" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp">
      <children xsi:type="menu:ToolBar" xmi:id="_RmPTtutOEeKo5Ms1QUgPTw" elementId="toolbar:com.sebulli.fakturama.toolbar.main1" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp">
        <tags>Draggable</tags>
        <children xsi:type="menu:HandledToolItem" xmi:id="_RmPTt-tOEeKo5Ms1QUgPTw" elementId="toolbar:main.toolbar.webShopImport.id" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp" accessibilityPhrase="" label="%icon.webshop.name" iconURI="icon://ICON_SHOP" tooltip="%icon.webshop.tooltip" command="_QWE00ENJEeOd4ZSH8zQNZQ">
          <visibleWhen xsi:type="ui:CoreExpression" xmi:id="_RmPTuOtOEeKo5Ms1QUgPTw" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp"/>
        </children>
        <children xsi:type="menu:HandledToolItem" xmi:id="_RmPTuetOEeKo5Ms1QUgPTw" elementId="toolbar:main.toolbar.printoo.id" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp" label="%main.menu.file.print" iconURI="icon://ICON_PRINTOO" enabled="false" command="_RmPT0etOEeKo5Ms1QUgPTw"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_RmPTuutOEeKo5Ms1QUgPTw" elementId="toolbar:main.toolbar.save.id" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp" label="%main.menu.file.save" iconURI="icon://ICON_SAVE" enabled="false" command="_RmPT0etOEeKo5Ms1QUgPTw"/>
      </children>
      <children xsi:type="menu:ToolBar" xmi:id="_RmPTu-tOEeKo5Ms1QUgPTw" elementId="toolbar:com.sebulli.fakturama.toolbar.main2" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp">
        <tags>Draggable</tags>
        <children xsi:type="menu:HandledToolItem" xmi:id="_zsAQMEmiEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.letter.id" label="%icon.new.letter.name" iconURI="icon://ICON_LETTER_NEW" tooltip="%main.menu.new.letter"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_RmPTvOtOEeKo5Ms1QUgPTw" elementId="toolbar:main.toolbar.new.offer.id" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp" label="%icon.new.offer.name" iconURI="icon://ICON_OFFER_NEW" tooltip="%main.menu.new.offer" enabled="false" command="_RmPT0-tOEeKo5Ms1QUgPTw"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_pUJrkEmiEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.order.id" label="%icon.new.order.name" iconURI="icon://ICON_ORDER_NEW" tooltip="%main.menu.new.order"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_sXjVsEmjEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.confirmation.id" label="%icon.new.confirmation.name" iconURI="icon://ICON_CONFIRMATION_NEW" tooltip="%main.menu.new.confirmation"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_8jxssEmjEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.invoice.id" label="%icon.new.invoice.name" iconURI="icon://ICON_INVOICE_NEW" tooltip="%main.menu.new.invoice"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_QmqzQEmkEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.delivery.id" label="%icon.new.delivery.name" iconURI="icon://ICON_DELIVERY_NEW" tooltip="%main.menu.new.delivery"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_gSnuwEmkEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.credit.id" label="%icon.new.credit.name" iconURI="icon://ICON_CREDIT_NEW" tooltip="%main.menu.new.credit"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_zw9WwEmkEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.dunning.id" label="%icon.new.dunning.name" iconURI="icon://ICON_DUNNING_NEW" tooltip="%main.menu.new.dunning"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_EfIkQEmlEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.proforma.id" label="%icon.new.proforma.name" iconURI="icon://ICON_PROFORMA_NEW" tooltip="%main.menu.new.proforma"/>
      </children>
      <children xsi:type="menu:ToolBar" xmi:id="_RmPTvetOEeKo5Ms1QUgPTw" elementId="toolbar:com.sebulli.fakturama.toolbar.main3" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp">
        <tags>Draggable</tags>
        <children xsi:type="menu:HandledToolItem" xmi:id="_N9mCwEmlEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.product.id" label="%icon.new.product.name" iconURI="icon://ICON_PRODUCT_NEW" tooltip="%icon.new.product.tooltip"/>
        <children xsi:type="menu:HandledToolItem" xmi:id="_9o-vUEmlEeOgQverL-ldNw" elementId="toolbar:main.toolbar.new.contact.id" label="%icon.new.contact.name" iconURI="icon://ICON_CONTACT_NEW" tooltip="%icon.new.contact.tooltip"/>
      </children>
      <children xsi:type="menu:ToolBar" xmi:id="_RmPTvutOEeKo5Ms1QUgPTw" elementId="toolbar:com.sebulli.fakturama.toolbar.main4" contributorURI="platform:/plugin/com.sebulli.fakturama.rcp">
        <tags>Draggable</tags>
      </children>
    </trimBars>

 */

		
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
			final Image iconImage) {
		createToolItem(coolbar, toolBar, commandId, null, iconImage, null);
	}
	
	private void createToolItem(final CoolBar coolbar, final ToolBar toolBar, final String commandId, 
			final Image iconImage, final Image disabledIcon) {
		createToolItem(coolbar, toolBar, commandId, null, iconImage, disabledIcon);
	}

	private void createToolItem(final CoolBar coolbar, final ToolBar toolBar, final String commandId, 
			final String tooltip, final Image iconImage) {
		createToolItem(coolbar, toolBar, commandId, tooltip, iconImage, null);
	}
	
	private void createToolItem(final CoolBar coolbar, final ToolBar toolBar, final String commandId, 
			final String tooltip, final Image iconImage, final Image disabledIcon) {
		ToolItem item = new ToolItem(toolBar, SWT.PUSH);
		final Command cmd = cmdService.getCommand(commandId);
		try {
			item.setText(cmd.getName());
			if(tooltip != null) {
				item.setToolTipText(tooltip);
			} else {
				item.setToolTipText(cmd.getDescription());
			}
			if(disabledIcon != null) {
				item.setDisabledImage(disabledIcon);
			}
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
