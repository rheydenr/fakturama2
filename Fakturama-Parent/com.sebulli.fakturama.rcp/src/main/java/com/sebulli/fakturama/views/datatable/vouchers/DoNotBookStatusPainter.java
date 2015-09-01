/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable.vouchers;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.swt.graphics.Image;

import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 *
 */
public class DoNotBookStatusPainter extends ImagePainter {
    
    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) cell.getDataValue(), Boolean.FALSE) ? Icon.COMMAND_REDPOINT.getImage(IconSize.DefaultIconSize) : null;
    }

}
