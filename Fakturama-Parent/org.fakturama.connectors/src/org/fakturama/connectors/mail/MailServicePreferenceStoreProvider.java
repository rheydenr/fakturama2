package org.fakturama.connectors.mail;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.osgi.framework.FrameworkUtil;

import com.opcoach.e4.preferences.IPreferenceStoreProvider;
import com.opcoach.e4.preferences.ScopedPreferenceStore;

public class MailServicePreferenceStoreProvider implements IPreferenceStoreProvider {
    
    private static IPersistentPreferenceStore preferenceStore;
    
    private static IPreferenceStoreProvider preferenceStoreProvider = new MailServicePreferenceStoreProvider(); 
    
    public static IPreferenceStoreProvider getInstance() {
        return preferenceStoreProvider;
    }

    @Override
    public IPersistentPreferenceStore getPreferenceStore() {
        if(preferenceStore == null) {
            preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
                    FrameworkUtil.getBundle(getClass()).getSymbolicName());
        }
        return preferenceStore;
    }
    
    public void closeStore() {
        try {
            preferenceStore.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        preferenceStore = null;
    }

}
