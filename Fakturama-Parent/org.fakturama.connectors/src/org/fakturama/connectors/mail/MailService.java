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

import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.osgi.service.component.annotations.Component;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.office.IPdfPostProcessor;

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
        MWindow mailAppDialog = (MWindow) modelService.find("org.fakturama.connectors.mailapp", application);

        MPart mainPart = (MPart) mailAppDialog.getChildren().get(0);
        partService.showPart(mainPart.getElementId(), PartState.ACTIVATE);
        partService.bringToTop(mainPart);
        
        mailAppDialog.setVisible(true);
        mailAppDialog.setToBeRendered(true);
        return true;
    }

    private MailSettings createSettings(Invoice invoice) {

        DocumentReceiver billingAdress = addressManager.getBillingAdress(invoice);

        MailSettings settings = new MailSettings()
                .withSender(prefs.node("/" + InstanceScope.SCOPE + "/com.sebulli.fakturama.rcp").get(Constants.PREFERENCES_YOURCOMPANY_EMAIL, ""))
                .withUser(prefs.get(MailServiceConstants.PREFERENCES_MAIL_USER, "")).withPassword(prefs.get(MailServiceConstants.PREFERENCES_MAIL_PASSWORD, ""))
                .withHost(prefs.get(MailServiceConstants.PREFERENCES_MAIL_HOST, "")).withReceiversTo(billingAdress.getEmail())
                .withReceiversCC("c.c.fritz@erde.de")
                .withReceiversBCC("b.c.c.ben@funkytown.de")
                .withSubject("Your invoice No. " + invoice.getName());

        settings.setBody("nur mal so zum Testen");
        settings.addToAdditionalDocs("C:\\Dokumente\\meins.txt");
        return settings;
    }
}
