/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Ralf Heydenreich - initial API and implementation
 */
package org.fakturama.export.einvoice;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;

import com.opcoach.e4.preferences.IPreferenceStoreProvider;
import com.opcoach.e4.preferences.ScopedPreferenceStore;

/**
 * The {@link ZFPreferenceStoreProvider} creates an
 * {@link IPreferenceStoreProvider} which is essential for handling preferences.
 * With this store you can access the preferences which are handled by
 * {@link ScopedPreferenceStore}.
 *
 */
public class ZFPreferenceStoreProvider implements IPreferenceStoreProvider {
	
	private static IPersistentPreferenceStore preferenceStore;
	
	private static IPreferenceStoreProvider preferenceStoreProvider = new ZFPreferenceStoreProvider(); 
	
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
