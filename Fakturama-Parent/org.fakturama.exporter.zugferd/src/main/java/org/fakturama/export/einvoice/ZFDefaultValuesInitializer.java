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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.sebulli.fakturama.preferences.IInitializablePreference;

/**
 *
 */
public class ZFDefaultValuesInitializer extends AbstractPreferenceInitializer {

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore defaultValuesNode = ZFPreferenceStoreProvider.getInstance().getPreferenceStore();       
        
        final Bundle bundle = FrameworkUtil.getBundle(ZFDefaultValuesInitializer.class);
        IInitializablePreference p = ContextInjectionFactory.make(ZugferdPreferences.class, EclipseContextFactory.getServiceContext(bundle.getBundleContext()));
        p.setInitValues(defaultValuesNode);
        
        EclipseContextFactory.getServiceContext(bundle.getBundleContext()).set(IPreferenceStore.class, defaultValuesNode);
    }
}
