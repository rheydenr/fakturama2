/**
 * 
 */
package com.sebulli.fakturama.views.datatable;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.swt.graphics.Image;

import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * ImagePainter for the default checkmark (used in views tables).
 */
public class DefaultCheckmarkPainter extends ImagePainter {
	
	@Override
	protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		return BooleanUtils.toBooleanDefaultIfNull((Boolean) cell.getDataValue(), Boolean.FALSE) ? Icon.COMMAND_CHECKED.getImage(IconSize.DefaultIconSize) : null;
	}
}
