package com.sebulli.fakturama.preferences;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.LifecycleManager;

public interface IInitializablePreference {

	/**
	 * Set the default values for this preference page
	 * 
	 * @param node
	 *            The preference node
	 */
	public void setInitValues(IPreferenceStore node);

	/**
	 * Loads or save preferences in or to database.
	 * 
     * If the "preferencesInDatabase" field is <code>null</code> then this method is launched from {@link DefaultValuesInitializer}.
     * But at this point the <code>preferencesInDatabase</code> field isn't initialized (from {@link LifecycleManager}),
     * so that it could produce an ugly NPE. Therefore we test the value and only if it's present we initialize
     * some properties from DB.
     * The "loadOrSavePreferencesFromOrInDatabase" property is set in class {@link PreferencesInDatabase} (which
     * in turn is called from {@link LifecycleManager}).
     * 
     *  @param context the {@link IEclipseContext}
     */
    public void loadOrSaveUserValuesFromDB(IEclipseContext context);

}