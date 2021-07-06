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

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.sebulli.fakturama.views.datatable.common.CellImagePainter;

/**
 * This class represents an Cell editor that does nothing than
 * opening the product preview dialog
 * 
 * @author Gerd Bartelt
 */
public class PictureViewEditor extends AbstractCellEditor {
    
    // The scaled image with width and height (used to resize the dialog)
    private Image scaledImage = null;
    private int width = 300;
    private int height = 200;
	
    private Control parent;

    protected Map<String, Object> editDialogSettings;

	public PictureViewEditor() {}
	
    @Override
    public Object getEditorValue() {
        return scaledImage;
    }

    @Override
    public void setEditorValue(Object value) {
        // nothing to do
    }

    @Override
    public Control getEditorControl() {
        return this.parent;
    }

    
    /**
     * Opens a preview dialog with the picture
     * 
     * @param 
     *      pictureName Name of the picture
     */
    @Override
    public Control createEditorControl(Composite composite) {
        // Add a label that contains the image
        Label label = new Label(composite, SWT.NONE);
        if(getEditorValue() != null) {
            label.setImage((Image) getEditorValue());
        }
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.CENTER, SWT.TOP).applyTo(label);
        this.parent = composite;
       return this.parent;
    }

  @Override
  protected Control activateCell(Composite parent, Object originalCanonicalValue) {
//    // Exit, if no picture is set
//    if (pictureName.isEmpty())
//        return 0;
//    
//    // Exit, if no picture is set
//    if (scaledImage == null) 
//        return 0;
      // Display the picture, if it is set.
      if (originalCanonicalValue != null) {
//          this.pictureName = (String) originalCanonicalValue;
//
//          // Load the image, based on the picture name
//          Image image = JFaceResources.getImageRegistry().get(pictureName);

			ByteArrayInputStream imgStream = new ByteArrayInputStream((byte[]) originalCanonicalValue);
			Image image = new Image(Display.getCurrent(), imgStream);
    	  
    	  
          // Get the pictures size
          width = image.getBounds().width;
          height = image.getBounds().height;

          // Scale it to maximum 250px
          int maxWidth = CellImagePainter.MAX_IMAGE_PREVIEW_WIDTH;
          
          // Maximum picture width 
          if (width > maxWidth) {
              height = maxWidth * height / width;
              width = maxWidth;
          }

          // Rescale the picture to the maximum width
          scaledImage = new Image(parent.getDisplay(), image.getImageData().scaledTo(width, height));
      }
      this.parent = createEditorControl(parent);
      return this.parent;
  }

}
