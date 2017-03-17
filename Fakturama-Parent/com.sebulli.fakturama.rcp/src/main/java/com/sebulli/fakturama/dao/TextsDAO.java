package com.sebulli.fakturama.dao;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.TextModule_;

@Creatable
public class TextsDAO extends AbstractDAO<TextModule> {

	protected Class<TextModule> getEntityClass() {
		return TextModule.class;
	}

	/**
	 * Gets the all visible properties of this VAT object.
	 * 
	 * @return String[] of visible VAT properties
	 */
	public String[] getVisibleProperties() {
		return new String[] { TextModule_.name.getName(), TextModule_.text.getName() };
	}
}
