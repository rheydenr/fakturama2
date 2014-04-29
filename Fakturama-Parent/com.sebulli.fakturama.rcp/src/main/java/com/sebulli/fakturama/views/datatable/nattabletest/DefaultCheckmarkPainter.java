/**
 * 
 */
package com.sebulli.fakturama.views.datatable.nattabletest;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.swt.graphics.Image;

import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * @author rheydenr
 *
 */
public class DefaultCheckmarkPainter extends ImagePainter {
	
	@Override
	protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		return (boolean) cell.getDataValue() ? Icon.COMMAND_CHECKED.getImage(IconSize.DefaultIconSize) : null;
	}
}
