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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

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

import com.sebulli.fakturama.log.ILogger;

import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

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
    private ILogger log;

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
                .onSelect(t -> sendMail())
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
        MUIElement mailAppDialog = modelService.find(MailService.MAIL_APP_MAIN_WINDOW_ID, application);
        mailAppDialog.setVisible(false);
        mailAppDialog.setToBeRendered(false);
    }

    private void sendMail() {
        // create some properties and get the default Session
        Properties props = System.getProperties();
        props.put("mail.smtp.host", settings.getHost());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");
     
        Authenticator authenticator = new Authenticator() {
            final PasswordAuthentication authentication = new PasswordAuthentication(settings.getUser(), settings.getPassword());

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authentication;
            }
        };
        Session session = Session.getInstance(props, authenticator);
//        session.setDebug(debug);
        
        try {
            // create a message
            MimeMessage msg = new MimeMessage(session);
            //set From email field
            msg.setFrom(new InternetAddress(settings.getSender()));
            msg.setSender(new InternetAddress("ich@erde.de"));
            
            msg.setRecipients(Message.RecipientType.TO, settings.getReceiversTo());
            msg.setRecipients(Message.RecipientType.CC,settings.getReceiversCC());
            msg.setRecipients(Message.RecipientType.BCC, settings.getReceiversBCC());
            
            msg.setSubject(settings.getSubject());

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(settings.getBody());

            
            /*
             * Use the following approach instead of the above line if
             * you want to control the MIME type of the attached file.
             * Normally you should never need to do this.
             *
            FileDataSource fds = new FileDataSource(filename) {
            public String getContentType() {
                return "application/octet-stream";
            }
            };
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName(fds.getName());
             */

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
//            mp.addBodyPart(mbp1);

            // PLAIN TEXT
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Here is your plain text message");
            mp.addBodyPart(messageBodyPart);

            // HTML TEXT
            messageBodyPart = new MimeBodyPart();
            String htmlText = "<H1>I am the html part</H1>";
            messageBodyPart.setContent(htmlText, "text/html");
            mp.addBodyPart(messageBodyPart);
            
            // add attachments
            settings.getAdditionalDocs().stream().map(this::createMimePart).forEach(p -> {
                try {
                    mp.addBodyPart(p);
                } catch (MessagingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });

            // add the Multipart to the message
            msg.setContent(mp);

            // set the Date: header
            msg.setSentDate(new Date());

            /*
             * If you want to control the Content-Transfer-Encoding
             * of the attached file, do the following. Normally you
             * should never need to do this.
             *
            msg.saveChanges();
            mbp2.setHeader("Content-Transfer-Encoding", "base64");
             */
         
            CompletableFuture.runAsync(() -> {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                try {

                    // send the message
                    Transport.send(msg);
                } catch (final MessagingException e) {
                  log.error(e, "can't send mail");
                }
              }, Executors.newSingleThreadExecutor());           

        } catch (MessagingException mex) {
            log.error(mex, "can't send mail");
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
                log.error(ex, "can't send mail");
            }
        } finally {
            closeDialog();
        }
    }
    
    private MimeBodyPart createMimePart(String file) {
        // create the next message part
        MimeBodyPart mbp3 = new MimeBodyPart();
        try {
            // attach the file to the message
            mbp3.attachFile(file);
        } catch (IOException | MessagingException ioex) {
            ioex.printStackTrace();
        }
        return mbp3;
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
