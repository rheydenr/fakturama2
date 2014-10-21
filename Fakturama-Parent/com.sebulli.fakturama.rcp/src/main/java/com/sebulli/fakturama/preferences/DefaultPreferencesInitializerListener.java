package com.sebulli.fakturama.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

public interface DefaultPreferencesInitializerListener {

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public abstract void setInitValues(IPreferenceStore node);

}