package com.sebulli.fakturama.dialogs;

import java.util.Collection;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.SWTRenderersMessages;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SaveablePartPromptDialog extends Dialog {

	private Collection<MPart> collection;

	private CheckboxTableViewer tableViewer;
	private IEclipseContext context;

	private Object[] checkedElements = new Object[0];

	public SaveablePartPromptDialog(Shell shell, Collection<MPart> collection, IEclipseContext context) {
		super(shell);
		this.collection = collection;
		this.context = context;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(SWTRenderersMessages.choosePartsToSaveTitle);
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		parent = (Composite) super.createDialogArea(parent);

		Label label = new Label(parent, SWT.LEAD);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label.setText(SWTRenderersMessages.choosePartsToSave);

		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 250;
		data.widthHint = 300;
		tableViewer.getControl().setLayoutData(data);
		tableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((MPart) element).getLocalizedLabel();
			}
		});
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(collection);
		tableViewer.setAllChecked(true);

		return parent;
	}

	@Override
	public void create() {
		super.create();
		applyDialogStyles(getShell());
	}

	@Override
	protected void okPressed() {
		checkedElements = tableViewer.getCheckedElements();
		super.okPressed();
	}

	public Object[] getCheckedElements() {
		return checkedElements;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void applyDialogStyles(Control control) {
		IStylingEngine engine = (IStylingEngine) context
				.get(IStylingEngine.SERVICE_NAME);
		if (engine != null) {
			Shell shell = control.getShell();
			if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
				shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
			}

			engine.style(shell);
		}
	}

}
