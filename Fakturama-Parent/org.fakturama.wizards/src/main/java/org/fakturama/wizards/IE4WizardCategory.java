package org.fakturama.wizards;

import org.eclipse.core.runtime.IPath;

/**
 * A wizard category may contain other categories or wizard elements. 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IE4WizardCategory {
	
	public void setLabel(String label);
	public void setId(String id);

	/**
	 * Returns the category child object corresponding to the passed path
	 * (relative to this object), or <code>null</code> if such an object could
	 * not be found.  The segments of this path should correspond to category ids.
	 * 
	 * @param path
	 *            the search path
	 * @return the category or <code>null</code>
	 */
	IE4WizardCategory findCategory(IPath path);

	/**
	 * Find a wizard that has the provided id. This will search recursivly over
	 * this categories children.
	 * 
	 * @param id
	 *            the id to search for
	 * @return the wizard or <code>null</code>
	 */
	IE4WizardDescriptor findWizard(String id);

	/**
	 * Return the immediate child categories.
	 * 
	 * @return the child categories. Never <code>null</code>.
	 */
	IE4WizardCategory[] getCategories();

	/**
	 * Return the identifier of this category.
	 * 
	 * @return the identifier of this category
	 */
	String getId();

	/**
	 * Return the label for this category.
	 * 
	 * @return the label for this category
	 */
	String getLabel();

	/**
	 * Return the parent category.
	 * 
	 * @return the parent category. May be <code>null</code>.
	 */
	IE4WizardCategory getParent();

	/**
	 * Return this wizards path. The segments of this path will correspond to
	 * category ids.
	 * 
	 * @return the path
	 */
	IPath getPath();

	/**
	 * Return the wizards in this category, minus the wizards which failed
	 * the Expressions check.
	 * 
	 * @return the wizards in this category. Never <code>null</code>
	 */
	IE4WizardDescriptor[] getWizards();
}
