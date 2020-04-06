/**
 * 
 */
package com.sebulli.fakturama.handlers.paramconverter;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

/**
 *
 */
public class BooleanParameterValueConverter extends AbstractParameterValueConverter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractParameterValueConverter#convertToObject(
	 * java.lang.String)
	 */
	@Override
	public Object convertToObject(String parameterValue) throws ParameterValueConversionException {
		return BooleanUtils.toBoolean(parameterValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractParameterValueConverter#convertToString(
	 * java.lang.Object)
	 */
	@Override
	public String convertToString(Object parameterValue) throws ParameterValueConversionException {
		String retval = "";
		if (parameterValue instanceof Boolean) {
			retval = BooleanUtils.toStringTrueFalse((Boolean) parameterValue);
		} else if(parameterValue instanceof String) {
			// try to convert a String into boolean
			retval = (String) parameterValue;
		}
		return retval;
	}

}
