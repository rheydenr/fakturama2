/**
 * 
 */
package com.sebulli.fakturama.views.datatable;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.swt.graphics.Image;

import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * {@link ImagePainter} for all sorts of Images in a NatTable.
 */
public class CellImagePainter extends ImagePainter {
    
    public static final int MAX_IMAGE_PREVIEW_WIDTH = 250;

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        Image retval = null;
        Object dataValue = cell.getDataValue();
        if (dataValue instanceof Icon) {
            Icon icon = (Icon) dataValue;
            if (icon != null) {
                retval = icon.getImage(IconSize.DefaultIconSize);
            }
        } else if (dataValue instanceof Image) {
            retval = (Image)dataValue;
        }
        return retval;
    }
}
