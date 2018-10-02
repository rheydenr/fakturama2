/**
 * 
 */
package com.sebulli.fakturama.parts.converter;

import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.LocaleUtil;

/**
 * Converter for percentage values. Can also convert double values (in contrast to native NatTable converter).
 *
 */
public class DoublePercentageDisplayConverter extends DisplayConverter {
	
	private ILocaleService localeUtil;
	
	/**
	 * hidden constructor
	 */
	@SuppressWarnings("unused")
	private DoublePercentageDisplayConverter() {}
	
	/**
	 * @param localeUtil
	 */
	public DoublePercentageDisplayConverter(ILocaleService localeUtil) {
		this.localeUtil = localeUtil;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#canonicalToDisplayValue(java.lang.Object)
	 */
	@Override
	public Object canonicalToDisplayValue(Object canonicalValue) {
        if (canonicalValue != null) {
            double percentageValue = ((Number) canonicalValue).doubleValue();
            double displayInt = percentageValue * 100;
            return String.format("%.2f %%", displayInt);
        }
        return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#displayToCanonicalValue(java.lang.Object)
	 */
	@Override
	public Object displayToCanonicalValue(Object displayValue) {
        String displayString = (String) displayValue;
        if(StringUtils.isBlank(displayString) || displayString.equalsIgnoreCase("0")) {
        	return null;
        }
    	try {
    		// don't use NumberFormat.getPercentInstance()! The formatting of percentage values is evil!
    		// We can use "normal" numbers. 
    		return NumberFormat.getNumberInstance(localeUtil.getDefaultLocale()).parse(displayString).doubleValue()/100;
        } catch (ParseException e) {
            throw new NumberFormatException(e.getLocalizedMessage());
        }
	}
}

