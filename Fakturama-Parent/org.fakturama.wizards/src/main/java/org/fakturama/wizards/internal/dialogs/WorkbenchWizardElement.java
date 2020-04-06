package org.fakturama.wizards.internal.dialogs;

import javax.inject.Inject;

import org.eclipse.core.runtime.Adapters;

/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fair Isaac Corporation <Hemant.Singh@Gmail.com> - http://bugs.eclipse.org/326695
 *******************************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;
import org.fakturama.wizards.Activator;
import org.fakturama.wizards.IE4WizardCategory;
import org.fakturama.wizards.IE4WizardDescriptor;
import org.fakturama.wizards.IWorkbenchWizard;
import org.fakturama.wizards.internal.dialogs.model.WorkbenchAdapter;
import org.fakturama.wizards.internal.registry.RegistryReader;

import com.sebulli.fakturama.ui.dialogs.model.IWorkbenchAdapter;
import com.sebulli.fakturama.ui.dialogs.registry.IWorkbenchRegistryConstants;

/**
 * Instances represent registered wizards.
 */
public class WorkbenchWizardElement extends WorkbenchAdapter implements
        IAdaptable, IE4WizardDescriptor {
    private String id;
    
    private ImageDescriptor imageDescriptor;

//    private SelectionEnabler selectionEnabler;
    
    @Inject
    private ESelectionService selectionService;

    private IConfigurationElement configurationElement;

    private Image descriptionImage;
    
    private WizardCollectionElement parentCategory;
    
	/**
	 * TODO: DO we need to  make this API?
	 */
	public static final String TAG_PROJECT = "project"; //$NON-NLS-1$

	private static final String [] EMPTY_TAGS = new String[0];

	private static final String [] PROJECT_TAGS = new String[] {TAG_PROJECT};

	private String[] keywordLabels;
    
    /**
     * Create a new instance of this class
     * 
     * @param configurationElement
     * @since 3.1
     */
    public WorkbenchWizardElement(IConfigurationElement configurationElement) {
        this.configurationElement = configurationElement;
        id = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
    }

    /**
     * Answer a boolean indicating whether the receiver is able to handle the
     * passed selection
     * 
     * @return boolean
     * @param selection
     *            IStructuredSelection
     */
    public boolean canHandleSelection(IStructuredSelection selection) {
    	// FIXME
        return true; //getSelectionEnabler().isEnabledForSelection(selection);
    }

    public IStructuredSelection adaptedSelection(IStructuredSelection selection) {
        if (canHandleSelection(selection)) {
			return selection;
		}

        IStructuredSelection adaptedSelection = convertToResources(selection);
        if (canHandleSelection(adaptedSelection)) {
			return adaptedSelection;
		}

        //Couldn't find one that works so just return
        return StructuredSelection.EMPTY;
    }

    /**
     * Create an the instance of the object described by the configuration
     * element. That is, create the instance of the class the isv supplied in
     * the extension point.
     * @return the new object
     * @throws CoreException 
     */
    public IWizard createExecutableExtension() throws CoreException {
        return (IWizard) configurationElement.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
    }

    /**
     * Returns an object which is an instance of the given class associated
     * with this object. Returns <code>null</code> if no such object can be
     * found.
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class) {
            return this;
        }
//        else if (adapter == IPluginContribution.class) {
//        	return this;
//        }
        else if (adapter == IConfigurationElement.class) {
        	return configurationElement;
        }
        return Adapters.adapt(this, adapter);
    }

    /**
     * @return IConfigurationElement
     */
    public IConfigurationElement getConfigurationElement() {
        return configurationElement;
    }

    /**
     * Answer the description parameter of this element
     * 
     * @return java.lang.String
     */
    public String getDescription() {
        return RegistryReader.getDescription(configurationElement);
    }

    /**
     * Answer the icon of this element.
     */
    public ImageDescriptor getImageDescriptor() {
    	if (imageDescriptor == null) {
    		String iconName = configurationElement
                    .getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
	        if (iconName == null) {
				return null;
			}
	       imageDescriptor = Activator.getDefault().imageDescriptorFromPlugin(configurationElement.getNamespaceIdentifier(), iconName);
    	}
        return imageDescriptor;
    }

    /**
     * Returns the name of this wizard element.
     */
    public ImageDescriptor getImageDescriptor(Object element) {
        return getImageDescriptor();
    }
    
    /**
     * Returns the name of this wizard element.
     */
    public String getLabel(Object element) {
        return configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
    }
//
//    /**
//     * Answer self's action enabler, creating it first iff necessary
//     */
//    protected SelectionEnabler getSelectionEnabler() {
//        if (selectionEnabler == null) {
//			selectionEnabler = new SelectionEnabler(configurationElement);
//		}
//
//        return selectionEnabler;
//    }

    /**
     * Attempt to convert the elements in the passed selection into resources
     * by asking each for its IResource property (if it isn't already a
     * resource). If all elements in the initial selection can be converted to
     * resources then answer a new selection containing these resources;
     * otherwise answer an empty selection.
     * 
     * @param originalSelection the original selection
     * @return the converted selection or an empty selection
     */
	private IStructuredSelection convertToResources(
			IStructuredSelection originalSelection) {
		if (selectionService == null || originalSelection == null) {
			return StructuredSelection.EMPTY;
		}
		return StructuredSelection.EMPTY;/* ((ISelectionConversionService) selectionService)
				.convertToResources(originalSelection);*/
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
    public String getLocalId() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return (configurationElement != null) ? configurationElement
                .getNamespaceIdentifier() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.INewWizardDescriptor#getDescriptionImage()
     */
    public Image getDescriptionImage() {
    	if (descriptionImage == null) {
    		String descImage = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_DESCRIPTION_IMAGE);
    		if (descImage == null) {
				return null;
			}
    		//FIXME
            descriptionImage = null/*AbstractUIPlugin.imageDescriptorFromPlugin(
                    configurationElement.getNamespaceIdentifier(), descImage)*/;
    	}
        return descriptionImage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.INewWizardDescriptor#getHelpHref()
     */
    public String getHelpHref() {
        return configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_HELP_HREF);
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.INewWizardDescriptor#createWizard()
	 */
	public IWorkbenchWizard createWizard() throws CoreException {
		return (IWorkbenchWizard) createExecutableExtension();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartDescriptor#getLabel()
	 */
	public String getLabel() {		
		return getLabel(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.INewWizardDescriptor#getCategory()
	 */
	public IE4WizardCategory getCategory() {
		return (IE4WizardCategory) getParent(this);
	}
	
	/**
	 * Return the collection.
	 * 
	 * @return the collection
	 * @since 3.1
	 */
	public WizardCollectionElement getCollectionElement() {
		return (WizardCollectionElement) getParent(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getTags()
	 */
	public String [] getTags() {
 
        String flag = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_PROJECT);
        if (Boolean.valueOf(flag).booleanValue()) {
        	return PROJECT_TAGS;
        }
        
        return EMPTY_TAGS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object object) {
		return parentCategory;
	}

	/**
	 * Set the parent category.
	 * 
	 * @param parent the parent category
	 * @since 3.1
	 */
	public void setParent(WizardCollectionElement parent) {
		parentCategory = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#canFinishEarly()
	 */
	public boolean canFinishEarly() {
		return Boolean.valueOf(configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_CAN_FINISH_EARLY)).booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#hasPages()
	 */
	public boolean hasPages() {
		String hasPagesString = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_HAS_PAGES);
		// default value is true
		if (hasPagesString == null) {
			return true;
		}
		return Boolean.valueOf(hasPagesString).booleanValue();
	}

	public String[] getKeywordLabels() {
		if (keywordLabels == null) {

			IConfigurationElement[] children = configurationElement
					.getChildren(IWorkbenchRegistryConstants.TAG_KEYWORD_REFERENCE);
			keywordLabels = new String[children.length];
//			KeywordRegistry registry = KeywordRegistry.getInstance();
//			for (int i = 0; i < children.length; i++) {
//				String id = children[i]
//						.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
//				keywordLabels[i] = registry.getKeywordLabel(id);
//			}
		}
		return keywordLabels;
	}
	
	
	public String getName() {
		return "meins!";
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCategory(IE4WizardCategory category) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDescriptionImage(Image descriptionImage) {
		this.descriptionImage = descriptionImage;
	}

}
