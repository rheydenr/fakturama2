package org.fakturama.export.wizard.contacts;

import org.eclipse.jface.wizard.IWizard;
import org.fakturama.export.AbstractWizardNode;

public class AddressListExportWizardNode extends AbstractWizardNode {
    /**
     * a descriptive label of the wizard
     */
    private String name;
    private String category = "org.fakturama.export.contacts";
    
    /**
     * This is the wizard that this IWizardNode represents. One reason to
     * keep this reference is because we can check if the wizard is created at the
     * isContentCreated method.
     */
    IWizard wizard;
    
    public AddressListExportWizardNode(String name) {
        this.name = name;
    }
    
    @Override
    public IWizard getWizard() {
        if(wizard == null) {
            wizard = new AddressListExportWizard();
        }
        
        return wizard;
    }

    /**
     * Returns whether a wizard has been created for this node.
     */
    @Override
    public boolean isContentCreated() {
        return wizard != null;
    }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCategory() {
		return category;
	}
}