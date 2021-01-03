/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;

import com.sebulli.fakturama.authorization.AllowedFor;
import com.sebulli.fakturama.authorization.FakturamaRole;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;

/**
 * Handler class for deleting an item from a list.
 * 
 */
public class DeleteHandler {
    

    @Inject
    @Preference
    private IEclipsePreferences preferences;

	@CanExecute
	@AllowedFor(roles = { FakturamaRole.ADMIN, FakturamaRole.USER }) // experimental!!!
	public boolean isEnabled(@Active MPart activePart, ESelectionService selectionService) {
//		Method[] methods = MethodUtils.getMethodsWithAnnotation(getClass(), AllowedFor.class);
//		for (Method m : methods) {
//			AllowedFor a = m.getAnnotation(AllowedFor.class);
//			System.out.println("Methode:    " + m.getName());
//			System.out.println("roles()[0]: " + a.roles()[0]);
//			System.out.println("roles()[1]: " + a.roles()[1]);
//		}
		List<?> selection = (List<?>)selectionService.getSelection(activePart.getElementId());
		
		return activePart.getObject() instanceof AbstractViewDataTable
			&& selection != null && !selection.isEmpty() && !isDefault(selection);
	}

	/**
	 * Checks if any of the list entries is a default entry
	 * 
	 * @param selection
	 * @return
	 */
	private boolean isDefault(List<?> selection) {
	    for (Object object : selection) {
            if(object instanceof VAT
                    && ((IEntity)object).getId() == preferences.getLong(Constants.DEFAULT_VAT, 0)) {
                return true;
            }
            if(object instanceof Shipping
                    && ((IEntity)object).getId() == preferences.getLong(Constants.DEFAULT_SHIPPING, 0)) {
                return true;
            }
            if(object instanceof Payment
                    && ((IEntity)object).getId() == preferences.getLong(Constants.DEFAULT_PAYMENT, 0)) {
                return true;
            }
        }
        return false;
    }

    @Execute
	public void handleDelete(@Optional @Named("com.sebulli.fakturama.cmddelparam.objId") String objId,
			@Active MPart activePart, ESelectionService selectionService) {
		((AbstractViewDataTable<?, ?>) activePart.getObject()).removeSelectedEntry();
		selectionService.setSelection(null);
	}
}
