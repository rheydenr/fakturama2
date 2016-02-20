package com.sebulli.fakturama.preferences;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

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
	
	private static PreferenceStore instance; 
	
	public FakturamaPreferenceStoreProvider() {
		instance = new PreferenceStore("myFile.prop");
		try {
			instance.load();
//		instance = new ScopedPreferenceStore(InstanceScope.INSTANCE,
//				String.format("/%s/%s", InstanceScope.SCOPE, Activator.getContext().getBundle().getSymbolicName()),
//				Constants.DEFAULT_PREFERENCES_NODE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IPersistentPreferenceStore getPreferenceStore() {
		// System.out.println("Use my preference store for this plugin");
		return instance;
	}
	
	public void closeStore() {
		try {
			instance.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		instance = null;
	}

}
