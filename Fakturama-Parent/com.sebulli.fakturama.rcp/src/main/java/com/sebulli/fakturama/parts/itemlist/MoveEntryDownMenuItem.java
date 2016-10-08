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

package com.sebulli.fakturama.parts.itemlist;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
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
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;

/**
 * This action moves the selected entry down
 * 
 * @author Gerd Bartelt
 */
public class MoveEntryDownMenuItem implements IMenuItemProvider {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected IEventBroker evtBroker;
    
    @Inject
    protected ESelectionService selectionService;

    private EntityGridListLayer<? extends IEntity> gridListLayer;
    
    /**
     * 
     */
    public MoveEntryDownMenuItem() {
        // default constructor is only for ContextInjectionFactory
    }

    /**
     * @param gridListLayer
     */
    public MoveEntryDownMenuItem(EntityGridListLayer<? extends IEntity> gridListLayer) {
        this.gridListLayer = gridListLayer;
    }
    
    @Execute
    public void moveRowDown(@Active MPart activePart) {
        @SuppressWarnings("rawtypes")
		AbstractViewDataTable currentListtable = (AbstractViewDataTable) activePart.getObject();
        NatTable natTable = currentListtable.getNatTable();
    }

    @Override
    public void addMenuItem(NatTable natTable, Menu popupMenu) {
        final MenuItem moveRowDown = new MenuItem(popupMenu, SWT.PUSH);
        moveRowDown.setText(msg.commandDocumentsMoveDownName);
        //  //T: Tool Tip Text
        //  setToolTipText(_("Move down the selected entry"));
          // The id is used to refer to the action in a menu or tool bar
        //            moveRowUp.setID(???);
        moveRowDown.setImage(Icon.COMMAND_DOWN.getImage(IconSize.DefaultIconSize));
        moveRowDown.setEnabled(true);
        moveRowDown.setAccelerator(SWT.MOD1 | SWT.F7); // doesn't work at the moment 

        moveRowDown.addSelectionListener(new SelectionAdapter() {

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
                if (natTable.getRowCount() > 2 && pos < natTable.getRowCount()) {  // the header row has to be added for this calculation!
                	// yes, +2 is ok!
                	// see https://www.eclipse.org/forums/index.php/t/1081741/
                    ILayerCommand cmd = new RowReorderCommand(natTable, pos, pos + 2);
                    natTable.doCommand(cmd);
                } else {
                	moveRowDown.setEnabled(false);
                }

                // old code:
                // documentEditor.moveItem(uds, false);
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
