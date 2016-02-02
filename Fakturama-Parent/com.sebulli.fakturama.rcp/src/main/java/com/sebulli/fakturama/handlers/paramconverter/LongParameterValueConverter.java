package com.sebulli.fakturama.handlers.paramconverter;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

public class LongParameterValueConverter extends AbstractParameterValueConverter {

	@Override
	public Object convertToObject(String parameterValue) throws ParameterValueConversionException {
		return Long.parseLong(parameterValue);
	}

	@Override
	public String convertToString(Object parameterValue) throws ParameterValueConversionException {
		return (String)parameterValue;
	}
	
}