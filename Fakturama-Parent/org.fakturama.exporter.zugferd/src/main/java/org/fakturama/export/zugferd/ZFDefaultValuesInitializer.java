/**
 * 
 */
package org.fakturama.export.zugferd;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.sebulli.fakturama.preferences.FakturamaPreferenceStoreProvider;
import com.sebulli.fakturama.preferences.IInitializablePreference;

/**
 * @author Ralf
 *
 */
public class ZFDefaultValuesInitializer extends AbstractPreferenceInitializer {

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore defaultValuesNode = FakturamaPreferenceStoreProvider.getInstance().getPreferenceStore();       
        
        final Bundle bundle = FrameworkUtil.getBundle(com.sebulli.fakturama.preferences.FakturamaPreferenceStoreProvider.class);
        IInitializablePreference p = ContextInjectionFactory.make(ZugferdPreferences.class, EclipseContextFactory.getServiceContext(bundle.getBundleContext()));
        p.setInitValues(defaultValuesNode);
        
        EclipseContextFactory.getServiceContext(bundle.getBundleContext()).set(IPreferenceStore.class, defaultValuesNode);
        
    }

}
