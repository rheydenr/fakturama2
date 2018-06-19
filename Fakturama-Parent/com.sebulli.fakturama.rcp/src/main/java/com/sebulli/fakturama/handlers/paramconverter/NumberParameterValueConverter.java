package com.sebulli.fakturama.handlers.paramconverter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

public class NumberParameterValueConverter extends AbstractParameterValueConverter {

	@Override
	public Object convertToObject(String parameterValue) throws ParameterValueConversionException {
		if(StringUtils.isNumeric(parameterValue)) {
			return Integer.parseInt(parameterValue);
		} else {
			return parameterValue;
		}
	}

	@Override
	public String convertToString(Object parameterValue) throws ParameterValueConversionException {
		String retval = "";
		if(parameterValue instanceof Number) {
			switch (parameterValue.getClass().getName()) {
			case "java.lang.Integer":
				retval = ((Integer) parameterValue).toString();
				break;
			case "java.lang.Long": 
				retval = ((Long)parameterValue).toString();
				break;
			default:
				retval = parameterValue.toString();
				break;
			}
		}
		return retval;
	}
	
}