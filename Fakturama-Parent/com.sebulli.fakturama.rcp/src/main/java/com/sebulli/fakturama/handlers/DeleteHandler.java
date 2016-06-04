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

import com.sebulli.fakturama.authorization.AllowedFor;
import com.sebulli.fakturama.authorization.FakturamaRole;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;

/**
 * Handler class for deleting an item from a list.
 *
 */
public class DeleteHandler {

	@CanExecute
	@AllowedFor(roles = { FakturamaRole.ADMIN, FakturamaRole.USER }) // experimental!!!
	public boolean isEnabled(@Active MPart activePart) {
//		Method[] methods = MethodUtils.getMethodsWithAnnotation(getClass(), AllowedFor.class);
//		for (Method m : methods) {
//			AllowedFor a = m.getAnnotation(AllowedFor.class);
//			System.out.println("Methode:    " + m.getName());
//			System.out.println("roles()[0]: " + a.roles()[0]);
//			System.out.println("roles()[1]: " + a.roles()[1]);
//		}
		return activePart.getObject() instanceof AbstractViewDataTable 
			|| activePart.getObject() instanceof DocumentsListTable;
	}

	@SuppressWarnings("rawtypes") // here we don't need a generic...
	@Execute
	public void handleDelete(@Optional @Named("com.sebulli.fakturama.cmddelparam.objId") String objId,
			@Active MPart activePart) {
		((AbstractViewDataTable) activePart.getObject()).removeSelectedEntry();
	}
}
