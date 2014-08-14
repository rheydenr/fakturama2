/**
 * 
 */
package com.sebulli.fakturama.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.views.datatable.vats.VATListTable;

/**
 * Handler class for deleting an item from a list.
 *
 */
public class DeleteHandler {
    @Inject
    @Translation
    protected Messages msg;

    @Execute
    public void handleDelete(@Optional @Named("com.sebulli.fakturama.cmddelparam.objId") String objId,
            @Active MPart activePart) {
        activePart.getObject();
        ((VATListTable)activePart.getObject()).removeSelectedEntry();
    }
}
