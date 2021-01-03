package com.sebulli.fakturama.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.opcoach.e4.preferences.ScopedPreferenceStore;

public class FakturamaPreferenceStore extends ScopedPreferenceStore {

    public FakturamaPreferenceStore(IScopeContext context, String qualifier) {
        super(context, qualifier);
    }
    
    public FakturamaPreferenceStore(IScopeContext context, String qualifier,
            String defaultQualifierPath) {
        super(context, qualifier, defaultQualifierPath);
    }
    
    @Override
    public boolean getBoolean(String name) {
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
