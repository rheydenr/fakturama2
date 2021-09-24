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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.office.IPdfPostProcessor;
import com.sebulli.fakturama.office.TemplateFinder;
import com.sebulli.fakturama.office.TemplateProcessor;
import com.sebulli.fakturama.util.DocumentTypeUtil;
import com.sun.mail.util.MailConnectException;

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
 * The Mail Service class is an {@link IPdfPostProcessor} for sending mails
 * after successful creation of a PDF.
 */
@Component()
public class MailService implements IPdfPostProcessor {

    @Inject
    private IDocumentAddressManager addressManager;

    @Inject
    @Preference
    private IEclipsePreferences prefs;

    @Inject
    private IEclipseContext ctx;
    
    @Inject
    private ILogger log;

    @Inject
    private EModelService modelService;
    
    @Inject
    private EPartService partService;

    @Inject
    private MApplication application;

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    @Translation
    protected MailServiceMessages mailServiceMessages;

    @Override
    public boolean canProcess() {
        return prefs.getBoolean(MailServiceConstants.PREFERENCES_MAIL_ACTIVE, false);
    }

    @Override
    public boolean processPdf(Optional<Invoice> inputDocument) {
        if (!inputDocument.isPresent()) {
            return true;
        }

        DocumentReceiver billingAdress = addressManager.getBillingAdress(inputDocument.get());
        if(StringUtils.isAllBlank(billingAdress.getEmail())) {
            // ignore silently...
            return true;
        }
        

        // Collect some settings...
        MailSettings settings = createSettings(inputDocument.get());
        ctx.set(MailSettings.class, settings);
        
        // check settings
        if(!settings.isValid()) {
            MessageDialog.openError(ctx.get(Shell.class), msg.dialogMessageboxTitleError, mailServiceMessages.mailserviceSettingsInvalid);
            return false;
        }

        //... and open the mail dialog for examining the mail to send
        // (only if user wants to see the dialog)
        if (prefs.getBoolean("WANNA_SHOW_SENDMAIL_DIALOG", true)) {  // setting isn't available at the moment!!!
            ctx.set(MailService.class, this);
            
            MWindow mailAppDialog = (MWindow) modelService.find(MailServiceConstants.MAIL_APP_MAIN_WINDOW_ID, application);

            MPart mainPart = (MPart) mailAppDialog.getChildren().get(0);
            partService.showPart(mainPart.getElementId(), PartState.ACTIVATE);
            mainPart.setVisible(true);
            partService.bringToTop(mainPart);
            mailAppDialog.setOnTop(true);

            mailAppDialog.setVisible(true);
            mailAppDialog.setToBeRendered(true);
        } else {
            sendMail(settings);
        }
        return true;
    }

    private MailSettings createSettings(Invoice invoice) {

        TemplateProcessor templateProcessor = ContextInjectionFactory.make(TemplateProcessor.class, ctx);
        DocumentReceiver billingAdress = addressManager.getBillingAdress(invoice);
        String rcpBundleName = FrameworkUtil.getBundle(IPdfPostProcessor.class).getSymbolicName();
        String rcpBundlePrefsNodeName = String.format("/%s/%s", InstanceScope.SCOPE, rcpBundleName);

        MailSettings settings = new MailSettings()
                .withSender(prefs.node(rcpBundlePrefsNodeName).get(Constants.PREFERENCES_YOURCOMPANY_EMAIL, ""))
                .withUser(prefs.get(MailServiceConstants.PREFERENCES_MAIL_USER, "")) 
                .withPassword(prefs.get(MailServiceConstants.PREFERENCES_MAIL_PASSWORD, "")) 
                .withHost(prefs.get(MailServiceConstants.PREFERENCES_MAIL_HOST, ""))
                .withReceiversTo(billingAdress.getEmail())
                .withSubject(createMailSubject(invoice, templateProcessor));

        settings.setBody(createBodyFromTemplate(invoice, templateProcessor));
        
        List<String> additionalDocs = collectAdditionalDocs(invoice); 
        settings.addToAdditionalDocs(additionalDocs);
        return settings;
    }

    private List<String> collectAdditionalDocs(Invoice invoice) {
        List<String> retList = new ArrayList<>();
        
        // the PDF is always an attachment
        retList.add(invoice.getPdfPath());
        
        // optional documents found in additional path
        String additionalFilesPath = prefs.get(MailServiceConstants.PREFERENCES_MAIL_ADDITIONAL_DOCUMENTS_PATH, "");
        if(!additionalFilesPath.isBlank()) {
            Path templatePath1 = Paths.get(additionalFilesPath);
            List<String> templates = scanPathForadditionalFiles(templatePath1);
            retList.addAll(templates);
        }
        return retList;
    }
    
    /**
     * Scans the additional files path for all templates. If an additional file exists, add it
     * to the list of available additional files
     * 
     * @param additionalFilePath
     *            path which is scanned
     */
    private List<String> scanPathForadditionalFiles(Path additionalFilePath) {
        List<String> additionalFiles = new ArrayList<>();
        try {
            if(Files.exists(additionalFilePath)) {
                additionalFiles = Files.list(additionalFilePath)
                        .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error(e, "Error while scanning the additional files directory: " + additionalFilePath.toString());
        }
        return additionalFiles;
    }

    private String createMailSubject(Document invoice, TemplateProcessor templateProcessor) {
        String prefDescriptor;
        switch (invoice.getBillingType()) {
        case INVOICE:
            prefDescriptor = MailServiceConstants.PREFERENCES_MAIL_SUBJECT_INVOICE;
            break;
        case DELIVERY:
            prefDescriptor = MailServiceConstants.PREFERENCES_MAIL_SUBJECT_DELIVERY;
            break;
        case OFFER:
            prefDescriptor = MailServiceConstants.PREFERENCES_MAIL_SUBJECT_OFFER;
            break;
        case DUNNING:
            prefDescriptor = MailServiceConstants.PREFERENCES_MAIL_SUBJECT_DUNNING;
            break;
        case ORDER:
            prefDescriptor = MailServiceConstants.PREFERENCES_MAIL_SUBJECT_ORDER;
            break;
        case PROFORMA:
            prefDescriptor = MailServiceConstants.PREFERENCES_MAIL_SUBJECT_PROFORMA;
            break;
        case CREDIT:
            prefDescriptor = MailServiceConstants.PREFERENCES_MAIL_SUBJECT_CREDIT;
            break;
        default:
            prefDescriptor = "";
            break;
        }
        return templateProcessor.fill(invoice, Optional.empty(), 
                prefs.get(prefDescriptor, "<no subject>"));
    }

    private String createBodyFromTemplate(Document invoice, TemplateProcessor templateProcessor) {
        String templateString = "";
        TemplateFinder templateFinder = ContextInjectionFactory.make(TemplateFinder.class, ctx);
        List<Path> templates = templateFinder.collectTemplates(DocumentTypeUtil.findByBillingType(invoice.getBillingType()),
                TemplateFinder.TXT_TEMPLATE_FILEEXTENSION);

        if (templates != null && !templates.isEmpty()) {
            Path mailTemplatePath = templates.get(0);
            if (Files.exists(mailTemplatePath)) {
                try {
                    templateString = Files.readString(mailTemplatePath);
                } catch (IOException e) {
                    log.error(e, "mail template can't be processed: " + mailTemplatePath.getFileName());
                }
            }
        }

        return templateProcessor.fill(invoice, Optional.empty(), templateString);
    }

    public void sendMail(final MailSettings settings) {
        // create some properties and get the default Session
        Properties props = System.getProperties();
        props.put(MailServiceConstants.MAIL_SMTP_HOST, settings.getHost());
        props.put(MailServiceConstants.MAIL_SMTP_AUTH, "true");
        props.put(MailServiceConstants.MAIL_SMTP_STARTTLS_ENABLE, "true");
        props.put(MailServiceConstants.MAIL_SMTP_PORT, MailServiceConstants.MAIL_SMTP_DEFAULT_PORT);
     
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
            msg.setSender(new InternetAddress(settings.getSender()));
            
            msg.setRecipients(Message.RecipientType.TO, settings.getReceiversTo());
            msg.setRecipients(Message.RecipientType.CC,settings.getReceiversCC());
            msg.setRecipients(Message.RecipientType.BCC, settings.getReceiversBCC());
            
            msg.setSubject(settings.getSubject());

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();

            // create and fill the first message part
            // PLAIN TEXT
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(settings.getBody());
            mp.addBodyPart(messageBodyPart);
//
//            // HTML TEXT ==> future feature!
//            messageBodyPart = new MimeBodyPart();
//            String htmlText = settings.getBodyHtml();
//            messageBodyPart.setContent(htmlText, "text/html");
//            mp.addBodyPart(messageBodyPart);
            
            // add attachments
            settings.getAdditionalDocs().stream().map(this::createMimePart).forEach(p -> {
                try {
                    mp.addBodyPart(p);
                } catch (MessagingException e) {
                    log.error(e, "can't add mime body");
                }
            });

            // add the Multipart to the message
            msg.setContent(mp);

            // set the Date: header
            msg.setSentDate(new Date());

            CompletableFuture.runAsync(() -> {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                try {

                    // send the message
                    Transport.send(msg);
                } catch (final MailConnectException e) {
                  log.error(e, "can't connect to mail server ("+settings.getHost()+")");
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

    private void closeDialog() {
        Optional<MUIElement> mailAppDialog = Optional.ofNullable(modelService.find(MailServiceConstants.MAIL_APP_MAIN_WINDOW_ID, application));
        mailAppDialog.ifPresent(m -> {
            m.setVisible(false);
            m.setToBeRendered(false);
        });
    }

    private MimeBodyPart createMimePart(String file) {
        // create the next message part
        MimeBodyPart mbp3 = new MimeBodyPart();
        try {
            // attach the file to the message
            mbp3.attachFile(file);
        } catch (IOException | MessagingException ioex) {
            log.error(ioex, "can't create mime body part");
        }
        return mbp3;
    }
}
