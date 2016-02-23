package com.sebulli.fakturama.preferences;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

import com.opcoach.e4.preferences.IPreferenceStoreProvider;
import com.opcoach.e4.preferences.ScopedPreferenceStore;
import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.misc.Constants;

/**
 * The {@link FakturamaPreferenceStoreProvider} creates an
 * {@link IPreferenceStoreProvider} which is essential for handling preferences.
 * With this store you can access the preferences which are handled by
 * {@link ScopedPreferenceStore}.
 *
 */
public class FakturamaPreferenceStoreProvider implements IPreferenceStoreProvider {
	
	private static IPersistentPreferenceStore preferenceStore;
	
	private static IPreferenceStoreProvider preferenceStoreProvider = new FakturamaPreferenceStoreProvider(); 
	
	public static IPreferenceStoreProvider getInstance() {
		return preferenceStoreProvider;
	}

	@Override
	public IPersistentPreferenceStore getPreferenceStore() {
		if(preferenceStore == null) {
			preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
					Activator.getContext().getBundle().getSymbolicName());
		}
		return preferenceStore;
	}
	
	public void closeStore() {
		try {
			preferenceStore.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		preferenceStore = null;
	}

}
