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

package com.sebulli.fakturama.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.gui.CellEditDialog;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This class represents the product picture preview dialog
 * 
 */
public class ProductPictureDialog extends CellEditDialog {
    private int width = 300;
    private int height = 200;

    /**
     * Constructor
     * 
     * @param shell
     *            for the dialog
     * @param pictureName
     *            The name of the product picture to show
     */
    public ProductPictureDialog(Shell parentShell, final Object originalCanonicalValue, final ILayerCell cell, final ICellEditor cellEditor,
            final IConfigRegistry configRegistry) {
        super(parentShell, originalCanonicalValue, cell, cellEditor, configRegistry);
    }

    /**
     * Open a dialog with the picture
     * 
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        
        Composite panel = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        GridLayout panelLayout = new GridLayout(1, true);
        panelLayout.marginWidth = 8;
        panel.setLayout(panelLayout);

        //add a custom message if there is one configured in the edit dialog settings
        if (this.editDialogSettings != null && this.editDialogSettings.containsKey(DIALOG_MESSAGE)) {
            String customMessage = this.editDialogSettings.get(DIALOG_MESSAGE).toString();
            Label customMessageLabel = new Label(panel, SWT.NONE);
            customMessageLabel.setText(customMessage);
            GridDataFactory.fillDefaults().grab(true, false).hint(100, 30).applyTo(customMessageLabel);
        }
        
        //activate the new editor
        this.cellEditor.activateCell(
                panel, 
                this.originalCanonicalValue, 
                EditModeEnum.DIALOG, 
                this.cellEditHandler, 
                this.cell, 
                this.configRegistry);
        
        Control editorControl = this.cellEditor.getEditorControl();
        
        // propagate the ESC event from the editor to the dialog
        editorControl.addKeyListener(getEscKeyListener());

        //if the editor control already has no layout data set already, apply the default one
        //this check allows to specify a custom layout data while creating the editor control
        //in the ICellEditor
        if (editorControl.getLayoutData() == null) {
            GridDataFactory.fillDefaults().grab(true, false).hint(100, 30).applyTo(editorControl);
        }

        return panel;
    }
    
    /**
     * Only OK button is allowed.
     * 
     * @see Dialog
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
//        createButton(parent, IDialogConstants.CANCEL_ID,
//                IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Point getInitialSize() {
        Point retval = null;
        // copied from CellEditDialog
        if (this.editDialogSettings != null) {
            Object settingsSize = this.editDialogSettings.get(DIALOG_SHELL_SIZE);
            if (settingsSize != null && settingsSize instanceof Point) {
                retval = (Point) settingsSize;
            }
        } else {
            // Scale the dialog to the picture
            // if no other setting is given
            retval = new Point(width + 50, height + 120);
        }
        if (retval == null) {
            retval = super.getInitialSize();
        }
        return retval;
    }
}
