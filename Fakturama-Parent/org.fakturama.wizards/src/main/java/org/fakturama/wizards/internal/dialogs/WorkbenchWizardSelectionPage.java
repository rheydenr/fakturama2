package org.fakturama.wizards.internal.dialogs;

import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.fakturama.wizards.internal.AdaptableList;

import com.sebulli.fakturama.ui.dialogs.WorkbenchMessages;

/**
 * Page for selecting a wizard from a group of available wizards.
 */
public abstract class WorkbenchWizardSelectionPage extends WizardSelectionPage {

    // variables
    protected IWorkbench workbench;

    protected AdaptableList wizardElements;

    public TableViewer wizardSelectionViewer;

    protected IStructuredSelection currentResourceSelection;
    
    protected String triggerPointId;

    /**
     *	Create an instance of this class
     */
    public WorkbenchWizardSelectionPage(String name, IWorkbench aWorkbench,
            IStructuredSelection currentSelection, AdaptableList elements, 
            String triggerPointId) {
        super(name);
        this.wizardElements = elements;
        this.currentResourceSelection = currentSelection;
        this.workbench = aWorkbench;
        this.triggerPointId = triggerPointId;
        setTitle(WorkbenchMessages.Select);
    }

    /**
     *	Answer the wizard object corresponding to the passed id, or null
     *	if such an object could not be found
     *
     *	@return WizardElement
     *	@param searchId the id to search on
     */
    protected WorkbenchWizardElement findWizard(String searchId) {
        Object[] children = wizardElements.getChildren();
        for (int i = 0; i < children.length; ++i) {
            WorkbenchWizardElement currentWizard = (WorkbenchWizardElement) children[i];
            if (currentWizard.getId().equals(searchId)) {
				return currentWizard;
			}
        }

        return null;
    }

    public IStructuredSelection getCurrentResourceSelection() {
        return this.currentResourceSelection;
    }

    public IWorkbench getWorkbench() {
        return this.workbench;
    }

    /**
     *	Specify the passed wizard node as being selected, meaning that if
     *	it's non-null then the wizard to be displayed when the user next
     *	presses the Next button should be determined by asking the passed
     *	node.
     *
     *	@param node org.eclipse.jface.wizards.IWizardNode
     */
    public void selectWizardNode(IWizardNode node) {
        setSelectedNode(node);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
     */
//    public IWizardPage getNextPage() { 
//        ITriggerPoint triggerPoint = getWorkbench().getActivitySupport()
//        .getTriggerPointManager().getTriggerPoint(triggerPointId);
//        if (triggerPoint == null || WorkbenchActivityHelper.allowUseOf(triggerPoint, getSelectedNode())) {
//			return super.getNextPage();
//		}
//        return null;
//    }
}
