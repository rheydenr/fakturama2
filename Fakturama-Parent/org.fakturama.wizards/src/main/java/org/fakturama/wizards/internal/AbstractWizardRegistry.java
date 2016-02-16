package org.fakturama.wizards.internal;

import org.fakturama.wizards.IE4WizardCategory;
import org.fakturama.wizards.IE4WizardDescriptor;
import org.fakturama.wizards.IWizardRegistry;
import org.fakturama.wizards.internal.dialogs.WizardCollectionElement;
import org.fakturama.wizards.internal.dialogs.WorkbenchWizardElement;

/**
 * Abstract base class for various workbench wizards.
 * 
 * @since 3.1
 */
public abstract class AbstractWizardRegistry implements IWizardRegistry {

	private boolean initialized = false;

	private WorkbenchWizardElement[] primaryWizards;

	private WizardCollectionElement wizardElements;

	/**
	 * Create a new instance of this class.
	 */
	public AbstractWizardRegistry() {
		super();
	}
	
	/**
	 * Dispose of this registry.
	 */
	public void dispose() {
		primaryWizards = null;
		wizardElements = null;
		initialized = false;
	}

	/**
	 * Perform initialization of this registry. Should never be called by
	 * implementations. 
	 */
	protected abstract void doInitialize();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardRegistry#findCategory(java.lang.String)
	 */
	public IE4WizardCategory findCategory(String id) {
		initialize();
		return wizardElements.findCategory(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardRegistry#findWizard(java.lang.String)
	 */
	public IE4WizardDescriptor findWizard(String id) {
		initialize();
		return wizardElements.findWizard(id, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardRegistry#getPrimaryWizards()
	 */
	public IE4WizardDescriptor[] getPrimaryWizards() {
		initialize();
		return primaryWizards;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardRegistry#getRootCategory()
	 */
	public IE4WizardCategory getRootCategory() {
		initialize();
		return wizardElements;
	}

	/**
	 * Return the wizard elements.
	 * 
	 * @return the wizard elements
	 */
	protected WizardCollectionElement getWizardElements() {
		initialize();
		return wizardElements;
	}

	/**
	 * Read the contents of the registry if necessary.
	 */
	protected final synchronized void initialize() {
		if (isInitialized()) {
			return;
		}

		initialized = true;
		doInitialize();
	}

	/**
	 * Return whether the registry has been read.
	 * 
	 * @return whether the registry has been read
	 */
	private boolean isInitialized() {
		return initialized;
	}

	/**
	 * Set the primary wizards.
	 * 
	 * @param primaryWizards
	 *            the primary wizards
	 */
	protected void setPrimaryWizards(WorkbenchWizardElement[] primaryWizards) {
		this.primaryWizards = primaryWizards;
	}

	/**
	 * Set the wizard elements.
	 * 
	 * @param wizardElements
	 *            the wizard elements
	 */
	protected void setWizardElements(WizardCollectionElement wizardElements) {
		this.wizardElements = wizardElements;
	}
}
