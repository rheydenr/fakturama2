package com.sebulli.fakturama.views.datatable.impl;

import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

public class ColumnHeaderStyleConfiguration extends 
DefaultColumnHeaderStyleConfiguration {
    {
        this.font = GUIHelper.DEFAULT_FONT;
        this.bgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
        this.fgColor = GUIHelper.COLOR_WIDGET_FOREGROUND;
        this.hAlign = HorizontalAlignmentEnum.CENTER;
        this.vAlign = VerticalAlignmentEnum.MIDDLE;
        this.borderStyle = null;
        this.cellPainter = new BeveledBorderDecorator(new TextPainter());
    }
}