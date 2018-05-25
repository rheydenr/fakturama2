package com.sebulli.fakturama.views.datatable.vouchers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;

import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.views.datatable.MoneyDisplayConverter;

class VoucherTableConfiguration extends AbstractRegistryConfiguration {
    
	@Inject
	private ILocaleService localeUtil;

    static final String DONOTBOOK_LABEL = "Do_Not_Book_Label";

	@Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        Style styleLeftAligned = new Style();
        styleLeftAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
        Style styleRightAligned = new Style();
        styleRightAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
        Style styleCentered = new Style();
        styleCentered.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);

        // default style for the most of the cells
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
                                               styleLeftAligned,                // value of the attribute
                                               DisplayMode.NORMAL,              // apply during normal rendering i.e not during selection or edit
                                               GridRegion.BODY.toString());     // apply the above for all cells with this label
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER, 
                new DoNotBookStatusPainter(),
                DisplayMode.NORMAL, VoucherTableConfiguration.DONOTBOOK_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                styleCentered,      
                DisplayMode.NORMAL,             
                VoucherTableConfiguration.DONOTBOOK_LABEL); 

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                styleRightAligned,      
                DisplayMode.NORMAL,             
                ExpenditureVoucherListTable.DATE_CELL_LABEL ); 
        SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, localeUtil.getDefaultLocale());
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new DefaultDateDisplayConverter(dateFormat.toPattern()),
                DisplayMode.NORMAL,
                ExpenditureVoucherListTable.DATE_CELL_LABEL);

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                styleRightAligned,      
                DisplayMode.NORMAL,             
                ExpenditureVoucherListTable.MONEYVALUE_CELL_LABEL ); 
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new MoneyDisplayConverter(),
                DisplayMode.NORMAL,
                ExpenditureVoucherListTable.MONEYVALUE_CELL_LABEL);
        }
}