package com.sebulli.fakturama.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;

import com.opcoach.e4.preferences.IPreferenceStoreProvider;
import com.opcoach.e4.preferences.ScopedPreferenceStore;
import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.misc.Constants;

/**
 * The {@link FakturamaPreferenceStoreProvider} creates an {@link IPreferenceStoreProvider}
 * which is essential for handling preferences. With this store you can access the preferences
 * which are handled by {@link ScopedPreferenceStore}.
 *
 */
public class FakturamaPreferenceStoreProvider implements IPreferenceStoreProvider
{

	public FakturamaPreferenceStoreProvider()
	{
	}

	@Override
	public IPreferenceStore getPreferenceStore()
	{
//		System.out.println("Use my preference Store for this plugin");
        return new ScopedPreferenceStore(InstanceScope.INSTANCE, String.format("/%s/%s", InstanceScope.SCOPE, Activator
                .getContext().getBundle().getSymbolicName()),
                Constants.DEFAULT_PREFERENCES_NODE);
	}

}
