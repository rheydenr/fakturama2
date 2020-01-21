/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.ui.dialogs.model;

import org.eclipse.jface.resource.ImageDescriptor;

import com.sebulli.fakturama.ui.dialogs.model.IWorkbenchAdapter;

/**
 *
 */
public class WorkbenchAdapter implements IWorkbenchAdapter {
    /**
     * The empty list of children.
     */
    protected static final Object[] NO_CHILDREN = new Object[0];

    /**
     * The default implementation of this <code>IWorkbenchAdapter</code> method
     * returns the empty list. Subclasses may override.
     */
    public Object[] getChildren(Object object) {
        return NO_CHILDREN;
    }

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.wizards.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
        return null;
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.wizards.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
        return object == null ? "" : object.toString(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.wizards.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object o) {
        return null;
	}

}
