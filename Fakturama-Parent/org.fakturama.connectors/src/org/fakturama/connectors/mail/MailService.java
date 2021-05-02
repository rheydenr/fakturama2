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

import org.osgi.service.component.annotations.Component;

import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.office.IPdfPostProcessor;

/**
 * The Mail Service class is an {@link IPdfPostProcessor} for sending mails
 * after successful creation of a PDF. 
 */
@Component()
public class MailService implements IPdfPostProcessor {

    @Override
    public boolean canProcess() {
        return true;
    }

    @Override
    public boolean processPdf(Optional<Invoice> inputDocument) {
        return false;
    }
}
