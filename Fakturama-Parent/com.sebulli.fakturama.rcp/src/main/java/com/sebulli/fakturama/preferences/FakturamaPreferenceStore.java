package com.sebulli.fakturama.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;

import com.opcoach.e4.preferences.ScopedPreferenceStore;

public class FakturamaPreferenceStore extends ScopedPreferenceStore {

    private ISecurePreferences secPrefs;

    public FakturamaPreferenceStore(IScopeContext context, String qualifier) {
        super(context, qualifier);
        secPrefs = SecurePreferencesFactory.getDefault();       
        secPrefs.node(qualifier);
    }
    
    public FakturamaPreferenceStore(IScopeContext context, String qualifier,
            String defaultQualifierPath) {
        super(context, qualifier, defaultQualifierPath);
        secPrefs = SecurePreferencesFactory.getDefault();   
        
        // use it:
        secPrefs.node(qualifier);
        secPrefs.node(defaultQualifierPath);
    }
   
//    
//    @Override
//    public void setDefault(String name, boolean value) {
//        try {
//            secPrefs.putBoolean("DEFAULT_" + name, value, false);
//        } catch (StorageException e) {
//            e.printStackTrace();
//        }
//    }
//    
//    @Override
//    public boolean getDefaultBoolean(String name) {
//        throw new UnsupportedOperationException("Default values can pnly be get via normal getters.");
//    }
//    
//    @Override
//    public void setValue(String name, boolean value) {
//        boolean oldValue = getBoolean(name);
//        if (oldValue == value) {
//            return;
//        }
//        try {
//            silentRunning = true;// Turn off updates from the store
//            if (getDefaultBoolean(name) == value) {
//                getStorePreferences().remove(name);
//            } else {
//                getStorePreferences().putBoolean(name, value);
//            }
//            dirty = true;
//            firePropertyChangeEvent(name, oldValue ? Boolean.TRUE
//                    : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
//        } finally {
//            silentRunning = false;// Restart listening to preferences
//        }
//    }
//
    
    @Override
    public boolean getBoolean(String name) {
//        boolean defaultValue = BOOLEAN_DEFAULT_DEFAULT;
//        try {
//            defaultValue = secPrefs.getBoolean("DEFAULT_" + name, BOOLEAN_DEFAULT_DEFAULT);
//             return secPrefs.getBoolean(name, defaultValue);
//       } catch (StorageException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return defaultValue;
//        
        // old mechanics:
        String value = internalGet(name);
        return value == null ? getDefaultBoolean(name) : Boolean.valueOf(value)
                .booleanValue();
    }
    
    @Override
    public double getDouble(String name) {
        String value = internalGet(name);
        if (value == null) {
            return getDefaultDouble(name);
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return getDefaultDouble(name);
        }
    }
    @Override
    public float getFloat(String name) {
        String value = internalGet(name);
        if (value == null) {
            return getDefaultFloat(name);
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return getDefaultFloat(name);
        }
    }

    @Override
    public int getInt(String name) {
        String value = internalGet(name);
        if (value == null) {
            return getDefaultInt(name);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return getDefaultInt(name);
        }
    }

    @Override
    public long getLong(String name) {
        String value = internalGet(name);
        if (value == null) {
            return getDefaultLong(name);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return getDefaultLong(name);
        }
    }

    @Override
    public String getString(String name) {
        String value = internalGet(name);
        return value == null ? getDefaultString(name) : value;
    }

    // copy from super class
    private String internalGet(String key) {
        return Platform.getPreferencesService().get(key, null,
                getPreferenceNodes(true));
    }
}
