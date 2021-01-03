/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package org.fakturama.export.einvoice;

import java.io.Serializable;
import java.util.Optional;

import com.sebulli.fakturama.model.Invoice;

/**
 *
 */
public interface IEinvoice {

    public Serializable getInvoiceXml(Optional<Invoice> invoiceDoc);

}
