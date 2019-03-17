/**
 * 
 */
package org.javamoney.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.money.spi.ServiceProvider;

import org.osgi.framework.ServiceReference;

/**
 *
 */
public class OsgiServiceProvider implements ServiceProvider {
    /** List of services loaded, per class. */
    @SuppressWarnings("rawtypes")
	private  final ConcurrentHashMap<Class, List<Object>> servicesLoaded = new ConcurrentHashMap<>();

    @Override
    public int getPriority() {
        return 1;   // OSGi service has a higher priority than "normal" JavaMoney ServiceProvider
    }

    /**
     * Loads and registers services.
     *
     * @param serviceType
     *            The service type.
     * @param <T>
     *            the concrete type.
     * @param defaultList
     *            the list of items returned, if no services were found.
     * @return the items found, never {@code null}.
     */
    @Override
    public <T> List<T> getServices(final Class<T> serviceType) {
        @SuppressWarnings("unchecked")
        List<T> found = (List<T>) servicesLoaded.get(serviceType);
        if (found != null) {
            return found;
        }

        return loadServices(serviceType);
    }

    /**
     * Loads and registers services.
     *
     * @param   serviceType  The service type.
     * @param   <T>          the concrete type.
     * @param   defaultList  the list of items returned, if no services were found.
     *
     * @return  the items found, never {@code null}.
     */
    private <T> List<T> loadServices(final Class<T> serviceType) {
        List<T> services = new ArrayList<>();
        try {
            
            // read service from OSGi registry
            Collection<ServiceReference<T>> serviceReferences = Activator.getBundleContext().getServiceReferences(serviceType, null);
            
            services = serviceReferences.stream().map(sr -> Activator.getBundleContext().getService(sr)).collect(Collectors.toList());
            @SuppressWarnings("unchecked")
            final List<T> previousServices = (List<T>) servicesLoaded.putIfAbsent(serviceType, (List<Object>) services);
            return Collections.unmodifiableList(previousServices != null ? previousServices : services);
        } catch (Exception e) {
            Logger.getLogger(OsgiServiceProvider.class.getName()).log(Level.WARNING,
                                                                         "Error loading services of type " + serviceType, e);
            return services;
        }
    }
}
