/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package com.sebulli.fakturama.views.datatable.common;


import org.eclipse.nebula.widgets.nattable.selection.action.RowSelectionDragMode;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectRowAction;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionBindings;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class NoHeaderRowOnlySelectionBindings extends DefaultSelectionBindings {

	@Override
	protected void configureBodyMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
		IMouseAction action = new SelectRowAction();
		uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), action);
		uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), action);
		uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.CTRL), action);
		uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.SHIFT | SWT.MOD1), action);
	}

	@Override
	protected void configureBodyMouseDragMode(UiBindingRegistry uiBindingRegistry) {
		IDragMode dragMode = new RowSelectionDragMode();
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.NONE), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.MOD1), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT | SWT.MOD1), dragMode);
	}
	
//	protected void configureColumnHeaderMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
//		//do nothing
//	}
}
