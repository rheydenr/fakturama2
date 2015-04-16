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

import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.gui.CellEditDialog;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.swtDefaults().numColumns(1).applyTo(composite);
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(composite);

        // Add a label that contains the image
        Label label = new Label(composite, SWT.NONE);
        label.setImage((Image) cellEditor.getEditorValue());
//        GridDataFactory.fillDefaults().grab(true, false).align(SWT.CENTER, SWT.TOP).applyTo(label);
        return composite;
    }

    @Override
    protected Point getInitialSize() {
        // Scale the dialog to the picture
        return new Point(width + 50, height + 100);
    }
}
