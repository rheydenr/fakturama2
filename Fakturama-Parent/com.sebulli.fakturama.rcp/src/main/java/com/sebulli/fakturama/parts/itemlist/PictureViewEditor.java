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

import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.gui.ICellEditDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.dialogs.ProductPictureDialog;
import com.sebulli.fakturama.views.datatable.CellImagePainter;

/**
 * This class represents an Cell editor that does nothing than
 * opening the product preview dialog
 * 
 * @author Gerd Bartelt
 */
public class PictureViewEditor extends AbstractCellEditor implements ICellEditDialog {

    // The picture name
    private String pictureName ="";
    
    //The shell to display the dialog
    private Shell shell = null;
    
    // The scaled image with width and height (used to resize the dialog)
    private Image scaledImage = null;
    private int width = 300;
    private int height = 200;
	
    private Composite parent;

    protected Map<String, Object> editDialogSettings;

	public PictureViewEditor() {}
	
    @Override
    public Object getEditorValue() {
        return scaledImage;
    }

    @Override
    public void setEditorValue(Object value) {
        // TODO Auto-generated method stub
        
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
    public Control createEditorControl(Composite parent) {
        this.parent = parent;
        return parent;
    }
    
    @Override
    public int open() {
      // Exit, if no picture is set
      if (pictureName.isEmpty())
          return 0;
      
      // Exit, if no picture is set
      if (scaledImage == null) 
          return 0;

        // Create the picture
      ProductPictureDialog preview = new ProductPictureDialog(shell, pictureName, layerCell, this, configRegistry);
        
        // Open the dialog
      return preview.open();
    }

  @Override
  protected Control activateCell(Composite parent, Object originalCanonicalValue) {
      this.parent = parent;
      // Display the picture, if it is set.
      if (originalCanonicalValue != null) {
          this.pictureName = (String) originalCanonicalValue;

          // Load the image, based on the picture name
          Image image = JFaceResources.getImageRegistry().get(pictureName);

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
      return parent;
  }
    
    @Override
    public Object getCommittedValue() {
        return null;
    }

    @Override
    public EditTypeEnum getEditType() {
        return EditTypeEnum.SET;
    }

    @Override
    public Object calculateValue(Object currentValue, Object processValue) {
        return currentValue;
    }

    @Override
    public void setDialogSettings(Map<String, Object> editDialogSettings) {
        // nothing to do
    }

}
