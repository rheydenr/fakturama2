package com.sebulli.fakturama.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

public interface IPreferencesInitializerListener {

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node);

}