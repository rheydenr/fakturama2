package org.fakturama.wizards.internal.dialogs;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.fakturama.wizards.activities.WorkbenchActivityHelper;
import org.fakturama.wizards.internal.AdaptableList;

/**
 * Viewer filter designed to work with the new wizard page (and its input/content provider).
 * This will filter all non-primary wizards that are not enabled by activity.
 * 
 * @since 3.0
 */
public class WizardActivityFilter extends ViewerFilter {
	private boolean filterPrimaryWizards = false;

	public void setFilterPrimaryWizards(boolean filter) {
		filterPrimaryWizards = filter;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
                .getContentProvider()).getChildren(element);
        if (children.length > 0) {
			return filter(viewer, element, children).length > 0;
		}

		if (parentElement.getClass().equals(AdaptableList.class) && !filterPrimaryWizards) {
			return true; //top-level ("primary") wizards should always be returned
		}

        if (WorkbenchActivityHelper.filterItem(element)) {
			return false;
		}

        return true;
    }
}
