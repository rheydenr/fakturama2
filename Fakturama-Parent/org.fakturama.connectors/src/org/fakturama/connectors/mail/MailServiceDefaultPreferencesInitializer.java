/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package org.fakturama.connectors.mail;

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
public class MailServiceDefaultPreferencesInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore defaultValuesNode = MailServicePreferenceStoreProvider.getInstance().getPreferenceStore();       
        
        final Bundle bundle = FrameworkUtil.getBundle(MailServiceDefaultPreferencesInitializer.class);
        IInitializablePreference p = ContextInjectionFactory.make(MailServicePreferences.class, EclipseContextFactory.getServiceContext(bundle.getBundleContext()));
        p.setInitValues(defaultValuesNode);
        
        EclipseContextFactory.getServiceContext(bundle.getBundleContext()).set(IPreferenceStore.class, defaultValuesNode);
    }
}
