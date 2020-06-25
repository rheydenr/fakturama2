/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Ralf Heydenreich - initial API and implementation
 */
package org.fakturama.export.einvoice;

import java.util.Optional;

import com.sebulli.fakturama.model.Document;

public interface IEinvoiceCreator {

    /**
     * Create an e-Invoice based on invoice document and {@link ConformanceLevel}. 
     * A PDF is created in the location which is given by invoice document. 
     *  
     * @param invoice an invoice document
     * @param zugferdProfile the {@link ConformanceLevel} of the resulting file
     * @return <code>true</code>, if the conversion was successful
     */
    boolean createEInvoice(Optional<Document> invoice, ConformanceLevel zugferdProfile);

}
