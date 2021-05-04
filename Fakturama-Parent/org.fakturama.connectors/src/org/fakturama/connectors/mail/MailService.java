/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package org.fakturama.connectors.mail;

import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.jface.preference.PreferenceNode;
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
    
    @Override
    public boolean canProcess() {
        return prefs.getBoolean(MailServiceConstants.PREFERENCES_MAIL_ACTIVE, false);
    }

    @Override
    public boolean processPdf(Optional<Invoice> inputDocument) {
        if(!inputDocument.isPresent())
            return true;
        
        // Collect some settings...
        MailSettings settings = createSettings(inputDocument.get());
        ctx.set(MailSettings.class, settings);
        
        //... and open the mail dialog for examining the mail to send
        MailInfoDialog dlg = null;
        dlg = ContextInjectionFactory.make(MailInfoDialog.class, ctx);
//        dlg.setDialogBoundsSettings(getDialogSettings("MailInfoDialog"), Dialog.DIALOG_PERSISTSIZE | Dialog.DIALOG_PERSISTLOCATION);
        dlg.open();
        
        return true;
    }

    private MailSettings createSettings(Invoice invoice) {
        
        DocumentReceiver billingAdress = addressManager.getBillingAdress(invoice);
        
        MailSettings settings = new MailSettings()
                .withSender(prefs.node("/" + InstanceScope.SCOPE + "/com.sebulli.fakturama.rcp").get(Constants.PREFERENCES_YOURCOMPANY_EMAIL, ""))
                .withUser(prefs.get(MailServiceConstants.PREFERENCES_MAIL_USER, ""))
                .withPassword(prefs.get(MailServiceConstants.PREFERENCES_MAIL_USER, ""))
                .withHost(prefs.get(MailServiceConstants.PREFERENCES_MAIL_HOST, ""))
                .withReceiversTo(billingAdress.getEmail());
        
        settings.setBody("nur mal so zum Testen");
        settings.addToAdditionalDocs("C:\\Dokumente\\meins.txt");
        return settings;
    }
}
