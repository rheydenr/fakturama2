/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package org.fakturama.connectors.mail;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *
 */
public class MailInfoDialog extends TrayDialog {
    private DataBindingContext bindingContext = new DataBindingContext();
    private Text receiverTo, receiverCC, receiverBCC, subject, body;
    private ListViewer listViewer;

    @Inject
    private MailSettings settings;

    @Inject
    public MailInfoDialog(@Active Shell shell) {
        super(shell);
    }

    @Override
    protected void configureShell(Shell parent) {
        super.configureShell(parent);
        parent.setText("Mail Service");
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);

        Composite top = CompositeFactory.newComposite(SWT.BORDER).layout(GridLayoutFactory.fillDefaults().numColumns(2).create())
                .layoutData(GridDataFactory.fillDefaults().create()).create(parent);

        LabelFactory.newLabel(SWT.NONE).text("send via").create(top);
        LabelFactory.newLabel(SWT.NONE).text(settings.getHost()).create(top);

        LabelFactory.newLabel(SWT.NONE).text("send from").create(top);
        LabelFactory.newLabel(SWT.NONE).text(settings.getSender()).create(top);

        LabelFactory.newLabel(SWT.NONE).text("to").create(top);
        receiverTo = TextFactory.newText(SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create()).create(top);

        LabelFactory.newLabel(SWT.NONE).text("cc").create(top);
        receiverCC = TextFactory.newText(SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create()).create(top);

        LabelFactory.newLabel(SWT.NONE).text("bcc").create(top);
        receiverBCC = TextFactory.newText(SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create()).create(top);

        LabelFactory.newLabel(SWT.NONE).text("subject").create(top);
        subject = TextFactory.newText(SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create()).create(top);

        body = TextFactory.newText(SWT.BORDER | SWT.WRAP).layoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, true).minSize(80, 100).create())
                .create(top);

        Composite attachmentPanel = CompositeFactory.newComposite(SWT.BORDER).layoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, true).create())
                .layout(GridLayoutFactory.fillDefaults().numColumns(2).create()).create(top);
        addAttachmentListViewer(attachmentPanel);
        addButtons(attachmentPanel);

        bindFields();

        return parent;
    }

    @Override
    protected void okPressed() {
        //        sendMail()
        super.okPressed();
    }

    private void bindFields() {
        IObservableFactory<Control, IObservableList<String>> listFactory = WidgetProperties.items().listFactory();
        IObservableValue<String> rec = WidgetProperties.text(SWT.FocusOut).observe(receiverTo);
        IObservableValue<String> receiversTo = PojoProperties.value(MailSettings.class, "receiversTo", String.class).observe(settings);

        IObservableValue<String> recCC = WidgetProperties.text(SWT.FocusOut).observe(receiverCC);
        IObservableValue<String> receiversCC = PojoProperties.value(MailSettings.class, "receiversCC", String.class).observe(settings);

        IObservableValue<String> recBCC = WidgetProperties.text(SWT.FocusOut).observe(receiverBCC);
        IObservableValue<String> receiversBCC = PojoProperties.value(MailSettings.class, "receiversBCC", String.class).observe(settings);

        IObservableValue<String> subj = WidgetProperties.text(SWT.FocusOut).observe(subject);
        IObservableValue<String> subjString = PojoProperties.value(MailSettings.class, "subject", String.class).observe(settings);

        IObservableValue<String> bodyWidget = WidgetProperties.text(SWT.FocusOut).observe(body);
        IObservableValue<String> bodyString = PojoProperties.value(MailSettings.class, "body", String.class).observe(settings);

        IObservableList<String> attachmentList = listFactory.createObservable(listViewer.getControl());
//        IObservableList<String> attachmentList = WidgetProperties.items().observe(listViewer.getControl());
        IObservableList<String> att = PojoProperties.list(MailSettings.class, "additionalDocs", String.class).observe(settings);
        
        bindingContext.bindValue(rec, receiversTo);
        //        bindingContext.bindValue(recCC, receiversCC);
        //        bindingContext.bindValue(recBCC, receiversBCC);
        bindingContext.bindValue(subj, subjString);
        bindingContext.bindValue(bodyWidget, bodyString);
        
        bindingContext.bindList(attachmentList, att);

    }

    //  Vector<String> languages = new Vector<>();

    private void addAttachmentListViewer(Composite top) {
        listViewer = new ListViewer(top, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        listViewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                @SuppressWarnings("unchecked")
                List<String> v = (ArrayList<String>) inputElement;
                return v.toArray();
            }
        });

        listViewer.setInput(settings.getAdditionalDocs());

        listViewer.setLabelProvider(new LabelProvider() {
            public Image getImage(Object element) {
                return null;
            }

            public String getText(Object element) {
                return (String) element;
            }
        });

        listViewer.setComparator(new ViewerComparator() {
            public int compare(Viewer viewer, Object e1, Object e2) {
                return ((String) e1).compareTo(((String) e2));
            }

        });

        GridDataFactory.fillDefaults().grab(true, true).applyTo(listViewer.getList());

    }

    Button buttonAdd;
    Button buttonRemove;

    private void addButtons(Composite top) {
        Composite composite = new Composite(top, SWT.NULL);
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        fillLayout.spacing = 2;

        composite.setLayout(fillLayout);

        buttonAdd = new Button(composite, SWT.PUSH);
        buttonAdd.setText("Add");

        buttonRemove = new Button(composite, SWT.PUSH);
        buttonRemove.setText("Remove");

        buttonAdd.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputDialog dlg = new InputDialog(getShell(), "title", "msg", "default", new IInputValidator() {

                    @Override
                    public String isValid(String newText) {
                        // TODO Auto-generated method stub
                        return null;
                    }
                });

                dlg.open();

                String text = dlg.getValue();
                if (text != null) {
                    settings.addToAdditionalDocs(text);
                }

                listViewer.refresh(false);
            }
        });

        buttonRemove.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
                String language = (String) selection.getFirstElement();
                if (language == null) {
                    System.out.println("Please select an item first.");
                    return;
                }

                settings.removeFromAdditionalDocs(language);
                listViewer.refresh(false);
            }
        });
    }

}
