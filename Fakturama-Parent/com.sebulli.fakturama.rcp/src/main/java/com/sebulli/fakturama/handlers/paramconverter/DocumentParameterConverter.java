/**
 * 
 */
package com.sebulli.fakturama.handlers.paramconverter;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.IEntity;

/**
 * Converts a {@link Document} parameter (from ApplicationModel's command). This
 * is necessary for handling different parameter types.
 *
 */
public class DocumentParameterConverter extends AbstractParameterValueConverter {
	
	@Inject
	private DocumentsDAO documentsDao;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractParameterValueConverter#convertToObject(
	 * java.lang.String)
	 */
	@Override
	public Object convertToObject(String parameterValue) throws ParameterValueConversionException {
		if(StringUtils.isNumeric(parameterValue)) {
			return documentsDao.findById(Long.parseLong(parameterValue));
		}
		return null;
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
		// return only the ID of the given document
		if (parameterValue != null && parameterValue instanceof IEntity) {
			return Long.toString(((IEntity) parameterValue).getId());
		} else {
			return (String) parameterValue;
		}
	}

}
