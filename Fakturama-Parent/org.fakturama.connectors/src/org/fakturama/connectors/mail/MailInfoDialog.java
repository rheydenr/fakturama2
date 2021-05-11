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
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *
 */
public class MailInfoDialog {
    private DataBindingContext bindingContext = new DataBindingContext();
    private Text receiverTo, receiverCC, receiverBCC, subject, body;
    private ListViewer listViewer;
    private Shell shell;

    @Inject
    private EModelService modelService;

    @Inject
    private MApplication application;

    @Inject
    private MailSettings settings;
    
    @Inject
    private MailService mailService;

    @PostConstruct
    protected Control createDialogArea(@Active Shell shell, Composite parent) {
        this.shell = shell;

        Composite top = CompositeFactory.newComposite(SWT.BORDER).layout(GridLayoutFactory.fillDefaults().margins(10, 10).numColumns(2).create())
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

        Composite attachmentPanel = CompositeFactory.newComposite(SWT.BORDER)
                .layoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, true).create())
                .layout(GridLayoutFactory.fillDefaults().numColumns(2).create()).create(top);
        
        addAttachmentListViewer(attachmentPanel);
        addButtons(attachmentPanel);
        
        Composite bottomPanel = CompositeFactory.newComposite(SWT.NONE)
                .layoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create())
                .layout(GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(true).create())
                .create(top);
        LabelFactory.newLabel(SWT.NONE)
            .layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
            .create(bottomPanel); // invisible label
        LabelFactory.newLabel(SWT.NONE)
            .layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
            .create(bottomPanel); // invisible label
        
        Composite buttonPanel = CompositeFactory.newComposite(SWT.NONE)
                .layoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.END, SWT.FILL).create())
                .layout(new FillLayout())
                .create(bottomPanel);

        ButtonFactory.newButton(SWT.PUSH)
                .layoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).create())
                .text("Send")
                .onSelect(t -> mailService.sendMail(settings))
                .create(buttonPanel);

        ButtonFactory.newButton(SWT.PUSH)
                .layoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).create())
                .text("Cancel")
                .onSelect(t -> {
                    closeDialog();
                })
                .create(buttonPanel);

        bindFields();

        return parent;
    }

    private void closeDialog() {
        Optional<MUIElement> mailAppDialog = Optional.ofNullable(modelService.find(MailService.MAIL_APP_MAIN_WINDOW_ID, application));
        mailAppDialog.ifPresent(m -> {
            m.setVisible(false);
            m.setToBeRendered(false);
        });
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
        IObservableList<String> att = PojoProperties.list(MailSettings.class, "additionalDocs", String.class).observe(settings);

        bindingContext.bindValue(rec, receiversTo);
        bindingContext.bindValue(recCC, receiversCC);
        bindingContext.bindValue(recBCC, receiversBCC);
        bindingContext.bindValue(subj, subjString);
        bindingContext.bindValue(bodyWidget, bodyString);

        bindingContext.bindList(attachmentList, att);

    }

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

        GridDataFactory.fillDefaults().grab(true, true).applyTo(listViewer.getList());

    }

    private void addButtons(Composite top) {
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        fillLayout.spacing = 2;

        Composite composite = CompositeFactory.newComposite(SWT.NULL).layout(fillLayout).create(top);

        ButtonFactory.newButton(SWT.PUSH).text("Add").onSelect(t -> {
            FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
            fileDialog.setText("Please select an attachment for the mail");

            fileDialog.open();

            String[] text = fileDialog.getFileNames();
            if (text != null) {
                settings.addToAdditionalDocs(fileDialog.getFilterPath(), text);
            }

            listViewer.refresh(false);
        }).create(composite);

        ButtonFactory.newButton(SWT.PUSH).text("Remove").onSelect(t -> {
            IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
            String language = (String) selection.getFirstElement();
            if (language == null) {
                MessageDialog.openInformation(shell, "Info", "Please select an item first.");
                return;
            }

            settings.removeFromAdditionalDocs(language);
            listViewer.refresh(false);
        }).create(composite);
    }
}
