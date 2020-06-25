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
 
package com.sebulli.fakturama.office;

import java.util.Optional;

import com.sebulli.fakturama.model.Document;

/**
 * Defines an interface for post processing of the generated PDFs. This is useful e.g. for
 * creating XRechnung (ZUGFeRD) invoices, where the generated PDF from LibreOffice has to 
 * converted to a given format.
 */
public interface IPdfPostProcessor {
    
    /**
     * Checks the service if it can be run.
     * 
     * @return <code>true</code>, if the processing can be started
     */
    public boolean canProcess();
    
    /**
     * Process the PDF.
     * 
     * @param inputDocument {@link Document} which is the base for the PDF
     * @return <code>true</code>, if the operation was successful
     */
    public boolean processPdf(Optional<Document> inputDocument);
}
