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
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.osgi.service.component.annotations.Component;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.office.IPdfPostProcessor;

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

    public static final String MAIL_APP_MAIN_WINDOW_ID = "org.fakturama.connectors.mailapp";

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

    @Override
    public boolean canProcess() {
        return prefs.getBoolean(MailServiceConstants.PREFERENCES_MAIL_ACTIVE, false);
    }

    @Override
    public boolean processPdf(Optional<Invoice> inputDocument) {
        if (!inputDocument.isPresent())
            return true;

        // Collect some settings...
        MailSettings settings = createSettings(inputDocument.get());
        ctx.set(MailSettings.class, settings);

        //... and open the mail dialog for examining the mail to send
        // (only if user wants to see the dialog)
        if (prefs.getBoolean("WANNA_SHOW_SENDMAIL_DIALOG", true)) {
            ctx.set(MailService.class, this);
            
            MWindow mailAppDialog = (MWindow) modelService.find(MAIL_APP_MAIN_WINDOW_ID, application);

            MPart mainPart = (MPart) mailAppDialog.getChildren().get(0);
            partService.showPart(mainPart.getElementId(), PartState.ACTIVATE);
            mainPart.setVisible(true);
            partService.bringToTop(mainPart);

            mailAppDialog.setVisible(true);
            mailAppDialog.setToBeRendered(true);
        } else {
            sendMail(settings);
        }
        return true;
    }

    private MailSettings createSettings(Invoice invoice) {

        DocumentReceiver billingAdress = addressManager.getBillingAdress(invoice);

        MailSettings settings = new MailSettings()
                .withSender(prefs.node("/" + InstanceScope.SCOPE + "/com.sebulli.fakturama.rcp").get(Constants.PREFERENCES_YOURCOMPANY_EMAIL, ""))
                .withUser(prefs.get(MailServiceConstants.PREFERENCES_MAIL_USER, "a6251406eb8784"))
                .withPassword(prefs.get(MailServiceConstants.PREFERENCES_MAIL_PASSWORD, "f2a28eb950877f"))
                .withHost(prefs.get(MailServiceConstants.PREFERENCES_MAIL_HOST, "smtp.mailtrap.io"))
                .withReceiversTo(billingAdress.getEmail())
                .withReceiversCC("rheydenr@justmail.de")
                .withReceiversBCC("ralf.heydenreich@t-online.de")
                .withSubject("Your invoice No. " + invoice.getName());

        settings.setBody("nur mal so zum Testen");
        settings.addToAdditionalDocs("d:\\MeineDaten\\Dokumente\\", "Meins.txt");
        return settings;
    }
    

    public void sendMail(MailSettings settings) {
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
            msg.setSender(new InternetAddress(settings.getSender()));
            
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
            messageBodyPart.setText(settings.getBody());
            mp.addBodyPart(messageBodyPart);

            // HTML TEXT
            messageBodyPart = new MimeBodyPart();
            String htmlText = settings.getBodyHtml();
            messageBodyPart.setContent(htmlText, "text/html");
            mp.addBodyPart(messageBodyPart);
            
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

    private void closeDialog() {
        Optional<MUIElement> mailAppDialog = Optional.ofNullable(modelService.find(MailService.MAIL_APP_MAIN_WINDOW_ID, application));
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
