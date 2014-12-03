/**
 * 
 */
package com.sebulli.fakturama.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable;

/**
 * Handler class for deleting an item from a list.
 *
 */
public class DeleteHandler {

    @CanExecute
    public boolean isEnabled(@Active MPart activePart) {
    	return activePart.getObject() instanceof AbstractViewDataTable;
    }

    @SuppressWarnings("rawtypes")  // here we  don't need a generic...
    @Execute
    public void handleDelete(@Optional @Named("com.sebulli.fakturama.cmddelparam.objId") String objId,
            @Active MPart activePart) {
        ((AbstractViewDataTable)activePart.getObject()).removeSelectedEntry();
    }
}
