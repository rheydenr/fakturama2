package com.sebulli.fakturama.parts.widget.contentprovider;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * {@link IStructuredContentProvider} that takes a {@link Map} of values to show them in a combobox.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
final public class HashMapContentProvider<K, V> implements IStructuredContentProvider {
    @SuppressWarnings("unchecked")
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Map) {
            return ((Map<K, V>) inputElement).keySet().toArray();
        }
        return new Object[0];
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing.
    }

    @Override
    public void dispose() {
        // do nothing.
    }
}