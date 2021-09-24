/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.parts;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;

import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;

/**
 *
 */
public class FakturamaCoolbarAction extends Action {

    @Inject
    private EHandlerService handlerService;

    @Inject
    private EPartService partService;

    @Inject
    private ECommandService cmdService;

    @Inject
    private IEclipseContext ctx;

    private ParameterizedCommand pCmd;

    private ToolBar toolBar;
    private String defaultAction = null;
    
    /**
     * Identifier key for the "visible preference" for each of the items
     */
    private String visiblePreferenceId = null;

    public FakturamaCoolbarAction(ParameterizedCommand pCmd, ToolBar toolBar, int style) throws NotDefinedException {
        super(pCmd.getCommand().getName(), style);
        this.setId(pCmd.getId());
        this.pCmd = pCmd;
        this.toolBar = toolBar;
    }

    
    @Override
    public boolean isEnabled() {
        return handlerService.canExecute(pCmd);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        throw new UnsupportedOperationException("enabled state is got from given command.");
    }

    @Override
    public void runWithEvent(Event event) {

        ParameterizedCommand parameterizedCommand = pCmd;
        if (handlerService.canExecute(parameterizedCommand)) {
            final IEclipseContext staticContext = EclipseContextFactory.create("fakturama-static-context");
            if (getDefaultAction() != null) {
                // default action if clicked directly on the button
                    Map<String, Object> params = new HashMap<>();
                    params.put(CallEditor.PARAM_EDITOR_TYPE, ((DropdownMenuCreator)getMenuCreator()).getDefaultCommandId());
                    params.put(CallEditor.PARAM_FORCE_NEW, BooleanUtils.toStringTrueFalse(true));
                    parameterizedCommand = cmdService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
            }

            // if CTRL key is pressed then we try to duplicate the current editor into a new one
            if ((event.stateMask & SWT.MOD1) == SWT.MOD1) {
                // does only work under certain circumstances
                ParameterizedCommand duplicateCmd = cmdService.createCommand(CommandIds.CMD_OBJECT_DUPLICATE);
                if (handlerService.canExecute(duplicateCmd)) {
                    MPart activePart = partService.getActivePart();
                    // item has to correspond to the active editor!
                    if (activePart != null && activePart.getObject() instanceof Editor) {
                        String editorType = (String) parameterizedCommand.getParameterMap().get(CallEditor.PARAM_EDITOR_TYPE);
                        if (editorType != null && activePart.getElementId().equalsIgnoreCase(editorType)) {
                            staticContext.set(CallEditor.PARAM_COPY, Boolean.TRUE);
                            staticContext.set(CallEditor.PARAM_FORCE_NEW, Boolean.FALSE);
                            staticContext.set(CallEditor.PARAM_OBJ_ID, activePart.getTransientData().get(CallEditor.PARAM_OBJ_ID));
                        }
                    }
                }
            } else {
                // if called from CoolBar it is *always* a new one...
                staticContext.set(CallEditor.PARAM_FORCE_NEW, Boolean.TRUE);
                // NOTE: You can't set it if it was set before. Therefore we set it here.
            }
            /*
             * Dirty hack. The HandlerService first determines the active leaf in the
             * current context before it is executing the command. The current active leaf
             * in the context is the document editor. But in its context there's a setting
             * for the "category" parameter. But this parameter is not reasonable e.g. for
             * new product editors. Therefore we have to remove it from context. The
             * alternative would be to set another active leaf, but I didn't get it.
             */
            if (ctx != null && ctx.getActiveLeaf() != null) {
                ctx.getActiveLeaf().remove(CallEditor.PARAM_CATEGORY);
            }

            // clear SelectionService so that following calls don't get confused (esp. CallEditor)
            // Important: Use the correct SelectionService from WorkbenchContext!
            ctx.set(IServiceConstants.ACTIVE_SELECTION, null);
            handlerService.executeHandler(parameterizedCommand, staticContext);
        } else {
            MessageDialog.openInformation(toolBar.getShell(), "Action Info", "current action can't be executed!");
        }
    }


    public String getDefaultAction() {
        return defaultAction;
    }


    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }


    public String getVisiblePreferenceId() {
        return visiblePreferenceId;
    }


    public void setVisiblePreferenceId(String visiblePreferenceId) {
        this.visiblePreferenceId = visiblePreferenceId;
    }

}
