package com.sebulli.fakturama.wizards.internal.dialog;

import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.viewers.Viewer;
import org.fakturama.wizards.internal.dialogs.WizardCollectionElement;
import org.fakturama.wizards.internal.dialogs.WorkbenchWizardElement;

/**
 * A class that handles filtering wizard node items based on a supplied matching
 * string and keywords
 * 
 * @since 3.2
 * 
 */
public class WizardPatternFilter extends PatternFilter {

	/**
	 * Create a new instance of a WizardPatternFilter 
	 * @param isMatchItem
	 */
	public WizardPatternFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	public boolean isElementSelectable(Object element) {
		return element instanceof WorkbenchWizardElement;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (element instanceof WizardCollectionElement) {
			return false;
		}
		
		if (element instanceof WorkbenchWizardElement) {
			WorkbenchWizardElement desc = (WorkbenchWizardElement) element;
			String text = desc.getLabel();
			if (wordMatches(text)) {
				return true;
			}

			String[] keywordLabels = desc.getKeywordLabels();
			for (int i = 0; i < keywordLabels.length; i++) {
				if (wordMatches(keywordLabels[i]))
					return true;
			}
		}
		return false;
	}

}
