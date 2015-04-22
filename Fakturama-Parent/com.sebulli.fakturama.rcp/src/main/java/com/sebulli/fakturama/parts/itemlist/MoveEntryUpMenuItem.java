/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.parts.itemlist;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;

/**
 * This action moves the selected entry up
 * 
 */
public class MoveEntryUpMenuItem implements IMenuItemProvider {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected IEventBroker evtBroker;

    private EntityGridListLayer<? extends IEntity> gridListLayer;
    
    public MoveEntryUpMenuItem() {
        // default constructor is only for ContextInjectionFactory
    }

    public MoveEntryUpMenuItem(EntityGridListLayer<? extends IEntity> gridListLayer) {
        this.gridListLayer = gridListLayer;
    }
    
//    @CanExecute
    public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION)
                            @Optional Object selection) {
       if (selection!=null/* && selection instanceof MyObject*/)
        return true;
       return false;
    }
    
    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SELECTION)
    @Optional Object selection) {
        if(selection != null) {
            NatEventData natEventData = MenuItemProviders.getNatEventData((SelectionEvent) selection);
            // Get the position of the selected element
            NatTable natTable = natEventData.getNatTable();
            int pos = natEventData.getRowPosition() - 1;  // count without header row
            // Do not move one single item
            if (natTable.getRowCount() > 2 && pos > 0) {  // the header row has to be added for this calculation!
                ILayerCommand cmd = new RowReorderCommand(gridListLayer.getBodyLayerStack().getRowReorderLayer(), pos, pos - 1);
                natTable.doCommand(cmd);natTable.refresh();
            }
        }
    }
    
    @Override
    public void addMenuItem(NatTable natTable, Menu popupMenu) {
        MenuItem moveRowUp = new MenuItem(popupMenu, SWT.PUSH);
        moveRowUp.setText(msg.commandDocumentsMoveUpName);
        //  //T: Tool Tip Text
        //  setToolTipText(_("Move up the selected entry"));
          // The id is used to refer to the action in a menu or tool bar
        //            moveRowUp.setID(???);
        moveRowUp.setImage(Icon.COMMAND_UP.getImage(IconSize.DefaultIconSize));
        moveRowUp.setEnabled(true);
        moveRowUp.setAccelerator(SWT.ALT + SWT.ARROW_UP); // doesn't work at the moment 

        moveRowUp.addSelectionListener(new SelectionAdapter() {

            /**
             * Move an item up or down
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                NatEventData natEventData = MenuItemProviders.getNatEventData(e);
                // Get the position of the selected element
                NatTable natTable = natEventData.getNatTable();
                int pos = natEventData.getRowPosition();  // count without header row
                // Do not move one single item
                if (natTable.getRowCount() > 2 && pos > 0) {  // the header row has to be added for this calculation!
                    ILayerCommand cmd = new RowReorderCommand(natTable, pos, pos - 1);
                    natTable.doCommand(cmd);//natTable.refresh();
                }

                // old code:
                // documentEditor.moveItem(uds, true);
            }
        });
    }

    /**
     * @param gridListLayer
     *            the gridListLayer to set
     */
    public void setGridListLayer(EntityGridListLayer<? extends IEntity> gridListLayer) {
        this.gridListLayer = gridListLayer;
    }

}
