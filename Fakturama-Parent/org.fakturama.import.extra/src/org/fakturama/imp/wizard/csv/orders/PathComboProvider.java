/**
 * 
 */
package org.fakturama.imp.wizard.csv.orders;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 *
 */
public class PathComboProvider implements IStructuredContentProvider {
    @SuppressWarnings("unchecked")
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
            return ((List<Path>) inputElement).toArray();
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
