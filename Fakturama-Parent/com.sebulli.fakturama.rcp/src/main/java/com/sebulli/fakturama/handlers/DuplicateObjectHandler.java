
package com.sebulli.fakturama.handlers;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.parts.ProductEditor;

public class DuplicateObjectHandler {

	@Inject
	@Translation
	protected Messages msg;

	@Inject
	@Optional
	private IPreferenceStore preferences;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private ECommandService commandService;

	@CanExecute
	public boolean canExecute(@Optional @Active MPart activePart) {
		// at the moment it only works for Product Editor
		return activePart != null && ProductEditor.ID.equalsIgnoreCase(activePart.getElementId());
	}

	@Execute
	public void execute(@Optional @Active MPart activePart) {
		Map<String, Object> params = new HashMap<>();
		params.put(CallEditor.PARAM_EDITOR_TYPE, activePart.getElementId());
		params.put(CallEditor.PARAM_COPY, Boolean.TRUE);
		params.put(CallEditor.PARAM_OBJ_ID, activePart.getTransientData().get(CallEditor.PARAM_OBJ_ID));
		ParameterizedCommand pCmdCopy = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
		if (handlerService.canExecute(pCmdCopy)) {
			handlerService.executeHandler(pCmdCopy);
		}
	}

}